package com.danielchoi.simon;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener{

    static final int activityRef = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);


        Typeface tf = Typeface.createFromAsset(getAssets(),  "fonts/digitaldismay.otf");
        ImageButton ib = (ImageButton) findViewById(R.id.start_imageButton);
        ib.setOnClickListener(this);

        Button b = (Button) findViewById(R.id.about_button);
        b.setTypeface(tf);
        b.setOnClickListener(this);

        Button s = (Button) findViewById(R.id.score_button);
        s.setTypeface(tf);
        s.setOnClickListener(this);


    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.start_imageButton){
            Vibrator vb = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            vb.vibrate(10);
            Intent startIntent = new Intent(getApplicationContext(), GameActivity.class);
            startActivity(startIntent);
        }else if(view.getId() == R.id.score_button){
            Vibrator vb = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            vb.vibrate(10);
            Intent scoreIntent = new Intent(getApplicationContext(), ScoreActivity.class);
            scoreIntent.putExtra("calling-Activity", activityRef);
            startActivity(scoreIntent);
        }else{
            Vibrator vb = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            vb.vibrate(10);
            Intent aboutIntent = new Intent(getApplicationContext(), AboutActivity.class);
            startActivity(aboutIntent);
        }
    }
}
