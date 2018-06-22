package Util;

import Core.Connector;
import Event.ConcurrentHashMapEvent;
import Network.ClientWrapper;
import com.turn.ttorrent.client.Client;
import com.turn.ttorrent.common.Torrent;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class ServerOperations {

    private static Connector channel;
    private static ConcurrentHashMapEvent<String, ArrayList<Torrent>> groupTorrents;
    private static ConcurrentHashMapEvent<String, ArrayList<String>> groupUsers;
    public static String username;
    private static ArrayList<Client> activeClients = new ArrayList<>();
    private static ArrayList<String> trackersOnline = new ArrayList<>();

    public static void setGroupTorrents(ConcurrentHashMapEvent<String, ArrayList<Torrent>> groupTorrents) { ServerOperations.groupTorrents = groupTorrents; }

    public static void setGroupUsers(ConcurrentHashMapEvent<String, ArrayList<String>> groupUsers) { ServerOperations.groupUsers = groupUsers; }

    public static void setTrackersOnline(ArrayList<String> trackersParam){ ServerOperations.trackersOnline = trackersParam; }

    public static void setChannel(Connector channel){
        ServerOperations.channel = channel;
    }

    public static void sendTorrent(String path, String group){

        Torrent t;
        try {
            t = TorrentUtil.createTorrent(path, username, trackersOnline);
            Client c = TorrentUtil.upload(t, path, channel, username, group);
            if(c!=null)
                activeClients.add(c);

        } catch (IOException | InterruptedException | ParserConfigurationException | SAXException | NoSuchAlgorithmException e) {
            System.out.println("Couldn't bind, fallback to local");
            e.printStackTrace();
        }
    }

    public static void addTorrent(Torrent t, String group) throws InterruptedException, NoSuchAlgorithmException, IOException {
        //Temos de colocar o tracker do torrent o nosso primário
        int iteration = 0;
        URI uri = null;
        for(URI uriIt: t.getAnnounceList().get(0)){
            //Verificar qual deles é o nosso primario
            if(uriIt.toString().equals(trackersOnline.get(0))){
                uri = uriIt;
                break;
            }
            iteration++;
        }
        //Remover o nosso primario do indice onde esta e coloca-lo em primeiro
        t.getAnnounceList().get(0).remove(iteration);
        t.getAnnounceList().get(0).add(0, uri);
        //Volta a codificar novamente o torrent
        t.newAnnounceEncode();
        //Guarda o torrent
        FileUtils.addTorrent(t, group);
        ArrayList<Torrent> gt = groupTorrents.get(group);
        gt.add(0, t);
        groupTorrents.putTorrent(group, gt);
    }

    public static void updateUsers(ArrayList<String> users, String group){

    }

    public static boolean login(String usernameLogin, String password) throws IOException {
        if(channel == null)
            return false;
        boolean ret;

        ClientWrapper.Login request = ClientWrapper.Login.newBuilder()
                .setUsername(usernameLogin)
                .setPassword(password).build();
        ClientWrapper.ClientMessage wrapper = ClientWrapper.ClientMessage.newBuilder()
                .setLogin(request).build();

        if(!channel.send(wrapper))
            return false;

        try {
            ret = channel.readResponse();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }

        if(ret)
            username = usernameLogin;

        return ret;
    }

    public static boolean register(String username, String password, String name) {
        if(channel == null)
            return false;
        boolean ret;

        ClientWrapper.Register request = ClientWrapper.Register.newBuilder()
                .setUsername(username)
                .setPassword(password)
                .setName(name).build();
        ClientWrapper.ClientMessage wrapper = ClientWrapper.ClientMessage.newBuilder()
                .setRegister(request).build();

        if(!channel.send(wrapper))
            return false;

        try {
            ret = channel.readResponse();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }

        return ret;
    }

    public static boolean createGroup(String groupName) {
        if(channel == null)
            return false;

        boolean ret;

        ClientWrapper.CreateGroup request = ClientWrapper.CreateGroup.newBuilder()
                .setGroup(groupName).build();

        ClientWrapper.ClientMessage wrapper = ClientWrapper.ClientMessage.newBuilder()
                .setCreateGroup(request).build();

        if(!channel.send(wrapper))
            return false;

        try {
            ret = channel.readResponse();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }

        if(ret){
            groupTorrents.put(groupName, new ArrayList<>());
            groupUsers.put(groupName, new ArrayList<>());
        }

        return ret;
    }

    public static boolean joinGroup(String groupName) {
        if(channel == null)
            return false;

        boolean ret;

        ClientWrapper.JoinGroup request = ClientWrapper.JoinGroup.newBuilder()
                .setGroup(groupName).build();

        ClientWrapper.ClientMessage wrapper = ClientWrapper.ClientMessage.newBuilder()
                .setJoinGroup(request).build();

        if(!channel.send(wrapper))
            return false;

        try {
            ret = channel.readResponse();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }

        if(ret){
            groupTorrents.put(groupName, new ArrayList<>());
            groupUsers.put(groupName, new ArrayList<>());
        }

        return ret;
    }

}
