package com.negocio.libromayor;

import android.os.Bundle;

import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // Los plugins propios deben registrarse ANTES de super.onCreate().
        registerPlugin(FileSaverPlugin.class);
        super.onCreate(savedInstanceState);
    }
}
