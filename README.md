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

### 4 Sample

AndroidManifest.xml
```
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="your.domain.name">

    <uses-permission android:name="android.permission.INTERNET"/>
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>

    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/AppTheme">
        <activity android:name=".MainActivity">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <activity android:name="com.microsoft.identity.client.BrowserTabActivity">
            <intent-filter>
                <action android:name="android.intent.action.VIEW"/>
                <category android:name="android.intent.category.DEFAULT"/>
                <category android:name="android.intent.category.BROWSABLE"/>
                <data android:host="auth" android:scheme="msal508f0c8e-0b04-45f5-8514-69a5570026c4"/>
            </intent-filter>
        </activity>

    </application>
</manifest>
```

MainActivity.java
```
package com.your.packagename;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.microsoft.graph.authentication.MSALAuthenticationProvider;
import com.microsoft.graph.concurrency.ICallback;
import com.microsoft.graph.core.ClientException;
import com.microsoft.graph.models.extensions.IGraphServiceClient;
import com.microsoft.graph.models.extensions.User;
import com.microsoft.graph.requests.extensions.GraphServiceClient;
import com.microsoft.identity.client.AuthenticationCallback;
import com.microsoft.identity.client.AuthenticationResult;
import com.microsoft.identity.client.PublicClientApplication;
import com.microsoft.identity.client.exception.MsalClientException;
import com.microsoft.identity.client.exception.MsalException;
import com.microsoft.identity.client.exception.MsalServiceException;

public class MainActivity extends AppCompatActivity {
    class LongOperation extends AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String... strings) {

            //Make a graph call
            return graphClient.me().buildRequest().get().displayName;

        }
        @Override
        protected void onPostExecute(String result) {
            TextView txt = (TextView) findViewById(R.id.viewid);
            txt.setText(result);
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}
    };
    IGraphServiceClient graphClient;
    String SCOPES[] = {"https://graph.microsoft.com/User.Read"};
    PublicClientApplication publicClientApplication;
    MSALAuthenticationProvider msalAuthenticationProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    publicClientApplication = new PublicClientApplication(this.getApplicationContext(), "YOUR CLIENT ID HERE");
    msalAuthenticationProvider = new MSALAuthenticationProvider(this,
                getApplication(),
                publicClientApplication,
                SCOPES);

    graphClient =
              GraphServiceClient
                      .builder()
                      .authenticationProvider(msalAuthenticationProvider)
                      .buildClient();


    Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                LongOperation longOperation = new LongOperation();
                longOperation.execute("");
            }
        });
    }

    //Create onActivityResult in MainActivity and call the below function
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        msalAuthenticationProvider.handleInteractiveRequestRedirect(requestCode, resultCode, data);
    }
}
```

## 5. Documentation

For more detailed documentation, see:

* [Overview](https://github.com/microsoftgraph/msgraph-sdk-java/wiki/Overview)
* [Extending the library](https://github.com/microsoftgraph/msgraph-sdk-java/wiki/Extending-the-Library)
* [Handling Open Types, PATCH support with `null` values](https://github.com/microsoftgraph/msgraph-sdk-java/wiki/Working-with-Open-Types)
* [Collections](https://github.com/microsoftgraph/msgraph-sdk-java/wiki/Working-with-Collections)
* [Making custom requests](https://github.com/microsoftgraph/msgraph-sdk-java/wiki/Custom-Requests)
* [Known issues](https://github.com/microsoftgraph/msgraph-sdk-java/wiki/Known-Issues)
* [Contributions](https://github.com/microsoftgraph/msgraph-sdk-java/blob/master/CONTRIBUTING.md)

## 6. Issues

For known issues, see [issues](https://github.com/microsoftgraph/msgraph-sdk-android-auth/issues).

## 7. Contributions

The Microsoft Graph SDK is open for contribution. To contribute to this project, see [Contributing](https://github.com/microsoftgraph/msgraph-sdk-java/blob/master/CONTRIBUTING.md).

Thanks to everyone who has already devoted time to improving the library:

<!-- ALL-CONTRIBUTORS-LIST:START  -->
<!-- prettier-ignore -->
| [<img src="https://avatars3.githubusercontent.com/u/16473684?v=4" width="100px;"/><br /><sub><b>Nakul Sabharwal</b></sub>](https://developer.microsoft.com/graph)<br />[](#question-NakulSabharwal "Answering Questions") [](https://github.com/microsoftgraph/msgraph-sdk-java/commits?author=NakulSabharwal "Code") [](https://github.com/microsoftgraph/msgraph-sdk-java/wiki "Documentation") [](#review-NakulSabharwal "Reviewed Pull Requests") [](https://github.com/microsoftgraph/msgraph-sdk-java/commits?author=NakulSabharwal "Tests")| [<img src="https://avatars2.githubusercontent.com/u/3197588?v=4" width="100px;"/><br /><sub><b>Deepak Agrawal</b></sub>](https://github.com/deepak2016)<br />
| :---: | :---: |
<!-- ALL-CONTRIBUTORS-LIST:END -->

This project follows the [all-contributors](https://github.com/kentcdodds/all-contributors) specification. Contributions of any kind are welcome!

## 8. Supported Java versions
The Microsoft Graph SDK for Java library is supported at runtime for Java 7+ and [Android API revision 21](http://source.android.com/source/build-numbers.html) and greater.

## 9. License

Copyright (c) Microsoft Corporation. All Rights Reserved. Licensed under the [MIT license](LICENSE).

## 10. Third-party notices

[Third-party notices](THIRD%20PARTY%20NOTICES)
