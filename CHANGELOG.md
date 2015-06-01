# Liquid Android SDK Change Log

## 1.2.0: Attributes bulk insert + bug fixes

  * **[feature]** Add setUserAttributes() to set multiple user attributes
  * **[bugfix]** Fix NPE in softReset()
  * **[bugfix]** LiquidPackage loading on initialize

<br>

## 1.1.0: Better session handling

  * **[feature]** Identifying a user no longer starts a new session
  * **[feature]** Keep User unique_id when calling `resetUser();` in an anonymous user
  * **[bugfix]** Fix UUID generation when user is a time traveler
  * **[bugfix]** Fix push registration in SDK below API 21

<br>
## 1.0.1: Add DeviceID accessor from Liquid to support Appsflyer Integration

  * **[feature]** Add DeviceID accessor from Liquid to support Appsflyer Integration

<br>

## 1.0.0: Push Notifications with title and custom sound

  * **[feature]** Add push notifications custom sound and title (configurable over Liquid dashboard)
  * **[feature]** Add option to set Logger level
  * **[bugfix]** Fix custom data points flush interval
  * **[enhancement]** Support GZIP requests
  * **[enhancement]** Improve stability and performance
  * **[enhancement]** Remove deprecated methods

<br>
## 0.8.4-beta: Minor Issues

* **[bugfix]** Fix NPE when user is loaded without attributes Hashmap

<br>
## 0.8.3-beta: Minor Issues

* **[bugfix]** Fix minor concurrency issue in **track();** method.
* **[bugfix]** Fix NullPointerException when getting connectivity status using emulator.
* **[enhancement]** Performance improvements.

<br>
## 0.8.2-beta: Change lib structure to Android Studio + Gradle

* **[feature]** Add Liquid to Maven Central Repository
* **[feature]** Change project structure to support Android Studio projects and Gradle

<br>
## 0.8.1-beta: Fix NullPointerException when trying to get values

* **[bugfix]** Fix NullPointerException when trying to get values before a liquid package arrives.

<br>
## 0.8.0-beta: User alias + Stability improvements + Rename device attributes

* **[feature]** Add support to **alias** *anonymous* users with *identified* users.
* **[feature]** Anonymous users are now automatically handled. If you never identify your user, all activity will be tracked anonymously. As soon as an user is identified, all that anonymous activity will be automatically aliased to the identified user.
* **[feature]** When a user is identified, it is kept in cache for the next launch. This means that you don't need to identify each time you start the app again.
* **[bugfix]** Fix a problem on HTTP Requests queue, that could cause duplication or loss of events.
* **[bugfix]** Prevent starting new session when activity changes.
* **[enhancement]** Better handling of background and foreground events.
* **[enhancement]** Speed and stability improvements.
* **[enhancement]** Improvements on event tracking dates that avoid two events tracked too quickly to be tracked in the wrong order.
* **[enhancement]** The use of reserved names on Users and Events attributes will raise an assert under development environment (skipped/ignored attributes in production).
* **[change]** Changed Device attributes from `camelCase` to `underscore` naming convention, and removed `_` prefix (e.g: attribute `_systemVersion` was changed to `system_version`). This will not affect your queries on Liquid dashboard.
* **[deprecate]** Method `flushOnBackground:` was deprecated. When the app goes on background, HTTP queue is always flushed.

-----------------

<br>
## 0.7.0-beta: Minor issues

* **[feature]** Invalid characters on attributes will raise an exception in development.
* **[deprecate]** `identifyUser(String identifier, Location location)` and `identifyUser(String identifier,	HashMap<String, Object> attributes, Location location)`.
* **[deprecate]** `setUserLocation(Location l) -> Use `setCurrentLocation(Location l)` instead.

-----------------

<br>
## 0.6.0-beta: First public release

* First public release
