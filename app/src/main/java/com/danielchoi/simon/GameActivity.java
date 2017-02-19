package com.danielchoi.simon;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

public class GameActivity extends AppCompatActivity
        implements View.OnClickListener,PopupMenu.OnMenuItemClickListener{

    //***********************************************************DECLARE*VARIABLES*
    Vector<Integer> userPattern,simonPattern;
    private int gameMode, count, score, flashSpeed;
    private int colorButtons[], colorDrawable[], pressedDrawable[], soundID[];
    private FlashSimon flash;
    private FlashPressed fPressed;
    private CountDown countDown;
    private SoundPool soundPool;
    private Set<Integer> soundsLoaded;
    public Typeface customFont;
    public TextView scoreTextView;
    public Vibrator vb;
    public ImageButton gButton, bButton, yButton, rButton;

    //*********************************************************Initialize*Variables*

    private void setVariables(){
        count = 1;
        score = 0;
        simonPattern = new Vector<>();
        userPattern = new Vector<>();
        flashSpeed = 1000;

        vb = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        colorButtons = new int[]{R.id.green_imageButton, R.id.blue_imageButton, R.id.yellow_imageButton, R.id.red_imageButton}; // Color button ids
        colorDrawable = new int[]{R.drawable.green,R.drawable.blue,R.drawable.yellow,R.drawable.red};
        pressedDrawable = new int[]{R.drawable.greenpressed ,R.drawable.bluepressed,R.drawable.yellowpressed,R.drawable.redpressed};
        soundID = new int []{soundPool.load(this,R.raw.greennote,1), soundPool.load(this,R.raw.bluenote,1), soundPool.load(this,R.raw.yellownote,1),soundPool.load(this,R.raw.rednote,1),soundPool.load(this,R.raw.countdown,1),soundPool.load(this,R.raw.go,1)};

        customFont= Typeface.createFromAsset(getAssets(),  "fonts/digitaldismay.otf");

        scoreTextView = (TextView)findViewById(R.id.score_textView);
        scoreTextView.setTypeface(customFont);

        gButton = (ImageButton) findViewById(R.id.green_imageButton);
        gButton.setOnClickListener(this);
        bButton = (ImageButton) findViewById(R.id.blue_imageButton);
        bButton.setOnClickListener(this);
        yButton = (ImageButton) findViewById(R.id.yellow_imageButton);
        yButton.setOnClickListener(this);
        rButton = (ImageButton) findViewById(R.id.red_imageButton);
        rButton.setOnClickListener(this);


    }//Initialize Variables.

    //******************************************************************LIFECYCLES*
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        soundsLoaded = new HashSet<Integer>();
    }

    @Override
    protected void onPause(){
        super.onPause();
        if(flash != null){
            flash.cancel(true);
            flash = null;
        }
        if(soundPool != null){
            soundPool.release();
            soundPool = null;
            soundsLoaded.clear();
        }

    }

    @Override
    protected void onResume(){
        super.onResume();

        AudioAttributes.Builder attrBuilder = new AudioAttributes.Builder();
        attrBuilder.setUsage(AudioAttributes.USAGE_GAME);

        final SoundPool.Builder spBuilder = new SoundPool.Builder();
        spBuilder.setAudioAttributes(attrBuilder.build());
        spBuilder.setMaxStreams(2);
        soundPool = spBuilder.build();
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                if(status == 0){
                    soundsLoaded.add(sampleId);
                    Log.i("SOUND", "Sound loaded = "+sampleId);
                }else{
                    Log.i("SOUND", "Error cannot load sound status = "+status);
                }

            }
        });

        chooseGameMode();

    }

    //***********************************************************************SIMON*

    private int addPattern(){// Function that makes simon choose random colors
        Random rand = new Random();
        int index = rand.nextInt(4);
        count++;
        Log.i("Random: ", ""+index);
        return index;
    }//Called from simonsTurn

    private void chooseGameMode(){
        if(gameMode == 2){
            setVariables();
            toast("Get Ready, Game Mode II");
            countDown = new CountDown();
            countDown.execute();
            play();
        }else if(gameMode ==3){
            setVariables();
            toast("Get Ready, Game Mode III");
            countDown = new CountDown();
            countDown.execute();
            play();
        }else{
            setVariables();
            toast("Get Ready, Game Mode I");
            countDown = new CountDown();
            countDown.execute();
            play();
        }
    }//Called from onCreate & Menu

    private void lockButtons(){// Function to stop user from clicking buttons while simon is active
        for(int id : colorButtons){
            ImageButton ib = (ImageButton) findViewById(id);
            ib.setClickable(false);
        }
    }//Called by simonsTurn

    private void play(){
        simonsTurn();
    }//Called by onCreate

    private void simonsTurn() {
        //toast("Simon's Turn");
        lockButtons(); // Stop user from clicking buttons until simon is finished
        simonPattern.add(addPattern());//add a random pattern
        flash = new FlashSimon();
        flash.execute();
    }//Called from onCreate

    private void unLockButtons(){// Function to let user click buttons when simon is inactive
        for(int id : colorButtons){
            ImageButton ib = (ImageButton) findViewById(id);
            ib.setClickable(true);
        }
    }//Called from userTurn

    private void updateScore(){
        String scoreString;
        if(score<10) scoreString = "0"+score;
        else scoreString = String.valueOf(score);

        scoreTextView.setText(scoreString);
    }

    private void usersTurn(){
        unLockButtons();

    }//Called from FLASH after thread Completes

    //*********************************************************************THREADS*

    class FlashSimon extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... voids) {

            for(int i = 0; i<simonPattern.size(); i++){
                final int y = simonPattern.get(i);
                try {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ImageButton flash = (ImageButton) findViewById(colorButtons[y]);
                        flash.setImageResource(pressedDrawable[y]);
                    }
                });

                if(soundsLoaded.contains(soundID[y])){
                    soundPool.play(soundID[y],1.0f, 1.0f, 0, 0, 1.0f);
                }
                Thread.sleep(flashSpeed);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                    ImageButton flash = (ImageButton) findViewById(colorButtons[y]);
                    flash.setImageResource(colorDrawable[y]);
                    }
                });

            } catch (InterruptedException e) {
                Log.i("THREAD=====","FLASH was interupted");
            }

            }//for


            return null;
        }//doiInBackground

        @Override
        protected void onPostExecute(Void aVoid) {
            toast("Your turn");
            usersTurn();
        }

    } //Flashes Simon's Pattern

    class FlashPressed extends AsyncTask<Void, Void, Void>{

        final int bPressed;
        FlashPressed(int x){
            bPressed = x;
        }
        @Override
        protected Void doInBackground(Void... voids) {


            return null;
        }
    }//Called when button pressed

    class CountDown extends AsyncTask<Void, String, Void>{

        int cdSound = soundID[4];
        int time = 1000;

        @Override
        protected void onPreExecute() {
            try {
                Thread.sleep(1250);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            String display;
            try {
                for(int i = 3; i>=0; i--){
                    if (i == 0) {
                        display = "go";
                        cdSound = soundID[5];
                    } else display = "0" + i;

                    publishProgress(display);
                    Thread.sleep(time);
                }//for
            }catch (InterruptedException e) {
                e.printStackTrace();
            }//catch

            return null;

        }//doInBackground

        @Override
        protected void onProgressUpdate(String... values) {
            String display = values[0];
            scoreTextView.setText(display);
            if(soundsLoaded.contains(cdSound)){
                soundPool.play(cdSound,1.0f, 1.0f, 0, 0, 1.0f);
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            updateScore();
        }
    }//Beginning Countdown

    //**************************************************************UTILITY*&*MENU*

    @Override
    public void onClick(View view) {
        if(view.getId() == colorButtons[0]){//green
            vb.vibrate(10);
            fPressed = new FlashPressed(0);
            fPressed.execute();
        }else if(view.getId() == colorButtons[1]){
            vb.vibrate(10);
        }else if(view.getId() == colorButtons[2]){
            vb.vibrate(10);
        }else if(view.getId() == colorButtons[3]){
            vb.vibrate(10);
        }

    }//onClicks

    @Override
    public boolean onMenuItemClick(MenuItem item)  {
        if(item.getItemId() == R.id.gameMode1){
            vb.vibrate(10);
            gameMode = 0;
            chooseGameMode();
            return true;
        }else if(item.getItemId() == R.id.gameMode2){
            vb.vibrate(10);
            gameMode = 1;
            chooseGameMode();
            return true;
        }else if(item.getItemId() == R.id.gameMode3){
            vb.vibrate(10);
            gameMode = 2;
            chooseGameMode();
            return true;
        }else if(item.getItemId() == R.id.actionRestart){
            vb.vibrate(10);
            chooseGameMode();
            return true;
        }else if(item.getItemId() == R.id.actionQuit){
            vb.vibrate(10);
            Intent aboutIntent = new Intent(this, HomeActivity.class);
            startActivity(aboutIntent);
            return true;
        }
        return false;
    }//Menu Clicks

    public void popupMenu(View v) {
        vb.vibrate(10);
        PopupMenu popup = new PopupMenu(this, v);
        popup.setOnMenuItemClickListener(GameActivity.this);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu, popup.getMenu());
        popup.show();
    }//PopUpMenu

    private void toast(String s){
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }//Called from anywhere



}