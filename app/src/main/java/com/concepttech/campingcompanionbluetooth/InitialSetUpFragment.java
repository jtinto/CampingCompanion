package com.concepttech.campingcompanionbluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link InitialSetUpFragment.InitialSetUpCallback} interface
 * to handle interaction events.
 * Use the {@link InitialSetUpFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class InitialSetUpFragment extends Fragment implements View.OnClickListener{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final int REQUEST_ENABLE_BT = 3;

    // TODO: Rename and change types of parameters
    private String mParam1, TAG = "InitialSetUp";
    private String mParam2;
    private View ThisView;
    private InitialSetUpCallback mCallback;

    public InitialSetUpFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment InitialSetUpFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static InitialSetUpFragment newInstance(String param1, String param2) {
        InitialSetUpFragment fragment = new InitialSetUpFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onClick(View view){
        switch (view.getId()){
            case R.id.SetUpStartButton:
                if(!checkAccessCoarseLocationPermission()){
                    ActivityCompat.requestPermissions(getActivity(),
                            new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                            1);
                }else{
                    if(!checkBluetooth()){
                        RequestBluetooth();
                    }else mCallback.InitialSetUpCallback();
                }
                break;
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
        ThisView = inflater.inflate(R.layout.fragment_initial_set_up, container, false);
        FirstPageInitialize();
        return ThisView;
    }
    private void FirstPageInitialize(){
        Button button = ThisView.findViewById(R.id.SetUpStartButton);
        button.setOnClickListener(this);
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof InitialSetUpCallback) {
            mCallback = (InitialSetUpCallback) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
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
    private boolean checkAccessCoarseLocationPermission()
    {
        String permission = android.Manifest.permission.ACCESS_COARSE_LOCATION;
        int res = getContext().checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
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
    public interface InitialSetUpCallback {
        // TODO: Update argument type and name
        void InitialSetUpCallback();
    }
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
                        mCallback.InitialSetUpCallback();
                    }
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(getActivity().getApplicationContext(), "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                }
                return;
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                Log.d(TAG,"REQUEST_ENABLE_BT");
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    Log.d(TAG,"RESULT_OK");
                    mCallback.InitialSetUpCallback();
                } else {
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(getActivity(), R.string.bt_not_enabled_leaving,
                            Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                }
        }
    }
}
