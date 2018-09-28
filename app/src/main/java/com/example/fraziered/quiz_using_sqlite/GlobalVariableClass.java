package com.example.fraziered.quiz_using_sqlite;

import android.app.Application;

import java.util.List;


public class GlobalVariableClass extends Application {

    public List<Question> shuffled_list;

    public int questionCountTotal;

    public int score;
}
