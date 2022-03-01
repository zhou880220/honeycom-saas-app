package com.honeycom.saas.mobile.ws;

import java.io.DataOutputStream;
import java.net.Socket;

//import org.apache.commons.io.FileUtils;

public class PrinterS {


    public void run(String ip, String port, String zplStr) throws Exception {

//        try (Socket clientSocket = new Socket("172.16.6.230", 9100)) {
        try (Socket clientSocket = new Socket(ip, Integer.parseInt(port))) {
            // open data output stream
            DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());

            // send data to printer
//            String strTest = getStringFromFile("assets://label.zpl.txt");
//            outToServer.writeBytes(strTest);
//            outToServer.writeBytes(zplStr);
            byte[] bs = zplStr.getBytes("gb18030");
//            outToServer.write(zplStr);
            outToServer.write(bs);
            // close data stream and socket
            outToServer.close();
            clientSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
//    public void run(String zplStr) throws Exception {
//
//        // used variables
//        Socket clientSocket;
//        DataOutputStream outToServer;
//
//        // open connection
//        clientSocket = new Socket("172.16.6.230", 9100);
//
//        // open data output stream
//        outToServer = new DataOutputStream(clientSocket.getOutputStream());
//
//        // send data to printer
////        outToServer.writeBytes(getStringFromFile("/Users/neo/Code/hc-android/v2wsclient/app/src/main/assets/label.zpl.txt"));
//        outToServer.writeBytes(zplStr);
//
//        // close data stream and socket
//        outToServer.close();
//        clientSocket.close();
//
//    }
//    public String convertStreamToString(InputStream is) throws Exception {
//        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
//        StringBuilder sb = new StringBuilder();
//        String line = null;
//        while ((line = reader.readLine()) != null) {
//            sb.append(line).append("\n");
//        }
//        reader.close();
//        return sb.toString();
//    }

//    public String getStringFromFile(String filePath) throws Exception {
////        File file = new File("file:///android_asset/helloworld.txt");
//
//        File fl = new File(filePath);
//        FileInputStream fin = new FileInputStream(fl);
//        String ret = convertStreamToString(fin);
//        //Make sure you close all streams.
//        fin.close();
//        return ret;
//    }


//    public String getStrFromFile(String path){
//        String tContents = "";
//
//        try {
//            InputStream stream = getAssets().open(inFile);
//
//            int size = stream.available();
//            byte[] buffer = new byte[size];
//            stream.read(buffer);
//            stream.close();
//            tContents = new String(buffer);
//        } catch (IOException e) {
//            // Handle exceptions here
//        }
//
//        return tContents;
//    }
}
