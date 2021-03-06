package Util;

import Network.Interserver;
import com.google.protobuf.ByteString;
import com.turn.ttorrent.client.Client;
import com.turn.ttorrent.client.SharedTorrent;
import com.turn.ttorrent.client.peer.SharingPeer;
import com.turn.ttorrent.common.Peer;
import com.turn.ttorrent.common.Torrent;
import com.turn.ttorrent.tracker.TrackedPeer;
import com.turn.ttorrent.tracker.TrackedTorrent;
import com.turn.ttorrent.tracker.Tracker;
import org.xml.sax.SAXException;

import javax.sound.midi.Track;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.Socket;
import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    public static TrackedTorrent announceTrackedTorrentWithObservers(Tracker tck, Torrent t, Map<String, Client> clients, ConcurrentHashMap<String,
        ArrayList<TrackedPeer>> deletionsWaiting, boolean replication, String group) throws IOException, NoSuchAlgorithmException {
        TrackedTorrent tt = new TrackedTorrent(t);
        ArrayList<String> finishedPeers = new ArrayList<>();

        tt.addObserver((o, arg) -> {
            TrackedPeer tp = (TrackedPeer) arg;
            synchronized (clients) {

                if (!tp.getState().equals(TrackedPeer.PeerState.STOPPED) && !tp.getState().equals(TrackedPeer.PeerState.UNKNOWN)) {
                    if (tp.getLeft() == 0) {
                        boolean canDelete = false;
                        if(!finishedPeers.contains(tp.getHexPeerId()) &&
                                !tp.getHexPeerId().equals(clients.get(tt.getHexInfoHash()).getPeerSpec().getHexPeerId())){
                            finishedPeers.add(tp.getHexPeerId());
                            //Mais um que fez download
                            try {
                                canDelete = ZooKeeperUtil.incrementReceived(group, t.getHexInfoHash());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        //Verificar se posso desligar
                        boolean allDownloaded = true;
                        if (tp.getHexPeerId().equals(clients.get(tt.getHexInfoHash()).getPeerSpec().getHexPeerId())) {
                            try {
                                removeInjectionRequest(tp, tt, "");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        if ((clients.containsKey(tt.getHexInfoHash()))) {
                            Set<SharingPeer> peersConnected = clients.get(tt.getHexInfoHash()).getPeers();
                            for (SharingPeer sp : peersConnected) {
                                allDownloaded = allDownloaded && !(sp.isDownloading());
                            }
                        }
                        //Se toda a gente terminou e todos os trackers já pediram para terminar.
                        if ((clients.containsKey(tt.getHexInfoHash()) &&
                                allDownloaded &&
                                (deletionsWaiting.size() == tt.getInjectedPeers().size() - 1) &&
                                tt.getPeers().values().stream().allMatch(x -> x.getLeft() == 0))) {
                            System.out.println("\u001B[31mWe will remove local peer\u001B[0m");
                            if (replication) {
                                System.out.println("WILL REPLICATE");
                                clients.get(tt.getHexInfoHash()).stop(false);
                                for (TrackedPeer del : tt.getInjectedPeers())
                                    if (!del.getHexPeerId().equals(clients.get(tt.getHexInfoHash()).getPeerSpec().getHexPeerId()))
                                        tt.removeInjectedPeer(del.getHexPeerId());

                                clients.remove(tt.getHexInfoHash());
                                deletionsWaiting.remove(tt.getHexInfoHash());
                                try {
                                    if (canDelete) {
                                        System.out.println("TODA A GENTE FEZ O DOWNLOAD");
                                        //Peço a outros servidores para eliminar;
                                        removeInjectionRequest(tp, tt, group);
                                        tck.remove(t);
                                        new Thread(() -> {
                                            try {
                                                FileUtils.deleteTorrent(t, group);
                                                FileUtils.deleteFiles(t);
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }).start();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                try {
                                System.out.println("WONT REPLICATE");
                                clients.get(tt.getHexInfoHash()).stop(false);
                                clients.remove(tt.getHexInfoHash());
                                deletionsWaiting.remove(tt.getHexInfoHash());
                                tck.remove(t);
                                //Peço a outros servidores para eliminar;
                                if(canDelete)
                                    removeInjectionRequest(tp, tt, group);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                new Thread(() -> {
                                    try {
                                        FileUtils.deleteFiles(t);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }).start();
                            }
                        }
                    } else {
                        System.out.println("\u001B[31mNew guy, let's start a new local client if we don't have one\u001B[0m");
                        if (!clients.containsKey(t.getHexInfoHash())) {
                            System.out.println("\u001B[31mWe don't have a client\u001B[0m");
                            Client c = null;
                            //Thread safe to avoid creating Clients that will be
                            //collected as garbage;
                            try {
                                c = TorrentUtil.initClient(t, FileUtils.fileDir);
                                clients.put(t.getHexInfoHash(), c);
                            } catch (IOException | NoSuchAlgorithmException | InterruptedException | ParserConfigurationException | SAXException e) {
                                e.printStackTrace();
                            }
                        } else {
                            System.out.println("\u001B[31mIgnore because we have a client\u001B[0m");
                        }
                    }
                }
            }
        });
        //Anuncia o torrent com o observable
        tck.announce(tt);

        return tt;
    }

    public static void injectionRequest(Peer peer, ConcurrentHashMap<String, ArrayList<TrackedPeer>> injectionsWaiting, TrackedTorrent tt) throws IOException {
        System.out.println("PEDIDO DE INJEÇÃO");
        //Inject peer in each of the trackers
        int it = 0;
        for(URI tracker: tt.getAnnounceList().get(0)) {
            //Ignorar-me a mim próprio
            if(it != 0) {
                Socket s = new Socket(tracker.getHost(), 6000);
                Interserver.InterServerMessage im = Interserver.InterServerMessage.newBuilder()
                        .setTypeOp(true)
                        .setServerIp(ByteString.copyFromUtf8(peer.getIp()))
                        .setServerCliPort(peer.getPort())
                        .setTorrentHexId(ByteString.copyFromUtf8(tt.getHexInfoHash()))
                        .setPeerId(ByteString.copyFrom(peer.getPeerId()))
                        .setGroup("")
                        .build();
                im.writeDelimitedTo(s.getOutputStream());
                s.getOutputStream().flush();
                s.getOutputStream().close();
                s.close();
            }
            it++;
        }

        //Injetar os servers que já me tinham pedido.
        if(injectionsWaiting.containsKey(tt.getHexInfoHash())){
            for (TrackedPeer tp : injectionsWaiting.get(tt.getHexInfoHash())) {
                System.out.println("INJETEI " + tp.getHexPeerId() + " EM ESPERA");
                tt.injectPeer(new TrackedPeer(tt, tp.getIp(),  tp.getPort(), tp.getPeerId()));
            }
            injectionsWaiting.remove(tt.getHexInfoHash());
        }
    }


    public static void removeInjectionRequest(Peer peer, TrackedTorrent tt, String group) throws IOException {
        //Send remove request
        int it = 0;
        for(URI tracker: tt.getAnnounceList().get(0)) {
            //Ignorar-me a mim próprio
            if(it != 0) {
                Socket s = new Socket(tracker.getHost(), 6000);
                Interserver.InterServerMessage im = Interserver.InterServerMessage.newBuilder()
                        .setTypeOp(false)
                        .setServerIp(ByteString.copyFromUtf8(peer.getIp()))
                        .setServerCliPort(peer.getPort())
                        .setTorrentHexId(ByteString.copyFromUtf8(tt.getHexInfoHash()))
                        .setPeerId(ByteString.copyFromUtf8(new String(peer.getPeerId().array())))
                        .setGroup(group)
                        .build();
                im.writeDelimitedTo(s.getOutputStream());
                s.getOutputStream().flush();
                s.getOutputStream().close();
                s.close();
            }
            it++;
        }
    }


}
