package Core;

import Network.ServerWrapper;
import Util.FileUtils;
import Util.TorrentUtil;
import com.google.protobuf.ByteString;
import com.google.protobuf.CodedInputStream;
import com.turn.ttorrent.client.Client;
import com.turn.ttorrent.common.Peer;
import com.turn.ttorrent.common.Torrent;
import com.turn.ttorrent.tracker.TrackedPeer;
import com.turn.ttorrent.tracker.TrackedTorrent;
import com.turn.ttorrent.tracker.Tracker;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MainServerListener extends Thread{
    private int port;
    private ServerSocket server;
    private boolean stop;
    private Tracker tck;
    private Map<String, Client> openClients;
    private ConcurrentHashMap<String, ArrayList<TrackedPeer>> injectionsWaiting;

    public MainServerListener(int portParam, Tracker tckParam, Map<String, Client> clientsParam, ConcurrentHashMap<String, ArrayList<TrackedPeer>> injectionsWaitingParam) throws IOException {
        this.port = portParam;
        this.tck = tckParam;
        this.openClients = clientsParam;
        this.injectionsWaiting = injectionsWaitingParam;
        this.stop = false;
        this.server = new ServerSocket(port);
    }

    @Override
    public void run() {
        while (!stop){
            try {
                //Wait for a new connection;
                Socket s = server.accept();
                OutputStream out = s.getOutputStream();
                CodedInputStream in = CodedInputStream.newInstance(s.getInputStream());

                byte[] length_b = in.readRawBytes(4);
                int l = ByteBuffer.wrap(length_b).getInt();
                byte[] data = in.readRawBytes(l);
                ServerWrapper.ServerMessage sm = ServerWrapper.ServerMessage.parseFrom(data);
                //Received a torrent, let's handle
                channelRead(sm);

            } catch (Exception e) {
                try {
                    this.shutdown();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                e.printStackTrace();
            }
        }
    }

    public void shutdown() throws IOException {
        //Close server channel;
        this.stop =true;
        server.close();
    }

    private void channelRead(ServerWrapper.ServerMessage request) throws IOException, NoSuchAlgorithmException, InterruptedException, ParserConfigurationException, SAXException {
        Torrent t = new Torrent(request.getTrackerTorrent().getContent().toByteArray(), false);
        String group = request.getTrackerTorrent().getGroup();
        //Verificar o nosso local na lista de trackers e colocar
        //o próprio em primeiro lugar
        String ip = FileUtils.getMyIP();
        //Vamos colocar o nosso IP em primeiro lugar;
        int iteration = 0;
        URI uri = null;
        for(URI uriIt: t.getAnnounceList().get(0)){
            if(uriIt.getPort() == tck.getAnnounceUrl().getPort() &&
                    uriIt.getHost().equals(ip)){
                uri = uriIt;
                break;
            }
            iteration++;
        }
        //Redirecionar o torrent para os outros trackers se eu
        //sou o principal
        if(iteration == 0) {
            redirect(t, group);
        }else {
            //Recodificiar se não sou o principal
            t.getAnnounceList().get(0).remove(iteration);
            t.getAnnounceList().get(0).add(0, uri);
            //Volta a codificar novamente o torrent
            t.newAnnounceEncode();
        }

        //Save torrent for fault sake
        FileUtils.saveTorrent(t);
        //Init a client, so server can get the file
        Client serverCli = TorrentUtil.initClient(t, FileUtils.fileDir);
        openClients.put(t.getHexInfoHash(), serverCli);
        //Get a tracked torrent with observables;
        TrackedTorrent tt =  TorrentUtil.announceTrackedTorrentWithObservers(tck, t, openClients, true);

        //Obtem o peer local e define-o para
        //recebermos os updates de users normais também
        Peer cli = serverCli.getPeerSpec();
        tt.setlocalInjectPeerID(cli.getHexPeerId());
        //Efetuar o pedido de injeção aos outros servers e injetar os pendentes
        tt.injectPeer(new TrackedPeer(tt, cli.getIp(), cli.getPort(), cli.getPeerId()));
        TorrentUtil.injectionRequest(cli, injectionsWaiting, tt);
    }

    private void redirect(Torrent t, String group) throws IOException {
        List<URI> trackers = t.getAnnounceList().get(0);
        //Remover-me
        trackers.remove(0);
        //Redirecionar
        for(URI tracker: trackers) {
            Socket s = new Socket(tracker.getHost(), 5000);
            ServerWrapper.TrackerTorrent tt = ServerWrapper.TrackerTorrent.newBuilder()
                    .setContent(ByteString.copyFrom(t.getEncoded()))
                    .setGroup(group)
                    .build();
            ServerWrapper.ServerMessage im = ServerWrapper.ServerMessage.newBuilder()
                    .setTrackerTorrent(tt)
                    .build();
            //Efetuar a escrita de forma igual ao front end.
            byte[] size = ByteBuffer.allocate(4).putInt(im.getSerializedSize()).array();
            s.getOutputStream().write(size);
            im.writeTo(s.getOutputStream());
            s.getOutputStream().flush();
            s.getOutputStream().close();
            s.close();
        }
    }
}
