package com.concepttech.campingcompanionbluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.google.android.gms.nearby.messages.internal.Update;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmlpull.v1.XmlSerializer;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import static android.bluetooth.BluetoothDevice.ACTION_BOND_STATE_CHANGED;
import static com.concepttech.campingcompanionbluetooth.Constants.DeviceName;
import static com.concepttech.campingcompanionbluetooth.Constants.HomeFragmentLaunchConnection;
import static com.concepttech.campingcompanionbluetooth.Constants.HomeFragmentLaunchLights;
import static com.concepttech.campingcompanionbluetooth.Constants.HomeFragmentLaunchLocation;
import static com.concepttech.campingcompanionbluetooth.Constants.HomeFragmentLaunchLog;
import static com.concepttech.campingcompanionbluetooth.Constants.HomeFragmentLaunchStatus;
import static com.concepttech.campingcompanionbluetooth.Constants.LabelIndex;
import static com.concepttech.campingcompanionbluetooth.Constants.LightsFragmentBack;
import static com.concepttech.campingcompanionbluetooth.Constants.LightsFragmentChangeColor;
import static com.concepttech.campingcompanionbluetooth.Constants.isValidLabel;

public class Main extends FragmentActivity implements ConnectionFragment.ConnectionFragmentCallback,
                                            HomeFragment.HomeCallback,
                                            LightsFragment.LightsFragmentCallback,
                                            InitialSetUpFragment.InitialSetUpCallback,
                                            LocationFragment.MapFragmentCallBack,
                                            StatusFragment.StatusFragmentCallback{

    private DeviceState deviceState;
    int timeout =0;
    private static final int REQUEST_ENABLE_BT = 3;
    private boolean RTR1 = false , RTR2 = false, messagetypevalid = false , ResReq = false, BluetoothDeviceFound,StatusGood = false, InitialSetUpDone = false,
    LocationLoaded = false, StatusLoaded = false;
    private String mConnectedDeviceName = null, TAG = "MainActivity", DeviceMAC;
    private CustomBlueToothAdapter mCustomBluetoothAdapter = null;
    private BluetoothAdapter mBluetoothAdapter = null;
    private Context context;
    private Timer timer;
    private LightsFragment Lightsfragment;
    private HomeFragment Homefragment;
    private ConnectionFragment Connectionfragment;
    private LocationFragment Locationfragment;
    private StatusFragment Statusfragment;
    public void StatusFragmentCallback(){
        StatusLoaded = false;
        CancelTimer();
        LaunchHomeFragment();
    }
    public void MapFragmentCallBack(){
        LocationLoaded = false;
        CancelTimer();
        LaunchHomeFragment();
    }
    public void InitialSetUpCallback(){
        InitialSetUpDone = true;
        LaunchConnectionFragment();
    }
    public void LightsFragmentCallback(String request, DeviceState state){
        if(request != null && deviceState != null){
            deviceState = state;
            switch (request){
                case LightsFragmentChangeColor:
                    SendLightCommand();
                    break;
                case LightsFragmentBack:
                    LaunchHomeFragment();
                    break;
            }
        }
    }
    public void HomeCallback(String request){
        if(request != null && request.length() > 0) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            switch (request) {
                case HomeFragmentLaunchLights:
                    if(Lightsfragment == null) Lightsfragment = new LightsFragment();
                    Lightsfragment.SetState(deviceState, context);
                    transaction.replace(R.id.fragment_holder, Lightsfragment);
                    transaction.commit();
                    break;
                case HomeFragmentLaunchStatus:
                    StatusLoaded = true;
                    if(Statusfragment == null) Statusfragment = new StatusFragment();
                    Statusfragment.SetDeviceState(deviceState);
                    transaction.replace(R.id.fragment_holder, Statusfragment);
                    transaction.commit();
                    StartTimer();
                    break;
                case HomeFragmentLaunchLocation:
                    LocationLoaded = true;
                    if(Locationfragment == null) Locationfragment = new LocationFragment();
                    Locationfragment.SetDeviceState(deviceState);
                    transaction.replace(R.id.fragment_holder, Locationfragment);
                    transaction.commit();
                    StartTimer();
                    break;
                case HomeFragmentLaunchLog:
                    break;
                case HomeFragmentLaunchConnection:
                    LaunchConnectionFragment();
                    break;
            }
        }
    }
    public void ConnectionFragmentCallback(boolean ResultOk, final BluetoothDevice device){
        BluetoothDeviceFound = ResultOk;
        if(ResultOk){
            try {
                Method m = device.getClass().getMethod("createBond", (Class[]) null);
                m.invoke(device, (Object[]) null);
                DeviceMAC = device.getAddress();
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if(timeout < 10){
                            if(device.getBondState() == BluetoothDevice.BOND_BONDED && timeout > 5){
                                Log.d(TAG,"about to connect ");
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Connect(DeviceMAC);
                                        LaunchHomeFragment();
                                    }
                                });
                                timeout = 0;
                                this.cancel();
                            }
                        }else{
                            timeout = 0;
                            Log.d(TAG,"Timeout");
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(Main.this, "Connection Failed", Toast.LENGTH_LONG).show();
                                }
                            });
                            this.cancel();
                        }
                        timeout++;
                        Log.d(TAG,"Timer run: " + timeout);
                    }
                }, 1000,1000);
            }catch (Exception e) {
                Log.e("pairDevice()", e.getMessage());
            }
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = getApplicationContext();
        if (context != null) {
            Initialize();
        }
    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        if (mCustomBluetoothAdapter != null) {
            mCustomBluetoothAdapter.stop();
        }
        if(Connectionfragment != null) Connectionfragment.Cleanup();
        CancelTimer();
    }
    private void LaunchHomeFragment(){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if(Homefragment == null) Homefragment = new HomeFragment();
        Homefragment.SetParameters(deviceState);
        transaction.replace(R.id.fragment_holder, Homefragment);
        transaction.commit();
    }
    private void LaunchConnectionFragment(){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if(Connectionfragment == null) Connectionfragment = new ConnectionFragment();
        transaction.replace(R.id.fragment_holder, Connectionfragment);
        transaction.commit();
    }
    private void Initialize(){
        if(deviceState == null) deviceState = new DeviceState();
        CheckStatus();
        if(StatusGood) {
            SetUpBluetoothAdapter();
            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
                    if (device.getName().equals(DeviceName)) {
                        InitialSetUpDone = true;
                        Connect(device.getAddress());
                        LaunchHomeFragment();
                        return;
                    }
                }
            }
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            InitialSetUpFragment fragment = new InitialSetUpFragment();
            transaction.replace(R.id.fragment_holder, fragment);
            transaction.commit();

        }
    }
    private void CheckStatus(){
        if(!checkAccessCoarseLocationPermission()) {
            ActivityCompat.requestPermissions(Main.this,
                    new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    1);
        }else if(!checkBluetooth()){
            RequestBluetooth();
        }else{
            StatusGood = true;
        }
    }
    private void RequestBluetooth(){
        Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
    }
    private boolean checkBluetooth(){
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            return false;
        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                return false;
            }
        }
        return true;
    }
    private boolean checkAccessCoarseLocationPermission(){
        String permission = android.Manifest.permission.ACCESS_COARSE_LOCATION;
        int res = checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }
    private void Connect(String deviceMAC){
        if(deviceMAC != null && deviceMAC.length() > 0){
            DeviceMAC = deviceMAC;
            BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(DeviceMAC);
            // Attempt to connect to the device
            mCustomBluetoothAdapter.connect(device, false);
            if (deviceState == null) deviceState = new DeviceState();
        }
    }
    private void UpdateFragmentState(){
        if(LocationLoaded) Locationfragment.SetDeviceState(deviceState);
        else if(StatusLoaded) Statusfragment.SetDeviceState(deviceState);
    }
    private void StartTimer(){
        if(timer == null) {
            timer = new Timer();
            timer.schedule( new TimerTask() {
                @Override
                public void run() {
                    RequestData();
                }
            }, 0, 5000);
        }
    }
    private void CancelTimer(){
        if(timer != null){
            timer.cancel();
            timer.purge();
            timer = null;
        }
    }
    private void SetUpBluetoothAdapter(){
        if(mBluetoothAdapter == null){
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            mCustomBluetoothAdapter = new CustomBlueToothAdapter(Main.this,mHandler);
            if (mBluetoothAdapter == null) {
                Toast.makeText(Main.this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }
    private void UpdateValues(){
    }
    private void ensureDiscoverable() {
        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }
    private void Write(String message){
        if(mCustomBluetoothAdapter!=null){
            if(mCustomBluetoothAdapter.getState() != CustomBlueToothAdapter.STATE_CONNECTED || message==null){
                //state is not connected do not allow write
                return;
            }
            if(message.length() > 0){
                mCustomBluetoothAdapter.write(message.getBytes());
            }
        }else{
            Log.d(TAG,"mCustomBluetoothAdapter is null, cannot write");
        }
    }
    private void HandleRecievedMessage(String message){
        //if(mCustomBluetoothAdapter.getState() == CustomBlueToothAdapter.STATE_CONNECTED){
        int messagetype = GetMessageType(message);
        Log.d(TAG,Constants.MESSAGETYPE + messagetype);
        messagetypevalid = true;

        switch(messagetype){
            case Constants.INIT:
                InitMessage(message);
                break;
            case Constants.UPADATEDATAPHONE2HUB:
                //should never reach this as a recieved message on the phone
                UpdateFromPhoneMessage(message);
                break;
            case Constants.UPDATEDATAHUB2PHONE:
                UpdateFromHubMessage(message);
                break;
            case Constants.COMMAND:
                //should never reach this as a recieved message on the phone
                CommandMessage(message);
                break;
            case Constants.SOCIALMESSAGE:
                //should never reach this as a recieved message on the phone
                SocialMessage(message);
                break;
            case Constants.COMMANDCONFIRMATION:
                CommandConfirmMessage(message);
                break;
            case Constants.RESENDREQUEST:
                ResendRequest(message);
                break;
            case Constants.RESENDREQUESTCONFIRMATION:
                ResendRequestConfirmation(message);
                break;
            case Constants.GENERALCONFIRMATION:
                GeneralConfirmationMessage(message);
                break;
            case Constants.MESSAGETYPERETURNERROR:
                //bad return value from getmessagetype, handle here
                break;
            default:
                //invalid value for message type TODO: need to determine what to do in this case
                messagetypevalid = false;
                break;
        }
        //}
    }
    //this function is called when the message recieved has the INIT value type, it first checks to verify if init has already been done
    //if it has then a user interaction will be required to confirm the new state that will be created
    private void InitMessage(String message){
        if(mCustomBluetoothAdapter.getState() == CustomBlueToothAdapter.STATE_CONNECTED){
            if(deviceState != null){
                //TODO: use handler to prompt user about new Init Request, need to make this secure so no one can fake a hub and connect to the phone

            }else{
                //this must be a new connection if deviceState hasnt been set.

            }
        }
    }
    //this function should never be called on the phone if reached and called it will not produce errors but something needs to be done because data is bad
    //or hub is not a hub
    private void UpdateFromPhoneMessage(String message){

    }
    //this function is called when phone recieves message to update data like temp or gps
    private void UpdateFromHubMessage(String message){
        Log.d(TAG,"UpdateFromHubMessage");
        if(message != null){
            if(message.length() > 0){
                int numArgs = GetNumArgs(message);
                int temp = numArgs;
                int bindex = 0,lindex = 0,eindex = 0;
                String tempmessage = "nothing...yet";
                boolean end_reached = false;
                do{
                    if(temp == numArgs){
                        if(message.contains(Constants.LABELDATASEP) && message.contains(Constants.BODYBEGIN) && message.contains(Constants.DATAEND)){
                            bindex = message.indexOf(Constants.BODYEND);
                            bindex = message.indexOf(Constants.BODYBEGIN, bindex);
                            lindex = message.indexOf(Constants.LABELDATASEP, bindex);
                            eindex = message.indexOf(Constants.DATAEND, bindex);
                        }
                    }else{
                        if(tempmessage.contains(Constants.LABELDATASEP) && tempmessage.contains(Constants.DATAEND)){
                            bindex = 0;
                            lindex = tempmessage.indexOf(Constants.LABELDATASEP);
                            eindex = tempmessage.indexOf(Constants.DATAEND);
                        }
                    }
                    if(lindex > bindex && eindex > lindex){
                        String label,labeldata;
                        if(numArgs == temp){
                            label = message.substring(bindex + 1,lindex);
                            labeldata = message.substring(lindex + 1, eindex);
                        }else{
                            label = tempmessage.substring(bindex,lindex);
                            labeldata = tempmessage.substring(lindex + 1, eindex);
                        }
                        Log.d(TAG,"LABEL : " + label + " BODY: " + labeldata);
                        if(label.length() > 0) {
                            int labelindex = LabelIndex(label);
                            switch (labelindex){
                                case 0:
                                    Log.d(TAG,"TEMPERATURE LABEL");
                                    if(isNumericDouble(labeldata)){
                                        Log.d(TAG,"SETTING TEMPERATURE");
                                        deviceState.setTemperature(Double.parseDouble(labeldata));
                                    }
                                    break;
                                case 1:
                                    Log.d(TAG,"Humidity LABEL");
                                    if(isNumericDouble(labeldata)){
                                        Log.d(TAG,"SETTING Humidity");
                                        deviceState.setHumidity(Double.parseDouble(labeldata));
                                    }
                                    break;
                                case 2:
                                    Log.d(TAG,"Barometricpressure LABEL");
                                    if(isNumericDouble(labeldata)){
                                        Log.d(TAG,"SETTING Barometricpressure");
                                        deviceState.setBarometricpressure(Double.parseDouble(labeldata));
                                    }
                                    break;
                                case 10:
                                    Log.d(TAG,"Latitude LABEL");
                                    if(isNumericDouble(labeldata)){
                                        Log.d(TAG,"SETTING Latitude");
                                        deviceState.setLatitude(Double.parseDouble(labeldata));
                                    }
                                    break;
                                case 11:
                                    Log.d(TAG,"Longitude LABEL");
                                    if(isNumericDouble(labeldata)){
                                        Log.d(TAG,"SETTING Longitude");
                                        deviceState.setLongitude(Double.parseDouble(labeldata));
                                    }
                                    break;

                            }
                        }
                    }
                    if(eindex + 1 < message.length()){
                        if(numArgs == temp) tempmessage = message.substring(eindex + 1,message.length());
                        else tempmessage = tempmessage.substring(eindex + 1,tempmessage.length());
                    }
                    else {
                        end_reached = true;
                    }
                    Log.d(TAG , tempmessage);
                    temp--;
                }while(temp>0 && !end_reached);
            }
            UpdateValues();
        }
    }
    //this finction is called when a command is recieved, whichi should be nver on a pone so TODO: make code for command sent by hub
    private void CommandMessage(String message){/*
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(multiLangTranslation(R.string.manualshippermessage));
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setRawInputType(Configuration.KEYBOARD_12KEY);
        alert.setView(input);
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //Put actions for OK button here
            }
        });
        alert.setNegativeButton(multiLangTranslation(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //Put actions for CANCEL button here, or leave in blank
            }
        });
        alert.show();*/
    }
    //this function is called when hubn sends a confirmation of the command, check data sent to match command and resend command if necessary
    private void CommandConfirmMessage(String message){

    }
    //this function is called after the confirmation from any message sent to the hub is recieved caompare data to update and resend if necessary
    private void GeneralConfirmationMessage(String message){

    }
    //this function handles social messages sent to user, if broadcast is not enabled then do not do anything with it
    private void SocialMessage(String message){

    }
    //this function handles requests to resend data from the hub
    private void ResendRequest(String message){

    }
    //this function handles a confirmation to resend from the hub
    private void ResendRequestConfirmation(String message){

    }
    private void SendLightCommand(){
        String command = BuildHeader(Constants.COMMAND, 1) + BuildBody(Constants.LIGHTCOMMANDLABELS);
        Log.d(TAG, command);
        Write(command);
        //TODO: pass command to write function after checking validity
    }
    private void RequestData(){
        String command = BuildHeader(Constants.MESSAGE_READ, 1) + BuildBody(Constants.LIGHTCOMMANDLABELS);
        Log.d(TAG, command);
        Write(command);
        //TODO: pass command to write function after checking validity
    }
    private String BuildHeader(int messagetype, int numargs){
        if(messagetype >= 0 && messagetype <= Constants.MESSAGETYPEMAXVALUE && numargs > 0){
            if(messagetype >= 0 && messagetype <= Constants.MESSAGETYPEMAXVALUE){
                return Constants.BODYBEGIN + Constants.MTYPE + Constants.LABELDATASEP + messagetype +
                        Constants.DATAEND + Constants.MARGS + Constants.LABELDATASEP + numargs +
                        Constants.DATAEND + Constants.BODYEND;
            }
        }
        return "Error";
    }
    private String BuildBody(String[] labels){
        String body = "Error";
        if(labels != null){
            if(labels.length > 0){
                int i;
                body = Constants.BODYBEGIN;
                for(i = 0; i < labels.length; i++){
                    if(labels[i] != null){
                        if(labels[i].length() > 0 && isValidLabel(labels[i])){
                            body += labels[i] + Constants.LABELDATASEP;
                            int index = LabelIndex(labels[i]);
                            switch(index){
                                case 0:
                                    break;
                                case 1:
                                    break;
                                case 2:
                                    break;
                                case 3:
                                    break;
                                case 4:
                                    //lightstatus
                                    Log.d(TAG, "Lightstatus Body Builder");
                                    String lightstatusdata = deviceState.getLightStatusDataString();
                                    Log.d(TAG, "Lightstatus: " + lightstatusdata);
                                    if(lightstatusdata.length() > 0){
                                        if(!lightstatusdata.equals("Error")){
                                            body += lightstatusdata + Constants.DATAEND;
                                            Log.d(TAG, "body: " + body);
                                        }//TODO: handle error with else here
                                    }
                                    break;
                                case 5:
                                    break;
                                case 6:
                                    break;
                                case 7:
                                    //LIGHTSCOLORS
                                    Log.d(TAG, "LIGHTSCOLORS Body Builder");
                                    String lightcolors = deviceState.getLightColorDataString();
                                    Log.d(TAG, "Lightcolors: " + lightcolors);
                                    if(lightcolors.length() > 0){
                                        if(!lightcolors.equals("Error")){
                                            body += lightcolors + Constants.DATAEND;
                                            Log.d(TAG, "body: " + body);
                                        }//TODO: handle error with else here
                                    }
                                    break;
                                case 8:
                                    break;
                                case 9:
                                    break;
                                case 10:
                                    break;
                                case 11:
                                    break;
                                case 12:
                                    break;
                                case 13:
                                    break;
                                case 14:
                                    break;
                                case 15:
                                    break;
                                case 16:
                                    break;
                                case 17:
                                    break;
                                case 18:
                                    break;
                                case 19:
                                    break;
                                case 20:
                                    break;
                                case 21:
                                    break;
                                case 22:
                                    break;
                                case 23:
                                    break;
                                case 24:
                                    break;
                                case 25:
                                    break;
                                case 26:
                                    break;
                                case -1:
                                    //TODO: Error code here, fix
                                    break;
                            }
                        }
                    }
                }
                body += Constants.BODYEND;
            }
        }
        Log.d(TAG, "body: " + body);
        return body;
    }
    //this function checks argument to make sure its only numbers, also checks null and empty and length(defined in constants)
    private boolean isNumericInt(String string){
        if(string != null){
            if(string.length()>0 && string.length() == Constants.MAXALLOWEDTYPEDIGITS){
                return string.matches(Constants.INTNUMERICREGEX);
            }
        }
        return false;
    }
    private boolean isNumericDouble(String string){
        if(string != null){
            if(string.length()>0){
                return string.matches(Constants.DOUBLENUMERICREGEX);
            }
        }
        return false;
    }
    private int GetNumArgs(String message){
        if(message != null){
            if(message.length() > 0){

                //display message for debugging  after we are sure its not null or empty
                Log.d(TAG,Constants.MESS + message);

                if(message.contains(Constants.MARGS)){
                    //after initial check determine what to do

                    int Bindex, Eindex, Cindex;
                    Bindex = message.indexOf(Constants.MARGS);
                    Bindex = message.indexOf(Constants.LABELDATASEP , Bindex);
                    Eindex = message.indexOf(Constants.DATAEND , Bindex);

                    //after creating the indexes to get the message type make sure the indexes are not -1 and that the end is after the beginning

                    if(Bindex != -1 && Eindex != -1){
                        if(Eindex > Bindex){

                            String numargsstring =message.substring(Bindex+1 , Eindex);

                            if(isNumericInt(numargsstring)){
                                return Integer.parseInt(numargsstring);
                            }else{
                                RTR1 = true;
                                RequestResend(Constants.MESSAGEARGSNOTNUMORLONG);
                            }
                        }else{
                            RTR1 = true;
                            RequestResend(Constants.MESSAGENUMARGSEMPTY);
                        }
                    }else{
                        //message must have bad data because no colon was found
                        RTR1 = true;
                        RequestResend(Constants.NOCOLERR);
                    }

                }else{
                    //message did not contain the required messagetype or args

                    RTR1 = true;
                    RequestResend(Constants.TYPEARGERR);
                }
            }else{
                //message was empty if this is reached
                RTR1 = true;
                RequestResend(Constants.MESSAGEEMPTYERR);
            }
        }else{
            //message was null if this is reached
            RTR1 = true;
            RequestResend(Constants.MESSAGENULLERR);
        }
        return -1;
    }
    private int GetMessageType(String message){

        //check if message is not null, empty and contains both the type and the number of arguments
        if(message != null){
            if(message.length() > 0){

                //display message for debugging  after we are sure its not null or empty
                Log.d(TAG,Constants.MESS + message);

                if(message.contains(Constants.MTYPE) && message.contains(Constants.MARGS)){
                    //after initial check determine what to do

                    int Bindex, Eindex, Cindex;
                    Bindex = message.indexOf(Constants.MTYPE);
                    Bindex = message.indexOf(Constants.LABELDATASEP , Bindex);
                    Eindex = message.indexOf(Constants.DATAEND , Bindex);

                    //after creating the indexes to get the message type make sure the indexes are not -1 and that the end is after the beginning

                    if(Bindex != -1 && Eindex != -1){
                        if(Eindex > Bindex){

                            String typestring =message.substring(Bindex+1 , Eindex);

                            if(isNumericInt(typestring)){
                                return Integer.parseInt(typestring);
                            }else{
                                RTR1 = true;
                                RequestResend(Constants.MESSAGETYPENOTNUMORLONG);
                            }
                        }else{
                            RTR1 = true;
                            RequestResend(Constants.MESSAGETYPEEMPTY);
                        }
                    }else{
                        //message must have bad data because no colon was found
                        RTR1 = true;
                        RequestResend(Constants.NOCOLERR);
                    }

                }else{
                    //message did not contain the required messagetype or args

                    RTR1 = true;
                    RequestResend(Constants.TYPEARGERR);
                }
            }else{
                //message was empty if this is reached
                RTR1 = true;
                RequestResend(Constants.MESSAGEEMPTYERR);
            }
        }else{
            //message was null if this is reached
            RTR1 = true;
            RequestResend(Constants.MESSAGENULLERR);
        }
        return -1;

    }
    //function to handle errors, mostly to reduce code, takes the log message to post as an argument
    //and will always check the argument to be valid, also requires a flag RTR1 to be set in order
    //to proceed with request
    private void RequestResend(String messagetopost){
        if(messagetopost!=null){
            if(messagetopost.length()>0){
                if(RTR1&&!RTR2){
                    Log.d(TAG,Constants.ERR + messagetopost);

                    //in order to actually request the resend the flag (RTR2) is set here then checked in the overloaded function RequestResend
                    //so that it cannot be called elsewhere and work

                    RTR1 = false;
                    RTR2 = true;
                    RequestResend();
                    Log.d(TAG,Constants.RESENDLOGMESS);
                }else{
                    Log.d(TAG,Constants.ERR + Constants.RTRERR);
                }
            }else{
                Log.d(TAG,Constants.ERR + Constants.MESSTOPOSTEMPTY);
            }
        }else{
            Log.d(TAG,Constants.ERR + Constants.MESSTOPOSTNULL);
        }
    }
    //called from overloaded form after log message is posted and flags rotated, creates string to send, saves it and then sets a flag to look for a specific confirmation
    private void RequestResend(){
        if(!RTR1&&RTR2){
            RTR2 = false;
            ResReq = true;
            //build request string and send
        }else{
            Log.d(TAG,Constants.ERR + Constants.RTRERR);
        }
    }
    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                Log.d(TAG,"REQUEST_ENABLE_BT");
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    Log.d(TAG,"RESULT_OK");
                    // Bluetooth is now enabled, so set up a chat session
                    Initialize();
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(Main.this, R.string.bt_not_enabled_leaving,
                            Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }
    }
    private final Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case CustomBlueToothAdapter.STATE_CONNECTED:
                            //setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));
                            break;
                        case CustomBlueToothAdapter.STATE_CONNECTING:
                            //setStatus(R.string.title_connecting);
                            break;
                        case CustomBlueToothAdapter.STATE_LISTEN:
                        case CustomBlueToothAdapter.STATE_NONE:
                            //setStatus(R.string.title_not_connected);
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    //this should match message just sent, log it here too
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    HandleRecievedMessage(readMessage);
                    if(StatusLoaded || LocationLoaded) UpdateFragmentState();
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    if (null != Main.this) {
                        Toast.makeText(Main.this, "Connected to "
                                + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case Constants.MESSAGE_TOAST:
                    if (null != Main.this) {
                        Toast.makeText(Main.this, msg.getData().getString(Constants.TOAST),
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
            return true;
        }
    });
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(!checkBluetooth()){
                        RequestBluetooth();
                    }else{
                        Initialize();
                    }
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(Main.this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
                    finish();
                }
                return;
            }
        }
    }
}
