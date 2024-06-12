package com.jhotadhari.reactnative.mapsforge;

import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;

import org.mapsforge.core.graphics.Bitmap;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.Point;
import org.mapsforge.map.layer.overlay.Marker;

public class TouchMarker extends Marker {

    protected ReactContext reactContext;
    protected int tabDistanceThreshold;

    public TouchMarker(ReactContext reactContext_, int tabDistanceThreshold_, LatLong latLong, Bitmap bitmap, int horizontalOffset, int verticalOffset) {
        super(latLong, bitmap, horizontalOffset, verticalOffset);
        reactContext = reactContext_;
        tabDistanceThreshold = tabDistanceThreshold_;
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
            double distance = layerXY.distance( tapXY );
            if (  distance <= tabDistanceThreshold ) {
                WritableMap params = new WritableNativeMap();
                params.putArray( "tapLatLong", Utils.latLongToArray( tapLatLong ) );
                params.putDouble( "distance",  distance );
                params.putMap( "layerXY",   Utils.pointToObj( layerXY ) );
                params.putMap( "tapXY",   Utils.pointToObj( tapXY ) );
                params.putInt( "hash", this.hashCode() );
                Utils.sendEvent( reactContext, "MarkerTouch", params );
                return true;
            }
        }
        return false;
    }

}
