package com.honeycom.saas.mobile.ws.server;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.ParcelUuid;
import android.util.Log;

import com.honeycom.saas.mobile.ws.bean.Result;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

/**
 * 蓝牙打印服务
 */
public class BluetoothPrintService {

    private String TAG = "BluetoothPrintService_TAG";
    private String deviceAddress = null;
    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter
            .getDefaultAdapter();
    private BluetoothDevice device = null;
    private BluetoothSocket bluetoothSocket = null;
    private OutputStream outputStream = null;
    private InputStream inputStream = null;
    private boolean isConnection = false;


    public BluetoothPrintService(String deviceAddress) {
        super();
        this.deviceAddress = deviceAddress;
        this.device = this.bluetoothAdapter.getRemoteDevice(this.deviceAddress);
    }

    /**
     * 初始化连接蓝牙驱动
     *
     * @param mac 蓝牙设备mac地址
     * @return
     */
    public Result loadBluetoothDevice(String mac) {
        Result result = Result.success();
        if (bluetoothAdapter != null) {
            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
            if (pairedDevices != null && pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
                    String address = device.getAddress();
                    if (mac.equals(address)) {
                        this.device = device;
                    }
                }
            }
            if (this.device == null || pairedDevices == null || pairedDevices.size() == 0) {
                result = Result.failed();
                result.setMsg("未找到匹配蓝牙，使用前请先打开蓝牙并匹配，mac地址配置正确！");
            }
        } else {
            result = Result.failed();
            result.setMsg("设备不支持蓝牙连接或者蓝牙未打开！");
        }
        return result;
    }

    /**
     * 初始化连接蓝牙驱动
     *
     * @return
     */
    private Result connectBluetooth() {
        Result result = Result.success();
        isConnection = false;
        try {
            if (this.device == null) {
                result = Result.failed();
                result.setMsg("未找到匹配蓝牙，使用前请先打开蓝牙并匹配，mac地址配置正确！");
            }
            ParcelUuid[] uuids = device.getUuids();
            if (bluetoothSocket != null && bluetoothSocket.isConnected()) {
                bluetoothSocket.close();
            }
            bluetoothSocket = device.createRfcommSocketToServiceRecord(uuids[0].getUuid());
            bluetoothSocket.connect();
            outputStream = bluetoothSocket.getOutputStream();
            inputStream = bluetoothSocket.getInputStream();
            isConnection = true;
        } catch (IOException e) {
            result = Result.failed();
            result.setMsg("蓝牙连接异常！");
            result.setData("创建蓝牙连接异常: " + e.getMessage());
            Log.e(TAG, "创建蓝牙连接异常: " + e.getMessage());
        }
        return result;
    }

    /**
     * 打印内容
     *
     * @param data
     */
    public Result printData(String data) {
        Result result = Result.success();
        if (!this.isConnection) {
            result = Result.failed();
            result.setMsg("蓝牙未连接成功");
        }
        try {
            // 连接蓝牙
            result = connectBluetooth();
            if (result.getCode() != 200) {
                return result;
            }

            byte[] bs = data.getBytes("gb18030");
            outputStream.write(bs);
            outputStream.flush();

            // 执行完打印关闭蓝牙连接
            closeConnect();
        } catch (IOException e) {
            result.setMsg("蓝牙未连接成功");
            result.setData("蓝牙未连接成功:" + e.getMessage());
            Log.e(TAG, "蓝牙打印异常: " + e.getMessage());
        }
        return result;
    }

    /**
     * 关闭蓝牙连接
     */
    private void closeConnect() {
        try {
            if (bluetoothSocket != null && this.bluetoothSocket.isConnected()) {
                bluetoothSocket.close();
                isConnection = false;
            }
            if (outputStream != null) {
                outputStream.close();
            }
        } catch (IOException e) {
            Log.e(TAG, "蓝牙关闭异常: " + e.getMessage());
        }
    }
}
