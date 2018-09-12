import { NativeModules } from 'react-native';
const { FloaterIcon } = NativeModules;

let _isReady = false;

const onReady = () => {
	_isReady = true;
	return FloaterIcon.onReady();
}

const isReady = () => {
	return _isReady;
}

const show = () => {
	return FloaterIcon.show();
}

const hide = () => {
	return FloaterIcon.hide();
}

const isFloating = () => {
	return FloaterIcon.isFloating();
}

export default {
	onReady,
	show,
	hide,
	isFloating,
	isReady
}
