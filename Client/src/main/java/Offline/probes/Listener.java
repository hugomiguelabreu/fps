package Offline.probes;

import Network.AddrBroadcastOuterClass;
import Offline.Utils.LocalAddresses;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Listener extends Thread {
    //private ConcurrentHashMap<String, Peer> kappa;
    private ConcurrentHashMap<String, Peer> peers;
    private final int sendPort = 5556; // porta de onde saem os multicasts
    private final int receivePort = 5557; // porta de destino dos multicasts

    public Listener(String username, ArrayList<LocalAddresses> ips){

        peers = new ConcurrentHashMap<>();

        for(LocalAddresses addr : ips){
            Peer p = new Peer(username, addr.getIpv6(), addr.getIpv4(), new Timestamp(System.currentTimeMillis())); // put null ??
            peers.put(addr.getIpv6(),p);
        }

        //Peer p = new Peer(ip,5555);
        //peers.put(ip, p);

    }

    public ConcurrentHashMap<String, Peer> getPeers() {
        return peers;
    }

    @Override
    public void run(){

        checkPeers();

        //DatagramSocket socket = null;
        String ipv6, ipv4;
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

                    AddrBroadcastOuterClass.AddrBroadcast addr = AddrBroadcastOuterClass.AddrBroadcast.parseDelimitedFrom(ret);

                    // System.out.println("recieved : " + addr.getAddr());

                    ipv6 = addr.getIpv6();
                    ipv4 = addr.getIpv4();


                    //AddrOuterClass.Addr addr = AddrOuterClass.Addr.parseDelimitedFrom(ret);


                    if(! peers.containsKey(ipv6)){

                        peers.put(ipv6,new Peer(addr.getUsername(), ipv6, ipv4, new Timestamp(System.currentTimeMillis())));
                        System.out.println( "\u001B[32m" + "new dude : " + "\u001B[0m" + addr.getUsername());
                        System.out.println( "\u001B[32m" + "ipv6 : " + "\u001B[0m" + ipv6);
                        System.out.println( "\u001B[32m" + "ipv4 : " + "\u001B[0m" + ipv4);
                    }

                    else{

                        Peer tmp = peers.get(ipv6);

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

    private void checkPeers() {

        // -------------- Para verificar se o timestamp ja antigo ou nao ------------------

        ScheduledExecutorService executor = Executors.newScheduledThreadPool ( 1 );

        Runnable r = new Runnable () {
            @Override
            public void run () {
                try {  // Always wrap your Runnable with a try-catch as any uncaught Exception causes the ScheduledExecutorService to silently terminate.
                    //System.out.println ( "Now: " + Instant.now () );  // Our task at hand in this example: Capturing the current moment in UTC.

                    for (Map.Entry<String, Peer> entry : peers.entrySet()) {

//                        System.out.println("current time: " + System.currentTimeMillis());
//                        System.out.println("timestamp: " + entry.getValue().getLast().getTime());


                        if( (System.currentTimeMillis() - entry.getValue().getLast().getTime() ) > 5000 ){

                            System.out.println("remover");
                            peers.remove(entry.getKey());
                        }

                    }


                } catch ( Exception e ) {
                    System.out.println ( "Oops, uncaught Exception surfaced at Runnable in ScheduledExecutorService." );
                }
            }
        };

        executor.scheduleAtFixedRate ( r , 4000 , 4000 , TimeUnit.MILLISECONDS ); // ( runnable , initialDelay , period , TimeUnit )

    }

//    public long getDateDiff(long timeUpdate, long timeNow, TimeUnit timeUnit)
//    {
//        long diffInMillies = Math.abs(timeNow- timeUpdate);
//        return timeUnit.convert(diffInMillies, TimeUnit.MILLISECONDS);
//    }

}
