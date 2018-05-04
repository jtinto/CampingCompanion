package com.concepttech.campingcompanionbluetooth;

import android.annotation.SuppressLint;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Timer;
import java.util.TimerTask;


public class LocationFragment extends Fragment implements View.OnClickListener{
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;
    private GoogleMap Map;
    private MapView mapView;
    private boolean MapBeingInteractedwith = false;
    private float startX;
    private float startY;
    private View view;
    private DeviceState deviceState;
    private MapFragmentCallBack mListener;
    private CameraPosition LastPosition;
    private Timer timer;
    Button CenterButton;

    public LocationFragment() {
        // Required empty public constructor
    }
    public static LocationFragment newInstance(String param1, String param2) {
        LocationFragment fragment = new LocationFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.MapFragmentBackButton:
                CancelTimer();
                mListener.MapFragmentCallBack();
            case R.id.MapFragmentCenterButton:
                MapBeingInteractedwith = false;
                LastPosition = Map.getCameraPosition();
                CenterButton.setVisibility(View.INVISIBLE);
                UpdateLocation();
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

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view  = inflater.inflate(R.layout.fragment_location, container, false);
        Button back = view.findViewById(R.id.MapFragmentBackButton);
        back.setOnClickListener(this);
        CenterButton = view.findViewById(R.id.MapFragmentCenterButton);
        CenterButton.setOnClickListener(this);
        mapView = view.findViewById(R.id.MapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                Map = googleMap;
                UpdateLocation();
                StartTimer();
            }
        });
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof MapFragmentCallBack) {
            mListener = (MapFragmentCallBack) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
    public void SetDeviceState(DeviceState state){deviceState = state;}
    private void UpdateLocation(){
        if(Map != null && mapView != null && deviceState != null) {
            LatLng coordinates;
            if (deviceState.getLatitude() != 0 && deviceState.getLongitude() != 0) {
                coordinates = new LatLng(deviceState.getLatitude(), deviceState.getLongitude());
            } else {
                coordinates = new LatLng(37.716226, -97.287538);
            }
            if(LastPosition != null && !Map.getCameraPosition().equals(LastPosition)){
                MapBeingInteractedwith = true;
                CenterButton.setVisibility(View.VISIBLE);
            }
            Map.addMarker(new MarkerOptions().position(coordinates));
            if(!MapBeingInteractedwith){
                Map.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinates, 20));
                LastPosition = Map.getCameraPosition();
            }
            mapView.onResume();
        }
    }
    private void StartTimer(){
        if(timer == null) {
            timer = new Timer();
            timer.schedule( new TimerTask() {
                @Override
                public void run() {
                    if(getActivity() != null) getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            UpdateLocation();
                        }
                    });
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
    private boolean isAClick(float startX, float endX, float startY, float endY) {
        float differenceX = Math.abs(startX - endX);
        float differenceY = Math.abs(startY - endY);
        int CLICK_ACTION_THRESHOLD = 200;
        return !(differenceX > CLICK_ACTION_THRESHOLD/* =5 */ || differenceY > CLICK_ACTION_THRESHOLD);
    }
    public interface MapFragmentCallBack {
        void MapFragmentCallBack();
    }
}
