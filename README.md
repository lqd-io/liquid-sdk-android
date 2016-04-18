# Liquid Android SDK

[![Join the chat at https://gitter.im/lqd-io/liquid-sdk-android](https://badges.gitter.im/lqd-io/liquid-sdk-android.svg)](https://gitter.im/lqd-io/liquid-sdk-android?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

[![Build Status](https://travis-ci.org/lqd-io/liquid-sdk-android.svg?branch=master)](https://travis-ci.org/lqd-io/liquid-sdk-android) [![Codacy Badge](https://api.codacy.com/project/badge/abc35fcee53b4e12b1858e2a3dadca9e)](https://www.codacy.com/app/letz/liquid-sdk-android)
<br>
[![Maven Central](http://img.shields.io/maven-central/v/io.lqd/liquid-android.svg?style=flat)](http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22liquid-android%22)


# Quick Start to Liquid SDK for Android

This document is just a quick start introduction to Liquid SDK for Android. We recommend you to read the full documentation at [https://www.onliquid.com/documentation/android](https://www.onliquid.com/documentation/android).

To integrate Liquid in your app, just follow these simple steps below.

## Setup

#### Android Studio / Gradle (recomended)

```
// build.gradle file
dependencies {
  // Your Dependencies
  compile 'io.lqd:liquid-android:2.0.2@aar'
}
```

#### Maven

```xml
<!-- pom.xml file -->
<dependency>
    <groupId>io.lqd</groupId>
    <artifactId>liquid-android</artifactId>
    <version>2.0.2</version>
</dependency>
```

#### Eclipse

1. Clone [Liquid SDK for android](https://github.com/lqd-io/liquid-sdk-android/).
2. Import the project to Eclipse (or other IDE).
3. Add Liquid to your Application Project as a library.


## Start using Liquid

### 1. Add Permissions to Application Manifest

```xml
<!-- AndroidManifest.xml -->
<uses-permission android:name="android.permission.INTERNET"></uses-permission>
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"></uses-permission>
```

### 2. Initialize Liquid Singleton

In your `onCreate()` callback method initialize the Liquid Singleton
```java
Liquid lqd;

@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    lqd = Liquid.initialize(this, "YOUR_APP_TOKEN");
}
```

### 3. Add Callbacks (mandatory if `minSdkVersion` < 14)

If your app supports applications with `minSdkVersion` < 14, you need to add the methods below to your activities that use Liquid.

```java
	@Override
	public void onResume() {
		super.onResume();
		Liquid.getInstance().activityResumed(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		Liquid.getInstance().activityPaused(this);
	}

	@Override
	public void onStop() {
		super.onStop();
		Liquid.getInstance().activityStopped(this);
	}

	@Override
	public void onStart() {
		super.onStart();
		Liquid.getInstance().activityStarted(this);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Liquid.getInstance().activityDestroyed(this);
	}
```

### 4. Identify a user (optional)

If all your users are anonymous, you can skip this step. If not, you need to identify them and define their profile.
Typically this is done at the same time your user logs in your app (or you perform an auto login), as seen in the example below:

```java
lqd.identifyUser("USER_ID");
```

The **username** or **email** are some of the typical user identifiers used by apps.

### 5. Track events

You can track any type of event in your app, using one of the following methods:

```java
  lqd.track("Bought Product");
```

### 6. Personalize your app (with dynamic variables)

You can transform any old-fashioned static variable into a "liquid" dynamic variable just by replacing it with a Liquid method. You can use a dynamic variable like this:

```java
mWelcomeMessage.setText(liquid.getStringVariable("welcome_message", "Default Welcome!"));
```

### Full documentation

We recommend you to read the full documentation at [https://www.lqd.io/documentation/android](https://www.onliquid.com/documentation/android).


# Author

Liquid Data Intelligence, S.A.

# License

Liquid is available under the Apache license. See the LICENSE file for more info.
