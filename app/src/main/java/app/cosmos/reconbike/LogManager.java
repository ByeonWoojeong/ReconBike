package app.cosmos.reconbike;

import android.util.Log;


public class LogManager {
    private static final String TAG = "LOG";
    private static final boolean DEBUG = true;
    static void print(String msg){
        Log.v(TAG,msg);
    }
}
