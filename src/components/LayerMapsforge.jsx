/**
 * External dependencies
 */
import React, { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import {isNumber} from 'lodash-es';

/**
 * Internal dependencies
 */
import useRefState from '../compose/useRefState';
import promiseQueue from '../promiseQueue';
import usePrevious from '../compose/usePrevious';
import useRenderStyleOptions from '../compose/useRenderStyleOptions';
import { MapLayerMapsforgeModule } from '../nativeMapModules';

const LayerMapsforge = ( {
	mapViewNativeTag,
	mapFile,
	renderTheme,
	renderStyle,
	renderOverlays,
	reactTreeIndex,
	cachePersistence,	// 0, 1, 2	// `0` is not persistent. `1` gets purged on certain layer prop changes, but persistent on app restarts. `2` never gets purged.
} ) => {

	renderTheme = renderTheme || 'DEFAULT';
	renderStyle = renderStyle || '';
	renderOverlays = renderOverlays || [];
	cachePersistence = isNumber( cachePersistence ) ? cachePersistence : 1;

	const renderStylePrev = usePrevious( renderStyle );

	const [hash, setHash] = useRefState( null );
	const [triggerCreateNew, setTriggerCreateNew] = useState( null );

	const { renderStyleDefaultId } = useRenderStyleOptions( ( {
		renderTheme,
		nativeTag: mapViewNativeTag,
	} ) );

	const createLayer = () => {
		setHash( false );
		promiseQueue.enqueue( () => {
			MapLayerMapsforgeModule.createLayer(
				mapViewNativeTag,
				mapFile,
				renderTheme,
				renderStyle,
				cachePersistence,
				renderOverlays,
				reactTreeIndex,
			).then( newHash => newHash ? setHash( parseInt( newHash, 10 ) ) : null );
		} );
	};

	useEffect( () => {
		if ( hash === null && mapViewNativeTag && mapFile ) {
			createLayer();
		}
		return () => {
			if ( hash && mapViewNativeTag ) {
				promiseQueue.enqueue( () => {
					MapLayerMapsforgeModule.removeLayer(
						mapViewNativeTag,
						hash,
						cachePersistence < 2		// forcePurge
					);
				} );
			}
		};
	}, [
		mapViewNativeTag,
		!! hash,
		triggerCreateNew,
	] );

	useEffect( () => {
		if ( mapViewNativeTag ) {
			if ( hash ) {
				let shouldRecreate = true;
				if (
					renderStyle !== renderStylePrev
					&& ( ! renderStylePrev || ! renderStylePrev?.length )
					&& ( renderStyle && renderStyleDefaultId && renderStyle === renderStyleDefaultId )
				) {
					shouldRecreate = false;
				}
				if ( shouldRecreate ) {
					promiseQueue.enqueue( () => {
						MapLayerMapsforgeModule.removeLayer(
							mapViewNativeTag,
							hash,
							cachePersistence < 2		// forcePurge
						).then( removedHash => {
							if ( removedHash ) {
								setHash( null )
								setTriggerCreateNew( Math.random() );
							}
						} );
					} );
				}
			} else if ( hash === null && mapFile ) {
				setTriggerCreateNew( Math.random() );
			}
		}
	}, [
		mapFile,
		renderTheme,
		renderStyle,
		cachePersistence,
		( renderOverlays && Array.isArray( renderOverlays ) && renderOverlays.length
			? renderOverlays.join( '' )
			: null
		),
	] );

	return null;
};
LayerMapsforge.isMapLayer = true;

LayerMapsforge.propTypes = {
	mapViewNativeTag: PropTypes.number,
	mapFile: PropTypes.string,
	renderTheme: PropTypes.string,
	reactTreeIndex: PropTypes.number,
	renderStyle: PropTypes.string,
	renderOverlays: PropTypes.array,
	cachePersistence: function( props, propName, componentName ) {
		if ( props[propName] && (
			! isNumber( props[propName] )
			|| props[propName] > 2
			|| props[propName] < 0
		) ) {
			return new Error( 'Invalid prop `' + propName + '` supplied to' + ' `' + componentName + '`. Validation failed.' );
		}
	},
};

export default LayerMapsforge;
