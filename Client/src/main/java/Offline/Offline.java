package Offline;

import Offline.LocalTorrent.TorrentListener;
import Offline.Utils.LocalAddresses;
import Offline.Probes.Broadcast;
import Offline.Probes.Listener;
import com.turn.ttorrent.common.Torrent;

import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Scanner;

public class Offline {

    public static Listener listener;
    public static Broadcast broadcast;

    /**
     * Inicia os Probes e o Torrentlistner
     * @param username nome do dude loged
     */
    public static void startProbes(String username, ArrayList<Torrent> availableParam){
        Scanner sc = new Scanner(System.in);
        // start Probes
        ArrayList<LocalAddresses> ownAddresses =  findLocalAddresses();

        //Starts listening for .torrents in the network
        listener = new Listener(username, ownAddresses);
        listener.start();
        //Start broadcasting address and info
        broadcast = new Broadcast(username, ownAddresses);
        broadcast.start();

        for (LocalAddresses addr : ownAddresses){
            // inica o listener de torrents na porta 5558
            new TorrentListener(addr.getIpv4(), 5558, availableParam).start();
        }
    }

    /**
     * Descobre todos os enderecos locais de todas as interfaces de rede ativas
     * @return ArrayList<LocalAddresses> ret
     */

    public static ArrayList<LocalAddresses> findLocalAddresses() {
        ArrayList<LocalAddresses> ret = new ArrayList<>();
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                // filters out 127.0.0.1 and inactive interfaces
                if (iface.isLoopback() || !iface.isUp())
                    continue;

                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                String ipv6 = null;
                String ipv4 = null;

                while(addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if(Inet6Address.class == addr.getClass() && addr.isLinkLocalAddress()){
                        ipv6 = addr.getHostAddress().replaceAll("%.*", ""); // e preciso tirar o %interface
                        System.out.println("local ipv6 address: " + ipv6);
                    }
                    if(Inet4Address.class == addr.getClass()){
                        ipv4 = addr.getHostName();
                        System.out.println("local ipv4 address: " + ipv4);
                    }
                }

//                ArrayList<InterfaceAddress> bcast = new ArrayList<>(iface.getInterfaceAddresses());
//
//                for(InterfaceAddress addr : bcast){
//
//                    if( addr.getBroadcast() != null){
//                        ipv4 = addr.getBroadcast().getHostName();
//                        System.out.println("local ipv4 address: " + ipv4);
//                    }
//                }

                ret.add(new LocalAddresses(ipv6, ipv4));
            }
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }

        return ret;
    }

}
