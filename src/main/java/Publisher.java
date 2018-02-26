import org.zeromq.ZMQ;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;


public class Publisher extends Thread {

    private ZMQ.Context context;
    private ZMQ.Socket socket;
    private byte[] bytesArray;

    public Publisher() throws IOException {

        File file = new File("/home/hugoabreu/Desktop/filePubSub/src/main/java/eder1.jpg");
        bytesArray = new byte[(int) file.length()];

        FileInputStream fis = new FileInputStream(file);
        fis.read(bytesArray); //read file into bytes[]
        fis.close();

        context = ZMQ.context(1);
        socket = context.socket(ZMQ.PUB);
        socket.bind("tcp://*:5600");
    }

    @Override
    public void run() {

        try {
            sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        while (true) {
            socket.send(bytesArray);
        }

    }
}
