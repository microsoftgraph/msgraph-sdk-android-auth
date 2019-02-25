package com.microsoft.graph.authentication;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.microsoft.identity.client.AuthenticationCallback;
import com.microsoft.identity.client.AuthenticationResult;
import com.microsoft.identity.client.IAccount;
import com.microsoft.identity.client.PublicClientApplication;
import com.microsoft.identity.client.exception.MsalClientException;
import com.microsoft.identity.client.exception.MsalException;
import com.microsoft.identity.client.exception.MsalServiceException;
import com.microsoft.identity.client.exception.MsalUiRequiredException;

import com.microsoft.graph.http.IHttpRequest;

import java.util.List;

public class MSALAuthenticationProvider implements IMSALAuthenticationProvider {

    final String TAG = this.getClass().getSimpleName();
    private PublicClientApplication publicClientApplication;
    private String scopes[];
    private Application application;
    private IHttpRequest httpRequest;
    private Callback callbacks;

    /**
     * Initializes MSALAuthenticationProvider for authentication
     *
     * @param activity                The current activity instance
     * @param application             The current application instance
     * @param publicClientApplication The PublicClientApplication instance
     * @param scopes                  Scopes to access the protected resource
     */
    public MSALAuthenticationProvider(@NonNull Activity activity,
                                      @NonNull Application application,
                                      @NonNull PublicClientApplication publicClientApplication,
                                      @NonNull String scopes[]) {
        callbacks = new Callback(activity);
        application.registerActivityLifecycleCallbacks(callbacks);
        this.publicClientApplication = publicClientApplication;
        this.scopes = scopes;
    }

    @Override
    public void authenticateRequest(IHttpRequest httpRequest) {
        Log.d(TAG, "authenticating request");

        List<IAccount> accounts = publicClientApplication.getAccounts();
        if (accounts != null && accounts.size() > 0) {
            Log.d(TAG, "acquireTokenSilentAsync");
            IAccount firstAccount = accounts.get(0);
            publicClientApplication.acquireTokenSilentAsync(scopes, firstAccount, getAuthSilentCallback());
        } else {
            Log.d(TAG, "acquire token interactively");
            publicClientApplication.acquireToken(getCurrentActivity(), scopes, getAuthInteractiveCallback());
        }
    }

    private Activity getCurrentActivity() {
        Log.d(TAG, "getCurrentActivity: ");
        return this.callbacks.getActivity();
    }

    private AuthenticationCallback getAuthSilentCallback() {
        return new AuthenticationCallback() {
            @Override
            public void onSuccess(AuthenticationResult authenticationResult) {
                Log.d(TAG, "Silent authentication success");
                httpRequest.addHeader("Authorization", "Bearer " + authenticationResult.getAccessToken());
            }

            @Override
            public void onError(MsalException exception) {
                if (exception instanceof MsalClientException) {
                    Log.d(TAG, "Exception inside MSAL" + exception.getMessage());
                    exception.printStackTrace();
                } else if (exception instanceof MsalServiceException) {
                    Log.d(TAG, "Exception when communicating with the STS, likely config issue " + exception.getMessage() + exception);
                    exception.printStackTrace();
                } else if (exception instanceof MsalUiRequiredException) {
                    Log.d(TAG, "acquire token interactively");
                    publicClientApplication.acquireToken(getCurrentActivity(), scopes, getAuthInteractiveCallback());
                }
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "User pressed cancel");
            }
        };
    }

    private AuthenticationCallback getAuthInteractiveCallback() {
        return new AuthenticationCallback() {
            @Override
            public void onSuccess(AuthenticationResult authenticationResult) {
                Log.d(TAG, "interactive authentication success");
                httpRequest.addHeader("Authorization", "Bearer " + authenticationResult.getAccessToken());
            }

            @Override
            public void onError(MsalException exception) {
                Log.d(TAG, "Interactive authentication error " + exception);
                if (exception instanceof MsalClientException) {
                    Log.d(TAG, "Exception inside MSAL" + exception.getMessage());
                    exception.printStackTrace();
                } else if (exception instanceof MsalServiceException) {
                    Log.d(TAG, "Exception when communicating with the STS, likely config issue " + exception.getMessage() + exception);
                    exception.printStackTrace();
                }
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "User pressed cancel");
            }
        };
    }

    public void handleInteractiveRequestRedirect(int requestCode, int resultCode, Intent data) {
        publicClientApplication.handleInteractiveRequestRedirect(requestCode, resultCode, data);
    }

}
