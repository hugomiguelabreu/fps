
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.atomic.DistributedAtomicLong;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Arrays;


public class Zook implements Runnable{

    public static void main(String[] args) {
        (new Thread (new Zook())).run();
    }

    private CuratorFramework client;
    private RetryPolicy retryPolicy;


    public boolean incrementReceived(String group, String torrentId, String user) throws Exception {

        String path = "/groups/" + group + "/torrents/" + torrentId;
        DistributedAtomicLong counter = new DistributedAtomicLong(client, "/teste2", retryPolicy);

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




    public void connect(){

        retryPolicy = new ExponentialBackoffRetry(1000, 3);
        client = CuratorFrameworkFactory.newClient("localhost:2184", retryPolicy);
        client.start();

    }

    @Override
    public void run() {
        try {
            connect();
            byte[] bytes = client.getData().forPath("/groups/leddit/torrents/JIB/file");
            System.out.println(new String(bytes));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
