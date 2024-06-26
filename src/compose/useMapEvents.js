/**
 * External dependencies
 */
import { isFunction } from 'lodash-es';
import { useEffect } from 'react';
import { NativeEventEmitter } from 'react-native';

const useMapEvents = ( {
	nativeTag,
    onMoveStart,
    onZoom,
    onFrameBuffer,
} ) => {

    useEffect( () => {
		const eventEmitter = new NativeEventEmitter();
		let eventListener = eventEmitter.addListener( 'onMoveStart', result => {
			if ( result.nativeTag === nativeTag && isFunction( onMoveStart ) ) {
                onMoveStart( result );
			}
		} );
		return () => {
			eventListener.remove();
		};
	}, [
		nativeTag,
		onMoveStart,
	] );

	useEffect( () => {
		const eventEmitter = new NativeEventEmitter();
		let eventListener = eventEmitter.addListener( 'MapZoom', result => {
			if ( result.nativeTag === nativeTag && isFunction( onZoom ) ) {
                onZoom( result );
			}
		} );
		return () => {
			eventListener.remove();
		};
	}, [
		nativeTag,
		onZoom,
	] );

	useEffect( () => {
		const eventEmitter = new NativeEventEmitter();
		let eventListener = eventEmitter.addListener( 'onFrameBuffer', result => {
			if ( result.nativeTag === nativeTag && isFunction( onFrameBuffer ) ) {
                onFrameBuffer( result );
			}
		} );
		return () => {
			eventListener.remove();
		};
	}, [
		nativeTag,
		onFrameBuffer,
	] );

};

export default useMapEvents;
