package com.example.finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EditText serverIpInput = findViewById(R.id.serverIpInput);
        EditText nicknameInput = findViewById(R.id.nicknameInput);
        Button connectButton = findViewById(R.id.connectButton);

        connectButton.setOnClickListener(v -> {
            String serverIp = serverIpInput.getText().toString().trim();
            String nickname = nicknameInput.getText().toString().trim();

            if (serverIp.isEmpty()) {
                serverIpInput.setError("서버 IP를 입력하세요");
            } else if (nickname.isEmpty()) {
                nicknameInput.setError("별명을 입력하세요");
            } else {
                Intent intent = new Intent(MainActivity.this,ChatActivity.class);
                intent.putExtra("serverIp", serverIp);
                intent.putExtra("nickname", nickname);
                startActivity(intent);
            }
        });
    }
}