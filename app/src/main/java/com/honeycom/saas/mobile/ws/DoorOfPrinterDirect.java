package com.honeycom.saas.mobile.ws;

import com.google.gson.Gson;
import com.honeycom.saas.mobile.ws.bean.PrintBean;

import java.io.DataOutputStream;
import java.net.Socket;

public class DoorOfPrinterDirect {


    public static int status = 0;

    public static boolean run(String data) throws Exception {
        status = 0;
        Gson gson = new Gson();
        PrintBean printBean = gson.fromJson(data, PrintBean.class);
        String test = String.valueOf((int) (Math.random() * 50 + 1));
        new Thread(() -> {
            DoorOfPrinterBySocket s = new DoorOfPrinterBySocket();
            try {
                if (s.run(printBean.getIp(), printBean.getPort(), printBean.getZplString())) {
                    status = 1;
                }
            } catch (Exception e) {
                e.printStackTrace();
                status = 0;
            }
        }).start();
        Thread.sleep(500);
        return status == 1;
    }
}
