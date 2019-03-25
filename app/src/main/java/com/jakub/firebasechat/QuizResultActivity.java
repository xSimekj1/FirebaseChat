package com.jakub.firebasechat;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class QuizResultActivity extends AppCompatActivity {

    TextView total_points;
    TextView correct_points;
    TextView incorrect_points;
    Button result_back_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_result);

        total_points = findViewById(R.id.result_total);
        correct_points = findViewById(R.id.result_correct);
        incorrect_points = findViewById(R.id.result_incorrect);
        result_back_button = findViewById(R.id.result_button_backToProfile);
        
        String total = getIntent().getStringExtra("total");
        String correct = getIntent().getStringExtra("correct");
        String incorrect = getIntent().getStringExtra("incorrect");

        total_points.setText(total);
        correct_points.setText(correct);
        incorrect_points.setText(incorrect);

        result_back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent resultIntent = new Intent(QuizResultActivity.this, UsersActivity.class);
                startActivity(resultIntent);
            }
        });
    }
}
