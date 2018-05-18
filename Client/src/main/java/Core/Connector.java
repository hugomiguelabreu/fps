package Core;

import Network.ClientWrapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;

public class Connector extends Thread{

    private OutputStream out;
    private InputStream in;
    private Socket socket;
    private boolean connected;
    private boolean stop;

    public Connector(Collection<String> ips) throws URISyntaxException {
        for (String ip : ips) {
            URI uri = new URI("fps://" + ip);
            try {
                socket = new Socket(uri.getHost(), uri.getPort());
                out = socket.getOutputStream();
                in = socket.getInputStream();
            } catch (Exception e) {
                System.out.println("\u001B[31mError opening socket\u001B[0m");
                this.connected = false;
                return;
            }
        }
        this.connected = true;
        this.stop = false;
    }

    @Override
    public void run() {
        while (!stop){
            try {
                boolean cm =
                        ClientWrapper.ClientMessage.parseDelimitedFrom(in).getResponse().getRep();
                //TODO: verificar o que recebo e chamar cenas;
                System.out.println("RECEBI CENAS " + cm);
                //TODO: check if not null
            } catch (Exception e) {
                this.close();
                //e.printStackTrace();
            }
        }
    }

    public boolean send(ClientWrapper.ClientMessage msg){
        if(!this.connected)
            return false;

        try {
            out.write(msg.getSerializedSize());
            msg.writeTo(out);
        } catch (IOException e) {
            return false;
            //e.printStackTrace();
        }
        return true;
    }

    public boolean isConnected(){
        return this.connected;
    }

    public boolean close(){
        if(!this.connected)
            return true;

        try {
            this.connected = false;
            this.stop = true;
            out.flush();
            out.close();
            in.close();
            socket.close();
        } catch (IOException e) {
            return false;
            //e.printStackTrace();
        }
        this.interrupt();
        return true;
    }
}
