package com.danielchoi.simon;

import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import java.util.Random;
import java.util.Vector;

public class HomeActivity extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        ImageButton ib = (ImageButton) findViewById(R.id.start_imageButton);
        if(ib != null){
            ib.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View v){
                    Vibrator vb = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    vb.vibrate(10);
                    Intent startIntent = new Intent(getApplicationContext(), GameActivity.class);
                    startActivity(startIntent);
                }
            });
        }

    }

}
