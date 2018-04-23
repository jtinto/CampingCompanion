package com.concepttech.campingcompanionbluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Set;

import static com.concepttech.campingcompanionbluetooth.Constants.ScanText;
import static com.concepttech.campingcompanionbluetooth.Constants.ScanningText;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ConnectionFragment.ConnectionFragmentCallback} interface
 * to handle interaction events.
 * Use the {@link ConnectionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ConnectionFragment extends Fragment implements View.OnClickListener{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    public static String EXTRA_DEVICE_ADDRESS = "device_address", TAG = "ConnectionsFragment";
    private BluetoothAdapter mBtAdapter;
    private View ThisView;
    private Button scanButton;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private ArrayAdapter<String> mNewDevicesArrayAdapter, pairedDevicesArrayAdapter;
    private ArrayList<BluetoothDevice> mDevicesArray = new ArrayList<>();
    ListView newDevicesListView;

    private ConnectionFragmentCallback mCallback;

    public ConnectionFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ConnectionFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ConnectionFragment newInstance(String param1, String param2) {
        ConnectionFragment fragment = new ConnectionFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public void onClick(View view){
        if(view.getId() == R.id.button_scan){
            doDiscovery();
        }
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ThisView = inflater.inflate(R.layout.fragment_connection, container, false);
        Initialize();
        return ThisView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    private void Initialize(){
        scanButton = ThisView.findViewById(R.id.button_scan);
        scanButton.setOnClickListener(this);
        mNewDevicesArrayAdapter = new ArrayAdapter<>(ThisView.getContext(), R.layout.device_name);
        newDevicesListView = ThisView.findViewById(R.id.new_devices);
        newDevicesListView.setAdapter(mNewDevicesArrayAdapter);
        newDevicesListView.setOnItemClickListener(mDeviceClickListener);
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        ThisView.getContext().registerReceiver(mReceiver, filter);
        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        ThisView.getContext().registerReceiver(mReceiver, filter);
        // Get the local Bluetooth adapter
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        doDiscovery();
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ConnectionFragmentCallback) {
            mCallback = (ConnectionFragmentCallback) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement ConnectionFragmentCallback");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
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

    private void doDiscovery() {
        Log.d(TAG, "doDiscovery()");

        // If we're already discovering, stop it
        if (mBtAdapter.isDiscovering()) {
            scanButton.setText(ScanText);
            scanButton.setActivated(true);
            mBtAdapter.cancelDiscovery();
        }else{
            scanButton.setActivated(false);
            scanButton.setText(ScanningText);
        }
        // Request discover from BluetoothAdapter
        mBtAdapter.startDiscovery();
    }
    private AdapterView.OnItemClickListener mDeviceClickListener
            = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            // Cancel discovery because it's costly and we're about to connect
            mBtAdapter.cancelDiscovery();

            // Get the device MAC address, which is the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);
            boolean found = false;
            for (BluetoothDevice device: mDevicesArray) {
                if(!found && device.getAddress().equals(address)) {
                    found = true;
                    mCallback.ConnectionFragmentCallback(true, device);
                }
            }
        }
    };

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // If it's already paired, skip it, because it's been listed already
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    String name = "";
                    if(device.getName() == null) name = "Unknown";
                    else name = device.getName();
                    mNewDevicesArrayAdapter.add(name + "\n" + device.getAddress());
                    mDevicesArray.add(device);
                }
                // When discovery is finished, change the Activity title
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                if (mNewDevicesArrayAdapter.getCount() == 0) {
                    scanButton.setText(ScanText);
                    scanButton.setActivated(true);
                    String noDevices = getResources().getText(R.string.none_found).toString();
                    mNewDevicesArrayAdapter.add(noDevices);
                }
            }
        }
    };
    public void Cleanup(){
        if(getContext() != null) getContext().unregisterReceiver(mReceiver);
    }
    public interface ConnectionFragmentCallback {
        // TODO: Update argument type and name
        void ConnectionFragmentCallback(boolean ResultOk, BluetoothDevice device);
    }
}
