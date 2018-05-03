package Util;

import com.turn.ttorrent.client.Client;
import com.turn.ttorrent.client.SharedTorrent;
import com.turn.ttorrent.common.Torrent;
import com.turn.ttorrent.tracker.TrackedPeer;
import com.turn.ttorrent.tracker.TrackedTorrent;
import org.xml.sax.SAXException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.net.Inet4Address;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

public class TorrentUtil {

    public static Client initClient(Torrent t, String destParam) throws IOException, NoSuchAlgorithmException, InterruptedException, SAXException, ParserConfigurationException {
        File dest = new File(destParam);
        SharedTorrent st = new SharedTorrent(t, dest);
        Client c = new Client(
                Inet4Address.getByName("0.0.0.0"),
                st);

        c.setMaxDownloadRate(0.0);
        c.setMaxUploadRate(0.0);

        c.share(-1);

        if (com.turn.ttorrent.client.Client.ClientState.ERROR.equals(c.getState()))
            System.exit(1);

        return c;
    }

    public static TrackedTorrent getTrackedTorrentWithObservers(Torrent t, Map<String, Client> clients) throws IOException, NoSuchAlgorithmException {
        TrackedTorrent tt = new TrackedTorrent(t);

        tt.addObserver((o, arg) -> {
            TrackedPeer tp = (TrackedPeer) arg;
            if(tp.getLeft() == 0){
                if(tt.getPeers().values().stream().allMatch(x -> x.getLeft()==0)){
                    clients.get(tt.getHexInfoHash()).stop();
                    clients.remove(tt.getHexInfoHash());
                }
            }else{
                if(!clients.containsKey(t.getHexInfoHash())){
                    Client c = null;
                    try {
                        c = TorrentUtil.initClient(t, "/tmp/");
                    } catch (IOException | NoSuchAlgorithmException | InterruptedException | ParserConfigurationException | SAXException e) {
                        e.printStackTrace();
                    }
                    clients.put(t.getHexInfoHash(), c);
                }
            }
        });
        return tt;
    }
}
