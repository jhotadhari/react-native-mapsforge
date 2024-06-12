package com.jhotadhari.reactnative.mapsforge;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import android.graphics.BitmapFactory;

import org.mapsforge.core.graphics.Bitmap;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.android.graphics.AndroidBitmap;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.overlay.Marker;

public class MapMarkerModule extends ReactContextBaseJavaModule {

    protected Map<Integer, Marker> markers;

    MapMarkerModule(ReactApplicationContext context) {
        super(context);
        markers = new HashMap<>();
    }

    @Override
    public String getName() {
        return "MapMarkerModule";
    }

    protected Bitmap getBitmap( String path, int width, int height ) {
        android.graphics.Bitmap bitmap = null;

        if ( path.startsWith( "/" ) ) {
            File imgFile = new File( path );
            if( imgFile.exists() ) {
                bitmap = BitmapFactory.decodeFile( imgFile.getAbsolutePath() );
            }
        }

        if ( null == bitmap ) {
            bitmap = BitmapFactory.decodeResource(
                this.getReactApplicationContext().getResources(),
                R.drawable.marker_default
            );
        }

        bitmap = android.graphics.Bitmap.createScaledBitmap(
            bitmap,
            width,
            height,
            false
        );

        return new AndroidBitmap( bitmap );
    }

    @ReactMethod
    public void createMarker(int reactTag, int tabDistanceThreshold, ReadableArray latLong, ReadableMap icon, int reactTreeIndex, Promise promise ) {
        try {
            Bitmap bitmap = getBitmap(
                icon.getString( "path" ),
                icon.getInt( "width" ),
                icon.getInt( "height" )
            );
            MapView mapView = (MapView) Utils.getMapView( this.getReactApplicationContext(), reactTag );
            if ( null == mapView ) {
                promise.resolve( false );
                return;
            }
            Marker marker = new TouchMarker(
                this.getReactApplicationContext(),
                tabDistanceThreshold,
                new LatLong(
                    (Double) latLong.toArrayList().get(0),
                    (Double) latLong.toArrayList().get(1)
                ),
                bitmap,
                (int) icon.getArray("anchor").getDouble(0 ),
                (int) icon.getArray("anchor").getDouble(1 )
            );
            mapView.getLayerManager().getLayers().add(
                    Math.min( mapView.getLayerManager().getLayers().size(), (int) reactTreeIndex ),
                    marker
            );
            int hash = marker.hashCode();
            markers.put( hash, marker );
            promise.resolve(hash);
        } catch(Exception e) {
            promise.reject("Create Event Error", e);
        }
    }

    @ReactMethod
    public void setMarkerLocation( int reactTag, int hash, ReadableArray latLong, Promise promise ) {
        try {
            MapView mapView = (MapView) Utils.getMapView( this.getReactApplicationContext(), reactTag );
            if ( null == mapView ) {
                promise.resolve( false );
                return;
            }
            Marker marker = markers.get( hash );
            marker.setLatLong( new LatLong(
                    (Double) latLong.toArrayList().get(0),
                    (Double) latLong.toArrayList().get(1)
            ) );
            marker.requestRedraw();
            promise.resolve(hash);
        } catch(Exception e) {
            promise.reject("Create Event Error", e);
        }
    }
    @ReactMethod
    public void setMarkerIcon( int reactTag, int hash, ReadableMap icon, Promise promise ) {
        try {
            MapView mapView = (MapView) Utils.getMapView( this.getReactApplicationContext(), reactTag );
            if ( null == mapView ) {
                promise.resolve( false );
                return;
            }
            Marker marker = markers.get( hash );
            Bitmap bitmap = getBitmap(
                icon.getString( "path" ),
                icon.getInt( "width" ),
                icon.getInt( "height" )
            );
            marker.setBitmap( bitmap );
            marker.setHorizontalOffset( (int) icon.getArray("anchor").getDouble(0 ) );
            marker.setVerticalOffset( (int) icon.getArray("anchor").getDouble(1 ) );
            marker.requestRedraw();
            promise.resolve(hash);
        } catch(Exception e) {
            promise.reject("Create Event Error", e);
        }
    }

    @ReactMethod
    public void removeMarker(int reactTag, int hash, Promise promise) {
        try {
            MapView mapView = (MapView) Utils.getMapView( this.getReactApplicationContext(), reactTag );
            if ( null == mapView ) {
                promise.resolve( false );
                return;
            }
            mapView.getLayerManager().getLayers().remove( markers.get( hash ) );
            promise.resolve(hash);
        } catch(Exception e) {
            promise.reject("Create Event Error", e);
        }
    }

}