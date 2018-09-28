package com.example.fraziered.quiz_using_sqlite;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_QUIZ = 1;

    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String KEY_HIGHSCORE = "keyHighscore";

    private TextView textViewHighscore;

    private int highscore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // PART 6
//        textViewHighscore = findViewById(R.id.text_view_highscore);
//        loadHighscore();

        Button buttonStartQuiz = findViewById(R.id.button_start_quiz);

        buttonStartQuiz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // PART 6...I think
//                ((GlobalVariableClass) getApplicationContext()).closedOnce = false; // TESTING

                startQuiz();
            }
        });
    }

    private void startQuiz() {

        Intent intent;

        // This makes sure the program doesn't crash if there is null data.
        try {
            intent = new Intent(MainActivity.this, QuizActivity.class);
        } catch (Exception e) {
            Log.i("info", "my problem is: " + e);
            return;
        }
        // If all is well up there ^^^^
        intent = new Intent(MainActivity.this, QuizActivity.class);

//        startActivity(intent); // Up until PART 5

        // PART 6
        startActivityForResult(intent, REQUEST_CODE_QUIZ); // This gets a result back from the activity it starts.
    }

    // PART 6
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_QUIZ) {
            if (resultCode == RESULT_OK) {
                int score = data.getIntExtra(QuizActivity.EXTRA_SCORE, 0);
                if (score > highscore) {
                    updateHighscore(score);
                }
            }
        }
    }


    private void loadHighscore() {
        SharedPreferences prefs = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        highscore = prefs.getInt(KEY_HIGHSCORE, 0);
        textViewHighscore.setText("Highscore: " + highscore);
    }

    private void updateHighscore(int highscoreNew) {
        highscore = highscoreNew;
        textViewHighscore.setText("Highscore: " + highscore);

        SharedPreferences prefs = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_HIGHSCORE, highscore);
        editor.apply();
    }

    @Override
    public void onBackPressed() {

        finish(); // Exit the app.
    }
}