package com.jhotadhari.reactnative.mapsforge;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;

import org.mapsforge.map.android.util.AndroidUtil;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.datastore.MapDataStore;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.renderer.TileRendererLayer;
import org.mapsforge.map.reader.MapFile;
import org.mapsforge.map.rendertheme.ExternalRenderTheme;
import org.mapsforge.map.rendertheme.InternalRenderTheme;
import org.mapsforge.map.rendertheme.XmlRenderTheme;
import org.mapsforge.map.rendertheme.XmlRenderThemeStyleLayer;
import org.mapsforge.map.rendertheme.XmlRenderThemeStyleMenu;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.mapsforge.map.rendertheme.XmlRenderThemeMenuCallback;

import java.util.Set;

public class MapLayerMapsforgeModule extends ReactContextBaseJavaModule {

    public String getName() {
        return "MapLayerMapsforgeModule";
    }

    protected class RenderThemeParser implements XmlRenderThemeMenuCallback {

        protected String renderThemePath;
        protected String renderStyle;
        protected ReadableArray renderOverlays;
        protected int reactTag;

        RenderThemeParser(
                int reactTag_,
                String renderThemePath_,
                String renderStyle_,
                ReadableArray renderOverlays_
        ) {
            renderThemePath = renderThemePath_;
            reactTag = reactTag_;
            renderStyle = renderStyle_;
            renderOverlays = renderOverlays_;
        }

        @Override
        public Set<String> getCategories( XmlRenderThemeStyleMenu menuStyle ) {

            addRenderThemeStyleMenu( renderThemePath, reactTag, menuStyle );

            XmlRenderThemeStyleLayer baseLayer = menuStyle.getLayer( renderStyle );
            if ( baseLayer == null ) {
                baseLayer = menuStyle.getLayer( menuStyle.getDefaultValue() );
                if ( baseLayer == null ) {
                    return null;
                }
            }

            Set<String> result = baseLayer.getCategories();

            // add the categories from overlays that are enabled
            for (XmlRenderThemeStyleLayer overlay : baseLayer.getOverlays()) {
                if ( renderOverlays.toArrayList().contains( overlay.getId() ) ) {
                    result.addAll(overlay.getCategories());
                }
            }

            return result;
        }
    }

    protected Map<Integer, TileRendererLayer> layers = new HashMap<>();
    protected Map<Integer, String> cachesMap = new HashMap<>();

    protected Map<String, XmlRenderThemeStyleMenu> renderThemeStyleMenus = new HashMap<>();

    MapLayerMapsforgeModule(ReactApplicationContext context) {
        super(context);
    }

    public void addRenderThemeStyleMenu( String filePath, int reactTag, XmlRenderThemeStyleMenu renderThemeStyleMenu ) {
        if ( ! renderThemeStyleMenus.containsKey( filePath ) ) {

            renderThemeStyleMenus.put(
                filePath,
                renderThemeStyleMenu
            );

            WritableMap params = new WritableNativeMap();
            params.putInt( "nativeTag", reactTag );
            params.putString( "filePath", filePath );
            params.putMap( "collection", parseRenderThemeOptions( filePath ) );

            Utils.sendEvent( this.getReactApplicationContext(), "RenderThemeParsed", params );
        }
    }

    protected WritableMap parseRenderThemeOptions( String renderThemePath ) {
        XmlRenderThemeStyleMenu renderThemeStyleMenu = this.renderThemeStyleMenus.get( renderThemePath );

        if ( null == renderThemeStyleMenu ) {
            return null;
        }

        Map layers = renderThemeStyleMenu.getLayers();
        WritableMap response = new WritableNativeMap();

        for ( Object key : layers.keySet() ) {
            XmlRenderThemeStyleLayer layer = (XmlRenderThemeStyleLayer) layers.get(String.valueOf(key));
            if ( ! layer.isEnabled() && layer.isVisible() ) {

                WritableMap responseItem = new WritableNativeMap();
                WritableMap opts = new WritableNativeMap();
                for (XmlRenderThemeStyleLayer overlay : layer.getOverlays()) {
                    opts.putString(
                            String.valueOf( overlay.getId() ),
                            overlay.getTitle( renderThemeStyleMenu.getDefaultLanguage() )
                    );
                }
                responseItem.putString( "value", layer.getId() );
                responseItem.putString( "label", layer.getTitle( renderThemeStyleMenu.getDefaultLanguage() ) );
                responseItem.putMap( "options", opts );
                if ( Objects.equals( layer.getId(), renderThemeStyleMenu.getDefaultValue() ) )  {
                    responseItem.putBoolean( "default", true );
                }

                response.putMap(
                        String.valueOf( layer.getId() ),
                        responseItem
                );
            }
        }

        return response;
    }

