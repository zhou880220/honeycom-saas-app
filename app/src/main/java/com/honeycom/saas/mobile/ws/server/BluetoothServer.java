package com.honeycom.saas.mobile.ws.server;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.ParcelUuid;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Set;

//import org.apache.commons.io.FileUtils;

public class BluetoothServer {

    private OutputStream outputStream;
    private InputStream inStream;
    private BluetoothDevice device = null;
    private BluetoothSocket socket = null; //  = device.createRfcommSocketToServiceRecord(uuids[0].getUuid());
//    private void init() throws IOException {
//        BluetoothAdapter blueAdapter = BluetoothAdapter.getDefaultAdapter();
//        if (blueAdapter != null) {
//            if (blueAdapter.isEnabled()) {
//                Set<BluetoothDevice> bondedDevices = blueAdapter.getBondedDevices();
//
//                if(bondedDevices.size() > 0) {
//                    Object[] devices = (Object []) bondedDevices.toArray();
//                    BluetoothDevice device = (BluetoothDevice) devices[position];
//
//                    ParcelUuid[] uuids = device.getUuids();
//                    BluetoothSocket socket = device.createRfcommSocketToServiceRecord(uuids[0].getUuid());
//                    socket.connect();
//                    outputStream = socket.getOutputStream();
//                    inStream = socket.getInputStream();
//                }
//
//                Log.e("error", "No appropriate paired devices.");
//            } else {
//                Log.e("error", "Bluetooth is disabled.");
//            }
//        }
//    }

    public boolean isDeviceNull() {
        return this.device == null;
    }

    public String lockDevice(String addr) throws Exception {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null) {
            // Device doesn't support Bluetooth
            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
            if (pairedDevices.size() > 0) {
                // There are paired devices. Get the name and address of each paired device.
                for (BluetoothDevice device : pairedDevices) {
                    String deviceName = device.getName();
                    String curaddr = device.getAddress();
                    String deviceHardwareAddress = device.getAddress(); // MAC address
                    Log.e("BluetoothServer", deviceName);
//                    if (deviceName.equals("DP-230L-D918")) {
                    if (addr.equals(curaddr)) {
//                        printTest(device);
                        this.device = device;
                        return "founded";
                    }
                    Log.e("BluetoothServer", deviceHardwareAddress);
                }
            }
        }
        throw new Exception("can not found device.");
    }


    public void settleConn() throws IOException {
        synchronized (this) {
            if (device == null) return;
            ParcelUuid[] uuids = device.getUuids();
            if (socket != null && socket.isConnected()) socket.close();
            socket = device.createRfcommSocketToServiceRecord(uuids[0].getUuid());
            socket.connect();
            outputStream = socket.getOutputStream();
            inStream = socket.getInputStream();
        }
    }

    public void write(String s) throws Exception {
        if (!this.socket.isConnected()) throw new Exception("connection lost.");
        byte[] bs = s.getBytes("gb18030");
        outputStream.write(bs);
    }

    public void close() throws IOException {
        socket.close();
    }

    public void run() {
        final int BUFFER_SIZE = 1024;
        byte[] buffer = new byte[BUFFER_SIZE];
        int bytes = 0;
        int b = BUFFER_SIZE;

        while (true) {
            try {
                bytes = inStream.read(buffer, bytes, BUFFER_SIZE - bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
