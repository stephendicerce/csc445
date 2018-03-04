import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Random;

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
        byte[] byteArray;
        byte[] receivedBytes;
        Random r = new Random();

        try {
            for (int i = 0; i < 3; ++i) {
                switch (i) {
                    case 0:
                        byteArray = new byte[firstSize];
                        receivedBytes = new byte[firstSize];
                        break;
                    case 1:
                        byteArray = new byte[secondSize];
                        receivedBytes = new byte[secondSize];
                        break;
                    default:
                        byteArray = new byte[thirdSize];
                        receivedBytes = new byte[thirdSize];
                        break;
                }

                for (int j = 0, length = byteArray.length; j < length; ++j) {
                    int randomInt = r.nextInt(1);
                    if (randomInt == 0)
                        byteArray[j] = 0;
                    if (randomInt == 1)
                        byteArray[j] = 1;
                }
                byteArray[byteArray.length - 1] = 1;

                DatagramPacket sendPacket = new DatagramPacket(byteArray, byteArray.length, address, port);
                socket.send(sendPacket);
                startTime = System.nanoTime();
                DatagramPacket receivePacket = new DatagramPacket(receivedBytes,receivedBytes.length);
                socket.receive(receivePacket);
                elapsedTime = System.nanoTime()-startTime;

                if(i==0)
                    System.out.println("The amount of time to send " + firstSize + " packet(s) is: " + elapsedTime);
                else if(i==1)
                    System.out.println("The amount of time to send " + secondSize + " packet(s) is: " + elapsedTime);
                else
                    System.out.println("The amount of time to send " + thirdSize + " packet(s) is: " + elapsedTime);
            }
        } catch(IOException e) {
            System.out.println("IOException has occurred");
        }

    }
}
