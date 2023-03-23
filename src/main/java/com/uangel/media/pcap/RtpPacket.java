package com.uangel.media.pcap;


import lombok.Data;

import java.util.Arrays;

/**
 *
 * @author kangmoo Heo
 */
@Data
public class RtpPacket {

    // RTP SendTime
    private long tsMs;

    //size of the RTP header:
    public static final int HEADER_SIZE = 12;

    //Fields that compose the RTP header
    private int version;
    private int padding;
    private int extension;
    private int cc;
    private int marker;
    private int payloadtype;
    private int sequenceNumber;
    private int timeStamp;
    private int ssrc;

    //Bitstream of the RTP header
    private byte[] header;

    //size of the RTP payload
    private int payloadSize;
    //Bitstream of the RTP payload
    private byte[] payload;

    //--------------------------
    //Constructor of an RTPpacket object from header fields and payload bitstream
    //--------------------------
    public RtpPacket(int pType, int framenb, int time, byte[] data, int dataLength) {
        //fill by default header fields:
        version = 2;
        padding = 0;
        extension = 0;
        cc = 0;
        marker = 0;
        ssrc = 1337;    // Identifies the server

        //fill changing header fields:
        sequenceNumber = framenb;
        timeStamp = time;
        payloadtype = pType;

        //build the header bistream:
        header = new byte[HEADER_SIZE];

        //fill the header array of byte with RTP header fields
        header[0] = (byte) (version << 6 | padding << 5 | extension << 4 | cc);
        header[1] = (byte) (marker << 7 | payloadtype & 0x000000FF);
        header[2] = (byte) (sequenceNumber >> 8);
        header[3] = (byte) (sequenceNumber & 0xFF);
        header[4] = (byte) (timeStamp >> 24);
        header[5] = (byte) (timeStamp >> 16);
        header[6] = (byte) (timeStamp >> 8);
        header[7] = (byte) (timeStamp & 0xFF);
        header[8] = (byte) (ssrc >> 24);
        header[9] = (byte) (ssrc >> 16);
        header[10] = (byte) (ssrc >> 8);
        header[11] = (byte) (ssrc & 0xFF);

        //fill the payload bitstream:
        payloadSize = dataLength;
        payload = new byte[dataLength];

        //fill payload array of byte from data (given in parameter of the constructor)
        payload = Arrays.copyOf(data, payloadSize);
    }

    //--------------------------
    //Constructor of an RTPpacket object from the packet bistream
    //--------------------------
    public RtpPacket(byte[] packet, int packet_size, long tsMs) {
        this.tsMs = tsMs;
        //fill default fields:
        version = 2;
        padding = 0;
        extension = 0;
        cc = 0;
        marker = 0;
        ssrc = 0;

        //check if total packet size is lower than the header size
        if (packet_size >= HEADER_SIZE) {
            //get the header bitsream:
            header = new byte[HEADER_SIZE];
            for (int i = 0; i < HEADER_SIZE; i++)
                header[i] = packet[i];

            //get the payload bitstream:
            payloadSize = packet_size - HEADER_SIZE;
            payload = new byte[payloadSize];
            for (int i = HEADER_SIZE; i < packet_size; i++)
                payload[i - HEADER_SIZE] = packet[i];

            //interpret the changing fields of the header:
            version = (header[0] & 0xFF) >>> 6;
            payloadtype = header[1] & 0x7F;
            sequenceNumber = (header[3] & 0xFF) + ((header[2] & 0xFF) << 8);
            timeStamp = (header[7] & 0xFF) + ((header[6] & 0xFF) << 8) + ((header[5] & 0xFF) << 16) + ((header[4] & 0xFF) << 24);
        }
    }

    //--------------------------
    //getpayload: return the payload bistream of the RTPpacket and its size
    //--------------------------
    public int getPayload(byte[] data) {

        for (int i = 0; i < payloadSize; i++)
            data[i] = payload[i];

        return (payloadSize);
    }

    public byte[] getPayload() {
        byte[] res = new byte[payloadSize];
        System.arraycopy(payload, 0, res, 0, payloadSize);
        return res;
    }

    //--------------------------
    //getpayload_length: return the length of the payload
    //--------------------------
    public int getPayloadLength() {
        return (payloadSize);
    }

    //--------------------------
    //getlength: return the total length of the RTP packet
    //--------------------------
    public int getLength() {
        return (payloadSize + HEADER_SIZE);
    }

    //--------------------------
    //getpacket: returns the packet bitstream and its length
    //--------------------------
    public int getPacket(byte[] packet) {
        //construct the packet = header + payload
        System.arraycopy(header, 0, packet, 0, HEADER_SIZE);
        System.arraycopy(payload, 0, packet, HEADER_SIZE, payload.length);
        //return total size of the packet
        return (payloadSize + HEADER_SIZE);
    }

    public byte[] getpacket() {
        byte[] res = new byte[HEADER_SIZE + payloadSize];
        System.arraycopy(header, 0, res, 0, HEADER_SIZE);
        System.arraycopy(payload, 0, res, HEADER_SIZE, payloadSize);
        return res;
    }

    //--------------------------
    //gettimestamp
    //--------------------------

    public int getTimestamp() {
        return (timeStamp);
    }

    //--------------------------
    //getsequencenumber
    //--------------------------
    public int getSequencenumber() {
        return (sequenceNumber);
    }

    //--------------------------
    //getpayloadtype
    //--------------------------
    public int getPayloadtype() {
        return (payloadtype);
    }

    public String toString() {
        return "Version: " + version
                + ", Padding: " + padding
                + ", Extension: " + extension
                + ", CC: " + cc
                + ", Marker: " + marker
                + ", PayloadType: " + payloadtype
                + ", SequenceNumber: " + sequenceNumber
                + ", TimeStamp: " + timeStamp
                + ", SSRC: " + ssrc;
    }
}

