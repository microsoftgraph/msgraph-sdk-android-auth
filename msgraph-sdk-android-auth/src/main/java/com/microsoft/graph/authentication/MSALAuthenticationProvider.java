package com.microsoft.graph.authentication;

import android.app.Activity;
import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.microsoft.graph.httpcore.ICoreAuthenticationProvider;
import com.microsoft.identity.client.AuthenticationCallback;
import com.microsoft.identity.client.IAccount;
import com.microsoft.identity.client.IAuthenticationResult;
import com.microsoft.identity.client.IMultipleAccountPublicClientApplication;
import com.microsoft.identity.client.IPublicClientApplication;
import com.microsoft.identity.client.ISingleAccountPublicClientApplication;
import com.microsoft.identity.client.exception.MsalClientException;
import com.microsoft.identity.client.exception.MsalException;
import com.microsoft.identity.client.exception.MsalServiceException;
import com.microsoft.identity.client.exception.MsalUiRequiredException;
import com.microsoft.graph.core.ClientException;

import com.microsoft.graph.http.IHttpRequest;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import okhttp3.Request;

public class MSALAuthenticationProvider implements ICoreAuthenticationProvider, IAuthenticationProvider {
    private final String TAG = this.getClass().getSimpleName();
    private IPublicClientApplication publicClientApplication;
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
                                      @NonNull IPublicClientApplication publicClientApplication,
                                      @NonNull String scopes[]) {
        this.publicClientApplication = publicClientApplication;
        this.scopes = scopes;
        this.application = application;
        this.callbacks = new Callback(activity);
        application.registerActivityLifecycleCallbacks(callbacks);
    }

    private class AuthorizationData{
        private IAuthenticationResult authenticationResult;
        private CountDownLatch latch;
        private ClientException clientException;
        public AuthorizationData(CountDownLatch latch){
            this.latch = latch;
        }
        public IAuthenticationResult getAuthenticationResult(){
            return authenticationResult;
        }
        public void setAuthenticationResult(IAuthenticationResult authenticationResult){
            this.authenticationResult = authenticationResult;
            this.getLatch().countDown();
        }
        public CountDownLatch getLatch(){
            return latch;
        }
        public void setClientException(ClientException clientException){
            this.clientException = clientException;
            this.getLatch().countDown();
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

    private void startAuthentication(final AuthorizationData authorizationData){
        if (publicClientApplication instanceof IMultipleAccountPublicClientApplication){
            final IMultipleAccountPublicClientApplication multipleAccountPCA = (IMultipleAccountPublicClientApplication) publicClientApplication;
            multipleAccountPCA.getAccounts(new IPublicClientApplication.LoadAccountsCallback() {
                @Override
                public void onTaskCompleted(List<IAccount> accounts) {
                    // Pick first account.
                    if (accounts != null && accounts.size() > 0) {
                        Log.d(TAG, "Trying to acquire token silently");
                        multipleAccountPCA. acquireTokenSilentAsync(scopes,
                                accounts.get(0),
                                Constants.DEFAULT_AUTHORITY,
                                getAuthSilentCallback(authorizationData));
                    } else {
                        getAccessTokenInteractiveAsync(authorizationData);
                    }
                }

                @Override
                public void onError(MsalException exception) {
                    authorizationData.setClientException(getClientExceptionFromMsalException(exception));
                }
            });
        } else if (publicClientApplication instanceof ISingleAccountPublicClientApplication) {
            final ISingleAccountPublicClientApplication singleAccountPCA = (ISingleAccountPublicClientApplication) publicClientApplication;
            singleAccountPCA.getCurrentAccountAsync(new ISingleAccountPublicClientApplication.CurrentAccountCallback() {
                @Override
                public void onAccountLoaded(@Nullable IAccount activeAccount) {
                    if (activeAccount != null) {
                        Log.d(TAG, "Trying to acquire token silently");
                        singleAccountPCA.acquireTokenSilentAsync(scopes,
                                Constants.DEFAULT_AUTHORITY,
                                getAuthSilentCallback(authorizationData));
                    } else {
                        getAccessTokenInteractiveAsync(authorizationData);
                    }
                }

                @Override
                public void onAccountChanged(@Nullable IAccount priorAccount, @Nullable IAccount currentAccount) {
                }

                @Override
                public void onError(@NonNull MsalException exception) {
                    authorizationData.setClientException(getClientExceptionFromMsalException(exception));
                }
            });
        }
    }

    private void getAccessTokenInteractiveAsync(AuthorizationData authorizationData){
        Log.d(TAG, "Acquiring token interactively");
        publicClientApplication.acquireToken(getCurrentActivity(), scopes, getAuthInteractiveCallback(authorizationData));
    }

    private Activity getCurrentActivity() {
        return this.callbacks.getActivity();
    }

    private ClientException getClientExceptionFromMsalException(final MsalException e){
        String message = e.getMessage();
        if (e instanceof MsalClientException) {
            message = "Exception inside MSAL" + e.getMessage();
        } else if (e instanceof MsalServiceException) {
            message = "Exception when communicating with the STS, likely config issue " + e.getMessage();
        }
        return new ClientException(message, e);
    }

    private AuthenticationCallback getAuthSilentCallback(final AuthorizationData authorizationData) {
        return new AuthenticationCallback() {
            @Override
            public void onSuccess(IAuthenticationResult authenticationResult) {
                Log.d(TAG, "Silent authentication successful");
                authorizationData.setAuthenticationResult(authenticationResult);
            }

            @Override
            public void onError(MsalException e) {
                Log.d(TAG, "Silent authentication error");
                if(e instanceof MsalUiRequiredException) {
                    getAccessTokenInteractiveAsync(authorizationData);
                } else {
                    authorizationData.setClientException(getClientExceptionFromMsalException(e));
                }
            }

            @Override
            public void onCancel() {
                ClientException clientException = new ClientException("User pressed cancel", new Exception("Cancelled acquiring token silently"));
                authorizationData.setClientException(clientException);
            }
        };
    }

    private AuthenticationCallback getAuthInteractiveCallback(final AuthorizationData authorizationData) {
        return new AuthenticationCallback() {
            @Override
            public void onSuccess(IAuthenticationResult authenticationResult) {
                Log.d(TAG, "Interactive authentication successful");
                authorizationData.setAuthenticationResult(authenticationResult);
            }

            @Override
            public void onError(MsalException e) {
                Log.d(TAG, "Interactive authentication error");
                authorizationData.setClientException(getClientExceptionFromMsalException(e));
            }

            @Override
            public void onCancel() {
                ClientException clientException = new ClientException("User pressed cancel", new Exception("Cancelled acquiring token interactively"));
                authorizationData.setClientException(clientException);
            }
        };
    }
}
