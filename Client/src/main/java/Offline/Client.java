package Offline;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Scanner;

public class Client {

    public static void main(String[] args) { // main para testar

        Scanner sc = new Scanner(System.in);
        String username;

        System.out.println("username: ");
        username = sc.nextLine();

        findLocalAddresses();



    }

    private static ArrayList<String> findLocalAddresses() {

        ArrayList<String> ret = new ArrayList<String>();

        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                // filters out 127.0.0.1 and inactive interfaces
                if (iface.isLoopback() || !iface.isUp())
                    continue;

                Enumeration<InetAddress> addresses = iface.getInetAddresses();

                while(addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if(Inet6Address.class == addr.getClass() && addr.isLinkLocalAddress()){
                        ret.add(addr.getHostAddress().replaceAll("%.*", "")); // e preciso tirar o %interface
                        System.out.println("local address: " + addr.getHostAddress());
                    }
                }
            }
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }

        return ret;

    }



}
