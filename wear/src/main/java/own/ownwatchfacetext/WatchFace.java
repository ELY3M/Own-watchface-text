/*
 */

package own.ownwatchfacetext;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.SimpleTimeZone;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Digital watch face with seconds. In ambient mode, the seconds aren't displayed. On devices with
 * low-bit ambient mode, the text is drawn without anti-aliasing in ambient mode.
 */
public class WatchFace extends CanvasWatchFaceService  {

    private static final String TAG = "watchface";



    /**
     * Update rate in milliseconds for interactive mode. We update once a second since seconds are
     * displayed in interactive mode.
     */
    private static final long INTERACTIVE_UPDATE_RATE_MS = TimeUnit.SECONDS.toMillis(1);

    /**
     * Handler message id for updating the time periodically in interactive mode.
     */
    private static final int MSG_UPDATE_TIME = 0;

    @Override
    public Engine onCreateEngine() {
        return new Engine();
    }


    private class Engine extends CanvasWatchFaceService.Engine implements DataApi.DataListener,
            GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
        static final int MSG_UPDATE_TIME = 0;

        // How often mUpdateTimeHandler ticks in milliseconds.
        long mInteractiveUpdateRateMs = NORMAL_UPDATE_RATE_MS;

        // Handler to update the time periodically in interactive mode.
        final Handler mUpdateTimeHandler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                switch (message.what) {
                    case MSG_UPDATE_TIME:
                        //Log.v(TAG, "updating time");
                        invalidate();
                        if (shouldTimerBeRunning()) {
                            long timeMs = System.currentTimeMillis();
                            long delayMs =
                                    mInteractiveUpdateRateMs - (timeMs % mInteractiveUpdateRateMs);
                            mUpdateTimeHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs);
                        }
                        break;
                }
            }
        };



        GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(WatchFace.this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Wearable.API)
                .build();

        Resources resources = WatchFace.this.getResources();

        Typeface BOLD_TYPEFACE =
                Typeface.createFromAsset(resources.getAssets(), "fonts/DS-DIGIB.TTF");

        Typeface NORMAL_TYPEFACE =
                Typeface.createFromAsset(resources.getAssets(), "fonts/DS-DIGI.TTF");

        Typeface ITALIC_TYPEFACE =
                Typeface.createFromAsset(resources.getAssets(), "fonts/DS-DIGIT.TTF");

        Typeface PIXELLCD =
                Typeface.createFromAsset(resources.getAssets(), "fonts/PixelLCD-7.ttf");

        private static final String TIME_FORMAT_12_NOSECS = "h:mm";
        private static final String TIME_FORMAT_12 = "h:mm:ss";
        private static final String TIME_FORMAT_24_NOSECS = "H:mm";
        private static final String TIME_FORMAT_24 = "H:mm:ss";
        private static final String PERIOD_FORMAT = "a";
        private static final String DATESTAMP_FORMAT = "EEE MMM, dd yyyy";
        private static final String TIMESTAMP_FORMAT = "HH:mm:ss Z";


        private static final double MOON_PHASE_LENGTH = 29.530588853;
        private Calendar moonCalendar;


        String lat;
        String lon;
        String temp = "103°F";
        String icon = "skc";
        String weather = "skc";
        String TempString = temp;
        String WeatherString = weather;


        // Update rate in milliseconds for normal (not ambient) mode.
        private static final long NORMAL_UPDATE_RATE_MS = 500;


        //settings
        boolean clockAct;
        boolean clockDim;
        boolean clocknosecsAct;
        boolean clocknosecsDim;
        boolean periodDim;
        boolean dateDim;
        boolean timeDim;
        boolean tempDim;
        boolean weatherDim;
        boolean alwaysUtc;
        boolean showtime;
        boolean northernhemi;

        boolean mIsLowBitAmbient;
        boolean mIsMute;

        boolean mRegisteredTimeZoneReceiver = false;
        Date mDate;

        Rect cardPeekRectangle = new Rect(0, 0, 0, 0);

        WatchFaceStyle shortCards;
        WatchFaceStyle variableCards;

        SimpleDateFormat timeSdf;
        SimpleDateFormat timenosecsSdf;
        SimpleDateFormat periodSdf;
        SimpleDateFormat dateStampSdf;
        SimpleDateFormat timeStampSdf;


        Paint mClocknosecsPaint;
        Paint mPeriodPaint;
        Paint mDatestampPaint;
        Paint mTimestampPaint;
        Paint mTempPaint;
        Paint mWeatherPaint;

        int mInteractiveTextColor = getResources().getColor(R.color.aqua);
        int mAmbientTextColor = getResources().getColor(R.color.aqua);
        int mBackgroundColor = getResources().getColor(R.color.black);

        float mXOffset;
        float mYOffset;
        float mPadding;


        Paint mBackgroundPaint;
        Bitmap mBackgroundBitmap = BitmapFactory.decodeResource(resources, R.drawable.back);
        Bitmap mBackgroundScaledBitmap;

        Paint mClockPaint;
        boolean mAmbient;
        Calendar mCalendar;
        final BroadcastReceiver mTimeZoneReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mCalendar.setTimeZone(TimeZone.getDefault());
                invalidate();
            }
        };



        private void updateTimeZone(TimeZone tz) {
            timeSdf.setTimeZone(tz);
            timenosecsSdf.setTimeZone(tz);
            periodSdf.setTimeZone(tz);
            dateStampSdf.setTimeZone(tz);
            if (alwaysUtc) {
                timeStampSdf.setTimeZone(new SimpleTimeZone(0, "UTC"));
            } else {
                timeStampSdf.setTimeZone(tz);
            }
            mDate.setTime(System.currentTimeMillis());
        }


        @Override
        public void onCreate(SurfaceHolder holder) {

            Log.d(TAG, "onCreate");
            super.onCreate(holder);
            mCalendar = Calendar.getInstance();

            //StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            //StrictMode.setThreadPolicy(policy);

            // A style with variable height notification cards.  Note that setting the
            // HotwordIndicatorGravity or StatusBarGravity to BOTTOM will force the notification
            // cards to be short, regardless of the CardPeekMode.
            variableCards = new WatchFaceStyle.Builder(WatchFace.this)
                    .setAmbientPeekMode(WatchFaceStyle.AMBIENT_PEEK_MODE_VISIBLE)
                    .setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE)
                    .setCardPeekMode(WatchFaceStyle.PEEK_MODE_VARIABLE)
                    .setHotwordIndicatorGravity(Gravity.TOP | Gravity.LEFT)
                    .setPeekOpacityMode(WatchFaceStyle.PEEK_OPACITY_MODE_TRANSLUCENT)
                    .setShowSystemUiTime(false)
                    .setShowUnreadCountIndicator(false)
                    .setStatusBarGravity(Gravity.TOP | Gravity.LEFT)
                    .setViewProtection(WatchFaceStyle.PROTECT_STATUS_BAR)
                    .build();

            // A style with short height notification cards.
            shortCards = new WatchFaceStyle.Builder(WatchFace.this)
                    .setAmbientPeekMode(WatchFaceStyle.AMBIENT_PEEK_MODE_VISIBLE)
                    .setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE)
                    .setCardPeekMode(WatchFaceStyle.PEEK_MODE_SHORT)
                    .setHotwordIndicatorGravity(Gravity.BOTTOM | Gravity.RIGHT)
                    .setPeekOpacityMode(WatchFaceStyle.PEEK_OPACITY_MODE_TRANSLUCENT)
                    .setShowSystemUiTime(false)
                    .setShowUnreadCountIndicator(false)
                    .setStatusBarGravity(Gravity.BOTTOM | Gravity.LEFT)
                    .setViewProtection(WatchFaceStyle.PROTECT_STATUS_BAR)
                    .build();

            boolean useShortCards = WatchFaceUtil
                    .getBoolean(getApplicationContext(), WatchFaceUtil.KEY_USE_SHORT_CARDS,
                            WatchFaceUtil.KEY_USE_SHORT_CARDS_DEF);
            if (useShortCards) {
                Log.d(TAG, "Using short notification cards");
                setWatchFaceStyle(shortCards);
            } else {
                Log.d(TAG, "Using variable notification cards");
                setWatchFaceStyle(variableCards);
            }

            if (DateFormat.is24HourFormat(getApplicationContext())) {
                timeSdf = new SimpleDateFormat(TIME_FORMAT_24);
                timenosecsSdf = new SimpleDateFormat(TIME_FORMAT_24_NOSECS);
            } else {
                timeSdf = new SimpleDateFormat(TIME_FORMAT_12);
                timenosecsSdf = new SimpleDateFormat(TIME_FORMAT_12_NOSECS);
            }

            //date formatters
            periodSdf = new SimpleDateFormat(PERIOD_FORMAT);
            dateStampSdf = new SimpleDateFormat(DATESTAMP_FORMAT);
            timeStampSdf = new SimpleDateFormat(TIMESTAMP_FORMAT);

            mBackgroundPaint = new Paint();
            mBackgroundPaint.setColor(mBackgroundColor);
            mClockPaint = createTextPaint(mInteractiveTextColor, PIXELLCD);
            mClocknosecsPaint = createTextPaint(mInteractiveTextColor, PIXELLCD);
            mPeriodPaint = createTextPaint(mInteractiveTextColor, PIXELLCD);
            mDatestampPaint = createTextPaint(mInteractiveTextColor, NORMAL_TYPEFACE);
            mTimestampPaint = createTextPaint(mInteractiveTextColor, NORMAL_TYPEFACE);
            mTempPaint = createTextPaint(mInteractiveTextColor, NORMAL_TYPEFACE);
            mWeatherPaint = createTextPaint(mInteractiveTextColor, NORMAL_TYPEFACE);


            // initial setup, load persisted or default values, can be overridden by companion app
            Context context = getApplicationContext();
            float clockSize = WatchFaceUtil.getInt(context, WatchFaceUtil.KEY_CLOCK_SIZE, WatchFaceUtil.KEY_CLOCK_SIZE_DEF);
            float clocknosecsSize = WatchFaceUtil.getInt(context, WatchFaceUtil.KEY_CLOCK_NOSECS_SIZE, WatchFaceUtil.KEY_CLOCK_NOSECS_SIZE_DEF);
            float markerSize = WatchFaceUtil.getInt(context, WatchFaceUtil.KEY_MARKER_SIZE, WatchFaceUtil.KEY_MARKER_SIZE_DEF);
            float datestampSize = WatchFaceUtil.getInt(context, WatchFaceUtil.KEY_DATE_SIZE, WatchFaceUtil.KEY_DATE_SIZE_DEF);
            float timestampSize = WatchFaceUtil.getInt(context, WatchFaceUtil.KEY_TIME_SIZE, WatchFaceUtil.KEY_TIME_SIZE_DEF);
            float tempSize = WatchFaceUtil.getInt(context, WatchFaceUtil.KEY_TEMP_SIZE, WatchFaceUtil.KEY_TEMP_SIZE_DEF);
            float weatherSize = WatchFaceUtil.getInt(context, WatchFaceUtil.KEY_WEATHER_SIZE, WatchFaceUtil.KEY_WEATHER_SIZE_DEF);

            //get gps
            lat = WatchFaceUtil.getString(context, WatchFaceUtil.KEY_LAT, WatchFaceUtil.KEY_LAT_DEF);
            lon = WatchFaceUtil.getString(context, WatchFaceUtil.KEY_LON, WatchFaceUtil.KEY_LON_DEF);

            //get weather
            temp = WatchFaceUtil.getString(context, WatchFaceUtil.KEY_TEMP, WatchFaceUtil.KEY_TEMP_DEF);
            icon = WatchFaceUtil.getString(context, WatchFaceUtil.KEY_ICON, WatchFaceUtil.KEY_ICON_DEF);
            weather = WatchFaceUtil.getString(context, WatchFaceUtil.KEY_WEATHER, WatchFaceUtil.KEY_WEATHER_DEF);


            Log.i(TAG, "oncreate firstget Weather: Temp: " + temp + " icon: " + icon + " Weather: " + weather + " EOF");

            // set the text sizes scaled according to the screen density
            float density = getResources().getDisplayMetrics().density;
            mClockPaint.setTextSize(clockSize * density);
            mClocknosecsPaint.setTextSize(clocknosecsSize * density);
            mPeriodPaint.setTextSize(markerSize * density);
            mDatestampPaint.setTextSize(datestampSize * density);
            mTimestampPaint.setTextSize(timestampSize * density);
            mTempPaint.setTextSize(tempSize * density);
            mWeatherPaint.setTextSize(weatherSize * density);


            clockAct = WatchFaceUtil.getBoolean(context, WatchFaceUtil.KEY_CLOCK_ACT, WatchFaceUtil.KEY_CLOCK_ACT_DEF);
            clockDim = WatchFaceUtil.getBoolean(context, WatchFaceUtil.KEY_CLOCK_DIM, WatchFaceUtil.KEY_CLOCK_DIM_DEF);
            clocknosecsAct = WatchFaceUtil.getBoolean(context, WatchFaceUtil.KEY_CLOCK_NOSECS_ACT, WatchFaceUtil.KEY_CLOCK_NOSECS_ACT_DEF);
            clocknosecsDim = WatchFaceUtil.getBoolean(context, WatchFaceUtil.KEY_CLOCK_NOSECS_DIM, WatchFaceUtil.KEY_CLOCK_NOSECS_DIM_DEF);
            periodDim = WatchFaceUtil.getBoolean(context, WatchFaceUtil.KEY_MARKER_DIM, WatchFaceUtil.KEY_MARKER_DIM_DEF);
            dateDim = WatchFaceUtil.getBoolean(context, WatchFaceUtil.KEY_DATE_DIM, WatchFaceUtil.KEY_DATE_DIM_DEF);
            timeDim = WatchFaceUtil.getBoolean(context, WatchFaceUtil.KEY_TIME_DIM, WatchFaceUtil.KEY_TIME_DIM_DEF);
            tempDim = WatchFaceUtil.getBoolean(context, WatchFaceUtil.KEY_TEMP_DIM, WatchFaceUtil.KEY_TEMP_DIM_DEF);
            weatherDim = WatchFaceUtil.getBoolean(context, WatchFaceUtil.KEY_WEATHER_DIM, WatchFaceUtil.KEY_WEATHER_DIM_DEF);
            alwaysUtc = WatchFaceUtil.getBoolean(context, WatchFaceUtil.KEY_ALWAYS_UTC, WatchFaceUtil.KEY_ALWAYS_UTC_DEF);
            showtime = WatchFaceUtil.getBoolean(context, WatchFaceUtil.KEY_SHOW_TIME, WatchFaceUtil.KEY_SHOW_TIME_DEF);
            northernhemi = WatchFaceUtil.getBoolean(context, WatchFaceUtil.KEY_NORTHERNHEMI, WatchFaceUtil.KEY_NORTHERNHEMI_DEF);

            mDate = new Date();

        }

        @Override
        public void onDestroy() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            super.onDestroy();
        }


        private Paint createTextPaint(int Color, Typeface typeface) {
            Paint paint = new Paint();
            paint.setColor(Color);
            paint.setTypeface(typeface);
            paint.setAntiAlias(true);
            return paint;
        }



        @Override
        public void onVisibilityChanged(boolean visible) {
            Log.d(TAG, "onVisibilityChanged: " + visible);
            super.onVisibilityChanged(visible);

            if (visible) {
                mGoogleApiClient.connect();

                registerReceiver();

                // Update time zone in case it changed while we weren't visible.
                updateTimeZone(TimeZone.getDefault());


            } else {
                unregisterReceiver();

                if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                    Wearable.DataApi.removeListener(mGoogleApiClient, this);
                    mGoogleApiClient.disconnect();
                }
            }

            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
            updateTimer();
        }

        private void registerReceiver() {
            if (mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = true;
            IntentFilter filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
            WatchFace.this.registerReceiver(mTimeZoneReceiver, filter);
        }

        private void unregisterReceiver() {
            if (!mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = false;
            WatchFace.this.unregisterReceiver(mTimeZoneReceiver);
        }



        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);

            boolean burnInProtection = properties.getBoolean(PROPERTY_BURN_IN_PROTECTION, false);

            mIsLowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);

            Log.d(TAG, "onPropertiesChanged: burn-in protection = " + burnInProtection
                    + ", low-bit ambient = " + mIsLowBitAmbient);

        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            Log.d(TAG, "onTimeTick: ambient = " + isInAmbientMode());
            invalidate();
        }

        @Override
        public void onPeekCardPositionUpdate(Rect rect) {
            super.onPeekCardPositionUpdate(rect);
            Log.d(TAG, "onPeekCardPositionUpdate: " + rect);

            cardPeekRectangle = rect;

            invalidate();
        }


        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);
            Log.d(TAG, "onAmbientModeChanged: " + inAmbientMode);

            adjustPaintColorToCurrentMode(mClockPaint, mInteractiveTextColor, mAmbientTextColor);
            adjustPaintColorToCurrentMode(mClocknosecsPaint, mInteractiveTextColor, mAmbientTextColor);
            adjustPaintColorToCurrentMode(mPeriodPaint, mInteractiveTextColor, mAmbientTextColor);
            adjustPaintColorToCurrentMode(mTimestampPaint, mInteractiveTextColor, mAmbientTextColor);
            adjustPaintColorToCurrentMode(mTempPaint, mInteractiveTextColor, mAmbientTextColor);
            adjustPaintColorToCurrentMode(mWeatherPaint, mInteractiveTextColor, mAmbientTextColor);

            // these are always ambient
            //adjustPaintColorToCurrentMode(mDatestampPaint, mInteractiveTextColor, mAmbientTextColor);
            //adjustPaintColorToCurrentMode(mTimestampPaint, mInteractiveTextColor, mAmbientTextColor);

            // When this property is set to true, the screen supports fewer bits for each color in
            // ambient mode. In this case, watch faces should disable anti-aliasing in ambient mode.
            if (mIsLowBitAmbient) {
                boolean antiAlias = !inAmbientMode;
                mDatestampPaint.setAntiAlias(antiAlias);
                mClockPaint.setAntiAlias(antiAlias);
                mClocknosecsPaint.setAntiAlias(antiAlias);
                mPeriodPaint.setAntiAlias(antiAlias);
                mTimestampPaint.setAntiAlias(antiAlias);
                mTempPaint.setAntiAlias(antiAlias);
                mWeatherPaint.setAntiAlias(antiAlias);

            }
            invalidate();

            // Whether the timer should be running depends on whether we're in ambient mode (as well
            // as whether we're visible), so we may need to start or stop the timer.
            updateTimer();
        }

        /**
         * Captures tap event (and tap type) and toggles the background color if the user finishes
         * a tap.
         */
        /*
        @Override
        public void onTapCommand(int tapType, int x, int y, long eventTime) {
            switch (tapType) {
                case TAP_TYPE_TOUCH:
                    // The user has started touching the screen.
                    break;
                case TAP_TYPE_TOUCH_CANCEL:
                    // The user has started a different gesture or otherwise cancelled the tap.
                    break;
                case TAP_TYPE_TAP:
                    // The user has completed the tap gesture.
                    // TODO: Add code to handle the tap gesture.
                    Toast.makeText(getApplicationContext(), R.string.message, Toast.LENGTH_SHORT)
                            .show();
                    break;
            }
            invalidate();
        }
        */


        private void adjustPaintColorToCurrentMode(Paint paint, int interactiveColor,
                                                   int ambientColor) {
            paint.setColor(isInAmbientMode() ? ambientColor : interactiveColor);
        }

        @Override
        public void onInterruptionFilterChanged(int interruptionFilter) {
            Log.d(TAG, "onInterruptionFilterChanged: " + interruptionFilter);
            super.onInterruptionFilterChanged(interruptionFilter);

            boolean inMuteMode = interruptionFilter
                    == android.support.wearable.watchface.WatchFaceService.INTERRUPTION_FILTER_NONE;

            if (mIsMute != inMuteMode) {
                mIsMute = inMuteMode;
                invalidate();
            }
        }


        @Override
        public void onDraw(Canvas canvas, Rect bounds) {

            // where the magic happens...
            mDate.setTime(System.currentTimeMillis());

            mXOffset = resources.getDimension(R.dimen.x_offset);
            mYOffset = resources.getDimension(R.dimen.y_offset);
            mPadding = resources.getDimension(R.dimen.padding);

            mDatestampPaint.setTextAlign(Paint.Align.CENTER);
            mClockPaint.setTextAlign(Paint.Align.LEFT);
            mClocknosecsPaint.setTextAlign(Paint.Align.CENTER);
            mPeriodPaint.setTextAlign(Paint.Align.RIGHT);
            mTimestampPaint.setTextAlign(Paint.Align.CENTER);
            mTempPaint.setTextAlign(Paint.Align.CENTER);
            mWeatherPaint.setTextAlign(Paint.Align.CENTER);

            int width = bounds.width();
            int height = bounds.height();
            float centerX = width / 2f;
            float centerY = height / 2f;

            // Draw the background.
            if (isInAmbientMode()) {
                // black background
                canvas.drawRect(0, 0, width, height, mBackgroundPaint);
            } else {
                if (mBackgroundScaledBitmap == null
                        || mBackgroundScaledBitmap.getWidth() != width
                        || mBackgroundScaledBitmap.getHeight() != height) {
                    mBackgroundScaledBitmap = Bitmap
                            .createScaledBitmap(mBackgroundBitmap, width, height, true);
                }
                // fancy image background
                canvas.drawBitmap(mBackgroundScaledBitmap, 0, 0, null);
                ///moon
                moonupdate(canvas);
                //weather
                weathericon(canvas);
                //weather via wifi
                //weatherupdate(canvas);
                //new getweatherviajson().execute();



            }


            // Update the strings
            String datestampString = dateStampSdf.format(mDate);
            String clockString = timeSdf.format(mDate);
            String clocknosecsString = timenosecsSdf.format(mDate);
            String periodString = periodSdf.format(mDate);
            String timestampString = timeStampSdf.format(mDate);
            TempString = temp;
            WeatherString = weather;

            float xClock, yClock;
            float xClocknosecs, yClocknosecs;
            float xPeriod, yPeriod;
            float xDatestamp, yDatestamp;
            float xTimestamp, yTimestamp;
            float xTemp, yTemp;
            float xWeather, yWeather;




            xDatestamp = width / 2f;
            yDatestamp = 25;

            xPeriod = width - mPadding;
            yPeriod = height / 2f;

            ///xClock = width / 2f;
            xClock = mPadding;
            yClock = height / 2f;
            xClocknosecs = (width / 2f) - 30;
            yClocknosecs = height / 2f;

            xTimestamp = width / 2f;
            yTimestamp = height - 4;

            xTemp = 50;
            yTemp = height - 80;

            xWeather = width / 2f;
            yWeather = height - 30;


            if (cardPeekRectangle.top == 0) {
                cardPeekRectangle.top = height;
            }

            if (isInAmbientMode()) {
                // draw these when ambient
                if (clockDim) {
                    if (yClock < cardPeekRectangle.top) {
                        canvas.drawText(clockString, xClock, yClock, mClockPaint);
                    }
                }
                if (clocknosecsDim) {
                    if (yClocknosecs < cardPeekRectangle.top) {
                        canvas.drawText(clocknosecsString, xClocknosecs, yClocknosecs, mClocknosecsPaint);
                    }
                }
                if (periodDim) {
                    if (!DateFormat.is24HourFormat(getApplicationContext())) {
                        if (yPeriod < cardPeekRectangle.top) {
                            canvas.drawText(periodString, xPeriod, yPeriod, mPeriodPaint);
                        }
                    }
                }

                if (dateDim) {
                    if (yDatestamp < cardPeekRectangle.top) {
                        canvas.drawText(datestampString, xDatestamp, yDatestamp, mDatestampPaint);
                    }
                }

                if (tempDim) {
                    if (yTemp < cardPeekRectangle.top) {
                        canvas.drawText(TempString, xTemp, yTemp, mTempPaint);
                    }
                }

                if (weatherDim) {
                    if (yWeather < cardPeekRectangle.top) {
                        canvas.drawText(WeatherString, xWeather, yWeather, mWeatherPaint);
                    }
                }

                if (timeDim) {
                    if (yTimestamp < cardPeekRectangle.top) {
                        canvas.drawText(timestampString, xTimestamp, yTimestamp, mTimestampPaint);
                    }
                }


            } else {
                // draw these when interactive
                if (clockAct) {
                    if (yClock < cardPeekRectangle.top) {
                        canvas.drawText(clockString, xClock, yClock, mClockPaint);
                    }
                }
                if (clocknosecsAct) {
                    if (yClocknosecs < cardPeekRectangle.top) {
                        canvas.drawText(clocknosecsString, xClocknosecs, yClocknosecs, mClocknosecsPaint);
                    }
                }

                if (!DateFormat.is24HourFormat(getApplicationContext())) {
                    if (yPeriod < cardPeekRectangle.top) {
                        canvas.drawText(periodString, xPeriod, yPeriod, mPeriodPaint);
                    }
                }

                if (yDatestamp < cardPeekRectangle.top) {
                    canvas.drawText(datestampString, xDatestamp, yDatestamp, mDatestampPaint);
                }

                if (yTemp < cardPeekRectangle.top) {
                    canvas.drawText(TempString, xTemp, yTemp, mTempPaint);
                }

                if (yWeather < cardPeekRectangle.top) {
                    canvas.drawText(WeatherString, xWeather, yWeather, mWeatherPaint);
                }


                if (showtime) {
                    if (yTimestamp < cardPeekRectangle.top) {
                        canvas.drawText(timestampString, xTimestamp, yTimestamp, mTimestampPaint);
                    }
                }

            }

        }





        // Starts the mUpdateTimeHandler timer if it should be running and isn't currently stops it
        // if it shouldn't be running but currently is.
        private void updateTimer() {
            Log.d(TAG, "updateTimer");
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            if (shouldTimerBeRunning()) {
                mUpdateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME);
            }
        }

        // Returns whether the mUpdateTimeHandler timer should be running. The timer should only
        // run when we're visible and in interactive mode.
        private boolean shouldTimerBeRunning() {
            return isVisible() && !isInAmbientMode();
        }

        private void updateConfigDataItemAndUiOnStartup() {
            WatchFaceUtil.fetchConfigDataMap(mGoogleApiClient,
                    new WatchFaceUtil.FetchConfigDataMapCallback() {
                        @Override
                        public void onConfigDataMapFetched(DataMap startupConfig) {
                            // use the newly received settings
                            if (startupConfig != null && !startupConfig.isEmpty()) {
                                updateUiForConfigDataMap(startupConfig);
                            }
                        }
                    }
            );
        }

        @Override
        public void onDataChanged(DataEventBuffer dataEvents) {
            try {
                for (DataEvent dataEvent : dataEvents) {
                    if (dataEvent.getType() != DataEvent.TYPE_CHANGED) {
                        continue;
                    }

                    DataItem dataItem = dataEvent.getDataItem();
                    if (!dataItem.getUri().getPath().equals(WatchFaceUtil.PATH_WITH_FEATURE)) {
                        continue;
                    }

                    DataMapItem dataMapItem = DataMapItem.fromDataItem(dataItem);
                    DataMap config = dataMapItem.getDataMap();
                    Log.d(TAG, "Config DataItem updated:" + config);
                    if (config != null && !config.isEmpty()) {
                        updateUiForConfigDataMap(config);
                    }
                }
            } finally {
                dataEvents.close();
            }
        }

        private void updateUiForConfigDataMap(final DataMap dataMap) {
            Log.d(TAG, "updateUiForConfigDataMap: " + dataMap);

            // font sizes
            int clockSize = dataMap.getInt(WatchFaceUtil.KEY_CLOCK_SIZE, WatchFaceUtil.KEY_CLOCK_SIZE_DEF);
            int clocknosecsSize = dataMap.getInt(WatchFaceUtil.KEY_CLOCK_NOSECS_SIZE, WatchFaceUtil.KEY_CLOCK_NOSECS_SIZE_DEF);
            int periodSize = dataMap.getInt(WatchFaceUtil.KEY_MARKER_SIZE, WatchFaceUtil.KEY_MARKER_SIZE_DEF);
            int dateSize = dataMap.getInt(WatchFaceUtil.KEY_DATE_SIZE, WatchFaceUtil.KEY_DATE_SIZE_DEF);
            int timeSize = dataMap.getInt(WatchFaceUtil.KEY_TIME_SIZE, WatchFaceUtil.KEY_TIME_SIZE_DEF);
            int tempSize = dataMap.getInt(WatchFaceUtil.KEY_TEMP_SIZE, WatchFaceUtil.KEY_TEMP_SIZE_DEF);
            int weatherSize = dataMap.getInt(WatchFaceUtil.KEY_WEATHER_SIZE, WatchFaceUtil.KEY_WEATHER_SIZE_DEF);


            // visibility flags
            clockAct = dataMap.getBoolean(WatchFaceUtil.KEY_CLOCK_ACT, WatchFaceUtil.KEY_CLOCK_ACT_DEF);
            clockDim = dataMap.getBoolean(WatchFaceUtil.KEY_CLOCK_DIM, WatchFaceUtil.KEY_CLOCK_DIM_DEF);
            clocknosecsAct = dataMap.getBoolean(WatchFaceUtil.KEY_CLOCK_NOSECS_ACT, WatchFaceUtil.KEY_CLOCK_NOSECS_ACT_DEF);
            clocknosecsDim = dataMap.getBoolean(WatchFaceUtil.KEY_CLOCK_NOSECS_DIM, WatchFaceUtil.KEY_CLOCK_NOSECS_DIM_DEF);
            periodDim = dataMap.getBoolean(WatchFaceUtil.KEY_MARKER_DIM, WatchFaceUtil.KEY_MARKER_DIM_DEF);
            dateDim = dataMap.getBoolean(WatchFaceUtil.KEY_DATE_DIM, WatchFaceUtil.KEY_DATE_DIM_DEF);
            timeDim = dataMap.getBoolean(WatchFaceUtil.KEY_TIME_DIM, WatchFaceUtil.KEY_TIME_DIM_DEF);
            tempDim = dataMap.getBoolean(WatchFaceUtil.KEY_TEMP_DIM, WatchFaceUtil.KEY_TEMP_DIM_DEF);
            weatherDim = dataMap.getBoolean(WatchFaceUtil.KEY_WEATHER_DIM, WatchFaceUtil.KEY_WEATHER_DIM_DEF);

            alwaysUtc = dataMap.getBoolean(WatchFaceUtil.KEY_ALWAYS_UTC, WatchFaceUtil.KEY_ALWAYS_UTC_DEF);
            showtime = dataMap.getBoolean(WatchFaceUtil.KEY_SHOW_TIME, WatchFaceUtil.KEY_SHOW_TIME_DEF);
            northernhemi = dataMap.getBoolean(WatchFaceUtil.KEY_NORTHERNHEMI, WatchFaceUtil.KEY_NORTHERNHEMI_DEF);

            //gps stuff
            lat = dataMap.getString(WatchFaceUtil.KEY_LAT, WatchFaceUtil.KEY_LAT_DEF);
            lon = dataMap.getString(WatchFaceUtil.KEY_LON, WatchFaceUtil.KEY_LON_DEF);

            //weather stuff
            temp = dataMap.getString(WatchFaceUtil.KEY_TEMP, WatchFaceUtil.KEY_TEMP_DEF);
            icon = dataMap.getString(WatchFaceUtil.KEY_ICON, WatchFaceUtil.KEY_ICON_DEF);
            weather = dataMap.getString(WatchFaceUtil.KEY_WEATHER, WatchFaceUtil.KEY_WEATHER_DEF);
            Log.i(TAG, "configupdate... Weather: Temp: " + temp + " icon: " + icon + " Weather: " + weather + " EOF...");

            // notification card style
            boolean useShortCards = dataMap.getBoolean(WatchFaceUtil.KEY_USE_SHORT_CARDS, WatchFaceUtil.KEY_USE_SHORT_CARDS_DEF);

            // update the style accordingly
            if (useShortCards) {
                Log.d(TAG, "Using short notification cards");
                setWatchFaceStyle(shortCards);
            } else {
                Log.d(TAG, "Using variable notification cards");
                setWatchFaceStyle(variableCards);
            }

            // set the text sizes scaled according to the screen density
            float density = getResources().getDisplayMetrics().density;
            mDatestampPaint.setTextSize(dateSize * density);
            mClockPaint.setTextSize(clockSize * density);
            mClocknosecsPaint.setTextSize(clocknosecsSize * density);
            mPeriodPaint.setTextSize(periodSize * density);
            mTimestampPaint.setTextSize(timeSize * density);
            mTempPaint.setTextSize(tempSize * density);
            mWeatherPaint.setTextSize(weatherSize * density);

            // show the timestamp in UTC timezone if appropriate
            if (alwaysUtc) {
                timeStampSdf.setTimeZone(new SimpleTimeZone(0, "UTC"));
            } else {
                timeStampSdf.setTimeZone(TimeZone.getDefault());
            }

            ///need to update weather///
            TempString = temp;
            WeatherString = weather;

            // redraw the canvas
            invalidate();

            // persist these values for the next time the watch face is instantiated
            saveConfigValues(clockSize, clocknosecsSize, periodSize, dateSize, timeSize, useShortCards, tempSize, weatherSize);

            Log.i(TAG, "end of onDataChanged... Weather: Temp: " + temp + " icon: " + icon + " Weather: " + weather + " EOF...");


        }

        private void saveConfigValues(int clockSize, int clocknosecsSize, int periodSize, int dateSize, int timeSize, boolean useShortCards, int tempSize, int weatherSize) {
            Log.d(TAG, "saveConfigValues");

            Context context = getApplicationContext();

            WatchFaceUtil.setInt(context, WatchFaceUtil.KEY_CLOCK_SIZE, clockSize);
            WatchFaceUtil.setInt(context, WatchFaceUtil.KEY_CLOCK_NOSECS_SIZE, clocknosecsSize);
            WatchFaceUtil.setInt(context, WatchFaceUtil.KEY_MARKER_SIZE, periodSize);
            WatchFaceUtil.setInt(context, WatchFaceUtil.KEY_DATE_SIZE, dateSize);
            WatchFaceUtil.setInt(context, WatchFaceUtil.KEY_TIME_SIZE, timeSize);
            WatchFaceUtil.setInt(context, WatchFaceUtil.KEY_TEMP_SIZE, tempSize);
            WatchFaceUtil.setInt(context, WatchFaceUtil.KEY_WEATHER_SIZE, weatherSize);
            WatchFaceUtil.setBoolean(context, WatchFaceUtil.KEY_CLOCK_ACT, clockAct);
            WatchFaceUtil.setBoolean(context, WatchFaceUtil.KEY_CLOCK_DIM, clockDim);
            WatchFaceUtil.setBoolean(context, WatchFaceUtil.KEY_CLOCK_NOSECS_ACT, clocknosecsAct);
            WatchFaceUtil.setBoolean(context, WatchFaceUtil.KEY_CLOCK_NOSECS_DIM, clocknosecsDim);
            WatchFaceUtil.setBoolean(context, WatchFaceUtil.KEY_MARKER_DIM, periodDim);
            WatchFaceUtil.setBoolean(context, WatchFaceUtil.KEY_DATE_DIM, dateDim);
            WatchFaceUtil.setBoolean(context, WatchFaceUtil.KEY_TIME_DIM, timeDim);
            WatchFaceUtil.setBoolean(context, WatchFaceUtil.KEY_TEMP_DIM, tempDim);
            WatchFaceUtil.setBoolean(context, WatchFaceUtil.KEY_WEATHER_DIM, weatherDim);
            WatchFaceUtil.setBoolean(context, WatchFaceUtil.KEY_ALWAYS_UTC, alwaysUtc);
            WatchFaceUtil.setBoolean(context, WatchFaceUtil.KEY_SHOW_TIME, showtime);
            WatchFaceUtil.setBoolean(context, WatchFaceUtil.KEY_NORTHERNHEMI, northernhemi);
            WatchFaceUtil.setBoolean(context, WatchFaceUtil.KEY_USE_SHORT_CARDS, useShortCards);

        }

        @Override
        public void onConnected(Bundle connectionHint) {
            Log.d(TAG, "onConnected: " + connectionHint);
            Wearable.DataApi.addListener(mGoogleApiClient, Engine.this);
            updateConfigDataItemAndUiOnStartup();

        }

        @Override
        public void onConnectionSuspended(int cause) {
            Log.d(TAG, "onConnectionSuspended: " + cause);
        }

        @Override
        public void onConnectionFailed(ConnectionResult result) {
            Log.d(TAG, "onConnectionFailed: " + result);
        }









        /////moon stuff/////
        public void moonupdate(Canvas canvas) {
            Resources resources = WatchFace.this.getResources();
            Bitmap mMoonBitmap;
            Bitmap mMoonResizedBitmap;
            northernhemi = WatchFaceUtil.getBoolean(getApplicationContext(), WatchFaceUtil.KEY_NORTHERNHEMI,
                    WatchFaceUtil.KEY_NORTHERNHEMI_DEF);

            double phase = computeMoonPhase();
            Log.i(TAG, "Computed moon phase: " + phase);

            int phaseValue = ((int) Math.floor(phase)) % 30;
            Log.i(TAG, "Discrete phase value: " + phaseValue);

            Drawable moonDrawable = resources.getDrawable(IMAGE_LOOKUP[phaseValue]);

            if (northernhemi) {
                mMoonBitmap = ((BitmapDrawable) moonDrawable).getBitmap();
                mMoonResizedBitmap = Bitmap.createScaledBitmap(mMoonBitmap, 73, 73, false);
                canvas.drawBitmap(mMoonResizedBitmap, 23, 23, null);
            } else {
                Matrix matrix = new Matrix();
                matrix.postRotate(180);
                mMoonBitmap = ((BitmapDrawable) moonDrawable).getBitmap();
                mMoonResizedBitmap = Bitmap.createScaledBitmap(mMoonBitmap, 73, 73, false);
                Bitmap mMoonrotatedBitmap = Bitmap.createBitmap(mMoonResizedBitmap, 0, 0, mMoonResizedBitmap.getWidth(), mMoonResizedBitmap.getHeight(), matrix, true);
                canvas.drawBitmap(mMoonrotatedBitmap, 23, 23, null);

            }
        }

        /* not all fucking watches have gps :(
                private boolean isNorthernHemi() {
                    LocationManager locationManager =
                            (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    Location location =
                            locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                    if (location != null) {
                        return location.getLongitude() > 0;
                    }

                    return false;
                }
        */
        // Computes moon phase based upon Bradley E. Schaefer's moon phase algorithm.
        private double computeMoonPhase() {
            moonCalendar = Calendar.getInstance();
            int year = moonCalendar.get(Calendar.YEAR);
            int month = moonCalendar.get(Calendar.MONTH) + 1;
            int day = moonCalendar.get(Calendar.DAY_OF_MONTH);
            int minute = moonCalendar.get(Calendar.MINUTE); ///for testing///
            Log.i(TAG, "year: " + year + " month: " + month + " day: " + day);
            Log.i(TAG, "test: " + minute); ///testing///
            // Convert the year into the format expected by the algorithm.
            double transformedYear = year - Math.floor((12 - month) / 10);
            Log.i(TAG, "transformedYear: " + transformedYear);

            // Convert the month into the format expected by the algorithm.
            int transformedMonth = month + 9;
            if (transformedMonth >= 12) {
                transformedMonth = transformedMonth - 12;
            }
            Log.i(TAG, "transformedMonth: " + transformedMonth);

            // Logic to compute moon phase as a fraction between 0 and 1
            double term1 = Math.floor(365.25 * (transformedYear + 4712));
            double term2 = Math.floor(30.6 * transformedMonth + 0.5);
            double term3 = Math.floor(Math.floor((transformedYear / 100) + 49) * 0.75) - 38;

            double intermediate = term1 + term2 + day + 59;
            if (intermediate > 2299160) {
                intermediate = intermediate - term3;
            }
            Log.i(TAG, "intermediate: " + intermediate);

            double normalizedPhase = (intermediate - 2451550.1) / MOON_PHASE_LENGTH;
            normalizedPhase = normalizedPhase - Math.floor(normalizedPhase);
            if (normalizedPhase < 0) {
                normalizedPhase = normalizedPhase + 1;
            }
            Log.i(TAG, "normalizedPhase: " + normalizedPhase);

            // Return the result as a value between 0 and MOON_PHASE_LENGTH
            return normalizedPhase * MOON_PHASE_LENGTH;
        }

        private final int[] IMAGE_LOOKUP = {
                R.drawable.moon0,
                R.drawable.moon1,
                R.drawable.moon2,
                R.drawable.moon3,
                R.drawable.moon4,
                R.drawable.moon5,
                R.drawable.moon6,
                R.drawable.moon7,
                R.drawable.moon8,
                R.drawable.moon9,
                R.drawable.moon10,
                R.drawable.moon11,
                R.drawable.moon12,
                R.drawable.moon13,
                R.drawable.moon14,
                R.drawable.moon15,
                R.drawable.moon16,
                R.drawable.moon17,
                R.drawable.moon18,
                R.drawable.moon19,
                R.drawable.moon20,
                R.drawable.moon21,
                R.drawable.moon22,
                R.drawable.moon23,
                R.drawable.moon24,
                R.drawable.moon25,
                R.drawable.moon26,
                R.drawable.moon27,
                R.drawable.moon28,
                R.drawable.moon29,
        };
////  end of moon stuff /////


////weather stuff
        public void weathericon(Canvas canvas) {
            Resources resources = WatchFace.this.getResources();
            Bitmap mIconBitmap;
            Bitmap mIconResizedBitmap;
            Log.i(TAG, "in weathericon() Temp: " + temp + " icon: " + icon + " Weather: " + weather);
            int res = getResources().getIdentifier(icon, "drawable", getPackageName());
            Log.i(TAG, "in weathericon() getPackageName(): " + getPackageName());
            Log.i(TAG, "in weathericon() res: " + res);
            Drawable IconDrawable = resources.getDrawable(res);
            mIconBitmap = ((BitmapDrawable) IconDrawable).getBitmap();
            mIconResizedBitmap = Bitmap.createScaledBitmap(mIconBitmap, 100, 70, false);
            canvas.drawBitmap(mIconResizedBitmap, 90, 160, null);

        }

///end of weather stuff///




    }


}