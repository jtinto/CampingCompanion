package com.concepttech.campingcompanionbluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;

public class Main extends FragmentActivity implements BluetoothController.OnFragmentInteractionListener{

    private DeviceState deviceState;
    public void onFragmentInteraction(Uri uri){

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            deviceState = new DeviceState();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            BluetoothController fragment = new BluetoothController();
            fragment.SetParameters(deviceState);
            transaction.replace(R.id.fragment_holder, fragment);
            transaction.commit();
        }
    }
}
