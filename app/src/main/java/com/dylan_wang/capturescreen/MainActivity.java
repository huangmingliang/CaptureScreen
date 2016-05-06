package com.dylan_wang.capturescreen;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class MainActivity extends AppCompatActivity {
    private String TAG = "CaptureScreenService";
    private int result = 0;
    private Intent intent = null;
    private int REQUEST_MEDIA_PROJECTION = 1;
    private MediaProjectionManager mMediaProjectionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMediaProjectionManager = (MediaProjectionManager)getApplication().getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        startIntent();
    }

    private void startIntent(){
        if(intent != null && result != 0){
            Log.i(TAG, "user agree the application to capture screen");
            ((CaptureScreenApplication)getApplication()).setResult(result);
            ((CaptureScreenApplication)getApplication()).setIntent(intent);
            Intent intent = new Intent(getApplicationContext(), CaptureScreenService.class);
            startService(intent);
            Log.i(TAG, "start service CaptureScreenService");
        }else{
            startActivityForResult(mMediaProjectionManager.createScreenCaptureIntent(), REQUEST_MEDIA_PROJECTION);
            ((CaptureScreenApplication)getApplication()).setMediaProjectionManager(mMediaProjectionManager);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_MEDIA_PROJECTION) {
            if (resultCode != Activity.RESULT_OK) {
                return;
            }else if(data != null && resultCode != 0){
                Log.i(TAG, "user agree the application to capture screen");
                result = resultCode;
                intent = data;
                ((CaptureScreenApplication)getApplication()).setResult(resultCode);
                ((CaptureScreenApplication)getApplication()).setIntent(data);
                Intent intent = new Intent(getApplicationContext(), CaptureScreenService.class);
                startService(intent);
                Log.i(TAG, "start service CaptureScreenService");
                finish();
            }
        }
    }
}
