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
import android.text.LoginFilter;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

public class GameActivity extends AppCompatActivity
        implements View.OnTouchListener,PopupMenu.OnMenuItemClickListener{

    //***********************************************************DECLARE*VARIABLES*
    Vector<Integer> userPattern = new Vector<>(),simonPattern = new Vector<>();
    private int tempo, gameMode, count, score, flashSpeed, hintCount, userChoice, choiceCount;
    private int colorButtons[], colorDrawable[], pressedDrawable[], soundID[];
    private FlashSimon flash;
    private CountDown countDown;
    private SoundPool soundPool;
    private Set<Integer> soundsLoaded;
    private boolean lockButtons;
    public Typeface customFont;
    public TextView scoreTextView;
    public Vibrator vb;
    public ImageButton gButton, bButton, yButton, rButton;
    public boolean match;
    public static final int activityRef = 2000;

    //*********************************************************Initialize*Variables*

    private void setVariables(){
        count = 0;
        score = 0;
        hintCount = 3;
        simonPattern.clear();
        userPattern.clear();
        flashSpeed = 1000;
        tempo = flashSpeed;
        lockButtons = true;
        vb = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        colorButtons = new int[]{R.id.green_imageButton, R.id.blue_imageButton, R.id.yellow_imageButton,
            R.id.red_imageButton}; // Color button ids
        colorDrawable = new int[]{R.drawable.green,R.drawable.blue,R.drawable.yellow,R.drawable.red};
        pressedDrawable = new int[]{R.drawable.greenpressed ,R.drawable.bluepressed,R.drawable.yellowpressed,
            R.drawable.redpressed};
        soundID = new int []{soundPool.load(this,R.raw.greennote,1), soundPool.load(this,R.raw.bluenote,1),
            soundPool.load(this,R.raw.yellownote,1),soundPool.load(this,R.raw.rednote,1),
            soundPool.load(this,R.raw.countdown,1),soundPool.load(this,R.raw.go,1)};

        customFont= Typeface.createFromAsset(getAssets(),  "fonts/digitaldismay.otf");

        scoreTextView = (TextView)findViewById(R.id.score_textView);
        scoreTextView.setTypeface(customFont);

        gButton = (ImageButton) findViewById(R.id.green_imageButton);
        gButton.setOnTouchListener(this);
        bButton = (ImageButton) findViewById(R.id.blue_imageButton);
        bButton.setOnTouchListener(this);
        yButton = (ImageButton) findViewById(R.id.yellow_imageButton);
        yButton.setOnTouchListener(this);
        rButton = (ImageButton) findViewById(R.id.red_imageButton);
        rButton.setOnTouchListener(this);

        findViewById(R.id.hint_imageButton).setVisibility(View.VISIBLE);
        findViewById(R.id.hint_imageButton).setOnClickListener(new HintButtonListener());
        findViewById(R.id.setScore_button).setOnClickListener(new SetHighScore());


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
    //Pattern for game mode 1 (Predetermined Pattern: EASY difficulty)
    private int addPattern1(){// Function that makes simon choose predetermined colors
        int index;
            if((count * count) % 11 > 5){
                index = (count * count * 3) % 4;
            }else{
                index = (count * count * count) % 3;
            }

        count++;
        Log.i("Random: ", ""+index);
        return index;
    }//Called from simonsTurn
    //Pattern for game mode 2 (Random Pattern: Normal difficulty)
    private int addPattern2(){// Function that makes simon choose random colors
        Random rand = new Random(System.nanoTime());
        int index = rand.nextInt(100);
        index = index % 4;
        count++;
        Log.i("Random ", ""+index);
        return index;
    }//Called from simonsTurn
    private int addPattern3(){// Function that makes sets simons predetermined colors and users chooses in reverse
        int index;
        if((count * count) % 11 > 5){
            index = (count * count * 3) % 4;
        }else{
            index = (count * count * count) % 3;
        }

        count++;
        Log.i("Random: ", ""+index);
        return index;
    }//Called from simonsTurn
    //Pattern for game mode 3 ()
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

    private void play(){
        simonsTurn();

    }//Called by onCreate

    private void simonsTurn() {
        //toast("Simon's Turn");
        // Choose pattern from which game mode is selected

        if(gameMode == 2){
            simonPattern.add(addPattern2());//add a random pattern: NORMAL
            flash = new FlashSimon();
            flash.execute();
        }else if(gameMode ==3){
            simonPattern.add(addPattern3());//add a predetermined pattern that will be checked in reverse: HARD
            flash = new FlashSimon();
            flash.execute();
        }else{
            simonPattern.add(addPattern1());//add a predetermined pattern: EASY
            flash = new FlashSimon();
            flash.execute();
        }
        Log.i("simonPattern", "" + simonPattern);
    }//Called from onCreate

    private void updateScore(){
        String scoreString;
        if(score<10) scoreString = "0"+score;
        else scoreString = String.valueOf(score);

        scoreTextView.setText(scoreString);
    }

    private void usersTurn(){
        lockButtons = false;
        simonsTurn();// For testing code *******************************************************
        //Should allow "counts" amount of input here. On click needs to be lock after input
        if (choiceCount > 0) { // Jump in statement only if user has pressed button.
            if (choiceCount < simonPattern.size()) {
                seqCompare();
            } else if (choiceCount == simonPattern.size()) {
                lockButtons = true;
                seqCompare();
                if (match) {
                    choiceCount = 0;
                    new java.util.Timer().schedule(
                        new java.util.TimerTask() {
                            @Override
                            public void run() {
                                simonsTurn();
                            }
                        },
                        2000 // Delay in ms.
                    );
                }
            }
        }
    }//Called from FLASH after thread Completes

    private void gameOver() {
        choiceCount = 0;
        lockButtons = true;
        Log.i("gameOver","Game Over");
        toast("Wrong Choice. Game Over!");
    }

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
                    // Increase Tempo
                    if(tempo > 320) {
                        tempo -= 20;
                    }
                    Thread.sleep(tempo);
                    runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                    ImageButton flash = (ImageButton) findViewById(colorButtons[y]);
                    flash.setImageResource(colorDrawable[y]);
                    }
                });

                } catch (InterruptedException e) {
                    Log.i("THREAD=====","FLASH was interrupted");
                }

            }//for
            //**************************************************Debugging Tempo*
            //Log.i("*******", "Tempo is : " + tempo);
            //Log.i("*******", "Count is : " + count);
            return null;
        }//doiInBackground

        @Override
        protected void onPostExecute(Void aVoid) {
            toast("Your turn");
            usersTurn();
        }

    } //Flashes Simon's Pattern

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

    //*************************************************FUNCTIONALITY*UTILITY*&*MENU*

    class HintButtonListener implements View.OnClickListener{
        @Override
        public void onClick(View view) {
            if(hintCount >= 0){
                hintCount--;
                flash = new FlashSimon();
                flash.execute();
                toastHigh("Hints Remaining: "+hintCount);
            }
            if(hintCount == 0){
                findViewById(R.id.hint_imageButton).setVisibility(View.INVISIBLE);
            }
        }
    }
    class SetHighScore implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            Intent scoreIntent = new Intent(getApplicationContext(), ScoreActivity.class);
            scoreIntent.putExtra("score", score);
            scoreIntent.putExtra("calling-Activity", activityRef);
            startActivity(scoreIntent);

        }
    }

    private void playerPress(int id){
        vb.vibrate(10);
        ImageButton flash = (ImageButton) findViewById(colorButtons[id]);
        flash.setImageResource(pressedDrawable[id]);
        if(soundsLoaded.contains(soundID[id])){
            soundPool.play(soundID[id],1.0f, 1.0f, 0, 0, 1.0f);
        }
    }//OnPressDown

    private void playerUp(int id){
        ImageButton flash = (ImageButton) findViewById(colorButtons[id]);
        flash.setImageResource(colorDrawable[id]);
        Log.i("Button=", " "+id);
        userChoice = id;
        choiceCount++;
        usersTurn();
    }//OnPressUp

    private void toast(String s){
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }//Called from anywhere

    private void toastHigh(String s){
        Toast toast = Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP, 0, 50);
        toast.show();
    }//Called from anywhere

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {

        if (view.getId() == colorButtons[0] && !lockButtons) {//green
            if(motionEvent.getAction()==MotionEvent.ACTION_DOWN){
                playerPress(0);
            }else if(motionEvent.getAction()==MotionEvent.ACTION_UP){
                playerUp(0);
            }

        } else if (view.getId() == colorButtons[1] && !lockButtons) {
            if(motionEvent.getAction()==MotionEvent.ACTION_DOWN){
                playerPress(1);
            }else if(motionEvent.getAction()==MotionEvent.ACTION_UP){
                playerUp(1);
            }
        } else if (view.getId() == colorButtons[2] && !lockButtons) {
            if(motionEvent.getAction()==MotionEvent.ACTION_DOWN){
                playerPress(2);
            }else if(motionEvent.getAction()==MotionEvent.ACTION_UP){
                playerUp(2);
            }
        } else if (view.getId() == colorButtons[3] && !lockButtons) {
            if(motionEvent.getAction()==MotionEvent.ACTION_DOWN){
                playerPress(3);
            }else if(motionEvent.getAction()==MotionEvent.ACTION_UP){
                playerUp(3);
            }
        }

        return false;
    }//onTouch

    @Override
    public boolean onMenuItemClick(MenuItem item)  {
        if(item.getItemId() == R.id.gameMode1){
            vb.vibrate(10);
            gameMode = 1;
            chooseGameMode();
            return true;
        }else if(item.getItemId() == R.id.gameMode2){
            vb.vibrate(10);
            gameMode = 2;
            chooseGameMode();
            return true;
        }else if(item.getItemId() == R.id.gameMode3){
            vb.vibrate(10);
            gameMode = 3;
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

    /**
     * seqCompare method does a simple check to see if user choice matches simon's pattern.
     */
    public void seqCompare() {
        if (simonPattern.elementAt(choiceCount-1).equals(userChoice)) {
            Log.i("Match", " simon: " + simonPattern.elementAt(choiceCount-1) + " user: " + userChoice);
            match = true;
        } else {
            Log.i("No Match", " simon: " + simonPattern.elementAt(choiceCount-1) + " user: " + userChoice);
            match = false;
            gameOver();
        }
    }

}