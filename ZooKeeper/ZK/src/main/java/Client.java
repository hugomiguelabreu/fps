import client_network.ClientWrapper;
import com.google.protobuf.ByteString;

import java.io.File;
import java.net.Socket;
import java.nio.file.Files;
import java.util.UUID;

public class Client implements Runnable{
    String username;
    String password;

    public Client(String name, String password) {
        this.username = name;
        this.password = password;
    }

    @Override
    public void run() {
        Socket socket = null;
        try {

            if(username.equals("jib") || username.equals("cr7"))
                socket = new Socket("localhost"  , 2000);
            else
                socket = new Socket("localhost"  , 2001);

            ClientWrapper.Login login = ClientWrapper.Login.newBuilder()
                    .setUsername(username)
                    .setPassword(password).build();

            ClientWrapper.ClientMessage wrapper = ClientWrapper.ClientMessage.newBuilder()
                    .setLogin(login).build();

            socket.getOutputStream().write(wrapper.getSerializedSize());
            wrapper.writeTo(socket.getOutputStream());
            System.out.println(ClientWrapper.ClientMessage.parseDelimitedFrom(socket.getInputStream()).getResponse().getRep());
            Thread.sleep(2000);


            char[] data = new char[100000000];
            String uniqueId = UUID.randomUUID().toString().split("-")[4];

            StringBuilder sb = new StringBuilder();

            if (username.equals("jib")){

                File file = new File("/home/jreis/Documents/output.dat");
                ClientWrapper.TorrentWrapper torr = ClientWrapper.TorrentWrapper.newBuilder()
                        .setGroup("leddit")
                        .setId(uniqueId)
                        .setContent(ByteString.copyFrom(Files.readAllBytes(file.toPath()))).build();


                wrapper = ClientWrapper.ClientMessage.newBuilder()
                        .setTorrentWrapper(torr).build();

                socket.getOutputStream().write(wrapper.getSerializedSize());
                wrapper.writeTo(socket.getOutputStream());
            }
            System.out.println(username + " : " + ClientWrapper.ClientMessage.parseDelimitedFrom(socket.getInputStream()).getTorrentWrapper());
            socket.close();


        } catch (Exception e) { e.printStackTrace();}
    }

    public static void main(String[] args) {
        (new Thread(new Client("jib", "asd"))).start();
            (new Thread(new Client("divisao", "123"))).start();
        (new Thread(new Client("cr7", "123"))).start();
    }
}
