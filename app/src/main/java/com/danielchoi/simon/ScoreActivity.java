package com.danielchoi.simon;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class ScoreActivity extends AppCompatActivity {
    ListView lv;
    ScoreAdapter scoreAdapter;
    private List<Scores> scoresList;
    private final String SCORE_FILENAME = "simonScores.txt";
    private final String DELIMITER = "<õ@Scores@õ>";
    private String name,score, place;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score);

        Typeface tf = Typeface.createFromAsset(getAssets(),  "fonts/digitaldismay.otf");
        TextView tv = (TextView) findViewById(R.id.highScoreTv);
        tv.setTypeface(tf);

        lv = (ListView) findViewById(R.id.scoreListView);
        scoresList = readData();
        scoreAdapter = new ScoreAdapter(getApplicationContext(), R.layout.score_row, scoresList);
        lv.setAdapter(scoreAdapter);


       int callingActivity = getIntent().getIntExtra("calling-Activity", 0);
        if(callingActivity == 2000 ){//Home
            enterUserName();
        }
    }

    /**************************************************
     * This checks the score file and fills up the list.
     * @return
     */
    private List<Scores> readData(){

        List<Scores> scoresList = new ArrayList<>();

        try {
            FileInputStream fis = openFileInput(SCORE_FILENAME);
            Scanner scanner = new Scanner(fis).useDelimiter(DELIMITER);
            while(scanner.hasNext()){
                String place = scanner.next().trim();
                String name = scanner.next().trim();
                String score = scanner.next().trim();

                Log.i("ON try: Place: ", place);
                Log.i("ON try: Name: ", name);
                Log.i("ON try: Score: ", score);

                Scores scoreScan = new Scores (place, name, score);
                scoresList.add(scoreScan);
            }
            scanner.close();

        } catch (FileNotFoundException e) {
            //file does not exist
            Log.i("ON IN catch", "======");
        } catch (NoSuchElementException e){
            Log.i("No Such Element", "======");
        }


        return scoresList;

    }

    /**************************************************
     * This write the scores to a file
     */
    private void writeData(){
        try {
            FileOutputStream fos = openFileOutput(SCORE_FILENAME, Context.MODE_PRIVATE);
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            BufferedWriter bw = new BufferedWriter(osw);
            PrintWriter pw = new PrintWriter(bw);

            for(int i = 0; i < scoreAdapter.getCount(); i++){
                Scores scores = (Scores) scoreAdapter.getItem(i);
                pw.println(DELIMITER
                        + scores.getPlace() + DELIMITER
                        + scores.getName()+ DELIMITER
                        + scores.getScore());
            }

            pw.close();

        } catch (FileNotFoundException e) {
            Log.e("WRITE_ERR", "Cannot save data: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "Error saving data", Toast.LENGTH_SHORT).show();
        }
    }

    private void enterUserName(){
        View view = (LayoutInflater.from(this)).inflate(R.layout.enter_name, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final EditText et= (EditText) view.findViewById(R.id.editText);
        builder.setView(view)
        .setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                name = et.getText().toString();
                Log.i("Name: ",name );
                setScores();
            }
        })
        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Log.i("Canceled ","");
            }
        });
        builder.create();
        builder.show();

    }

    private void setScores(){
        int temp = getIntent().getIntExtra("score", 0);
        score = Integer.toString(temp);
        place = Integer.toString(1);
        sortScores();
        Scores newScore = new Scores(place,name,score);
        scoresList.add(newScore);
        Log.i("Score: ",""+score);
        writeData();
    }

    private void sortScores(){


    }

    @Override
    public void onBackPressed() {
        Intent aboutIntent = new Intent(this, HomeActivity.class);
        startActivity(aboutIntent);
    }

    public class ScoreAdapter extends ArrayAdapter{

        private List<Scores> sL;
        private LayoutInflater inflater;
        private int resource;

        public ScoreAdapter(Context context, int resource, List objects){
            super(context, resource, objects);

            this.resource = resource;
            sL = objects;
            inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if(convertView == null){
                convertView = inflater.inflate(resource, null);
                TextView placeTV = (TextView) convertView.findViewById(R.id.placeTv);
                TextView nameTV = (TextView) convertView.findViewById(R.id.nameTv);
                TextView scoreTV = (TextView) convertView.findViewById(R.id.scoreTv);

                placeTV.setText(sL.get(position).getPlace());
                nameTV.setText(sL.get(position).getName());
                scoreTV.setText(sL.get(position).getScore());
            }

return convertView;
        }
        }

}
