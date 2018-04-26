import Handlers.TorrentListenerHandler;
import Handlers.TorrentListenerInitializer;
import Misc.TorrentUtil;
import Network.TorrentWrapperOuterClass;
import com.google.protobuf.ByteString;
import com.turn.ttorrent.common.Torrent;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.*;
import java.io.IOException;
import java.net.*;
import java.nio.channels.UnsupportedAddressTypeException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.TimeoutException;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Object.class);

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {

        Scanner sc = new Scanner(System.in);
        String input;
        String username = "default";
        Torrent t = null;

        Channel ch = startClient();
        if(ch == null){
            System.out.println("\u001B[31mError opening socket\u001B[0m");
            System.exit(2);
        }
        System.out.println("Started client");

        while (!(input = sc.nextLine()).equals("quit")){

            if(input.equals("upload")){
                System.out.println("What is the file?");
                ArrayList<String> trc = new ArrayList<String>();
                trc.add("http://192.168.43.243:6969/announce");
                //trc.add("http://localhost:8989/annouce");
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

            if(input.equals("info")){
                System.out.println(t.getName());
                System.out.println(t.getSize());
                System.out.println(t.getAnnounceList());
                System.out.println(t.getCreatedBy());
            }
        }

    }

    private static Channel startClient() throws SocketException, UnknownHostException {
        EventLoopGroup group = new NioEventLoopGroup();
        Channel ch = null;
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                .channel(NioSocketChannel.class)
                .handler(new TorrentListenerInitializer(getIPv4Address(null).getHostAddress()));
                // Make a new connection.
                ch = b.connect("192.168.43.243", 5000).sync().channel();
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
