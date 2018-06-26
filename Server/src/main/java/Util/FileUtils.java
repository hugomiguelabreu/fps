package Util;

import com.turn.ttorrent.client.Client;
import com.turn.ttorrent.common.Torrent;
import com.turn.ttorrent.tracker.TrackedPeer;
import com.turn.ttorrent.tracker.TrackedTorrent;
import com.turn.ttorrent.tracker.Tracker;
import org.apache.commons.io.IOUtils;
import org.xml.sax.SAXException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FileUtils {

    public static String fileDir = System.getProperty( "user.home" ) + "/.fps-server/files/";

    public static void initDir(){
        new File(System.getProperty( "user.home" ) + "/.fps-server").mkdirs();
        new File(System.getProperty( "user.home" ) + "/.fps-server/files").mkdirs();
    }

    public static Torrent loadTorrent(String hex, String group) throws IOException, NoSuchAlgorithmException, InterruptedException, SAXException, ParserConfigurationException {
        File parent = new File(System.getProperty("user.home") + "/.fps-server/" + group + "/" + hex);
        if(parent.exists())
            return Torrent.load(parent);
        else
            return null;
    }

    public static boolean loadTorrents(Tracker tck, Map<String, Client> clients, ConcurrentHashMap<String, ArrayList<TrackedPeer>> deletionsWaiting) throws IOException, NoSuchAlgorithmException, InterruptedException, SAXException, ParserConfigurationException {
        File parent = new File(System.getProperty("user.home") + "/.fps-server");
        //Ao carregar os torrent não criamos clientes, pois o servidores está a ligar;
        //supostamente, irá iniciar clientes à medida que for necessário.
        String group;
        if(parent.exists()) {
            for (File f : parent.listFiles()) {
                if(f.isDirectory() && !f.getName().equals("files")) {
                    for(File l: f.listFiles()) {
                        if (!l.isDirectory()) {
                            System.out.println("READING:" + l.getName());
                            Torrent t = Torrent.load(l);
                            //Colocar torrent no tracker para o anunciar.
                            //Se guardei o torrent é porque tenho responsabilidade de replicar.
                            TrackedTorrent tt = TorrentUtil.announceTrackedTorrentWithObservers(tck, t, clients, deletionsWaiting, true, f.getName());
                        }
                    }
                }
            }
        }
        return true;
    }

    public static void saveTorrent(Torrent t, String group) throws IOException {
        FileOutputStream fos;
        new File(System.getProperty( "user.home" ) + "/.fps-server/" + group).mkdirs();
        fos = new FileOutputStream(System.getProperty( "user.home" ) + "/.fps-server/" + group + "/" + t.getHexInfoHash());
        t.save(fos);
        IOUtils.closeQuietly(fos);
    }

    public static void deleteFiles(Torrent t) throws IOException {
        File fileDownloaded = new File(System.getProperty( "user.home" ) + "/.fps-server/files/" + t.getFilenames().get(0));
        System.out.println("DELETING " + fileDownloaded.toPath().toString());
        Files.deleteIfExists(fileDownloaded.toPath());
    }

    public static void deleteTorrent(Torrent t, String group) throws IOException {
        File torrent = new File(System.getProperty( "user.home" ) + "/.fps-server/" + group + "/" + t.getHexInfoHash());
        Files.deleteIfExists(torrent.toPath());
    }

    public static String getMyIP() throws IOException {
        URL whatismyip = new URL("http://checkip.amazonaws.com");
        BufferedReader in = new BufferedReader(new InputStreamReader(
                whatismyip.openStream()));
        String ip = in.readLine(); //you get the IP as a String
        return ip;
    }

}
