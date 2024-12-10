package com.example.finalproject;

import android.os.Bundle;
import android.util.Log;
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

    private boolean isServer; // 서버/클라이언트 역할
    private static final int PORT = 12345; // 포트 번호
    private static final String TAG = "ChatActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // UI 요소 초기화
        chatLog = findViewById(R.id.chatLog);
        messageInput = findViewById(R.id.messageInput);
        sendMessageButton = findViewById(R.id.sendMessageButton);
        chatRole = findViewById(R.id.chatRole);
        chatScrollView = findViewById(R.id.chatScrollView);

        // 서버/클라이언트 역할 결정
        isServer = getIntent().getBooleanExtra("isServer", false);
        if (isServer) {
            chatRole.setText("Role: Server");
            initializeServer();
        } else {
            chatRole.setText("Role: Client");
            String serverIp = getIntent().getStringExtra("serverIp");
            if (serverIp == null || serverIp.isEmpty()) {
                appendChatLog("서버 IP가 설정되지 않았습니다.");
            } else {
                initializeClient(serverIp);
            }
        }

        // 메시지 전송 버튼
        sendMessageButton.setOnClickListener(v -> sendMessage());
    }

    /**
     * 서버 초기화
     */
    private void initializeServer() {
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(PORT);
                appendChatLog("서버 소켓 생성 완료, 포트: " + PORT);
                Log.d(TAG, "서버가 모든 인터페이스에서 포트 " + PORT + "에서 실행 중입니다.");

                socket = serverSocket.accept(); // 클라이언트 연결 대기
                appendChatLog("클라이언트 연결됨: " + socket.getInetAddress());
                Log.d(TAG, "클라이언트 연결 성공: " + socket.getInetAddress());

                setupStreams(); // 스트림 설정
                listenForMessages(); // 메시지 수신
            } catch (Exception e) {
                appendChatLog("서버 오류: " + e.getMessage());
                Log.e(TAG, "서버 오류 발생", e);
            }
        }).start();
    }

    /**
     * 클라이언트 초기화
     */
    private void initializeClient(String serverIp) {
        new Thread(() -> {
            try {
                appendChatLog("서버 연결 시도: " + serverIp + ":" + PORT);
                Log.d(TAG, "클라이언트 소켓 연결 시도: " + serverIp + ":" + PORT);

                socket = new Socket(serverIp, PORT); // 서버 연결
                appendChatLog("서버에 연결됨");
                Log.d(TAG, "서버 연결 성공: " + serverIp + ":" + PORT);

                setupStreams(); // 스트림 설정
                listenForMessages(); // 메시지 수신
            } catch (Exception e) {
                appendChatLog("클라이언트 오류: " + e.getMessage());
                Log.e(TAG, "클라이언트 연결 오류", e);
            }
        }).start();
    }

    /**
     * 스트림 설정
     */
    private void setupStreams() {
        try {
            output = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            appendChatLog("스트림 연결 성공");
            Log.d(TAG, "스트림 연결 성공: Input/Output 스트림이 설정되었습니다.");
        } catch (Exception e) {
            appendChatLog("스트림 설정 오류: " + e.getMessage());
            Log.e(TAG, "스트림 설정 오류", e);
        }
    }

    /**
     * 메시지 수신
     */
    private void listenForMessages() {
        try {
            String message;
            while ((message = input.readLine()) != null) {
                Log.d(TAG, "메시지 수신: " + message);
                appendChatLog("상대방: " + message);
            }
        } catch (Exception e) {
            appendChatLog("메시지 수신 오류: " + e.getMessage());
            Log.e(TAG, "메시지 수신 오류", e);
        }
    }

    /**
     * 메시지 전송
     */
    private void sendMessage() {
        String message = messageInput.getText().toString();
        if (!message.isEmpty() && output != null) {
            new Thread(() -> {
                try {
                    Log.d(TAG, "메시지 전송: " + message);
                    output.println(message); // 메시지 전송
                    runOnUiThread(() -> {
                        appendChatLog("나: " + message); // 로그 업데이트
                        messageInput.setText(""); // 입력 필드 초기화
                    });
                } catch (Exception e) {
                    runOnUiThread(() -> appendChatLog("메시지 전송 오류: " + e.getMessage()));
                    Log.e(TAG, "메시지 전송 오류", e);
                }
            }).start();
        }
    }

    /**
     * 로그 추가
     */
    private void appendChatLog(String message) {
        runOnUiThread(() -> {
            chatLog.append(message + "\n");
            chatScrollView.post(() -> chatScrollView.fullScroll(ScrollView.FOCUS_DOWN));
        });
    }

    /**
     * 소켓 닫기
     */
    private void closeSocket() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
                Log.d(TAG, "소켓이 닫혔습니다.");
            }
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                Log.d(TAG, "서버 소켓이 닫혔습니다.");
            }
        } catch (Exception e) {
            Log.e(TAG, "소켓 닫기 오류", e);
        }
    }

    /**
     * 리소스 정리
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeSocket();
    }
}