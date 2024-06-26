package com.jhotadhari.reactnative.mapsforge;

import android.view.MotionEvent;

import androidx.annotation.Nullable;

import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import org.mapsforge.core.model.Dimension;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.MapPosition;
import org.mapsforge.core.util.LatLongUtils;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.view.InputListener;

public class MapInputListener implements InputListener {

    protected MapFragment mapFragment;
    protected ReactContext reactContext;

    protected MapView mapView;

    protected int nativeTag;

    MapInputListener( MapFragment mapFragment_, ReactContext reactContext_, int nativeTag_, MapView mapView_ ) {
        mapFragment = mapFragment_;
        reactContext = reactContext_;
        nativeTag = nativeTag_;
        mapView = mapView_;
    }

    @Override
    public void onMoveEvent() {
        WritableMap params = new WritableNativeMap();
        params.putInt( "nativeTag", nativeTag );
        LatLong latLong = mapView.getModel().mapViewPosition.getMapPosition().latLong;
        WritableArray latLongA = new WritableNativeArray();
        latLongA.pushDouble( latLong.latitude );
        latLongA.pushDouble( latLong.longitude );
        params.putArray( "center",  latLongA );
        Utils.sendEvent( reactContext, "onMoveStart", params );
    };

    @Override
    public void onZoomEvent() {
        WritableMap params = new WritableNativeMap();
        params.putInt( "nativeTag", nativeTag );
        params.putInt( "zoom",  mapView.getModel().mapViewPosition.getZoomLevel() );
        Utils.sendEvent( reactContext, "MapZoom", params );
    };

}
