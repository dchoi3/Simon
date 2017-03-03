package com.danielchoi.simon;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

public class GameActivity extends AppCompatActivity
    implements View.OnTouchListener, PopupMenu.OnMenuItemClickListener {

    // VAR Declaration.
    Vector<Integer> userPattern = new Vector<>(), simonPattern = new Vector<>();
    private int tempo, count, score, hintCount, userChoice, choiceCount, gameMode;
    private int colorButtons[], colorDrawable[], pressedDrawable[], soundID[];
    private FlashSimon flash;
    private CountDown countDown;
    private SoundPool soundPool;
    private Set<Integer> soundsLoaded;
    private boolean lockButtons;
    private static final String GAMEMODESP = "gamemodesp";
    public Typeface customFont;
    public TextView scoreTextView, turnTextView, modeTextView;
    public Vibrator vb;
    public ImageButton gButton, bButton, yButton, rButton;
    public boolean match;
    public static final int activityRef = 2000;

    // VAR Initialization.
    private void setVariables() {
        SharedPreferences sharedPreferences = this.getPreferences(Context.MODE_PRIVATE);
        gameMode = sharedPreferences.getInt(GAMEMODESP,1);
        count = 0;
        score = 0;
        hintCount = 3;
        simonPattern.clear();
        userPattern.clear();
        tempo = 1000;
        lockButtons = true;
        vb = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        colorButtons = new int[]{R.id.green_imageButton, R.id.blue_imageButton, R.id.yellow_imageButton,
            R.id.red_imageButton}; // Color button ids
        colorDrawable = new int[]{R.drawable.green, R.drawable.blue, R.drawable.yellow, R.drawable.red};
        pressedDrawable = new int[]{R.drawable.greenpressed, R.drawable.bluepressed, R.drawable.yellowpressed,
            R.drawable.redpressed};
        soundID = new int[]{soundPool.load(this, R.raw.greennote, 1), soundPool.load(this, R.raw.bluenote, 1),
            soundPool.load(this, R.raw.yellownote, 1), soundPool.load(this, R.raw.rednote, 1),
            soundPool.load(this, R.raw.countdown, 1), soundPool.load(this, R.raw.go, 1)};

        customFont = Typeface.createFromAsset(getAssets(), "fonts/digitaldismay.otf");

        scoreTextView = (TextView) findViewById(R.id.score_textView);
        modeTextView = (TextView) findViewById(R.id.gameMode_textView);
        turnTextView = (TextView) findViewById(R.id.turn_textView);

        modeTextView.setTypeface(customFont);
        turnTextView.setTypeface(customFont);
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

        //resetButton Images just incase thread was canceled between flashes
        for(int x = 0; x < 4; x++) {
            ImageButton imageButton = (ImageButton) findViewById(colorButtons[x]);
            imageButton.setImageResource(colorDrawable[x]);
        }

    }

    /**
     * onCreate is the initial life cycle of the app.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        soundsLoaded = new HashSet<Integer>();

    }

    /**
     * onPause cancels sounds and animations.
     */
    @Override
    protected void onPause() {
        super.onPause();

        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
            soundsLoaded.clear();
        }
        clearThreads();
    }

    /**
     * onResume loads up the sounds and prepares game mode.
     */
    @Override
    protected void onResume() {
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
                if (status == 0) {
                    soundsLoaded.add(sampleId);
                    Log.i("SOUND", "Sound loaded = " + sampleId);
                } else {
                    Log.i("SOUND", "Error cannot load sound status = " + status);
                }
            }
        });
        chooseGameMode();
    }

    /**
     * predefinedPattern calculates the index to create the predetermined pattern.
     *
     * @return The index calculated by the formula.
     */
    public int predefinedPattern() {
        int index;
        if ((count * count) % 11 > 5) {
            index = (count * count * 3) % 4;
        } else {
            index = (count * count * count) % 3;
        }
        count++;
        return index;
    }

    /**
     * randomMode randomly returns an int to be added to Simon's pattern.
     *
     * @return index to be added to simon's pattern.
     */
    private int randomPattern() {
        Random rand = new Random(System.nanoTime());
        int index = rand.nextInt(100);
        index = index % 4;
        count++;
        return index;
    }

    /**
     * chooseGameMode determines what game mode is chosen from the menu.
     */
    private void chooseGameMode() {
        setVariables();

        if (gameMode == 2) {
            setModeTextView("Random Mode");
            countDown = new CountDown();
            countDown.execute();
        } else if (gameMode == 3) {
            setModeTextView("Reuerse Mode");
            countDown = new CountDown();
            countDown.execute();
        } else {
            setModeTextView("Pattern Mode");
            countDown = new CountDown();
            countDown.execute();
        }
    }

    /**
     * simonsTurn calls the appropriate pattern based on the game mode chosen.
     */
    private void simonsTurn() {
        if (gameMode == 2) {
            simonPattern.add(randomPattern()); // Medium.
        } else if (gameMode == 3) {
            simonPattern.add(predefinedPattern()); // Hard.
        } else {
            simonPattern.add(predefinedPattern()); // Easy.
        }
        flash = new FlashSimon();
        flash.execute();
        Log.i("simonPattern", "" + simonPattern);
    }

    /**
     * updateScore updates the score.
     */
    private void updateScore() {
        String scoreString;
        score = score+gameMode;
        if (score < 10) scoreString = "0" + score;
        else if(score >= 99){
            score = 99;
            scoreString = String.valueOf(score);
            gameOver();
        }else {scoreString = String.valueOf(score);}
        scoreTextView.setText(scoreString);
    }

    /**
     * usersTurn determines where the user is in their progress to matching Simon's pattern and
     * sends the user's choice to be compared against Simon's pattern.
     */
    private void usersTurn() {
        lockButtons = false;
        if (choiceCount > 0) {
            if (choiceCount < simonPattern.size()) {
                seqCompare();
            }else if (choiceCount == simonPattern.size()) {
                lockButtons = true;
                seqCompare();
                if (match) {
                    choiceCount = 0;
                    updateScore();
                    try {
                        Thread.sleep(1500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    simonsTurn();
                }
            }
        }
    }

    /**
     * gameOver simply ends the game when the user makes the wrong choice.
     */
    private void gameOver() {
        choiceCount = 0;
        lockButtons = true;
        setHighScore();
    }

    // THREADS

    /**
     * FlashSimon flashes when Simon is choosing a pattern.
     */
    class FlashSimon extends AsyncTask<Void, Void, Void> {

        /**
         * doInBackground loads sounds for simonPattern.
         */
        @Override
        protected Void doInBackground(Void... voids) {

            for (int i = 0; i < simonPattern.size(); i++) {
                final int y = simonPattern.get(i);
                try {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ImageButton flash = (ImageButton) findViewById(colorButtons[y]);
                            flash.setImageResource(pressedDrawable[y]);
                        }
                    });
                    if (soundsLoaded.contains(soundID[y])) {
                        soundPool.play(soundID[y], 1.0f, 1.0f, 0, 0, 1.0f);
                    }
                    if (tempo > 320) {
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
                    Log.i("THREAD=====", "FLASH was interrupted");
                }
            }
            return null;
        }

        /**
         * onPostExecute runs after Simon has established his first pattern.
         */
        @Override
        protected void onPostExecute(Void aVoid) {
            setTurnTextView("Your turn");
            usersTurn();
        }

        @Override
        protected void onPreExecute() {
            setTurnTextView("SiMon's Turn");
        }
    }

    /**
     * CountDown counts down to 0 to ready the user.
     */
    class CountDown extends AsyncTask<Void, String, Void> {

        int cdSound = soundID[4];
        int time = 1000;

        /**
         * onPreExecute...
         */
        @Override
        protected void onPreExecute() {
            try {
                Thread.sleep(1250);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        /**
         * doInBackground plays sound during the countdown.
         *
         * @return null
         */
        @Override
        protected Void doInBackground(Void... voids) {
            String display;
            try {
                for (int i = 3; i >= 0; i--) {
                    if(isCancelled()) return null;
                    if (i == 0) {
                        display = "go";
                        cdSound = soundID[5];
                    } else display = "0" + i;

                    publishProgress(display);
                    Thread.sleep(time);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        /**
         * onProgressUpdates updates the score display.
         */
        @Override
        protected void onProgressUpdate(String... values) {
            String display = values[0];
            scoreTextView.setText(display);
            if (soundsLoaded.contains(cdSound)) {
                soundPool.play(cdSound, 1.0f, 1.0f, 0, 0, 1.0f);
            }
        }

        /**
         * onPostExecute runs when game is over.
         */
        @Override
        protected void onPostExecute(Void aVoid) {
            scoreTextView.setText("00");
            simonsTurn();
        }
    }

    public void clearThreads(){
        if(countDown != null){
            countDown.cancel(true);
            countDown = null;
        }
        if(flash != null){
            flash.cancel(true);
            flash = null;
        }
    }


    // Functionality, Utility, Menu

    /**
     * HintButtonListener listens for usage of the hint button.
     */
    class HintButtonListener implements View.OnClickListener {
        /**
         * onClick tracks when the user uses a hint.
         */
        @Override
        public void onClick(View view) {
            if (hintCount >= 0) {
                hintCount--;
                flash = new FlashSimon();
                flash.execute();
                toastHigh("Hints Remaining: " + hintCount);
            }
            if (hintCount == 0) {
                findViewById(R.id.hint_imageButton).setVisibility(View.INVISIBLE);
            }
        }
    }

    /**
     * SetHighScore records the high scores of a completed game.
     */
    public void setHighScore(){
            Intent scoreIntent = new Intent(getApplicationContext(), ScoreActivity.class);
            scoreIntent.putExtra("score", score);
            scoreIntent.putExtra("calling-Activity", activityRef);
            startActivity(scoreIntent);
    }

    /**
     * playerPress plays a sound and flashes an image when a button is pressed.
     *
     * @param id The id of the button selected.
     */
    private void playerPress(int id) {
        vb.vibrate(10);
        ImageButton flash = (ImageButton) findViewById(colorButtons[id]);
        flash.setImageResource(pressedDrawable[id]);
        if (soundsLoaded.contains(soundID[id])) {
            soundPool.play(soundID[id], 1.0f, 1.0f, 0, 0, 1.0f);
        }
    }

    /**
     * playerUp returns button image to normal and stores the button selected, how many clicks the
     * user has committed.
     *
     * @param id The id of the button selected.
     */
    private void playerUp(int id) {
        ImageButton flash = (ImageButton) findViewById(colorButtons[id]);
        flash.setImageResource(colorDrawable[id]);
        Log.i("Button=", " " + id);
        userChoice = id;
        choiceCount++;
        usersTurn();
    }

    /**
     * toastHigh is a reusable methods for creating multiple toasts that are at the top of the
     * screen.
     *
     * @param s The string to be displayed in the toast.
     */
    private void toastHigh(String s) {
        Toast toast = Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP, 0, 50);
        toast.show();
    }//Called from anywhere

    private void setModeTextView(String s){
        modeTextView.setText(s);
    }

    private void setTurnTextView(String s){
        turnTextView.setText(s);
    }

    /**
     * onTouch determines which button was clicked by the user.
     */
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {

        if (view.getId() == colorButtons[0] && !lockButtons) {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                playerPress(0);
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                playerUp(0);
            }
        } else if (view.getId() == colorButtons[1] && !lockButtons) {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                playerPress(1);
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                playerUp(1);
            }
        } else if (view.getId() == colorButtons[2] && !lockButtons) {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                playerPress(2);
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                playerUp(2);
            }
        } else if (view.getId() == colorButtons[3] && !lockButtons) {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                playerPress(3);
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                playerUp(3);
            }
        }
        return false;
    }

    /**
     * onMenuItemClick detects what menu item was clicked.
     *
     * @param item The menu item clicked.
     */
    @Override
    public boolean onMenuItemClick(MenuItem item) {
        vb.vibrate(10);
        clearThreads();

        if (item.getItemId() == R.id.gameMode1) {
            gameMode = 1;
            insertSharedPreference();
            chooseGameMode();
            return true;
        } else if (item.getItemId() == R.id.gameMode2) {
            gameMode = 2;
            insertSharedPreference();
            chooseGameMode();
            return true;
        } else if (item.getItemId() == R.id.gameMode3) {
            gameMode = 3;
            insertSharedPreference();
            chooseGameMode();
            return true;
        } else if (item.getItemId() == R.id.actionAbout) {
            Intent aboutIntent = new Intent(this, AboutActivity.class);
            startActivity(aboutIntent);
            return true;
        } else if (item.getItemId() == R.id.actionRestart) {
            chooseGameMode();
            return true;
        } else if (item.getItemId() == R.id.actionQuit) {
            Intent aboutIntent = new Intent(this, HomeActivity.class);
            startActivity(aboutIntent);
            return true;
        }
        return false;
    }

    private void insertSharedPreference(){

        SharedPreferences sharedPreferences = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(GAMEMODESP, gameMode);
        editor.apply();

    }

    /**
     * popupMenu...
     */
    public void popupMenu(View v) {
        vb.vibrate(10);
        PopupMenu popup = new PopupMenu(this, v);
        popup.setOnMenuItemClickListener(GameActivity.this);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu, popup.getMenu());
        popup.show();
    }

    /**
     * seqCompare method does a simple check to see if user choice matches simon's pattern.
     */
    public void seqCompare() {
        if (gameMode == 3) {
            reversePattern();
        }
        if (simonPattern.elementAt(choiceCount - 1).equals(userChoice)) {
            Log.i("Match", " simon: " + simonPattern.elementAt(choiceCount - 1) + " user: " + userChoice);
            match = true;
            if (gameMode == 3) {
                reversePattern();
            }
        } else {
            Log.i("No Match", " simon: " + simonPattern.elementAt(choiceCount - 1) + " user: " + userChoice);
            match = false;
            gameOver();
        }
    }

    private void reversePattern(){
        Collections.reverse(simonPattern);
    }
}