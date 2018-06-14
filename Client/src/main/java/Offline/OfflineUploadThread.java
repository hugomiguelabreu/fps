package Offline;

import Util.TorrentUtil;
import com.turn.ttorrent.common.Torrent;
import com.turn.ttorrent.tracker.Tracker;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class OfflineUploadThread extends Thread {


    private static Tracker offlineTck;
    private String path;
    private String username;
    private Torrent t;

    public OfflineUploadThread(){

    }

    public void newUpload(String path, String username, Tracker offlineTck){

        this.path = path;
        this.username = username;
        this.offlineTck = offlineTck;
    }

    public void run(){

        try {

            ArrayList<String> trc = new ArrayList<String>();
            trc.add("http://" + Offline.findLocalAddresses().get(0).getIpv4()  + ":6969/announce");

            //Already has a tracker running?
            if(offlineTck == null) { //TODO o tracker pode estar parado e nao ser null pq lambda exps
                String httpAddress = Offline.findLocalAddresses().get(0).getIpv4();
                offlineTck = new Tracker(new InetSocketAddress(InetAddress.getByName(httpAddress), 6969));
                offlineTck.start();
            }

            t = TorrentUtil.createTorrent(path, username, trc);
            TorrentUtil.upload(t, path, offlineTck, username);

            System.out.println("Upload intention initiated");

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }


    }


}
