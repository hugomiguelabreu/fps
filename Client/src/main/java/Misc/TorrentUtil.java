package Misc;
import Network.TorrentWrapperOuterClass;
import Offline.Offline;
import Offline.Utils.LocalAddresses;
import Offline.Utils.User;
import com.google.protobuf.ByteString;
import com.turn.ttorrent.client.Client;
import com.turn.ttorrent.client.SharedTorrent;
import com.turn.ttorrent.common.Torrent;
//import javafx.util.converter.ByteStringConverter;
import com.turn.ttorrent.tracker.TrackedTorrent;
import com.turn.ttorrent.tracker.Tracker;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TorrentUtil {

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

    public static void upload(Torrent t, String path, Tracker tck) throws IOException, NoSuchAlgorithmException
    {
        ArrayList<LocalAddresses> ownAddresses = Offline.findLocalAddresses();
        File dest = new File(path);
        //Seeding starts.
        SharedTorrent st = new SharedTorrent(t, dest.getParentFile());
        Client c = new Client(
                InetAddress.getByName(ownAddresses.get(0).getIpv4()),
                st);
        c.share(-1);
        tck.announce(new TrackedTorrent(t));

        //Creates a protobuf to send file info
        TorrentWrapperOuterClass.TorrentWrapper tw = TorrentWrapperOuterClass.TorrentWrapper.newBuilder().setContent(ByteString.copyFrom(t.getEncoded())).build();
        //Offline sends to all users
        ConcurrentHashMap<String, User> foundUsers = Offline.listener.getUsers();
        for (Map.Entry<String, User> entry : foundUsers.entrySet()) {
            if(!entry.getValue().getUsername().equals(t.getCreatedBy())){
                System.out.println("Sending to: " + entry.getKey());
                Socket s = new Socket(entry.getValue().getIpv4(), 5558);
                tw.writeDelimitedTo(s.getOutputStream());
            }
        }
    }

    public static void upload(Torrent t, String path, Channel ch) throws IOException, NoSuchAlgorithmException, InterruptedException {
        ArrayList<LocalAddresses> ownAddresses = Offline.findLocalAddresses();
        File dest = new File(path);
        //Seeding starts.
        SharedTorrent st = new SharedTorrent(t, dest.getParentFile());
        Client c = new Client(
                InetAddress.getByName(ownAddresses.get(0).getIpv4()),
                st);
        c.share(-1);
        //Creates a protobuf to send file info
        TorrentWrapperOuterClass.TorrentWrapper tw = TorrentWrapperOuterClass.TorrentWrapper.newBuilder().setContent(ByteString.copyFrom(t.getEncoded())).build();
        //Escreve e espera pela escrita no socket
        ch.writeAndFlush(tw).sync();
    }

    public static void download(String ipv4, SharedTorrent st)
    {
        try {
            Client c = new Client(
                    InetAddress.getByName(ipv4),
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
