package com.honeycom.saas.mobile.ws;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
                Thread.sleep(100);
                String msg = sendQ.take();
                if (!(msg.length() > 0)) continue;
                msg = msg.replace("escode:", "");
                OutputStream out = socket.getOutputStream();
                PrintWriter op = new PrintWriter(out);
//                if (isHexadecimal(msg)){
                try {
                    byte[] value = hexStringToByteArray(msg);
                    out.write(value);
                    out.flush();
                } catch (Exception e) {
                }
                try {
                    op.println(msg);
                    op.flush();
                } catch (Exception e) {
                }
//                } else {
//                }
//                op.println(msg);
//                op.flush();

            }
//            op.close();
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

    private static final Pattern HEXADECIMAL_PATTERN = Pattern.compile("\\p{XDigit}+");


    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    boolean isHexadecimal(String msg) {
        Pattern p = Pattern.compile("[0-9a-fA-F]+");
        Matcher m = p.matcher(msg);
        if (m.matches()) return true;
        return false;
    }

}










