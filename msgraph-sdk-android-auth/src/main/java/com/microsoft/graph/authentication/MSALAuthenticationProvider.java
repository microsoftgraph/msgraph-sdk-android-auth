package com.microsoft.graph.authentication;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import javax.annotation.Nonnull;
import android.util.Log;

import com.microsoft.identity.client.AuthenticationCallback;
import com.microsoft.identity.client.AuthenticationResult;
import com.microsoft.identity.client.IAccount;
import com.microsoft.identity.client.PublicClientApplication;
import com.microsoft.identity.client.exception.MsalClientException;
import com.microsoft.identity.client.exception.MsalException;
import com.microsoft.identity.client.exception.MsalServiceException;
import com.microsoft.identity.client.exception.MsalUiRequiredException;
import com.microsoft.graph.core.ClientException;

import com.microsoft.graph.http.IHttpRequest;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import okhttp3.Request;

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
    public MSALAuthenticationProvider(@Nonnull Activity activity,
                                      @Nonnull Application application,
                                      @Nonnull PublicClientApplication publicClientApplication,
                                      @Nonnull String scopes[]) {
        this.publicClientApplication = publicClientApplication;
        this.scopes = scopes;
        this.application = application;
        this.callbacks = new Callback(activity);
        application.registerActivityLifecycleCallbacks(callbacks);
    }

    private class AuthorizationData{
        private AuthenticationResult authenticationResult;
        private CountDownLatch latch;
        private ClientException clientException;
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
        public void setClientException(ClientException clientException){
            this.clientException = clientException;
        }
        public ClientException getClientException(){
            return clientException;
        }
    }

    @Override
    public Request authenticateRequest(Request request){
        Log.d(TAG, "Authenticating request");
        AuthorizationData authorizationData = new AuthorizationData(new CountDownLatch(1));
        startAuthentication(authorizationData);
        try {
            authorizationData.getLatch().await();
        }catch (InterruptedException e){
            e.printStackTrace();
        }
        if(authorizationData.getAuthenticationResult() != null) {
            return request.newBuilder().addHeader(Constants.AUTHORIZATION_HEADER, Constants.BEARER + authorizationData.getAuthenticationResult().getAccessToken()).build();
        }
        else throw authorizationData.getClientException();
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
        if(authorizationData.getAuthenticationResult()!=null)
            httpRequest.addHeader(Constants.AUTHORIZATION_HEADER, Constants.BEARER + authorizationData.getAuthenticationResult().getAccessToken());
        else throw authorizationData.getClientException();
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
                    String message = e.getMessage();
                    if (e instanceof MsalClientException) {
                        message = "Exception inside MSAL" + e.getMessage();
                    } else if (e instanceof MsalServiceException) {
                        message = "Exception when communicating with the STS, likely config issue " + e.getMessage();
                    }
                    authorizationData.setClientException(new ClientException(message, e));
                    authorizationData.getLatch().countDown();
                }
            }

            @Override
            public void onCancel() {
                ClientException clientException = new ClientException("User pressed cancel", new Exception("Cancelled acquiring token silently"));
                authorizationData.setClientException(clientException);
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
                String message = e.getMessage();
                if (e instanceof MsalClientException) {
                    message = "Exception inside MSAL " + e.getMessage();
                } else if (e instanceof MsalServiceException) {
                    message = "Exception when communicating with the STS, likely config issue " + e.getMessage();
                }
                ClientException clientException = new ClientException(message, e);
                authorizationData.setClientException(clientException);
                authorizationData.getLatch().countDown();
            }

            @Override
            public void onCancel() {
                ClientException clientException = new ClientException("User pressed cancel", new Exception("Cancelled acquiring token interactively"));
                authorizationData.setClientException(clientException);
                authorizationData.getLatch().countDown();
            }
        };
    }

    public void handleInteractiveRequestRedirect(int requestCode, int resultCode, Intent data) {
        publicClientApplication.handleInteractiveRequestRedirect(requestCode, resultCode, data);
    }
}
