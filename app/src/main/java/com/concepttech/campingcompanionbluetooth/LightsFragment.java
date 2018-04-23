package com.concepttech.campingcompanionbluetooth;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;

import static android.support.v4.content.ContextCompat.getColor;
import static com.concepttech.campingcompanionbluetooth.Constants.LightStatusOff;
import static com.concepttech.campingcompanionbluetooth.Constants.LightsFragmentBack;
import static com.concepttech.campingcompanionbluetooth.Constants.LightsFragmentChangeColor;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link LightsFragment.LightsFragmentCallback} interface
 * to handle interaction events.
 * Use the {@link LightsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LightsFragment extends Fragment implements View.OnClickListener,
                                                        SeekBar.OnSeekBarChangeListener{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private View view,LightFragmentRedSelector, LightFragmentBlueSelector, LightFragmentGreenSelector, LightFragmentCyanSelector,
            LightFragmentPurpleSelector, LightFragmentPinkSelector, LightFragmentYellowSelector, LightFragmentOrangeSelector, PreviewView;
    private Button TheaterButton, Rainbow1Button, Raingbow2Button, BackButton, TurnOffButton;
    private SeekBar RedSeekBar,BlueSeekBar,GreenSeekBar;
    private LightsFragmentCallback mCallback;
    private DeviceState deviceState;
    private Context context;

    public LightsFragment() {
        // Required empty public constructor
    }
    public void SetState(DeviceState state, Context context){
        deviceState = state;
        this.context = context;
    }
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment LightsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LightsFragment newInstance(String param1, String param2) {
        LightsFragment fragment = new LightsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        int id = Color.argb(255,RedSeekBar.getProgress(),GreenSeekBar.getProgress(),BlueSeekBar.getProgress());
        SetColor(id);
        mCallback.LightsFragmentCallback(LightsFragmentChangeColor,deviceState);
    }
    @Override
    public void onClick(View view) {
        int id;
        if (deviceState.getLightStatusDataString().equals(LightStatusOff)) deviceState.TurnLightOn();
        switch (view.getId()) {
            case R.id.LightFragmentRedSelector:
                id = getColor(context,R.color.Red);
                SetColor(id);
                mCallback.LightsFragmentCallback(LightsFragmentChangeColor, deviceState);
                break;
            case R.id.LightFragmentBlueSelector:
                id = getColor(context,R.color.Blue);
                SetColor(id);
                mCallback.LightsFragmentCallback(LightsFragmentChangeColor, deviceState);
                break;
            case R.id.LightFragmentGreenSelector:
                id = getColor(context,R.color.Green);
                SetColor(id);
                mCallback.LightsFragmentCallback(LightsFragmentChangeColor, deviceState);
                break;
            case R.id.LightFragmentCyanSelector:
                id = getColor(context,R.color.Cyan);
                SetColor(id);
                mCallback.LightsFragmentCallback(LightsFragmentChangeColor, deviceState);
                break;
            case R.id.LightFragmentOrangeSelector:
                id = getColor(context,R.color.Orange);
                SetColor(id);
                mCallback.LightsFragmentCallback(LightsFragmentChangeColor, deviceState);
                break;
            case R.id.LightFragmentPinkSelector:
                id = getColor(context,R.color.Pink);
                SetColor(id);
                mCallback.LightsFragmentCallback(LightsFragmentChangeColor, deviceState);
                break;
            case R.id.LightFragmentPurpleSelector:
                id = getColor(context,R.color.Purple);
                SetColor(id);
                mCallback.LightsFragmentCallback(LightsFragmentChangeColor, deviceState);
                break;
            case R.id.LightFragmentYellowSelector:
                id = getColor(context,R.color.Yellow);
                SetColor(id);
                mCallback.LightsFragmentCallback(LightsFragmentChangeColor, deviceState);
                break;
            case R.id.LightFragmentTheaterButton:
                deviceState.TurnLightsTheater(0);
                mCallback.LightsFragmentCallback(LightsFragmentChangeColor, deviceState);
                break;
            case R.id.LightFragmentRainbow1Button:
                deviceState.TurnLightsTheater(1);
                mCallback.LightsFragmentCallback(LightsFragmentChangeColor, deviceState);
                break;
            case R.id.LightFragmentRainbow2Button:
                deviceState.TurnLightsTheater(2);
                mCallback.LightsFragmentCallback(LightsFragmentChangeColor, deviceState);
                break;
            case R.id.TurnLightsOffButton:
                deviceState.TurnLightOff();
                mCallback.LightsFragmentCallback(LightsFragmentChangeColor, deviceState);
                break;
            case R.id.LightFragmentBackButton:
                mCallback.LightsFragmentCallback(LightsFragmentBack, deviceState);
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
        // Inflate the layout for this fragment
        if(view == null) view = inflater.inflate(R.layout.fragment_lights, container, false);
        Initialize();
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof LightsFragmentCallback) {
            mCallback = (LightsFragmentCallback) context;
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
    private void Initialize(){
        if(view != null){
            LightFragmentRedSelector = view.findViewById(R.id.LightFragmentRedSelector);
            LightFragmentBlueSelector = view.findViewById(R.id.LightFragmentBlueSelector);
            LightFragmentGreenSelector = view.findViewById(R.id.LightFragmentGreenSelector);
            LightFragmentCyanSelector = view.findViewById(R.id.LightFragmentCyanSelector);
            LightFragmentPurpleSelector = view.findViewById(R.id.LightFragmentPurpleSelector);
            LightFragmentPinkSelector = view.findViewById(R.id.LightFragmentPinkSelector);
            LightFragmentYellowSelector = view.findViewById(R.id.LightFragmentYellowSelector);
            LightFragmentOrangeSelector = view.findViewById(R.id.LightFragmentOrangeSelector);
            TheaterButton = view.findViewById(R.id.LightFragmentTheaterButton);
            Rainbow1Button = view.findViewById(R.id.LightFragmentRainbow1Button);
            Raingbow2Button = view.findViewById(R.id.LightFragmentRainbow2Button);
            BackButton = view.findViewById(R.id.LightFragmentBackButton);
            TurnOffButton = view.findViewById(R.id.TurnLightsOffButton);
            PreviewView = view.findViewById(R.id.ColorPreviewView);
            RedSeekBar = view.findViewById(R.id.LightFragmentRedSeekBar);
            GreenSeekBar = view.findViewById(R.id.LightFragmentGreenSeekBar);
            BlueSeekBar = view.findViewById(R.id.LightFragmentBlueSeekBar);
            LightFragmentRedSelector.setOnClickListener(this);
            LightFragmentBlueSelector.setOnClickListener(this);
            LightFragmentGreenSelector.setOnClickListener(this);
            LightFragmentCyanSelector.setOnClickListener(this);
            LightFragmentPurpleSelector.setOnClickListener(this);
            LightFragmentPinkSelector.setOnClickListener(this);
            LightFragmentYellowSelector.setOnClickListener(this);
            LightFragmentOrangeSelector.setOnClickListener(this);
            TheaterButton.setOnClickListener(this);
            Rainbow1Button.setOnClickListener(this);
            Raingbow2Button.setOnClickListener(this);
            BackButton.setOnClickListener(this);
            TurnOffButton.setOnClickListener(this);
            RedSeekBar.setMax(255);
            GreenSeekBar.setMax(255);
            BlueSeekBar.setMax(255);
            RedSeekBar.setOnSeekBarChangeListener(this);
            GreenSeekBar.setOnSeekBarChangeListener(this);
            BlueSeekBar.setOnSeekBarChangeListener(this);
        }
    }
    private void SetColor(int id){
        int red = Color.red(id), green = Color.green(id), blue = Color.blue(id);
        deviceState.setLightcolors(red,green,blue);
        PreviewView.setBackgroundColor(id);
    }
    public interface LightsFragmentCallback {
        // TODO: Update argument type and name
        void LightsFragmentCallback(String request, DeviceState state);
    }
}
