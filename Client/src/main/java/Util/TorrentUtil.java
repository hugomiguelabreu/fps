package Util;

import Core.Connector;
import Network.ClientWrapper;
import Offline.Offline;
import Offline.Utils.LocalAddresses;
import Offline.Utils.User;
import com.google.protobuf.ByteString;
import com.turn.ttorrent.client.Client;
import com.turn.ttorrent.client.SharedTorrent;
import com.turn.ttorrent.common.Torrent;
import com.turn.ttorrent.tracker.TrackedPeer;
import com.turn.ttorrent.tracker.TrackedTorrent;
import com.turn.ttorrent.tracker.Tracker;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.Pane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TorrentUtil {

    private static HashMap<String,ArrayList<String>> torrentPeers = new HashMap<>(); // peers que estao a fazer download de um torrent
    private static final Logger logger = LoggerFactory.getLogger(Torrent.class);

    /**
     * Creates a torrent file to upload
     * @param trackers Name to save the torrent
     */
    public static Torrent createTorrent(String source, String username, ArrayList<String> trackers) throws IOException, NoSuchAlgorithmException {
        return createTorrent(source, username, Torrent.DEFAULT_PIECE_LENGTH, trackers);
    }

    /**
     * Creates a torrent file with specific pieces
     * @param pieces Name to save the torrent
     */
    public static Torrent createTorrent(String source, String username, int pieces, ArrayList<String> trackers) throws IOException, NoSuchAlgorithmException {
        //pieces = Torrent.DEFAULT_PIECE_LENGTH;
        //pieces = pieces * 1024;
        logger.info("Using piece length of {} bytes.", pieces);
        Torrent torrent = null;

        try {
            List<URI> announceURIs = new ArrayList<URI>();
            for (String url : trackers) {
                announceURIs.add(new URI(url));
            }

            List<List<URI>> announceList = new ArrayList<List<URI>>();
            announceList.add(announceURIs);

            File sourceFile = new File(source);
            if (!sourceFile.exists() || !sourceFile.canRead()) {
                throw new IllegalArgumentException(
                        "Cannot access source file or directory " +
                                sourceFile.getName());
            }

            if (sourceFile.isDirectory()) {
                logger.error("Can't transfer a directory.");
                return null;
            } else {
                torrent = Torrent.create(sourceFile, pieces, announceList, username);
            }

        } catch (Exception e) {
            logger.error("{}", e.getMessage(), e);
            System.exit(2);
        }

        return torrent;
    }

    public static Torrent loadTorrent(String filename) throws IOException, NoSuchAlgorithmException
    {
        return Torrent.load(new File(filename), true);
    }

    /**
     * Inicia o processo de upload de um ficheiro Offline
     * Inicia o traker nele proprio
     * @param t Torrent a fazer upload
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */

    public static Client upload(Torrent t, String path, Tracker tck, String username, String userToSend) throws IOException, NoSuchAlgorithmException, InterruptedException, SAXException, ParserConfigurationException {
        ArrayList<LocalAddresses> ownAddresses = Offline.findLocalAddresses();
        File dest = new File(path);

        SharedTorrent st = new SharedTorrent(t, dest.getParentFile());


        Client c = new Client(
                InetAddress.getByName(ownAddresses.get(0).getIpv4()),
                st, username);

        c.share(-1);

        TrackedTorrent tr = new TrackedTorrent(t);
        tck.announce(tr);

        tr.addObserver((o,arg) -> {

            try{

                TrackedPeer p = (TrackedPeer) arg;

                if(p.getLeft() == 0 && p.getState() != TrackedPeer.PeerState.UNKNOWN && p.getState() != TrackedPeer.PeerState.STOPPED){



                    byte[] b = p.getPeerId().array();
                    String tmp = new String(b);
                    String id = tmp.replaceAll("-TO0042-", "");

//                    p.getPeerId().get(b, 0,p.getPeerId().capacity());
//                    String tmp = new String(b);
//                    String id = tmp.replaceAll("-TO0042-", "");

                    //System.out.println("id: " + id);
                    //System.out.println("size = " + torrentPeers.get(tr.getHexInfoHash()).size() );

                    torrentPeers.get(tr.getHexInfoHash()).remove(id);
                    System.out.println("peer " + id +" completed");
                    //System.out.println("ARRAY = "  + torrentPeers.get(tr.getHexInfoHash()));

                    if(torrentPeers.get(tr.getHexInfoHash()).size() == 0){

                        //System.out.println("Tirar torrent do tracker");
                        tck.remove(t);
                        System.out.println("Stop Client");
                        c.stop();

                        if(tck.getTrackedTorrents().isEmpty()){

                            tck.stop();
                            System.out.println("stop tracker");
                                                    }

                        //System.out.println("name = " + t.getName());
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }

        });

        //Creates a protobuf to send file info

        ClientWrapper.TorrentWrapper tw = ClientWrapper.TorrentWrapper.newBuilder()
                .setContent(ByteString.copyFrom(t.getEncoded()))
                .build();

        ClientWrapper.ClientMessage wrapper = ClientWrapper.ClientMessage.newBuilder()
                .setTorrentWrapper(tw).build();

        //Offline sends to all users
        ConcurrentHashMap<String, User> foundUsers = Offline.listener.getUsers();

        torrentPeers.put(t.getHexInfoHash(),new ArrayList<String>());

        if(userToSend == null){

            //Broadcast
            for (Map.Entry<String, User> entry : foundUsers.entrySet()) {

                if(!entry.getValue().getUsername().equals(t.getCreatedBy())){

                    torrentPeers.get(tr.getHexInfoHash()).add(entry.getValue().getUsername());

                    System.out.println("Sending to: " + entry.getKey());
                    Socket s = new Socket(entry.getValue().getIpv4(), 5558);
                    wrapper.writeDelimitedTo(s.getOutputStream());
                }
            }
        }
        else {

            //Manda so para um men
            for (Map.Entry<String, User> entry : foundUsers.entrySet()) {

                if(entry.getValue().getUsername().equals(userToSend)){

                    torrentPeers.get(tr.getHexInfoHash()).add(entry.getValue().getUsername());

                    System.out.println("Sending to: " + entry.getKey());
                    Socket s = new Socket(entry.getValue().getIpv4(), 5558);
                    wrapper.writeDelimitedTo(s.getOutputStream());
                }
            }
        }
        return c;
    }

    public static Client upload(Torrent t, String path, Connector channel, String username, String group) throws IOException, NoSuchAlgorithmException, InterruptedException, SAXException, ParserConfigurationException {
        File dest = new File(path);
        String ip = TorrentUtil.getIp();

        //Creates a protobuf to send file inf

        ClientWrapper.TorrentWrapper sw = ClientWrapper.TorrentWrapper.newBuilder()
                .setContent(ByteString.copyFrom(t.getEncoded()))
                .setGroup(group)
                .build();

        ClientWrapper.ClientMessage wrapper = ClientWrapper.ClientMessage.newBuilder()
                .setTorrentWrapper(sw).build();

        //Escreve e espera pela escrita no socket
        if(!channel.send(wrapper))
            return null;

        //Após iniciada a intenção, iniciamos o cliente.
        SharedTorrent st = new SharedTorrent(t, dest.getParentFile());
        Client c = new Client(
                InetAddress.getByName(ip),
                st,
                username);

        c.share(-1);

        return c;
    }

    public static void download(SharedTorrent st, boolean online, String username)
    {
        String ip;
        try {
            if(online){
                ip = TorrentUtil.getIp();
            }else{
                ip = Offline.findLocalAddresses().get(0).getIpv4();
            }

            Client c = new Client(
                    InetAddress.getByName(ip),
                    st);
            c.setMaxDownloadRate(0.0);
            c.setMaxUploadRate(0.0);

            //Download and seed
            c.addObserver((o, arg) -> {

                System.out.println(st.getCompletion());
                System.out.println(arg);
            });

            c.share(-1);

            if (com.turn.ttorrent.client.Client.ClientState.ERROR.equals(c.getState())) {
                System.exit(1);
            }
        } catch (IOException | InterruptedException | ParserConfigurationException | SAXException e) {
            e.printStackTrace();
        }
    }

    public static void download(SharedTorrent st, boolean online, String username, ProgressBar pb, ProgressIndicator pi)
    {
        String ip;
        try {
            if(online){
                ip = TorrentUtil.getIp();
            }else{
                ip = Offline.findLocalAddresses().get(0).getIpv4();
            }

            Client c = new Client(
                    InetAddress.getByName(ip),
                    st);
            c.setMaxDownloadRate(0.0);
            c.setMaxUploadRate(0.0);

            //Download and seed
            c.addObserver((o, arg) -> {
                // update UI thread
                Platform.runLater(() -> {
                    pb.setProgress(st.getCompletion()/100);
                    pi.setProgress(st.getCompletion()/100);
                });
                System.out.println(st.getCompletion());
                System.out.println(arg);
            });

            c.share(-1);

            if (com.turn.ttorrent.client.Client.ClientState.ERROR.equals(c.getState())) {
                System.exit(1);
            }
        } catch (IOException | InterruptedException | ParserConfigurationException | SAXException e) {
            e.printStackTrace();
        }
    }

    public static String getIp() throws IOException {
        URL whatismyip = new URL("http://checkip.amazonaws.com");
        BufferedReader in = new BufferedReader(new InputStreamReader(
                whatismyip.openStream()));
        return in.readLine(); //you get the IP as a String
    }
}
