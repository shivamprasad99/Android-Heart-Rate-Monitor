package com.example.hp.cameracv2;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;


public class Display extends AppCompatActivity {
    double hr;
    public Display(){
        hr=0;
    }
    public Display(double x){
        this.hr=x;
    }


    TextView textView;
    public void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_display);
        textView=(TextView) findViewById(R.id.textView);
        textView.setText("HR="+hr);
    }


}