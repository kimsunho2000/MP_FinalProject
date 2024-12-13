/* ----- 시작 화면 액티비티 ----- */
package com.example.finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    /* 현재 구현된 사항으로는 안드로이드의 loopback address인 10.0.2.2로 소켓통신이 가능함
    별도의 포트포워딩을 통해 다른 네트워크와 통신 가능(미구현)
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EditText serverIpInput = findViewById(R.id.serverIpInput);
        EditText nicknameInput = findViewById(R.id.nicknameInput);
        Button connectButton = findViewById(R.id.connectButton);

        connectButton.setOnClickListener(v -> { //서버 IP,닉네임 저장
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