/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package stopwaitsend;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 *
 * @author tom
 */
public class StopWaitSend {

    /**
     * @param args the command line arguments
     * @throws java.io.FileNotFoundException
     */
    public static void main(String[] args) throws FileNotFoundException, IOException {

        String errorType = args[0];
        String logFile = args[1];
        String srcFile = args[2];

        PrintWriter pw = new PrintWriter(logFile, "UTF-8");
        FileInputStream fis = new FileInputStream(srcFile);
        byte[] fileBytes = new byte[fis.available()];

        int j = 0;
        while (fis.available() > 0) {
            fileBytes[j] = (byte) fis.read();
            j += 1;
        }

        ByteArrayInputStream read = new ByteArrayInputStream(fileBytes);

        int seq = 0;
        int ack = 0;

        byte[] bytes = new byte[15];
        DatagramPacket inPacket = new DatagramPacket(bytes, bytes.length);
        DatagramSocket inSocket = new DatagramSocket(7777);

//        The following int is only used with error type 2
        int frameCount = 1;
        
        while (read.available() > 0) {

//<editor-fold defaultstate="collapsed" desc="error code 0">
            if (errorType.equals("0")) {
                if (seq > 1) {
                    seq = 0;
                }
                Frame frame = buildFrameClean(read, seq, ack);
                sendFrame(frame);
                logFrame(pw, frame);
                Frame ackFrame = getAck(inSocket, inPacket, bytes);
                seq += 1;
            }
//</editor-fold>

// TODO:  Frames after the first frame are being sent twice
            if (errorType.equals("1")) {
                if (seq > 1) {
                    seq = 0;
                }
                Frame frame = buildFrameClean(read, seq, ack);
//                Emulates drop of frame 2
                if(frameCount != 2){
                    sendFrame(frame);
                }
                logFrame(pw, frame);
                Frame ackFrame = getAck(inSocket, inPacket, bytes);
                if(ackFrame.getAck() != frame.getSeq()){
                    sendFrame(frame);
                    logFrame(pw, frame);
                }
                seq += 1;
                frameCount++;

            }

        }
        sendStopPacket();

        pw.close();

    }

    private static void logFrame(PrintWriter pw, Frame frame) throws IOException {

        byte snum = frame.getSeq();
        String seqNum = "";
        String data = new String(frame.getData()).trim().replaceAll("\n", "`");
        String log;
        String crc = frame.getCrcString();
        
        
        
        if (snum == 0) {
            seqNum = "0";
        } else {
            seqNum = "1";
        }
        
        
        
        if(data.length() < 6){
            log = seqNum + '\t' + "\"" + data + "\"" + '\t' + '\t' + crc;
        }else{
            log = seqNum + '\t' + "\"" + data + "\"" + '\t' + crc;
        }

        pw.println(log);

    }

    private static Frame buildFrameClean(ByteArrayInputStream read, int seq, int ack) {
        byte[] data = new byte[8];
        read.read(data, 0, 8);

        byte[] crc = CRC16.go(data);

        byte seqByte = (byte) seq;
        byte ackByte = (byte) ack;

        Frame frame = new Frame(seqByte, ackByte, data, crc);
        return frame;
    }

    private static void sendFrame(Frame frame) throws UnknownHostException, SocketException, IOException {

        byte[] bytes = new byte[15];

        int count = 0;
        for (byte b : frame.getBytes()) {
            bytes[count] = b;
            count++;
        }

        DatagramPacket pack = new DatagramPacket(bytes, bytes.length, InetAddress.getByName("localhost"), 8888);
        DatagramSocket sock = new DatagramSocket();

        sock.send(pack);
    }

    private static Frame getAck(DatagramSocket sock, DatagramPacket pack, byte[] bytes) throws SocketException, IOException {

        sock.receive(pack);

        Frame frame = new Frame();

        byte[] rawBytes = pack.getData();
        frame.setSeq(rawBytes[0]);
        frame.setAck(rawBytes[1]);

        byte[] dataTemp = new byte[8];
        for (int i = 2; i < 10; i++) {
            dataTemp[i - 2] = rawBytes[i];
        }
        frame.setData(dataTemp);

        byte[] crcTemp = new byte[2];
        for (int j = 10; j < 12; j++) {
            crcTemp[j - 10] = rawBytes[j];
        }
        frame.setCrc(crcTemp);

        return frame;

    }

    private static void sendStopPacket() throws SocketException, IOException {
        byte[] stop = new byte[8];

        for (int i = 0; i < 8; i++) {
            stop[i] = 127;
        }

        Frame frame = new Frame((byte) 0x00, (byte) 0x00, stop);
        frame.setData(stop);

        sendFrame(frame);
    }

}
