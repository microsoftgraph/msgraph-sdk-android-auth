# Microsoft Graph Android Authentication Library

Use MS Graph Android Authentication library to authenticate to use [Microsoft Graph API](https://graph.microsoft.io/en-us/getting-started) in your Android application!
Use this authentication library along with [msgraph-sdk-java](https://github.com/microsoftgraph/msgraph-sdk-java) to make requests to Microsoft Graph APIs.

## 1. Installation

### 1.1 Install via Gradle

#### Step 1 : Add this repository in project level `build.gradle`
```
allprojects {
    repositories {
        google()
        jcenter()
        jcenter{
            url 'http://oss.jfrog.org/oss-snapshot-local'
        }
    }
}
```

#### Step 2 : Add dependency for `microsoft-graph-android-auth` to your app's `build.gradle`:

```gradle
dependency {
    // Include the auth library as a dependency in app level build.gradle
    implementation 'com.microsoft.graph:microsoft-graph-android-auth:0.1.0-SNAPSHOT'
}
```

#### Step 3 : Configure your app's AndroidManifest.xml to give Internet permissions
```
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
```
#### Step 4 : Add BrowserTabActivity activity in AndroidManifest.xml. Make sure to put your App/Client ID at <YOUR_CLIENT_ID>.
```
<!--Intent filter to capture System Browser calling back to our app after Sign In-->
<activity
    android:name="com.microsoft.identity.client.BrowserTabActivity">
    <intent-filter>
    <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data android:scheme="msal<YOUR_CLIENT_ID>"
            android:host="auth" />
        </intent-filter>
</activity>
```

## 2. Getting started

### 2.1 Registering your android application
Register your application by following the steps at [Register your app with the Azure AD v2.0 endpoint](https://developer.microsoft.com/en-us/graph/docs/concepts/auth_register_app_v2).
Once registered you will get CLIENT_ID of your application.

### 2.2 Create an instance of **MSALAuthenticationProvider**

```
PublicClientApplication publicClientApplication = new PublicClientApplication(getApplicationContext(), "CLIENT_ID_OF_YOUR_APPLICATION");
MSALAuthenticationProvider msalAuthenticationProvider = new MSALAuthenticationProvider(
    getActivity(),
    getApplication(),
    publicClientApplication,
    scopes);
```

### 2.3 Create an instance of the **GraphServiceClient**
```java
IGraphServiceClient graphClient =
  GraphServiceClient
    .builder()
    .authenticationProvider(msalAuthenticationProvider)
    .buildClient();
```

### 2.4 Add auth helper in your Activity
```
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    msalAuthenticationProvider.handleInteractiveRequestRedirect(requestCode, resultCode, data);
}
```


### 2.5 Make requests against the [Microsoft Graph REST API](https://developer.microsoft.com/en-us/graph/docs/concepts/overview)

**Get your name**
```java
User user = graphClient
  .me()
  .buildRequest()
  .get();
System.out.println(user.displayName);
```
  
**To retrieve the user's drive**
```java
graphClient
  .me()
  .drive()
  .buildRequest()
  .get(new ICallback<Drive>() {
     @Override
     public void success(final Drive result) {
        System.out.println("Found Drive " + result.id);
     }
     ...
     // Handle failure case
  });
```

## 3. Issues

For known issues, see [issues](https://github.com/microsoftgraph/msgraph-sdk-android-auth/issues).

## 4. Contributions

This library is open for contribution. To contribute to this project, see [Contributing](https://github.com/microsoftgraph/msgraph-sdk-android-auth/blob/master/CONTRIBUTING.md).

Thanks to everyone who has already devoted time to improving the library:

<!-- ALL-CONTRIBUTORS-LIST:START  -->
<!-- prettier-ignore -->
| [<img src="https://avatars3.githubusercontent.com/u/16473684?v=4" width="100px;"/><br /><sub><b>Nakul Sabharwal</b></sub>](https://developer.microsoft.com/graph)<br />[](#question-NakulSabharwal "Answering Questions") [](https://github.com/microsoftgraph/msgraph-sdk-android-auth/commits?author=NakulSabharwal "Code") [](https://github.com/microsoftgraph/msgraph-sdk-android-auth/wiki "Documentation") [](#review-NakulSabharwal "Reviewed Pull Requests") [](https://github.com/microsoftgraph/msgraph-sdk-android-auth/commits?author=NakulSabharwal "Tests")| [<img src="https://avatars2.githubusercontent.com/u/3197588?v=4" width="100px;"/><br /><sub><b>Deepak Agrawal</b></sub>](https://github.com/deepak2016)<br />
| :---: | :---: |
<!-- ALL-CONTRIBUTORS-LIST:END -->

This project follows the [all-contributors](https://github.com/kentcdodds/all-contributors) specification. Contributions of any kind are welcome!

## 5. Supported Java versions
This library supports runtime for Java 7+ and [Android API revision 21](http://source.android.com/source/build-numbers.html) and greater.

## 6. License

Copyright (c) Microsoft Corporation. All Rights Reserved. Licensed under the [MIT license](LICENSE).

## 7. Third-party notices

[Third-party notices](THIRD%20PARTY%20NOTICES)