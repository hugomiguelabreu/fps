package Util;

import com.turn.ttorrent.client.Client;
import com.turn.ttorrent.common.Torrent;
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
import java.util.Map;

public class FileUtils {

    public static String fileDir = System.getProperty( "user.home" ) + "/.fps-server/files/";

    public static void initDir(){
        new File(System.getProperty( "user.home" ) + "/.fps-server").mkdirs();
        new File(System.getProperty( "user.home" ) + "/.fps-server/files").mkdirs();
    }

    public static boolean loadTorrents(Tracker tck, Map<String, Client> clients) throws IOException, NoSuchAlgorithmException, InterruptedException, SAXException, ParserConfigurationException {
        File parent = new File(System.getProperty("user.home") + "/.fps-server");
        //Ao carregar os torrent não criamos clientes, pois o servidores está a ligar;
        //supostamente, irá iniciar clientes à medida que for necessário.
        if(parent.exists()) {
            for (File f : parent.listFiles()) {
                if (!f.isDirectory()) {
                    Torrent t = Torrent.load(f);
                    //Colocar torrent no tracker para o anunciar.
                    //Se guardei o torrent é porque tenho responsabilidade de replicar.
                    TrackedTorrent tt = TorrentUtil.announceTrackedTorrentWithObservers(tck, t, clients, true);
                }
            }
        }
        return true;
    }

    public static void saveTorrent(Torrent t) throws IOException {
        FileOutputStream fos;
        fos = new FileOutputStream(System.getProperty( "user.home" ) + "/.fps-server/" + t.getHexInfoHash());
        t.save(fos);
        IOUtils.closeQuietly(fos);
    }

    public static void deleteFiles(Torrent t) throws IOException {
        File torrent = new File(System.getProperty( "user.home" ) + "/.fps-server/" + t.getHexInfoHash());
        File fileDownloaded = new File(System.getProperty( "user.home" ) + "/.fps-server/files/" + t.getFilenames().get(0));
        Files.deleteIfExists(torrent.toPath());
        Files.deleteIfExists(fileDownloaded.toPath());
    }

    public static void deleteTorrent(Torrent t) throws IOException {
        File torrent = new File(System.getProperty( "user.home" ) + "/.fps-server/" + t.getHexInfoHash());
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
