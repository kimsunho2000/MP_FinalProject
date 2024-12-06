package com.example.finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button serverButton = findViewById(R.id.serverButton);
        Button clientButton = findViewById(R.id.clientButton);

        serverButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ChatActivity.class);
            intent.putExtra("isServer", true); // 서버 모드 설정
            startActivity(intent);
        });

        clientButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ChatActivity.class);
            intent.putExtra("isServer", false); // 클라이언트 모드 설정
            startActivity(intent);
        });
    }
}