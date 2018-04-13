package com.concepttech.campingcompanionbluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import static com.concepttech.campingcompanionbluetooth.Constants.LabelIndex;
import static com.concepttech.campingcompanionbluetooth.Constants.isValidLabel;


/**
 * this class is created by main, and interacts with the CustomBluetoothAdapter
 * class to send and receive data from the pi
 */
public class BluetoothController extends Fragment implements SeekBar.OnSeekBarChangeListener,View.OnClickListener{

    private final static String TAG = "BluetoothController:";
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;
    private boolean RTR1 = false , RTR2 = false, messagetypevalid = false , ResReq = false;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private TextView TemperatureTextView,HumididtyTextView,BarometricTextView,LatitudeTextView,LongitudeTextView,ColorPreviewTextView;
    private SeekBar RedSeekBar,BlueSeekBar,GreenSeekBar;
    private Button SendTurnLightOffCommandButton,SendTurnLightOnCommandButton,TestUpdateStringButton, MakeDiscoverableButton, PairDeviceButton;
    private int redprogress,greenprogress,blueprogress,redcheckprogress,greencheckprogress,bluecheckprogress;
    private boolean colorchanged;
    private OnFragmentInteractionListener mListener;

    /**
     * Name of the connected device
     */
    private String mConnectedDeviceName = null;

