declare module "react-native-floater-icon" {

    class FloaterIcon {
        static onReady(): void;
        static onShowHide(show :boolean): void;
        static show(): void;
        static hide(): void;
    }

    export = FloaterIcon;

}
