package Util;

import Network.Interserver;
import com.google.protobuf.ByteString;
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
import java.net.Socket;
import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
            TrackedPeer tp = (TrackedPeer) arg;
            System.out.println("\u001B[31m" + tp.getIp() + " changed\u001B[0m");
            if (!tp.getState().equals(TrackedPeer.PeerState.STOPPED) && !tp.getState().equals(TrackedPeer.PeerState.UNKNOWN)) {
                if (tp.getLeft() == 0) {
                    System.out.println("\u001B[31m" + tp.getIp() + " is over\u001B[0m");
                    if ((clients.containsKey(tt.getHexInfoHash()) &&
                            tt.getPeers().values().stream().allMatch(x -> x.getLeft() == 0)) || tt.getPeers().size() == 0) {
                        System.out.println("\u001B[31mWe will remove local peer\u001B[0m");
                        synchronized (clients) {
                            //TODO:TESTAR FALSE
                            tt.removelocalInjectPeerID(clients.get(tt.getHexInfoHash()).getPeerSpec().getHexPeerId());
                            tck.remove(t);
                            try {
                                removeInjectionRequest(clients.get(tt.getHexInfoHash()).getPeerSpec(), tt);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            clients.get(tt.getHexInfoHash()).stop(false);
                            clients.remove(tt.getHexInfoHash());
                        }
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
                        //Thread safe to avoid creating Clients that will be
                        //collected as garbage;
                        synchronized (clients) {
                            try {
                                c = TorrentUtil.initClient(t, FileUtils.fileDir);
                            } catch (IOException | NoSuchAlgorithmException | InterruptedException | ParserConfigurationException | SAXException e) {
                                e.printStackTrace();
                            }
                            clients.put(t.getHexInfoHash(), c);
                        }
                    }else{
                        System.out.println("\u001B[31mIgnore because we have a client\u001B[0m");
                    }
                }
            }
        });
        //Anuncia o torrent com o observable
        tck.announce(tt);

        return tt;
    }

    public static void injectionRequest(Peer peer, ConcurrentHashMap<String, ArrayList<TrackedPeer>> injectionsWaiting, TrackedTorrent tt) throws IOException {
        //Inject peer in each of the trackers
        for(URI tracker: tt.getAnnounceList().get(0)) {
            Socket s = new Socket(tracker.getHost(), 6000);
            Interserver.InterServerMessage im = Interserver.InterServerMessage.newBuilder()
                    .setTypeOp(true)
                    .setServerIp(ByteString.copyFromUtf8(peer.getIp()))
                    .setServerCliPort(peer.getPort())
                    .setTorrentHexId(ByteString.copyFromUtf8(tt.getHexInfoHash()))
                    .setPeerId(ByteString.copyFromUtf8(new String(peer.getPeerId().array())))
                    .build();
            im.writeDelimitedTo(s.getOutputStream());
            s.getOutputStream().flush();
            s.getOutputStream().close();
            s.close();
        }

        //Injetar os servers que já me tinham pedido.
        if(injectionsWaiting.containsKey(tt.getHexInfoHash()))
            for (TrackedPeer tp : injectionsWaiting.get(tt.getHexInfoHash()))
                tt.injectPeer(tp);
    }


    public static void removeInjectionRequest(Peer peer, TrackedTorrent tt) throws IOException {
        //Send remove request
        for(URI tracker: tt.getAnnounceList().get(0)) {
            Socket s = new Socket(tracker.getHost(), 6000);
            Interserver.InterServerMessage im = Interserver.InterServerMessage.newBuilder()
                    .setTypeOp(false)
                    .setServerIp(ByteString.copyFromUtf8(peer.getIp()))
                    .setServerCliPort(peer.getPort())
                    .setTorrentHexId(ByteString.copyFromUtf8(tt.getHexInfoHash()))
                    .setPeerId(ByteString.copyFromUtf8(new String(peer.getPeerId().array())))
                    .build();
            im.writeDelimitedTo(s.getOutputStream());
            s.getOutputStream().flush();
            s.getOutputStream().close();
            s.close();
        }
    }


}
