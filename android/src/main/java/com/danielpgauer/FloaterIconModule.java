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
    return "FloaterIconModule";
  }

  private WritableMap makeErrorPayload(Exception ex) {
    WritableMap error = Arguments.createMap();
    error.putString("message", ex.getMessage());
    return error;
  }

  @ReactMethod
  public void show(Callback callback) {
	try {
		FloatService.getService().show();
		
		callback.invoke(null, null);
	} catch (Exception ex) {
		ex.printStackTrace();
		callback.invoke(makeErrorPayload(ex));
    }
  }

  @ReactMethod
  public void hide(Callback callback) {
    Uri uri = Uri.parse(uriString);
    
	try {
		FloatService.getService().hide();
		
		callback.invoke(null, null);
	} catch (Exception ex) {
		ex.printStackTrace();
		callback.invoke(makeErrorPayload(ex));
    }
  }
}
