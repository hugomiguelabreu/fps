package Offline;

import Handlers.TorrentClientInitializer;
import Misc.TorrentUtil;
import Network.TorrentWrapperOuterClass;
import Offline.LocalTorrent.ListenerTorrents;
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
        ArrayList<String> ownAdrresses;

        System.out.println("username: ");
        username = sc.nextLine();

        ownAdrresses =  findLocalAddresses();

        Listener l = new Listener(username, ownAdrresses);
        l.start();
        new Broadcast(username, ownAdrresses).start();

        for (String addr : ownAdrresses){

            new ListenerTorrents(username, addr).start();
        }


        //Channel ch = startClient();

        while (!(input = sc.nextLine()).equals("quit")) {

            if(input.equals("upload")){ //

                FilenameFilter filter = new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        return name.endsWith(".torrent");
                    }
                };

                try {
                    com.turn.ttorrent.tracker.Tracker tck = new Tracker(new InetSocketAddress(ownAdrresses.get(0),6969));

                    File parent = new File("./");
                    for (File f : parent.listFiles(filter)) {
                        logger.info("Loading torrent from " + f.getName());
                        tck.announce(TrackedTorrent.load(f));
                    }

                    logger.info("Starting tracker with {} announced torrents...",
                            tck.getTrackedTorrents().size());
                    tck.start();
                    System.out.println(tck.getTrackedTorrents());
                } catch (Exception e) {
                    logger.error("{}", e.getMessage(), e);
                    System.exit(2);
                }

                System.out.println("What is the file?");
                ArrayList<String> trc = new ArrayList<String>();
                trc.add(ownAdrresses.get(0));
                Torrent t = TorrentUtil.createTorrent(sc.nextLine(), username, trc);

                TorrentWrapperOuterClass.TorrentWrapper tw = TorrentWrapperOuterClass.TorrentWrapper.newBuilder().setContent(ByteString.copyFrom(t.getEncoded())).build();

                // mandar este tw para a rede local

                //TODO teste

                ConcurrentHashMap<String, Peer> dudes = l.getPeers();

                for (Map.Entry<String, Peer> entry : dudes.entrySet()) {

                    if(! entry.getValue().getUsername().equals(username)){

                        System.out.println("a enviar para " + entry.getKey());

                        Socket s = new Socket(entry.getKey(), 5558);

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

    private static ArrayList<String> findLocalAddresses() {

        ArrayList<String> ret = new ArrayList<String>();

        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                // filters out 127.0.0.1 and inactive interfaces
                if (iface.isLoopback() || !iface.isUp())
                    continue;

                Enumeration<InetAddress> addresses = iface.getInetAddresses();

                while(addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if(Inet6Address.class == addr.getClass() && addr.isLinkLocalAddress()){
                        ret.add(addr.getHostAddress().replaceAll("%.*", "")); // e preciso tirar o %interface
                        System.out.println("local address: " + addr.getHostAddress());
                    }
                }
            }
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }

        return ret;

    }


}
