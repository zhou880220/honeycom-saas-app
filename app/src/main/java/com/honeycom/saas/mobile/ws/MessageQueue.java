package com.honeycom.saas.mobile.ws;

import java.util.LinkedList;

public class MessageQueue {

    private LinkedList<String> list = new LinkedList<>();

//    public MessageQueue(int capacity) {
//        this.capacity = capacity;
//    }

    public String take() {
        String message = "";
        if (!list.isEmpty()) message = list.removeFirst();
        return message;
    }

    public void put(String message) {
        if (list.size() > 100) list = new LinkedList<>();
        list.add(message);
    }

}
