package com.jakub.firebasechat;

import static java.nio.charset.StandardCharsets.UTF_8;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Handler;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.*;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate.Status;
import com.google.android.gms.nearby.connection.Strategy;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class nearbyActivity extends AppCompatActivity {

    private static final String TAG = "FireBaseQuiz";

    private static final String[] REQUIRED_PERMISSIONS =
            new String[] {
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.CHANGE_WIFI_STATE,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
            };

    private static final int REQUEST_CODE_REQUIRED_PERMISSIONS = 1;

    private static final Strategy STRATEGY = Strategy.P2P_STAR;

    private enum GameChoice {
        ROCK,
        PAPER,
        SCISSORS;

        boolean beats(GameChoice other) {
            return (this == GameChoice.ROCK && other == GameChoice.SCISSORS)
                    || (this == GameChoice.SCISSORS && other == GameChoice.PAPER)
                    || (this == GameChoice.PAPER && other == GameChoice.ROCK);
        }
    }

    // Our handle to Nearby Connections
    private ConnectionsClient connectionsClient;

    //Firebase references for questions
    //DatabaseReference mQuestionCountReference;
    //DatabaseReference mQuestionsDatabase;

    // Our randomly generated name
//    private final String codeName = getIntent().getStringExtra("user_id");
    private final String codeName = "Simi";

    private String opponentEndpointId;
    private String opponentName;
    private int opponentScore;
    private GameChoice opponentChoice;

    private int myScore;
    private GameChoice myChoice;

    //int total = 0; //number of questions

    private Button findOpponentButton;
    private Button disconnectButton;
    private Button rockButton;
    private Button paperButton;
    private Button scissorsButton;

    /*Quiz elements*/
//    private TextView quiz_question_label;
//    private Button quiz_option_a;
//    private Button quiz_option_b;
//    private Button quiz_option_c;
//    private Button quiz_option_d;

    private TextView opponentText;
    private TextView statusText;
    private TextView scoreText;

    // Callbacks for receiving payloads
    private final PayloadCallback payloadCallback =
            new PayloadCallback() {
                @Override
                public void onPayloadReceived(String endpointId, Payload payload) {
                    opponentChoice = GameChoice.valueOf(new String(payload.asBytes(), UTF_8));
                }

                @Override
                public void onPayloadTransferUpdate(String endpointId, PayloadTransferUpdate update) {
                    if (update.getStatus() == Status.SUCCESS && myChoice != null && opponentChoice != null) {
                        finishRound();
                    }
                }
            };

    // Callbacks for finding other devices
    private final EndpointDiscoveryCallback endpointDiscoveryCallback =
            new EndpointDiscoveryCallback() {
                @Override
                public void onEndpointFound(String endpointId, DiscoveredEndpointInfo info) {
                    Log.i(TAG, "onEndpointFound: endpoint found, connecting");
                    connectionsClient.requestConnection(codeName, endpointId, connectionLifecycleCallback);
                }

                @Override
                public void onEndpointLost(String endpointId) {}
            };
    // Callbacks for connections to other devices
    private final ConnectionLifecycleCallback connectionLifecycleCallback =
            new ConnectionLifecycleCallback() {
                @Override
                public void onConnectionInitiated(String endpointId, ConnectionInfo connectionInfo) {
                    Log.i(TAG, "onConnectionInitiated: accepting connection");
                    connectionsClient.acceptConnection(endpointId, payloadCallback);
                    opponentName = connectionInfo.getEndpointName();
                }

                @Override
                public void onConnectionResult(String endpointId, ConnectionResolution result) {
                    if (result.getStatus().isSuccess()) {
                        Log.i(TAG, "onConnectionResult: connection successful");

                        connectionsClient.stopDiscovery();
                        connectionsClient.stopAdvertising();

                        opponentEndpointId = endpointId;
                        setOpponentName(opponentName);
                        setStatusText(getString(R.string.status_connected));
                        setButtonState(true);
                    } else {
                        Log.i(TAG, "onConnectionResult: connection failed");
                    }
                }

                @Override
                public void onDisconnected(String endpointId) {
                    Log.i(TAG, "onDisconnected: disconnected from the opponent");
                    resetGame();
                }
            };

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_nearby);

        findOpponentButton = findViewById(R.id.find_opponent);
        disconnectButton = findViewById(R.id.disconnect);
        rockButton = findViewById(R.id.rock);
        paperButton = findViewById(R.id.paper);
        scissorsButton = findViewById(R.id.scissors);

        /*Set quiz elemtents*/
