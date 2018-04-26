package Offline.Probes;

import Network.AddrBroadcastOuterClass;
import Offline.Utils.LocalAddresses;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Broadcast extends Thread {

    private String username;
    private String ip = null;
    private ArrayList<LocalAddresses> ips;
    //private ArrayList<DatagramSocket> sockets;
    private HashMap<DatagramSocket, byte[]> sockets;
    private final int sendPort = 5556; // porta de onde saem os multicasts
    private final int receivePort = 5557; // porta de destino dos multicasts

    public Broadcast(String username, ArrayList<LocalAddresses> ips){

        this.username = username;
        this.ips = ips;
        sockets = new HashMap<>();
        try{
            for(LocalAddresses ip : ips){
                DatagramSocket ds = new DatagramSocket(new InetSocketAddress(ip.getIpv6(), sendPort));
                ds.setBroadcast(true);
                AddrBroadcastOuterClass.AddrBroadcast data = AddrBroadcastOuterClass.AddrBroadcast.newBuilder()
                        .setIpv6(ip.getIpv6())
                        .setIpv4(ip.getIpv4())
                        .setUsername(username).build();
                ByteArrayOutputStream buff = new ByteArrayOutputStream(1024);
                        data.writeDelimitedTo(buff);
                byte[] send = buff.toByteArray();

                sockets.put(ds, send);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<LocalAddresses> getIps() {
        return ips;
    }

    @Override
    public void run(){
        System.out.println("Started broadcasting");
        try {
            while(true){
                for (Map.Entry<DatagramSocket, byte[]> entry : sockets.entrySet()) {
                    DatagramPacket packet = new DatagramPacket(entry.getValue(), entry.getValue().length, InetAddress.getByName("ff02::1"), receivePort);
                    entry.getKey().send(packet);
                }
                Thread.sleep(2000);
            }

        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }
}
