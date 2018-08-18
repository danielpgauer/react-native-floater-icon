package com.danielpgauer;

import android.content.ContentUris;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.database.Cursor;

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

public class FloaterIconModule extends ReactContextBaseJavaModule {

  static ReactApplicationContext RCTContext;

  public FloaterIconModule(ReactApplicationContext reactContext) {
    super(reactContext);

    RCTContext = reactContext;
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
  public void isFloating() {
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
}
