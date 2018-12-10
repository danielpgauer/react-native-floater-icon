package com.danielpgauer;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.database.Cursor;

import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;

import com.danielpgauer.FloatService;
import com.danielpgauer.IFloatService;

public class FloaterIconModule extends ReactContextBaseJavaModule
  implements LifecycleEventListener {

  static ReactApplicationContext RCTContext;

  static FloatService floatService;

  public FloaterIconModule(ReactApplicationContext reactContext) {
    super(reactContext);

    RCTContext = reactContext;

    reactContext.addLifecycleEventListener(this);
  }

  @Override
  public String getName() {
    return "FloaterIcon";
  }

  private WritableMap makeErrorPayload(Exception ex) {
    WritableMap error = Arguments.createMap();
    error.putString("message", ex.getMessage());
    return error;
  }

  @ReactMethod
  public void start(final Promise p) {
    if (floatService != null) {
      //p.reject("ERROR", new Error("FloaterIcon already started"));
      //return;
    }

    try {
      Activity currentActivity = getCurrentActivity();
      if (currentActivity == null) {
        p.reject("ERROR", new Error("Activity doesn't exist"));
        return;
      }

      FloatService.create(currentActivity.getClass(), RCTContext, new IFloatService() {
          @Override
          public void created(FloatService service) {
              floatService = service;
              p.resolve(true);
          }

          @Override
          public void onShowHide(boolean show) {
          }
      });
     } catch (Exception ex) {
      ex.printStackTrace();
      p.reject("ERROR", new Error(ex.getMessage()));
    }
  }

  @ReactMethod
  public void stop(Promise p) {
    try {
      FloatService.getService().unbind();

      floatService = null;
      
      p.resolve(true);
    } catch (Exception ex) {
      ex.printStackTrace();
      p.reject("ERROR", new Error(ex.getMessage()));
    }
  }

  @ReactMethod
  public void isFloating(Promise p) {
    try {
      boolean isFloating = FloatService.getService().isFloating();
      
      p.resolve(isFloating);
    } catch (Exception ex) {
      ex.printStackTrace();
      p.reject("ERROR", new Error(ex.getMessage()));
    }
  }

  @ReactMethod
  public void show(Promise p) {
    try {
      FloatService.getService().show();
      
      p.resolve(true);
    } catch (Exception ex) {
      ex.printStackTrace();
      p.reject("ERROR", new Error(ex.getMessage()));
    }
  }

  @ReactMethod
  public void hide(Promise p) {
    try {
      FloatService.getService().hide();
      
      p.resolve(true);
    } catch (Exception ex) {
      ex.printStackTrace();
      p.reject("ERROR", new Error(ex.getMessage()));
    }
  }

  @Override
  public void onHostResume() {
      // Activity `onResume`
  }

  @Override
  public void onHostPause() {
      // Activity `onPause`
  }

  @Override
  public void onHostDestroy() {
      if (floatService != null) {
        try {
            floatService.unbind();
        } catch (Exception e) {
        }
      }
  }
}
