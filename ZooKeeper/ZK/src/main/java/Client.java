import client_network.Wrapper;
import com.google.protobuf.ByteString;

import java.io.IOException;
import java.net.Socket;

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
            if(username.equals("jib"))
                socket = new Socket("localhost"  , 2000);
            else
                socket = new Socket("localhost"  , 2001);

            Wrapper.Login login = Wrapper.Login.newBuilder()
                    .setUsername(username)
                    .setPassword(password).build();

            Wrapper.ClientMessage wrapper = Wrapper.ClientMessage.newBuilder()
                    .setLogin(login).build();

            socket.getOutputStream().write(wrapper.getSerializedSize());
            wrapper.writeTo(socket.getOutputStream());
            System.out.println(Wrapper.ClientMessage.parseDelimitedFrom(socket.getInputStream()).getResponse().getRep());
            Thread.sleep(3000);

            if (username.equals("jib")){
                Wrapper.TorrentWrapper torr = Wrapper.TorrentWrapper.newBuilder()
                        .setGroup("leddit")
                        .setContent(ByteString.copyFromUtf8("le xis de XD")).build();

                wrapper = Wrapper.ClientMessage.newBuilder()
                        .setTorrentWrapper(torr).build();

                socket.getOutputStream().write(wrapper.getSerializedSize());
                wrapper.writeTo(socket.getOutputStream());
            }
            System.out.println(username + " : " + Wrapper.ClientMessage.parseDelimitedFrom(socket.getInputStream()).getTorrentWrapper());


        } catch (Exception e) { e.printStackTrace();}
    }

    public static void main(String[] args) {
        (new Thread(new Client("jib", "asd"))).start();
        (new Thread(new Client("divisao", "123"))).start();
        (new Thread(new Client("cr7", "123"))).start();
    }
}
