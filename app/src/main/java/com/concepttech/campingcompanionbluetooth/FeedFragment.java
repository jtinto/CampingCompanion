package com.concepttech.campingcompanionbluetooth;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.util.List;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

import static com.concepttech.campingcompanionbluetooth.Constants.PlacesJSONIDTag;
import static com.concepttech.campingcompanionbluetooth.Constants.PlacesJSONResultsTag;
import static com.concepttech.campingcompanionbluetooth.Constants.PlacesKeyTag;
import static com.concepttech.campingcompanionbluetooth.Constants.PlacesLocationTag;
import static com.concepttech.campingcompanionbluetooth.Constants.PlacesRadiusTag;
import static com.concepttech.campingcompanionbluetooth.Constants.PlacesTypeTag;
import static com.concepttech.campingcompanionbluetooth.Constants.PlacesURL;


public class FeedFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2", TAG = "FeedFragment";
    private View view;
    private String mParam1;
    private String mParam2;
    private FeedListAdapter feedListAdapter;
    private FeedFragmentCallback mListener;
    private ValueEventListener FeedListener;
    private DeviceState deviceState;
    private Context context;
    private String Country, AdminArea, Locality, LocationName;
    private boolean ListenerSet = false;
    public FeedFragment() {
        // Required empty public constructor
    }

    public static FeedFragment newInstance(String param1, String param2) {
        FeedFragment fragment = new FeedFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    public void SetParameters(DeviceState deviceState, Context context){
        this.deviceState = deviceState;
        this.context = context;
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
        view = inflater.inflate(R.layout.fragment_feed_fragrment, container, false);
        getLocation();
        if(feedListAdapter == null) feedListAdapter = new FeedListAdapter(getContext());
        if(!ListenerSet) SetListener();
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FeedFragmentCallback) {
            mListener = (FeedFragmentCallback) context;
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
    private void getLocation(){
        try {
            String key = getResources().getString(R.string.Key);
            String url = PlacesURL + PlacesLocationTag + deviceState.getLatitude() + "," + deviceState.getLongitude() +
                    PlacesRadiusTag + 5 + PlacesKeyTag + key;
            URL Url = new URL(url);
            HttpsURLConnection connection = (HttpsURLConnection) Url.openConnection();
            connection.setDoInput(true);
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.connect();
            try {/*
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                    sb.append("\n");
                }
                reader.close();*/
                JSONObject jsonObject = new JSONObject(connection.getResponseMessage());
                JSONArray jsonArray = jsonObject.getJSONArray(PlacesJSONResultsTag);
                String id = null;
                for(int i = 0; i<1; i++){
                    jsonObject = jsonArray.getJSONObject(i);
                    if(jsonObject.has(PlacesJSONIDTag)) id = jsonObject.getString(PlacesJSONIDTag);
                }
                if(id != null){
                    getAddressFromID(id);
                }
            } catch(Exception e){
                Log.e(TAG,"Error parsing JSON: " + e.getMessage());
            } finally{
                connection.disconnect();
            }
        }catch (Exception e){
            Log.e(TAG, "Error getting location data");
        }
    }
    private void getAddressFromID(String placeid){
        try {
            final GeoDataClient geoDataClient = Places.getGeoDataClient(context,null);
            geoDataClient.getPlaceById(placeid).addOnCompleteListener(new OnCompleteListener<PlaceBufferResponse>() {
                @SuppressLint("RestrictedApi")
                @Override
                public void onComplete(@NonNull Task<PlaceBufferResponse> task) {
                    if (task.isSuccessful()) {
                        PlaceBufferResponse places = task.getResult();
                        @SuppressLint("RestrictedApi") Place myPlace = places.get(0);
                        Log.i(TAG, "Place found: " + myPlace.getName());
                        Address address = getAddressFromName(myPlace.getAddress().toString());
                        if(address != null){
                            Country = address.getCountryName();
                            AdminArea = address.getAdminArea();
                            Locality = address.getLocality();
                            if(address.getFeatureName() != null){
                                LocationName = address.getFeatureName();
                                Toast.makeText(context, "Found: " + LocationName, Toast.LENGTH_LONG).show();
                            }
                            else Toast.makeText(context, "Cannot find place name", Toast.LENGTH_LONG).show();
                        }
                        places.release();
                    } else {
                        Log.e(TAG, "Place not found.");
                    }
                }
            });
        }catch (Exception e){
            Log.e(TAG,"Error getting address");
        }
    }
    private Address getAddressFromName(String name){
        try {
            Geocoder geocoder = new Geocoder(context);
            List<Address> addresses = geocoder.getFromLocationName(name, 1);
            return addresses.get(0);
        }catch (Exception e){
            Log.e(TAG,"Error getting address");
        }
        return null;
    }
    private void SetListener(){
        if(FeedListener == null){

        }
    }
    public interface FeedFragmentCallback {
        // TODO: Update argument type and name
        void FeedFragmentCallback(String Command);
    }
}
