package own.ownwatchfacetext;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Settings {

    public static final String KEY_CLOCK_SIZE = "clock_size";
    public static final int KEY_CLOCK_SIZE_DEF = 26;
    public static final String KEY_CLOCK_ACT = "clock_act";
    public static final boolean KEY_CLOCK_ACT_DEF = true;
    public static final String KEY_CLOCK_DIM = "clock_dim";
    public static final boolean KEY_CLOCK_DIM_DEF = false;
    public static final String KEY_CLOCK_NOSECS_SIZE = "clock_nosecs_size";
    public static final int KEY_CLOCK_NOSECS_SIZE_DEF = 43;
    public static final String KEY_CLOCK_NOSECS_ACT = "clock_nosecs_act";
    public static final boolean KEY_CLOCK_NOSECS_ACT_DEF = false;
    public static final String KEY_CLOCK_NOSECS_DIM = "clock_nosecs_dim";
    public static final boolean KEY_CLOCK_NOSECS_DIM_DEF = true;
    public static final String KEY_MARKER_SIZE = "marker_size";
    public static final int KEY_MARKER_SIZE_DEF = 18;
    public static final String KEY_MARKER_DIM = "marker_dim";
    public static final boolean KEY_MARKER_DIM_DEF = true;
    public static final String KEY_DATE_SIZE = "date_size";
    public static final int KEY_DATE_SIZE_DEF = 18;
    public static final String KEY_DATE_DIM = "date_dim";
    public static final boolean KEY_DATE_DIM_DEF = true;
    public static final String KEY_TIME_SIZE = "time_size";
    public static final int KEY_TIME_SIZE_DEF = 18;
    public static final String KEY_TIME_DIM = "time_dim";
    public static final boolean KEY_TIME_DIM_DEF = false;
    public static final String KEY_ALWAYS_UTC = "always_utc";
    public static final boolean KEY_ALWAYS_UTC_DEF = true;
    public static final String KEY_SHOW_TIME = "show_time";
    public static final boolean KEY_SHOW_TIME_DEF = true;
    public static final String KEY_NORTHERNHEMI = "northernhemi";    
	public static final boolean KEY_NORTHERNHEMI_DEF = true;
    public static final String KEY_USE_SHORT_CARDS = "use_short_cards";
    public static final boolean KEY_USE_SHORT_CARDS_DEF = false;
    public static final String KEY_LAT = "lat";
    public static final String KEY_LAT_DEF = "0.0";
    public static final String KEY_LON = "lon";
    public static final String KEY_LON_DEF = "0.0";
    public static final String KEY_TEMP = "temp";
    public static final String KEY_TEMP_DEF = "103°F";
    public static final String KEY_ICON = "icon";
    public static final String KEY_ICON_DEF = "skc";
    public static final String KEY_WEATHER = "weather";
    public static final String KEY_WEATHER_DEF = "Clear";
    public static final String KEY_TEMP_SIZE = "temp_size";
    public static final int KEY_TEMP_SIZE_DEF = 30;
    public static final String KEY_TEMP_DIM = "temp_dim";
    public static final boolean KEY_TEMP_DIM_DEF = false;
    public static final String KEY_WEATHER_SIZE = "weather_size";
    public static final int KEY_WEATHER_SIZE_DEF = 13;
    public static final String KEY_WEATHER_DIM = "weather_dim";
    public static final boolean KEY_WEATHER_DIM_DEF = false;
    public static final String PATH_WEATHER_REQUIRE = "/WeatherService/Require";
    public static final String PATH_WITH_FEATURE = "/OwnWatchFaceNWSTEXT";



    public static String getString(final Context context, final String key, final String defaultValue) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(key, defaultValue);
    }

    public static int getInt(final Context context, final String key, final int defaultValue) {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(key, defaultValue);
    }

    public static boolean getBoolean(final Context context, final String key,
            final boolean defaultValue) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(key, defaultValue);
    }

    public static void setString(final Context context, final String key, final String value) {
        final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        final SharedPreferences.Editor editor = settings.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static void setInt(final Context context, final String key, final int value) {
        final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        final SharedPreferences.Editor editor = settings.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public static void setBoolean(final Context context, final String key, final boolean value) {
        final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        final SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public static void resetAllPrefs(final Context context) {
        final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        final SharedPreferences.Editor editor = settings.edit();

        editor.clear();
        editor.commit();
    }

}
