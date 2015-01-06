package cards.herscher.cardmaster;

import android.util.Log;

public final class Logger
{
    private final static String MASTER_TAG = "CardMaster_";

    public static void e(String tag, String message)
    {
        Log.e(MASTER_TAG + tag, message);
    }

    public static void e(String tag, String fmt, Object... messages)
    {
        Log.e(MASTER_TAG + tag, String.format(fmt, messages));
    }
    
    public static void w(String tag, String message)
    {
        Log.w(MASTER_TAG + tag, message);
    }

    public static void w(String tag, String fmt, Object... messages)
    {
        Log.w(MASTER_TAG + tag, String.format(fmt, messages));
    }
    
    public static void i(String tag, String message)
    {
        Log.i(MASTER_TAG + tag, message);
    }

    public static void i(String tag, String fmt, Object... messages)
    {
        Log.i(MASTER_TAG + tag, String.format(fmt, messages));
    }
    
    public static void v(String tag, String message)
    {
        Log.v(MASTER_TAG + tag, message);
    }

    public static void v(String tag, String fmt, Object... messages)
    {
        Log.v(MASTER_TAG + tag, String.format(fmt, messages));
    }
    
    public static void d(String tag, String message)
    {
        Log.d(MASTER_TAG + tag, message);
    }

    public static void d(String tag, String fmt, Object... messages)
    {
        Log.d(MASTER_TAG + tag, String.format(fmt, messages));
    }
}
