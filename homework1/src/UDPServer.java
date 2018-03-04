import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPServer {

    private int port;
    private DatagramSocket socket;
    private boolean running;

    UDPServer(int port) {
        this.port = port;
    }

    public boolean openSocket() {
        try {
            socket = new DatagramSocket(port);
            return true;
        } catch(IOException e) {
            return false;
        }
    }

    public void listening(int firstSize, int secondSize, int thirdSize) {
        running = true;
        byte[] byteArray;
        byte[] received;

        for(int i=0; i<3; ++i) {
            if(i==0) {
               byteArray = new byte[secondSize];
               received = new byte[secondSize];
            } else if(i==1) {
                byteArray = new byte[secondSize];
                received = new byte[secondSize];
            } else {
                byteArray = new byte[thirdSize];
                received = new byte[thirdSize];
            }
            while (running) {
                DatagramPacket packet = new DatagramPacket(byteArray, byteArray.length);

                try {
                    socket.receive(packet);
                    InetAddress address = packet.getAddress();
                    int port = packet.getPort();


                    received[received.length - 1] = 0;
                    packet = new DatagramPacket(byteArray, byteArray.length, address, port);
                    received = packet.getData();


                    if (received[received.length - 1] == 1) {
                        System.out.println("Whole message received.");
                        running = false;
                    }
                    socket.send(packet);

                } catch (IOException e) {
                    System.out.println("IO Exception has occurred.");
                }
            }
            System.out.println(i);
        }
        socket.close();
    }
}
