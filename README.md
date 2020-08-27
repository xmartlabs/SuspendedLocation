# Suspended Location
![Check and compile](https://github.com/xmartlabs/SuspendedLocation/workflows/Check%20and%20compile/badge.svg?branch=master)
[![Release](https://jitpack.io/v/xmartlabs/AndroidSwissKnife.svg)](https://jitpack.io/#xmartlabs/SuspendedLocation)

A library that wraps the google [places](https://developers.google.com/places/android-sdk/overview) and [geolocation](https://developers.google.com/android/reference/com/google/android/gms/location/package-summary) Apis through coroutines

It's divided into a series of modules with different proposes:
- [Core](/core): Contains functions that wraps [geolocation](https://developers.google.com/android/reference/com/google/android/gms/location/package-summary) api.
- [Places](/places): Contains functions that wraps [places](https://developers.google.com/places/android-sdk/overview) api.

## Setup
Add library to project dependencies with JitPack.

```groovy
repositories {
    maven { url "https://jitpack.io" }
}

dependencies {
    implementation "com.github.xmartlabs.SuspendedLocation:core:${master-latest-hash-commit}"
    implementation "com.github.xmartlabs.SuspendedLocation:places:${master-latest-hash-commit}"
}
```

## About
Made with ❤️ by [XMARTLABS](http://xmartlabs.com)
