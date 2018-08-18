import { NativeModules } from 'react-native';
const { FloaterIcon } = NativeModules;

export const onReady = () => {
	return FloaterIcon.onReady();
}

export const onShowHide = () => {
	return FloaterIcon.onShowHide();
}

export const show = () => {
	return FloaterIcon.show();
}

export const hide = () => {
	return FloaterIcon.hide();
}

