package com.honeycom.saas.mobile.ws;

import java.io.DataOutputStream;
import java.net.Socket;

public class DoorOfPrinterBySocket {


    public boolean run(String ip, String port, String zplStr) throws Exception {
        synchronized (this) {
            boolean res = doPrint(ip, port, zplStr);
//            if (!res) {
//                // 尝试二次打印 避免 目前设备 热机丢数据.
//                Thread.sleep(500);
//                return doPrint(ip, port, zplStr);
//            }
        }
        return true;
    }

    private boolean doPrint(String ip, String port, String zplStr) {
        try (Socket clientSocket = new Socket(ip, Integer.parseInt(port))) {
            DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
            byte[] bs = zplStr.getBytes("gb18030");
            outToServer.write(bs);
            outToServer.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
