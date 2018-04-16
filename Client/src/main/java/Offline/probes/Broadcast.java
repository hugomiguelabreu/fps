package Offline.probes;

//import protos.AddrOuterClass;

import Network.AddrBroadcastOuterClass;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class Broadcast extends Thread {

    private String username;
    private String ip = null;
    private ArrayList<String> ips;
    //private ArrayList<DatagramSocket> sockets;
    private HashMap<DatagramSocket, byte[]> sockets;
    private final int sendPort = 5556; // porta de onde saem os multicasts
    private final int receivePort = 5557; // porta de destino dos multicasts

    public Broadcast(String username, ArrayList<String> ips){

        this.username = username;
        this.ips = ips;
        sockets = new HashMap<>();

        try{

            for(String ip : ips){

                DatagramSocket ds = new DatagramSocket(new InetSocketAddress(ip, sendPort));
                ds.setBroadcast(true);

                AddrBroadcastOuterClass.AddrBroadcast data = AddrBroadcastOuterClass.AddrBroadcast.newBuilder()
                        .setAddr(ip)
                        .setUsername(username).build();

                ByteArrayOutputStream buff = new ByteArrayOutputStream(1024);
                        data.writeDelimitedTo(buff);

                byte[] send = buff.toByteArray();

                sockets.put(ds, send);


            }

        }catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


//        try {
//            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
//            while (interfaces.hasMoreElements()) {
//                NetworkInterface iface = interfaces.nextElement();
//                // filters out 127.0.0.1 and inactive interfaces
//                if (iface.isLoopback() || !iface.isUp())
//                    continue;
//
//                Enumeration<InetAddress> addresses = iface.getInetAddresses();
//
//                while(addresses.hasMoreElements()) {
//                    InetAddress addr = addresses.nextElement();
//                    if(Inet6Address.class == addr.getClass() && addr.isLinkLocalAddress()){
//                        ips.add(addr.getHostAddress().replaceAll("%.*", "")); // e preciso tirar o %interface
//                        ip = addr.getHostAddress().replaceAll("%.*", "");
//                        System.out.println(ip);  //fe80:0:0:0:2936:f914:6634:5e05%wlp3s0
//
//
//                        DatagramSocket ds = new DatagramSocket(new InetSocketAddress(ip, sendPort));
//                        ds.setBroadcast(true);
//
//                        AddrOuterClass.Addr.Builder addrProto = AddrOuterClass.Addr.newBuilder();
//                        addrProto.setAddr(ip);
//                        addrProto.setPortNumber(5555);
//                        AddrOuterClass.Addr data = addrProto.build();
//
//                        ByteArrayOutputStream buff = new ByteArrayOutputStream(1024);
//                        data.writeDelimitedTo(buff);
//
//                        byte[] send = buff.toByteArray();
//
//                        sockets.put(ds, send);
//
////                        DatagramSocket ds = new DatagramSocket(port, InetAddress.getByName(ip));
////                        ds.setBroadcast(true);
////                        ds.setReuseAddress(true);
////                        sockets.add(ds);
////                        MulticastSocket ms = new MulticastSocket(new InetSocketAddress(ip,port));
////                        sockets.add(ms);
////                        System.out.println(ms.getInterface());
//                        //a eduroam nao da ipv6 global so local
//                        //se o ip comecar por fe80: e link local, ou seja, e so dentro da rede
//                        //e preciso verificar se e global ou nao com isLinkLocalAddress()
//                        //so deve haver 1 por cada interface
//                        //se houverem varias interfaces depois vesse
//                    }
//                }
//            }
//        } catch (SocketException e) {
//            throw new RuntimeException(e);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

    }

    public ArrayList<String> getIps() {
        return ips;
    }

    @Override
    public void run(){

        System.out.println("run");

        try {

            while(true){

//                for(DatagramSocket ds : sockets){
//
//                    AddrOuterClass.Addr.Builder addr = AddrOuterClass.Addr.newBuilder();
//                    addr.setAddr(ds.getLocalAddress().toString());
//                    addr.setPortNumber(5555);
//                    AddrOuterClass.Addr data = addr.build();
//
//                    ByteArrayOutputStream buff = new ByteArrayOutputStream(1024);
//                    data.writeDelimitedTo(buff);
//
//                    byte[] send = buff.toByteArray();
//                    // ipv6 link local
//                    DatagramPacket packet = new DatagramPacket(send, send.length, InetAddress.getByName("ff02::1"), port);
//
//                }

                for (Map.Entry<DatagramSocket, byte[]> entry : sockets.entrySet()) {

                    DatagramPacket packet = new DatagramPacket(entry.getValue(), entry.getValue().length, InetAddress.getByName("ff02::1"), receivePort);
                    entry.getKey().send(packet);

                }

//                for(String to : ips){
//                    // ipv6 link local
//                    DatagramPacket packet = new DatagramPacket(send, send.length, InetAddress.getByName("ff02::1"), sendPort);
//
//                    //System.out.println(send.length);
//
//                    socket.send(packet);
//                }

                Thread.sleep(2000);
            }

        } catch (SocketException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }



}
