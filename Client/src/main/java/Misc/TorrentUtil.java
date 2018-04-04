package Misc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import com.turn.ttorrent.common.Torrent;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TorrentUtil {

    private static final Logger logger = LoggerFactory.getLogger(Torrent.class);

    /**
     * Constructs a new util to handle
     * and create torrent filel
     *
     * @param filename Name to save the torrent
     */

    public TorrentUtil(String filename, String source)
    {
    }

    public Torrent createTorrent(String filename, String source, int pieces) throws IOException, NoSuchAlgorithmException {
        pieces = Torrent.DEFAULT_PIECE_LENGTH;
        //pieces = pieces * 1024;
        logger.info("Using piece length of {} bytes.", pieces);
        logger.warn("At this point it should get the interested trackers");

        Vector<String> announceURLs = new Vector<String>();
        announceURLs.add("http://localhost:6969");

        OutputStream fos = null;
        try {
            if (filename != null) {
                fos = new FileOutputStream(filename);
            }
            //Process the announce URLs into URIs
            List<URI> announceURIs = new ArrayList<URI>();
            for (String url : announceURLs) {
                announceURIs.add(new URI(url));
            }
            //Create the announce-list as a list of lists of URIs
            //Assume all the URI's are first tier trackers
            List<List<URI>> announceList = new ArrayList<List<URI>>();
            announceList.add(announceURIs);

            File sourceFile = new File(source);
            if (!sourceFile.exists() || !sourceFile.canRead()) {
                throw new IllegalArgumentException(
                        "Cannot access source file or directory " +
                                sourceFile.getName());
            }

            String creator = String.format("%s",
                    System.getProperty("user.name"));

            Torrent torrent = null;
            if (sourceFile.isDirectory()) {
                logger.error("Can't transfer a directory.");
                return null;
            } else {
                torrent = Torrent.create(sourceFile, pieces, announceList, creator);
            }

            torrent.save(fos);

        } catch (Exception e) {
            logger.error("{}", e.getMessage(), e);
            System.exit(2);
        } finally {
            if (fos != System.out) {
                IOUtils.closeQuietly(fos);
            }
            return loadTorrent(filename);
        }
    }

    public Torrent loadTorrent(String filename) throws IOException, NoSuchAlgorithmException
    {
        return Torrent.load(new File(filename), true);
    }
}
