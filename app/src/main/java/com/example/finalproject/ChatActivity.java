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

public class ChatActivity extends AppCompatActivity { //채팅 시작했을때의 엑티비티

    private TextView nicknameDisplay;
    private RecyclerView recyclerViewMessages;
    private EditText messageInput;
    private Button sendMessageButton;

    private ServerSocket serverSocket;
    private Socket socket;
    private PrintWriter output;
    private BufferedReader input;

    private boolean isServer;
    private String nickname;
    private boolean isConnected = false; // 서버 연결 상태 플래그
    private static final int PORT = 12345;
    private static final String TAG = "ChatActivity";

    private MessageAdapter messageAdapter;
    private List<Message> messageList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // UI 요소 초기화 및 바인딩
        nicknameDisplay = findViewById(R.id.nicknameDisplay);
        recyclerViewMessages = findViewById(R.id.recyclerViewMessages);
        messageInput = findViewById(R.id.messageInput);
        sendMessageButton = findViewById(R.id.sendMessageButton);

        // RecyclerView 설정,MessageAdapter를 활용하여 메모리 절약 및 스크롤 뷰에 적용
        messageAdapter = new MessageAdapter(messageList);
        recyclerViewMessages.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewMessages.setAdapter(messageAdapter);

        // 역할 및 닉네임 설정
        isServer = getIntent().getBooleanExtra("isServer", false);
        nickname = getIntent().getStringExtra("nickname");
        if (nickname == null || nickname.isEmpty()) {
            nickname = isServer ? "Server" : "Client";
        }
        nicknameDisplay.setText(nickname); //디스플레아에 닉네임 표시

        // 화면 회전 데이터 복원
        if (savedInstanceState != null) {
            messageList.addAll((List<Message>) savedInstanceState.getSerializable("messageList"));
            messageAdapter.notifyDataSetChanged();
            isConnected = savedInstanceState.getBoolean("isConnected", false);
        }

        // 서버 또는 클라이언트 초기화
        if (isServer) {
            initializeServer();
        } else {
            String serverIp = getIntent().getStringExtra("serverIp");
            if (serverIp == null || serverIp.isEmpty()) {
                appendMessage(new Message("서버 IP가 설정되지 않았습니다.", Message.TYPE_INFO));
            } else {
                initializeClient(serverIp);
            }
        }

        // 메시지 전송 버튼 리스너
        sendMessageButton.setOnClickListener(v -> sendMessage());
    }

    //서버 초기화
    private void initializeServer() {
        if (isConnected) return; // 이미 연결 상태라면 초기화하지 않음

        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(PORT);
                appendMessage(new Message("서버가 포트 " + PORT + "에서 대기 중입니다.", Message.TYPE_INFO));

                socket = serverSocket.accept();
                appendMessage(new Message("클라이언트 연결됨: " + socket.getInetAddress(), Message.TYPE_INFO));
                isConnected = true; // 연결 상태 업데이트

                setupStreams();
                listenForMessages();
            } catch (Exception e) {
                appendMessage(new Message("서버 오류: " + e.getMessage(), Message.TYPE_INFO));
                Log.e(TAG, "서버 오류 발생", e);
            }
        }).start();
    }

    //클라이언트 초기화
    private void initializeClient(String serverIp) {
        if (isConnected) return; // 이미 연결 상태라면 초기화하지 않음

        new Thread(() -> {
            try {
                appendMessage(new Message("서버 연결 시도: " + serverIp + ":" + PORT, Message.TYPE_INFO));
                socket = new Socket(serverIp, PORT);
                appendMessage(new Message("서버에 연결됨", Message.TYPE_INFO));
                isConnected = true; // 연결 상태 업데이트

                setupStreams();
                listenForMessages();
            } catch (Exception e) {
                appendMessage(new Message("클라이언트 오류: " + e.getMessage(), Message.TYPE_INFO));
                Log.e(TAG, "클라이언트 연결 오류", e);
            }
        }).start();
    }

    //스트림 설정,소켓과 서버를 연결하는 파이프
    private void setupStreams() {
        try {
            output = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (Exception e) {
            appendMessage(new Message("스트림 설정 오류: " + e.getMessage(), Message.TYPE_INFO));
            Log.e(TAG, "스트림 설정 오류", e);
        }
    }

   //메세지 수신 모듈
    private void listenForMessages() {
        try {
            String message;
            while ((message = input.readLine()) != null) {
                appendMessage(new Message(message, Message.TYPE_RECEIVED));
            }
        } catch (Exception e) {
            appendMessage(new Message("메시지 수신 오류: " + e.getMessage(), Message.TYPE_INFO));
            Log.e(TAG, "메시지 수신 오류", e);
        }
    }

    //메세지 전송 모듈
    private void sendMessage() {
        String message = messageInput.getText().toString();
        if (!message.isEmpty() && output != null) {
            new Thread(() -> {
                try {
                    String fullMessage = nickname + ": " + message;
                    output.println(fullMessage);
                    appendMessage(new Message(fullMessage, Message.TYPE_SENT));
                    runOnUiThread(() -> messageInput.setText(""));
                } catch (Exception e) {
                    appendMessage(new Message("메시지 전송 오류: " + e.getMessage(), Message.TYPE_INFO));
                    Log.e(TAG, "메시지 전송 오류", e);
                }
            }).start();
        }
    }

    //메세지 추가 모듈
    private void appendMessage(Message message) {
        runOnUiThread(() -> {
            messageList.add(message);
            messageAdapter.notifyItemInserted(messageList.size() - 1);
            recyclerViewMessages.scrollToPosition(messageList.size() - 1);
        });
    }

    //화면 회전시 데이터 저장
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("messageList", new ArrayList<>(messageList));
        outState.putBoolean("isConnected", isConnected);
    }

    //소켓 닫는 모듈
    private void closeSocket() {
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeSocket();
    }
}