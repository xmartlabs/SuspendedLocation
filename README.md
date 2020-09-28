# Suspended Location
![Check and compile](https://github.com/xmartlabs/SuspendedLocation/workflows/Run%20lints%20and%20compile/badge.svg?branch=master)
[![Release](https://jitpack.io/v/xmartlabs/AndroidSwissKnife.svg)](https://jitpack.io/#xmartlabs/SuspendedLocation:0.0.1)

A library that wraps the google [places](https://developers.google.com/places/android-sdk/overview) and [geolocation](https://developers.google.com/android/reference/com/google/android/gms/location/package-summary) APIs through coroutines

It's divided into a series of modules with different purposes:
- [Core](/core): Contains functions that wrap [geolocation](https://developers.google.com/android/reference/com/google/android/gms/location/package-summary) API.
- [Places](/places): Contains functions that wrap [places](https://developers.google.com/places/android-sdk/overview) API.

## Setup
Add library to project dependencies with JitPack.

```groovy
repositories {
    maven { url "https://jitpack.io" }
}

dependencies {
    implementation "com.github.xmartlabs.SuspendedLocation:core:0.0.1"
    implementation "com.github.xmartlabs.SuspendedLocation:places:0.0.1"
}
```

## About
Made with ❤️ by [XMARTLABS](http://xmartlabs.com)
