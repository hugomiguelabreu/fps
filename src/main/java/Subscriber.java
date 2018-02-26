import org.zeromq.ZMQ;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Subscriber extends Thread {

    private ArrayList<String> connections;
    private ArrayList<String> subscriptions;
    private ZMQ.Context contextProc;
    private ZMQ.Socket socket;
    private boolean k = false;

    public Subscriber(){
        contextProc = ZMQ.context(1);
        socket = contextProc.socket(ZMQ.SUB);
        socket.connect ("tcp://localhost:5600");
    }

    public void para(){
        k = true;
    }

    public void run(){

        socket.subscribe("");
        byte[] b;
        List<Byte> lb = new ArrayList<>();

        while (!k) {
            b = socket.recv();

            for (byte e:b) {
                lb.add(e);
            }

            System.out.println(".\r");
            System.out.println("..\r");
            System.out.println("...\r");

            if(!socket.hasReceiveMore())
                break;
        }

        byte[] finalissimo = new byte[lb.size()];
        int i = 0;
        for (Byte k:lb) {
            finalissimo[i++] = k.byteValue();
        }

        Path path = Paths.get("/home/hugoabreu/Desktop/filePubSub/src/main/java/kek" + new Random().nextInt() + ".jpg");
        try {
            java.nio.file.Files.write(path, finalissimo);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
