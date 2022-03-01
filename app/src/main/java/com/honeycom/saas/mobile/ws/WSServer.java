package com.honeycom.saas.mobile.ws;

import android.util.Log;

import org.java_websocket.WebSocket;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.DefaultSSLWebSocketServerFactory;
import org.java_websocket.server.WebSocketServer;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Collections;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.security.cert.CertificateException;
import javax.security.cert.X509Certificate;


/**
 * A simple WebSocketServer implementation. Keeps track of a "chatroom".
 */
public class WSServer extends WebSocketServer {

    public static volatile StringBuffer shareBuf = new StringBuffer();

    MessageQueue mq;
    MessageQueue sendQ;
    public static String currentMsg = "";

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
        System.out.println(
                conn.getRemoteSocketAddress().getAddress().getHostAddress() + " entered the room!");
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        broadcast(conn + " has left the room!");
        System.out.println(conn + " has left the room!");
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        broadcast(message);
        System.out.println("ws-s: " + conn + ": " + message);
        sendQ.put(message);
    }

    @Override
    public void onMessage(WebSocket conn, ByteBuffer message) {
        broadcast(message.array());
        System.out.println(conn + ": " + message);
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
        System.out.println("Server started!");
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
//            SSLContext sslContext = getSSL();
//            s.setWebSocketFactory(new DefaultSSLWebSocketServerFactory(sslContext));
            s.start();
            System.out.println("ChatServer started on port: " + s.getPort());

            BufferedReader sysin = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
//            String in = sysin.readLine();
//            String in = "test code";//sysin.readLine();
//            WSServer.shareBuf.to
//                Thread.sleep(500);
                String taskStr = mq.take();
//                Log.d(WebViewActivity.WVTaskName, taskStr);
                if (taskStr.length() > 0)  {
                    s.broadcast(taskStr);
                    currentMsg = taskStr;
                }
                Thread.sleep(50);
//                if (bqInterrupt) {
//                    Thread.currentThread().interrupt();
//                }
//            if (in.equals("exit")) {
//                s.stop(1000);
//                break;
//            }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
//        finally {
//            Thread.currentThread().interrupt();
//        }
    }
//    /**
//     * Method which returns a SSLContext from a keystore or IllegalArgumentException on error
//     *
//     * @return a valid SSLContext
//     * @throws IllegalArgumentException when some exception occurred
//     */
//    private SSLContext getSSLContextFromKeystore() {
//        // load up the key store
//        String storeType = "JKS";
//        String keystore = "server.keystore";
//        String storePassword = "123456";
//        String keyPassword = "123456";
//
//        KeyStore ks;
//        SSLContext sslContext;
//        try {
//            // alias key0
//            InputStream()
//            ks = KeyStore.getInstance(storeType);
//            Path file = Paths.get("honeycomb-saas.jks", keystore);
//            ks.load(Files.newInputStream(file), storePassword.toCharArray());
//            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
//            kmf.init(ks, keyPassword.toCharArray());
//            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
//            tmf.init(ks);
//
//
//            sslContext = SSLContext.getInstance("TLS");
//            sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
//        } catch (KeyStoreException | IOException
//                | NoSuchAlgorithmException | KeyManagementException
//                | UnrecoverableKeyException | java.security.cert.CertificateException e) {
//            throw new IllegalArgumentException();
//        }
//        return sslContext;
//    }
//    private static SSLContext getSSL(){
//
//    }
}