    /**
     * Array adapter for the conversation thread
     */
    /**
     * String buffer for outgoing messages
     */
    private StringBuffer mOutStringBuffer;
    /**
     * Local Bluetooth adapter
     */
    private BluetoothAdapter mBluetoothAdapter = null;
    /**
     * Member object for the chat services
     */
    private DeviceState CurrentState;
    private CustomBlueToothAdapter mCustomBluetoothAdapter = null;
    public BluetoothController() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment BluetoothController.
     */
    // TODO: Rename and change types and number of parameters
    public static BluetoothController newInstance(String param1, String param2) {
        BluetoothController fragment = new BluetoothController();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public void onClick(View view){
        switch (view.getId()){
            case R.id.sendTurnlightOncommandbutton:
                CurrentState.TurnLightOn(new int[]{0});
                SendLightCommand();
                break;
            case R.id.sendTurnlightOffcommandbutton:
                CurrentState.TurnLightOff(new int[]{0});
                SendLightCommand();
                break;
            case R.id.TestUpdateStringButton:
                //HandleRecievedMessage("<messageType:2;numArgs:5;/><TEMP:65.5;HUMIDITY:55.5;BAROPRESS:665.5;LATITUDE:35.5223423;LONGITUDE:65.2342345;/>");
                RequestGeoData();
                break;
            case R.id.MakeDiscoverableButton:
                ensureDiscoverable();
                break;
            case R.id.PairDeviceButton:
                Intent serverIntent = new Intent(getActivity(), DeviceListActivity.class);
                startActivityForResult(serverIntent,REQUEST_CONNECT_DEVICE_INSECURE);
                break;
        }
    }
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        switch(seekBar.getId()){
            case R.id.redseekbar:
                redprogress = progress;
                break;
            case R.id.blueseekbar:
                blueprogress = progress;
                break;
            case R.id.greenseekbar:
                greenprogress = progress;
                break;
        }
        setColorPreviewTextView();
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        switch(seekBar.getId()){
            case R.id.redseekbar:
                break;
            case R.id.blueseekbar:
                break;
            case R.id.greenseekbar:
                break;
        }
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        Log.d(TAG,"redseekprogress: " + redprogress);
        Log.d(TAG,"greenseekprogress: " + greenprogress);
        Log.d(TAG,"blueseekprogress: " + blueprogress);
        CurrentState.setLightcolors(0,redprogress,greenprogress,blueprogress);
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            FragmentActivity activity = getActivity();
            Toast.makeText(activity, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            activity.finish();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_bluetooth_controller, container, false);
        InitializeViews(v);
        // Inflate the layout for this fragment
        return v;

    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }
    @Override
    public void onStart() {
        super.onStart();
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else if (mCustomBluetoothAdapter == null) {
            //need resume handling here
            setup();
        }
    }
    @Override
    public void onResume() {
        super.onResume();

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mCustomBluetoothAdapter != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mCustomBluetoothAdapter.getState() == CustomBlueToothAdapter.STATE_NONE) {
                // Start the Bluetooth chat services
                mCustomBluetoothAdapter.start();
            }
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mCustomBluetoothAdapter != null) {
            mCustomBluetoothAdapter.stop();
        }
    }

    public void SetParameters(DeviceState state){
        if(state != null)
        CurrentState = state;
    }
    private void InitializeViews(View view){
        RedSeekBar = view.findViewById(R.id.redseekbar);
        GreenSeekBar = view.findViewById(R.id.greenseekbar);
        BlueSeekBar = view.findViewById(R.id.blueseekbar);
        SendTurnLightOnCommandButton = view.findViewById(R.id.sendTurnlightOncommandbutton);
        SendTurnLightOffCommandButton = view.findViewById(R.id.sendTurnlightOffcommandbutton);
        TemperatureTextView = view.findViewById(R.id.temperaturetextview);
        HumididtyTextView = view.findViewById(R.id.HumidityTextview);
        BarometricTextView = view.findViewById(R.id.BarometricTextview);
        LatitudeTextView = view.findViewById(R.id.LatitudeTextview);
        LongitudeTextView = view.findViewById(R.id.LongtitudeTextview);
        ColorPreviewTextView = view.findViewById(R.id.colorpreview);
        TestUpdateStringButton = view.findViewById(R.id.TestUpdateStringButton);
        MakeDiscoverableButton = view.findViewById(R.id.MakeDiscoverableButton);
        PairDeviceButton = view.findViewById(R.id.PairDeviceButton);
        RedSeekBar.setMax(256);
        GreenSeekBar.setMax(256);
        BlueSeekBar.setMax(256);
        RedSeekBar.setOnSeekBarChangeListener(this);
        GreenSeekBar.setOnSeekBarChangeListener(this);
        BlueSeekBar.setOnSeekBarChangeListener(this);
        SendTurnLightOnCommandButton.setOnClickListener(this);
        SendTurnLightOffCommandButton.setOnClickListener(this);
        MakeDiscoverableButton.setOnClickListener(this);
        PairDeviceButton.setOnClickListener(this);
        TestUpdateStringButton.setOnClickListener(this);
        CurrentState = new DeviceState();
    }
    private void setColorPreviewTextView(){
        if(ColorPreviewTextView != null){
            if(redprogress == 0 && greenprogress == 0 && blueprogress == 0) {
                ColorPreviewTextView.setBackgroundColor(Color.WHITE);
            }else{
                ColorPreviewTextView.setBackgroundColor(Color.rgb(redprogress, greenprogress, blueprogress));
            }
        }
    }
    private void UpdateValues(){
        if(TemperatureTextView != null && HumididtyTextView != null && BarometricTextView != null && LatitudeTextView != null && LongitudeTextView != null){
            TemperatureTextView.setText(Double.toString(CurrentState.getTemperature()) + " deg F");
            HumididtyTextView.setText(Double.toString(CurrentState.getHumidity())+ "%");
            BarometricTextView.setText(Double.toString(CurrentState.getBarometricpressure())+ " hPa");
            LatitudeTextView.setText(Double.toString(CurrentState.getLatitude())+ " deg");
            LongitudeTextView.setText(Double.toString(CurrentState.getLongitude())+ " deg");
        }
    }
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                Log.d(TAG,"REQUEST_CONNECT_DEVICE_SECURE");
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    Log.d(TAG,"RESULT_OK");
                    connectDevice(data, true);
                }
                break;
            case REQUEST_CONNECT_DEVICE_INSECURE:
                Log.d(TAG,"REQUEST_CONNECT_DEVICE_INSECURE");
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    Log.d(TAG,"RESULT_OK");
                    connectDevice(data, false);
                }
                break;
            case REQUEST_ENABLE_BT:
                Log.d(TAG,"REQUEST_ENABLE_BT");
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    Log.d(TAG,"RESULT_OK");
                    // Bluetooth is now enabled, so set up a chat session
                    setup();
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(getActivity(), R.string.bt_not_enabled_leaving,
                            Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                }
        }
    }
    private void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
        String address = data.getExtras()
                .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mCustomBluetoothAdapter.connect(device, secure);
    }
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
    private void setup(){
        mCustomBluetoothAdapter = new CustomBlueToothAdapter(getActivity(),mHandler);
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
            if(CurrentState != null){
                //TODO: use handler to prompt user about new Init Request, need to make this secure so no one can fake a hub and connect to the phone

            }else{
                //this must be a new connection if CurrentState hasnt been set.

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
                                        CurrentState.setTemperature(Double.parseDouble(labeldata));
                                    }
                                    break;
                                case 1:
                                    Log.d(TAG,"Humidity LABEL");
                                    if(isNumericDouble(labeldata)){
                                        Log.d(TAG,"SETTING Humidity");
                                        CurrentState.setHumidity(Double.parseDouble(labeldata));
                                    }
                                    break;
                                case 2:
                                    Log.d(TAG,"Barometricpressure LABEL");
                                    if(isNumericDouble(labeldata)){
                                        Log.d(TAG,"SETTING Barometricpressure");
                                        CurrentState.setBarometricpressure(Double.parseDouble(labeldata));
                                    }
                                    break;
                                case 10:
                                    Log.d(TAG,"Latitude LABEL");
                                    if(isNumericDouble(labeldata)){
                                        Log.d(TAG,"SETTING Latitude");
                                        CurrentState.setLatitude(Double.parseDouble(labeldata));
                                    }
                                    break;
                                case 11:
                                    Log.d(TAG,"Longitude LABEL");
                                    if(isNumericDouble(labeldata)){
                                        Log.d(TAG,"SETTING Longitude");
                                        CurrentState.setLongitude(Double.parseDouble(labeldata));
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
    private void RequestGeoData(){
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
                                    String lightstatusdata = CurrentState.getLightStatusDataString();
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
                                    String lightcolors = CurrentState.getLightColorDataString();
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
    private final Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            FragmentActivity activity = getActivity();
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
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    if (null != activity) {
                        Toast.makeText(activity, "Connected to "
                                + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case Constants.MESSAGE_TOAST:
                    if (null != activity) {
                        Toast.makeText(activity, msg.getData().getString(Constants.TOAST),
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
            return true;
        }
    });
}