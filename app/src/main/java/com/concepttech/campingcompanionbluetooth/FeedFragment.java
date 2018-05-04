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
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import javax.net.ssl.HttpsURLConnection;

import static com.concepttech.campingcompanionbluetooth.Constants.FeedCameraCommand;
import static com.concepttech.campingcompanionbluetooth.Constants.FeedFragmentHomeCommand;
import static com.concepttech.campingcompanionbluetooth.Constants.GetDatabaseLocationDataString;
import static com.concepttech.campingcompanionbluetooth.Constants.GetDatabaseLocationString;
import static com.concepttech.campingcompanionbluetooth.Constants.PhotoCountTag;
import static com.concepttech.campingcompanionbluetooth.Constants.PhotoName;
import static com.concepttech.campingcompanionbluetooth.Constants.PlacesJSONIDTag;
import static com.concepttech.campingcompanionbluetooth.Constants.PlacesJSONResultsTag;
import static com.concepttech.campingcompanionbluetooth.Constants.PlacesKeyTag;
import static com.concepttech.campingcompanionbluetooth.Constants.PlacesLocationTag;
import static com.concepttech.campingcompanionbluetooth.Constants.PlacesRadiusTag;
import static com.concepttech.campingcompanionbluetooth.Constants.PlacesURL;


