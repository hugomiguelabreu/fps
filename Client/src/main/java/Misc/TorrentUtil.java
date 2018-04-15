package Misc;
import Network.TorrentWrapperOuterClass;
import com.google.protobuf.ByteString;
import com.turn.ttorrent.common.Torrent;
import javafx.util.converter.ByteStringConverter;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

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
}
