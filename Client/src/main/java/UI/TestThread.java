package UI;

import Event.ArrayListEvent;
import Event.ConcurrentHashMapEvent;
import Offline.Utils.User;
import Util.TorrentUtil;
import com.turn.ttorrent.common.Torrent;
import com.turn.ttorrent.tracker.Tracker;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class TestThread extends Thread{

    private ArrayListEvent<Torrent> kek;

    public TestThread(ArrayListEvent<Torrent> kek){
        this.kek = kek;
    }

    @Override
    public void run(){


        for(int i = 0; i<4; i++){

            ArrayList<String> ts = new ArrayList<>();

            try {

                Torrent t = TorrentUtil.createTorrent("/home/padrao/Desktop/kappa.jpg", "usrname", ts);
                kek.addTorrent(t);
                Thread.sleep(1000);

            } catch (IOException | NoSuchAlgorithmException | InterruptedException e) {
                e.printStackTrace();
            }

        }

    }

}
