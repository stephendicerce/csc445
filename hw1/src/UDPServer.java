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
        byte[] byteArray = new byte[firstSize];

        while(running) {
            DatagramPacket packet = new DatagramPacket(byteArray, byteArray.length);

            try {
                socket.receive(packet);
                InetAddress address = packet.getAddress();
                int port = packet.getPort();

                packet = new DatagramPacket(byteArray, byteArray.length, address, port);
                String recieved = new String(packet.getData(), 0, packet.getLength());

                if (recieved.equals("end")) {
                    running = false;
                    continue;
                }
                socket.send(packet);

            } catch (IOException e) {
                System.out.println("IO Exception has occurred.");
            }
        }
        socket.close();
    }
}
