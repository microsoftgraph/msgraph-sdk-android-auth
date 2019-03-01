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

    private class AuthorizationData{
        private AuthenticationResult authenticationResult;
        private CountDownLatch latch;
        public AuthorizationData(CountDownLatch latch){
            this.latch = latch;
        }
        public AuthenticationResult getAuthenticationResult(){
            return authenticationResult;
        }
        public void setAuthenticationResult(AuthenticationResult authenticationResult){
            this.authenticationResult = authenticationResult;
        }
        public CountDownLatch getLatch(){
            return latch;
        }
    }

    @Override
    public void authenticateRequest(IHttpRequest httpRequest) {
        Log.d(TAG, "Authenticating request");
        AuthorizationData authorizationData = new AuthorizationData(new CountDownLatch(1));
        startAuthentication(authorizationData);
        try {
            authorizationData.getLatch().await();
        }catch (InterruptedException e){
            e.printStackTrace();
        }
        httpRequest.addHeader(Constants.AUTHORIZATION_HEADER, Constants.BEARER + authorizationData.getAuthenticationResult().getAccessToken());
    }

    private void startAuthentication(AuthorizationData authorizationData){
        List<IAccount> accounts = publicClientApplication.getAccounts();
        if (accounts != null && accounts.size() > 0) {
            IAccount firstAccount = accounts.get(0);
            GetAccessTokenSilentAsync(authorizationData, firstAccount);
        } else {
            GetAccessTokenInteractiveAsync(authorizationData);
        }
    }

    private void GetAccessTokenSilentAsync(AuthorizationData authorizationData, IAccount firstAccount){
        Log.d(TAG, "Trying to acquire token silently");
        publicClientApplication.acquireTokenSilentAsync(scopes, firstAccount, getAuthSilentCallback(authorizationData));
    }

    private void GetAccessTokenInteractiveAsync(AuthorizationData authorizationData){
        Log.d(TAG, "Acquiring token interactively");
        publicClientApplication.acquireToken(getCurrentActivity(), scopes, getAuthInteractiveCallback(authorizationData));
    }

    private Activity getCurrentActivity() {
        Log.d(TAG, "Get current activity : " + this.callbacks.getActivity().getLocalClassName());
        return this.callbacks.getActivity();
    }

    private AuthenticationCallback getAuthSilentCallback(final AuthorizationData authorizationData) {
        return new AuthenticationCallback() {
            @Override
            public void onSuccess(AuthenticationResult authenticationResult) {
                Log.d(TAG, "Silent authentication successful");
                authorizationData.setAuthenticationResult(authenticationResult);
                authorizationData.getLatch().countDown();
            }

            @Override
            public void onError(MsalException e) {
                if(e instanceof MsalUiRequiredException) {
                    GetAccessTokenInteractiveAsync(authorizationData);
                } else {
                    if (e instanceof MsalClientException) {
                        Log.d(TAG, "Exception inside MSAL" + e.getMessage());
                    } else if (e instanceof MsalServiceException) {
                        Log.d(TAG, "Exception when communicating with the STS, likely config issue " + e.getMessage());
                    }
                    e.printStackTrace();
                    authorizationData.getLatch().countDown();
                }
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "User pressed cancel");
                authorizationData.getLatch().countDown();
            }
        };
    }

    private AuthenticationCallback getAuthInteractiveCallback(final AuthorizationData authorizationData) {
        return new AuthenticationCallback() {
            @Override
            public void onSuccess(AuthenticationResult authenticationResult) {
                Log.d(TAG, "Interactive authentication successful");
                authorizationData.setAuthenticationResult(authenticationResult);
                authorizationData.getLatch().countDown();
            }

            @Override
            public void onError(MsalException e) {
                Log.d(TAG, "Interactive authentication error");
                if (e instanceof MsalClientException) {
                    Log.d(TAG, "Exception inside MSAL " + e.getMessage());
                } else if (e instanceof MsalServiceException) {
                    Log.d(TAG, "Exception when communicating with the STS, likely config issue " + e.getMessage());
                }
                e.printStackTrace();
                authorizationData.getLatch().countDown();
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "User pressed cancel");
                authorizationData.getLatch().countDown();
            }
        };
    }

    public void handleInteractiveRequestRedirect(int requestCode, int resultCode, Intent data) {
        publicClientApplication.handleInteractiveRequestRedirect(requestCode, resultCode, data);
    }
}
