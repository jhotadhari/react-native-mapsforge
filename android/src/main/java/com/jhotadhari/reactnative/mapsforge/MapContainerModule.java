package com.jhotadhari.reactnative.mapsforge;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;

import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.android.view.MapView;

import java.util.ArrayList;

public class MapContainerModule extends ReactContextBaseJavaModule {

    MapContainerModule(ReactApplicationContext context) {
        super(context);
    }

    @Override
    public String getName() {
        return "MapContainerModule";
    }

    @ReactMethod
    public void getLayersCreated(int reactTag, Promise promise ) {
        try {
            MapView mapView = (MapView) Utils.getMapView( this.getReactApplicationContext(), reactTag );
            if ( null == mapView ) {
                promise.resolve( false );
                return;
            }
            promise.resolve( true );
        } catch(Exception e) {
            promise.reject("Create Event Error", e);
        }
    }
    @ReactMethod
    public void setZoom(int reactTag, int zoom, Promise promise ) {
        try {
            MapView mapView = (MapView) Utils.getMapView( this.getReactApplicationContext(), reactTag );
            if ( null == mapView ) {
                promise.resolve( false );
                return;
            }
            mapView.setZoomLevel( (byte) zoom );

            promise.resolve(true);
        } catch(Exception e) {
            promise.reject("Create Event Error", e);
        }
    }

    @ReactMethod
    public void setCenter(int reactTag, ReadableArray center, Promise promise ) {
        try {
            MapView mapView = (MapView) Utils.getMapView( this.getReactApplicationContext(), reactTag );
            if ( null == mapView ) {
                promise.resolve( false );
                return;
            }
            mapView.setCenter( new LatLong(
                (double) center.toArrayList().get(0),
                (double) center.toArrayList().get(1)
            ) );
            promise.resolve(true);
        } catch(Exception e) {
            promise.reject("Create Event Error", e);
        }
    }

    @ReactMethod
    public void setMinZoom(int reactTag, int minZoom, Promise promise ) {
        try {
            MapView mapView = (MapView) Utils.getMapView( this.getReactApplicationContext(), reactTag );
            if ( null == mapView ) {
                promise.resolve( false );
                return;
            }
            mapView.setZoomLevelMin( (byte) minZoom );
            promise.resolve(true);
        } catch(Exception e) {
            promise.reject("Create Event Error", e);
        }
    }
    @ReactMethod
    public void setMaxZoom(int reactTag, int maxZoom, Promise promise ) {
        try {
            MapView mapView = (MapView) Utils.getMapView( this.getReactApplicationContext(), reactTag );
            if ( null == mapView ) {
                promise.resolve( false );
                return;
            }
            mapView.setZoomLevelMax( (byte) maxZoom );
            promise.resolve(true);
        } catch(Exception e) {
            promise.reject("Create Event Error", e);
        }
    }
    @ReactMethod
    public void zoomIn(int reactTag, Promise promise ) {
        try {
            MapView mapView = (MapView) Utils.getMapView( this.getReactApplicationContext(), reactTag );
            if ( null == mapView ) {
                promise.resolve( false );
                return;
            }
            mapView.setZoomLevel( (byte) ( mapView.getModel().mapViewPosition.getZoomLevel() + 1 ) );
            promise.resolve(true);
        } catch(Exception e) {
            promise.reject("Create Event Error", e);
        }
    }
    @ReactMethod
    public void zoomOut(int reactTag, Promise promise ) {
        try {
            MapView mapView = (MapView) Utils.getMapView( this.getReactApplicationContext(), reactTag );
            if ( null == mapView ) {
                promise.resolve( false );
                return;
            }
            mapView.setZoomLevel( (byte) ( mapView.getModel().mapViewPosition.getZoomLevel() - 1 ) );
            promise.resolve(true);
        } catch(Exception e) {
            promise.reject("Create Event Error", e);
        }
    }

}