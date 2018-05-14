package Util;

import com.turn.ttorrent.client.Client;
import com.turn.ttorrent.common.Torrent;
import com.turn.ttorrent.tracker.TrackedTorrent;
import com.turn.ttorrent.tracker.Tracker;
import org.apache.commons.io.IOUtils;
import org.xml.sax.SAXException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

public class FileUtils {

    public static String fileDir = System.getProperty( "user.home" ) + "/.fps/files/";

    public static void initDir(){
        new File(System.getProperty( "user.home" ) + "/.fps").mkdirs();
        new File(System.getProperty( "user.home" ) + "/.fps/files").mkdirs();
    }

    public static boolean loadTorrents(Tracker tck, Map<String, Client> clients) throws IOException, NoSuchAlgorithmException, InterruptedException, SAXException, ParserConfigurationException {
        File parent = new File(System.getProperty("user.home") + "/.fps");
        //Ao carregar os torrent não criamos clientes, pois o servidores está a ligar;
        //supostamente, irá iniciar clientes à medida que for necessário.
        if(parent.exists()) {
            for (File f : parent.listFiles()) {
                if (!f.isDirectory()) {
                    Torrent t = Torrent.load(f);
                    //Colocar torrent no tracker para o anunciar.
                    TrackedTorrent tt = TorrentUtil.announceTrackedTorrentWithObservers(tck, t, clients);
                }
            }
        }
        return true;
    }

    public static void saveTorrent(Torrent t) throws IOException {
        FileOutputStream fos;
        fos = new FileOutputStream(System.getProperty( "user.home" ) + "/.fps/" + t.getHexInfoHash());
        t.save(fos);
        IOUtils.closeQuietly(fos);
    }

    public static void deleteFiles(Torrent t) throws IOException {
        File torrent = new File(System.getProperty( "user.home" ) + "/.fps/" + t.getHexInfoHash());
        File fileDownloaded = new File(System.getProperty( "user.home" ) + "/.fps/files/" + t.getFilenames().get(0));
        Files.deleteIfExists(torrent.toPath());
        Files.deleteIfExists(fileDownloaded.toPath());
    }

}
