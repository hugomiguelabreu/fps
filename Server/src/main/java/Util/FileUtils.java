package Util;

import com.turn.ttorrent.tracker.TrackedTorrent;
import com.turn.ttorrent.tracker.Tracker;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class FileUtils {

    public static boolean loadTorrents(Tracker tck){
        try {
            File parent = new File(System.getProperty("user.home") + "/persisted-files");
            if(parent.exists())
                for (File f : parent.listFiles()) {
                        tck.announce(TrackedTorrent.load(f));
                }
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

}
