package org.scan.collect.android.injection.config;

import android.app.Application;

import org.scan.collect.android.activities.InstanceUploaderList;
import org.scan.collect.android.adapters.InstanceUploaderAdapter;
import org.scan.collect.android.application.Collect;
import org.scan.collect.android.fragments.DataManagerList;
import org.scan.collect.android.injection.ActivityBuilder;
import org.scan.collect.android.injection.config.scopes.PerApplication;
import org.scan.collect.android.tasks.sms.SmsSentBroadcastReceiver;
import org.scan.collect.android.tasks.sms.SmsNotificationReceiver;
import org.scan.collect.android.tasks.sms.SmsSender;
import org.scan.collect.android.tasks.sms.SmsService;

import dagger.BindsInstance;
import dagger.Component;
import dagger.android.support.AndroidSupportInjectionModule;

/**
 * Primary module, bootstraps the injection system and
 * injects the main Collect instance here.
 * <p>
 * Shouldn't be modified unless absolutely necessary.
 */
@PerApplication
@Component(modules = {
        AndroidSupportInjectionModule.class,
        AppModule.class,
        ActivityBuilder.class
})
public interface AppComponent {

    @Component.Builder
    interface Builder {

        @BindsInstance
        Builder application(Application application);

        AppComponent build();
    }

    void inject(Collect collect);

    void inject(SmsService smsService);

    void inject(SmsSender smsSender);

    void inject(SmsSentBroadcastReceiver smsSentBroadcastReceiver);

    void inject(SmsNotificationReceiver smsNotificationReceiver);

    void inject(InstanceUploaderList instanceUploaderList);

    void inject(InstanceUploaderAdapter instanceUploaderAdapter);

    void inject(DataManagerList dataManagerList);
}
