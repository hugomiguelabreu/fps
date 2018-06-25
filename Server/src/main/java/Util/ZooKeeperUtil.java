package Util;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.atomic.DistributedAtomicLong;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

public class ZooKeeperUtil {
    private static CuratorFramework client;
    private static RetryPolicy retryPolicy;

    public ZooKeeperUtil(String host){
        try {
            this.connect(host, 1000);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void connect(String hosts, int sessionTimeout) throws IOException, InterruptedException {
        retryPolicy = new ExponentialBackoffRetry(1000, 3);
        client = CuratorFrameworkFactory.newClient(hosts, retryPolicy);
        client.start();
    }

    public static boolean incrementReceived(String group, String torrentId) throws Exception {

        String path = "/groups/" + group + "/torrents/" + torrentId;
        DistributedAtomicLong counter = new DistributedAtomicLong(client, path + "/file/counter", retryPolicy);

        if (client.checkExists().forPath(path + "/file/counter") == null)
            counter.initialize((long)0);


        byte[] data = client.getData().forPath(path + "/file/total");
        int total = Integer.parseInt(new String(data));

        if (counter.increment().postValue() == total - 1) {
            client.delete().deletingChildrenIfNeeded().forPath(path);
            return true;
        }

        return false;

    }

    public static boolean registerTracker(String serverId, String serverHost){
        String path = "/trackers/" + serverId;
        try{
            if (client.checkExists().forPath(path) != null){
                client.setData().forPath(path, serverHost.getBytes());
            }else{
                client.create().withMode(CreateMode.EPHEMERAL).forPath(path, serverHost.getBytes());
            }
        } catch (Exception e) { return false; }

        return true;
    }

}
