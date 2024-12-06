package com.example.finalproject;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class ChatActivity extends AppCompatActivity {

    private TextView chatLog;
    private EditText messageInput;
    private Button sendMessageButton;
    private TextView chatRole;
    private ScrollView chatScrollView;

    private ServerSocket serverSocket;
    private Socket socket;
    private PrintWriter output;
    private BufferedReader input;

    private boolean isServer; // 서버/클라이언트 역할 결정

    private static final int PORT = 12345; // 포트 번호

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatLog = findViewById(R.id.chatLog);
        messageInput = findViewById(R.id.messageInput);
        sendMessageButton = findViewById(R.id.sendMessageButton);
        chatRole = findViewById(R.id.chatRole);
        chatScrollView = findViewById(R.id.chatScrollView);

        // Intent에서 역할 정보 가져오기
        isServer = getIntent().getBooleanExtra("isServer", true);

        // 역할에 따라 서버/클라이언트 실행
        if (isServer) {
            chatRole.setText("Role: Server");
            startServer();
        } else {
            chatRole.setText("Role: Client");
            String serverIp = getIntent().getStringExtra("serverIp"); // 서버 IP 전달받기
            startClient(serverIp);
        }

        // 메시지 전송 버튼 클릭 리스너
        sendMessageButton.setOnClickListener(v -> sendMessage());
    }

    private void startServer() {
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(PORT);
                appendChatLog("서버 대기 중...");

                socket = serverSocket.accept();
                appendChatLog("클라이언트 연결됨");

                setupStreams();
                listenForMessages();
            } catch (Exception e) {
                appendChatLog("서버 오류: " + e.getMessage());
            }
        }).start();
    }

    private void startClient(String serverIp) {
        new Thread(() -> {
            try {
                socket = new Socket(serverIp, PORT);
                appendChatLog("서버에 연결됨");

                setupStreams();
                listenForMessages();
            } catch (Exception e) {
                appendChatLog("클라이언트 오류: " + e.getMessage());
            }
        }).start();
    }

    private void setupStreams() {
        try {
            output = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (Exception e) {
            appendChatLog("스트림 설정 오류: " + e.getMessage());
        }
    }

    private void listenForMessages() {
        try {
            String message;
            while ((message = input.readLine()) != null) {
                appendChatLog("상대방: " + message);
            }
        } catch (Exception e) {
            appendChatLog("메시지 수신 오류: " + e.getMessage());
        }
    }

    private void sendMessage() {
        String message = messageInput.getText().toString();
        if (!message.isEmpty() && output != null) {
            output.println(message);
            appendChatLog("나: " + message);
            messageInput.setText("");

            // 메시지 전송 후 스크롤 맨 아래로 이동
            chatScrollView.post(() -> chatScrollView.fullScroll(View.FOCUS_DOWN));
        }
    }

    private void appendChatLog(String message) {
        runOnUiThread(() -> {
            chatLog.append(message + "\n");
            chatScrollView.post(() -> chatScrollView.fullScroll(View.FOCUS_DOWN));
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (socket != null) socket.close();
            if (serverSocket != null) serverSocket.close();
        } catch (Exception e) {
            appendChatLog("종료 오류: " + e.getMessage());
        }
    }
}