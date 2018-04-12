import org.apache.zookeeper.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class Keeper {

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

    /**
     * Registers user to a given username, password and name.
     * @return true if successful, false otherwise.
     */
    public boolean register(String username, String password, String name){
        StringBuilder path = new StringBuilder();

        path.append("/users/").append(username);
        StringBuilder path_sv = new StringBuilder(path).append("/sv");
        StringBuilder path_name = new StringBuilder(path).append("/name");
        StringBuilder path_online = new StringBuilder(path).append("/online");

        try {
            zk.create(path.toString(), password.getBytes(),
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            zk.create(path_name.toString(), name.getBytes(),
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            zk.create(path_sv.toString(), "".getBytes(),
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            zk.create(path_online.toString(), "false".getBytes(),
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        } catch (Exception e) { e.printStackTrace(); return false; }

        return true;
    }

    /**
     * Logins user to a given username, password and name.
     * @return true if successful, false otherwise.
     */
    public boolean login(String username, String password, String sv){
        StringBuilder path = new StringBuilder("/users/").append(username);

        try {
            byte[] data = zk.getData(path.toString(), false, null);

            if (!password.equals(new String(data)))
                return false;

            StringBuilder path_sv = new StringBuilder(path).append("/sv");
            StringBuilder path_online = new StringBuilder(path).append("/online");
            zk.setData(path_sv.toString(), sv.getBytes(),
                    zk.exists(path_sv.toString(),false).getVersion());
            zk.setData(path_online.toString(), "true".getBytes(),
                    zk.exists(path_online.toString(),false).getVersion());

        } catch (Exception e) { e.printStackTrace(); return false; }

        return true;
    }


    public boolean createGroup(String groupName, String user) {
        String path = "/groups/" + groupName;

        try {
            if (zk.exists(path,false) != null)
                return false;

            zk.create(path, null,
                ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);

            zk.create(path + "/log", null,
                ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);

            zk.create(path + "/meta", user.getBytes(),
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);

            zk.create(path + "/users", user.getBytes(),
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        } catch (Exception e) { return false; }

        return true;
    }

    public boolean joinGroup(String groupName, String user) {
        String path = "/groups/" + groupName + "/users";

        try{
            if (zk.exists(path,false) == null)
                return false;

            zk.create(path + "/" + user, null,
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        } catch (Exception e) { return false; }
        return true;
    }

    public void heartbeat(String username){
        StringBuilder path = new StringBuilder("/users/").append(username).append("/online");

        try{
            byte[] data = zk.getData(path.toString(), false, null);

            if ("true" != Arrays.toString(data))
                zk.setData(path.toString(), "true".getBytes(),
                    zk.exists(path.toString(),true).getVersion());

        }catch(Exception e) {e.printStackTrace();}
    }

    public void setOffline(String username){
        StringBuilder path = new StringBuilder("/users/").append(username).append("/online");

        try{
            byte[] data = zk.getData(path.toString(), false, null);

            if ("false" != Arrays.toString(data))
                zk.setData(path.toString(), "false".getBytes(),
                        zk.exists(path.toString(),true).getVersion());

        }catch(Exception e) {e.printStackTrace();}
    }

    public void getGroupUsers(String group){
        String path = "/" + group;
        List<String> children = null;
        try {
            children = zk.getChildren(path, false);
        } catch (Exception e) { e.printStackTrace();}

        if (children.isEmpty()) {
            System.out.printf("No members in group %s\n", group);
            return;
        }
        Collections.sort(children);
        System.out.println(children);
        System.out.println("--------------------");
    }

    public void init(){
        try {
            zk.create("/users", null,
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);

            zk.create("/groups", null,
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        } catch (Exception e) {e.printStackTrace(); }
    }


    public static void main(String[] args){
        Keeper k = new Keeper();
        try {
            k.connect("localhost:2184", 1000);
        } catch (Exception e) {e.printStackTrace(); }

        k.init();
    }

}
