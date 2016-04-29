/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package stopwaitsend;

/**
 *
 * @author tom
 */
public class Frame {

//    private final static byte preamble = 0x7e;
//    private final static byte kind = 0x00;
    
    private byte seq = 0x00;
    private byte ack = 0x00;
    
    private byte[] data = new byte[8];
    private byte[] crc = new byte[2];

    public Frame() {
    }
    
    public Frame(byte seq, byte ack, byte[] data) {
        this.seq = seq;
        this.ack = ack;
        this.data = data;
        this.crc = CRC16.go(data);
    }
    
    public Frame(byte seq, byte ack, byte[] data, byte[] crc) {
        this.seq = seq;
        this.ack = ack;
        this.data = data;
        this.crc = crc;
    }
           
    public byte getSeq() {
        return seq;
    }

    public void setSeq(byte seq) {
        this.seq = seq;
    }

    public byte getAck() {
        return ack;
    }

    public void setAck(byte ack) {
        this.ack = ack;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public byte[] getCrc() {
        return crc;
    }

    public void setCrc(byte[] crc) {
        this.crc = crc;
    }

    public byte[] getBytes() {
        
        byte[] frameBytes = new byte[12];
        frameBytes[0] = (byte) getSeq();
        frameBytes[1] = (byte) getAck();
        
        int i = 2;
        for(Byte b : getData()){
            frameBytes[i] = b;
            i++;
        }
        
        for(Byte b : getCrc()){
            frameBytes[i] = b;
            i++;
        }
        return frameBytes;
    }
    
    public String getCrcString(){
        
        int crcA = crc[0];
        int crcB = crc[1];
        
        return crcA + ", " + crcB;
    }
    
    @Override
    public String toString(){
         String dataText = new String(data);
         return dataText;
    }
    
    
    
}
