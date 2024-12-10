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
        Button connectButton = findViewById(R.id.connectButton); // 버튼 이름 변경: 클라이언트 연결 전용

        // 클라이언트 시작
        connectButton.setOnClickListener(v -> {
            String serverIp = serverIpInput.getText().toString().trim();
            if (!serverIp.isEmpty()) {
                try {
                    Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                    intent.putExtra("serverIp", serverIp); // 서버 IP 전달
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                serverIpInput.setError("유효한 서버 IP를 입력하세요");
            }
        });
    }
}