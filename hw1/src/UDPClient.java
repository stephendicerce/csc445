import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPClient {
    private DatagramSocket socket;
    private InetAddress address;
    String hostname;
    int port;

    UDPClient(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    public boolean openSocket() {
        try {
            socket = new DatagramSocket();
            address = InetAddress.getByName(hostname);
            return true;
        } catch(IOException e) {
            return false;
        }
    }

    public void echo(int firstSize, int secondSize, int thirdSize) {
        long startTime;
        long elapsedTime;
        byte[] byteArray = new byte[firstSize];

        for(int i=0;)

    }
}
