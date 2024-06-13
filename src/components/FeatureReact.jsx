/**
 * External dependencies
 */
import React, {
	useEffect,
	useRef,
	useState,
} from 'react';
import {
	Animated,
	NativeEventEmitter,
} from 'react-native';
import PropTypes from 'prop-types';

/**
 * Internal dependencies
 */
import MapPropTypes from '../MapPropTypes';
import promiseQueue from '../promiseQueue';
import { MapFeatureReactModule } from '../nativeMapModules';

const FeatureReact = ( {
	mapViewNativeTag,
	latLong,
	children,
} ) => {

	const [
		uid, setUid,
	] = useState( null );

	const fadeAnim = {
		x: useRef( new Animated.Value(0) ).current,
		y: useRef( new Animated.Value(0) ).current,
	};

	useEffect( () => {
		if ( null === uid && mapViewNativeTag ) {
			setUid( false );
			promiseQueue.enqueue( () => {
				MapFeatureReactModule.createFeature(
					mapViewNativeTag,
					latLong,
				).then( newUid => {
					if ( newUid ) {
						promiseQueue.enqueue( () => setHash( newUid ) );
					}
				} );
			} );
		}
		return () => {
			if ( uid && mapViewNativeTag ) {
				promiseQueue.enqueue( () => {
					MapFeatureReactModule.removeFeature( mapViewNativeTag, uid );
				} );
			}
		};
	}, [
		mapViewNativeTag,
		!! uid,
	] );

	useEffect( () => {
		if ( uid && mapViewNativeTag ) {
			const eventEmitter = new NativeEventEmitter();
			let eventListener = eventEmitter.addListener( 'MapMoveFeature', result => {
				if ( result.nativeTag === mapViewNativeTag && result.uid === uid && result.xy ) {
					Animated.timing( fadeAnim.x, {
						toValue: result.xy.x,
						duration: 10,
						useNativeDriver: true,
					} ).start();
					Animated.timing( fadeAnim.y, {
						toValue: result.xy.y,
						duration: 10,
						useNativeDriver: true,
					} ).start();
				}
			} );
			return () => {
				eventListener.remove();
			};
		}
	}, [mapViewNativeTag,uid] );

	useEffect( () => {
		if ( uid && mapViewNativeTag ) {
			promiseQueue.enqueue( () => {
				MapFeatureReactModule.setLocation( mapViewNativeTag, uid, latLong );
			} );
		}
	}, [latLong.join( '' )] );

	return <Animated.View style={ {
		zIndex: 99999,
		top: 0,
		left: 0,
		position: 'absolute',
		transform: [
			{ translateX: fadeAnim.x },
			{ translateY: fadeAnim.y },
		],
	} }>
		{ children }
	</Animated.View>;
};

FeatureReact.propTypes = {
	mapViewNativeTag: PropTypes.number,
	latLong: MapPropTypes.latLong,
};

export default FeatureReact;