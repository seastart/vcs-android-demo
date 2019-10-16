package com.freewind.meetingdemo.PB;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class UdpSocket {

    private DatagramSocket socket;

    public void open() throws SocketException {
        socket=new DatagramSocket();

    }

    public void close(){
        if(socket!=null){
            socket.close();
            socket=null;
        }
    }

    public void send(Packet packet,InetAddress address,int port) throws IOException {
        byte[] buf=packet.toBytesArray();
        final DatagramPacket p=new DatagramPacket(buf,buf.length,address,port);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    socket.send(p);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void send(DatagramPacket dpacket) throws IOException {
        socket.send(dpacket);
    }

    public DatagramPacket receive() throws IOException {
        byte[] buf=new byte[10240];
        DatagramPacket p=new DatagramPacket(buf,buf.length);
        if (socket != null && !socket.isClosed())
            socket.receive(p);
        return p;
    }
}
