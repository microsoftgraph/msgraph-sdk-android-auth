# Microsoft Graph Android Authentication Provider for Java SDK

Get started with the Microsoft Graph SDK for Java by integrating the [Microsoft Graph API](https://graph.microsoft.io/en-us/getting-started) into your Android or Java application!
Use Microsoft Graph Android Auth Library for authentication

## 1. Installation

### 1.1 Install via Gradle

Add repository in project level `build.gradle`
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

implementation dependency for `microsoft-graph-android-auth` to your app's `build.gradle`:

```gradle
dependency {
    // Include the auth sdk as a dependency
    implementation 'com.microsoft.graph:microsoft-graph-android-auth:0.1.0-SNAPSHOT'
}
```

### 1.3 Enable ProGuard (Android)
The nature of the Graph API is such that the SDK needs quite a large set of classes to describe its functionality. You need to ensure that [ProGuard](https://developer.android.com/studio/build/shrink-code.html) is enabled on your project. Otherwise, you will incur long build times for functionality that is not necessarily relevant to your particular application. If you are still hitting the 64K method limit, you can also enable [multidexing](https://developer.android.com/studio/build/multidex.html).

### 1.4 Configure the AndroidManifest.xml
    Give your app Internet permissions

    <uses-permission android:name="android.permission.INTERNET"/>
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>

    Configure your Intent filter, make sure you add your App/Client ID

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

## 2. Getting started

### 2.1 Register your android application

Register your application by following the steps at [Register your app with the Azure AD v2.0 endpoint](https://developer.microsoft.com/en-us/graph/docs/concepts/auth_register_app_v2).

### 2.2 Create an MSALAuthenticationProvider object

An instance of the **GraphServiceClient** class handles building requests, sending them to the Microsoft Graph API, and processing the responses. To create a new instance of this class, you need to provide an instance of `MSALAuthenticationProvider`, which can authenticate requests to Microsoft Graph.

```
PublicClientApplication publicClientApplication = new PublicClientApplication(getApplicationContext(), "CLIENT_ID_OF_YOUR_APPLICATION");
MSALAuthenticationProvider msalAuthenticationProvider = new MSALAuthenticationProvider(
    getActivity(),
    getApplication(),
    publicClientApplication,
    scopes);
```

### 1.5 Add Auth helper in your activity calling graph
//Create an onActivityResult method
/* Handles the redirect from the System Browser */
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
     msalAuthenticationProvider.handleInteractiveRequestRedirect(requestCode, resultCode, data);
}

### 2.3 Get a GraphServiceClient object
After you have set the correct application ID/client ID, you must get a **GraphServiceClient** object to make requests against the service. The SDK stores the account information for you, but when a user signs in for the first time, it invokes the UI to get the user's account information.

```java
IGraphServiceClient graphClient =
  GraphServiceClient
    .builder()
    .authenticationProvider(msalAuthenticationProvider)
    .buildClient();
```

## 3. Make requests against the service

After you have a GraphServiceClient object, you can begin making calls against the service. The requests against the service look like our [REST API](https://developer.microsoft.com/en-us/graph/docs/concepts/overview).

### 3.1 Get the user's drive

To retrieve the user's drive:

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

## 4. Issues

For known issues, see [issues](https://github.com/microsoftgraph/msgraph-sdk-android-auth/issues).

## 5. Contributions

The Microsoft Graph SDK is open for contribution. To contribute to this project, see [Contributing](https://github.com/microsoftgraph/msgraph-sdk-android-auth/blob/master/CONTRIBUTING.md).

Thanks to everyone who has already devoted time to improving the library:

<!-- ALL-CONTRIBUTORS-LIST:START  -->
<!-- prettier-ignore -->
| [<img src="https://avatars3.githubusercontent.com/u/16473684?v=4" width="100px;"/><br /><sub><b>Nakul Sabharwal</b></sub>](https://developer.microsoft.com/graph)<br />[](#question-NakulSabharwal "Answering Questions") [](https://github.com/microsoftgraph/msgraph-sdk-android-auth/commits?author=NakulSabharwal "Code") [](https://github.com/microsoftgraph/msgraph-sdk-android-auth/wiki "Documentation") [](#review-NakulSabharwal "Reviewed Pull Requests") [](https://github.com/microsoftgraph/msgraph-sdk-android-auth/commits?author=NakulSabharwal "Tests")| [<img src="https://avatars2.githubusercontent.com/u/3197588?v=4" width="100px;"/><br /><sub><b>Deepak Agrawal</b></sub>](https://github.com/deepak2016)<br />
| :---: | :---: |
<!-- ALL-CONTRIBUTORS-LIST:END -->

This project follows the [all-contributors](https://github.com/kentcdodds/all-contributors) specification. Contributions of any kind are welcome!

## 6. Supported Java versions
The Microsoft Graph SDK for Java library is supported at runtime for Java 7+ and [Android API revision 21](http://source.android.com/source/build-numbers.html) and greater.

## 7. License

Copyright (c) Microsoft Corporation. All Rights Reserved. Licensed under the [MIT license](LICENSE).

## 8. Third-party notices

[Third-party notices](THIRD%20PARTY%20NOTICES)
