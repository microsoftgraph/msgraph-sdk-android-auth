package com.microsoft.graph;

import android.content.Intent;

import com.microsoft.graph.httpcore.IAuthenticationProvider;

public interface IMSALAuthenticationProvider extends IAuthenticationProvider {
    public void handleInteractiveRequestRedirect(int requestCode, int resultCode, Intent data);
}
