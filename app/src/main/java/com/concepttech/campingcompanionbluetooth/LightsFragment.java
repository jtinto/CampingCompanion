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

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import static android.support.v4.content.ContextCompat.getColor;
import static com.concepttech.campingcompanionbluetooth.Constants.LightStaticStatus;
import static com.concepttech.campingcompanionbluetooth.Constants.LightStatusOff;
import static com.concepttech.campingcompanionbluetooth.Constants.LightsFragmentBack;
import static com.concepttech.campingcompanionbluetooth.Constants.LightsFragmentChangeColor;
import static com.concepttech.campingcompanionbluetooth.Constants.Rainbow1Status;
import static com.concepttech.campingcompanionbluetooth.Constants.Rainbow2Status;
import static com.concepttech.campingcompanionbluetooth.Constants.TheaterStatus;

public class LightsFragment extends Fragment implements View.OnClickListener,
                                                        SeekBar.OnSeekBarChangeListener{

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;
    private ArrayList<View> Previews = new ArrayList<>();
    private View view,LightFragmentRedSelector, LightFragmentBlueSelector, LightFragmentGreenSelector, LightFragmentWhiteSelector,
            LightFragmentPurpleSelector, LightFragmentPinkSelector, LightFragmentYellowSelector, LightFragmentOrangeSelector;
    private Button TheaterButton, Rainbow1Button, Raingbow2Button, BackButton, TurnOnOffButton;
    private SeekBar RedSeekBar,BlueSeekBar,GreenSeekBar;
    private LightsFragmentCallback mCallback;
    private DeviceState deviceState;
    private Context context;
    private Timer Theater1Timer,Theater2Timer,Theater3Timer;
    private boolean StaticSelected,Theater1Selected,Theater2Selected,Theater3Selected;
    private boolean Timer1Active = false,Timer2Active = false,Timer3Active = false, LightsOn = false;
    private int ChaseIndex, RainbowIndex = 0, LastSetColor = 0;

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
        SetColorForBar(RedSeekBar.getProgress(),GreenSeekBar.getProgress(),BlueSeekBar.getProgress());
        if(!Theater1Selected&&!Theater2Selected&&!Theater3Selected) deviceState.TurnLightOn();
        mCallback.LightsFragmentCallback(LightsFragmentChangeColor, deviceState);
    }
    @Override
    public void onClick(View view) {
        int id;
        if(deviceState != null) {
            TheaterButton.setBackground(context.getDrawable(R.drawable.peblgreenroundedcornerssemitransparent));
            Rainbow1Button.setBackground(context.getDrawable(R.drawable.peblgreenroundedcornerssemitransparent));
            Raingbow2Button.setBackground(context.getDrawable(R.drawable.peblgreenroundedcornerssemitransparent));
        }
        switch (view.getId()) {
            case R.id.LightFragmentRedSelector:
                if(!Theater1Selected&&!Theater2Selected&&!Theater3Selected) deviceState.TurnLightOn();
                id = getColor(context,R.color.Red);
                SetColor(id);
                mCallback.LightsFragmentCallback(LightsFragmentChangeColor, deviceState);
                break;
            case R.id.LightFragmentBlueSelector:
                if(!Theater1Selected&&!Theater2Selected&&!Theater3Selected) deviceState.TurnLightOn();
                id = getColor(context,R.color.Blue);
                SetColor(id);
                mCallback.LightsFragmentCallback(LightsFragmentChangeColor, deviceState);
                break;
            case R.id.LightFragmentGreenSelector:
                if(!Theater1Selected&&!Theater2Selected&&!Theater3Selected) deviceState.TurnLightOn();
                id = getColor(context,R.color.Green);
                SetColor(id);
                mCallback.LightsFragmentCallback(LightsFragmentChangeColor, deviceState);
                break;
            case R.id.LightFragmentWhiteSelector:
                if(!Theater1Selected&&!Theater2Selected&&!Theater3Selected) deviceState.TurnLightOn();
                id = getColor(context,R.color.White);
                SetColor(id);
                mCallback.LightsFragmentCallback(LightsFragmentChangeColor, deviceState);
                break;
            case R.id.LightFragmentOrangeSelector:
                if(!Theater1Selected&&!Theater2Selected&&!Theater3Selected) deviceState.TurnLightOn();
                id = getColor(context,R.color.Orange);
                SetColor(id);
                mCallback.LightsFragmentCallback(LightsFragmentChangeColor, deviceState);
                break;
            case R.id.LightFragmentPinkSelector:
                if(!Theater1Selected&&!Theater2Selected&&!Theater3Selected) deviceState.TurnLightOn();
                id = getColor(context,R.color.Pink);
                SetColor(id);
                mCallback.LightsFragmentCallback(LightsFragmentChangeColor, deviceState);
                break;
            case R.id.LightFragmentPurpleSelector:
                if(!Theater1Selected&&!Theater2Selected&&!Theater3Selected) deviceState.TurnLightOn();
                id = getColor(context,R.color.Purple);
                SetColor(id);
                mCallback.LightsFragmentCallback(LightsFragmentChangeColor, deviceState);
                break;
            case R.id.LightFragmentYellowSelector:
                if(!Theater1Selected&&!Theater2Selected&&!Theater3Selected) deviceState.TurnLightOn();
                id = getColor(context,R.color.Yellow);
                SetColor(id);
                mCallback.LightsFragmentCallback(LightsFragmentChangeColor, deviceState);
                break;
            case R.id.LightFragmentTheaterButton:
                if(Theater1Selected) {
                    if(Timer1Active) Theater1Stop();
                    deviceState.TurnLightOn();
                    Theater1Selected = false;
                }else {
                    Theater1Selected = true;
                    if(LastSetColor == 0) LastSetColor = getColor(context,R.color.White);
                    SetColor(LastSetColor);
                    deviceState.TurnLightsTheater(0);
                    Theater1Start();
                }
                Theater2Selected = Theater3Selected = false;
                mCallback.LightsFragmentCallback(LightsFragmentChangeColor, deviceState);
                break;
            case R.id.LightFragmentRainbow1Button:
                if(Theater2Selected) {
                    if(Timer2Active) Theater2Stop();
                    deviceState.TurnLightOn();
                    Theater2Selected = false;
                }else {
                    Theater2Selected = true;
                    deviceState.TurnLightsTheater(1);
                    Theater2Start();
                }
                Theater1Selected = Theater3Selected = false;
                mCallback.LightsFragmentCallback(LightsFragmentChangeColor, deviceState);
                break;
            case R.id.LightFragmentRainbow2Button:
                if(Theater3Selected) {
                    if(Timer3Active) Theater3Stop();
                    deviceState.TurnLightOn();
                    Theater3Selected = false;
                }
                else {
                    Theater3Selected = true;
                    deviceState.TurnLightsTheater(2);
                    Theater3Start();
                }
                Theater1Selected = Theater2Selected = false;
                mCallback.LightsFragmentCallback(LightsFragmentChangeColor, deviceState);
                break;
            case R.id.LightFragmentBackButton:
                Theater1Selected = Theater2Selected = Theater3Selected = false;
                mCallback.LightsFragmentCallback(LightsFragmentBack, deviceState);
                break;
            case R.id.LightFragmentTurnOnOffButton:
                Theater1Selected = Theater2Selected = Theater3Selected = false;
                if(Timer1Active) Theater1Stop();
                if(Timer2Active) Theater2Stop();
                if(Timer3Active) Theater3Stop();
                deviceState.TurnLightOff();
                mCallback.LightsFragmentCallback(LightsFragmentChangeColor, deviceState);
        }
        if(deviceState != null) {
            if (deviceState.getLightStatusDataString().equals(TheaterStatus))
                TheaterButton.setBackground(context.getDrawable(R.drawable.selectedpeblebuttonroundedcorners));
            else if (deviceState.getLightStatusDataString().equals(Rainbow1Status))
                Rainbow1Button.setBackground(context.getDrawable(R.drawable.selectedpeblebuttonroundedcorners));
            else if (deviceState.getLightStatusDataString().equals(Rainbow2Status))
                Raingbow2Button.setBackground(context.getDrawable(R.drawable.selectedpeblebuttonroundedcorners));
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
    @Override
    public void onDestroy(){
        if(Timer1Active) Theater1Stop();
        if(Timer2Active) Theater2Stop();
        if(Timer3Active) Theater3Stop();
        super.onDestroy();
    }
    private void Initialize(){
        if(view != null){
            LightFragmentRedSelector = view.findViewById(R.id.LightFragmentRedSelector);
            LightFragmentBlueSelector = view.findViewById(R.id.LightFragmentBlueSelector);
            LightFragmentGreenSelector = view.findViewById(R.id.LightFragmentGreenSelector);
            LightFragmentWhiteSelector = view.findViewById(R.id.LightFragmentWhiteSelector);
            LightFragmentPurpleSelector = view.findViewById(R.id.LightFragmentPurpleSelector);
            LightFragmentPinkSelector = view.findViewById(R.id.LightFragmentPinkSelector);
            LightFragmentYellowSelector = view.findViewById(R.id.LightFragmentYellowSelector);
            LightFragmentOrangeSelector = view.findViewById(R.id.LightFragmentOrangeSelector);
            Previews.add(view.findViewById(R.id.ColorPreviewView1));
            Previews.add(view.findViewById(R.id.ColorPreviewView2));
            Previews.add(view.findViewById(R.id.ColorPreviewView3));
            Previews.add(view.findViewById(R.id.ColorPreviewView4));
            Previews.add(view.findViewById(R.id.ColorPreviewView5));
            Previews.add(view.findViewById(R.id.ColorPreviewView6));
            Previews.add(view.findViewById(R.id.ColorPreviewView7));
            Previews.add(view.findViewById(R.id.ColorPreviewView8));
            TheaterButton = view.findViewById(R.id.LightFragmentTheaterButton);
            Rainbow1Button = view.findViewById(R.id.LightFragmentRainbow1Button);
            Raingbow2Button = view.findViewById(R.id.LightFragmentRainbow2Button);
            BackButton = view.findViewById(R.id.LightFragmentBackButton);
            TurnOnOffButton = view.findViewById(R.id.LightFragmentTurnOnOffButton);
            RedSeekBar = view.findViewById(R.id.LightFragmentRedSeekBar);
            GreenSeekBar = view.findViewById(R.id.LightFragmentGreenSeekBar);
            BlueSeekBar = view.findViewById(R.id.LightFragmentBlueSeekBar);
            LightFragmentRedSelector.setOnClickListener(this);
            LightFragmentBlueSelector.setOnClickListener(this);
            LightFragmentGreenSelector.setOnClickListener(this);
            LightFragmentWhiteSelector.setOnClickListener(this);
            LightFragmentPurpleSelector.setOnClickListener(this);
            LightFragmentPinkSelector.setOnClickListener(this);
            LightFragmentYellowSelector.setOnClickListener(this);
            LightFragmentOrangeSelector.setOnClickListener(this);
            TurnOnOffButton.setOnClickListener(this);
            TheaterButton.setOnClickListener(this);
            Rainbow1Button.setOnClickListener(this);
            Raingbow2Button.setOnClickListener(this);
            BackButton.setOnClickListener(this);
            RedSeekBar.setMax(255);
            GreenSeekBar.setMax(255);
            BlueSeekBar.setMax(255);
            RedSeekBar.setOnSeekBarChangeListener(this);
            GreenSeekBar.setOnSeekBarChangeListener(this);
            BlueSeekBar.setOnSeekBarChangeListener(this);
            if(deviceState != null) {
                if (deviceState.getLightStatusDataString().equals(TheaterStatus)){
                    TheaterButton.setBackground(context.getDrawable(R.drawable.selectedpeblebuttonroundedcorners));
                    LightsOn = true;
                }
                else if (deviceState.getLightStatusDataString().equals(Rainbow1Status)) {
                    Rainbow1Button.setBackground(context.getDrawable(R.drawable.selectedpeblebuttonroundedcorners));
                    LightsOn = true;
                }
                else if (deviceState.getLightStatusDataString().equals(Rainbow2Status)) {
                    Raingbow2Button.setBackground(context.getDrawable(R.drawable.selectedpeblebuttonroundedcorners));
                    LightsOn = true;
                }
            }
        }
    }
    private int[] GetRainbow(int index){
        if (index < 85) {
            return new int[]{index * 3, 255 - index * 3, 0};
        }
        else if (index < 170) {
            index -= 85;
            return new int[]{255 - index * 3, 0, index * 3};
        }
        else {
            index -= 170;
            return new int[]{0, index * 3, 255 - index * 3};
        }
    }
    private void SetColor(int id){
        int red = Color.red(id), green = Color.green(id), blue = Color.blue(id);
        RedSeekBar.setProgress(red);
        GreenSeekBar.setProgress(green);
        BlueSeekBar.setProgress(blue);
        LastSetColor = id;
        SetPreviewViewStaticColor(id);
        SetStateColor(red,green,blue);
    }
    private void SetColorForBar(int red, int green,int blue){
        int id = Color.argb(255,red,green,blue);
        SetPreviewViewStaticColor(id);
        SetStateColor(red,green,blue);
    }
    private void SetPreviewViewStaticColor(int id){
        for (View view: Previews
             ) {
            view.setBackgroundColor(id);
        }
    }
    private void Theater1Start(){
        if(Timer2Active) Theater2Stop();
        if(Timer3Active) Theater3Stop();
        if(Theater1Timer == null) {
            RainbowIndex = 0;
            Theater1Timer = new Timer();
            Timer1Active = true;
            Theater1Timer.schedule( new TimerTask() {
                @Override
                public void run() {
                    if (getActivity() != null) getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            for (int i = 0; i < Previews.size(); i += 3) {
                                if(i + (ChaseIndex-1) < Previews.size() && i + (ChaseIndex-1) >= 0)Previews.get(i + (ChaseIndex-1)).setBackgroundColor(Color.argb(0, 0, 0, 0));
                                if(i + ChaseIndex < Previews.size()) Previews.get(i + ChaseIndex).setBackgroundColor(LastSetColor);
                            }
                            ChaseIndex++;
                            if(ChaseIndex > 3) ChaseIndex = 0;
                        }
                    });
                }
            }, 0, 50);
        }
    }
    private void Theater1Stop(){
        if(Theater1Timer != null){
            Theater1Timer.cancel();
            Theater1Timer = null;
            Timer1Active = false;
        }
    }
    private void Theater2Start(){
        if(Timer1Active) Theater1Stop();
        if(Timer3Active) Theater3Stop();
        if(Theater2Timer == null) {
            Theater2Timer = new Timer();
            RainbowIndex = 0;
            Timer2Active = true;
            Theater2Timer.schedule( new TimerTask() {
                @Override
                public void run() {
                    if(getActivity() != null) getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            for (int i = 0; i < Previews.size(); i++) {
                                int[] colors = GetRainbow((i + RainbowIndex) & 255);
                                RainbowIndex++;
                                if(RainbowIndex > 10000) RainbowIndex = 0;
                                Previews.get(i).setBackgroundColor(Color.argb(255, colors[0], colors[1], colors[2]));
                            }
                        }
                    });
                }
            }, 0, 200);
        }
    }
    private void Theater2Stop(){
        if(Theater2Timer != null){
            Theater2Timer.cancel();
            Theater2Timer = null;
            Timer2Active = false;
        }
    }
    private void Theater3Start(){
        if(Timer1Active) Theater1Stop();
        if(Timer2Active) Theater2Stop();
        if(Theater3Timer == null) {
            Theater3Timer = new Timer();
            Timer3Active = true;
            RainbowIndex = 0;
            ChaseIndex = 0;
            Theater3Timer.schedule( new TimerTask() {
                @Override
                public void run() {
                    if (getActivity() != null) getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            for (int i = 0; i < Previews.size(); i++) {
                                int[] colors = GetRainbow(((i * 256 / Previews.size()) + RainbowIndex) & 255);
                                RainbowIndex++;
                                if(RainbowIndex > 10000) RainbowIndex = 0;
                                Previews.get(i).setBackgroundColor(Color.argb(255, colors[0], colors[1], colors[2]));
                            }
                        }
                    });
                }
            }, 0, 200);
        }
    }
    private void Theater3Stop(){
        if(Theater3Timer != null){
            Theater3Timer.cancel();
            Theater3Timer = null;
            Timer3Active = false;
        }
    }
    private void SetStateColor(int red,int green, int blue){
        deviceState.setLightcolors(red,green,blue);
    }
    public interface LightsFragmentCallback {
        // TODO: Update argument type and name
        void LightsFragmentCallback(String request, DeviceState state);
    }
    private void SolidRainbowLoop(){
        for (int i = 0; i < Previews.size(); i += 3) {
            if(i + (ChaseIndex-1) < Previews.size() && i + (ChaseIndex-1) >= 0)Previews.get(i + (ChaseIndex-1)).setBackgroundColor(Color.argb(0, 0, 0, 0));
            int[] colors = GetRainbow((i + RainbowIndex) % 255);
            RainbowIndex++;
            if (RainbowIndex > 10000) RainbowIndex = 0;
            if(i + ChaseIndex < Previews.size()) Previews.get(i + ChaseIndex).setBackgroundColor(Color.argb(255, colors[0], colors[1], colors[2]));
        }
        ChaseIndex++;
        if(ChaseIndex > 3) ChaseIndex = 0;
    }
}
