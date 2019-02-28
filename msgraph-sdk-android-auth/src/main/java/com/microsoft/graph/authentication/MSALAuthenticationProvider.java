package com.microsoft.graph.authentication;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

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
import java.util.concurrent.CountDownLatch;

public class MSALAuthenticationProvider implements IMSALAuthenticationProvider {
    private final String TAG = this.getClass().getSimpleName();
    private PublicClientApplication publicClientApplication;
    private String scopes[];
    private Application application;
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
        this.publicClientApplication = publicClientApplication;
        this.scopes = scopes;
        this.application = application;
        this.callbacks = new Callback(activity);
        application.registerActivityLifecycleCallbacks(callbacks);
    }

    @Override
    public void authenticateRequest(IHttpRequest httpRequest) {
        Log.d(TAG, "Authenticating request");
        CountDownLatch latch = new CountDownLatch(1);

        List<IAccount> accounts = publicClientApplication.getAccounts();
        if (accounts != null && accounts.size() > 0) {
            IAccount firstAccount = accounts.get(0);
            GetAccessTokenSilentAsync(httpRequest, firstAccount, latch);
        } else {
            GetAccessTokenInteractiveAsync(httpRequest, latch);
        }

        try {
            latch.await();
        }catch (InterruptedException e){
            e.printStackTrace();
        }
    }

    private void GetAccessTokenSilentAsync(IHttpRequest httpRequest, IAccount firstAccount, CountDownLatch latch){
        Log.d(TAG, "Trying to acquir token silently");
        publicClientApplication.acquireTokenSilentAsync(scopes, firstAccount, getAuthSilentCallback(httpRequest, latch));
    }

    private void GetAccessTokenInteractiveAsync(IHttpRequest httpRequest, CountDownLatch latch){
        Log.d(TAG, "Acquiring token interactively");
        publicClientApplication.acquireToken(getCurrentActivity(), scopes, getAuthInteractiveCallback(httpRequest, latch));
    }

    private Activity getCurrentActivity() {
        Log.d(TAG, "Get current activity");
        return this.callbacks.getActivity();
    }

    private AuthenticationCallback getAuthSilentCallback(final IHttpRequest httpRequest, final CountDownLatch latch) {
        return new AuthenticationCallback() {
            @Override
            public void onSuccess(AuthenticationResult authenticationResult) {
                Log.d(TAG, "Silent authentication successful");
                httpRequest.addHeader(Constants.AUTHORIZATION_HEADER, Constants.BEARER + authenticationResult.getAccessToken());
                latch.countDown();
            }

            @Override
            public void onError(MsalException e) {
                if (e instanceof MsalClientException) {
                    Log.d(TAG, "Exception inside MSAL" + e.getMessage());
                    e.printStackTrace();
                } else if (e instanceof MsalServiceException) {
                    Log.d(TAG, "Exception when communicating with the STS, likely config issue " + e.getMessage());
                    e.printStackTrace();
                } else if (e instanceof MsalUiRequiredException) {
                    getAuthInteractiveCallback(httpRequest, latch);
                }
                latch.countDown();
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "User pressed cancel");
                latch.countDown();
            }
        };
    }

    private AuthenticationCallback getAuthInteractiveCallback(final IHttpRequest httpRequest, final CountDownLatch latch) {
        return new AuthenticationCallback() {
            @Override
            public void onSuccess(AuthenticationResult authenticationResult) {
                Log.d(TAG, "Interactive authentication successful");
                httpRequest.addHeader(Constants.AUTHORIZATION_HEADER, Constants.BEARER + authenticationResult.getAccessToken());
                latch.countDown();
            }

            @Override
            public void onError(MsalException e) {
                Log.d(TAG, "Interactive authentication error");
                if (e instanceof MsalClientException) {
                    Log.d(TAG, "Exception inside MSAL" + e.getMessage());
                } else if (e instanceof MsalServiceException) {
                    Log.d(TAG, "Exception when communicating with the STS, likely config issue " + e.getMessage());
                }
                e.printStackTrace();
                latch.countDown();
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "User pressed cancel");
                latch.countDown();
            }
        };
    }

    public void handleInteractiveRequestRedirect(int requestCode, int resultCode, Intent data) {
        publicClientApplication.handleInteractiveRequestRedirect(requestCode, resultCode, data);
    }
}
