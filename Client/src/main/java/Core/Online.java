package Core;

import Core.Connector;
import Event.ArrayListEvent;
import Event.MapEvent;
import Util.FileUtils;
import Util.TorrentUtil;
import com.turn.ttorrent.client.Client;
import com.turn.ttorrent.common.Torrent;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class Online {

    private static String username;
    private static ArrayList<Client> activeClients = new ArrayList<>();
    private static Connector channel;

    public static void addTorrent(Torrent t, String group){
        FileUtils.addTorrent(t, group);
    }

    public static void updateUsers(ArrayList<String> users){

    }

    public static void upload(String path, String group) throws IOException, NoSuchAlgorithmException {
        ArrayList<String> trc = new ArrayList<String>();
        //TODO: This IP must be dynamic

        trc.add("http://localhost:6969/announce");
        trc.add("http://localhost:7070/announce");

        Torrent t = null;

        t = TorrentUtil.createTorrent(path, username, trc);
        FileUtils.addTorrent(t, group);

        try {
            activeClients.add(TorrentUtil.upload(t, path, channel, username, group));
        } catch (IOException | InterruptedException | ParserConfigurationException | SAXException e) {
            System.out.println("Couldn't bind, fallback to local");
            e.printStackTrace();
        }
    }

}
