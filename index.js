import { NativeModules } from 'react-native';
const { FloaterIcon } = NativeModules;

const onReady = () => {
	return FloaterIcon.onReady();
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
	isFloating
}
