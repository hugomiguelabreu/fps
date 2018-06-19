import Core.Connector;
import Event.ArrayListEvent;
import Util.FileUtils;
import Util.ServerOperations;
import Util.TorrentUtil;
import Network.ClientWrapper;
import Offline.Offline;
import com.turn.ttorrent.client.Client;
import com.turn.ttorrent.client.SharedTorrent;
import com.turn.ttorrent.common.Torrent;
import com.turn.ttorrent.tracker.Tracker;
import io.netty.channel.EventLoopGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Object.class);
    private static ArrayList<Client> activeClients;
    private static EventLoopGroup group = null;
    private static ArrayList<String> servers = new ArrayList<>();
    private static Connector channel;

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, InterruptedException, SAXException, ParserConfigurationException, URISyntaxException {

        Scanner sc = new Scanner(System.in);
        String input;

        String username, password, name;
        FileUtils.initDir();
        Torrent t;
        ArrayListEvent<Torrent> available = new ArrayListEvent<>();
        Tracker offlineTck = null;
        activeClients = new ArrayList<>();

        System.out.println("Started client");
        servers.add("localhost:2000");

        //TODO connect to frontEnd
        channel = new Connector(servers);
        if(channel.isConnected()){
            channel.start();
            ServerOperations.setChannel(channel);
        }

        while (true){
            System.out.println("[l]login");
            System.out.println("[r]register");

            input = sc.nextLine();
            if(input.equals("l")){
                System.out.println("username:");
                username = sc.nextLine();
                System.out.println("password:");
                password = sc.nextLine();
                if(ServerOperations.login(username, password)){
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
                if(ServerOperations.register(username, password, name)){
                    System.out.println("registed");
                    break;
                }
            }
            System.out.println("---------------------------------");
        }

        System.out.println("Online or Offline?");
        boolean type = sc.nextLine().equals("Online");

        if(!type) {
            Offline.startProbes(username, available);
        }else {
            if (!channel.isConnected()) {
                // System.exit(2);
                System.out.println("Falling back to Offline");
                Offline.startProbes(username, available);
                type = false;
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
                    trc.add("http://localhost:7070/announce");

                    t = TorrentUtil.createTorrent(path, username, trc);
                    //TODO: GROUP DINAMICO
                    String group = "leddit";
                    try {
                        activeClients.add(TorrentUtil.upload(t, path, channel, username, group));
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
                        TorrentUtil.upload(t, path, offlineTck, username, null);
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
    }

}
