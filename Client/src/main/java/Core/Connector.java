package Core;

import Network.ClientWrapper;
import UI.AppController;
import Util.FileUtils;
import Util.ServerOperations;
import com.turn.ttorrent.common.Torrent;
import javafx.fxml.FXMLLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Connector extends Thread{

    private OutputStream out;
    private InputStream in;
    private Socket socket;
    private LinkedBlockingQueue<Boolean> responses;
    private boolean connected;
    private boolean stop;


    public Connector(Collection<String> ips) throws URISyntaxException {
        responses = new LinkedBlockingQueue<>();
        for (String ip : ips) {
            URI uri = new URI("fps://" + ip);
            try {
                socket = new Socket(uri.getHost(), uri.getPort());
                out = socket.getOutputStream();
                in = socket.getInputStream();
            } catch (Exception e) {
                System.out.println("\u001B[31mError opening socket\u001B[0m");
                this.connected = false;
                return;
            }
        }
        this.connected = true;
        this.stop = false;
    }

    @Override
    public void run() {
        while (!stop){
            try {
                ClientWrapper.ClientMessage cm = ClientWrapper.ClientMessage.parseDelimitedFrom(in);
                boolean torrent =
                        cm.getMsgCase().equals(ClientWrapper.ClientMessage.MsgCase.TORRENTWRAPPER);

                //Recieved a torrent for a group. Add that torrent to the map;
                if(torrent){
                    byte[] byteTorrent = cm.getTorrentWrapper().getContent().toByteArray();
                    String group = cm.getTorrentWrapper().getGroup();
                    Torrent t = new Torrent(
                            byteTorrent,
                            true);
                    ServerOperations.addTorrent(t, group);
                }else{
                    boolean response = cm.getResponse().getRep();
                    this.responses.offer(response);
                }

                System.out.println("RECEBI CENAS ");
                //TODO: check if not null

            } catch (Exception e) {
                e.printStackTrace();
                this.close();
            }
        }
    }

    public boolean readResponse() throws InterruptedException {
        return this.responses.take();
    }

    public boolean send(ClientWrapper.ClientMessage msg){
        if(!this.connected)
            return false;
        try {
            out.write(msg.getSerializedSize());
            msg.writeTo(out);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean isConnected(){
        return this.connected;
    }

    public boolean close(){
        if(!this.connected)
            return true;

        try {
            this.connected = false;
            this.stop = true;
            out.flush();
            out.close();
            in.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        this.interrupt();
        return true;
    }
}
