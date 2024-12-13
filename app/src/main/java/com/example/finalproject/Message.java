package com.example.finalproject;

public class Message { //메세지 객체,Background color를 설정하여 디자인적으로 활용함
    public static final int TYPE_SENT = 1;
    public static final int TYPE_RECEIVED = 2;
    public static final int TYPE_INFO = 3;

    private String text;
    private int type;

    public Message(String text, int type) {
        this.text = text;
        this.type = type;
    }

    public String getText() {
        return text;
    }

    public int getType() {
        return type;
    }
}