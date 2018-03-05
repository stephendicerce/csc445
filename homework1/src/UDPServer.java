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
            running = true;
            if(i==0) {
               byteArray = new byte[firstSize];
               received = new byte[firstSize];
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

                    int count = 1;
                    System.out.println("Packets received: " + count);
                    ++count;

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

    public void interactionForMBye(int firstSize, int firstNumber, int secondSize, int secondNumber, int thirdSize, int thirdNumber) {
        int messageSize;
        int numberOfMessages;
        byte[] bytes = new byte[1024000];
        byte[] receivedBytes = new byte[1024000];
        byte[] ackByte = new byte[1];
        running = true;

        try {
            for (int i = 0; i < 3; ++i) {
                switch (i) {
                    case 0:
                        messageSize = firstSize;
                        numberOfMessages = firstNumber;
                        break;
                    case 1:
                        messageSize = secondSize;
                        numberOfMessages = secondNumber;
                        break;
                    default:
                        messageSize = thirdSize;
                        numberOfMessages = thirdNumber;
                }
                InetAddress address = null;
                int sendToPort = 0;
                int whereToStart = 0;
                for (int j = 0; j < numberOfMessages; ++j) {
                    DatagramPacket receivedPacket = new DatagramPacket(receivedBytes, whereToStart, messageSize);
                    socket.receive(receivedPacket);
                    whereToStart += messageSize;
                    address = receivedPacket.getAddress();
                    sendToPort = receivedPacket.getPort();


                }
                DatagramPacket ack = new DatagramPacket(ackByte, ackByte.length, address, sendToPort);
                socket.send(ack);

                /*
                whereToStart = 0;
                for (int j = 0; j < numberOfMessages; ++j) {
                    System.out.println("sending message " + j);
                    DatagramPacket packet = new DatagramPacket(bytes, whereToStart, messageSize, address, sendToPort);
                    socket.send(packet);
                    whereToStart += messageSize;

                    DatagramPacket ack = new DatagramPacket(ackByte, ackByte.length);
                    socket.receive(ack);
                }
                 **/



            }
        } catch(IOException e) {
            System.out.println("IO Exception occurred.");
        }
    }
}
