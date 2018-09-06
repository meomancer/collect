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

package org.odk.collect.android.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TextInputEditText;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import org.json.JSONException;
import org.json.JSONObject;
import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.http.HttpHeadResult;
import org.odk.collect.android.http.OpenRosaHttpInterface;
import org.odk.collect.android.preferences.PreferenceKeys;
import org.odk.collect.android.utilities.WebCredentialsUtils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

import javax.inject.Inject;

import timber.log.Timber;

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

    @Inject
    WebCredentialsUtils webCredentialsUtils;

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
        loginButton = findViewById(R.id.login);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

                //or set the values.
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(PreferenceKeys.KEY_USERNAME, usernameEditText.getText().toString()); //This is just an example, you could also put boolean, long, int or floats
                editor.commit();

                Collect.getInstance().getActivityLogger()
                        .logAction(this, "login", "click");
                Intent i = new Intent(getApplicationContext(),
                        MainMenuActivity.class);
                startActivity(i);
                finish();

                URI uri = URI.create(webCredentialsUtils.getServerFromPreferences());**/
                new Login().execute("https://kc.kobotoolbox.org/formList");
            }
        });
    }

    private String HttpPost(String myUrl) throws IOException, JSONException {

        URL url = new URL(myUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        Log.i(LoginActivity.class.toString(), conn.getRequestMethod());
        JSONObject jsonObject = buidJsonObject();
        setPostRequestContent(conn, jsonObject);
        conn.connect();
        Log.i(LoginActivity.class.toString(), conn.getResponseCode()+"");
        return conn.getResponseMessage() + "";
    }

    private JSONObject buidJsonObject() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.accumulate("username", usernameEditText.getText().toString());
        jsonObject.accumulate("password", passwordEditText.getText().toString());
        return jsonObject;
    }

    private void setPostRequestContent(
            HttpURLConnection conn, JSONObject jsonObject) throws IOException {
        OutputStream os = conn.getOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
        writer.write(jsonObject.toString());
        Log.i(LoginActivity.class.toString(), jsonObject.toString());
        writer.flush();
        writer.close();
        os.close();
    }


    private class Login extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            // params comes from the execute() call: params[0] is the url.
            try {
                try {
                    return HttpPost(urls[0]);
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
            Log.i(LoginActivity.class.toString(), result);
        }
    }

}