package com.tapdevs.myapp;

import android.app.Application;
import android.content.Context;

import com.tapdevs.myapp.injections.component.DaggerNetComponent;
import com.tapdevs.myapp.injections.component.NetComponent;
import com.tapdevs.myapp.injections.modules.AppModule;
import com.tapdevs.myapp.injections.modules.NetModule;
import com.tapdevs.myapp.utils.AppConstants;

import io.realm.Realm;
import timber.log.Timber;

/**
 * Created by  Jan Shair on 30/01/2017.
 */

public class MyApp extends Application {
    private NetComponent mNetComponent;
//    ApplicationComponent mApplicationComponent;



    @Override
    public void onCreate() {
        super.onCreate();
        mNetComponent = DaggerNetComponent.builder()
                // list of modules that are part of this component need to be created here too
                .appModule(new AppModule(this)) // This also corresponds to the name of your module: %component_name%Module
                .netModule(new NetModule(AppConstants.SERVER_URL))
                .build();

        if (BuildConfig.DEBUG) Timber.plant(new Timber.DebugTree());

    }

    public NetComponent getNetComponent() {
        return mNetComponent;
    }

    public static MyApp get(Context context) {
        return (MyApp) context.getApplicationContext();
    }

}
