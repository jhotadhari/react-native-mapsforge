/**
 * External dependencies
 */
import React, { useEffect } from 'react';
import { NativeEventEmitter } from 'react-native';
// import PropTypes from 'prop-types';

/**
 * Internal dependencies
 */
import useRefState from '../compose/useRefState';
// import MapPropTypes from '../MapPropTypes';
import promiseQueue from '../promiseQueue';
import { MapPolylineModule } from '../nativeMapModules';

const Polyline = ( {
	mapViewNativeTag,
	positions,
	file,
	onTab,
	tabDistanceThreshold,
	reactTreeIndex,
} ) => {

	tabDistanceThreshold = tabDistanceThreshold || 50;

	const [hash, setHash] = useRefState( null );

	const create = () => {
		promiseQueue.enqueue( () => {
			MapPolylineModule.create(
				mapViewNativeTag,
				( !! onTab && tabDistanceThreshold > 0 ? tabDistanceThreshold : 0 ),
				positions,
				file,
				reactTreeIndex,
			).then( newHash => newHash ? setHash( parseInt( newHash, 10 ) ) : null );
		} );
	};
	useEffect( () => {
		if ( hash === null && mapViewNativeTag ) {
			setHash( false );
			create();
		}
		return () => {
			if ( hash && mapViewNativeTag ) {
				promiseQueue.enqueue( () => {
					MapPolylineModule.remove( mapViewNativeTag, hash );
				} );
			}
		};
	}, [
		mapViewNativeTag,
		!! hash,
	] );

	useEffect( () => {
		if ( hash && mapViewNativeTag ) {
			promiseQueue.enqueue( () => {
				MapPolylineModule.setPositions( mapViewNativeTag, hash, positions );
			} );
		}
	}, [positions] );

	useEffect( () => {
		if ( onTab && hash && mapViewNativeTag ) {
			const eventEmitter = new NativeEventEmitter();
			let eventListener = eventEmitter.addListener( 'PolylineTouch', result => {
				if ( parseInt( result.hash, 10 ) === hash ) {
					onTab( result );
				}
			} );
			return () => {
				eventListener.remove();
			};
		}
	}, [mapViewNativeTag, hash] );

	return null;
};

Polyline.isMapLayer = true;

// Polyline.propTypes = {
// 	mapViewNativeTag: PropTypes.number,
// 	latLong: MapPropTypes.latLong,
// 	onTab: PropTypes.func,
// 	tabDistanceThreshold: PropTypes.number,
// 	icon: function( props, propName, componentName ) {
// 		if ( undefined !== props[propName] ) {

// 			let isError = typeof props[propName] !== 'object';

// 			const {
// 				path,
// 				width,
// 				height,
// 				anchor,
// 			} = props[propName];

// 			if ( ! isError && undefined !== path
// 				&& typeof path !== 'string'
// 			) {
// 				isError = true;
// 			}

// 			if ( ! isError && undefined !== width
// 				&& ( typeof width !== 'number' || width < 0 )
// 			) {
// 				isError = true;
// 			}

// 			if ( ! isError && undefined !== height
// 				&& ( typeof height !== 'number' || height < 0 )
// 			) {
// 				isError = true;
// 			}

// 			if ( ! isError && undefined !== anchor
// 				&& ( ! Array.isArray( anchor ) || anchor.length !== 2 || ! [...anchor].reduce( ( acc, val ) => acc ? typeof val === 'number' : acc, true ) )
// 			) {
// 				isError = true;
// 			}

// 			if ( isError ) {
// 				return new Error( 'Invalid prop `' + propName + '` supplied to' + ' `' + componentName + '`. Validation failed.' );
// 			}
// 		}
// 	},
// };

export default Polyline;
