import Core.MainServer;
import Util.FileUtils;
import com.turn.ttorrent.client.Client;
import com.turn.ttorrent.tracker.TrackedTorrent;
import com.turn.ttorrent.tracker.Tracker;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

public class ServerExec {

    public static void main(String[] args) throws IOException, InterruptedException, NoSuchAlgorithmException {
        ConcurrentHashMap<String, Client> clients = new ConcurrentHashMap<>();
        Tracker tck = new Tracker(new InetSocketAddress(6969));
        MainServer ms = new MainServer(5000, tck, clients);
        //Starts tracker
        tck.start();
        System.out.println("Tracker initiated");
        //Starts server;
        ms.start();
        System.out.println("Server initiated");

        try {
            if(FileUtils.loadTorrents(tck, clients))
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
        }
        ms.shutdown();
    }

}
