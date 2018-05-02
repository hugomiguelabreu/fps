package Core;

import com.turn.ttorrent.client.SharedTorrent;
import com.turn.ttorrent.common.Torrent;
import com.turn.ttorrent.client.Client;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.net.*;
import java.nio.channels.UnsupportedAddressTypeException;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;

/**
 * Client class for the tracker server.
 * This class handles the files that goes through the server
 */
public class ServerClient extends Thread{

    private Torrent t;
    private SharedTorrent st;
    private Client c;

    public ServerClient(Torrent tParam){
        this.t = tParam;
    }

    private static Inet4Address getIPv4Address(String iface)
            throws SocketException, UnsupportedAddressTypeException,
            UnknownHostException {
        if (iface != null) {
            Enumeration<InetAddress> addresses =
                    NetworkInterface.getByName(iface).getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress addr = addresses.nextElement();
                if (addr instanceof Inet4Address) {
                    return (Inet4Address)addr;
                }
            }
        }

        InetAddress localhost = InetAddress.getLocalHost();
        if (localhost instanceof Inet4Address) {
            return (Inet4Address)localhost;
        }

        throw new UnsupportedAddressTypeException();
    }

    public void run(){

        File dest = new File("/tmp/");

        try {
            st = new SharedTorrent(t, dest);
            c = new Client(Inet4Address.getByName("0.0.0.0"), st);
        } catch (IOException | NoSuchAlgorithmException | ParserConfigurationException | InterruptedException | SAXException e) {
            e.printStackTrace();
        }
        c.setMaxDownloadRate(0.0);
        c.setMaxUploadRate(0.0);

        c.share(-1);

        if (com.turn.ttorrent.client.Client.ClientState.ERROR.equals(c.getState())) {
            System.exit(1);
        }

    }

}
