package com.concepttech.campingcompanionbluetooth;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import static android.content.ContentValues.TAG;

public class CameraActivity extends Activity {
    private Camera mCamera;
    private CameraPreview mPreview;
    private String MainLocation = "", timepath;
    private boolean set_up_done = false;
    private Context context;
    private boolean released=false,intentHandled=false;
    private int count=0, Rotation = 0, FrontCameraID;
    private File SaveDirectory, SaveFile;
    private Context view_context=null;
    private Button video_button;
    private Bundle savedInstanceState;
    private void connectCamera(){
        mCamera = Camera.open(FrontCameraID);
    }
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.savedInstanceState = savedInstanceState;
        setContentView(R.layout.activity_camera);
        context = getApplicationContext();
        if (context != null)
            SaveDirectory = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES);
        else finish();
        if(!checkCameraPermission()){
            ActivityCompat.requestPermissions(CameraActivity.this,
                    new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    1);
        }else Initialize();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Initialize();
                } else {
                    Toast.makeText(context, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }
    }
    private void Initialize(){
        check_storage();
        FrontCameraID = findFrontFacingCamera();
        handleIntent(savedInstanceState);
        view_context = this;
        setSaveFile();
        setCamera();
        set_preview();
        video_button = findViewById(R.id.video_record_button);
        video_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePicture();
                displayKeepDialog();
            }
        });
    }
    private boolean checkCameraPermission()
    {
        String permission = Manifest.permission.CAMERA;
        int res = context.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }
    private void setSaveFile(){
        SaveFile = new File(SaveDirectory + "/" + MainLocation);
        manageFiles();
    }
    private void handleIntent(Bundle savedInstanceState){
        if(!intentHandled) {
            intentHandled = true;
            if (savedInstanceState == null) {
                Bundle extras = getIntent().getExtras();
                if (extras == null) {
                    MainLocation = null;
                } else {
                    if (getIntent().hasExtra("MainLocation")) {
                        MainLocation = extras.getString("MainLocation");
                        Log.d(TAG,MainLocation);
                    }
                    if (getIntent().hasExtra("TimeLocation")) {
                        MainLocation = extras.getString("MainLocation");
                        Log.d(TAG,MainLocation);
                    }
                }
            } else {
                if (getIntent().hasExtra("MainLocation")) {
                    MainLocation = (String) savedInstanceState.getSerializable("MainLocation");
                    Log.d(TAG,MainLocation);
                }
            }
        }
    }
    public void displayKeepDialog() {
        String dialogtitle;
        mCamera.release();
        dialogtitle = "Keep Photo?";
        AlertDialog.Builder builder = new AlertDialog.Builder(CameraActivity.this);
        builder.setCancelable(false);
        builder.setTitle(dialogtitle);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do something when click positive button
                Log.d(TAG,"yes pressed");
                Upload();

            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(TAG,"no pressed");
                boolean delete = SaveFile.delete();
                if (!delete) Log.d(TAG, "delete failed");
                video_button.setVisibility(View.VISIBLE);
                mCamera.startPreview();
                dialog.dismiss();
            }
        });
        String viewtext = "ViewPhoto";
        builder.setNeutralButton(viewtext, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                final AlertDialog.Builder builder = new AlertDialog.Builder(view_context);
                LayoutInflater inflater = getLayoutInflater();
                builder.setView(inflater.inflate(R.layout.picture_dialog,null))
                    .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            displayKeepDialog();
                        }
                    });
                final AlertDialog viewdialog = builder.create();
                viewdialog.show();
                viewdialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(false);
                viewdialog.getButton(AlertDialog.BUTTON_NEUTRAL).setEnabled(false);
                ImageView imageView = (ImageView) viewdialog.findViewById(R.id.ViewDialogImageView);
                imageView.setImageURI(Uri.fromFile(SaveFile));
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    public void set_preview(){
        if (!set_up_done && mCamera != null) {
            mPreview = new CameraPreview(getApplicationContext(), mCamera);
            FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
            preview.addView(mPreview);
            set_up_done = true;
        }
    }
    private void manageFiles(){
        if(SaveFile != null) {
            try {
                if (SaveFile.exists()) {
                    Log.d(TAG, "Save file exists");
                    boolean result = SaveFile.delete();
                    if (result) {
                        Log.d(TAG, "Deleted Existing save file");
                        boolean created = SaveFile.createNewFile();
                        if (created) Log.d(TAG, "New File Created");
                        else Log.d(TAG, "Error Creating new file");

                    } else Log.d(TAG, "Error Deleting Existing save file");
                } else {
                    Log.d(TAG, "Save file does not exist");
                    if(!SaveFile.getParentFile().exists()){
                        boolean direscreated = SaveFile.getParentFile().mkdirs();
                        if (direscreated) Log.d(TAG, "Created Save Parent directory");
                        else Log.d(TAG, "Error Creating Save Parent directory");
                    }
                    boolean created = SaveFile.createNewFile();
                    if (created) Log.d(TAG, "New File Created");
                    else Log.d(TAG, "Error Creating new file");
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }
    private void Upload() {
        StorageReference storageReference;
        final DatabaseReference TimeStampReference = FirebaseDatabase.getInstance().getReference(MainLocation);
        Log.d(TAG,"TimeStamp Reference: " + TimeStampReference.getRef());

        Uri file = Uri.fromFile(SaveFile);
        final UploadTask uploadTask;
        storageReference = FirebaseStorage.getInstance().getReference(MainLocation);
        uploadTask = storageReference.putFile(file);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                Log.d(TAG, "media success");
                TimeStampReference.setValue(new TimeStamp().toMap());
                uploadTask.removeOnSuccessListener(this);
            }
        });
    }
    private void takePicture(){
        if(mCamera != null) {
            mCamera.takePicture(null, null, mPicture);
        }
    }
    private void rotateImage(String file) throws IOException {
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file, bounds);
        BitmapFactory.Options opts = new BitmapFactory.Options();
        Bitmap bm = BitmapFactory.decodeFile(file, opts);
        Matrix matrix = new Matrix();
        matrix.postRotate(-1*Rotation, (float) bm.getWidth() / 2, (float) bm.getHeight() / 2);
        Bitmap rotatedBitmap = Bitmap.createBitmap(bm, 0, 0, bounds.outWidth, bounds.outHeight, matrix, true);
        FileOutputStream fos=new FileOutputStream(file);
        rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        fos.flush();
        fos.close();
    }
    private void setCamera(){
        try {
            mCamera = Camera.open(FrontCameraID);
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_FRONT, info);
            int degrees = 0;
            int rotation = this.getWindowManager().getDefaultDisplay().getRotation();
            switch (rotation) {
                case Surface.ROTATION_0: degrees = 0; break; //Natural orientation
                case Surface.ROTATION_90: degrees = 90; break; //Landscape left
                case Surface.ROTATION_180: degrees = 180; break;//Upside down
                case Surface.ROTATION_270: degrees = 270; break;//Landscape right
            }
            Rotation = (360 - info.orientation + degrees);
            mCamera.setDisplayOrientation(Rotation);
        }catch (Exception e){
            if (mCamera == null) {
                Log.d(TAG,"CameraActivity:Camera not set");
                while(!released&&count<4) {
                    try {
                        count++;
                        mCamera = Camera.open();
                        mCamera.release();
                        released = true;
                        connectCamera();
                        Log.d(TAG, "CameraActivity:Camera not set:camera released");
                    } catch (Exception ne) {
                        Log.d(TAG, "did not release");
                    }
                }
            }
        }
    }
    public static int findFrontFacingCamera() {

        // Search for the front facing camera
        int cameraId = 0;
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                Log.d(TAG,"Camera_Activity:Camera.open:findfrontcamera:found:"+i);
                cameraId = i;
                break;
            }
        }
        return cameraId;
    }
    public void check_storage(){
        if(SaveDirectory != null) {
            File mediaStorageDir = SaveDirectory;
            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    Log.d(TAG, "failed to create directory");
                }
            }
        }
    }
    private Camera.PictureCallback mPicture = new Camera.PictureCallback(){
        @Override
        public void onPictureTaken(byte[] data, Camera camera){
            if(SaveFile == null){
                Log.d("TEST", "Error creating media file, check storage permissions");
                return;
            }
            try{
                FileOutputStream fos = new FileOutputStream(SaveFile);
                fos.write(data);
                fos.close();
                rotateImage(SaveFile.getCanonicalPath());
                displayKeepDialog();
            }catch(FileNotFoundException e){
                Log.d("TEST","File not found: "+e.getMessage());
            } catch (IOException e){
                Log.d("TEST","Error accessing file: "+e.getMessage());
            }
        }
    };
}