//        quiz_option_a = (Button) findViewById(R.id.optionA);
//        quiz_option_b = (Button) findViewById(R.id.optionB);
//        quiz_option_c = (Button) findViewById(R.id.optionC);
//        quiz_option_d = (Button) findViewById(R.id.optionD);
//        quiz_question_label = (TextView) findViewById(R.id.quiz_question);

        opponentText = findViewById(R.id.opponent_name);
        statusText = findViewById(R.id.status);
        scoreText = findViewById(R.id.score);

        TextView nameView = findViewById(R.id.name);
        nameView.setText(getString(R.string.codename, codeName));

        connectionsClient = Nearby.getConnectionsClient(this);

//        mQuestionCountReference = FirebaseDatabase.getInstance().getReference().child("Questions");

        resetGame();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (!hasPermissions(this, REQUIRED_PERMISSIONS)) {
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_REQUIRED_PERMISSIONS);
        }
    }

    @Override
    protected void onStop() {
        connectionsClient.stopAllEndpoints();
        resetGame();

        super.onStop();
    }

    /** Returns true if the app was granted all the permissions. Otherwise, returns false. */
    private static boolean hasPermissions(Context context, String... permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /** Handles user acceptance (or denial) of our permission request. */
    @CallSuper
    @Override
    public void onRequestPermissionsResult(
            int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode != REQUEST_CODE_REQUIRED_PERMISSIONS) {
            return;
        }

        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, R.string.error_missing_permissions, Toast.LENGTH_LONG).show();
                finish();
                return;
            }
        }
        recreate();
    }

    /** Finds an opponent to play the game with using Nearby Connections. */
    public void findOpponent(View view) {
        startAdvertising();
        startDiscovery();
        setStatusText(getString(R.string.status_searching));
        findOpponentButton.setEnabled(false);
    }

    /** Disconnects from the opponent and reset the UI. */
    public void disconnect(View view) {
        connectionsClient.disconnectFromEndpoint(opponentEndpointId);
        resetGame();
    }

    /** Sends a {@link GameChoice} to the other player. */
    public void makeMove(View view) {
        if (view.getId() == R.id.rock) {
            sendGameChoice(GameChoice.ROCK);
        } else if (view.getId() == R.id.paper) {
            sendGameChoice(GameChoice.PAPER);
        } else if (view.getId() == R.id.scissors) {
            sendGameChoice(GameChoice.SCISSORS);
        }
    }

    /** Starts looking for other players using Nearby Connections. */
    private void startDiscovery() {
        // Note: Discovery may fail. To keep this demo simple, we don't handle failures.
        connectionsClient.startDiscovery(
                getPackageName(), endpointDiscoveryCallback,
                new DiscoveryOptions.Builder().setStrategy(STRATEGY).build());
    }

    /** Broadcasts our presence using Nearby Connections so other players can find us. */
    private void startAdvertising() {
        // Note: Advertising may fail. To keep this demo simple, we don't handle failures.
        connectionsClient.startAdvertising(
                codeName, getPackageName(), connectionLifecycleCallback,
                new AdvertisingOptions.Builder().setStrategy(STRATEGY).build());
    }

    /** Wipes all game state and updates the UI accordingly. */
    private void resetGame() {
        opponentEndpointId = null;
        opponentName = null;
        opponentChoice = null;
        opponentScore = 0;
        myChoice = null;
        myScore = 0;

        setOpponentName(getString(R.string.no_opponent));
        setStatusText(getString(R.string.status_disconnected));
        updateScore(myScore, opponentScore);
        setButtonState(false);
    }

    /** Sends the user's selection of rock, paper, or scissors to the opponent. */
    private void sendGameChoice(GameChoice choice) {
        myChoice = choice;
        connectionsClient.sendPayload(
                opponentEndpointId, Payload.fromBytes(choice.name().getBytes(UTF_8)));

        setStatusText(getString(R.string.game_choice, choice.name()));
        // No changing your mind!
        setGameChoicesEnabled(false);
    }

    /** Determines the winner and update game state/UI after both players have chosen. */
    private void finishRound() {
        if (myChoice.beats(opponentChoice)) {
            // Win!
            setStatusText(getString(R.string.win_message, myChoice.name(), opponentChoice.name()));
            myScore++;
        } else if (myChoice == opponentChoice) {
            // Tie, same choice by both players
            setStatusText(getString(R.string.tie_message, myChoice.name()));
        } else {
            // Loss
            setStatusText(getString(R.string.loss_message, myChoice.name(), opponentChoice.name()));
            opponentScore++;
        }

        myChoice = null;
        opponentChoice = null;

        updateScore(myScore, opponentScore);

        // Ready for another round
        setGameChoicesEnabled(true);
    }

    /** Enables/disables buttons depending on the connection status. */
    private void setButtonState(boolean connected) {
        findOpponentButton.setEnabled(true);
        findOpponentButton.setVisibility(connected ? View.GONE : View.VISIBLE);
        disconnectButton.setVisibility(connected ? View.VISIBLE : View.GONE);

        setGameChoicesEnabled(connected);
    }

    /** Enables/disables the rock, paper, and scissors buttons. */
    private void setGameChoicesEnabled(boolean enabled) {
        rockButton.setEnabled(enabled);
        paperButton.setEnabled(enabled);
        scissorsButton.setEnabled(enabled);
    }

    /** Shows a status message to the user. */
    private void setStatusText(String text) {
        statusText.setText(text);
    }

    /** Updates the opponent name on the UI. */
    private void setOpponentName(String opponentName) {
        opponentText.setText(getString(R.string.opponent_name, opponentName));
    }

    /** Updates the running score ticker. */
    private void updateScore(int myScore, int opponentScore) {
        scoreText.setText(getString(R.string.game_score, myScore, opponentScore));
    }

    /*TO DO:
    Get Number of questions stored in database... if more than 10 then pick random 10 and store their ijndexes into array*/
