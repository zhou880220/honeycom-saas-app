package com.honeycom.saas.mobile.ws;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import static com.honeycom.saas.mobile.ws.BoardPosts.bqInterrupt;

//import tech.beeio.v2ws_client.WebViewActivity;

//import static tech.beeio.v2ws_client.ws.BoardPosts.bqInterrupt;

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
                if (bqInterrupt) {
                    Thread.currentThread().interrupt();
                }
                String msg = sendQ.take();
                Thread.sleep(200);
                OutputStream out = socket.getOutputStream();
                PrintWriter op = new PrintWriter(out);
                op.println(msg);
                op.flush();
//                op.close();
            }
        } catch (UnknownHostException ex) {
            System.out.println("Server not found: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("I/O error: " + ex.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            Thread.currentThread().interrupt();
        }

//        Thread.currentThread().interrupt();
    }
}










