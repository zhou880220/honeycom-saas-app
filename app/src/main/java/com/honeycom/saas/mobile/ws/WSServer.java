package com.honeycom.saas.mobile.ws;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Collections;

import org.java_websocket.WebSocket;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;


/**
 * A simple WebSocketServer implementation. Keeps track of a "chatroom".
 */
public class WSServer extends WebSocketServer {

    public static volatile StringBuffer shareBuf = new StringBuffer();

    MessageQueue mq;
    MessageQueue sendQ;

    public WSServer(int port, MessageQueue mq, MessageQueue sendQ) throws UnknownHostException {
        super(new InetSocketAddress(port));
        this.mq = mq;
        this.sendQ = sendQ;
    }

    public WSServer(InetSocketAddress address) {
        super(address);
    }

    public WSServer(int port, Draft_6455 draft) {
        super(new InetSocketAddress(port), Collections.<Draft>singletonList(draft));
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        conn.send("ws-s Welcome to the server!"); //This method sends a message to the new client
        broadcast("new connection: " + handshake
                .getResourceDescriptor()); //This method sends a message to all clients connected
        Log.e("WSServer", conn.getRemoteSocketAddress().getAddress().getHostAddress() + " entered the room!");
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        broadcast(conn + " has left the room!");
        Log.e("WSServer", conn + " has left the room!");
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        broadcast(message);
        Log.e("WSServer", "ws-s: " + conn + ": " + message);
        sendQ.put(message);
    }

    @Override
    public void onMessage(WebSocket conn, ByteBuffer message) {
        broadcast(message.array());
        Log.e("WSServer", conn + ": " + message);
//        String sendMsg = message.toString();
//        sendQ.put(sendMsg);
    }


    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
        if (conn != null) {
            // some errors like port binding failed may not be assignable to a specific websocket
        }
    }

    @Override
    public void onStart() {
        Log.e("WSServer", "Server started!");
        setConnectionLostTimeout(0);
        setConnectionLostTimeout(100);
    }
    public static void startWSServer(MessageQueue mq, MessageQueue sendQ, int port) {
//        int port = 8887; // 843 flash policy port
//        try {
////            port = Integer.parseInt(args[0]);
//        } catch (Exception ex) {
//        }
        try {
            WSServer s = new WSServer(port, mq, sendQ);
            s.start();
            Log.e("WSServer", "ChatServer started on port: " + s.getPort());

            BufferedReader sysin = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
//            String in = sysin.readLine();
//            String in = "test code";//sysin.readLine();
//            WSServer.shareBuf.to
//                Thread.sleep(500);
                String taskStr = mq.take();
                Log.e("WSServer", taskStr);
                s.broadcast(taskStr);
//            if (in.equals("exit")) {
//                s.stop(1000);
//                break;
//            }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}