//    private void getQuestions(){
//        final int[] countQuestions = new int[1];
//        mQuestionCountReference = FirebaseDatabase.getInstance().getReference().child("Questions");
//        mQuestionCountReference.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                if (dataSnapshot.exists()){
//                    countQuestions[0] = (int) dataSnapshot.getChildrenCount();
//                    updateQuestion(countQuestions[0]);
//                }else{
//                    quiz_question_label.setText("No Questions in database");
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//
//        });
//    }
//
//    private void updateQuestion(final int countQuestions) {
//        total++;
//        if (total > countQuestions) {
//            startResultActivity();
//        } else {
//            mQuestionsDatabase = FirebaseDatabase.getInstance().getReference().child("Questions").child(String.valueOf(total));
//            mQuestionsDatabase.addValueEventListener(new ValueEventListener() {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                    final Question question = dataSnapshot.getValue(Question.class);
//                    quiz_question_label.setText(question.getQuestion());
//                    quiz_option_a.setText(question.getOption1());
//                    quiz_option_b.setText(question.getOption2());
//                    quiz_option_c.setText(question.getOption3());
//                    quiz_option_d.setText(question.getOption4());
//
//                    quiz_option_a.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            evaluateQuestion(countQuestions, quiz_option_a, quiz_option_b, quiz_option_c, quiz_option_d, question);
//                        }
//                    });
//                    quiz_option_b.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            evaluateQuestion(countQuestions, quiz_option_b, quiz_option_a, quiz_option_c, quiz_option_d, question);
//                        }
//                    });
//                    quiz_option_c.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            evaluateQuestion(countQuestions, quiz_option_c, quiz_option_b, quiz_option_a, quiz_option_d, question);
//                        }
//                    });
//                    quiz_option_d.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            evaluateQuestion(countQuestions, quiz_option_d, quiz_option_b, quiz_option_c, quiz_option_a, question);
//                        }
//                    });
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                }
//
//                public void evaluateQuestion(final int countQuestions, final Button myChoice, final Button other2, final Button other3, final Button other4, Question question) {
//                    //Right Answer
//                    if (myChoice.getText().toString().equals(question.getAnswer())) {
//                        myChoice.setBackgroundColor(Color.GREEN);
//                        Handler handler = new Handler();
//                        handler.postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//                                myScore++;
//                                myChoice.setBackgroundResource(R.color.colorPrimary);
//                                updateQuestion(countQuestions);
//                            }
//                        }, 1500);
//                    } else {
//                        //Wrong answer
//                        myChoice.setBackgroundColor(Color.RED);
//                        if (other2.getText().toString().equals(question.getAnswer())) {
//                            other2.setBackgroundColor(Color.GREEN);
//                        } else if (other3.getText().toString().equals(question.getAnswer())) {
//                            other3.setBackgroundColor(Color.GREEN);
//                        } else {
//                            other4.setBackgroundColor(Color.GREEN);
//                        }
//
//                        Handler handler = new Handler();
//                        handler.postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//                                myChoice.setBackgroundResource(R.color.colorPrimary);
//                                other2.setBackgroundResource(R.color.colorPrimary);
//                                other3.setBackgroundResource(R.color.colorPrimary);
//                                other4.setBackgroundResource(R.color.colorPrimary);
//                                updateQuestion(countQuestions);
//                            }
//                        }, 1500);
//                    }
//                }
//            });
//        }
//    }
//
//    public void startResultActivity(){
//        Intent intent = new Intent(nearbyActivity.this, QuizResultActivity.class);
//        intent.putExtra("total", String.valueOf(total-1));
//        intent.putExtra("correct", String.valueOf(myScore));
//        intent.putExtra("incorrect", String.valueOf(opponentScore));
//        startActivity(intent);
//    }
}
