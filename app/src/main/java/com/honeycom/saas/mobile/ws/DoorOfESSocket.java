package com.honeycom.saas.mobile.ws;

import com.honeycom.saas.mobile.ws.server.WSServer;
import com.honeycom.saas.mobile.ws.worker.HTMLMsgAcceptee;
import com.honeycom.saas.mobile.ws.worker.SocketServer;

public class DoorOfESSocket {
    public Thread listerT = null;
    public Thread senderT = null;
    public Thread wsT = null;

    static MessageQueue queueOfInstruct = new MessageQueue();
    static MessageQueue queueOfES = new MessageQueue();
    public static boolean bqInterrupt = false;


    public DoorOfESSocket(String selfServePort) {
        if (wsT == null) {
            wsT = new Thread(() -> {
                WSServer.startWSServer(queueOfInstruct, queueOfES, Integer.parseInt(selfServePort));
            });
            wsT.start();
        }
    }

    public static void putInstructMsg(String msg) {
        queueOfInstruct.put(msg);
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
                new SocketServer(queueOfInstruct, queueOfES).launching(ip, Integer.parseInt(port));
            });
            listerT.start();
        }
        // sender for clean or something
        if (senderT == null) {
            senderT = new Thread(() -> {
                 new HTMLMsgAcceptee(queueOfInstruct, queueOfES).launching(ip, Integer.parseInt(port));
            });
            senderT.start();
        }
    }

    public static void pushMsgByCurrConn(String message){
        queueOfES.put(message);
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

