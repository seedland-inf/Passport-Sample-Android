package cc.seedland.inf.samples;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import cc.seedland.inf.passport.PassportHome;
import cc.seedland.inf.passport.stat.IStatEngine;
import cc.seedland.inf.passport.stat.PassportStatAgent;

/**
 * Created by xuchunlei on 2017/11/22.
 */

public class SampleApplication extends Application {

    public static Context CONTEXT;

    @Override
    public void onCreate() {
        super.onCreate();
//        PassportHome.getInstance().init(this, getString(R.string.channel), getString(R.string.key));
        PassportHome.init(this);
        PassportHome.enableTokenUpdate(true);
        CONTEXT = this;

        IStatEngine engine = new IStatEngine() {
            @Override
            public void onClickEvent(String eventId) {
                Log.e("stat", "click " + eventId + " launched");
            }

            @Override
            public void onPageEvent(String pageId) {
                Log.e("stat", "page " + pageId + " launched");
            }
        };
        PassportStatAgent.Builder builder = new PassportStatAgent.Builder(engine);
        builder.loginPassword("b050116")
                .loginCaptcha("b050117")
                .register("b050119")
                .passwordReset("b050120")
                .registerPerform("b050121")
                .passwordModifyPerform("b050055")
                .loginPasswordPage("p020002")
                .loginCaptchaPage("p020051")
                .passwordResetPage("p020003")
                .passwordModifyPage("p020004")
                .registerPage("p020001");

        builder.build();
    }
}
