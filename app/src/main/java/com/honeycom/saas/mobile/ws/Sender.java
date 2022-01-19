package com.honeycom.saas.mobile.ws;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * 自定义队列发送
 */
public class Sender {
    // ---------------------------------------------------------------------------------------------
    MessageQueue mq;
    MessageQueue sendQ;

    public Sender(MessageQueue mq, MessageQueue sendQ) {
        this.mq = mq;
        this.sendQ = sendQ;
    }

    // ---------------------------------------------------------------------------------------------
    public void launching(String ip, int port) {
        String hostname = ip; //"172.16.6.220";
        try (Socket socket = new Socket(hostname, port)) {
            InputStream is = socket.getInputStream();
            PrintWriter pw = new PrintWriter(socket.getOutputStream());
            while (true) {
                String msg = sendQ.take();
                Log.d("Sender","sending");
                OutputStream out = socket.getOutputStream();
                PrintWriter op = new PrintWriter(out);
                op.println(msg);
                op.flush();
//                op.close();
            }
        } catch (UnknownHostException ex) {
            Log.e("SocketServer", "Server not found:" + ex.getMessage());
        } catch (IOException ex) {
            Log.e("SocketServer", "I/O error: " + ex.getMessage());
        }

    }
}










