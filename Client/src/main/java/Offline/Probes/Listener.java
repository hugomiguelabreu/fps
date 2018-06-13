package Offline.Probes;

import Event.ConcurrentHashMapEvent;
import Network.AddrBroadcastOuterClass;
import Offline.Utils.LocalAddresses;
import Offline.Utils.User;
import UI.OfflineUI;
import javafx.collections.ObservableMap;

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

    private ConcurrentHashMapEvent<String, User> peers;
    private final int sendPort = 5556; // porta de onde saem os multicasts
    private final int receivePort = 5557; // porta de destino dos multicasts

    public Listener(String username, ArrayList<LocalAddresses> ips, OfflineUI ui){

        peers = new ConcurrentHashMapEvent<>();
        peers.registerCallback(ui);

        for(LocalAddresses addr : ips){
            User p = new User(username, addr.getIpv6(), addr.getIpv4(), new Timestamp(System.currentTimeMillis()));
            peers.put(addr.getIpv6(),p);
        }

        //User p = new User(ip,5555);
        //peers.put(ip, p);
    }

    public ConcurrentHashMap<String, User> getUsers() {
        return peers;
    }

    @Override
    public void run(){
        checkUsers();
        String ipv6, ipv4;
        int port;

        try {
            byte[] buff = new byte[1024];
            DatagramPacket dgram = new DatagramPacket(buff, buff.length);
            MulticastSocket socket = new MulticastSocket(receivePort); // must bind receive side
            socket.joinGroup(InetAddress.getByName("ff02::1"));

            while(true) {
                socket.receive(dgram);
                try{
                    ByteArrayInputStream ret = new ByteArrayInputStream(dgram.getData());
                    AddrBroadcastOuterClass.AddrBroadcast addr = AddrBroadcastOuterClass.AddrBroadcast.parseDelimitedFrom(ret);
                    ipv6 = addr.getIpv6();
                    ipv4 = addr.getIpv4();

                    if(! peers.containsKey(ipv6)){
                        peers.put(ipv6,new User(addr.getUsername(), ipv6, ipv4, new Timestamp(System.currentTimeMillis())));
                        System.out.println( "\u001B[32m" + "new dude : " + "\u001B[0m" + addr.getUsername());
                        System.out.println( "\u001B[32m" + "ipv6 : " + "\u001B[0m" + ipv6);
                        System.out.println( "\u001B[32m" + "ipv4 : " + "\u001B[0m" + ipv4);
                    } else{
                        User tmp = peers.get(ipv6);
                        if(tmp != null){
                            tmp.setLast(new Timestamp(System.currentTimeMillis()));
                        }
                    }
                }catch (IOException e) {
                    e.printStackTrace(); // podem vir cenas pela porta 5557 que nao sao protobuffs
                }

                dgram.setLength(buff.length);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void checkUsers() {

        // -------------- Para verificar se o timestamp ja antigo ou nao ------------------
        ScheduledExecutorService executor = Executors.newScheduledThreadPool ( 1 );

        Runnable r = () -> {
            try {

                for (Map.Entry<String, User> entry : peers.entrySet()) {

                    if( (System.currentTimeMillis() - entry.getValue().getLast().getTime() ) > 5000 ){

                        System.out.println("remover: " + entry.getKey());
                        peers.remove(entry.getKey());
                    }
                }

            } catch ( Exception e ) {
                e.printStackTrace();
            }
        };

        executor.scheduleAtFixedRate ( r , 4000 , 3000 , TimeUnit.MILLISECONDS ); // ( runnable , initialDelay , period , TimeUnit )
    }
}
