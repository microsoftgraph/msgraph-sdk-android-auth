package com.microsoft.graph.authentication;

import android.content.Intent;
import com.microsoft.graph.authentication.IAuthenticationProvider;


public interface IMSALAuthenticationProvider extends IAuthenticationProvider {
    public void handleInteractiveRequestRedirect(int requestCode, int resultCode, Intent data);
}
