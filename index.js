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

const isStarted = () => {
	return FloaterIcon.isStarted();
}

const start = () => {
	return FloaterIcon.start();
}

const stop = () => {
	return FloaterIcon.stop();
}

const isFloating = () => {
	return FloaterIcon.isFloating();
}

export default {
	onReady,
	show,
	hide,
	start,
	isStarted,
	stop,
	isFloating,
	isReady
}
