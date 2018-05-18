package Util;

import com.turn.ttorrent.common.Torrent;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileUtils {

    public static void initDir(){

        new File(System.getProperty( "user.home" ) + "/.fps").mkdirs();
    }

    public static void addTorrent(Torrent t){

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(System.getProperty( "user.home" ) + "/.fps/" + t.getHexInfoHash());
            t.save(fos);
            IOUtils.closeQuietly(fos);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
