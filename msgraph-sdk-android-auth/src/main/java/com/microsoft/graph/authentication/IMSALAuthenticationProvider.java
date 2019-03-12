package com.microsoft.graph.authentication;

import android.content.Intent;
import com.microsoft.graph.authentication.IAuthenticationProvider;
import com.microsoft.graph.httpcore.ICoreAuthenticationProvider;


public interface IMSALAuthenticationProvider extends IAuthenticationProvider , ICoreAuthenticationProvider {
    public void handleInteractiveRequestRedirect(int requestCode, int resultCode, Intent data);
}
