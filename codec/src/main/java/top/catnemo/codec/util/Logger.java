package top.catnemo.codec.util;

import android.util.Log;
import top.catnemo.codec.BuildConfig;

/**
 *
 * @author matrixJoy
 * @description
 * @date 2019-11-12   10:57 
 */
public class Logger {

    private static boolean isDebug = BuildConfig.DEBUG;

    public static void v(String tag, String msg) {
        if (isDebug) {
            Log.v(tag, msg);
        }
    }

    public static void d(String tag, String msg) {
        if (isDebug) {
            Log.d(tag, msg);
        }
    }


    public static void w(String tag, String msg) {
        if (isDebug) {
            Log.w(tag, msg);
        }
    }

    public static void e(String tag, String msg) {
        if (isDebug) {
            Log.e(tag, msg);
        }
    }

    public static void e(String tag, String msg, Throwable throwable) {
        if (isDebug) {
            Log.e(tag, msg, throwable);
        }
    }

}
