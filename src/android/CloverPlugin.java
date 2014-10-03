package com.termtegrity.spotskim.cloverplugin;
 
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.content.Intent;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.clover.sdk.util.CloverAccount;
import com.clover.sdk.v1.ResultStatus;
import com.clover.sdk.v1.ServiceConnector;
import com.clover.sdk.v1.merchant.Merchant;
import com.clover.sdk.v1.merchant.MerchantAddress;
import com.clover.sdk.v1.merchant.MerchantConnector;
import com.clover.sdk.v1.merchant.MerchantIntent;

import java.text.DateFormat;
import java.util.Date;

public class CloverPlugin extends CordovaPlugin {
    public static final String ACTION_GET_MERCHANT = "getMerchant";
    
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        try {
            if (ACTION_GET_MERCHANT.equals(action)) {
                JSONObject arg_object = args.getJSONObject(0);
                startAccountChooser();
                connect();
                getMerchant();
               callbackContext.success(merchantID);
               return true;
            }
            callbackContext.error("Invalid action");
            return false;
        } catch(Exception e) {
            System.err.println("Exception: " + e.getMessage());
            callbackContext.error(e.getMessage());
            return false;
        } 
    }

    private static final int REQUEST_ACCOUNT = 0;
    private MerchantConnector merchantConnector;
    private Account account;
    private String merchantID;
    private String deviceID;



    private void startAccountChooser() {
        Intent intent = AccountManager.newChooseAccountIntent(null, null, new String[]{CloverAccount.CLOVER_ACCOUNT_TYPE}, false, null, null, null, null);
        startActivityForResult(intent, REQUEST_ACCOUNT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ACCOUNT) {
            if (resultCode == RESULT_OK) {
                String name = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                String type = data.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE);
                account = new Account(name, type);
            } else {
                if (account == null) {
                    finish();
                }
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        if (account != null) {
            connect();
            getMerchant();
        } else {
            startAccountChooser();
        }
    }


    @Override
    protected void onPause() {
        disconnect();
        super.onPause();
    }

    private void connect() {
        disconnect();
        if (account != null) {
            merchantConnector = new MerchantConnector(this, account, this);
            //merchantConnector.setOnMerchantChangedListener(this);
            merchantConnector.connect();
        }
    }

    private void disconnect() {
        if (merchantConnector != null) {
            merchantConnector.disconnect();
            merchantConnector = null;
        }
    }

    @Override
    protected void onDestroy() {
        //unregisterReceiver(merchantChangedReceiver);
        super.onDestroy();
    }

    private void getMerchant() {
        merchantConnector.getMerchant(new MerchantConnector.MerchantCallback<Merchant>() {
            @Override
            public void onServiceSuccess(Merchant result, ResultStatus status) {
                super.onServiceSuccess(result, status);

                updateMerchant("get merchant success", status, result);

            }

            @Override
            public void onServiceFailure(ResultStatus status) {
                super.onServiceFailure(status);
                updateMerchant("get merchant failure", status, null);
            }

            @Override
            public void onServiceConnectionFailure() {
                super.onServiceConnectionFailure();
                updateMerchant("get merchant bind failure", null, null);
            }
        });
    }

    private void updateMerchant(String status, ResultStatus resultStatus, Merchant result) {
        merchantID = result.getMid();
        deviceID = result.getDeviceId();
    }
}