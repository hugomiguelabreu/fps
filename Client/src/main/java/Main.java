import Handlers.AutenticationInitializer;
import Handlers.TorrentListenerInitializer;
import Misc.FileUtils;
import Misc.TorrentUtil;
import Network.ClientWrapper;
import Offline.Offline;
import com.turn.ttorrent.client.Client;
import com.turn.ttorrent.client.SharedTorrent;
import com.turn.ttorrent.common.Torrent;
import com.turn.ttorrent.tracker.TrackedTorrent;
import com.turn.ttorrent.tracker.Tracker;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
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
import java.net.*;
import java.nio.channels.UnsupportedAddressTypeException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.SynchronousQueue;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Object.class);
    private static ArrayList<Client> activeClients;
    private static EventLoopGroup group = null;
    private static SynchronousQueue<Boolean> queue;

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, InterruptedException, SAXException, ParserConfigurationException {

        Scanner sc = new Scanner(System.in);
        String input;

        Channel frontCh = null;
        CompletableFuture<Boolean> result = new CompletableFuture<>();
        queue = new SynchronousQueue<Boolean>();
        String username = "default";
        String password, name;

        Torrent t = null;
        ArrayList<Torrent> available = new ArrayList<>();
        Channel ch = null;
        Tracker offlineTck = null;
        FileUtils.initDir();
        activeClients = new ArrayList<>();

        System.out.println("Started client");

        //frontCh = startClientFrontServer();

        while (true){

            System.out.println("[l]login");
            System.out.println("[r]register");

            input = sc.nextLine();

            if(input.equals("l")){
                System.out.println("username:");
                username = sc.nextLine();
                System.out.println("password:");
                password = sc.nextLine();
                if(login(username, password, frontCh, result)){
                    System.out.println("logged in");
                    break;
                }
            }

            if(input.equals("r")){
                System.out.println("username:");
                username = sc.nextLine();
                System.out.println("password:");
                password = sc.nextLine();
                System.out.println("name:");
                name = sc.nextLine();
                if(register(username, password, name, frontCh)){
                    System.out.println("registed");
                    break;
                }
            }

            System.out.println("---------------------------------");

        }


//        System.out.println("Tell me your username: ");
//        username = sc.nextLine();
        System.out.println("Online or Offline?");
        boolean type = sc.nextLine().equals("Online");

        if(!type) {
            Offline.startProbes(username, available);
        }else {
            ch = startClient(available);
            if (ch == null) {
                System.out.println("\u001B[31mError opening socket\u001B[0m");
                System.exit(2);
            }
        }

        while (!(input = sc.nextLine()).equals("quit")) {

            if (type) {
                System.out.println("Working online");
                if (input.equals("upload")) {
                    System.out.println("What is the file?");
                    String path = sc.nextLine();
                    ArrayList<String> trc = new ArrayList<String>();
                    //TODO: This IP must be dynamic

                    trc.add("http://localhost:6969/announce");
                    trc.add("http://192.168.43.288:6969/announce");

                    t = TorrentUtil.createTorrent(path, username, trc);

                    try {
                        activeClients.add(TorrentUtil.upload(t, path, ch, username));
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
                    if(offlineTck == null) { //TODO o tracker pode estar parado e nao ser null pq lambda exps
                        String httpAddress = Offline.findLocalAddresses().get(0).getIpv4();
                        offlineTck = new Tracker(new InetSocketAddress(InetAddress.getByName(httpAddress), 6969));
                        offlineTck.start();
                    }

                    t = TorrentUtil.createTorrent(path, username, trc);
                    try {
                        TorrentUtil.upload(t, path, offlineTck, username);
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
                TorrentUtil.download(st, type, username);
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

        //input == quit

        System.out.println("Shuting down...");
        for(Client c : activeClients){

            c.stop();
        }

        if (offlineTck != null) {
            offlineTck.stop();
        }

        if(group != null)
            group.shutdownGracefully();

        System.exit(0);

        //TODO:Terminar todos os clientes/tarckers abertos
    }

    private static Channel startClient(ArrayList<Torrent> available) throws SocketException, UnknownHostException {

        if(group == null)
            group = new NioEventLoopGroup();

        Channel ch = null;
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                .channel(NioSocketChannel.class)
                .handler(new TorrentListenerInitializer(available));
                // Make a new connection.
                ch = b.connect("localhost", 5000).sync().channel();

        } catch (Exception e) {
            e.printStackTrace();
            return ch;
        }

        return ch;
    }

    private static Channel startClientFrontServer(){

        if(group == null)
            group = new NioEventLoopGroup();

        Channel ch = null;
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new AutenticationInitializer(queue));
            // Make a new connection.


            ch = b.connect("localhost", 2000).sync().channel();

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
            //group.shutdownGracefully();
        }

        return ch;
    }

    private static boolean login(String username, String password, Channel frontCh, CompletableFuture<Boolean> result){

        return true;

//        boolean ret;
//

//                .setType(false) // register -> true , login -> false
//                .setUsername(username)
//                .setPassword(password)
//                .setName("").build();
//
//        frontCh.writeAndFlush(request);
//
//        try {
//            ret = result.get();
//            result = new CompletableFuture<>();
//            return ret;
//
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//            return false;
//        } catch (ExecutionException e) {
//            e.printStackTrace();
//            return false;
//        }

    }

    private static boolean register(String username, String password, String name, Channel frontCh) throws InterruptedException, IOException {

        Socket socket = new Socket("localhost", 2000);
        boolean ret;

        ClientWrapper.Register request = ClientWrapper.Register.newBuilder()
                .setUsername(username)
                .setPassword(password)
                .setName(name).build();

        ClientWrapper.ClientMessage wrapper = ClientWrapper.ClientMessage.newBuilder()
                .setRegister(request).build();

        socket.getOutputStream().write(wrapper.getSerializedSize());
        wrapper.writeTo(socket.getOutputStream());
        //System.out.println(ClientWrapper.ClientMessage.parseDelimitedFrom(socket.getInputStream()).getResponse().getRep());

        System.out.println(username + " : " + ClientWrapper.ClientMessage.parseDelimitedFrom(socket.getInputStream()).getResponse().getRep());

        return false;

//        ret = queue.take();
//            System.out.println("ret = " + ret);
//            return ret;

        //return true;

//        boolean ret;
//
//        ClientWrapper.Register request = ClientWrapper.Register.newBuilder()
//                .setUsername(username)
//                .setPassword(password)
//                .setName(name).build();
//
//        ClientWrapper.ClientMessage cw = ClientWrapper.ClientMessage.newBuilder()
//                .setRegister(request)
//                .build();
//
//        System.out.println("write channel");
//
//        frontCh.writeAndFlush(cw).sync();
//
//        System.out.println("write channel done");
//
//        try {
//            ret = queue.take();
//            System.out.println("ret = " + ret);
//            return ret;
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//            return false;
//        }
    }
}
