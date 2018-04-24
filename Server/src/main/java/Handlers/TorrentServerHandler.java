package Handlers;

import Core.ServerClient;
import Network.TorrentWrapperOuterClass;
import Util.FileUtils;
import com.turn.ttorrent.client.SharedTorrent;
import com.turn.ttorrent.common.Torrent;
import com.turn.ttorrent.tracker.TrackedTorrent;
import com.turn.ttorrent.tracker.Tracker;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.commons.io.IOUtils;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.UUID;


public class TorrentServerHandler extends SimpleChannelInboundHandler<TorrentWrapperOuterClass.TorrentWrapper>{

    private Tracker tck;

    public TorrentServerHandler(Tracker trackedTorrentsParam) {
        this.tck = trackedTorrentsParam;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, TorrentWrapperOuterClass.TorrentWrapper torrentWrapper) throws Exception {
        Torrent t = new Torrent(torrentWrapper.getContent().toByteArray(), false);

        new File(System.getProperty( "user.home" ) + "/persisted-files").mkdirs();
        FileOutputStream fos = new FileOutputStream(System.getProperty( "user.home" ) + "/persisted-files/" + t.getHexInfoHash());
        t.save(fos);
        IOUtils.closeQuietly(fos);

        TrackedTorrent tt = new TrackedTorrent(t);
        //No need to check if torrent is already announced
        tck.announce(tt);
        ServerClient sc = new ServerClient(t);
        sc.start();
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
