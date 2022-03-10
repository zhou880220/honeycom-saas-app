package com.honeycom.saas.mobile.ws;

import com.google.gson.Gson;
import com.honeycom.saas.mobile.ws.bean.PrintBean;

import java.io.DataOutputStream;
import java.net.Socket;

public class DoorOfPrinterDirect {

    public static boolean run(String data) throws Exception {
        Gson gson = new Gson();
        PrintBean printBean = gson.fromJson(data, PrintBean.class);
        String test = String.valueOf((int) (Math.random() * 50 + 1));
//        new Thread(() -> {
            DoorOfPrinterBySocket s = new DoorOfPrinterBySocket();
            try {
                return s.run(printBean.getIp(), printBean.getPort(), printBean.getZplString());
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
//        }).start();
    }
}
