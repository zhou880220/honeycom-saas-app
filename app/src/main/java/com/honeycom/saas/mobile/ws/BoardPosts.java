package com.honeycom.saas.mobile.ws;

public class BoardPosts {
    public Thread listerT = null;
    public Thread senderT = null;
    public Thread wsT = null;

    MessageQueue mq = new MessageQueue();
    MessageQueue sq = new MessageQueue();
//    MessageQueue messageQueue = new MessageQueue();
//    MessageQueue sendQueue = new MessageQueue();
    public static boolean bqInterrupt = false;


//    public BoardPosts(MessageQueue mq, MessageQueue sq, String selfServePort) {
    public BoardPosts(String selfServePort) {
        this.mq = mq;
        this.sq = sq;

        // WS server
        if (wsT == null) {
            wsT = new Thread(() -> {
                WSServer.startWSServer(mq, sq, Integer.parseInt(selfServePort));
            });
            wsT.start();
        }
    }


    public void switchNetwork(String ip, String port) {
        try {
            cleanNetwork();
        } catch (Exception e) {
            e.printStackTrace();
        }

        bqInterrupt = false;
        if (listerT != null || senderT != null) {
            if (listerT.isAlive()) listerT.interrupt();
            if (senderT.isAlive()) listerT.interrupt();
            return;
        }

        // listen
        if (listerT == null) {
            listerT = new Thread(() -> {
                new SocketServer(mq, sq).launching(ip, Integer.parseInt(port));
            });
            listerT.start();
        }
        // sender for clean or something
        if (senderT == null) {
            senderT = new Thread(() -> {
                new Sender(mq, sq).launching(ip, Integer.parseInt(port));
            });
            senderT.start();
        }
    }


    private void cleanNetwork() throws InterruptedException {
        bqInterrupt = true;
        Thread.sleep(500);
        if (listerT != null) {
            listerT.interrupt();
            listerT = null;
        }
        if (senderT != null) {
            senderT.interrupt();
            senderT = null;
        }
    }
}










