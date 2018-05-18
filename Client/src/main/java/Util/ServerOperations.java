package Util;

import Core.Connector;
import Network.ClientWrapper;

import java.io.IOException;
import java.net.URISyntaxException;

public class ServerOperations {

    private static Connector channel;

    public static void setChannel(Connector channel){
        ServerOperations.channel = channel;
    }

    public static boolean login(String username, String password) throws IOException {
        if(channel == null)
            return false;

        boolean ret = true;

        ClientWrapper.Login request = ClientWrapper.Login.newBuilder()
                .setUsername(username)
                .setPassword(password).build();

        ClientWrapper.ClientMessage wrapper = ClientWrapper.ClientMessage.newBuilder()
                .setLogin(request).build();

//        socket.getOutputStream().write(wrapper.getSerializedSize());
//        wrapper.writeTo(socket.getOutputStream());

        if(!channel.send(wrapper)){
            return false;
        }
        //ESPERAR PELA RESPOSTA

        return ret;

    }

    public static boolean register(String username, String password, String name) throws IOException, URISyntaxException {
        if(channel == null)
            return false;

        //Socket socket = new Socket("localhost", 2000);
        boolean ret = true;

        ClientWrapper.Register request = ClientWrapper.Register.newBuilder()
                .setUsername(username)
                .setPassword(password)
                .setName(name).build();
        ClientWrapper.ClientMessage wrapper = ClientWrapper.ClientMessage.newBuilder()
                .setRegister(request).build();

        if(!channel.send(wrapper)){
            return false;
        }
//        socket.getOutputStream().write(wrapper.getSerializedSize());
//        wrapper.writeTo(socket.getOutputStream());

//        ret = ClientWrapper.ClientMessage.parseDelimitedFrom(socket.getInputStream()).getResponse().getRep();

        return ret;
    }

    public static boolean createGroup(String groupName) throws IOException {
        if(channel == null)
            return false;

        boolean ret = true;

        ClientWrapper.CreateGroup request = ClientWrapper.CreateGroup.newBuilder()
                .setGroup(groupName).build();

        ClientWrapper.ClientMessage wrapper = ClientWrapper.ClientMessage.newBuilder()
                .setCreateGroup(request).build();

        //socket.getOutputStream().write(wrapper.getSerializedSize());
        //wrapper.writeTo(socket.getOutputStream());

        //ret = ClientWrapper.ClientMessage.parseDelimitedFrom(socket.getInputStream()).getResponse().getRep();

        if(!channel.send(wrapper)){
            return false;
        }

        return ret;
    }

    public static boolean joinGroup(String groupName) throws IOException {
        if(channel == null)
            return false;

        boolean ret = true;

        ClientWrapper.JoinGroup request = ClientWrapper.JoinGroup.newBuilder()
                .setGroup(groupName).build();

        ClientWrapper.ClientMessage wrapper = ClientWrapper.ClientMessage.newBuilder()
                .setJoinGroup(request).build();

        //socket.getOutputStream().write(wrapper.getSerializedSize());
        //wrapper.writeTo(socket.getOutputStream());

        //ret = ClientWrapper.ClientMessage.parseDelimitedFrom(socket.getInputStream()).getResponse().getRep();
        if(!channel.send(wrapper)){
            return false;
        }

        return ret;
    }

}
