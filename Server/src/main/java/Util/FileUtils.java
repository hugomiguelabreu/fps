package Util;

import Core.ServerClient;
import com.turn.ttorrent.tracker.TrackedTorrent;
import com.turn.ttorrent.tracker.Tracker;
import org.simpleframework.transport.Server;

import javax.xml.soap.SAAJResult;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

public class FileUtils {

    public static boolean loadTorrents(Tracker tck, HashMap<String, ServerClient> clients){
        try {
            File parent = new File(System.getProperty("user.home") + "/persisted-files");
            if(parent.exists())
                for (File f : parent.listFiles()) {
                    TrackedTorrent tt = TrackedTorrent.load(f);
                    tck.announce(TrackedTorrent.load(f));
                    ServerClient sc = new ServerClient(tt);
                    sc.start();
                    clients.put(tt.getHexInfoHash(), sc);
                }
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

}
