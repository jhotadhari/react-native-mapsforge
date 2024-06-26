/*
 * Copyright 2014 Ludwig M Brinckmann
 * Copyright 2015-2019 devemux86
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.jhotadhari.reactnative.mapsforge;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;

import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.MapPosition;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.Layer;
import org.mapsforge.map.model.MapViewPosition;
import org.mapsforge.map.model.common.Observer;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class MapFragment extends Fragment {

	protected MapView mapView;

	protected Observer observer = null;

    protected ReactContext reactContext;

    // Initial variables for controlling the map
    protected static int propZoom = 12;
    protected static int propMinZoom = 3;
    protected static int propMaxZoom = 50;
    protected LatLong propCenterLatLong;

    protected String hardwareKeyListenerUid = null;

    /**
     * Getter for the mapView instance.
     *
     * @return the MapView instance.
     */
    public MapView getMapView() {
        return mapView;
    }

    /**
     * @return the layout to be used,
     */
    protected int getLayoutId() {
        return R.layout.mapviewer;
    }

    /**
     * @return the id of the mapview inside the layout.
     */
    protected int getMapViewId() {
        return R.id.mapView;
    }

    /**
     * Hook to create map layers. You will need to create at least one layer to
     * have something visible on the map.
     */
    protected void createLayers() {
        WritableMap params = new WritableNativeMap();
        params.putInt( "nativeTag", this.getId() );
        Utils.sendEvent( reactContext, "MapLayersCreated", params );

        MapInputListener mapInputListener = new MapInputListener( this, this.reactContext, this.getId(), this.mapView );
        this.mapView.addInputListener( mapInputListener );
    }

	protected FixedWindowRateLimiter rateLimiter;

    MapFragment( ReactContext reactContext_, ArrayList center, int zoom, int minZoom, int maxZoom ) {
        super();

        reactContext = reactContext_;

		rateLimiter = new FixedWindowRateLimiter( 100, 1 );

        propCenterLatLong = new LatLong(
            (double) center.get(0),
            (double) center.get(1)
        );
        propZoom = zoom;
        propMinZoom = minZoom;
        propMaxZoom = maxZoom;
    }

    /**
     * Hook to create controls, such as scale bars.
     * You can add more controls.
     */
    protected void createControls() {
        initializePosition((MapViewPosition) mapView.getModel().mapViewPosition);
        mapView.setCenter(propCenterLatLong);
        mapView.setZoomLevel((byte) propZoom);
    }

    /**
     * The MaxTextWidthFactor determines how long a text may be before it is line broken. The
     * default setting should be good enough for most apps.
     *
     * @return the maximum text width factor for line breaking captions
     */
    protected float getMaxTextWidthFactor()
    {
        return 0.7f;
    }

    protected void addHardwareKeyListener() {
        try {
            HardwareKeyListener hardwareKeyListener = new HardwareKeyListener() {
                @Override
                public boolean onKeyUp(int keyCode, KeyEvent event) {
                    String keyCodeString = null;
                    for ( Field field : KeyEvent.class.getFields() ) {
                        if ( null == keyCodeString && field.getName().startsWith( "KEYCODE_" ) ) {
                            try {
                                int fieldKeyCode = (int) field.get( event );
                                if ( fieldKeyCode == keyCode ) {
                                    keyCodeString = field.getName();
                                }
                            } catch (IllegalAccessException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                    WritableMap params = new WritableNativeMap();
                    params.putInt( "keyCode", keyCode );
                    params.putString( "keyCodeString", keyCodeString );
                    Utils.sendEvent( reactContext, "onHardwareKeyUp", params );
                    return true;
                }
            };
            Class[] cArg = new Class[1];
            cArg[0] = HardwareKeyListener.class;
            Method meth = reactContext.getCurrentActivity().getClass().getMethod(
                    "addHardwareKeyListener",
                    cArg
            );
            Object value = meth.invoke(
                reactContext.getCurrentActivity(),
                hardwareKeyListener
            );
            String uid = (String) value;
            hardwareKeyListenerUid = uid;
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    protected void removeHardwareKeyListener() {
        try {
            if ( null != hardwareKeyListenerUid ) {
                Class[] cArg = new Class[1];
                cArg[0] = String.class;
                Method meth = reactContext.getCurrentActivity().getClass().getDeclaredMethod(
                        "removeHardwareKeyListener",
                        cArg
                );
                meth.invoke(
                        reactContext.getCurrentActivity(),
                        hardwareKeyListenerUid
                );
                hardwareKeyListenerUid = null;
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    /**
     * Template method to create the map views.
     */
    protected void createMapViews(View v) {
        mapView = initMapView(v);
        mapView.setClickable(true);
        mapView.getMapScaleBar().setVisible(true);
        mapView.setBuiltInZoomControls(false);
        mapView.setZoomLevelMin((byte) propMinZoom);
        mapView.setZoomLevelMax((byte) propMaxZoom);
        mapView.setZoomLevel((byte) propZoom);
        mapView.setCenter(new LatLong(propCenterLatLong.latitude, propCenterLatLong.longitude));
    }

    protected MapPosition getInitialPosition() {
        return new MapPosition(propCenterLatLong, (byte) propZoom);
    }

    /**
     * initializes the map view position.
     *
     * @param mvp the map view position to be set
     * @return the mapviewposition set
     */
    protected MapViewPosition initializePosition( MapViewPosition mvp ) {
        final LatLong center = mvp.getCenter();

        if (center.equals( propCenterLatLong ) ) {
            mvp.setMapPosition(this.getInitialPosition());
        }

        mvp.setZoomLevelMax( (byte) propMaxZoom );
        mvp.setZoomLevelMin( (byte) propMinZoom );

        return mvp;
    }

    /**
     * Hook to check for Android Runtime Permissions.
     */
    protected void checkPermissionsAndCreateLayersAndControls() {
        createLayers();
        createControls();
    }

    /**
     * Android Fragment life cycle method.
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(getLayoutId(), container, false );

        /**
         * App Initialization !!
         * behind the scenes, this initialization process gathers a bit of
         * information on your device, such as the screen resolution, that allows mapsforge to
         * automatically adapt the rendering for the device.
         */
        AndroidGraphicFactory.createInstance(this.getActivity().getApplication());
        createMapViews(v);
        checkPermissionsAndCreateLayersAndControls();
        addHardwareKeyListener();
		addObserver();

        return v;
    }

	public void addObserver() {
		observer = new Observer() {
			@Override
			public void onChange() {
				WritableMap params = getResponseBase();
				if ( rateLimiter.tryAcquire() ) {
					Utils.sendEvent( reactContext, "onFrameBuffer", params );
				}
			}
		};
		mapView.getModel().frameBufferModel.addObserver( observer );
	}

	public void removeObserver() {
		if ( null != observer ) {
			mapView.getModel().frameBufferModel.removeObserver( observer );
		}
	}

	protected WritableMap getResponseBase() {
		WritableMap params = new WritableNativeMap();
		params.putInt( "nativeTag", this.getId() );
		params.putArray( "center",  Utils.latLongToArray( mapView.getModel().mapViewPosition.getCenter() ) );
//		params.putDouble( "scaleFactor", mapView.getModel().mapViewPosition.getScaleFactor() );
		params.putInt( "zoom",  mapView.getModel().mapViewPosition.getZoomLevel() );
		return params;
	}

    protected void sendLifecycleEvent( String type ) {
        WritableMap params = getResponseBase();
        params.putString( "type", type );
        Utils.sendEvent( reactContext, "MapLifecycle", params );
    }

    @Override
    public void onPause() {

        sendLifecycleEvent( "onPause" );

		for ( Layer layer : getMapView().getLayerManager().getLayers()) {
            try {
				layer.getClass().getMethod("onPause").invoke( layer );
            } catch (NoSuchMethodException e) {
				//
            } catch (InvocationTargetException e) {
				//
            } catch (IllegalAccessException e) {
				//
            }
        }
        super.onPause();
    }

	@Override
	public void onResume() {
		super.onResume();

        sendLifecycleEvent( "onResume" );

		for ( Layer layer : getMapView().getLayerManager().getLayers()) {
			try {
				layer.getClass().getMethod("onResume").invoke( layer );
			} catch (NoSuchMethodException e) {
				//
			} catch (InvocationTargetException e) {
				//
			} catch (IllegalAccessException e) {
				//
			}
		}
	}

    @Override
    public void onDestroy() {
		removeObserver();
        mapView.destroyAll();
        removeHardwareKeyListener();
        AndroidGraphicFactory.clearResourceMemoryCache();
        super.onDestroy();
    }

    /**
     * Creates a map view using an XML layout file supplied by getLayoutId() and finds
     * the map view component inside it with getMapViewId().
     *
     * @return the Android MapView for this activity.
     */
    protected MapView initMapView(View v) {
        return (MapView) v.findViewById(getMapViewId());
    }

}
