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
                startTime = System.nanoTime();
                socket.send(sendPacket);
                DatagramPacket receivePacket = new DatagramPacket(receivedBytes,receivedBytes.length);
                socket.receive(receivePacket);
                elapsedTime = System.nanoTime()-startTime;

                if(i==0)
                    System.out.println("The amount of time to send " + firstSize + " packet(s) is: " + elapsedTime);
                else if(i==1)
                    System.out.println("The amount of time to send " + secondSize + " packet(s) is: " + elapsedTime);
                else
                    System.out.println("The amount of time to send " + thirdSize + " packet(s) is: " + elapsedTime);

                Thread.currentThread().sleep(5000);
            }
        } catch(IOException e) {
            System.out.println("IOException has occurred");
        } catch (InterruptedException e) {
            System.out.println("Thread was interrupted");
        }

    }

    public void interactionForMBye(int firstSize, int firstNumber, int secondSize, int secondNumber, int thirdSize, int thirdNumber) {
        int messageSize;
        int numberOfMessages;
        byte[] bytes = new byte[1024000];
        byte[] receivedBytes = new byte[1024000];
        Random random = new Random();
        byte[] ackByte = new byte[1];

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

                for (int j = 0, length = bytes.length; j < length; ++j) {
                    int randomInt = random.nextInt(1);
                    if (randomInt == 0)
                        bytes[j] = 0;
                    else if (randomInt == 1)
                        bytes[j] = 1;
                    else {
                        System.out.println("what happened");
                        bytes[j] = 1;
                    }
                }

                int whereToStart = 0;
                long startTime = System.nanoTime();
                for (int j = 0; j < numberOfMessages; ++j) {
                    DatagramPacket packet = new DatagramPacket(bytes, whereToStart, messageSize, address, port);
                    socket.send(packet);
                    whereToStart += messageSize;
                    //System.out.println(j);

                    DatagramPacket ack = new DatagramPacket(ackByte, ackByte.length);
                    socket.receive(ack);
                }

                DatagramPacket ack = new DatagramPacket(ackByte, ackByte.length);
                socket.receive(ack);

                /*whereToStart = 0;
                for (int j=0; j<numberOfMessages; ++j) {
                    DatagramPacket receivedPacket = new DatagramPacket(receivedBytes, whereToStart, messageSize);
                    socket.receive(receivedPacket);
                    whereToStart += messageSize;

                    DatagramPacket ack = new DatagramPacket(ackByte, ackByte.length, address, port);
                    socket.send(ack);
                }
                 */
                long elapsedTime = System.nanoTime()-startTime;
                System.out.println("elapsed time for " + numberOfMessages + " messages that are " + messageSize + "bytes long: " + elapsedTime);
            }
        } catch(IOException e) {
            System.out.println("IO Exception occurred.");
        }
    }
}
