package com.bardia.pocr.methods;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;

import java.util.Random;
import java.util.UUID;

public class Methods {
    private static final int MAX_LENGTH = 60;

    public static boolean checkConnectivity(Context context) {
        ConnectivityManager connMgr =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean isWifiConn = false;
        boolean isMobileConn = false;
        for (Network network : connMgr.getAllNetworks()) {
            NetworkInfo networkInfo = connMgr.getNetworkInfo(network);
            if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                isWifiConn |= networkInfo.isConnected();
            }
            if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                isMobileConn |= networkInfo.isConnected();
            }
        }
        if(isWifiConn || isMobileConn) {
            return true;
        } else {
            return false;
        }
    }

    public static String randomKey() {
        String key = UUID.randomUUID().toString();
        return key;
    }
}
