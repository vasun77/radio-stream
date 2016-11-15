package com.radiostream.javascript.proxy;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableArray;
import com.radiostream.di.components.DaggerJsProxyComponent;
import com.radiostream.di.components.JsProxyComponent;
import com.radiostream.di.modules.ReactContextModule;
import com.radiostream.networking.MetadataBackend;
import com.radiostream.networking.models.PlaylistsResult;
import com.radiostream.player.PlayerService;

import java.util.HashMap;
import java.util.Map;

import hugo.weaving.DebugLog;
import timber.log.Timber;

import static android.content.Context.BIND_AUTO_CREATE;

/**
 * Created by vitaly on 11/11/2016.
 */

@DebugLog
public class PlayerJsProxy extends ReactContextBaseJavaModule implements LifecycleEventListener {

  private static JsProxyComponent mJsProxyComponent = null;
  private PlayerService mPlayerService = null;

  private ServiceConnection mServiceConnection = new ServiceConnection() {
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
      Timber.i("Function start");
      PlayerService.PlayerServiceBinder binder = (PlayerService.PlayerServiceBinder) service;
      mPlayerService = binder.getService();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
      Timber.i("Function start");
      mPlayerService = null;
    }
  };

  public PlayerJsProxy(ReactApplicationContext reactContext) {
    super(reactContext);

    reactContext.addLifecycleEventListener(this);

    mJsProxyComponent = DaggerJsProxyComponent.builder().reactContextModule(new ReactContextModule(reactContext)).build();
    mJsProxyComponent.inject(this);
  }

  public static JsProxyComponent JsProxyComponent() {
    if (mJsProxyComponent == null) {
      throw new RuntimeException("Remote service was not initialized");
    }

    return mJsProxyComponent;
  }

  @Override
  public String getName() {
    return "PlayerJsProxy";
  }

  @Override
  public Map<String, Object> getConstants() {
    final Map<String, Object> constants = new HashMap<>();
    return constants;
  }

  @ReactMethod
  public void playPlaylist() {
  }

  @Override
  public void onHostResume() {
    Activity activity = this.getCurrentActivity();
    Timber.i("activity: %s", activity.toString());

    Intent musicServiceIntent = new Intent(activity, PlayerService.class);
    activity.bindService(musicServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    activity.startService(musicServiceIntent);
  }

  @Override
  public void onHostPause() {
    this.getCurrentActivity().unbindService(mServiceConnection);
  }

  @Override
  public void onHostDestroy() {

  }
}