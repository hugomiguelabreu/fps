package Util;

import com.turn.ttorrent.common.Torrent;
import com.turn.ttorrent.tracker.TrackedTorrent;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {

    public static void initDir(){

        new File(System.getProperty( "user.home" ) + "/.fps").mkdirs();
    }

    public static void addTorrent(Torrent t, String group){
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(System.getProperty( "user.home" ) + "/.fps/" + group + "/" + t.getHexInfoHash());
            t.save(fos);
            IOUtils.closeQuietly(fos);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<Torrent> load(String group) throws IOException, NoSuchAlgorithmException {
        ArrayList<Torrent> ret = new ArrayList<>();

        File parent = new File(System.getProperty("user.home") + "/.fps/" + group);
        if(parent.exists()) {
            for (File f : parent.listFiles()) {
                if (!f.isDirectory()) {
                    ret.add(Torrent.load(f));
                }
            }
        }
        return ret;
    }
}
