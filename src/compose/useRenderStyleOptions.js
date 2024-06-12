/**
 * External dependencies
 */
import {
	useEffect,
	useState,
} from 'react';
import {
	NativeModules,
	NativeEventEmitter,
} from 'react-native';

/**
 * Internal dependencies
 */
const { MapLayerMapsforgeModule } = NativeModules;

useRenderStyleOptions = ( {
	renderTheme,
	nativeTag,
} ) => {

	const [renderStyleOptions,setRenderStyleOptions] = useState( [] );

	const [
		renderStyleDefaultId, setRenderStyleDefault,
	] = useState( null );

	useEffect( () => {
		const eventEmitter = new NativeEventEmitter();
		let eventListener = eventEmitter.addListener( 'RenderThemeParsed', result => {
			if ( ! renderStyleOptions.length && result && renderTheme == result.filePath ) {
				setRenderStyleOptions( Object.values( result.collection ) );
				if ( ! renderStyleDefaultId ) {
					const defaultStyle = Object.values( result.collection ).find( obj => obj.default );
					if ( undefined !== defaultStyle && !! defaultStyle ) {
						setRenderStyleDefault( defaultStyle.value );
					}
				}

			}
		} );
		return () => {
			eventListener.remove();
		};
	}, [nativeTag] );

	useEffect( () => {
		if ( ! renderStyleOptions.length ) {
			MapLayerMapsforgeModule.getRenderThemeOptions( renderTheme ).then( res => {
				if ( res ) {
					setRenderStyleOptions( Object.values( res ) );

					if ( ! renderStyleDefaultId ) {
						const defaultStyle = Object.values( res ).find( obj => obj.default );
						if ( undefined !== defaultStyle && !! defaultStyle ) {
							setRenderStyleDefault( defaultStyle.value );
						}

					}
				}
			} );
		}
	}, [nativeTag,renderTheme] );

	return {
		renderStyleDefaultId,
		renderStyleOptions,
	};
};

export default useRenderStyleOptions;
