package com.danielchoi.simon;

import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        Typeface tf = Typeface.createFromAsset(getAssets(),  "fonts/digitaldismay.otf");
        TextView tv = (TextView) findViewById(R.id.developers);
        tv.setTypeface(tf);
    }
}
