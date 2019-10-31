package app.cosmos.reconbike;

import android.app.Activity;
import android.support.multidex.MultiDexApplication;
import android.view.Window;
import android.view.WindowManager;

import com.kakao.auth.KakaoSDK;
import com.tsengvn.typekit.Typekit;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class GlobalApplication extends MultiDexApplication {

    private static GlobalApplication mInstance;
    private static volatile Activity currentActivity = null;
    public static ApplicationLifecycleHandler applicationLifecycleHandler;
    public static boolean getOtherLogin = false;

    public static Activity getCurrentActivity() {
        LogManager.print("GlobalApplication : " +(currentActivity != null ? currentActivity.getClass().getSimpleName() : ""));
        return currentActivity;
    }

    public static void setCurrentActivity(Activity currentActivity) {
        GlobalApplication.currentActivity = currentActivity;
    }

    public static boolean setXiaomiDarkMode(Activity activity) {
        Class<? extends Window> clazz = activity.getWindow().getClass();
        try {
            int darkModeFlag = 0;
            Class<?> layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
            Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
            darkModeFlag = field.getInt(layoutParams);
            Method extraFlagField = clazz.getMethod("setExtraFlags", int.class, int.class);
            extraFlagField.invoke(activity.getWindow(), darkModeFlag, darkModeFlag);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean setMeizuDarkMode(Activity activity) {
        boolean result = false;
        try {
            WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
            Field darkFlag = WindowManager.LayoutParams.class.getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON");
            Field meizuFlags = WindowManager.LayoutParams.class.getDeclaredField("meizuFlags");
            darkFlag.setAccessible(true);
            meizuFlags.setAccessible(true);
            int bit = darkFlag.getInt(null);
            int value = meizuFlags.getInt(lp);
            value |= bit;
            meizuFlags.setInt(lp, value);
            activity.getWindow().setAttributes(lp);
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static GlobalApplication getGlobalApplicationContext() {
        if (mInstance == null) {
            throw new IllegalStateException("this application does not inherit GlobalApplication");
        }
        return mInstance;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        mInstance = null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        KakaoSDK.init(new KakaoSDKAdapter());
        Typekit.getInstance().addCustom1(Typekit.createFromAsset(this, "NotoSans-Medium.otf"));
        Typekit.getInstance().addCustom2(Typekit.createFromAsset(this, "NotoSans-Regular.otf"));
        Typekit.getInstance().addCustom3(Typekit.createFromAsset(this, "NotoSans-Bold.otf"));
        Typekit.getInstance().addCustom4(Typekit.createFromAsset(this, "Roboto-Medium.ttf"));
        Typekit.getInstance().addCustom5(Typekit.createFromAsset(this, "Roboto-Regular.ttf"));
        Typekit.getInstance().addCustom6(Typekit.createFromAsset(this, "Roboto-Bold.ttf"));
        applicationLifecycleHandler = new ApplicationLifecycleHandler();
        registerActivityLifecycleCallbacks(applicationLifecycleHandler);
        registerComponentCallbacks(applicationLifecycleHandler);
    }
}