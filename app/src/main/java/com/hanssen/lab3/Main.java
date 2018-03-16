package com.hanssen.lab3;

import android.app.Activity;
import android.os.Bundle;

public class Main extends Activity {

    Layout canvas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        canvas = new Layout(this);
        setContentView(canvas);
    }

    @Override
    public void onResume() {
        super.onResume();
        canvas.resume();
    }


    @Override
    public void onPause() {
        super.onPause();
        canvas.pause();
    }
}
