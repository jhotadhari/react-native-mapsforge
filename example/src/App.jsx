
/**
 * External dependencies
 */
import React, {
	useEffect,
	useState,
} from 'react';
import {
	Button,
	SafeAreaView,
	StatusBar,
	Text,
	useColorScheme,
	useWindowDimensions,
	PixelRatio,
	View,
} from 'react-native';

/**
 * react-native-mapsforge dependencies
 */
import {
	MapContainer,
	LayerMapsforge,
	Marker,
	Polyline,
	usePromiseQueueState,
	promiseQueue,
	useRenderStyleOptions,
	nativeMapModules,
} from 'react-native-mapsforge';
const { MapContainerModule } = nativeMapModules;

/**
 * Internal dependencies
 */
import PickerModalControl from './components/PickerModalControl.jsx';
import usePermissionsOk from './compose/usePermissionsOk.jsx';
import { randomNumber } from './utils';

const iconMarkerBase = {
	width: PixelRatio.getPixelSizeForLayoutSize( 40 ),
	height: PixelRatio.getPixelSizeForLayoutSize( 60 ),
	anchor: [
		0,
		- PixelRatio.getPixelSizeForLayoutSize( 60 ) / 2,
	],
};

const icons = [
	{},		// fallback to default icon.
	{
		...iconMarkerBase,
		path: '/storage/emulated/0/Android/media/com.jhotadhari.reactnative.mapsforgeExample/dummy/marker_green.png',
	},
	{
		...iconMarkerBase,
		path: '/storage/emulated/0/Android/media/com.jhotadhari.reactnative.mapsforgeExample/dummy/marker_red.png',
	},
];

const LiftViewIdStateUp = ( { mapViewNativeTag, setMainMapViewId } ) => {
	useEffect( () => {
		setMainMapViewId( mapViewNativeTag );
	}, [mapViewNativeTag] );
	return null;
};

const renderThemeOptions = [
	{ label: 'Elements', value: '/storage/emulated/0/Documents/orux/mapstyles/Elements.xml' },
	{ label: 'Alti', value: '/storage/emulated/0/Documents/orux/mapstyles/Alti.xml' },
	{ label: 'DEFAULT', value: 'DEFAULT' },
	{ label: 'OSMARENDER', value: 'OSMARENDER' },
];

const mapFileOptions = [
	{ label: 'Ecuador', value: '/storage/emulated/0/Documents/orux/mapfiles/Ecuador_oam.osm.map' },
	{ label: 'Colombia', value: '/storage/emulated/0/Documents/orux/mapfiles/Colombia_oam.osm.map' },
];

