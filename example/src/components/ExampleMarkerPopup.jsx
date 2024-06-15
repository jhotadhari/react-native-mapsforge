
/**
 * External dependencies
 */
import React, { useState } from 'react';
import {
	Button,
	Text,
	View,
} from 'react-native';

/**
 * Internal dependencies
 */
import { FeatureReact } from '../../map';

const ExampleMarkerPopup = ( {
	mapViewNativeTag,
	latLong,
} ) => {

	const [open, setOpen] = useState( false );

	const [dimBtn, setDimBtn] = useState( {
		width: 0,
		height: 0,
	} );
	const [dimPop, setDimPop] = useState( {
		width: 0,
		height: 0,
	} );

	const marginBottom = 10;

	return <FeatureReact mapViewNativeTag={ mapViewNativeTag } latLong={ latLong }>
		<View
			style={ {
				minWidth: 50,
				minHeight: 50,
				transform: [
					{ translateX: open ? -dimPop.width / 2 : -dimBtn.width / 2 },
					{ translateY: open ? ( -dimBtn.height / 2 - dimPop.height - marginBottom ) : -dimBtn.height / 2 },
				],
			} }
		>
			{ open && <View
				onLayout={ event => setDimPop( event.nativeEvent.layout ) }
				style={ {
					minWidth: 200,
					minHeight: 200,
					marginBottom,
					backgroundColor: '#000',
					padding: 10,
				} }
			>
				<Text>Bla bla bla bla bla</Text>
				<Text>Bla bla bla bla bla</Text>
				<Text>Bla bla bla bla bla</Text>
				<Text>Bla bla bla bla bla</Text>
				<Text>Bla bla bla bla bla</Text>
				<Text>Bla bla bla bla bla</Text>
			</View> }

			<View
				style={ {
					justifyContent: 'space-around',
					alignItems: 'center',
					flexDirection: 'row',
				} }
			>
				<View
					onLayout={ event => setDimBtn( event.nativeEvent.layout ) }
				>
					<Button
						title={ open ? 'close' : 'open' }
						onPress={ () => setOpen( ! open ) }
					/>
				</View>
			</View>
		</View>
	</FeatureReact>;
};

export default ExampleMarkerPopup;
