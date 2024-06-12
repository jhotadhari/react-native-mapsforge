package com.jhotadhari.reactnative.mapsforge;

import static com.facebook.react.bridge.ReadableType.Array;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableType;

import org.mapsforge.core.graphics.Paint;
import org.mapsforge.core.graphics.Style;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;

import org.mapsforge.core.graphics.Color;
import org.mapsforge.map.android.view.MapView;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.ticofab.androidgpxparser.parser.GPXParser;
import io.ticofab.androidgpxparser.parser.domain.Gpx;
import io.ticofab.androidgpxparser.parser.domain.TrackPoint;

public class MapPolylineModule extends ReactContextBaseJavaModule {

    protected Map<Integer, TouchPolyline> features;

    MapPolylineModule(ReactApplicationContext context) {
        super(context);
        features = new HashMap<>();
    }

    @Override
    public String getName() {
        return "MapPolylineModule";
    }


    static Paint createPaint(int color, int strokeWidth, Style style) {
        Paint paint = AndroidGraphicFactory.INSTANCE.createPaint();
        paint.setColor(color);
        paint.setStrokeWidth(strokeWidth);
        paint.setStyle(style);
        return paint;
    }

    protected static List positionsTolatLongsList( ReadableArray positions ) {
        List<LatLong> latLongs = new ArrayList<>();
        for (int index = 0; index < positions.size(); index++) {
            ReadableType readableType = positions.getType(index);
            if ( readableType == Array ) {
                latLongs.add( new LatLong(
                        (Double) positions.getArray( index ).toArrayList().get(0),
                        (Double) positions.getArray (index ).toArrayList().get(1)
                ) );
            }
        }
        return latLongs;
    }

    @ReactMethod
    public void create(int reactTag, int tabDistanceThreshold, ReadableArray positions, String filePath, int reactTreeIndex, Promise promise ) {
        try {
            MapView mapView = (MapView) Utils.getMapView( this.getReactApplicationContext(), reactTag );
            if ( null == mapView ) {
                promise.resolve( false );
                return;
            }

            Paint paintStroke = createPaint(
                    AndroidGraphicFactory.INSTANCE.createColor(Color.RED),
                    15,
                    Style.STROKE
            );

            TouchPolyline feature = new TouchPolyline(
                getReactApplicationContext(),
                mapView,
                tabDistanceThreshold,
                paintStroke,
                AndroidGraphicFactory.INSTANCE,
                true
            );

            if ( null != positions && positions.size() > 0 ) {
                feature.setPoints(positionsTolatLongsList(positions));
            } else if ( filePath != null && filePath.length() > 0 && filePath.startsWith( "/" ) && filePath.endsWith( ".gpx" ) ) {
                File gpxFile = new File( filePath );
                if( gpxFile.exists() ) {
                    GPXParser parser = new GPXParser();
                    Gpx parsedGpx = null;
                    try {
                        InputStream in = new FileInputStream(gpxFile);
                        parsedGpx = parser.parse(in);
                    } catch (IOException | XmlPullParserException e) {
//                        e.printStackTrace();
                        promise.resolve( false );
                        return;
                    }
                    if (parsedGpx == null) {
                        promise.resolve(false);
                        return;
                    } else {
                        List points = parsedGpx.getTracks().get(0).getTrackSegments().get(0).getTrackPoints();
                        for (int index = 0; index < points.size(); index++) {
                            TrackPoint point = (TrackPoint) points.get( index );
                            feature.addPoint( new LatLong(
                                (Double) point.getLatitude(),
                                (Double) point.getLongitude()
                            ) );
                        }
                    }
                }
            }

            mapView.getLayerManager().getLayers().add(
                    Math.min( mapView.getLayerManager().getLayers().size(), (int) reactTreeIndex ),
                    feature
            );

            int hash = feature.hashCode();
            features.put( hash, feature );
            promise.resolve(hash);
        } catch(Exception e) {
            promise.reject("Create Event Error", e);
        }
    }

    @ReactMethod
    public void setPositions( int reactTag, int hash, ReadableArray positions, Promise promise ) {
        try {
            MapView mapView = (MapView) Utils.getMapView( this.getReactApplicationContext(), reactTag );
            if ( null == mapView ) {
                promise.resolve( false );
                return;
            }
            TouchPolyline feature = features.get( hash );
            feature.setPoints(positionsTolatLongsList(positions));
            feature.requestRedraw();
            promise.resolve(hash);
        } catch(Exception e) {
            promise.reject("Create Event Error", e);
        }
    }

    @ReactMethod
    public void remove(int reactTag, int hash, Promise promise) {
        try {
            MapView mapView = (MapView) Utils.getMapView( this.getReactApplicationContext(), reactTag );
            if ( null == mapView ) {
                promise.resolve( false );
                return;
            }
            mapView.getLayerManager().getLayers().remove( features.get( hash ) );
            promise.resolve(hash);
        } catch(Exception e) {
            promise.reject("Create Event Error", e);
        }
    }

}