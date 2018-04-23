package Offline.Utils;

import java.sql.Timestamp;

public class Peer {

    private String username;
    private String ipv6;
    private String ipv4;
    private Timestamp last;

    public Peer(String username, String ipv6, String ipv4, Timestamp last) {
        this.username = username;
        this.ipv6 = ipv6;
        this.ipv4 = ipv4;
        this.last = last;
    }

//    public Peer(String username, String ip, Timestamp last) {
//        this.username = username;
//        this.ip = ip;
//        this.last = last;
//    }

    public String getUsername() {
        return username;
    }

    public String getIpv6() {
        return ipv6;
    }

    public String getIpv4() {
        return ipv4;
    }

    public Timestamp getLast() {
        return last;
    }

    public void setLast(Timestamp last) {
        this.last = last;
    }
}
