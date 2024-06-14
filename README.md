# react-native-mapsforge

React Native components to build Mapsforge maps. Offline rendering of OpenStreetMap data. Android only

**Just some ideas in early development state. Do not use this for production!**

## Installation

```sh
# npm install react-native-mapsforge

# It's not hosted on npm yet, let's install it from github:
npm install git+https://github.com/jhotadhari/react-native-mapsforge.git
```

## Usage

```js
import {
	MapContainer,
	LayerMapsforge,
	Marker,
	Polyline,
} from 'react-native-mapsforge';

const App = () => <SafeAreaView>

    <MapContainer
        height={ 300 }
        center={ [-0.10, -78.48 ] } // lat long
        zoom={ 13 }
        minZoom={ 5 }
        maxZoom={ 20 }
    >

        <LayerMapsforge
            mapFile={ '/storage/emulated/0/.../Ecuador_oam.osm.map' }
            renderTheme={ '/storage/emulated/0/.../theme.xml' }
        />

        <Polyline
            positions={ [
                [-0.20, -78.2],
                [-0.22, -78.4],
                [-0.24, -78.6],
            ] }
            onTab={ result => console.log( 'Marker', result ) }
        />

        <Marker
            latLong={ [-0.23, -78.5] }
            onTab={ result => console.log( 'Marker', result ) }
        />

    </MapContainer>

</SafeAreaView>;

```

### Where to get maps?

Download maps in mapsforge V5 format here [https://www.openandromaps.org/en/downloads](https://www.openandromaps.org/en/downloads)

## Contribution

Contributions welcome. You can report [issues or suggest features](https://github.com/jhotadhari/react-native-mapsforge/issues). Help me coding, fork the repository and make pull requests. Or [get me a coffee](https://waterproof-webdesign.info/donate).

## License

Attribution-NonCommercial-ShareAlike 4.0 International (CC BY-NC-SA 4.0)

This license lets you remix, adapt, and build upon this project non-commercially, as long as you credit [me](https://waterproof-webdesign.info) and license your new creations under the identical terms.

[View License Deed](https://creativecommons.org/licenses/by-nc-sa/4.0) | [View Legal Code](https://creativecommons.org/licenses/by-nc-sa/4.0/legalcode)


## Credits

- Made with [create-react-native-library](https://github.com/callstack/react-native-builder-bob)
- Thanks to [mapsforge](https://github.com/mapsforge/mapsforge/)
