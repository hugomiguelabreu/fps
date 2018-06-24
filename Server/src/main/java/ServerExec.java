import Core.InterserverListener;
import Core.MainServerListener;
import Util.FileUtils;
import Util.ZooKeeperUtil;
import com.turn.ttorrent.client.Client;
import com.turn.ttorrent.client.peer.SharingPeer;
import com.turn.ttorrent.tracker.TrackedPeer;
import com.turn.ttorrent.tracker.TrackedTorrent;
import com.turn.ttorrent.tracker.Tracker;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

public class ServerExec {

    public static void main(String[] args) throws IOException, InterruptedException, NoSuchAlgorithmException {
        ConcurrentHashMap<String, Client> clients = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, ArrayList<TrackedPeer>> injectionsWaiting = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, ArrayList<TrackedPeer>> deletionsWaiting = new ConcurrentHashMap<>();
        int mainPort = Integer.parseInt(args[0]);
        int tckPort = Integer.parseInt(args[1]);
        int interPort = Integer.parseInt(args[2]);
        String serverId = args[3];

        Tracker tck = new Tracker(new InetSocketAddress(tckPort));
        MainServerListener ms = new MainServerListener(mainPort, tck, clients, injectionsWaiting, deletionsWaiting);
        InterserverListener is = new InterserverListener(interPort, tck, clients, injectionsWaiting, deletionsWaiting);
        ZooKeeperUtil zk = new ZooKeeperUtil(FileUtils.getMyIP() + ":2182");

        FileUtils.initDir();
        //Starts tracker
        tck.start();
        System.out.println("Tracker initiated");
        //Starts server;
        ms.start();
        System.out.println("Server initiated");
        //Starts interserver protocol;
        is.start();
        System.out.println("Interservers initiated");
        //Registers tracker on ZooKeeper
        zk.registerTracker(serverId, FileUtils.getMyIP() + ":" + mainPort);
        System.out.println("Server registred");

        try {
            if(!FileUtils.loadTorrents(tck, clients, deletionsWaiting))
                System.out.println("Could not load torrents persisted / No files");
        } catch (SAXException | ParserConfigurationException e) {
            e.printStackTrace();
        }
        Scanner reader = new Scanner(System.in);
        String line;
        while (!(line = reader.nextLine()).equals("quit")){
            for (TrackedTorrent t:tck.getTrackedTorrents()) {
                System.out.println(t.getName());
                System.out.println(t.getCreatedBy());
                System.out.println("---------------------------");
            }
            for(Client c : clients.values()){
                System.out.println("CLIENT:");
                for (SharingPeer p : c.getPeers()){
                    System.out.println(p.getIp());
                }
            }
        }
        //Stop all clients;
        for (Client c: clients.values()){
            if(c!=null)
                c.stop();
        }
        //Stop all listeners and tracker;
        tck.stop();
        ms.shutdown();
        is.shutdown();
        System.exit(0);
    }

}
