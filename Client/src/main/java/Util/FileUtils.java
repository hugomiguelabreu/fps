package Util;

import com.turn.ttorrent.common.Torrent;
import com.turn.ttorrent.tracker.TrackedTorrent;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.security.Timestamp;
import java.util.*;

public class FileUtils {

    public static String saveFilesPath;

    public static void initDir(){
        new File(System.getProperty( "user.home" ) + "/.fps").mkdirs();
        saveFilesPath = "/tmp";
    }

    public static void initDir(String path){

        new File(System.getProperty( "user.home" ) + "/.fps").mkdirs();
        saveFilesPath = path;
    }

    public static void addTorrent(Torrent t, String group){
        FileOutputStream fos = null;
        try {
            new File(System.getProperty( "user.home" ) + "/.fps/" + group).mkdirs();
            fos = new FileOutputStream(System.getProperty( "user.home" ) + "/.fps/" + group + "/" + t.getHexInfoHash());
            t.save(fos);
            IOUtils.closeQuietly(fos);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void deleteTorrent(Torrent t, String group){
        Path path = Paths.get(System.getProperty( "user.home" ) + "/.fps/" + group + "/" + t.getHexInfoHash());
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<Torrent> load(String group) throws IOException, NoSuchAlgorithmException {
        ArrayList<Torrent> ret = new ArrayList<>();

        File parent = new File(System.getProperty("user.home") + "/.fps/" + group);
        if(parent.exists()) {
            File[] files = parent.listFiles();
            Arrays.sort(files, (f1, f2) -> Long.valueOf(f2.lastModified()).compareTo(f1.lastModified()));
            for (File f : files) {
                if (!f.isDirectory()) {
                    ret.add(Torrent.load(f));
                }
            }
        }

        return ret;
    }

    public static void deletePartFile(String file){
        Path path = Paths.get(saveFilesPath + file + ".part");
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
