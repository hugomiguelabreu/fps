package Offline.Utils;

public class LocalAddresses {

    private String ipv6;
    private String ipv4;

    public LocalAddresses(String ipv6, String ipv4) {
        this.ipv6 = ipv6;
        this.ipv4 = ipv4;
    }

    public String getIpv6() {
        return ipv6;
    }

    public String getIpv4() {
        return ipv4;
    }
}
