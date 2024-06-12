// replace with your package
package com.jhotadhari.reactnative.mapsforge;

import android.view.Choreographer;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.uimanager.annotations.ReactPropGroup;
import com.facebook.react.uimanager.ViewGroupManager;
import com.facebook.react.uimanager.ThemedReactContext;

import java.util.ArrayList;
import java.util.Map;

public class MapViewManager extends ViewGroupManager<FrameLayout> {

  public static final String REACT_CLASS = "MapViewManager";
  public final int COMMAND_CREATE = 1;
  private int propWidth;
  private int propHeight;

  private int propZoom;
  private int propMinZoom;
  private int propMaxZoom;
  private ArrayList propCenter;

  ReactApplicationContext reactContext;

  public MapViewManager(ReactApplicationContext reactContext) {
    this.reactContext = reactContext;
  }

  @Override
  public String getName() {
    return REACT_CLASS;
  }

  /**
   * Return a FrameLayout which will later hold the Fragment
   */
  @Override
  public FrameLayout createViewInstance(ThemedReactContext reactContext) {
    return new FrameLayout(reactContext);
  }

  /**
   * Map the "create" command to an integer
   */
  @Nullable
  @Override
  public Map<String, Integer> getCommandsMap() {
    return MapBuilder.of("create", COMMAND_CREATE);
  }

  /**
   * Handle "create" command (called from JS) and call createFragment method
   */
  @Override
  public void receiveCommand(
    @NonNull FrameLayout root,
    String commandId,
    @Nullable ReadableArray args
  ) {
    super.receiveCommand(root, commandId, args);
    int reactNativeViewId = args.getInt(0);
    int commandIdInt = Integer.parseInt(commandId);

    switch (commandIdInt) {
      case COMMAND_CREATE:
        createFragment(root, reactNativeViewId);
        break;
      default: {}
    }
  }

  @ReactPropGroup(names = {"width", "height"})
  public void setReactPropsStyle(FrameLayout view, int index, Integer value) {
    if (index == 0) {
      propWidth = value;
    }
    if (index == 1) {
      propHeight = value;
    }
  }

  @ReactPropGroup(names = {"zoom", "minZoom","maxZoom"})
  public void setReactPropsZoom(FrameLayout view, int index, Integer value) {
    if (index == 0) {
      propZoom = value;
    }
    if (index == 1) {
      propMinZoom = value;
    }
    if (index == 2) {
      propMaxZoom = value;
    }
  }

  @ReactProp(name="center")
  public void setReactPropCenter(FrameLayout view, ReadableArray value) {
    propCenter = value.toArrayList();
  }

  /**
   * Replace your React Native view with a custom fragment
   */
  public void createFragment(FrameLayout root, int reactNativeViewId) {
    ViewGroup parentView = (ViewGroup) root.findViewById(reactNativeViewId);
    setupLayout(parentView);

    final MapFragment mapFragment = new MapFragment( reactContext, propCenter, propZoom, propMinZoom, propMaxZoom );
    FragmentActivity activity = (FragmentActivity) reactContext.getCurrentActivity();
    activity.getSupportFragmentManager()
            .beginTransaction()
            .replace(reactNativeViewId, mapFragment, String.valueOf(reactNativeViewId))
            .commit();
  }

  public void setupLayout(View view) {
    Choreographer.getInstance().postFrameCallback(new Choreographer.FrameCallback() {
      @Override
      public void doFrame(long frameTimeNanos) {
        manuallyLayoutChildren(view);
        view.getViewTreeObserver().dispatchOnGlobalLayout();
        Choreographer.getInstance().postFrameCallback(this);
      }
    });
  }

  /**
   * Layout all children properly
   */
  public void manuallyLayoutChildren(View view) {
      // propWidth and propHeight coming from react-native props
      int width = propWidth;
      int height = propHeight;

      view.measure(
              View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
              View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY));

      view.layout(0, 0, width, height);
  }
}
