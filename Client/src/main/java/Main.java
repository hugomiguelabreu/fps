import Handlers.TorrentListenerHandler;
import Handlers.TorrentListenerInitializer;
import Misc.TorrentUtil;
import Network.TorrentWrapperOuterClass;
import Offline.Offline;
import com.google.protobuf.ByteString;
import com.turn.ttorrent.client.SharedTorrent;
import com.turn.ttorrent.common.Torrent;
import com.turn.ttorrent.tracker.Tracker;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.*;
import java.nio.channels.UnsupportedAddressTypeException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.TimeoutException;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Object.class);

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, InterruptedException, SAXException, ParserConfigurationException {

        Scanner sc = new Scanner(System.in);
        String input;
        String username = "default";
        Torrent t = null;
        ArrayList<Torrent> available = new ArrayList<>();
        Channel ch = null;
        Tracker offlineTck = null;

        System.out.println("Started client");
        System.out.println("Tell me your username: ");
        username = sc.nextLine();
        System.out.println("Online or Offline?");
        String type = sc.nextLine();

        if(type.equals("Offline")) {
            Offline.startProbes(username, available);
        }else {
            ch = startClient(available);
            if (ch == null) {
                System.out.println("\u001B[31mError opening socket\u001B[0m");
                System.exit(2);
            }
        }

        while (!(input = sc.nextLine()).equals("quit")) {
            if (type.equals("Online")) {
                System.out.println("Working online");
                if (input.equals("upload")) {
                    System.out.println("What is the file?");
                    String path = sc.nextLine();
                    ArrayList<String> trc = new ArrayList<String>();
                    //TODO: This IP must be dynamic
                    trc.add("http://138.68.151.167:6969/announce");
                    t = TorrentUtil.createTorrent(path, username, trc);

                    try {
                        TorrentUtil.upload(t, path, ch);
                    } catch (IOException | InterruptedException | ParserConfigurationException | SAXException e) {
                        System.out.println("Couldn't bind, fallback to local");
                        e.printStackTrace();
                    }

                    System.out.println("Upload intention initiated");
                }
            } else {
                System.out.println("Working offline");
                if (input.equals("upload")) {
                    System.out.println("What is the file?");
                    String path = sc.nextLine();
                    ArrayList<String> trc = new ArrayList<String>();
                    trc.add("http://" + Offline.findLocalAddresses().get(0).getIpv4()  + ":6969/announce");
                    //Already has a tracker running?
                    if(offlineTck == null) {
                        String httpAddress = Offline.findLocalAddresses().get(0).getIpv4();
                        offlineTck = new Tracker(new InetSocketAddress(InetAddress.getByName(httpAddress), 6969));
                        offlineTck.start();
                    }

                    t = TorrentUtil.createTorrent(path, username, trc);
                    try {
                        TorrentUtil.upload(t, path, offlineTck);
                    } catch (IOException | InterruptedException | ParserConfigurationException | SAXException e) {
                        System.out.println("Couldn't bind, ERROR.");
                        e.printStackTrace();
                    }
                    System.out.println("Upload intention initiated");
                }
            }

            if (input.equals("download"))
            {
                System.out.println("What's the file number?");
                File dest = new File("/tmp/");
                SharedTorrent st = new SharedTorrent(available.get(Integer.parseInt(sc.nextLine())), dest);
                System.out.println("Downloading to /tmp/");
                //TODO: Keep track of shared torrent to know when they end;
                TorrentUtil.download(getIPv4Address(null).getHostAddress(), st);
            }

            if (input.equals("info")) {
                int n = 0;
                for(Torrent tA : available) {
                    System.out.println("Torrent info " + n++ + ":");
                    System.out.println(tA.getName());
                    System.out.println(tA.getSize());
                    System.out.println(tA.getAnnounceList());
                    System.out.println(tA.getCreatedBy());
                    System.out.println("-----------------------------------");
                }
            }
        }
    }

    private static Channel startClient(ArrayList<Torrent> available) throws SocketException, UnknownHostException {
        EventLoopGroup group = new NioEventLoopGroup();
        Channel ch = null;
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                .channel(NioSocketChannel.class)
                .handler(new TorrentListenerInitializer(available));
                // Make a new connection.
                ch = b.connect("138.68.151.167", 5000).sync().channel();
                // Get the handler instance to initiate the request.
                //TorrentClientHandler handler = ch.pipeline().get(TorrentClientHandler.class);
                // Request and get the response.
                //List<String> response = handler.getLocalTimes(CITIES);
                // Close the connection.
                //sch.close();
        } catch (Exception e) {
            e.printStackTrace();
            return ch;
        } finally {
            group.shutdownGracefully();
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
