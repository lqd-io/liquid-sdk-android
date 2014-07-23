# Liquid Android SDK Change Log

## 0.8.0-beta: User alias + Stability improvements + Rename device attributes

* **[feature]** Add support to **alias** *anonymous* users with *identified* users.
* **[feature]** Anonymous users are now automatically handled. If you never identify your user, all activity will be tracked anonymously. As soon as an user is identified, all that anonymous activity will be automatically aliased to the identified user.
* **[feature]** When a user is identified, it is kept in cache for the next launch. This means that you don't need to identify each time you start the app again.
* **[bugfix]** Fix a problem on HTTP Requests queue, that could cause duplication or loss of events.
* **[bugfix]** Prevent starting new session when activity changes.
* **[enhancement]** Better handling of background and foreground events.
* **[enhancement]** Speed and stability improvements.
* **[enhancement]** Improvements on event tracking dates that avoid two events tracked too quickly to be tracked in the wrong order.
* **[enhancements]** The use of reserved names on Users and Events attributes will raise an assert under development environment (skipped/ignored attributes in production).
* **[change]** Changed Device attributes from `camelCase` to `underscore` naming convention, and removed `_` prefix (e.g: attribute `_systemVersion` was changed to `system_version`). This will not affect your queries on Liquid dashboard.
* **[deprecate]** Method `flushOnBackground:` was deprecated. When the app goes on background, HTTP queue is always flushed.


## 0.7.0-beta: Minor issues

* **[feature]** Invalid characters on attributes will raise an exception in development.
* **[deprecate]** `identifyUser(String identifier, Location location)` and `identifyUser(String identifier,	HashMap<String, Object> attributes, Location location)`.
* **[deprecate]** `setUserLocation(Location l) -> Use `setCurrentLocation(Location l)` instead.

## 0.6.0-beta: First public release

* First public release
