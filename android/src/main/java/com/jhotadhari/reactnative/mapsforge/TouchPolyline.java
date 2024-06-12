package com.jhotadhari.reactnative.mapsforge;

import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;

import org.mapsforge.core.graphics.Bitmap;
import org.mapsforge.core.graphics.GraphicFactory;
import org.mapsforge.core.graphics.Paint;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.Point;
import org.mapsforge.core.util.LatLongUtils;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.overlay.Marker;
import org.mapsforge.map.layer.overlay.Polyline;
import org.mapsforge.map.util.MapViewProjection;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class TouchPolyline extends Polyline {

    protected MapView mapView;

    protected ReactContext reactContext;
    protected int tabDistanceThreshold;

    protected List<LatLong> latLongs = new CopyOnWriteArrayList<>();
    protected Paint paintStroke;

    public TouchPolyline(ReactContext reactContext_, MapView mapView_, int tabDistanceThreshold_, Paint paintStroke, GraphicFactory graphicFactory) {
        this( reactContext_, mapView_, tabDistanceThreshold_, paintStroke, graphicFactory, false );
    }
    public TouchPolyline(ReactContext reactContext_, MapView mapView_, int tabDistanceThreshold_, Paint paintStroke, GraphicFactory graphicFactory, boolean keepAligned) {
        super( paintStroke, graphicFactory, keepAligned);
        this.paintStroke = super.getPaintStroke();
        this.latLongs = super.getLatLongs();
        mapView = mapView_;
        reactContext = reactContext_;
        tabDistanceThreshold = tabDistanceThreshold_;
    }

    public synchronized boolean contains(Point tapXY, MapViewProjection mapViewProjection) {
        // Touch min 20 px at baseline mdpi (160dpi)
        double distance = Math.max(
            tabDistanceThreshold / 2 * this.displayModel.getScaleFactor(),
            this.paintStroke.getStrokeWidth() / 2
        );
        Point point2 = null;
        for (int i = 0; i < this.latLongs.size() - 1; i++) {
            Point point1 = i == 0 ? mapViewProjection.toPixels(this.latLongs.get(i)) : point2;
            point2 = mapViewProjection.toPixels(this.latLongs.get(i + 1));
            if (LatLongUtils.distanceSegmentPoint(point1.x, point1.y, point2.x, point2.y, tapXY.x, tapXY.y) <= distance) {
                return true;
            }
        }
        return false;
    }

    /**
     * Handles a tap event. A return value of true indicates that the tap event has been handled by this overlay and
     * stops its propagation to other overlays.
     * <p/>
     * The default implementation of this method does nothing and returns false.
     *
     * @param tapLatLong the the geographic position of the tap.
     * @param layerXY    the xy position of the layer element (if available)
     * @param tapXY      the xy position of the tap
     * @return true if the tap event was handled, false otherwise.
     */
    public boolean onTap( LatLong tapLatLong, Point layerXY, Point tapXY) {
        if ( tabDistanceThreshold > 0 ) {
            if ( contains( tapXY, mapView.getMapViewProjection() ) ) {
                WritableMap params = new WritableNativeMap();
                params.putArray( "tapLatLong", Utils.latLongToArray( tapLatLong ) );
                params.putMap( "tapXY",   Utils.pointToObj( tapXY ) );
                params.putInt( "hash", this.hashCode() );
                Utils.sendEvent( reactContext, "PolylineTouch", params );
                return true;
            }
        }
        return false;
    }

}
