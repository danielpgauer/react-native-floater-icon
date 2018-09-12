declare module "react-native-floater-icon" {
  class FloaterIcon {
    static onReady(): void;
    static isReady(): boolean;
    static isFloating(): boolean;
    static show(): void;
    static hide(): void;
  }
  export = FloaterIcon;
}
