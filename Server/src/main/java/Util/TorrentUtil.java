package Util;

import com.turn.ttorrent.client.Client;
import com.turn.ttorrent.client.SharedTorrent;
import com.turn.ttorrent.common.Peer;
import com.turn.ttorrent.common.Torrent;
import com.turn.ttorrent.tracker.TrackedPeer;
import com.turn.ttorrent.tracker.TrackedTorrent;
import com.turn.ttorrent.tracker.Tracker;
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

    public static TrackedTorrent announceTrackedTorrentWithObservers(Tracker tck, Torrent t, Map<String, Client> clients) throws IOException, NoSuchAlgorithmException {
        TrackedTorrent tt = new TrackedTorrent(t);

        tt.addObserver((o, arg) -> {
            try {
                TrackedPeer tp = (TrackedPeer) arg;
                System.out.println("\u001B[31m" + tp.getIp() + " changed\u001B[0m");
                if (!tp.getState().equals(TrackedPeer.PeerState.STOPPED) && !tp.getState().equals(TrackedPeer.PeerState.UNKNOWN)) {
                    if (tp.getLeft() == 0) {
                        System.out.println("\u001B[31m" + tp.getIp() + " is over\u001B[0m");

                        for (TrackedPeer p : tt.getPeers().values()) {
                            if (p.getLeft() == 0)
                                System.out.println("\u001B[31m" + p.getIp() + " is over\u001B[0m");
                        }

                        if (clients.containsKey(tt.getHexInfoHash()) &&
                                tt.getPeers().values().stream().allMatch(x -> x.getLeft() == 0)) {
                            System.out.println("\u001B[31mWe will remove local peer\u001B[0m");
                            clients.get(tt.getHexInfoHash()).stop();
                            clients.remove(tt.getHexInfoHash());

                            /*tck.remove(tt);
                            new Thread(() -> {
                                try {
                                    FileUtils.deleteFiles(tt);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            });*/

                        }

                    } else {
                        System.out.println("\u001B[31mNew guy, let's start a new local client if we don't have one\u001B[0m");
                        if (!clients.containsKey(t.getHexInfoHash())) {
                            System.out.println("\u001B[31mWe don't have a client\u001B[0m");
                            Client c = null;
                            try {
                                c = TorrentUtil.initClient(t, FileUtils.fileDir);
                            } catch (IOException | NoSuchAlgorithmException | InterruptedException | ParserConfigurationException | SAXException e) {
                                e.printStackTrace();
                            }
                            clients.put(t.getHexInfoHash(), c);
                        }
                    }
                }
            }catch(Exception e){
                    e.printStackTrace();
            }
        });
        tck.announce(tt);
        return tt;
    }
}
