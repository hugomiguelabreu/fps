import client_network.ClientWrapper;
import com.google.protobuf.ByteString;
import com.google.protobuf.CodedInputStream;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.lang.reflect.Array;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.util.UUID;

public class Client implements Runnable{
    String username;
    String password;

    public Client(String name, String password) {
        this.username = name;
        this.password = password;
    }


    private void send(Socket socket, ClientWrapper.ClientMessage wrapper) {

       try {
           CodedInputStream in = CodedInputStream.newInstance(socket.getInputStream());

           byte[] size = ByteBuffer.allocate(4).putInt(wrapper.getSerializedSize()).array();
           socket.getOutputStream().write(size);
           wrapper.writeTo(socket.getOutputStream());

           byte[] header = in.readRawBytes(4);
           int l = ByteBuffer.wrap(header).getInt();
           System.out.println(l);
           byte[] data = in.readRawBytes(l);
           System.out.println(username + ": " + ClientWrapper.ClientMessage.parseFrom(data));

       } catch (Exception e) {}

    }


    @Override
    public void run() {
        Socket socket = null;
        try {
            int port;

            if(username.equals("Melon") || username.equals("Vaarg"))
                port = 2001;
            else
                port = 2002;

            socket = new Socket("localhost"  , port);

            ClientWrapper.Login login = ClientWrapper.Login.newBuilder()
                    .setUsername(username)
                    .setPassword(password)
                    .build();

            ClientWrapper.ClientMessage wrapper = ClientWrapper.ClientMessage.newBuilder()
                    .setLogin(login).build();

            send(socket, wrapper);
            Thread.sleep(2000);

            ClientWrapper.GroupUsers cg = ClientWrapper.GroupUsers.newBuilder()
                    .setGroupUsers(username)
                    .build();

            wrapper = ClientWrapper.ClientMessage.newBuilder()
                    .setGroupUsers(cg).build();

            send(socket,wrapper);


            /*


            if(username.equals("Vaarg")){
                ClientWrapper.CreateGroup cg = ClientWrapper.CreateGroup.newBuilder()
                                                .setGroup("BGUM24")
                                                .build();

                wrapper = ClientWrapper.ClientMessage.newBuilder()
                        .setCreateGroup(cg).build();


                send(socket, wrapper);

            }

             if(!username.equals("Vaarg")){
                ClientWrapper.JoinGroup cg = ClientWrapper.JoinGroup.newBuilder()
                                                .setGroup("Reuniao")
                                                .build();

                wrapper = ClientWrapper.ClientMessage.newBuilder()
                        .setJoinGroup(cg).build();


                send(socket, wrapper);

            }



            ClientWrapper.OnlineUsers cg = ClientWrapper.OnlineUsers.newBuilder()
                                            .setOnlineUsers("leddit")
                                            .build();

            wrapper = ClientWrapper.ClientMessage.newBuilder()
                    .setOnlineUsers(cg).build();

            System.out.println(wrapper);

            size = ByteBuffer.allocate(4).putInt(wrapper.getSerializedSize()).array();
            socket.getOutputStream().write(size);
            wrapper.writeTo(socket.getOutputStream());

            header = in.readRawBytes(4);
            l = ByteBuffer.wrap(header).getInt();
            data = in.readRawBytes(l);

            System.out.println(ClientWrapper.ClientMessage.parseFrom(data));





            if (username.equals("jib")){
                ClientWrapper.TorrentWrapper torr = ClientWrapper.TorrentWrapper.newBuilder()
                        .setGroup("leddit")
                        .setId("JIB")
                        .setContent(ByteString.copyFromUtf8("ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffasdasdasdasdaaaaaaaaaaaaaaaaaaaaaakc.xmqdslllllllllllllllllcccccccccccccccccccccccwooooooooooooooooooooooouuuuuuuuuuuuuuhhhhhhhhhhhhhhhhhhhhhhhhhffffffffffffffffffffvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv            wwwwwwwwwqqqqqqqqqqqrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrr"))
                        .build();

                wrapper = ClientWrapper.ClientMessage.newBuilder()
                        .setTorrentWrapper(torr).build();


                System.out.println("tamanho do proto: " + wrapper.getSerializedSize());


                socket.getOutputStream().write(ByteBuffer.allocate(4).putInt(wrapper.getSerializedSize()).array());
                wrapper.writeTo(socket.getOutputStream());
            }

            byte[] length_b = in.readRawBytes(4);
            l = ByteBuffer.wrap(length_b).getInt();
            System.out.println(l);


            data = in.readRawBytes(l);

            System.out.println(ClientWrapper.ClientMessage.parseFrom(data).getTorrentWrapper());
            socket.close();

            */



        } catch (Exception e) { e.printStackTrace();}
    }

    public static void main(String[] args) {

        (new Thread(new Client("Melon", "asd"))).start();
        (new Thread(new Client("Vaarg", "123"))).start();
        (new Thread(new Client("Tsuman", "123"))).start();
    }
}
