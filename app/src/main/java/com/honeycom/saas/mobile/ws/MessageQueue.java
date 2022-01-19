package com.honeycom.saas.mobile.ws;

import java.util.LinkedList;

/**
 * 自定义消息队列
 */
public class MessageQueue {


    //使用有序的链表来充当队列
//    private LinkedList<Message> list = new LinkedList<>();
    private LinkedList<String> list = new LinkedList<>();

    //队列容量
    private int capacity;

    public MessageQueue(int capacity) {
        this.capacity = capacity;
    }

    //获取消息
    public String take() {
        //检查消息队列是否为空
        synchronized (list) {
            while (list.isEmpty()) {
//                log.info("当前队列为空，消费者线程等待");
                try {
                    list.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            //从头部取出消息并返回
            String message = list.removeFirst();
            //同时通知所有等待存入的线程（但是实际上是唤醒所有的线程，包括等待获取消息的线程）
//            log.info("消息被消费");
            list.notifyAll();
            return message;
        }
    }

    public void put(String message) {
        synchronized (list) {
//            new Message(message);
            //检查队列是否已经满了
            while (list.size() == capacity) {
//                log.info("当前队列已满，生产者线程等待");
                try {
                    list.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            //消息加入队列的尾部
            list.add(message);
//            log.info("消息被放入队列,{}", message.toString());
            //通知所有等待获取消息的线程（但是实际上是唤醒所有的线程，包括等待存在的线程）
            list.notifyAll();
        }
    }

}
