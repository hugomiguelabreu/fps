
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

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

    public void incrementReceived(String group, String torrentId) throws KeeperException, InterruptedException {
        String path = "/groups/" + group + "/torrents/" + torrentId + "/file" ;

        byte[] data_current = zk.getData(path + "/current", false, null);
        int current = Integer.parseInt(Arrays.toString(data_current));

        byte[] data_total = zk.getData(path + "/total", false, null);
        int total = Integer.parseInt(Arrays.toString(data_total));

        if (current == total - 1)
            return;
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
