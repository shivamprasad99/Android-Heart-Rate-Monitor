package com.example.hp.cameracv2;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;

public class CustomCameraView extends JavaCameraView{
    public CustomCameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public void setPreviewFPS(double min, double max){
        Camera.Parameters params = mCamera.getParameters();
        params.setPreviewFpsRange((int)(min*1000), (int)(max*1000));
        mCamera.setParameters(params);
    }
}
