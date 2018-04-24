package Handlers;


import Network.TorrentWrapperOuterClass;
import com.turn.ttorrent.client.SharedTorrent;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.security.NoSuchAlgorithmException;
import java.util.Observable;
import java.util.Observer;

public class TorrentListenerHandler extends SimpleChannelInboundHandler<TorrentWrapperOuterClass.TorrentWrapper> {

    private String ownAddress;

    public TorrentListenerHandler(String ownAddress) {

        super(false);
        this.ownAddress = ownAddress;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, TorrentWrapperOuterClass.TorrentWrapper torrentWrapper) throws Exception {

        System.out.println("kappa");

        try {

            File dest = new File("/tmp/");

            final SharedTorrent st = new SharedTorrent(torrentWrapper.getContent().toByteArray(), dest);

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
