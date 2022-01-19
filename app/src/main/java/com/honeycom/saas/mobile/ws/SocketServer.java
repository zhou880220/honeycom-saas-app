package com.honeycom.saas.mobile.ws;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * 自定义socket
 */
public class SocketServer {
    // public class SS implements Runnable {
    // ---------------------------------------------------------------------------------------------
    MessageQueue mq;
    MessageQueue sendQ;

    public SocketServer(MessageQueue mq, MessageQueue sendQ) {
        this.mq = mq;
        this.sendQ = sendQ;
    }
    // ---------------------------------------------------------------------------------------------

    public void launching(String ip, int port) {
        Log.e("SocketServer", "start listen..."+ip);
        String hostname = ip; //"172.16.6.220";
        Log.e("SocketServer", "launched...");
        try (Socket socket = new Socket(hostname, port)) {
            InputStream is = socket.getInputStream();
            PrintWriter pw = new PrintWriter(socket.getOutputStream());
//            while (true) {
            Log.e("SocketServer", "looping...");
                byte[] buffer = new byte[1024];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    String output = new String(buffer, 0, read);
                    mq.put(output);
                }
//            }
        } catch (UnknownHostException ex) {
            Log.e("SocketServer", "Server not found: "+ ex.getMessage());
        } catch (IOException ex) {
            Log.e("SocketServer", "I/O error:  "+ ex.getMessage());
        }

    }


}










