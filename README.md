## react-native-floater-icon


## Installation (iOS)

Currently No Support

## Installation (Android)

`npm i react-native-floater-icon@https://github.com/danielpgauer/react-native-floater-icon.git --save`

Make alterations to the following files:

* `android/settings.gradle`

```gradle
...
include ':react-native-floater-icon'
project(':react-native-floater-icon').projectDir = new File(settingsDir, '../node_modules/react-native-floater-icon/android')
```

* `android/app/build.gradle`

```gradle
...
dependencies {
    ...
    compile project(':react-native-floater-icon')
}
```


```java
import com.danielpgauer.FloaterIconPackage; // <------- add package

public class MainApplication extends ReactActivity {
   // ...
    @Override
    protected List<ReactPackage> getPackages() {
      return Arrays.<ReactPackage>asList(
        new MainReactPackage(), // <---- add comma
        new FloaterIconPackage() // <---------- add package
      );
    }
```

## Example usage (Android only)

```javascript
// require the module
import * as FloaterIcon = require('react-native-floater-icon');

await FloaterIcon.show();
```

