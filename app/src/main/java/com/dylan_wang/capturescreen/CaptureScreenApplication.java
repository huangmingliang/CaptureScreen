package com.dylan_wang.capturescreen;

import android.app.Application;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;

/**
 * Created by huangmingliang on 2016/5/6.
 */
public class CaptureScreenApplication extends Application {
    private int result;
    private Intent intent;
    private MediaProjectionManager mMediaProjectionManager;

    public int getResult(){
        return result;
    }

    public Intent getIntent(){
        return intent;
    }

    public MediaProjectionManager getMediaProjectionManager(){
        return mMediaProjectionManager;
    }

    public void setResult(int result1){
        this.result = result1;
    }

    public void setIntent(Intent intent1){
        this.intent = intent1;
    }

    public void setMediaProjectionManager(MediaProjectionManager mMediaProjectionManager){
        this.mMediaProjectionManager = mMediaProjectionManager;
    }
}
