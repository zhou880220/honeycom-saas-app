package com.honeycom.saas.mobile.ws;

import java.io.IOException;

public class BoardPostsBlueTooth {
    // ---------------------------------------------------------------------------------------------

    public BluetoothServer bts = null;

    public boolean initBT(String addr) {
        if (this.bts == null) {
            this.bts = new BluetoothServer();
        }
        if (this.bts.isDeviceNull()) {
//                Toasty.info(getApplicationContext(), "bluetooth init success.", Toast.LENGTH_LONG, false).show();
            try {
                synchronized (this) {
                    this.bts.lockDevice(addr);
                    this.bts.settleConn();
                }
                return true;
            } catch (IOException e) {
//                    Toasty.warning(getApplicationContext(), "init failed", Toast.LENGTH_LONG, false).show();
                e.printStackTrace();
                return false;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    public boolean BTPrint(String zplStr) {
        try {
            this.bts.write(zplStr);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean BTClose() {
        try {
            this.bts.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean BTPrintByDirect(String ip, String port, String zplString) {
//            Toasty.info(getApplicationContext(), jsonString, Toast.LENGTH_SHORT, true).show();
//        String test = String.valueOf((int) (Math.random() * 50 + 1));
        new Thread(() -> {
            PrinterS s = new PrinterS();
            try {
                s.run(ip, port, zplString);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
        return true;
//            return wifiIpAddress(getApplicationContext());
    }
}










