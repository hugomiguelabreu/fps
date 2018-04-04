import Misc.TorrentUtil;
import com.turn.ttorrent.common.Torrent;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class Main {

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {

        TorrentUtil tu = new TorrentUtil("kk", "kk");
        Torrent t = tu.createTorrent("file.torrent", "file.out", 512);
        System.out.println(t.getName());
        System.out.println(t.getSize());
        System.out.println(t.isMultifile());
        System.out.println(t.getCreatedBy());
    }

}
