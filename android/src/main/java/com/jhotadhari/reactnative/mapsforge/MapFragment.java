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

import android.content.Context;
import android.os.Bundle;
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
import org.mapsforge.map.android.util.AndroidPreferences;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.model.MapViewPosition;
import org.mapsforge.map.model.common.PreferencesFacade;

import java.util.ArrayList;

/**
 * Based on on this persons work: a7med on 08.03.18.
 * https://stackoverflow.com/questions/15494210/fragment-support-for-mapsforge#answer-49278354
 *
 */
public class MapFragment extends Fragment {
    // Abstract variables for displaying the map
    protected MapView mapView;
    protected PreferencesFacade preferencesFacade;

    protected ReactContext reactContext;

    // Initial variables for controlling the map
    protected static int propZoom = 12;
    protected static int propMinZoom = 3;
    protected static int propMaxZoom = 50;
    protected LatLong propCenterLatLong;

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


//        OpenStreetMapMapnik tileSource = OpenStreetMapMapnik.INSTANCE;
//        downloadLayer = new TileDownloadLayer(
//                this.tileCaches.get(0),
//                this.mapView.getModel().mapViewPosition,
//                tileSource,
//                AndroidGraphicFactory.INSTANCE
//        );
//        mapView.getLayerManager().getLayers().add( downloadLayer );


//        Log.d("MAPFILE", String.valueOf(getMapFileName()));
//        TileRendererLayer tileRendererLayer = AndroidUtil.createTileRendererLayer(
//                this.tileCaches.get(0),
//                this.mapView.getModel().mapViewPosition,
//                getMapFile(),
//                getRenderTheme(),
//                false,
//                true,
//                false
//        );
//        this.mapView.getLayerManager().getLayers().add(tileRendererLayer);

        WritableMap params = new WritableNativeMap();
        params.putInt( "nativeTag", this.getId() );
        Utils.sendEvent( reactContext, "MapLayersCreated", params );

        MapInputListener mapInputListener = new MapInputListener( this, this.reactContext, this.getId(), this.mapView );
        this.mapView.addInputListener( mapInputListener );
    }

    MapFragment( ReactContext reactContext_, ArrayList center, int zoom, int minZoom, int maxZoom ) {
        super();

        reactContext = reactContext_;

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


    /**
     * Template method to create the map views.
     */
    protected void createMapViews(View v)
    {
        mapView = initMapView(v);
        mapView.getModel().init(this.preferencesFacade);
        mapView.setClickable(true);
        mapView.getMapScaleBar().setVisible(true);
        mapView.setBuiltInZoomControls(false);
        mapView.setZoomLevelMin((byte) propMinZoom);
        mapView.setZoomLevelMax((byte) propMaxZoom);
        mapView.setZoomLevel((byte) propZoom);
        mapView.setCenter(new LatLong(propCenterLatLong.latitude, propCenterLatLong.longitude));
    }

    /**
     * Creates the shared preferences that are being used to store map view data over
     * activity restarts.
     */
    protected void createSharedPreferences() {
        this.preferencesFacade = new AndroidPreferences(getActivity().getSharedPreferences(getPersistableId(), Context.MODE_PRIVATE));
    }

    /**
     * Extracts the initial position from the map file, falling back onto the value supplied
     * by getDefaultInitialPosition if there is no initial position coded into the map file.
     * You will only need to override this method if you do not want the initial position extracted
     * from the map file.
     *
     * @return the initial position encoded in the map file or a fallback value.
     */
    protected MapPosition getInitialPosition() {
//        final MapDataStore mapFile = getMapFile();
//        if (mapFile.startPosition() != null) {
//            Byte startZoomLevel = mapFile.startZoomLevel();
//            if (startZoomLevel == null)
//            {
//                // it is actually possible to have no start zoom level in the file
//                startZoomLevel = new Byte((byte) propZoom);
//            }
//            return new MapPosition(mapFile.startPosition(), startZoomLevel);
//        }
        return new MapPosition(propCenterLatLong, (byte) propZoom);
    }

    /**
     * The persistable ID is used to store settings information, like the center of the last view
     * and the zoomlevel. By default the simple name of the class is used. The value is not user
     * visibile.
     *
     * @return the id that is used to save this mapview.
     */
    protected String getPersistableId()
    {
        return String.valueOf( this.getId() );
    }

    /**
     * Configuration method to set if a map view activity will have zoom controls.
     *
     * @return true if the map has standard zoom controls.
     */
    protected boolean hasZoomControls()
    {
        return true;
    }

    /**
     * Configuration method to set if map view activity's zoom controls hide automatically.
     *
     * @return true if zoom controls hide automatically.
     */
    protected boolean isZoomControlsAutoHide()
    {
        return true;
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

        createSharedPreferences();
        createMapViews(v);
        checkPermissionsAndCreateLayersAndControls();

        return v;
    }

    /**
     * Android Activity life cycle method.
     */
    @Override
    public void onPause() {
        mapView.getModel().save( this.preferencesFacade );
        this.preferencesFacade.save();
        super.onPause();
    }

    /**
     * Android Activity life cycle method.
     */
    @Override
    public void onDestroy() {
        mapView.destroyAll();
        AndroidGraphicFactory.clearResourceMemoryCache();
//        tileCaches.clear();   // ??? TODO clear cache for this fragment.
        super.onDestroy();
    }

//    /**
//     * Hook to purge tile caches.
//     * By default we purge every tile cache that has been added to the tileCaches list.
//     */
//    protected void purgeTileCaches()
//    {
//        for (TileCache tileCache : tileCaches)
//        {
//            tileCache.purge();
//        }
//
//        tileCaches.clear();
//    }

    /**
     * Creates a map view using an XML layout file supplied by getLayoutId() and finds
     * the map view component inside it with getMapViewId().
     *
     * @return the Android MapView for this activity.
     */
    protected MapView initMapView(View v)
    {
        return (MapView) v.findViewById(getMapViewId());
    }



}
