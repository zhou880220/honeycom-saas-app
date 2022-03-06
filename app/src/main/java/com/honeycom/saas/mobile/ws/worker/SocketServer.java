package com.honeycom.saas.mobile.ws.worker;

import com.honeycom.saas.mobile.ws.MessageQueue;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import static com.honeycom.saas.mobile.ws.BoardPostsESSocket.bqInterrupt;

//import static tech.beeio.v2ws_client.ws.BoardPosts.bqInterrupt;

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
        System.out.println("start listen...");
//        String hostname = "172.16.6.220";
        String hostname = ip; //"172.16.6.220";
//        int port = 18899;
        System.out.println("sss launched...");
        try (Socket socket = new Socket(hostname, port)) {
            InputStream is = socket.getInputStream();
            PrintWriter pw = new PrintWriter(socket.getOutputStream());
//            while (true) {
                System.out.println("sss looping.");
                byte[] buffer = new byte[1024];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    if (bqInterrupt) {
                        Thread.currentThread().interrupt();
                    }
                    Thread.sleep(50);
                    String output = new String(buffer, 0, read);
                    mq.put(output);
                }
//            }
        } catch (UnknownHostException ex) {
            System.out.println("Server not found: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("I/O error: " + ex.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            Thread.currentThread().interrupt();
        }
    }


}










