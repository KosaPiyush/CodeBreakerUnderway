package com.kosa.steppingstone.codebreaker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class FIndOpponentActivity extends AppCompatActivity {

    TextView usernameTV;
    ListView onlinePlayersListView;
    List<String> roomList;
    Button createRoomButton;

    String playerName = "";
    String roomName = "";
    FirebaseDatabase database;
    DatabaseReference roomRef;
    DatabaseReference roomsRef;


    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    String currentUserID;
    FirebaseUser currentUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_opponent);

        usernameTV = findViewById(R.id.userNameTV);

        database = FirebaseDatabase.getInstance();

        Bundle bundle = getIntent().getExtras();
        playerName = bundle.getString("username");

        usernameTV.setText(playerName);

        roomName = playerName + "'s Room";

        onlinePlayersListView = findViewById(R.id.onlinePlayersListView);
        createRoomButton = findViewById(R.id.createRoomButton);

        // all existing available rooms
        roomList = new ArrayList<>();
        createRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //create room and add yourself as player;
                createRoomButton.setText("Creating Room");
                createRoomButton.setEnabled(false);
                roomName = playerName + "'s Room";
                roomRef = database.getReference("rooms/" + roomName + "/player1");
                roomRef.setValue(playerName);

                final AlertDialog.Builder dialog = new AlertDialog.Builder(getApplicationContext()).
                        setTitle("Waiting for opponent..").
                        setMessage("01:00");
                dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });



                new CountDownTimer(60000, 1000) {

                    @Override
                    public void onTick(long l) {
                        dialog.setMessage("00:" + (l/1000));

                    }

                    @Override
                    public void onFinish() {
                        finish();

                    }
                }.start();







            }
        });

        onlinePlayersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                //join an existing room and set yourself as player 2
                roomName = roomList.get(i);
                roomRef = database.getReference("rooms/" + roomName + "/player2");
                addRoomEventListener();
                roomRef.setValue(playerName);
            }
        });

        // Show if rooms are available
        addRoomsEventListener();



    }

    private void addRoomEventListener(){

        roomRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //join the room
                createRoomButton.setText("Create Room");
                createRoomButton.setEnabled(true);
                Intent intent = new Intent(getApplicationContext(), GameActivityMain.class);
                intent.putExtra("roomName", roomName);
                startActivity(intent);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //error
                createRoomButton.setText("CREATE ROOM");
                createRoomButton.setEnabled(true);
                Toast.makeText(getApplicationContext(), "ERROR", Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void addRoomsEventListener(){

        roomsRef = database.getReference("rooms");
        roomsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //show list of rooms
                roomList.clear();
                Iterable<DataSnapshot> rooms = dataSnapshot.getChildren();
                for(DataSnapshot snapshot : rooms){
                    roomList.add(snapshot.getKey());

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(FIndOpponentActivity.this,
                            android.R.layout.simple_list_item_1, roomList);
                    onlinePlayersListView.setAdapter(adapter);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //error = nothing


            }
        });
    }








}