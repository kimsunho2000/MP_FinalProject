package com.example.finalproject;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView recyclerViewMessages;
    private EditText messageInput;
    private Button sendMessageButton;

    private MessageAdapter messageAdapter;
    private List<Message> messageList;

    private ServerSocket serverSocket;
    private Socket socket;
    private PrintWriter output;
    private BufferedReader input;

    private boolean isServer;
    private String nickname;
    private static final int PORT = 12345;
    private static final String TAG = "ChatActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // UI 요소 초기화
        TextView nicknameDisplay = findViewById(R.id.nicknameDisplay); // 닉네임 표시 TextView
        recyclerViewMessages = findViewById(R.id.recyclerViewMessages);
        messageInput = findViewById(R.id.messageInput);
        sendMessageButton = findViewById(R.id.sendMessageButton);

        // RecyclerView 초기화
        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(messageList);
        recyclerViewMessages.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewMessages.setAdapter(messageAdapter);

        // 닉네임 설정
        nickname = getIntent().getStringExtra("nickname");
        if (nickname == null || nickname.isEmpty()) {
            nickname = "Anonymous";
        }
        nicknameDisplay.setText(nickname);

        // 서버/클라이언트 초기화
        isServer = getIntent().getBooleanExtra("isServer", false);
        if (isServer) {
            initializeServer();
        } else {
            String serverIp = getIntent().getStringExtra("serverIp");
            if (serverIp == null || serverIp.isEmpty()) {
                addMessageToUI(new Message("서버 IP가 설정되지 않았습니다.", Message.TYPE_INFO));
            } else {
                initializeClient(serverIp);
            }
        }

        sendMessageButton.setOnClickListener(v -> sendMessage());
    }

    private void initializeServer() {
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(PORT);
                addMessageToUI(new Message("서버가 포트 " + PORT + "에서 대기 중입니다.", Message.TYPE_INFO));
                socket = serverSocket.accept();
                addMessageToUI(new Message("클라이언트 연결됨: " + socket.getInetAddress(), Message.TYPE_INFO));

                setupStreams();
                listenForMessages();
            } catch (Exception e) {
                addMessageToUI(new Message("서버 오류: " + e.getMessage(), Message.TYPE_INFO));
                Log.e(TAG, "서버 오류 발생", e);
            }
        }).start();
    }

    private void initializeClient(String serverIp) {
        new Thread(() -> {
            try {
                addMessageToUI(new Message("서버 연결 시도: " + serverIp + " 포트넘버: " + PORT, Message.TYPE_INFO));
                socket = new Socket(serverIp, PORT);
                addMessageToUI(new Message("서버에 연결됨", Message.TYPE_INFO));

                setupStreams();
                listenForMessages();
            } catch (Exception e) {
                addMessageToUI(new Message("클라이언트 오류: " + e.getMessage(), Message.TYPE_INFO));
                Log.e(TAG, "클라이언트 연결 오류", e);
            }
        }).start();
    }

    private void setupStreams() {
        try {
            output = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (Exception e) {
            addMessageToUI(new Message("스트림 설정 오류: " + e.getMessage(), Message.TYPE_INFO));
            Log.e(TAG, "스트림 설정 오류", e);
        }
    }

    private void listenForMessages() {
        try {
            String message;
            while ((message = input.readLine()) != null) {
                addMessageToUI(new Message(message, Message.TYPE_RECEIVED));
            }
        } catch (Exception e) {
            addMessageToUI(new Message("메시지 수신 오류: " + e.getMessage(), Message.TYPE_INFO));
            Log.e(TAG, "메시지 수신 오류", e);
        }
    }

    private void sendMessage() {
        String message = messageInput.getText().toString();
        if (!message.isEmpty() && output != null) {
            new Thread(() -> {
                try {
                    String fullMessage = nickname + ": " + message;
                    output.println(fullMessage);
                    addMessageToUI(new Message(fullMessage, Message.TYPE_SENT));
                    runOnUiThread(() -> messageInput.setText(""));
                } catch (Exception e) {
                    addMessageToUI(new Message("메시지 전송 오류: " + e.getMessage(), Message.TYPE_INFO));
                    Log.e(TAG, "메시지 전송 오류", e);
                }
            }).start();
        }
    }

    private void addMessageToUI(Message message) {
        runOnUiThread(() -> {
            messageList.add(message);
            messageAdapter.notifyItemInserted(messageList.size() - 1);
            recyclerViewMessages.scrollToPosition(messageList.size() - 1);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "소켓 닫기 오류", e);
        }
    }
}