package com.example.fraziered.quiz_using_sqlite;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;


public class QuizActivity extends AppCompatActivity {

    public static final String EXTRA_SCORE = "extraScore";
    private static final long COUNTDOWN_IN_MILLIS = 30000; // 30 seconds

    private static final String KEY_SCORE = "keyScore";
    private static final String KEY_QUESTION_COUNT = "keyQuestionCount";
    private static final String KEY_MILLIS_LEFT = "keyMillisLeft";
    private static final String KEY_ANSWERED = "keyAnswered";
    private static final String KEY_QUESTION_LIST = "keyQuestionList";

    private TextView textViewQuestion;
    private TextView textViewScore;
    private TextView textViewQuestionCount;
    private TextView textViewDifficulty;
    private TextView textViewCountDown;

    private RadioGroup radGroup;
    private RadioButton radBtn1;
    private RadioButton radBtn2;
    private RadioButton radBtn3;

    private Button buttonConfirmNext;

    private ColorStateList textColorDefaultRadioButton; // Save the default radio button color to return it to original color later.
    private ColorStateList textColorDefaultCountDown;

    private CountDownTimer countDownTimer;
    private long timeLeftInMillis;

    //    private List<Question> questionList;
    private ArrayList<Question> questionList;
    private Question currentQuestion;
    private int questionCounter;
    private int questionCountTotal;

    private int score;

    private boolean answered;

    private long backPressedTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        textViewQuestion = findViewById(R.id.text_view_question);
        textViewScore = findViewById(R.id.text_view_score);
        textViewQuestionCount = findViewById(R.id.text_view_question_count);
        textViewDifficulty = findViewById(R.id.text_view_difficulty);
        textViewCountDown = findViewById(R.id.text_view_countdown);
        radGroup = findViewById(R.id.radio_group);
        radBtn1 = findViewById(R.id.radio_button1);
        radBtn2 = findViewById(R.id.radio_button2);
        radBtn3 = findViewById(R.id.radio_button3);
        buttonConfirmNext = findViewById(R.id.button_confirm_next);

        textColorDefaultRadioButton = radBtn1.getTextColors();
        textColorDefaultCountDown = textViewCountDown.getTextColors();

        Intent intent = getIntent();
        String difficulty = intent.getStringExtra(MainActivity.EXTRA_DIFFICULTY);

        String text = getString(R.string.difficulty_text) + difficulty;
        textViewDifficulty.setText(text);

        // Consider this resetting the score.
        score = 0;

        // This works here for now.
        // Load default score. Eventually I will have a better system....I hope.
        text = getString(R.string.score_text) + score;
        textViewScore.setText(text);

        if (savedInstanceState == null) {
            QuizDbHelper dbHelper = new QuizDbHelper(this);
            questionList = dbHelper.getAllQuestions();
            questionList = dbHelper.getQuestions(difficulty);
            questionCountTotal = questionList.size();
//        ((GlobalVariableClass) getApplicationContext()).questionCountTotal = questionList.size();
            Collections.shuffle(questionList);
            showNextQuestion();
        } else {
            questionList = savedInstanceState.getParcelableArrayList(KEY_QUESTION_LIST);
            if (questionList == null) {
                finish();
            }
            questionCountTotal = questionList.size(); // This is because the count total will be reset to 0 with each orientation change.
            questionCounter = savedInstanceState.getInt(KEY_QUESTION_COUNT);
            currentQuestion = questionList.get(questionCounter - 1);
            score = savedInstanceState.getInt(KEY_SCORE);
            timeLeftInMillis = savedInstanceState.getLong(KEY_MILLIS_LEFT);
            answered = savedInstanceState.getBoolean(KEY_ANSWERED);

            if (!answered) {
                startCountDown();
            } else {
                updateCountDownText();
                showSolution();
            }
        }

        buttonConfirmNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!answered) {
                    if (radBtn1.isChecked() || radBtn2.isChecked() || radBtn3.isChecked()) {
                        checkAnswer();
                    } else {
                        Toast.makeText(QuizActivity.this, "Please select an answer.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    showNextQuestion();
                }
            }
        });
    }

    private void showNextQuestion() {

        radBtn1.setTextColor(textColorDefaultRadioButton);
        radBtn2.setTextColor(textColorDefaultRadioButton);
        radBtn3.setTextColor(textColorDefaultRadioButton);
        radGroup.clearCheck();

        if (questionCounter < questionCountTotal) {
            currentQuestion = questionList.get(questionCounter);

            textViewQuestion.setText(currentQuestion.getQuestion());
            radBtn1.setText(currentQuestion.getOption1());
            radBtn2.setText(currentQuestion.getOption2());
            radBtn3.setText(currentQuestion.getOption3());

            questionCounter++;

            String text = getString(R.string.question_count_text) + questionCounter +
                    getString(R.string.forward_slash) + questionCountTotal; // It's wanting me to use a variable for the forward slash.
            //  I call it unnecessary, but whatever.
            textViewQuestionCount.setText(text);

            answered = false;
            buttonConfirmNext.setText(getString(R.string.confirm));

            timeLeftInMillis = COUNTDOWN_IN_MILLIS;
            startCountDown();
        } else {
            finishQuiz();
        }
    }

    private void startCountDown() {
        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) { // called everything second
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateCountDownText();
            }

            @Override
            public void onFinish() {
                timeLeftInMillis = 0;
                updateCountDownText();
                checkAnswer();
            }
        }.start(); // Immediately start count down timer as soon as it's created.
    }

    public void pauseCountDown() {
        countDownTimer.cancel();
    }

    // Don't know if this is necessary
    public void resumeCountDown() {
        startCountDown();
    }

    private void updateCountDownText() {
        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;

        String timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);

        textViewCountDown.setText(timeFormatted);

        if (timeLeftInMillis < 10000) {
            textViewCountDown.setTextColor(Color.RED);
        } else {
            textViewCountDown.setTextColor((textColorDefaultCountDown));
        }
    }


    private void checkAnswer() {

        answered = true;

        pauseCountDown();

        RadioButton radBtnSelected = findViewById(radGroup.getCheckedRadioButtonId());
        int ansNum = radGroup.indexOfChild(radBtnSelected) + 1;

        if (ansNum == currentQuestion.getAnswerNum()) {
            score++;

            String text = getString(R.string.score_text) + score;
            textViewScore.setText(text);

        }

        showSolution();
    }

    private void showSolution() {

        radBtn1.setTextColor(Color.RED);
        radBtn2.setTextColor(Color.RED);
        radBtn3.setTextColor(Color.RED);

        switch (currentQuestion.getAnswerNum()) {
            case 1:
                radBtn1.setTextColor(Color.BLACK);
                textViewQuestion.setText(getString(R.string.ans1_correct));
                break;
            case 2:
                radBtn2.setTextColor(Color.BLACK);
                textViewQuestion.setText(getString(R.string.ans2_correct));
                break;
            case 3:
                radBtn3.setTextColor(Color.BLACK);
                textViewQuestion.setText(getString(R.string.ans3_correct));
                break;
        }

        if (questionCounter < questionCountTotal) {
            buttonConfirmNext.setText(getString(R.string.next));
        } else {
            buttonConfirmNext.setText(getString(R.string.finish));
        }
    }

    private void finishQuiz() {

        Intent resultIntent = new Intent();

        resultIntent.putExtra(EXTRA_SCORE, score);
        setResult(RESULT_OK, resultIntent);

        finish(); // This simply re-opens the current activity. You have to then complete the quiz a second time for this to work properly.
    }

    @Override
    public void onBackPressed() {

        if (backPressedTime + 2000 > System.currentTimeMillis()) {

            finish(); // This doesn't save it and simply closes the activity.

        } else {
            Toast.makeText(this, "Press back again to exit the current quiz!", Toast.LENGTH_SHORT).show();
        }

        backPressedTime = System.currentTimeMillis();
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(KEY_SCORE, score);
        outState.putInt(KEY_QUESTION_COUNT, questionCounter);
        outState.putLong(KEY_MILLIS_LEFT, timeLeftInMillis);
        outState.putBoolean(KEY_ANSWERED, answered);
        outState.putParcelableArrayList(KEY_QUESTION_LIST, questionList);
    }
}