    @ReactMethod
    public void getRenderThemeOptions( String renderThemePath, Promise promise ) {
        try {
            WritableMap response = parseRenderThemeOptions( renderThemePath );
            promise.resolve( response );
        } catch(Exception e) {
            promise.reject("Create Event Error", e);
        }
    }

    protected XmlRenderTheme getRenderTheme(
            int reactTag,
            String renderThemePath,
            String renderStyle,
            ReadableArray renderOverlays
    ) {
        switch( renderThemePath ) {
            case "DEFAULT":
            case "OSMARENDER":
                return InternalRenderTheme.valueOf( renderThemePath );
            default:
                try {
                    return new ExternalRenderTheme( new File( renderThemePath ), new RenderThemeParser(
                        reactTag,
                        renderThemePath,
                        renderStyle,
                        renderOverlays
                    ) );
                } catch ( FileNotFoundException e ) {
                    return InternalRenderTheme.DEFAULT;
                }
        }
    }

    @ReactMethod
    public void createLayer(
            int reactTag,
            String mapFileName,
            String renderThemePath,
            String renderStyle,
            int cachePersistence,
            ReadableArray renderOverlays,
            int reactTreeIndex,
            Promise promise
    ) {
        try {
            MapFragment mapFragment = Utils.getMapFragment( this.getReactApplicationContext(), reactTag );
            MapView mapView = (MapView) Utils.getMapView( this.getReactApplicationContext(), reactTag );

            if ( mapFragment == null || null == mapView ) {
                promise.resolve( false );
                return;
            }
            File mapfile = new File( mapFileName );
            if ( ! mapfile.exists() ) {
                promise.resolve( false );
                return;
            }

			String persistableId = mapFileName + renderThemePath + renderStyle + renderOverlays.toArrayList().toString();

            TileCache tileCache = MapTileCacheController.getInstance( this.getReactApplicationContext() ).addCache(
				persistableId,
                mapView,
				cachePersistence > 0
            );

            MapDataStore mmapfile = new MapFile( mapfile );

            TileRendererLayer tileRendererLayer = AndroidUtil.createTileRendererLayer(
                    tileCache,
                    mapView.getModel().mapViewPosition,
                    mmapfile,
                    getRenderTheme(
                            reactTag,
                            renderThemePath,
                            renderStyle,
                            renderOverlays
                    ),
                    false,
                    true,
                    false
            );

            mapView.getLayerManager().getLayers().add(
                    Math.min( mapView.getLayerManager().getLayers().size(), (int) reactTreeIndex ),
                    tileRendererLayer
            );
            int hash = tileRendererLayer.hashCode();
            layers.put( hash, tileRendererLayer );
            cachesMap.put( hash, persistableId );
            promise.resolve(hash);
        } catch(Exception e) {
            promise.reject("Create Event Error", e);
        }
    }

    @ReactMethod
    public void removeLayer(int reactTag, int hash, Boolean forcePurge, Promise promise) {
        try {
			// Remove from mapView
            MapView mapView = (MapView) Utils.getMapView( this.getReactApplicationContext(), reactTag );
            if ( null == mapView ) {
                promise.resolve( false );
                return;
            }
            mapView.getLayerManager().getLayers().remove( layers.get( hash ) );

			// Remove from layers
			layers.remove( hash );

			// Remove from tileCacheController
			String persistableId = cachesMap.get( hash );
			if ( null != persistableId ) {
				MapTileCacheController tileCacheController = MapTileCacheController.getInstance( this.getReactApplicationContext() );
				tileCacheController.removeCache( persistableId, forcePurge );
			}

			// Remove from cachesMap
			cachesMap.remove( hash );

			promise.resolve( hash );
        } catch(Exception e) {
            promise.reject("Create Event Error", e);
        }
    }

}
