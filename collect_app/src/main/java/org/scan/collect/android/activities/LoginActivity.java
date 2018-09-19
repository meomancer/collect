/*
 * Copyright (C) 2009 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.scan.collect.android.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TextInputEditText;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import org.json.JSONException;
import org.scan.collect.android.R;
import org.scan.collect.android.application.Collect;
import org.scan.collect.android.preferences.PreferenceKeys;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Responsible for displaying buttons to launch the major activities. Launches
 * some activities based on returns of others.
 *
 * @author Irwan Fathurrahman (meomancer@gmail.com)
 */
public class LoginActivity extends CollectAbstractActivity {
    private Button loginButton;
    private TextInputEditText usernameEditText;
    private TextInputEditText passwordEditText;
    private TextView errorAuth;

    public static void startActivityAndCloseAllOthers(Activity activity) {
        activity.startActivity(new Intent(activity, LoginActivity.class));
        activity.overridePendingTransition(0, 0);
        activity.finishAffinity();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        usernameEditText = findViewById(R.id.username_edit);
        passwordEditText = findViewById(R.id.password_edit);
        errorAuth = findViewById(R.id.errorAuth);
        loginButton = findViewById(R.id.login);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Boolean rememberMePref = preferences.getBoolean(PreferenceKeys.KEY_REMEMBER_ME, false);
        String username = preferences.getString(PreferenceKeys.KEY_USERNAME, "");
        String password = preferences.getString(PreferenceKeys.KEY_PASSWORD, "");
        // Redirect into main menu if remember me and has username
        if (rememberMePref && !username.isEmpty()) {
            new LoginCheck(username, password).goToMenu();
        }


        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                errorAuth.setVisibility(View.GONE);
                String usernameInEdit = usernameEditText.getText().toString();
                String passwordInEdit = passwordEditText.getText().toString();
                if (usernameInEdit.equals(username) && passwordInEdit.equals(password)) {
                    new LoginCheck(username, password).goToMenu();
                } else {
                    new LoginCheck(usernameInEdit, passwordInEdit).execute(
                            getString(R.string.default_server_url) + "/formList");
                }
            }
        });
        {
            // dynamically construct the "ODK Collect vA.B" string
            TextView mainMenuMessageLabel = findViewById(R.id.main_menu_header);
            mainMenuMessageLabel.setText(Collect.getInstance()
                    .getVersionedAppName());
        }
    }

    private String HttpLogin(String myUrl, String username, String password) throws IOException, JSONException {
        URL url = new URL(myUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setUseCaches(false);
        byte[] usernamePassword = (username + ":" + password).getBytes();
        byte[] basicAuth = Base64.encode(usernamePassword, Base64.DEFAULT);
        String text = new String(basicAuth, "UTF-8");
        conn.setRequestProperty("Authorization", "basic " + text);
        Log.i(LoginActivity.class.toString(), conn.getResponseCode() + "");
        conn.connect();

        int responseCode = conn.getResponseCode();
        String result = "Not OK";
        if (responseCode == 200) {
            result = "OK";
        } else if (responseCode == 401) {
            InputStream errorstream = conn.getErrorStream();
            if (errorstream != null) {
                BufferedReader br = new BufferedReader(new InputStreamReader(errorstream));
                String response = "";
                String tempString;
                while ((tempString = br.readLine()) != null) {
                    response += tempString;
                }
                Log.i(LoginActivity.class.toString(), response);
                boolean isFound = response.indexOf("Using basic authentication without HTTPS") != -1 ? true : false;
                if (isFound) {
                    result = "OK";
                }
            }
        }
        return result;
    }

    private class LoginCheck extends AsyncTask<String, Void, String> {
        String username;
        String password;

        public LoginCheck(String inputUsername, String inputPassword) {
            username = inputUsername;
            password = inputPassword;
        }

        public void goToMenu() {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

            //or set the values.
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(PreferenceKeys.KEY_USERNAME, username);
            editor.putString(PreferenceKeys.KEY_PASSWORD, password);
            editor.putString(
                    PreferenceKeys.KEY_SERVER_URL,
                    getString(R.string.default_server_url)
            );
            editor.putBoolean(PreferenceKeys.KEY_REMEMBER_ME, true);
            editor.commit();

            Collect.getInstance().getActivityLogger()
                    .logAction(this, "login", "click");
            Intent i = new Intent(getApplicationContext(),
                    MainMenuActivity.class);
            startActivity(i);
            finish();
        }

        @Override
        protected String doInBackground(String... urls) {
            // params comes from the execute() call: params[0] is the url.
            try {
                try {
                    return HttpLogin(urls[0], username, password);
                } catch (JSONException e) {
                    e.printStackTrace();
                    return "Error!";
                }
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result.equals("OK")) {
                this.goToMenu();
            } else {
                errorAuth.setVisibility(View.VISIBLE);
            }
            Log.i(LoginActivity.class.toString(), result);
        }
    }

}