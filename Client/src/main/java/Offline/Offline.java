package Offline;

import Misc.TorrentUtil;
import Network.TorrentWrapperOuterClass;
import Offline.LocalTorrent.TorrentListener;
import Offline.Utils.LocalAddresses;
import Offline.Utils.Peer;
import Offline.probes.Broadcast;
import Offline.probes.Listener;
import com.google.protobuf.ByteString;
import com.turn.ttorrent.client.SharedTorrent;
import com.turn.ttorrent.common.Torrent;
import com.turn.ttorrent.tracker.TrackedTorrent;
import com.turn.ttorrent.tracker.Tracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

public class Offline {

    private static final Logger logger = LoggerFactory.getLogger(Object.class);
    private String username;
    private ArrayList<LocalAddresses> ownAdrresses;
    private Listener l;


    /**
     * inicia os probes e o Torrentlistner
     * @param username nome do dude loged
     */

    public void startProbes(String username){

        Scanner sc = new Scanner(System.in);
        this.username = username;

        // start probes

        ownAdrresses =  findLocalAddresses();

        l = new Listener(username, ownAdrresses);
        l.start();

        new Broadcast(username, ownAdrresses).start();

        for (LocalAddresses addr : ownAdrresses){

            // inica o listener de torrents na porta 5558
            // TODO passar a porta como argumento ????
            new TorrentListener(addr.getIpv4(), 5558).start();
        }


    }

    /**
     * inicia o processo de upload de um ficheiro
     * inicia o traker nele proprio
     * @param fileName nome do ficheiro para fazer upload
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */

    public void upload(String fileName) throws IOException, NoSuchAlgorithmException {

        System.out.println("gonna upload");

        com.turn.ttorrent.tracker.Tracker tck = null;

        //String announceAddress = "http://" + ownAdrresses.get(0).getIpv4();
        String trackerAddress = "http://" + ownAdrresses.get(0).getIpv4()  + ":6969/announce";

        try {

            //System.out.println(new InetSocketAddress(6963));

            //System.out.println(ownAdrresses.get(0).getIpv4());

//                    udpAddress = "upd://" +  ownAdrresses.get(0).getIpv4();
//
//                    System.out.println(udpAddress);

            String httpAddress = ownAdrresses.get(0).getIpv4();

            tck = new Tracker(new InetSocketAddress(InetAddress.getByName(httpAddress), 6969));
            tck.start();


            logger.info("Starting tracker with {} announced torrents...",
                    tck.getTrackedTorrents().size());
            //tck.start();
            //System.out.println(tck.getTrackedTorrents());
        } catch (Exception e) {
            logger.error("{}", e.getMessage(), e);
            System.exit(2);
        }

//        System.out.println("What is the file?");
//        String fileStr = sc.nextLine();

        ArrayList<String> trc = new ArrayList<>();
        trc.add(trackerAddress);
        Torrent t = TorrentUtil.createTorrent(fileName, username, trc);

        File file = new File(fileName);
        final SharedTorrent st = new SharedTorrent(t, new File(file.getParent()));

        com.turn.ttorrent.client.Client c = new com.turn.ttorrent.client.Client(
                InetAddress.getByName(ownAdrresses.get(0).getIpv4()),
                st);

        c.share(-1);

        tck.announce(new TrackedTorrent(t));

        TorrentWrapperOuterClass.TorrentWrapper tw = TorrentWrapperOuterClass.TorrentWrapper.newBuilder().setContent(ByteString.copyFrom(t.getEncoded())).build();

        // mandar este tw para a rede local

        //TODO teste

        ConcurrentHashMap<String, Peer> dudes = l.getPeers();

        for (Map.Entry<String, Peer> entry : dudes.entrySet()) {

            if(! entry.getValue().getUsername().equals(username)){

                System.out.println("a enviar para " + entry.getKey());

                Socket s = new Socket(entry.getValue().getIpv4(), 5558);
                tw.writeDelimitedTo(s.getOutputStream());

            }
        }

        System.out.println("Upload intention initiated");
    }


    /**
     * Descobre todos os enderecos locais de todas as interfaces de rede ativas
     * @return ArrayList<LocalAddresses> ret
     */

    private static ArrayList<LocalAddresses> findLocalAddresses() {

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
