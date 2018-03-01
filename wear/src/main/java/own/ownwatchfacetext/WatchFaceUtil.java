package own.ownwatchfacetext;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

public final class WatchFaceUtil {

    private static final String TAG = WatchFaceUtil.class.getSimpleName();
    public static final String KEY_CLOCK_SIZE = "clock_size";
    public static final int KEY_CLOCK_SIZE_DEF = 26;
    public static final String KEY_CLOCK_ACT = "clock_act";
    public static final boolean KEY_CLOCK_ACT_DEF = true;
    public static final String KEY_CLOCK_DIM = "clock_dim";
    public static final boolean KEY_CLOCK_DIM_DEF = true;
    public static final String KEY_CLOCK_NOSECS_SIZE = "clock_nosecs_size";
    public static final int KEY_CLOCK_NOSECS_SIZE_DEF = 43;
    public static final String KEY_CLOCK_NOSECS_ACT = "clock_nosecs_act";
    public static final boolean KEY_CLOCK_NOSECS_ACT_DEF = false;
    public static final String KEY_CLOCK_NOSECS_DIM = "clock_nosecs_dim";
    public static final boolean KEY_CLOCK_NOSECS_DIM_DEF = false;
    public static final String KEY_MARKER_SIZE = "marker_size";
    public static final int KEY_MARKER_SIZE_DEF = 20;
    public static final String KEY_MARKER_DIM = "marker_dim";
    public static final boolean KEY_MARKER_DIM_DEF = true;
    public static final String KEY_DATE_SIZE = "date_size";
    public static final int KEY_DATE_SIZE_DEF = 20;
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

    // Callback interface to perform an action with the current config DataMap
    public interface FetchConfigDataMapCallback {

        // Callback invoked with the current config DataMap
        void onConfigDataMapFetched(DataMap config);
    }

    // Asynchronously fetches the current config DataMap and passes it to the given callback. If
    // the current config DataItem doesn't exist, it isn't created and the callback receives an
    // empty DataMap.
    public static void fetchConfigDataMap(final GoogleApiClient client,
            final FetchConfigDataMapCallback callback) {
        Wearable.NodeApi.getLocalNode(client).setResultCallback(
                new ResultCallback<NodeApi.GetLocalNodeResult>() {
                    @Override
                    public void onResult(NodeApi.GetLocalNodeResult getLocalNodeResult) {
                        String localNode = getLocalNodeResult.getNode().getId();
                        Uri uri = new Uri.Builder()
                                .scheme("wear")
                                .path(WatchFaceUtil.PATH_WITH_FEATURE)
                                .authority(localNode)
                                .build();
                        Wearable.DataApi.getDataItem(client, uri)
                                .setResultCallback(new DataItemResultCallback(callback));
                    }
                }
        );
    }

    // Overwrites (or sets, if not present) the keys in the current config DataItem with the ones
    // appearing in the given DataMap. If the config DataItem doesn't exist, it's created. It is
    // allowed that only some of the keys used in the config DataItem appear in
    // configKeysToOverwrite. The rest of the keys remains unmodified in this case.
    public static void overwriteKeysInConfigDataMap(final GoogleApiClient googleApiClient,
            final DataMap configKeysToOverwrite) {

        WatchFaceUtil.fetchConfigDataMap(googleApiClient,
                new FetchConfigDataMapCallback() {
                    @Override
                    public void onConfigDataMapFetched(DataMap currentConfig) {
                        DataMap overwrittenConfig = new DataMap();
                        overwrittenConfig.putAll(currentConfig);
                        overwrittenConfig.putAll(configKeysToOverwrite);
                        WatchFaceUtil.putConfigDataItem(googleApiClient, overwrittenConfig);
                    }
                }
        );
    }


    // Overwrites the current config DataItem's DataMap with newConfig. If the config DataItem
    // doesn't exist, it's created.
    public static void putConfigDataItem(GoogleApiClient googleApiClient, DataMap newConfig) {
        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(PATH_WITH_FEATURE);
        DataMap configToPut = putDataMapRequest.getDataMap();
        configToPut.putAll(newConfig);
        Wearable.DataApi.putDataItem(googleApiClient, putDataMapRequest.asPutDataRequest())
                .setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                    @Override
                    public void onResult(DataApi.DataItemResult dataItemResult) {
                        if (Log.isLoggable(TAG, Log.DEBUG)) {
                            Log.d(TAG, "putDataItem result status: " + dataItemResult.getStatus());
                        }
                    }
                });
    }

    private static class DataItemResultCallback implements ResultCallback<DataApi.DataItemResult> {

        private final FetchConfigDataMapCallback mCallback;

        public DataItemResultCallback(FetchConfigDataMapCallback callback) {
            mCallback = callback;
        }

        @Override
        public void onResult(DataApi.DataItemResult dataItemResult) {
            if (dataItemResult.getStatus().isSuccess()) {
                if (dataItemResult.getDataItem() != null) {
                    DataItem configDataItem = dataItemResult.getDataItem();
                    DataMapItem dataMapItem = DataMapItem.fromDataItem(configDataItem);
                    DataMap config = dataMapItem.getDataMap();
                    mCallback.onConfigDataMapFetched(config);
                } else {
                    mCallback.onConfigDataMapFetched(new DataMap());
                }
            }
        }
    }

    // singleton
    private WatchFaceUtil() {
    }
}
