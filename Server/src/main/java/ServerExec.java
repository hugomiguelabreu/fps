import Core.MainServer;
import Util.FileUtils;
import com.turn.ttorrent.tracker.TrackedTorrent;
import com.turn.ttorrent.tracker.Tracker;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

public class ServerExec {

    public static void main(String[] args) throws IOException, InterruptedException, NoSuchAlgorithmException {
        //Starts tracker;
        Tracker tck = new Tracker(new InetSocketAddress(6969));
        MainServer ms = new MainServer(5000, tck);
        //Starts server;
        tck.start();
        ms.start();
        System.out.println("Tracker initiated");
        System.out.println("Server initiated");

        if(FileUtils.loadTorrents(tck))
            System.out.println("Could not load torrents persisted / No files");

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
