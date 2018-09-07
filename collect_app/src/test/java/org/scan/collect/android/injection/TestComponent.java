package org.scan.collect.android.injection;

import android.app.Application;

import org.scan.collect.android.injection.config.AppComponent;
import org.scan.collect.android.injection.config.scopes.PerApplication;
import org.scan.collect.android.sms.SmsSenderJobTest;
import org.scan.collect.android.sms.SmsServiceTest;

import dagger.BindsInstance;
import dagger.Component;
import dagger.android.support.AndroidSupportInjectionModule;

@PerApplication
@Component(modules = {
        AndroidSupportInjectionModule.class,
        TestModule.class,
        ActivityBuilder.class
})
public interface TestComponent extends AppComponent {
    @Component.Builder
    interface Builder {

        @BindsInstance
        TestComponent.Builder application(Application application);

        TestComponent build();
    }

    void inject(SmsSenderJobTest smsSenderJobTest);

    void inject(SmsServiceTest smsServiceTest);
}
