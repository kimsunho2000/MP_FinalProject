/* ----- 서버 구동 자바 코드 ----- */
package com.example.finalproject;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class LocalSocketServer {

    private static final int PORT = 12345; // 서버 포트 번호 설정(중복시 변경하면됨)
    private final Set<Socket> clientSockets = ConcurrentHashMap.newKeySet(); // 연결된 클라이언트 소켓 관리

    public static void main(String[] args) {
        new LocalSocketServer().startServer();
    }

    public void startServer() { //서버 시작 모듈
        System.out.println("------ 서버 초기화 -----");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("서버가 포트 " + PORT + "에서 대기 중입니다...");

            while (true) {
                Socket clientSocket = serverSocket.accept(); // 클라이언트 연결 대기
                System.out.println("클라이언트 연결됨: " + clientSocket.getInetAddress());

                clientSockets.add(clientSocket); // 클라이언트 소켓 추가

                // 각 클라이언트를 별도의 스레드에서 처리,자바는 스레드에서 네트워크 프로세스를 처리해야함.
                new Thread(() -> handleClient(clientSocket)).start();
            }
        } catch (IOException e) {
            System.err.println("서버 소켓 생성 오류: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleClient(Socket clientSocket) { //클라이언트를 처리하는 메소드
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            String clientAddress = clientSocket.getInetAddress().getHostAddress();
            System.out.println("클라이언트 [" + clientAddress + "] 처리 시작");

            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("클라이언트 [" + clientAddress + "]로부터 수신: " + message);

                // 다른 클라이언트로 메시지 브로드캐스트,Broadcast주소를 사용(?)
                broadcastMessage(clientSocket,message);
            }
        } catch (IOException e) {
            System.err.println("클라이언트 처리 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                clientSockets.remove(clientSocket); // 클라이언트 소켓 제거
                clientSocket.close();
                System.out.println("클라이언트 연결 종료");
            } catch (IOException e) {
                System.err.println("클라이언트 소켓 닫기 실패: " + e.getMessage());
            }
        }
    }

    private void broadcastMessage(Socket sender, String message) { //메세지 브로드캐스팅
        for (Socket client : clientSockets) {
            if (client != sender) { // 메시지를 보낸 클라이언트는 제외하고 나머지 클라이언트에만 전송
                try {
                    PrintWriter out = new PrintWriter(client.getOutputStream(), true);
                    out.println(message);
                    System.out.println("브로드캐스트 메시지 전송: " + message);
                } catch (IOException e) {
                    System.err.println("메시지 전송 실패: " + e.getMessage());
                }
            }
        }
    }
}