import files.Receiver;
import files.Sender;
import probes.Broadcast;
import probes.Listener;

import java.io.IOException;
import java.util.Scanner;

public class Main {

    //  mvn clean package
    //  mvn exec:java -Dexec.mainClass="Main" -s "pom.xml -Dexec.args="r"

    public static void main(String[] args) {

        Broadcast b = new Broadcast();
        b.start();

        Listener l = new Listener(b.getIps());
        l.start();



        if(args[0].equals("s")){

            Scanner scanner=new Scanner(System.in);
            scanner.nextLine(); // teste

            Sender s = new Sender(b.getIps(), l.getPeers());
            try {
                s.send();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }else if(args[0].equals("r")){

            Receiver r = new Receiver(b.getIps(), l.getPeers());
            try {
                r.listen();
            } catch (IOException e) {
                e.printStackTrace();
            }


        }

    }

}
