package Offline;

import Handlers.TorrentClientInitializer;
import Misc.TorrentUtil;
import Network.TorrentWrapperOuterClass;
import Offline.LocalTorrent.ListenerTorrents;
import Offline.Utils.LocalAddresses;
import Offline.probes.Broadcast;
import Offline.probes.Listener;
import Offline.probes.Peer;
import com.google.protobuf.ByteString;
import com.turn.ttorrent.client.SharedTorrent;
import com.turn.ttorrent.common.Torrent;
import com.turn.ttorrent.tracker.TrackedTorrent;
import com.turn.ttorrent.tracker.Tracker;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.*;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Client {

    private static final Logger logger = LoggerFactory.getLogger(Object.class);

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException { // main para testar

        Scanner sc = new Scanner(System.in);
        String username, input;
        ArrayList<LocalAddresses> ownAdrresses;

        System.out.println("username: ");
        username = sc.nextLine();

        ownAdrresses =  findLocalAddresses();

        Listener l = new Listener(username, ownAdrresses);
        l.start();

        new Broadcast(username, ownAdrresses).start();

        for (LocalAddresses addr : ownAdrresses){

            new ListenerTorrents(username, addr.getIpv4()).start();
        }


        //Channel ch = startClient();

        while (!(input = sc.nextLine()).equals("quit")) {

            if(input.equals("upload")){ //

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

                System.out.println("What is the file?");
                String fileStr = sc.nextLine();

                ArrayList<String> trc = new ArrayList<>();
                trc.add(trackerAddress);
                Torrent t = TorrentUtil.createTorrent(fileStr, username, trc);

                File file = new File(fileStr);
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

//            if(input.equals("download")){
//                System.out.println("What is the file?");
//                String file = sc.nextLine();
//                try {
//
//                    final SharedTorrent st = SharedTorrent.fromFile(
//                            new File(file),
//                            new File("/tmp/" + sc.nextLine()));
//
//
//                    com.turn.ttorrent.client.Client c = new com.turn.ttorrent.client.Client(
//                            getIPv4Address(null),
//                            st);
//
//                    c.setMaxDownloadRate(0.0);
//                    c.setMaxUploadRate(0.0);
//
//                    //Download and seed
//                    c.addObserver(new Observer() {
//                        @Override
//                        public void update(Observable o, Object arg) {
//                            System.out.println(st.getCompletion());
//                            System.out.println(arg);
//                        }
//                    });
//                    c.share(-1);
//
//                    if (com.turn.ttorrent.client.Client.ClientState.ERROR.equals(c.getState())) {
//                        System.exit(1);
//                    }
//                } catch (Exception e) {
//                    logger.error("Fatal error: {}", e.getMessage(), e);
//                    System.exit(2);
//                }
//            }

        }


    }

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

                        //System.out.println(addr.getHostAddress());

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
