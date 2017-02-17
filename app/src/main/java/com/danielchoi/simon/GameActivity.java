package com.danielchoi.simon;

import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.Random;
import java.util.Vector;

public class GameActivity extends AppCompatActivity
        implements View.OnClickListener{

    // Private Global Values
    private int count = 1;
    Vector<Integer> userPattern = new Vector<>();
    // Color button ids
    private int[] colors = new int[]{R.id.blue_imageButton, R.id.green_imageButton, R.id.red_imageButton, R.id.yellow_imageButton};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        //try to use this font.
        TextView tx = (TextView)findViewById(R.id.score_textView);
        Typeface custom_font = Typeface.createFromAsset(getAssets(),  "fonts/digitaldismay.otf");

        tx.setTypeface(custom_font);
        Button b = (Button) findViewById(R.id.play_button);
        b.setOnClickListener(this);
        for(int id : colors){
            ImageButton ib = (ImageButton) findViewById(id);
            ib.setOnClickListener(this);
        }

        simonsTurn();
        usersTurn();
    }

    // Function that shows simon's pattern for the user to repeat
    private void simonsTurn() {


        lockButtons(); // Stop user from clicking buttons until simon is finished
        // Call Random Color ID
        int colorId = setPattern();
        Vector<Integer> simonPattern = new Vector<>();
        simonPattern.add(colorId);
        unLockButtons();
    }//Called from onCreate


    // Function that allows user to repeat simon's pattern
    private void usersTurn(){
        // Compare user pattern to simon's

    }//Called from onCreate

    // Function that makes simon choose random colors
    private int setPattern(){
        int id = R.id.blue_imageButton;
        Random rand = new Random();
        int index = rand.nextInt(5);
        for(int i=0; i<count; i++){
            if(i == index){
                colors[i] = id;
            }
        }
        count++;
        return id;
    }//Called from simonsTurn

    // Function to stop user from clicking buttons while simon is active
    private void lockButtons(){
        for(int id : colors){
            ImageButton ib = (ImageButton) findViewById(id);
            ib.setClickable(false);
            ib.setLongClickable(false);
        }
    }
    // Function to let user click buttons when simon is inactive
    private void unLockButtons(){
        for(int id : colors){
            ImageButton ib = (ImageButton) findViewById(id);
            ib.setClickable(true);
            ib.setLongClickable(false);
        }
    }//Called from simonsTurn

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.play_button){
            simonsTurn();

        }
        // Adds a color to the users pattern until count matches simon
        for(int i=0; i<count; i++) {
            for (int ids : colors) {
                if (view.getId() == ids){
                    int colorId = view.getId();
                    userPattern.add(colorId);
                    usersTurn();
                }
            }
        }

    }
}