const App = () => {

	const isDarkMode = useColorScheme() === 'dark';

	const style = {
		backgroundColor: isDarkMode ? 'black' : '#eee',
		color: isDarkMode ? '#eee' : 'black',
	};

	const { permissionsOk, requestPermission } = usePermissionsOk();

	const promiseQueueState = usePromiseQueueState();

	const [
		mapFile, setMapFile,
	] = useState( mapFileOptions[0].value );
	const [
		showLayerMapsforge, setShowLayerMapsforge,
	] = useState( true );
	const [
		showMarkers, setShowMarkers,
	] = useState( true );

	const [
		mainMapViewId, setMainMapViewId,
	] = useState( null );

	const [
		iconIndex, setIconIndex,
	] = useState( 0 );

    const [renderOverlayOptions, setRenderOverlayOptions] = useState( [] );

    const [renderOverlays, setRenderOverlays] = useState( [] );
	const [
		renderTheme, setRenderTheme,
	] = useState( renderThemeOptions[0].value );

	const {
		renderStyleDefaultId,
		renderStyleOptions,
	} = useRenderStyleOptions( ( {
		renderTheme,
		nativeTag: mainMapViewId,
	} ) );

	const [
		renderStyle, setRenderStyle,
	] = useState( renderStyleDefaultId );

	useEffect( () => {
		if ( ! renderStyle && renderStyleDefaultId ) {
			setRenderStyle( renderStyleDefaultId );
		}

		if ( ! renderOverlayOptions.length ) {
			const renderStyleOptions_ = renderStyleOptions.find( opt => opt.value === renderStyle  );
			if ( undefined !== renderStyleOptions_ ) {
				const newItems = Object.keys( renderStyleOptions_.options ).map( value => {
					return {
						value,
						label: renderStyleOptions_.options[value],
					}
				} );
				setRenderOverlayOptions( newItems );
			}
		}
	}, [renderStyle,renderStyleDefaultId] );

	const [
		locations, setLocations,
	] = useState( Array.apply( null, Array( 1000 ) ).map( () => [
		randomNumber( -0.25, 0 ),		// lat
		randomNumber( -78.6, -78.37 )	// long
	] ) );


	const { width, height } = useWindowDimensions();

	return (
		<SafeAreaView style={ style }>
			<StatusBar
				barStyle={ isDarkMode ? 'light-content' : 'dark-content' }
				backgroundColor={ style.backgroundColor }
			/>

			{ ! permissionsOk && <View style={ {
				width,
				height,
				justifyContent: 'space-around',
				alignItems: 'center',
			} } >
				<View>

					<Text
						style={ { marginBottom: 10 } }
					>Need permission to access and manage all files</Text>

					<Button
						onPress={ () => {
							requestPermission().then( result => {
								console.log( 'debug result', result ); // debug
							} );
						} }
						title="open settings"
					/>
				</View>
			</View> }

			{ permissionsOk && <View >

				<MapContainer
					height={ height }
					center={ [
						-0.10, -78.48
					] }
					zoom={ 13 }
					// minZoom={ 12 }
					// maxZoom={ 18 }
				>

					<LiftViewIdStateUp setMainMapViewId={ setMainMapViewId } />

					{ showLayerMapsforge && <LayerMapsforge
						mapFile={ mapFile }
						renderTheme={ renderTheme }
						renderStyle={ renderStyle }
						renderOverlays={ renderOverlays }
					/> }

					<Polyline
						// positions={ locations }
						file={ '/storage/emulated/0/Documents/orux/tracklogs/2024-06-10 1713__20240610_1713.gpx' }
						onTab={ res => {
							console.log( 'debug Polyline res', res ); // debug
						} }
					/>

					{ showMarkers && [...locations].map( ( latLong, index ) => <Marker
						latLong={ latLong }
						key={ index }
						tabDistanceThreshold={ 80 }
						icon={ icons[iconIndex] }
						onTab={ res => {
							console.log( 'debug Marker res', res ); // debug
						} }
					/> ) }

				</MapContainer>

			</View> }



			<View style={ {
				...style,
				position: 'absolute',
				top: 0,
				left: 0,
				width,
			} }>

				<View style={ {
					flexDirection: 'row',
					width,
					justifyContent: 'space-evenly',
					alignItems: 'center',
					marginBottom: 10,
				} }>
					<Button
						onPress={ () => {
							setShowMarkers( ! showMarkers );
						} }
						title="Toggle Markers"
						disabled={ promiseQueueState > 0 }
					/>
					<Button
						onPress={ () => {
							const newLocations = [...locations].map( coords => [...coords].map( coord => Math.random() > 0.5 ? coord + 0.01 : coord - 0.01 ) );
							setLocations( newLocations );
						} }
						title="random locations"
						disabled={ promiseQueueState > 0 }
					/>
					<Button
						onPress={ () => setIconIndex( iconIndex + 1 === icons.length ? 0 : iconIndex + 1 ) }
						title="Change icons"
						disabled={ promiseQueueState > 0 }
					/>
				</View>

				<View style={ {
					...style,
					flexDirection: 'row',
					justifyContent: 'space-evenly',
					alignItems: 'center',
					width,
					marginBottom: 10,
				} }>
					<Button
						onPress={ () => {
							setShowLayerMapsforge( ! showLayerMapsforge );
						} }
						title="Toggle Mapsforge"
						disabled={ promiseQueueState > 0 }
					/>

					<Text>{ promiseQueueState > 0 ? 'busy' : 'idle'  }</Text>

					<Button
						onPress={ () => {
							promiseQueue.enqueue( () => {
								MapContainerModule.zoomIn( mainMapViewId );
							} );
						} }
						title="+"
						disabled={ promiseQueueState > 0 }
					/>
					<Button
						onPress={ () => {
							promiseQueue.enqueue( () => {
								MapContainerModule.zoomOut( mainMapViewId );
							} );
						} }
						title="-"
						disabled={ promiseQueueState > 0 }
					/>
				</View>

				<View style={ {
					...style,
					flexDirection: 'row',
					justifyContent: 'space-evenly',
					alignItems: 'center',
					width,
					marginBottom: 10,
				} }>

					<PickerModalControl
						headerLabel={ 'Map file' }
						options={ mapFileOptions }
						values={ [mapFile] }
						onChange={ clickedVal => setMapFile( clickedVal ) }
						closeOnChange={ false }
						disabled={ promiseQueueState > 0 }
					/>

					<PickerModalControl
						headerLabel={ 'Render theme' }
						options={ renderThemeOptions }
						values={ [renderTheme] }
						onChange={ clickedVal => setRenderTheme( clickedVal ) }
						closeOnChange={ false }
						disabled={ promiseQueueState > 0 }
					/>

					<PickerModalControl
						headerLabel={ 'Render style' }
						disabled={ promiseQueueState > 0 || ! renderStyleOptions.length }
						buttonLabelFallback={ 'Flavour' }
						options={ renderStyleOptions }
						values={ [renderStyle] }
						onChange={ clickedVal => setRenderStyle( clickedVal ) }
						closeOnChange={ false }
					/>

					<PickerModalControl
						multiple={ true }
						buttonLabel={ 'options' }
						headerLabel={ 'Render style options' }
						disabled={ promiseQueueState > 0 || ! renderStyleOptions.length }
						buttonLabelFallback={ 'test' }
						options={ renderOverlayOptions }
						values={ renderOverlays }
						onChange={ clickedVal => {
							const existingIndex = renderOverlays.findIndex( val => val === clickedVal );
							if ( -1 === existingIndex ) {
								setRenderOverlays( [
									...renderOverlays,
									clickedVal,
								] );
							} else {
								const newSelectedItems = [...renderOverlays];
								newSelectedItems.splice( existingIndex, 1 );
								setRenderOverlays( newSelectedItems );
							}
						} }
						closeOnChange={ false }
					/>

				</View>

			</View>
		</SafeAreaView>
	);
};

export default App;
