declare module "react-native-floater-icon" {
  class FloaterIcon {
    static onReady(): Promise<boolean>;
    static isReady(): Promise<boolean>;
    static isFloating(): Promise<boolean>;
    static show(): Promise<boolean>;
    static hide(): Promise<boolean>;
    static stop(): Promise<boolean>;
  }
  export = FloaterIcon;
}