public class FeedFragment extends Fragment implements View.OnClickListener{
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2", TAG = "FeedFragment";
    private View view;
    private String mParam1;
    private String mParam2;
    private FeedListAdapter feedListAdapter;
    private FeedFragmentCallback mListener;
    private ValueEventListener FeedListener, LocationListener;
    DatabaseReference locationdatareference;
    private Query FeedReference;
    private DeviceState deviceState;
    private Context context;
    private String Country, AdminArea, Locality, LocationName,LastEntryKey;
    private ArrayList<String> Locations = new ArrayList<>();
    private ArrayList<TimeStamp> TimeStamps = new ArrayList<>();
    private boolean ListenerSet = false, PromptedForLocation = false, GrantedLocation = false, ListenerSuccess = false, FragmentUnloaded = false,
    Downloading = false;
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
    public void onClick(View v){
        switch (v.getId()){
            case R.id.FeedBackButton:
                FragmentUnloaded = true;
                mListener.FeedFragmentCallback(FeedFragmentHomeCommand,null);
                break;
            case R.id.FeedCameraButton:
                FragmentUnloaded = true;
                mListener.FeedFragmentCallback(FeedCameraCommand,
                        new String[]{Country,AdminArea,Locality,LocationName,Integer.toString(PhotoCount+1)});
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
        view = inflater.inflate(R.layout.fragment_feed_fragrment, container, false);
        User = FirebaseAuth.getInstance().getCurrentUser();
        if(User == null){
            SignInDialog();
        }else {
            Initialize();
        }
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
    public void onResume(){
        super.onResume();
        if(FragmentUnloaded) {
            ((TextView)view.findViewById(R.id.FeedHeader)).setText(LocationName);
            GetLocationData();
            Initialize();
            if (feedListAdapter != null) feedListAdapter.notifyDataSetChanged();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {getLocation();
                } else {
                    Toast.makeText(context, "Permission denied to ACCESS_FINE_LOCATION", Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                }
                break;
        }
    }
    private void Initialize(){
        if (feedListAdapter == null) {
            feedListAdapter = new FeedListAdapter(getContext());
        }
        ((ListView)view.findViewById(R.id.FeedListView)).setAdapter(feedListAdapter);
        view.findViewById(R.id.FeedCameraButton).setOnClickListener(this);
        view.findViewById(R.id.FeedBackButton).setOnClickListener(this);
        if(!ListenerSuccess) getLocation();
        else GetLocationData();
    }
    private void CreateAccountDialog(){
        if(getActivity() != null) {
            final Dialog dialog = new Dialog(getActivity());
            dialog.setContentView(R.layout.create_account_dialog_layout);
            dialog.setTitle("Authenticate");
            Button authenticatebutton = dialog.findViewById(R.id.NewAccountDialogAuthenticate);
            Button cancelbutton = dialog.findViewById(R.id.NewAccountDialogCancel);
            authenticatebutton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    EditText usernameedittext = dialog.findViewById(R.id.NewAccountDialogUsernameEditText);
                    EditText passwordedittext = dialog.findViewById(R.id.NewAccountDialogPasswordEditText);
                    EditText confirmpasswordedittext = dialog.findViewById(R.id.NewAccountDialogConfirmPasswordEditText);
                    String username = usernameedittext.getText().toString();
                    String password = passwordedittext.getText().toString();
                    String confirmpassword = confirmpasswordedittext.getText().toString();
                    if (password.equals(confirmpassword)) {
                        FirebaseAuth.getInstance().createUserWithEmailAndPassword(username, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                dialog.dismiss();
                                if (task.isSuccessful()) {
                                    User = FirebaseAuth.getInstance().getCurrentUser();
                                    Initialize();
                                } else {
                                    CreateAccountDialog();
                                    Log.d(TAG, "Error auth failed " + task.getResult());
                                }
                            }
                        });
                    }
                    dialog.dismiss();

                }
            });
            cancelbutton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    mListener.FeedFragmentCallback(FeedFragmentHomeCommand, null);
                }
            });
            dialog.show();
        }
    }
    private void SignInDialog(){
        if(getActivity() != null) {
            final Dialog dialog = new Dialog(getActivity());
            dialog.setContentView(R.layout.sign_in_dialog_layout);
            dialog.setTitle("Authenticate");
            Button authenticatebutton = dialog.findViewById(R.id.LoginDialogAuthenticate);
            Button cancelbutton = dialog.findViewById(R.id.LoginDialogCancel);
            authenticatebutton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    EditText usernameedittext = (EditText) dialog.findViewById(R.id.LoginDialogUsernameEditText);
                    EditText passwordedittext = (EditText) dialog.findViewById(R.id.LoginDialogPasswordEditText);
                    String username = usernameedittext.getText().toString();
                    String password = passwordedittext.getText().toString();
                    if(!username.isEmpty() && !password.isEmpty()) {
                        AuthCredential credential = EmailAuthProvider
                                .getCredential(username, password);
                        FirebaseAuth.getInstance().signInWithCredential(credential)
                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        dialog.dismiss();
                                        if (task.isSuccessful()) {
                                            Initialize();
                                        } else {
                                            Toast.makeText(context, "Could not sign in", Toast.LENGTH_LONG).show();
                                            SignInDialog();
                                            Log.d(TAG, "Error auth failed");
                                        }
                                    }
                                });
                        dialog.dismiss();
                    }else
                        Toast.makeText(context, "Could not sign in", Toast.LENGTH_LONG).show();

                }
            });
            cancelbutton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    mListener.FeedFragmentCallback(FeedFragmentHomeCommand, null);
                }
            });
            (dialog.findViewById(R.id.SignInDialogNewAccountText)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    CreateAccountDialog();
                }
            });
            dialog.show();
        }
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
                    if(addresses.get(0).getFeatureName() != null){
                        if(Constants.isNumber(addresses.get(0).getFeatureName())){
                            ((TextView)view.findViewById(R.id.FeedHeader)).setText(addresses.get(0).getAddressLine(0));
                            LocationName = addresses.get(0).getAddressLine(0).replace(" ","");
                        }
                        else {
                            LocationName = addresses.get(0).getFeatureName().replace(" ","");
                            ((TextView)view.findViewById(R.id.FeedHeader)).setText(addresses.get(0).getFeatureName());
                        }
                        Toast.makeText(context, "Found: " + LocationName, Toast.LENGTH_LONG).show();
                    }
                    GetLocationData();
                }
            } catch (SecurityException e) {
                Log.e(TAG, "Exception getting location");
            } catch (Exception e) {
                Log.e(TAG, "Exception getting city: " + e.getMessage());
            }
        }
    }
    private void PromptForLocationUse(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Cannot detect PEBL GPS, would you like to use device GPS instead");
        builder.setPositiveButton("Yes", new Dialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                GrantedLocation = true;
                getLocation();
            }
        });
        builder.setNegativeButton("No", new Dialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                GrantedLocation = false;
                getLocation();
            }
        });
        PromptedForLocation = true;
        builder.show();
    }
    private void getLocation(){
        try {
            String key = getResources().getString(R.string.Key);
            String url;
            if(deviceState.getLongitude() != 0 && deviceState.getLatitude() != 0) {
                url = PlacesURL + PlacesLocationTag + deviceState.getLatitude() + "," + deviceState.getLongitude() +
                        PlacesRadiusTag + 5 + PlacesKeyTag + key;
                URL Url = new URL(url);
                HttpsURLConnection connection = (HttpsURLConnection) Url.openConnection();
                connection.setDoInput(true);
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                connection.connect();
                try {
                    JSONObject jsonObject = new JSONObject(connection.getResponseMessage());
                    JSONArray jsonArray = jsonObject.getJSONArray(PlacesJSONResultsTag);
                    String id = null;
                    for (int i = 0; i < 1; i++) {
                        jsonObject = jsonArray.getJSONObject(i);
                        if (jsonObject.has(PlacesJSONIDTag))
                            id = jsonObject.getString(PlacesJSONIDTag);
                    }
                    if (id != null) {
                        getAddressFromID(id);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing JSON: " + e.getMessage());
                } finally {
                    connection.disconnect();
                }
                GetLocationData();
            }
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
                        mListener.FeedFragmentCallback(FeedFragmentHomeCommand,null);
                        return;
                    }
                }else {
                    if(!PromptedForLocation) {
                        PromptForLocationUse();
                        return;
                    }else if(GrantedLocation) getDeviceLocation();
                    return;
                }
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
                                if(Constants.isNumber(address.getFeatureName())){
                                    ((TextView)view.findViewById(R.id.FeedHeader)).setText(address.getAddressLine(0));
                                    LocationName = address.getAddressLine(0);
                                }
                                else {
                                    LocationName = address.getFeatureName();
                                    ((TextView)view.findViewById(R.id.FeedHeader)).setText(address.getFeatureName());
                                }
                                Toast.makeText(context, "Found: " + LocationName, Toast.LENGTH_LONG).show();
                                GetLocationData();
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
        if(locationdatareference == null) {
            LocationListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    ListenerSuccess = true;
                    if (dataSnapshot != null && dataSnapshot.hasChildren()) {
                        for (DataSnapshot child : dataSnapshot.getChildren()) {
                            switch (child.getKey()) {
                                case PhotoCountTag:
                                    if (child.getValue() != null)
                                        PhotoCount = Integer.parseInt(child.getValue().toString());
                                    break;
                            }
                        }
                    }
                    locationdatareference.removeEventListener(this);
                    locationdatareference = null;
                    LocationListener = null;
                    if (!ListenerSet && PhotoCount > 0) GetEntries();
                    else{
                        feedListAdapter.TimeStamps.add(new TimeStamp());
                        feedListAdapter.notifyDataSetChanged();
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                    ListenerSet = false;
                    locationdatareference.removeEventListener(this);
                    LocationListener = null;
                    locationdatareference = null;
                }
            };
            locationdatareference =
                    FirebaseDatabase.getInstance().getReference(GetDatabaseLocationDataString(Country, AdminArea, Locality, LocationName));
            locationdatareference.addListenerForSingleValueEvent(LocationListener);
            StartTimer();
        }
    }
    private void GetEntries(){
        if(FeedListener == null){
            if(LastEntryKey != null && LastEntryKey.length() > 0) FeedReference =
                    FirebaseDatabase.getInstance().getReference(GetDatabaseLocationString(Country,AdminArea,Locality,LocationName)).limitToFirst(20);
            else FeedReference =
                    FirebaseDatabase.getInstance().getReference(GetDatabaseLocationString(Country,AdminArea,Locality,LocationName)).limitToFirst(20).startAt(LastEntryKey);
            FeedReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    FeedListener = this;
                    if(dataSnapshot != null && dataSnapshot.hasChildren()){
                        for (DataSnapshot child : dataSnapshot.getChildren()) {
                            if(child.hasChildren()) {
                                if (child.getKey() != null && !child.getKey().equals("LocationData")) {
                                    if (!Locations.contains(child.getKey()))
                                        Locations.add(child.getKey());
                                    TimeStamp temp = new TimeStamp();
                                    temp.buildFromSnapshot(child);
                                    if (!TimeStamps.contains(temp)) TimeStamps.add(temp);
                                }
                            }
                            if(Locations.size() > TimeStamps.size()) Locations.remove(Locations.size() - 1);
                            else if(Locations.size() < TimeStamps.size()) TimeStamps.remove(TimeStamps.size() - 1);
                            LastEntryKey = child.getKey();
                        }
                    }
                    if(TimeStamps.size() > 0) {
                        feedListAdapter.SetTimeStamps(TimeStamps);
                        SortEntries();
                        CheckPhotos();
                    }
                    if(!Downloading) feedListAdapter.notifyDataSetChanged();
                    FeedReference.removeEventListener(FeedListener);
                    FeedReference = null;
                    FeedListener = null;
                    ListenerSet = false;
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                    ListenerSet = false;
                }
            });
            ListenerSet = true;
        }
    }
    private void SortEntries(){
        for(int oi = 0; oi<TimeStamps.size();oi++)
        for(int i = 0;i<TimeStamps.size()-1;i++){
            if(TimeStamps.get(i).isOlderThan(TimeStamps.get(i+1))){
                TimeStamp temp = TimeStamps.get(i);
                String loctemp = Locations.get(i);
                TimeStamps.set(i,TimeStamps.get(i+1));
                TimeStamps.set(i+1,temp);
                Locations.set(i,Locations.get(i+1));
                Locations.set(i+1,loctemp);
            }
        }
    }
    private void CheckPhotos(){
        if(Locations.size()>0){
            File SaveDirectory = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES);
            if(SaveDirectory!=null)
            for(int i = 0;i<Locations.size();i++){
                File temp = new File(SaveDirectory.getPath() + "/" + GetDatabaseLocationString(Country,AdminArea,Locality,LocationName)
                        + "/" + Locations.get(i) + "/" + PhotoName);
                if(temp.exists()) feedListAdapter.SetPictureFile(temp,i);
                else DownloadFile(temp, GetDatabaseLocationString(Country,AdminArea,Locality,LocationName)
                        + "/" + Locations.get(i) + "/" + PhotoName);
            }
        }
    }
    private void DownloadFile(File file, String path){
        if(!Downloading){
            Downloading = true;
            manageFiles(file);
            FirebaseStorage.getInstance().getReference(path).getFile(file).addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task) {
                    Downloading = false;
                    GetLocationData();
                }
            });
        }
    }
    private void manageFiles(File file){
        if(file != null) {
            try {
                if (file.exists()) {
                    Log.d(TAG, "Save file exists");
                    boolean result = file.delete();
                    if (result) {
                        Log.d(TAG, "Deleted Existing save file");
                        boolean created = file.createNewFile();
                        if (created) Log.d(TAG, "New File Created");
                        else Log.d(TAG, "Error Creating new file");

                    } else Log.d(TAG, "Error Deleting Existing save file");
                } else {
                    Log.d(TAG, "Save file does not exist");
                    if(!file.getParentFile().exists()){
                        boolean direscreated = file.getParentFile().mkdirs();
                        if (direscreated) Log.d(TAG, "Created Save Parent directory");
                        else Log.d(TAG, "Error Creating Save Parent directory");
                    }
                    boolean created = file.createNewFile();
                    if (created) Log.d(TAG, "New File Created");
                    else Log.d(TAG, "Error Creating new file");
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }
    private void StartTimer(){
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if(!ListenerSuccess){
                    if(LocationListener != null && locationdatareference != null)
                        locationdatareference.removeEventListener(LocationListener);
                    SignInDialog();
                }
                this.cancel();
            }
        },10000,10000);
    }
    public interface FeedFragmentCallback {
        // TODO: Update argument type and name
        void FeedFragmentCallback(String Command, String[] extras);
    }
}
