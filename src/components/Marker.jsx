/**
 * External dependencies
 */
import React, {
	useEffect,
	useState,
} from 'react';
import {
	PixelRatio,
	NativeEventEmitter,
} from 'react-native';
import PropTypes from 'prop-types';

/**
 * Internal dependencies
 */
import MapPropTypes from '../MapPropTypes';
import { MapMarkerModule } from '../nativeMapModules';

const defaultIconSize = PixelRatio.getPixelSizeForLayoutSize( 20 );

const Marker = ( {
	mapViewNativeTag,
	latLong,
	onTab,
	tabDistanceThreshold,
	icon,
	reactTreeIndex,
} ) => {

	tabDistanceThreshold = tabDistanceThreshold || 50;

	const iconWithDefaults = {
		width: defaultIconSize,		// number
		height: defaultIconSize,	// number
		path: '',					// absolute path or empty. if empty, java will fallback to a round icon.
		anchor: [
			0, 0,
		],				// array of two numbers. horizontal and vertical offset from center.
		...( icon || {} ),
	};

	const [
		hash, setHash,
	] = useState( null );


	useEffect( () => {
		if ( null === hash && mapViewNativeTag ) {
			setHash( false );
			MapMarkerModule.createMarker(
				mapViewNativeTag,
				( !! onTab && tabDistanceThreshold > 0 ? tabDistanceThreshold : 0 ),
				latLong,
				iconWithDefaults,
				reactTreeIndex
			).then( newHash => {
				if ( newHash ) {
					setHash( newHash );
				}
			} );
		}
		return () => {
			if ( hash && mapViewNativeTag ) {
				MapMarkerModule.removeMarker( mapViewNativeTag, hash );
			}
		};
	}, [
		mapViewNativeTag,
		!! hash,
	] );

	useEffect( () => {
		if ( hash && mapViewNativeTag ) {
			MapMarkerModule.setMarkerLocation( mapViewNativeTag, hash, latLong );
		}
	}, [latLong] );

	useEffect( () => {
		if ( hash && mapViewNativeTag ) {
			MapMarkerModule.setMarkerIcon( mapViewNativeTag, hash, iconWithDefaults );
		}
	}, [icon] );

	useEffect( () => {
		if ( onTab && hash && mapViewNativeTag ) {
			const eventEmitter = new NativeEventEmitter();
			let eventListener = eventEmitter.addListener( 'MarkerTouch', result => {
				if ( result.hash == hash ) {
					onTab( result );
				}
			} );
			return () => {
				eventListener.remove();
			};
		}
	}, [mapViewNativeTag,hash] );

	return null;
};

Marker.isMapLayer = true;

Marker.propTypes = {
	mapViewNativeTag: PropTypes.number,
	latLong: MapPropTypes.latLong,
	onTab: PropTypes.func,
	tabDistanceThreshold: PropTypes.number,
	icon: function( props, propName, componentName ) {
		if ( undefined !== props[propName] ) {

			let isError = typeof props[propName] !== 'object';

			const {
				path,
				width,
				height,
				anchor,
			} = props[propName];

			if ( ! isError && undefined !== path
				&& typeof path !== 'string'
			) {
				isError = true;
			}

			if ( ! isError && undefined !== width
				&& ( typeof width !== 'number' || width < 0 )
			) {
				isError = true;
			}

			if ( ! isError && undefined !== height
				&& ( typeof height !== 'number' || height < 0 )
			) {
				isError = true;
			}

			if ( ! isError && undefined !== anchor
				&& ( ! Array.isArray( anchor ) || anchor.length !== 2 || ! [...anchor].reduce( ( acc, val ) => acc ? typeof val === 'number' : acc, true ) )
			) {
				isError = true;
			}

			if ( isError ) {
				return new Error( 'Invalid prop `' + propName + '` supplied to' + ' `' + componentName + '`. Validation failed.' );
			}
		}
	},
};

export default Marker;