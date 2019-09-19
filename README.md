# Microsoft Graph Android Preview Authentication Library

Use MS Graph Android Authentication library to authenticate to use [Microsoft Graph API](https://graph.microsoft.io/en-us/getting-started) in your Android application!
Use this authentication library along with [msgraph-sdk-java](https://github.com/microsoftgraph/msgraph-sdk-java) or [msgraph-sdk-java-core](https://github.com/microsoftgraph/msgraph-sdk-java-core) to make requests to Microsoft Graph APIs

## Important Note about the Microsoft Graph Android Preview Authentication Library
During the preview we may make changes to the API, and other mechanisms of this library, which you will be required to take along with bug fixes or feature improvements. This may impact your application. An API change may require you to update your code. When we provide the General Availability release we will require you to update to the General Availability version within six months, as applications written using a preview version of library may no longer work.

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

#### Step 3: Create your MSAL configuration file

1.  Register your application by following the steps at [Register your app with the Azure AD v2.0 endpoint](https://developer.microsoft.com/en-us/graph/docs/concepts/auth_register_app_v2).
It's simplest to create your configuration file as a "raw" resource file in your project resources.  You'll be able to refer to this using the generated resource identifier when constructing an instance of PublicClientApplication.

```javascript
{
  "client_id" : "<YOUR_CLIENT_ID>",
  "redirect_uri" : "msauth://<YOUR_PACKAGE_NAME>/<YOUR_BASE64_URL_ENCODED_PACKAGE_SIGNATURE>"
  ...
}
```

#### Step 4 : Configure your app's AndroidManifest.xml

1. Request the following permissions via the Android Manifest
```
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
```

2. Configure an intent filter in the Android Manifest, using your redirect URI.
```XML
    <!--Intent filter to capture authorization code response from the default browser on the device calling back to our app after interactive sign in -->
    <activity
        android:name="com.microsoft.identity.client.BrowserTabActivity">
        <intent-filter>
            <action android:name="android.intent.action.VIEW" />
            <category android:name="android.intent.category.DEFAULT" />
            <category android:name="android.intent.category.BROWSABLE" />
            <data
                android:scheme="msauth"
                android:host="<YOUR_PACKAGE_NAME>"
                android:host="/<YOUR_BASE64_URL_ENCODED_PACKAGE_SIGNATURE>" />
        </intent-filter>
    </activity>
```

## 2. Getting started

### 2.1 Create an instance of **MSALAuthenticationProvider**

```
PublicClientApplication.create(getContext(),
    R.raw.your_json_config_file,
    new IPublicClientApplication.ApplicationCreatedListener() {
        @Override
        public void onCreated(IPublicClientApplication publicClientApplication) {
            MSALAuthenticationProvider msalAuthenticationProvider = new MSALAuthenticationProvider(
                getActivity(),
                getApplication(),
                publicClientApplication,
                scopes);
            }

        @Override
        public void onError(MsalException exception) {
            ...
        }
    });
```

### 2.2 Make requests against the [Microsoft Graph REST API](https://developer.microsoft.com/en-us/graph/docs/concepts/overview)

#### **Get your info**
Usage in [msgraph-sdk-java](https://github.com/microsoftgraph/msgraph-sdk-java)
```java
IGraphServiceClient graphClient =
  GraphServiceClient
    .builder()
    .authenticationProvider(msalAuthenticationProvider)
    .buildClient();

User user = graphClient
  .me()
  .buildRequest()
  .get();
System.out.println(user.displayName);
```

Usage in [msgraph-sdk-java-core](https://github.com/microsoftgraph/msgraph-sdk-java-core)
```java
OkHttpClient graphClient = HttpClients.createDefault(msalAuthenticationProvider);
Request request = new Request.Builder().url("https://graph.microsoft.com/v1.0/me").build();
Response response = client.newCall(request).execute();
System.out.println(response.body().string());
```

#### **To retrieve the user's drive**
Usage in [msgraph-sdk-java](https://github.com/microsoftgraph/msgraph-sdk-java)
```java
IGraphServiceClient graphClient =
  GraphServiceClient
    .builder()
    .authenticationProvider(msalAuthenticationProvider)
    .buildClient();

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

Usage in [msgraph-sdk-java-core](https://github.com/microsoftgraph/msgraph-sdk-java-core)
```java
OkHttpClient graphClient = HttpClients.createDefault(msalAuthenticationProvider);
Request request = new Request.Builder().url("https://graph.microsoft.com/v1.0/me/drive").build();
Response response = client.newCall(request).execute();
System.out.println(response.body().string());
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
