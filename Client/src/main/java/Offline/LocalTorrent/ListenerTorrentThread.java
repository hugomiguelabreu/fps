package Offline.LocalTorrent;

import Network.TorrentWrapperOuterClass;
import com.turn.ttorrent.client.SharedTorrent;
import com.turn.ttorrent.common.Torrent;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.Observable;
import java.util.Observer;

public class ListenerTorrentThread extends Thread{

    private Socket socket;
    private String ownAddress;

    public ListenerTorrentThread(Socket socket,String ownaddress ){

        this.socket = socket;
        this.ownAddress = ownaddress;
    }

    @Override
    public void run(){

        try {

            InputStream stream = socket.getInputStream();

            TorrentWrapperOuterClass.TorrentWrapper torrent = TorrentWrapperOuterClass.TorrentWrapper.parseDelimitedFrom(stream);

            File dest = new File("/tmp/");

            final SharedTorrent st = new SharedTorrent(torrent.getContent().toByteArray(), dest);

            com.turn.ttorrent.client.Client c = new com.turn.ttorrent.client.Client(
                    InetAddress.getByName(ownAddress),
                    st);

            c.setMaxDownloadRate(0.0);
            c.setMaxUploadRate(0.0);

            //Download and seed
            c.addObserver(new Observer() {
                @Override
                public void update(Observable o, Object arg) {
                    System.out.println(st.getCompletion());
                    System.out.println(arg);
                }
            });

            c.share(-1);

            if (com.turn.ttorrent.client.Client.ClientState.ERROR.equals(c.getState())) {
                System.exit(1);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }


    }

}
