package com.concepttech.campingcompanionbluetooth;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

public class StatusFragment extends Fragment implements View.OnClickListener{
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;
    private DeviceState deviceState;
    private Timer timer;
    private View view;
    private TextView TemperatureView, BaroView, HumidityView;
    private Button BackButton;

    private StatusFragmentCallback mListener;

    public StatusFragment() {
        // Required empty public constructor
    }

    public static StatusFragment newInstance(String param1, String param2) {
        StatusFragment fragment = new StatusFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public void onClick(View v){
        if(v == BackButton){
            CancelTimer();
            mListener.StatusFragmentCallback();
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
        // Inflate the layout for this fragment
        if(view == null) view = inflater.inflate(R.layout.fragment_status, container, false);
        Initialize();
        return view;
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof StatusFragmentCallback) {
            mListener = (StatusFragmentCallback) context;
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

    private void Initialize() {
        if (view != null) {
            TemperatureView = view.findViewById(R.id.TemperatureTextview);
            BaroView = view.findViewById(R.id.BarometricTextview);
            HumidityView = view.findViewById(R.id.HumidityTextview);
            BackButton = view.findViewById(R.id.StatusFragmentBackButton);
            BackButton.setOnClickListener(this);
            StartTimer();
        }
    }
    public void SetDeviceState(DeviceState state){deviceState = state;}
    private void UpdateTextViews(){
        if(deviceState != null &&TemperatureView != null &&
                BaroView != null &&HumidityView != null){
            String placeholder = deviceState.getTemperature() + " degrees Farenheit";
            TemperatureView.setText(placeholder);
            placeholder = deviceState.getTemperature() + " inHg";
            BaroView.setText(placeholder);
            placeholder = deviceState.getTemperature() + "%";
            HumidityView.setText(placeholder);
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
                            UpdateTextViews();
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
    public interface StatusFragmentCallback {
        // TODO: Update argument type and name
        void StatusFragmentCallback();
    }
}
