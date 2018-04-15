import Handlers.TorrentClientHandler;
import Handlers.TorrentClientInitializer;
import Misc.TorrentUtil;
import Network.TorrentWrapperOuterClass;
import com.google.protobuf.ByteString;
import com.turn.ttorrent.client.Client;
import com.turn.ttorrent.client.SharedTorrent;
import com.turn.ttorrent.client.peer.SharingPeer;
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
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.*;
import java.nio.channels.UnsupportedAddressTypeException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Object.class);

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {

        Scanner sc = new Scanner(System.in);
        String input;
        String username = "default";
        Torrent t = null;

        Channel ch = startClient();
        System.out.println("Started client");

        while (!(input = sc.nextLine()).equals("quit")){
            if(input.equals("login")){
                username = sc.nextLine();
            }
            if(input.equals("upload")){
                System.out.println("What is the file?");
                ArrayList<String> trc = new ArrayList<String>();
                trc.add("http://localhost:6969/annouce");
                trc.add("http://localhost:8989/annouce");
                t = TorrentUtil.createTorrent(sc.nextLine(), username, trc);

                TorrentWrapperOuterClass.TorrentWrapper tw = TorrentWrapperOuterClass.TorrentWrapper.newBuilder().setContent(ByteString.copyFrom(t.getEncoded())).build();
                try {
                    //Escreve e espera pela escrita no socket
                    ch.writeAndFlush(tw).sync();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("Upload intention initiated");
            }

            if(input.equals("download")){
                System.out.println("What is the file?");
                String file = sc.nextLine();
                try {
                    final SharedTorrent st = SharedTorrent.fromFile(
                            new File(file),
                            new File("/tmp/" + sc.nextLine()));

                    Client c = new Client(
                            getIPv4Address(null),
                            st);

                    c.setMaxDownloadRate(0.0);
                    c.setMaxUploadRate(0.0);

                    //Download and seed
                    c.addObserver(new Observer() {
                        @Override
                        public void update(Observable o, Object arg) {
                            System.out.println(st.getCompletion());
                            System.out.println(arg);
                        }
                    });
                    c.share(-1);

                    if (Client.ClientState.ERROR.equals(c.getState())) {
                        System.exit(1);
                    }
                } catch (Exception e) {
                    logger.error("Fatal error: {}", e.getMessage(), e);
                    System.exit(2);
                }
            }

            if(input.equals("track")) {
                FilenameFilter filter = new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        return name.endsWith(".torrent");
                    }
                };

                try {
                    Tracker tck = new Tracker(new InetSocketAddress(6969));

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
            }

            if(input.equals("info")){
                System.out.println(t.getName());
                System.out.println(t.getSize());
                System.out.println(t.getAnnounceList());
                System.out.println(t.getCreatedBy());
            }
        }

    }

    private static Channel startClient(){
        EventLoopGroup group = new NioEventLoopGroup();
        Channel ch = null;
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                .channel(NioSocketChannel.class)
                .handler(new TorrentClientInitializer());
                // Make a new connection.
                ch = b.connect("localhost", 5000).sync().channel();
                // Get the handler instance to initiate the request.
                //TorrentClientHandler handler = ch.pipeline().get(TorrentClientHandler.class);
                // Request and get the response.
                //List<String> response = handler.getLocalTimes(CITIES);
                // Close the connection.
                //sch.close();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
                //group.shutdownGracefully();
        }

        return ch;
    }

    private static Inet4Address getIPv4Address(String iface)
            throws SocketException, UnsupportedAddressTypeException,
            UnknownHostException {
        if (iface != null) {
            Enumeration<InetAddress> addresses =
                    NetworkInterface.getByName(iface).getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress addr = addresses.nextElement();
                if (addr instanceof Inet4Address) {
                    return (Inet4Address)addr;
                }
            }
        }

        InetAddress localhost = InetAddress.getLocalHost();
        if (localhost instanceof Inet4Address) {
            return (Inet4Address)localhost;
        }

        throw new UnsupportedAddressTypeException();
    }
}
