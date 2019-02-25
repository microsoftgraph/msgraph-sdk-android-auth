package com.microsoft.graph.authentication;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import com.microsoft.identity.client.PublicClientApplication;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import com.microsoft.identity.client.AuthenticationCallback;
import com.microsoft.identity.client.AuthenticationResult;
import com.microsoft.identity.client.exception.MsalClientException;
import com.microsoft.identity.client.exception.MsalException;
import com.microsoft.identity.client.exception.MsalServiceException;
import com.microsoft.identity.client.exception.MsalUiRequiredException;

import static org.junit.Assert.*;
import com.microsoft.graph.http.IHttpRequest;

@Ignore
public class MSALAuthenticationProviderTest {

    private final String SCOPES[] = {"https://graph.microsoft.com/User.Read"};
    private Activity activity;
    private Application application;
    private Context context;
    private PublicClientApplication publicClientApplication;

    @Before
    public void initTest(){
        activity = Mockito.mock(Activity.class);
        application = Mockito.mock(Application.class);
        context = Mockito.mock(Context.class);
        publicClientApplication = new PublicClientApplication(context, "CLIENT_ID");

    }

    @Test
    public void initializationtest(){
        MSALAuthenticationProvider msalAuthenticationProvider = new MSALAuthenticationProvider(activity,
                application,
                publicClientApplication,
                SCOPES);
        assertNotNull(msalAuthenticationProvider);
    }

    @Test
    public void authenticateSilently() {
        final IHttpRequest httpRequest = Mockito.mock(IHttpRequest.class);

        MSALAuthenticationProvider msalAuthenticationProvider = new MSALAuthenticationProvider(activity,
                application,
                publicClientApplication,
                SCOPES);

        publicClientApplication.acquireToken(activity, SCOPES, new AuthenticationCallback() {
            @Override
            public void onSuccess(AuthenticationResult authenticationResult) {
                httpRequest.addHeader("Authorization", "Bearer " + authenticationResult.getAccessToken());
            }

            @Override
            public void onError(MsalException exception) {
                exception.printStackTrace();
            }

            @Override
            public void onCancel() {
            }
        });
        msalAuthenticationProvider.authenticateRequest(httpRequest);
    }

    @Test
    public void handleInteractiveRequestRedirect() {
        IHttpRequest httpRequest = Mockito.mock(IHttpRequest.class);

        publicClientApplication = new PublicClientApplication(context, "CLIENT_ID");
        MSALAuthenticationProvider msalAuthenticationProvider = new MSALAuthenticationProvider(activity,
                application,
                publicClientApplication,
                SCOPES);
        msalAuthenticationProvider.authenticateRequest(httpRequest);
    }
}