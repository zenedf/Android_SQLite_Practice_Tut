package com.example.fraziered.quiz_using_sqlite;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_QUIZ = 1;

    public static final String EXTRA_DIFFICULTY = "extraDifficulty";

    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String KEY_HIGHSCORE = "keyHighscore";

    private TextView textViewHighscore;
    private Spinner spinnerDifficulty;

    private int highscore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textViewHighscore = findViewById(R.id.text_view_highscore);
        spinnerDifficulty = findViewById(R.id.spinner_difficulty);

        String[] difficultyLevels = Question.getAllDifficultyLevels();

        ArrayAdapter<String> adapterDifficulty = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, difficultyLevels);
        adapterDifficulty.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDifficulty.setAdapter(adapterDifficulty); // This fills the spinner options with all difficulties available.

        loadHighscore();

        Button buttonStartQuiz = findViewById(R.id.button_start_quiz);

        buttonStartQuiz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startQuiz();
            }
        });
    }

    private void startQuiz() {

        String difficulty = spinnerDifficulty.getSelectedItem().toString();

        Intent intent;

        // This makes sure the program doesn't crash if there is null data.
        try {
            intent = new Intent(MainActivity.this, QuizActivity.class);
        } catch (Exception e) {
            Log.i("info", "my problem is: " + e);
            return;
        }
        // If all is well up there ^^^^
        // Go ahead and do the thing, but for real this time.
        intent = new Intent(MainActivity.this, QuizActivity.class);

        intent.putExtra(EXTRA_DIFFICULTY, difficulty);

//        startActivity(intent); // This simply starts the activity.
        startActivityForResult(intent, REQUEST_CODE_QUIZ); // This gets a result back from the activity it starts.
    }

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
        String text = getString(R.string.highscore_text) + highscore;
        textViewHighscore.setText(text);
    }

    private void updateHighscore(int highscoreNew) {

        highscore = highscoreNew;
        String text = getString(R.string.highscore_text) + highscore;
        textViewHighscore.setText(text);

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