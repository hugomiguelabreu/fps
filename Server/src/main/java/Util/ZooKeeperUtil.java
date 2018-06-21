package Util;

import org.apache.zookeeper.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

public class ZooKeeperUtil {
    private ZooKeeper zk;

    public ZooKeeperUtil(String host){
        try {
            this.connect(host, 1000);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void connect(String hosts, int sessionTimeout) throws IOException, InterruptedException {
            final CountDownLatch connectedSignal = new CountDownLatch(1);
            ZooKeeper zk = new ZooKeeper(hosts, sessionTimeout, event -> {
                if (event.getState() == Watcher.Event.KeeperState.SyncConnected)
                    connectedSignal.countDown();
            });
            connectedSignal.await();
            this.zk = zk;
    }

    public boolean registerTracker(String serverId, String serverHost){
        String path = "/trackers/" + serverId;
        try{
            if (zk.exists(path,false) != null){
                zk.setData(path.toString(), serverHost.getBytes(),
                        zk.exists(path.toString(),true).getVersion());
            }else{
                zk.create(path, serverHost.getBytes(),
                        ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
            }
        } catch (Exception e) { return false; }

        return true;
    }

}
