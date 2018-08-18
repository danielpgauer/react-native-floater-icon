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

    static FloatService mService;
    static boolean mbound = false;
    public static boolean isFloating = false;
    static Context context;
    static IFloatService callback;

    public static void create(Context context, IFloatService callback) {
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
                mService.floatApp(BuildConfig.APPLICATION_ID, 0);
            } else {
                iconView.setVisibility(View.VISIBLE);
            }

            callback.onShowHide(true);
        }
    }

    public void hide() {
        if(mbound) {
            isFloating = false;
            iconView.setVisibility(View.INVISIBLE);
            callback.onShowHide(false);
        }
    }

    public void unbind() {
        context.unbindService(mConnection);
        mbound = false;
    }

    private static ServiceConnection mConnection =  new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d("AppFloater", "Connected to service");
            mService = ((FloatService.FloatBinder) service).getService();
            mbound = true;
            callback.created(mService);
            callback.onShowHide(isFloating);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d("AppFloat", "Disconnected from service");
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
        Log.d("AppFloat", "Service started with null intent");
        return START_STICKY;
    }

    @Override
    public void onTaskRemoved (Intent rootIntent) {
        Log.d("AppFloat", "onTaskRemoved");
        removeIconsFromScreen();
    }

    private void addIconToScreen(final String packageName, int resourceId, int x, int y) {
        if(iconView != null) {
            return;
        }

        iconView = new ImageView(this);

        Drawable draw = getIcon(packageName, resourceId);
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
                        Log.d("AppFloat", "Action Down");
                        initialX = paramsF.x;
                        initialY = paramsF.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return false;
                    case MotionEvent.ACTION_MOVE:
                        Log.d("AppFloat", "Action Move");
                        paramsF.x = initialX + (int) (event.getRawX() - initialTouchX);
                        paramsF.y = initialY + (int) (event.getRawY() - initialTouchY);
                        windowManager.updateViewLayout(iconView, paramsF);
                        return false;
                    case MotionEvent.ACTION_UP:
                        Log.d("AppFloat", "Action Up");
                        Log.d("AppFloat", "DistanceX: " + Math.abs(initialTouchX - event.getRawX()));
                        Log.d("AppFloat", "DistanceY: " + Math.abs(initialTouchY - event.getRawY()));
                        Log.d("AppFloat", "elapsed gesture time: " + (event.getEventTime() - event.getDownTime()));
                        if((Math.abs(initialTouchX - event.getRawX()) <= mScaledTouchSlop) && (Math.abs(initialTouchY - event.getRawY()) <= mScaledTouchSlop)) {
                            if((event.getEventTime() - event.getDownTime()) < mTapTimeOut ) {
                                Log.d("AppFloat", "Click Detected");
                                startAppActivity(packageName);
                            } else if((event.getEventTime() - event.getDownTime()) >= mLongPressTimeOut) {
                                Log.d("AppFloat", "Long Click Detected");
                            }
                        }
                    default:
                        Log.d("AppFloat", "Action Default");
                        break;
                }
                return false;
            }

        });
    }

    private void startAppActivity(String packageName) {
        /*
        Intent intent = packageManager.getLaunchIntentForPackage(packageName);
            if(intent != null) {
                    startActivity(intent);
            }
         */
        Intent intent = new Intent(this, MainActivity.class);
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

    private Drawable getIcon(String packageName, int resourceId) {
        Drawable icon = null;

        if(resourceId == 0) {
            Log.v("AppFloat", "Resource ID not found, attempting to load package icon");
            try {
                icon = getPackageIcon(packageName);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            Log.v("AppFloat", "Loading image using Resource ID");
            icon = getPackageImage(packageName, resourceId);
        }

        if(icon == null) {
            Log.v("AppFloat", "Using default image for icon");
            icon = getResources().getDrawable(R.drawable.ic_launcher);
        }

        return icon;
    }

    private Drawable getPackageImage(String packageName, int resourceId) {
        return packageManager.getDrawable(packageName, resourceId, null);
    }

    private Drawable getPackageIcon(String appPackage) throws PackageManager.NameNotFoundException {
        Drawable draw = null;
        try {
            draw = packageManager.getApplicationIcon(appPackage);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return draw;
    }

    private Drawable mergeBitmap(Drawable base, Drawable status) {
        Bitmap bitmap = Bitmap.createBitmap(base.getIntrinsicWidth(), base.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bitmap);

        int top = base.getIntrinsicHeight() - status.getIntrinsicHeight();

        base.setBounds(0, 0, base.getIntrinsicWidth(), base.getIntrinsicHeight());
        status.setBounds(0, 70, 30, 100);

        base.draw(c);
        status.draw(c);

        Drawable draw = new BitmapDrawable(getResources(), bitmap);
        return draw;
    }

    /*
     * Public binding methods used by attached application
     */


    public void updateIconStatus(int statusId) {
        Drawable statusIcon;
        IconHolder holder = (IconHolder) iconView.getTag();

        Log.d("AppFloat", "updateIcon position: " + statusId);
        switch(statusId) {
            case 1:
                statusIcon = getResources().getDrawable(R.drawable.one);
                break;
            case 2:
                statusIcon = getResources().getDrawable(R.drawable.two);
                break;
            case 3:
                statusIcon = getResources().getDrawable(R.drawable.three);
                break;
            case 4:
                statusIcon =  getResources().getDrawable(R.drawable.four);
                break;
            case 5:
                statusIcon = getResources().getDrawable(R.drawable.five);
                break;
            case 6:
                statusIcon = getResources().getDrawable(R.drawable.six);
                break;
            case 7:
                statusIcon = getResources().getDrawable(R.drawable.seven);
                break;
            case 8:
                statusIcon = getResources().getDrawable(R.drawable.eight);
                break;
            case 9:
                statusIcon = getResources().getDrawable(R.drawable.nine);
                break;
            default:
                statusIcon = getResources().getDrawable(R.drawable.nineplus);
        }

        holder.statusId = statusId;
        holder.statusIcon = mergeBitmap(holder.defaultIcon, statusIcon);
        holder.view.setImageDrawable(holder.statusIcon);
    }

    public void floatApp(String packageName, int resourceId) {
        addIconToScreen(packageName, resourceId, -1, -1);
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
        Log.v("AppFloat", "destroy svc");
        super.onDestroy();
    }
}
