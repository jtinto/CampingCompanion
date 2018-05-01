package com.concepttech.campingcompanionbluetooth;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

import static com.concepttech.campingcompanionbluetooth.Constants.FeedFragmentHomeCommand;
import static com.concepttech.campingcompanionbluetooth.Constants.GetDatabaseLocationDataString;
import static com.concepttech.campingcompanionbluetooth.Constants.GetDatabaseLocationString;
import static com.concepttech.campingcompanionbluetooth.Constants.PlacesJSONIDTag;
import static com.concepttech.campingcompanionbluetooth.Constants.PlacesJSONResultsTag;
import static com.concepttech.campingcompanionbluetooth.Constants.PlacesKeyTag;
import static com.concepttech.campingcompanionbluetooth.Constants.PlacesLocationTag;
import static com.concepttech.campingcompanionbluetooth.Constants.PlacesRadiusTag;
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
    private Query FeedReference;
    private DeviceState deviceState;
    private Context context;
    private String Country, AdminArea, Locality, LocationName,LastEntryKey;
    private ArrayList<String> Locations = new ArrayList<>();
    private ArrayList<TimeStamp> TimeStamps = new ArrayList<>();
    private boolean ListenerSet = false, PromptedForLocation = false, GrantedLocation = false;
    private int PhotoCount = 0;
    private FirebaseUser User;
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
        User = FirebaseAuth.getInstance().getCurrentUser();
        if(User == null){

        }
        getLocation();
        if(feedListAdapter == null) feedListAdapter = new FeedListAdapter(getContext());
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
    public void onDestroy(){
        if(FeedReference != null && FeedListener != null) FeedReference.removeEventListener(FeedListener);
        super.onDestroy();
    }
    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {getLocation();
                } else {
                    Toast.makeText(context, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                }
                break;
        }
    }
    private void SignInDialog(){

    }
    private boolean checkAccessFineLocationPermission(){
        String permission = Manifest.permission.ACCESS_FINE_LOCATION;
        int res = context.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }
    private void getDeviceLocation(){
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        String locationProvider = LocationManager.NETWORK_PROVIDER;
        if(locationManager != null) {
            try {
                Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
                Geocoder gcd = new Geocoder(context, Locale.getDefault());
                List<Address> addresses;
                addresses = gcd.getFromLocation(lastKnownLocation.getLatitude(),
                        lastKnownLocation.getLongitude(), 1);
                if (addresses.size() > 0) {
                    Locality = addresses.get(0).getLocality();
                    Country = addresses.get(0).getCountryName();
                    AdminArea = addresses.get(0).getAdminArea();
                    LocationName = addresses.get(0).getFeatureName();
                }
            } catch (SecurityException e) {
                Log.e(TAG, "Exception getting location");
            } catch (Exception e) {
                Log.e(TAG, "Exception getting city: " + e.getMessage());
            }
        }
    }
    private void PromptForLocationUse(){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Message");
        builder.setPositiveButton("Yes", new Dialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                GrantedLocation = true;
            }
        });
        builder.setNegativeButton("No", new Dialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                GrantedLocation = false;
            }
        });
        PromptedForLocation = true;
        builder.show();
    }
    private void getLocation(){
        try {
            String key = getResources().getString(R.string.Key);
            String url;
            if(deviceState.getLongitude() != 0 && deviceState.getLatitude() != 0)
                url = PlacesURL + PlacesLocationTag + deviceState.getLatitude() + "," + deviceState.getLongitude() +
                    PlacesRadiusTag + 5 + PlacesKeyTag + key;
            else {
                if(!checkAccessFineLocationPermission()){
                    if(!PromptedForLocation) {
                        PromptForLocationUse();
                        return;
                    }
                    else if(GrantedLocation) {
                        ActivityCompat.requestPermissions(getActivity(),
                                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                                1);
                        return;
                    }else {
                        mListener.FeedFragmentCallback(FeedFragmentHomeCommand);
                        return;
                    }
                }else {
                    getDeviceLocation();
                    return;
                }
            }
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
        GetLocationData();
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
    private void GetLocationData() {
        DatabaseReference locationdatareference =
                FirebaseDatabase.getInstance().getReference(GetDatabaseLocationDataString(Country, AdminArea, Locality, LocationName));
        locationdatareference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null && dataSnapshot.hasChildren()) {
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        switch (child.getKey()) {
                            case "PhotoCount":
                                if (child.getValue() != null) PhotoCount = (int) child.getValue();
                                break;
                        }
                    }
                }
                if(!ListenerSet && PhotoCount > 0) GetEntries();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                ListenerSet = false;
            }
        });
    }
    private void GetEntries(){
        if(FeedListener == null){
            if(LastEntryKey != null && LastEntryKey.length() > 0) FeedReference =
                    FirebaseDatabase.getInstance().getReference(GetDatabaseLocationString(Country,AdminArea,Locality,LocationName)).orderByKey().limitToLast(20);
            else FeedReference =
                    FirebaseDatabase.getInstance().getReference(GetDatabaseLocationString(Country,AdminArea,Locality,LocationName)).orderByKey().limitToLast(20).startAt(LastEntryKey);
            FeedReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    FeedListener = this;
                    if(dataSnapshot != null && dataSnapshot.hasChildren()){
                        for (DataSnapshot child : dataSnapshot.getChildren()) {
                            if(child.hasChildren()) for (DataSnapshot data : child.getChildren()) {
                                switch (data.getKey()) {
                                    case "Location":
                                        if (child.getValue() != null && !Locations.contains(child.getValue().toString())) Locations.add(child.getValue().toString());
                                        break;
                                    case "TimeStamp":
                                        if (child.getValue() != null) {
                                            TimeStamp temp = new TimeStamp();
                                            temp.buildFromSnapshot(child);
                                            if(!TimeStamps.contains(temp)) TimeStamps.add(temp);
                                        }
                                        break;
                                }
                            }
                            if(Locations.size() > TimeStamps.size()) Locations.remove(Locations.size() - 1);
                            else if(Locations.size() < TimeStamps.size()) TimeStamps.remove(TimeStamps.size() - 1);
                            LastEntryKey = child.getKey();
                        }
                    }
                    if(TimeStamps.size() > 0) feedListAdapter.SetTimeStamps(TimeStamps);
                    feedListAdapter.notifyDataSetChanged();
                    FeedReference.removeEventListener(FeedListener);
                    FeedListener = null;
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                    ListenerSet = false;
                }
            });
            ListenerSet = true;
        }
    }
    public interface FeedFragmentCallback {
        // TODO: Update argument type and name
        void FeedFragmentCallback(String Command);
    }
}
