package com.example.fraziered.quiz_using_sqlite;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Collections;
import java.util.List;


public class QuizActivity extends AppCompatActivity {

    public static final String EXTRA_SCORE = "extraScore";

    private TextView textViewQuestion;
    private TextView textViewScore;
    private TextView textViewQuestionCount;
    private TextView textViewCountDown;

    private RadioGroup radGroup;
    private RadioButton radBtn1;
    private RadioButton radBtn2;
    private RadioButton radBtn3;

    private Button buttonConfirmNext;

    private ColorStateList textColorDefaultRadioButton; // Save the default radio button color to return it to original color later.

    private List<Question> questionList;

    private Question currentQuestion;

    private int questionCounter;

    private boolean answered;

    private long backPressedTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        textViewQuestion = findViewById(R.id.text_view_question);
        textViewScore = findViewById(R.id.text_view_score);
        textViewQuestionCount = findViewById(R.id.text_view_question_count);
        textViewCountDown = findViewById(R.id.text_view_countdown);
        radGroup = findViewById(R.id.radio_group);
        radBtn1 = findViewById(R.id.radio_button1);
        radBtn2 = findViewById(R.id.radio_button2);
        radBtn3 = findViewById(R.id.radio_button3);
        buttonConfirmNext = findViewById(R.id.button_confirm_next);

        textColorDefaultRadioButton = radBtn1.getTextColors();

        QuizDbHelper dbHelper = new QuizDbHelper(this);
        questionList = dbHelper.getAllQuestions();
        ((GlobalVariableClass) getApplicationContext()).questionCountTotal = questionList.size();

        // If this is the first time the QuizActivity has loaded:
        //   show the first question in the list,
        //   add one to the questionCounter variable
        //
        // Else:
        //   restore what needs to be restored before any method is ran,
        //   show the question that was previously displayed
        if (savedInstanceState == null) {

            ((GlobalVariableClass) getApplicationContext()).score = 0;

            Collections.shuffle(questionList);
            ((GlobalVariableClass) getApplicationContext()).shuffled_list = questionList;

            showNextQuestion();
        } else {
            // Anything before the method will restore before the method runs.
            questionCounter = savedInstanceState.getInt("int_question_count"); // Restore the preserved questionCounter
            textViewScore.setText(savedInstanceState.getString("score"));
            answered = savedInstanceState.getBoolean("answered");
            showSameQuestion();
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

    //     This saves the values before the orientation is changed.
    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Params are Key and Value
        outState.putString("question", textViewQuestion.getText().toString());
        outState.putString("score", textViewScore.getText().toString());
        outState.putString("confirm_button", buttonConfirmNext.getText().toString());

        outState.putInt("int_question_count", questionCounter);

        outState.putBoolean("answered", answered);

        super.onSaveInstanceState(outState);
    }

    //     This reverts the values to where it was before the orientation is changed.
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {

        super.onRestoreInstanceState(savedInstanceState);

        textViewQuestion.setText(savedInstanceState.getString("question"));

        answered = savedInstanceState.getBoolean("answered");

        buttonConfirmNext.setText(savedInstanceState.getString("confirm_button"));
    }

    private void showNextQuestion() {

        radBtn1.setTextColor(textColorDefaultRadioButton);
        radBtn2.setTextColor(textColorDefaultRadioButton);
        radBtn3.setTextColor(textColorDefaultRadioButton);
        radGroup.clearCheck();

        if (questionCounter < ((GlobalVariableClass) getApplicationContext()).questionCountTotal) {
            currentQuestion = ((GlobalVariableClass) getApplicationContext()).shuffled_list.get(questionCounter);

            textViewQuestion.setText(currentQuestion.getQuestion());
            radBtn1.setText(currentQuestion.getOption1());
            radBtn2.setText(currentQuestion.getOption2());
            radBtn3.setText(currentQuestion.getOption3());

            questionCounter++;
            textViewQuestionCount.setText("Question: " + questionCounter + "/" + ((GlobalVariableClass) getApplicationContext()).questionCountTotal);
            answered = false;
            buttonConfirmNext.setText("Confirm");
        } else {
            finishQuiz();
        }
    }

    private void showSameQuestion() {

        questionCounter--; // This is needed to display the proper question. 1 off error...

        radBtn1.setTextColor(textColorDefaultRadioButton);
        radBtn2.setTextColor(textColorDefaultRadioButton);
        radBtn3.setTextColor(textColorDefaultRadioButton);
        radGroup.clearCheck();

        currentQuestion = ((GlobalVariableClass) getApplicationContext()).shuffled_list.get(questionCounter);

        textViewQuestion.setText(currentQuestion.getQuestion());
        radBtn1.setText(currentQuestion.getOption1());
        radBtn2.setText(currentQuestion.getOption2());
        radBtn3.setText(currentQuestion.getOption3());

        if (answered) {
            showSolution();
        }

        questionCounter++; // Restores the question counter to the proper value.
        answered = false;

        textViewQuestionCount.setText("Question: " + questionCounter + "/" + ((GlobalVariableClass) getApplicationContext()).questionCountTotal);
        buttonConfirmNext.setText("Confirm");
    }

    private void checkAnswer() {

        answered = true;

        RadioButton radBtnSelected = findViewById(radGroup.getCheckedRadioButtonId());
        int ansNum = radGroup.indexOfChild(radBtnSelected) + 1;

        if (ansNum == currentQuestion.getAnswerNum()) {
            ((GlobalVariableClass) getApplicationContext()).score++;
            textViewScore.setText("Score: " + ((GlobalVariableClass) getApplicationContext()).score);
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
                textViewQuestion.setText("Answer 1 is correct!");
                break;
            case 2:
                radBtn2.setTextColor(Color.BLACK);
                textViewQuestion.setText("Answer 2 is correct!");
                break;
            case 3:
                radBtn3.setTextColor(Color.BLACK);
                textViewQuestion.setText("Answer 3 is correct!");
                break;
        }

        if (questionCounter < ((GlobalVariableClass) getApplicationContext()).questionCountTotal) {
            buttonConfirmNext.setText("Next");
        } else {
            buttonConfirmNext.setText("Finish");
        }
    }

    private void finishQuiz() {

        Intent resultIntent = new Intent();
        resultIntent.putExtra(EXTRA_SCORE, ((GlobalVariableClass) getApplicationContext()).score);
        setResult(RESULT_OK, resultIntent);

        finish(); // This simply re-opens the current activity. You have to then complete the quiz a second time for this to work properly.
    }

    @Override
    public void onBackPressed() {

        if (backPressedTime + 2000 > System.currentTimeMillis()) {
//            finishQuiz(); // This will still save the results you already have.

            finish(); // This doesn't save it and simply closes the activity.

        } else {
            Toast.makeText(this, "Press back again to exit the current quiz!", Toast.LENGTH_SHORT).show();
        }

        backPressedTime = System.currentTimeMillis();
    }
}
