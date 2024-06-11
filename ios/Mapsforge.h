
#ifdef RCT_NEW_ARCH_ENABLED
#import "RNMapsforgeSpec.h"

@interface Mapsforge : NSObject <NativeMapsforgeSpec>
#else
#import <React/RCTBridgeModule.h>

@interface Mapsforge : NSObject <RCTBridgeModule>
#endif

@end
