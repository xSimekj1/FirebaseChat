package com.jakub.firebasechat;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

public class ProfileActivity extends AppCompatActivity {

    private TextView mDisplayID;

    /*Quiz elements*/
    Button quiz_option_a,quiz_option_b,quiz_option_c,quiz_option_d;
    TextView quiz_question_label, quiz_timer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        String user_id = getIntent().getStringExtra("user_id");

        mDisplayID = (TextView) findViewById(R.id.profile_displayName);
        mDisplayID.setText(user_id);

        /*Set quiz elemtents*/
        quiz_option_a = (Button) findViewById(R.id.optionA);
        quiz_option_b = (Button) findViewById(R.id.optionB);
        quiz_option_c = (Button) findViewById(R.id.optionC);
        quiz_option_d = (Button) findViewById(R.id.optionD);
        quiz_question_label = (TextView) findViewById(R.id.profile_question);
        quiz_timer = (TextView) findViewById(R.id.quiz_timer);


    }
}
