package com.danielchoi.simon;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.Random;
import java.util.Vector;

public class HomeActivity extends AppCompatActivity {
    // Private Global Values
    private int count = 1;
    // Use this When Layout is finished:
    // private int[] colors = new int[]{R.id.blue, R.id.green, R.id.red, R.id.yellow};
    private int[] colors = new int[]{R.id.textView, R.id.textView, R.id.textView, R.id.textView};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);


        Vector<Integer> userPattern = new Vector<>();
        displaySimon();
    }

    private void displaySimon() {
        // Call Random Color ID
        int colorId = setPattern();
        Vector<Integer> simonPattern = new Vector<>();
        simonPattern.add(colorId);

    }
    private int setPattern(){
        int id = R.id.textView; // Change after layout is complete
        Random rand = new Random();
        int index = rand.nextInt(5);
        for(int i=0; i<count; i++){
            if(i == index){
                colors[i] = id;
            }
        }
        count++;
        return id;
    }
}
