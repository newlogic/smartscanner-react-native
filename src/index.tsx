import { NativeModules, Platform } from 'react-native';

const LINKING_ERROR =
  `The package 'smartscanner-react-native' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo managed workflow\n';

const SmartScanner = NativeModules.SmartscannerReactNativeModule
  ? NativeModules.SmartscannerReactNativeModule
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );
export type SmartScannerOptions = {
  action: string;
  options: {
    mode: string;
    mrzFormat?: string;
    scannerSize?: string;
    barcodeOptions?: {
      barcodeFormats: Array<String>;
    };
    config: {
      background?: string;
      branding?: boolean;
      isManualCapture?: boolean;
      label?: string;
      header?: string;
      imageResultType?: string | null;
      subHeader?: string;
      font?: string | null;
      orientation?: string | null;
    };
  };
};

export function executeScanner(options: SmartScannerOptions): Promise<number> {
  return SmartScanner.executeScanner(options);
}
export type SmartScannerErrorCodes =
  | 'SCANNER_CANCELLED'
  | 'SCANNER_UNKNOWN_CODE'
  | 'SCANNER_FAILED'
  | 'SCANNER_RESULTS_NOT_FOUND'
  | 'SCANNER_ACTIVITY_DOES_NOT_EXIST';
