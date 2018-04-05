package probes;

import Utils.Peer;
import protos.AddrOuterClass;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;

public class Listener extends Thread {

    private HashMap<String, Peer> peers;
    private final int sendPort = 5556; // porta de onde saem os multicasts
    private final int receivePort = 5557; // porta de destino dos multicasts

    public Listener(ArrayList<String> ips){

        peers = new HashMap<>();

        for(String addr : ips){
            //Peer p = new Peer(addr, 5555); // put null ??
            peers.put(addr,null);
        }

        //Peer p = new Peer(ip,5555);
        //peers.put(ip, p);

    }

    public HashMap<String, Peer> getPeers() {
        return peers;
    }

    @Override
    public void run(){

        //DatagramSocket socket = null;
        String ip;
        int port;

        try { // passar o try para dentro do while

            byte[] buff = new byte[1024];
            DatagramPacket dgram = new DatagramPacket(buff, buff.length);
            MulticastSocket socket = new MulticastSocket(receivePort); // must bind receive side
            socket.joinGroup(InetAddress.getByName("ff02::1"));

            while(true) {
                socket.receive(dgram);

                try{

                    ByteArrayInputStream ret = new ByteArrayInputStream(dgram.getData());

                    AddrOuterClass.Addr addr = AddrOuterClass.Addr.parseDelimitedFrom(ret);

                    System.out.println("recieved : " + addr.getAddr());
                    ip = addr.getAddr();
                    port = addr.getPortNumber();

                    if(! peers.containsKey(ip)){

                        peers.put(ip,new Peer(ip,port, new Timestamp(System.currentTimeMillis())));
                        System.out.println( "\u001B[32m" + "new dude : " + "\u001B[0m" + ip);
                    }

                    else{

                        Peer tmp = peers.get(ip);

                        if(tmp != null){

                            tmp.setLast(new Timestamp(System.currentTimeMillis()));
                        }
                    }


                }catch (IOException e) {
                    e.printStackTrace(); // podem vir cenas pela porta 5557 que nao sao protobuffs
                }

                dgram.setLength(buff.length); // must reset length field!
            }



            //socket = new DatagramSocket(5555);

//            byte[] buff = new byte[1024];// tamanho??
//            DatagramPacket data = new DatagramPacket(buff, buff.length);
//
//
//            while (true){
//
//                socket.receive(data);
//                //System.out.println("received stuff");
//
//                ByteArrayInputStream ret = new ByteArrayInputStream(data.getData());
//
//                AddrOuterClass.Addr addr = AddrOuterClass.Addr.parseDelimitedFrom(ret);
//
//                System.out.println("recieved : " + addr.getAddr());
//                ip = addr.getAddr();
//                port = addr.getPortNumber();
//
//                if(! peers.containsKey(ip)){
//
//                    peers.put(ip,new Peer(ip,port));
//                    System.out.println("new dude : " + ip);
//                }
//
//                //Thread.sleep(1000);
//            }

        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
