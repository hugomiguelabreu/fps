import files.Receiver;
import probes.Broadcast;
import probes.Listener;

import java.io.IOException;

public class MainRec {

    //  mvn clean package
    //  mvn exec:java -Dexec.mainClass="Main" -s "pom.xml -Dexec.args="r"

    public static void main(String[] args) {

        Broadcast b = new Broadcast();
        b.start();

        Listener l = new Listener(b.getIps());
        l.start();

        Receiver r = new Receiver(b.getIps(), l.getPeers());
        try {
            r.listen();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }

}
