import Handlers.TorrentListenerInitializer;
import Misc.TorrentUtil;
import Offline.Offline;
import com.turn.ttorrent.client.SharedTorrent;
import com.turn.ttorrent.common.Torrent;
import com.turn.ttorrent.tracker.Tracker;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.bitlet.weupnp.GatewayDevice;
import org.bitlet.weupnp.GatewayDiscover;
import org.bitlet.weupnp.PortMappingEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.net.*;
import java.nio.channels.UnsupportedAddressTypeException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Object.class);
    private static int SAMPLE_PORT = 7000;
    private static short WAIT_TIME = 10;
    private static boolean LIST_ALL_MAPPINGS = false;

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, InterruptedException, SAXException, ParserConfigurationException {


            GatewayDiscover gatewayDiscover = new GatewayDiscover();
            Map<InetAddress, GatewayDevice> gateways = gatewayDiscover.discover();

            if (gateways.isEmpty()) {
                System.out.println("No gateways found");
                System.out.println("Stopping weupnp");
                return;
            }
            System.out.println(gateways.size()+" gateway(s) found\n");

            int counter=0;
            for (GatewayDevice gw: gateways.values()) {
                counter++;
                System.out.println("Listing gateway details of device #" + counter+
                        "\n\tFriendly name: " + gw.getFriendlyName()+
                        "\n\tPresentation URL: " + gw.getPresentationURL()+
                        "\n\tModel name: " + gw.getModelName()+
                        "\n\tModel number: " + gw.getModelNumber()+
                        "\n\tLocal interface address: " + gw.getLocalAddress().getHostAddress()+"\n");
            }

            // choose the first active gateway for the tests
            GatewayDevice activeGW = gatewayDiscover.getValidGateway();

            if (null != activeGW) {
                System.out.println("Using gateway: " + activeGW.getFriendlyName());
            } else {
                System.out.println("No active gateway device found");
                System.out.println("Stopping weupnp");
                return;
            }


            // testing PortMappingNumberOfEntries
            Integer portMapCount = activeGW.getPortMappingNumberOfEntries();
            System.out.println("GetPortMappingNumberOfEntries: " + (portMapCount!=null?portMapCount.toString():"(unsupported)"));

            // testing getGenericPortMappingEntry
            PortMappingEntry portMapping = new PortMappingEntry();
            if (LIST_ALL_MAPPINGS) {
                int pmCount = 0;
                do {
                    if (activeGW.getGenericPortMappingEntry(pmCount,portMapping))
                        System.out.println("Portmapping #"+pmCount+" successfully retrieved ("+portMapping.getPortMappingDescription()+":"+portMapping.getExternalPort()+")");
                    else{
                        System.out.println("Portmapping #"+pmCount+" retrieval failed");
                        break;
                    }
                    pmCount++;
                } while (portMapping!=null);
            } else {
                if (activeGW.getGenericPortMappingEntry(0,portMapping))
                    System.out.println("Portmapping #0 successfully retrieved ("+portMapping.getPortMappingDescription()+":"+portMapping.getExternalPort()+")");
                else
                    System.out.println("Portmapping #0 retrival failed");
            }

            InetAddress localAddress = activeGW.getLocalAddress();
            System.out.println("Using local address: "+ localAddress.getHostAddress());
            String externalIPAddress = activeGW.getExternalIPAddress();
            System.out.println("External address: "+ externalIPAddress);

            System.out.println("Querying device to see if a port mapping already exists for port "+ SAMPLE_PORT);

            if (activeGW.getSpecificPortMappingEntry(SAMPLE_PORT,"TCP",portMapping)) {
                System.out.println("Port "+SAMPLE_PORT+" is already mapped. Aborting test.");
                return;
            } else {
                System.out.println("Mapping free. Sending port mapping request for port "+SAMPLE_PORT);

                // test static lease duration mapping
                if (activeGW.addPortMapping(SAMPLE_PORT,SAMPLE_PORT,localAddress.getHostAddress(),"TCP","test")) {
                    System.out.println("Mapping SUCCESSFUL. Waiting "+WAIT_TIME+" seconds before removing mapping...");
                    Thread.sleep(1000*WAIT_TIME);

                   /* if (activeGW.deletePortMapping(SAMPLE_PORT,"TCP")) {
                        System.out.println("Port mapping removed, test SUCCESSFUL");
                    } else {
                        System.out.println("Port mapping removal FAILED");
                    }*/
                }
            }

        Scanner sc = new Scanner(System.in);
        String input;
        String username = "default";
        Torrent t = null;
        ArrayList<Torrent> available = new ArrayList<>();
        Channel ch = null;
        Tracker offlineTck = null;

        System.out.println("Started client");
        System.out.println("Tell me your username: ");
        username = sc.nextLine();
        System.out.println("Online or Offline?");
        String type = sc.nextLine();

        if(type.equals("Offline")) {
            Offline.startProbes(username, available);
        }else {
            ch = startClient(available);
            if (ch == null) {
                System.out.println("\u001B[31mError opening socket\u001B[0m");
                System.exit(2);
            }
        }

        while (!(input = sc.nextLine()).equals("quit")) {
            if (type.equals("Online")) {
                System.out.println("Working online");
                if (input.equals("upload")) {
                    System.out.println("What is the file?");
                    String path = sc.nextLine();
                    ArrayList<String> trc = new ArrayList<String>();
                    //TODO: This IP must be dynamic
                    trc.add("http://178.62.23.209:6969/announce");
                    t = TorrentUtil.createTorrent(path, username, trc);

                    try {
                        TorrentUtil.upload(t, path, ch);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    System.out.println("Upload intention initiated");
                }
            } else {
                System.out.println("Working offline");
                if (input.equals("upload")) {
                    System.out.println("What is the file?");
                    String path = sc.nextLine();
                    ArrayList<String> trc = new ArrayList<String>();
                    trc.add("http://" + Offline.findLocalAddresses().get(0).getIpv4()  + ":6969/announce");
                    //Already has a tracker running?
                    if(offlineTck == null) {
                        String httpAddress = Offline.findLocalAddresses().get(0).getIpv4();
                        offlineTck = new Tracker(new InetSocketAddress(InetAddress.getByName(httpAddress), 6969));
                        offlineTck.start();
                    }

                    t = TorrentUtil.createTorrent(path, username, trc);
                    TorrentUtil.upload(t, path, offlineTck);
                    System.out.println("Upload intention initiated");
                }
            }

            if (input.equals("download")) {
                System.out.println("What's the file number?");
                File dest = new File("/tmp/");
                SharedTorrent st = new SharedTorrent(available.get(Integer.parseInt(sc.nextLine())), dest);
                System.out.println("Downloading to /tmp/");
                //TODO: Keep track of shared torrent to know when they end;
                TorrentUtil.download(getIPv4Address(null).getHostAddress(), st);
            }

            if (input.equals("info")) {
                int n = 0;
                for(Torrent tA : available) {
                    System.out.println("Torrent info " + n++ + ":");
                    System.out.println(tA.getName());
                    System.out.println(tA.getSize());
                    System.out.println(tA.getAnnounceList());
                    System.out.println(tA.getCreatedBy());
                    System.out.println("-----------------------------------");
                }
            }
        }
    }

    private static Channel startClient(ArrayList<Torrent> available) throws SocketException, UnknownHostException {
        EventLoopGroup group = new NioEventLoopGroup();
        Channel ch = null;
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                .channel(NioSocketChannel.class)
                .handler(new TorrentListenerInitializer(available));
                // Make a new connection.
                ch = b.connect("178.62.23.209", 5000).sync().channel();
                // Get the handler instance to initiate the request.
                //TorrentClientHandler handler = ch.pipeline().get(TorrentClientHandler.class);
                // Request and get the response.
                //List<String> response = handler.getLocalTimes(CITIES);
                // Close the connection.
                //sch.close();
        } catch (Exception e) {
            e.printStackTrace();
            return ch;
        } finally {
            //TODO: fechar grupo quando queremos fechar cliente
            //group.shutdownGracefully();
        }
        return ch;
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
}
