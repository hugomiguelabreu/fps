
import org.apache.zookeeper.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class Zook implements Runnable{

    public static void main(String[] args) {
        (new Thread (new Zook())).run();
    }

    public List<String> getOnline(String group) throws KeeperException, InterruptedException {
        String path = "/groups/" + group + "/users" ;
        List<String> children = null;
        try {
            children = zk.getChildren(path, false);
        } catch (Exception e) { e.printStackTrace();}

        List<String> online = new ArrayList<>();

        for (String user : children){
            byte[] data = zk.getData("/users/" + user + "/online", false, null);
            String result = new String(data);

            if (result.equals("true"))
                online.add(user);
        }

        return online;
    }

    public List<String> getGroupUsers(String group) {
        String path = "/groups/" + group + "/users" ;
        List<String> children = null;
        try {
            children = zk.getChildren(path, false);
        } catch (Exception e) { e.printStackTrace();}

        return children;
    }

    public void incrementReceived(String group, String torrentId, String user) throws KeeperException, InterruptedException {
        String path = "/groups/" + group + "/torrents/" + torrentId;
        zk.create(path + "/file" + user, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);

        List<String> children = zk.getChildren(path, false);
        byte[] data_total = zk.getData(path + "/file/total", false, null);
        int total = Integer.parseInt(Arrays.toString(data_total));

        if (children.size() == total - 1)
            ZKUtil.deleteRecursive(zk, path);
    }


    private ZooKeeper zk;

    public void connect(String hosts, int sessionTimeout)
            throws IOException, InterruptedException {

        final CountDownLatch connectedSignal = new CountDownLatch(1);
        ZooKeeper zk = new ZooKeeper(hosts, sessionTimeout, new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                if (event.getState() == Watcher.Event.KeeperState.SyncConnected)
                    connectedSignal.countDown();
            }
        });
        connectedSignal.await();
        this.zk = zk;
    }

    @Override
    public void run() {
        try {
            this.connect("localhost:2184", 1000);
            System.out.println(this.getGroupUsers("leddit"));
            System.out.println(this.getOnline("leddit"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
