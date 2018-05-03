package Misc;
import Network.TorrentWrapperOuterClass;
import Offline.Offline;
import Offline.Utils.LocalAddresses;
import Offline.Utils.User;
import com.google.protobuf.ByteString;
import com.turn.ttorrent.client.Client;
import com.turn.ttorrent.client.SharedTorrent;
import com.turn.ttorrent.client.peer.SharingPeer;
import com.turn.ttorrent.common.Peer;
import com.turn.ttorrent.common.Torrent;
//import javafx.util.converter.ByteStringConverter;
import com.turn.ttorrent.tracker.TrackedPeer;
import com.turn.ttorrent.tracker.TrackedTorrent;
import com.turn.ttorrent.tracker.Tracker;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
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
     * Inicia o processo de upload de um ficheiro
     * Inicia o traker nele proprio
     * @param t Torrent a fazer upload
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */

    public static void upload(Torrent t, String path, Tracker tck, String username) throws IOException, NoSuchAlgorithmException, InterruptedException, SAXException, ParserConfigurationException {
        ArrayList<LocalAddresses> ownAddresses = Offline.findLocalAddresses();
        File dest = new File(path);
        //Seeding starts.

        SharedTorrent st = new SharedTorrent(t, dest.getParentFile());


        Client c = new Client(
                InetAddress.getByName(ownAddresses.get(0).getIpv4()),
                st);

        c.share(-1);

        TrackedTorrent tr = new TrackedTorrent(t);
        tck.announce(tr);

        tr.addObserver((o,arg) -> {

            TrackedPeer p = (TrackedPeer) arg;
            //System.out.println("tr id:" + tr.getName());

            if(p.getLeft() == 0){

                byte[] b = new byte[p.getPeerId().limit()];
                p.getPeerId().get(b, 0,p.getPeerId().limit());
                String id = new String(b);

                //System.out.println("id: " + id);
                //System.out.println("size = " + torrentPeers.get(tr.getHexInfoHash()).size() );

                torrentPeers.get(tr.getHexInfoHash()).remove(id);
                System.out.println("peer " + id +" completed");
                //System.out.println("ARRAY = "  + torrentPeers.get(tr.getHexInfoHash()));

                if(torrentPeers.get(tr.getHexInfoHash()).size() == 0){

                    //System.out.println("Tirar torrent do tracker");
                    tck.remove(t);
                    c.stop();

                    if(tck.getTrackedTorrents().isEmpty()){
                        tck.stop();
                        System.out.println("stop tracker");
                    }

                    //System.out.println("name = " + t.getName());
                }
            }
        });

        //TODO: return client

        //Creates a protobuf to send file info
        TorrentWrapperOuterClass.TorrentWrapper tw = TorrentWrapperOuterClass.TorrentWrapper.newBuilder().setContent(ByteString.copyFrom(t.getEncoded())).build();
        //Offline sends to all users
        ConcurrentHashMap<String, User> foundUsers = Offline.listener.getUsers();

        torrentPeers.put(t.getHexInfoHash(),new ArrayList<String>());

        for (Map.Entry<String, User> entry : foundUsers.entrySet()) {
            if(!entry.getValue().getUsername().equals(t.getCreatedBy())){

                torrentPeers.get(tr.getHexInfoHash()).add(entry.getValue().getUsername());

                System.out.println("Sending to: " + entry.getKey());
                Socket s = new Socket(entry.getValue().getIpv4(), 5558);
                tw.writeDelimitedTo(s.getOutputStream());
            }
        }

    }

    public static Client upload(Torrent t, String path, Channel ch, String username) throws IOException, NoSuchAlgorithmException, InterruptedException, SAXException, ParserConfigurationException {

        File dest = new File(path);
        //Seeding starts.
        URL whatismyip = new URL("http://checkip.amazonaws.com");
        BufferedReader in = new BufferedReader(new InputStreamReader(
                whatismyip.openStream()));
        String ip = in.readLine(); //you get the IP as a String

        SharedTorrent st = new SharedTorrent(t, dest.getParentFile());
        //TODO: return client
        Client c = new Client(
                InetAddress.getByName(ip),
                st,
                username);
//        c.getPeerSpec().setPeerId(ByteBuffer.wrap(username.getBytes()));
        c.share(-1);

        //Creates a protobuf to send file info
        TorrentWrapperOuterClass.TorrentWrapper tw = TorrentWrapperOuterClass.TorrentWrapper.newBuilder().setContent(ByteString.copyFrom(t.getEncoded())).build();
        //Escreve e espera pela escrita no socket
        ch.writeAndFlush(tw).sync();

        return c;
    }

    public static void download(SharedTorrent st, boolean online, String username)
    {
        String ip;
        try {
            if(online){
                //Seeding starts.
                URL whatismyip = new URL("http://checkip.amazonaws.com");
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        whatismyip.openStream()));
                ip = in.readLine(); //you get the IP as a String
            }else{
                ip = Offline.findLocalAddresses().get(0).getIpv4();
            }

            Client c = new Client(
                    InetAddress.getByName(ip),
                    st);
            c.getPeerSpec().setPeerId(ByteBuffer.wrap(username.getBytes()));
            c.setMaxDownloadRate(0.0);
            c.setMaxUploadRate(0.0);
            //Download and seed
            c.addObserver((o, arg) -> {
                System.out.println(st.getCompletion());
                System.out.println(arg);

                for(SharingPeer s : c.getPeers()){

                    byte[] b = new byte[s.getPeerId().limit()];
                    s.getPeerId().get(b, 0,s.getPeerId().limit());
                    System.out.println(new String(b));
                }
            });

            c.share(-1);

            if (com.turn.ttorrent.client.Client.ClientState.ERROR.equals(c.getState())) {
                System.exit(1);
            }
        } catch (IOException | InterruptedException | ParserConfigurationException | SAXException e) {
            e.printStackTrace();
        }
    }
}
