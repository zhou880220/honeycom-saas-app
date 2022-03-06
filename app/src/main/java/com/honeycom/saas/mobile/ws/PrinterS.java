package com.honeycom.saas.mobile.ws;

import java.io.DataOutputStream;
import java.net.Socket;

public class PrinterS {


    public void run(String ip, String port, String zplStr) throws Exception {

        try (Socket clientSocket = new Socket(ip, Integer.parseInt(port))) {
            DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
            byte[] bs = zplStr.getBytes("gb18030");
            outToServer.write(bs);
            outToServer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
