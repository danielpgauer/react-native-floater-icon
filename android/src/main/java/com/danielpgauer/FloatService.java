package com.danielpgauer;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.ImageView;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Daniel P.Gauer on 18/08/18.
 */


@TargetApi(27)
public class FloatService extends Service {

    ImageView iconView = null;
    WindowManager windowManager;
    PackageManager packageManager;

    Binder binder = new FloatBinder();

    static Class mainActivity;
    static FloatService mService;
    static boolean mbound = false;
    public static boolean isFloating = false;
    static Context context;
    static IFloatService callback;

    public static void create(Class mainActivity, Context context, IFloatService callback) {
        if (FloatService.mainActivity != null) {
            return;
        }

        FloatService.mainActivity = mainActivity;
        FloatService.callback = callback;
        FloatService.context = context;
        Intent intent = new Intent(context, FloatService.class);
        context.startService(intent);

        context.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }
	
	public static FloatService getService() {
        return mService;
    }

    public static boolean isFloating() {
        return isFloating;
    }

    public void show() {
        if(mbound) {
            isFloating = true;
            if (iconView == null) {
                mService.floatApp(context.getPackageName());
            } else {
                iconView.setVisibility(View.VISIBLE);
            }

            callback.onShowHide(true);
        }
    }

    public void hide() {
        if(mbound) {
            isFloating = false;
            if (iconView != null) {
                iconView.setVisibility(View.INVISIBLE);
            }
            callback.onShowHide(false);
        }
    }

    public void unbind() {
        removeIconsFromScreen();
        context.unbindService(mConnection);
        try {
            stopSelf();
        } catch (Exception ex) {
        }
        mbound = false;
    }

    private static ServiceConnection mConnection =  new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d("FloaterIconer", "Connected to service");
            mService = ((FloatService.FloatBinder) service).getService();
            mbound = true;
            callback.created(mService);
            callback.onShowHide(isFloating);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d("FloaterIcon", "Disconnected from service");
            mbound = false;
        }
    };

    class IconHolder {
        public ImageView view;
        public int statusId;
        public String packageName;

        public Drawable defaultIcon;
        public Drawable statusIcon;
        public float x_pos;
        public float y_pos;

        IconHolder(ImageView view, Drawable baseIcon, String packageName) {
            this.view = view;
            this.defaultIcon = baseIcon;
            this.statusIcon = baseIcon;
            this.statusId = 0;
            this.packageName = new String(packageName);
        }
    }

    public class FloatBinder extends Binder {
        FloatService getService() {
            return FloatService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        packageManager = getPackageManager();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("FloaterIcon", "Service started with null intent");
        return START_STICKY;
    }

    @Override
    public void onTaskRemoved (Intent rootIntent) {
        Log.d("FloaterIcon", "onTaskRemoved");
        removeIconsFromScreen();
    }

    private void addIconToScreen(final String packageName, int x, int y) {
        if(iconView != null) {
            return;
        }

        iconView = new ImageView(this);

        Drawable draw = getIcon(packageName);
        iconView.setImageDrawable(draw);

        IconHolder iconHolder = new IconHolder(iconView, draw, packageName);
        iconView.setTag(iconHolder);

        final WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        setWindowParams(params, x, y);
        windowManager.addView(iconView, params);

        ViewConfiguration vc = ViewConfiguration.get(iconView.getContext());
        final int mScaledTouchSlop = 20;
        final int mLongPressTimeOut = vc.getLongPressTimeout();
        final int mTapTimeOut = vc.getTapTimeout();

        iconView.setOnTouchListener(new View.OnTouchListener() {
            private WindowManager.LayoutParams paramsF = params;
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        Log.d("FloaterIcon", "Action Down");
                        initialX = paramsF.x;
                        initialY = paramsF.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return false;
                    case MotionEvent.ACTION_MOVE:
                        Log.d("FloaterIcon", "Action Move");
                        paramsF.x = initialX + (int) (event.getRawX() - initialTouchX);
                        paramsF.y = initialY + (int) (event.getRawY() - initialTouchY);
                        windowManager.updateViewLayout(iconView, paramsF);
                        return false;
                    case MotionEvent.ACTION_UP:
                        Log.d("FloaterIcon", "Action Up");
                        Log.d("FloaterIcon", "DistanceX: " + Math.abs(initialTouchX - event.getRawX()));
                        Log.d("FloaterIcon", "DistanceY: " + Math.abs(initialTouchY - event.getRawY()));
                        Log.d("FloaterIcon", "elapsed gesture time: " + (event.getEventTime() - event.getDownTime()));
                        if((Math.abs(initialTouchX - event.getRawX()) <= mScaledTouchSlop) && (Math.abs(initialTouchY - event.getRawY()) <= mScaledTouchSlop)) {
                            if((event.getEventTime() - event.getDownTime()) < mTapTimeOut ) {
                                Log.d("FloaterIcon", "Click Detected");
                                startAppActivity(packageName);
                            } else if((event.getEventTime() - event.getDownTime()) >= mLongPressTimeOut) {
                                Log.d("FloaterIcon", "Long Click Detected");
                            }
                        }
                    default:
                        Log.d("FloaterIcon", "Action Default");
                        break;
                }
                return false;
            }

        });
    }

    private void startAppActivity(String packageName) {
        Intent intent = new Intent(this, mainActivity);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // You need this if starting
        //  the activity from a service
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        startActivity(intent);
    }

    private void setWindowParams(WindowManager.LayoutParams params, int x, int y) {
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.type = Build.VERSION.SDK_INT > 25
            ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            : WindowManager.LayoutParams.TYPE_PHONE;
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        params.format = PixelFormat.TRANSLUCENT;
        params.gravity = Gravity.TOP | Gravity.LEFT;
        if(x < 0 || y < 0) {
            params.y = dpToPx(100);
        } else {
            params.x = (int) x;
            params.y = (int) y;
        }
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }

    private Drawable getIcon(String packageName) {
        Log.v("FloaterIcon", "Using default image for icon");

        String resourceName = context.getPackageName() + ":" + "mipmap/ic_launcher";
        int resourceId = getResources().getIdentifier(resourceName, null, null);
        Drawable icon = getResources().getDrawableForDensity(resourceId, DisplayMetrics.DENSITY_XXHIGH);
        //ContextCompat.getDrawable(context, resourceId);

        return icon;
    }

    /*
     * Public binding methods used by attached application
     */

    public void floatApp(String packageName) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);
        addIconToScreen(
                packageName,
                metrics.widthPixels  - dpToPx(50),
                dpToPx(100)
        );
    }


    public void removeIconsFromScreen() {
        if (iconView != null) {
            windowManager.removeView(iconView);
            iconView = null;
        }
        //onDestroy();
    }

    @Override
    public void onDestroy() {
        Log.v("FloaterIcon", "destroy svc");
        super.onDestroy();
    }
}
