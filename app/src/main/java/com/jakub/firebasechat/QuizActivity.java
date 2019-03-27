package com.jakub.firebasechat;

import android.content.Intent;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class QuizActivity extends AppCompatActivity {

    private TextView mDisplayID;

    DatabaseReference mQuestionCountReference;
    DatabaseReference mQuestionsDatabase;

    /*Quiz elements*/
    TextView quiz_question_label;
    TextView quiz_timer;
    Button quiz_option_a;
    Button quiz_option_b;
    Button quiz_option_c;
    Button quiz_option_d;

    int total = 0; //number of questions
    int questions[];
    int correct = 0;
    int wrong = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        String user_id = getIntent().getStringExtra("user_id");

        mDisplayID = (TextView) findViewById(R.id.profile_displayName);
        mDisplayID.setText(user_id);

        /*Set quiz elemtents*/
        quiz_option_a = (Button) findViewById(R.id.optionA);
        quiz_option_b = (Button) findViewById(R.id.optionB);
        quiz_option_c = (Button) findViewById(R.id.optionC);
        quiz_option_d = (Button) findViewById(R.id.optionD);
        quiz_question_label = (TextView) findViewById(R.id.quiz_question);
        quiz_timer = (TextView) findViewById(R.id.quiz_timer);

        mQuestionCountReference = FirebaseDatabase.getInstance().getReference().child("Questions");

        getQuestions();

    }

    private void updateQuestion(final int countQuestions) {
        total++;
        if (total > countQuestions){
            startResultActivity();
        }else{
            mQuestionsDatabase = FirebaseDatabase.getInstance().getReference().child("Questions").child(String.valueOf(total));
            mQuestionsDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    final Question question = dataSnapshot.getValue(Question.class);
                    quiz_question_label.setText(question.getQuestion());
                    quiz_option_a.setText(question.getOption1());
                    quiz_option_b.setText(question.getOption2());
                    quiz_option_c.setText(question.getOption3());
                    quiz_option_d.setText(question.getOption4());

                    quiz_option_a.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            evaluateQuestion(countQuestions, quiz_option_a, quiz_option_b, quiz_option_c, quiz_option_d, question);
                        }
                    });
                    quiz_option_b.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            evaluateQuestion(countQuestions, quiz_option_b, quiz_option_a, quiz_option_c, quiz_option_d, question);
                        }
                    });
                    quiz_option_c.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            evaluateQuestion(countQuestions, quiz_option_c, quiz_option_b, quiz_option_a, quiz_option_d, question);
                        }
                    });
                    quiz_option_d.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            evaluateQuestion(countQuestions, quiz_option_d, quiz_option_b, quiz_option_c, quiz_option_a, question);
                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }

                public void evaluateQuestion(final int countQuestions, final Button myChoice, final Button other2, final Button other3, final Button other4, Question question){
                    //Right Answer
                    if (myChoice.getText().toString().equals(question.getAnswer())){
                        myChoice.setBackgroundColor(Color.GREEN);
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                correct++;
                                myChoice.setBackgroundResource(R.color.colorPrimary);
                                updateQuestion(countQuestions);
                            }
                        },1500);
                    }else {
                        //Wrong answer
                        wrong++;
                        myChoice.setBackgroundColor(Color.RED);
                        if (other2.getText().toString().equals(question.getAnswer())){
                            other2.setBackgroundColor(Color.GREEN);
                        }else if (other3.getText().toString().equals(question.getAnswer())){
                            other3.setBackgroundColor(Color.GREEN);
                        }else{
                            other4.setBackgroundColor(Color.GREEN);
                        }

                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                myChoice.setBackgroundResource(R.color.colorPrimary);
                                other2.setBackgroundResource(R.color.colorPrimary);
                                other3.setBackgroundResource(R.color.colorPrimary);
                                other4.setBackgroundResource(R.color.colorPrimary);
                                updateQuestion(countQuestions);
                            }
                        },1500);
                    }
                }
            });
        }

    }

    public void timer(int seconds, final TextView quiz_timer){
        new CountDownTimer(seconds * 1000 + 1000, 1000){
            public void onTick(long milisUntilFinished){
                int seconds = (int) (milisUntilFinished / 1000);
                int minutes = seconds / 60;
                seconds = seconds %60;
                quiz_timer.setText(String.format("%02d",minutes) + ":" + String.format("%02d", seconds));
            }
            public void onFinish(){
                quiz_timer.setText("Finished");
                startResultActivity();
            }
        }.start();
    }

    public void startResultActivity(){
        Intent intent = new Intent(QuizActivity.this, QuizResultActivity.class);
        intent.putExtra("total", String.valueOf(total-1));
        intent.putExtra("correct", String.valueOf(correct));
        intent.putExtra("incorrect", String.valueOf(wrong));
        startActivity(intent);
    }

    /*TO DO:
    Get Number of questions stored in database... if more than 10 then pick random 10 and store their ijndexes into array*/
    private void getQuestions(){
        final int[] countQuestions = new int[1];
        mQuestionCountReference = FirebaseDatabase.getInstance().getReference().child("Questions");
        mQuestionCountReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    countQuestions[0] = (int) dataSnapshot.getChildrenCount();
                    updateQuestion(countQuestions[0]);
                    timer(30, quiz_timer);
                }else{
                    quiz_question_label.setText("No Questions in database");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }

        });
    }
}
