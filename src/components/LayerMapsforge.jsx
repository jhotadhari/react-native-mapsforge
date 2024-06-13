/**
 * External dependencies
 */
import React, {
	useEffect,
	useState,
} from 'react';
import PropTypes from 'prop-types';

/**
 * Internal dependencies
 */
import usePrevious from '../compose/usePrevious';
import { MapLayerMapsforgeModule } from '../nativeMapModules';

const LayerMapsforge = ( {
	mapViewNativeTag,
	mapFile,
	renderTheme,
	renderStyle,
	renderOverlays,
	reactTreeIndex,
	// persistentCache,	// ??? TODO
} ) => {

	renderTheme = renderTheme || 'DEFAULT';
	renderStyle = renderStyle || '';
	renderOverlays = renderOverlays || [];

	const renderStylePrev = usePrevious( renderStyle );

	const [
		hash, setHash,
	] = useState( null );

	const { renderStyleDefaultId } = useRenderStyleOptions( ( {
		renderTheme,
		nativeTag: mapViewNativeTag,
	} ) );

	const createLayer = () => {
		MapLayerMapsforgeModule.createLayer(
			mapViewNativeTag,
			mapFile,
			renderTheme,
			renderStyle,
			renderOverlays,
			reactTreeIndex
		).then( newHash => {
			if ( newHash ) {
				setHash( newHash );
			}
		} );
	};

	useEffect( () => {
		if ( null === hash && mapViewNativeTag && mapFile ) {
			setHash( false );
			createLayer();
		}
		return () => {
			if ( hash && mapViewNativeTag ) {
				MapLayerMapsforgeModule.removeLayer( mapViewNativeTag, hash );
			}
		};
	}, [
		mapViewNativeTag,
		!! hash,
	] );

	useEffect( () => {
		if ( hash && mapViewNativeTag ) {
			let shouldRecreate = true;
			if (
				renderStyle !== renderStylePrev
				&& ( ! renderStylePrev || ! renderStylePrev?.length )
				&& ( renderStyle && renderStyleDefaultId && renderStyle === renderStyleDefaultId )
			) {
				shouldRecreate = false;
			}

			if ( shouldRecreate ) {
				MapLayerMapsforgeModule.removeLayer( mapViewNativeTag, hash ).then( removedHash => {
					if ( removedHash ) {
						createLayer()
					}
				} );
			}
		}
	}, [
		mapFile,
		renderTheme,
		renderStyle,
		( renderOverlays && Array.isArray( renderOverlays ) && renderOverlays.length ? renderOverlays : null ),
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
};

export default LayerMapsforge;