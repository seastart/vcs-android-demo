package com.freewind.meetingdemo.PB;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * 实时交互协议报文头
 */
public class Packet {
    private static final int HEADER_SIZE=12;
    private static final byte MAGIC_CODE='$';

    private Models.PacketType packetType;
    private Models.Command command;
    private short major;
    private short minor;

    private Models.Result result;
    private byte[] data;


    public Packet(){

    }

    public Packet(Models.Command cmd, byte[] data){
        this.command=cmd;
        this.packetType= Models.PacketType.PT_Request;
        this.major=0;
        this.minor=0;
        this.result= Models.Result.RESULT_OK;
        this.data=data;
    }

    public Packet(Models.Command cmd, Models.PacketType packetType){
        this.command=cmd;
        this.packetType= packetType;
        this.major=0;
        this.minor=0;
        this.result= Models.Result.RESULT_OK;
        this.data=null;
    }

    public Packet(Models.Command cmd, byte[] data, Models.PacketType packetType){
        this.command=cmd;
        this.packetType= packetType;
        this.major=0;
        this.minor=0;
        this.result= Models.Result.RESULT_OK;
        this.data=data;
    }

    public Packet(Models.Command cmd, byte[] data, Models.PacketType type, short major, short minor, Models.Result result){
        this.command=cmd;
        this.packetType= packetType;
        this.major=major;
        this.minor=minor;
        this.result= result;
        this.data=data;
    }


    public Models.PacketType getPacketType() {
        return packetType;
    }

    public void setPacketType(Models.PacketType packetType) {
        this.packetType = packetType;
    }

    public Models.Command getCommand() {
        return command;
    }

    public void setCommand(Models.Command command) {
        this.command = command;
    }

    public short getMajor() {
        return major;
    }

    public void setMajor(short major) {
        this.major = major;
    }

    public short getMinor() {
        return minor;
    }

    public void setMinor(short minor) {
        this.minor = minor;
    }

    public Models.Result getResult() {
        return result;
    }

    public void setResult(Models.Result result) {
        this.result = result;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }


    public static Packet makeResponseFromRequest(Packet request, Models.Result result, byte[] data){
        return new Packet(request.getCommand(),  data, Models.PacketType.PT_Response, request.getMajor(),request.getMinor(),result);
    }

    public byte[] toBytesArray(){

        int dataLen = (data != null ? data.length : 0);
        ByteBuffer buf=ByteBuffer.allocate(HEADER_SIZE + dataLen);
        buf.order(ByteOrder.BIG_ENDIAN);

        buf.put((byte)MAGIC_CODE);
        buf.put((byte)this.packetType.getNumber());
        buf.putShort((short)command.getNumber());
        buf.putShort(major);
        buf.putShort(minor);
        buf.putShort((short)dataLen);
        buf.putShort((short)result.getNumber());
        if (data != null)
        {
            buf.put(data);
        }

        return buf.array();
    }

    public static Packet parse(byte[] buf,int offset,int length){
        ByteBuffer bb=ByteBuffer.wrap(buf,offset,length);
        bb.order(ByteOrder.LITTLE_ENDIAN);

        int len=length-offset;
        if(len<HEADER_SIZE){
            return null;
        }

        byte magic=bb.get();
        if(magic!=MAGIC_CODE){
            return null;
        }

        Packet packet=new Packet();
        packet.setPacketType(Models.PacketType.values()[bb.getShort()]);
//        packet.setCommand(Models.Command.values()[bb.getShort()]);
        packet.setMajor(bb.getShort());
        packet.setMinor(bb.getShort());
        int dataLen=bb.getShort();
//        packet.setResult(Models.Result.values()[bb.getShort()]);

        if(len-HEADER_SIZE<dataLen){
            return null;
        }

        if(dataLen>0){
            byte[] dataBuf=new byte[dataLen];
            bb.get(dataBuf);
            packet.setData(dataBuf);
        }

        return packet;
    }


}
