import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPServer {

    private ServerSocket serverSocket;
    private Socket socket;
    private int port;

    TCPServer(int port){
        this.port = port;
    }

    /**
     * A method to open the sockets pertaining to the server
     * @return true if both sockets were opened successfully, false if one or both
     * sockets didn't successfully open.
     */
    public boolean openSockets() {
        try {
            serverSocket = new ServerSocket(port);
            return openSocket();
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * A private method to open a socket using the newly created server socket.
     * @return true if the socket was successfully opened, false if the socket
     * didn't successfully open.
     */
    private boolean openSocket() {
        try {
            socket = serverSocket.accept();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Listens for incoming connections and echos them back to the client
     * @param firstSize - the first size the byte array will be set to
     * @param secondSize - the second size the byte array will be set to
     */
    public void listening(int firstSize, int secondSize) {
        DataInputStream in;
        DataOutputStream out;
        byte[] byteArray = new byte[firstSize];
        boolean booleanValue = false;

        try {
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            while(!booleanValue) { // to echo a 1 byte message using read and write boolean which sends a single boolean as a byte according to documentation
                booleanValue = in.readBoolean();
                out.writeBoolean(booleanValue);
            }
            // Echos a 64 byte message using read and write,
            while(byteArray[byteArray.length-1] == 0) { // This line serves as a check to make sure the entire array was received. The client will always set the last value of the array to 1 before sending
                if(in.read(byteArray, 0, firstSize) != -1) { // Reads a byte array in with an offset set to 0 and length set to the first size according to the user
                    System.out.println("receiving 64 bytes");
                }
                System.out.println(".................");
            }

            out.write(byteArray,0,firstSize); // echos the byte array received back to the client

            // Echos a 1024 byte message using read and write
            byteArray = new byte[secondSize];
            while(byteArray[byteArray.length-1] == 0) { // This line serves as a check to make sure the entire array was received. The client will always set the last value of the array to 1 before sending
                if(in.read(byteArray,0,secondSize)!= -1) { // Reads a byte array in with an offset set to 0 and length set to the first size according to the user
                    System.out.println("receiving 1024 bytes");
                }
                System.out.println(".................");
            }

            out.write(byteArray,0,secondSize); // echos the byte array received back to the client

        } catch(IOException e) {
            System.out.println("Could not get Input Stream");
        }
    }

    public void measureTransferRates(int firstSize, int secondSize, int thirdSize, int fourthSize, int fifthSize) {
        DataInputStream in = null;
        DataOutputStream out = null;
        byte[] bytes;
        long receiveTime;
        long sendTime;

        try {
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            for (int i = 0; i < 5; ++i) {
                switch (i) {
                    case 0:
                        bytes = new byte[firstSize];
                        break;
                    case 1:
                        bytes = new byte[secondSize];
                        break;
                    case 2:
                        bytes = new byte[thirdSize];
                        break;
                    case 3:
                        bytes = new byte[fourthSize];
                        break;
                    default:
                        bytes = new byte[fifthSize];
                        break;
                }


                    in.readFully(bytes);
                    System.out.println("receiving " + bytes.length + " bytes..");


                //System.out.println("defining variables");
                int bytesSent = 0;
                int amountOfBytesLeftToSend = bytes.length;
                int totalAmountPossibleToSend = socket.getSendBufferSize();
                System.out.println("Total amount of bytes that can be sent: " + totalAmountPossibleToSend);
                int amountOfBytesToSend;
                boolean finished = false;
                //System.out.println("done defining variables");

                while(!finished) {
                    //System.out.println("in while loop");
                    if(amountOfBytesLeftToSend-totalAmountPossibleToSend<0) {
                        amountOfBytesToSend = amountOfBytesLeftToSend;

                        finished = true;
                    } else {
                        amountOfBytesToSend = totalAmountPossibleToSend;
                        amountOfBytesLeftToSend -= totalAmountPossibleToSend;
                    }
                    //System.out.println("about to write");

                    out.write(bytes, bytesSent, amountOfBytesToSend);
                    bytesSent = amountOfBytesToSend;
                    System.out.println("Sent back " + bytesSent + " to the client.");

                    //in.readBoolean();
                    //System.out.println("received ack");
                }
                System.out.println("Finished " + i + ".");
            }
        } catch(IOException e) {
            System.out.println("IO Exception has occurred.");
        }
        if(in != null && out != null) {
            try {
                in.close();
                out.close();
                socket.close();
                serverSocket.close();
            } catch (IOException e) {
                System.out.println("IOException");
            } catch (NullPointerException e) {
                System.out.println("Null pointer Exception");
            }
        }
    }

    public void interactionForMByte(int firstSize, int firstNumber, int secondSize, int secondNumber, int thirdSize, int thirdNumber){
        byte[] bytes = new byte[1000000];
        int messageSize;
        int numberOfMessages;
        DataInputStream in;
        DataOutputStream out;

        for(int i=0; i<3; ++i) {
            switch(i) {
                case 0: messageSize = firstSize;
                    numberOfMessages = firstNumber;
                    break;
                case 1: messageSize = secondSize;
                    numberOfMessages = secondNumber;
                    break;
                default:messageSize = thirdSize;
                    numberOfMessages = thirdNumber;
            }

            try {
                in = new DataInputStream(socket.getInputStream());
                out = new DataOutputStream(socket.getOutputStream());

                boolean finished = false;
                int messageStart = 0;
                int messageEnd = 0;
                int count = 0;

                while(!finished) {
                    if(messageStart + messageSize>bytes.length)
                        messageEnd = bytes.length;
                    else
                        messageEnd = messageStart + messageSize;
                    if(in.read(bytes, messageStart, messageEnd) != -1) {
                        ++count;
                        messageStart = messageEnd;
                        System.out.println(count + " messages received from client.");
                        out.writeBoolean(true);
                    } else {
                        System.out.println("...........");
                    }
                    if(count>=numberOfMessages) {
                        finished = true;
                    }

                }
                out.writeBoolean(true);
                System.out.println("finished receiving messages for " + i +".");
                int start = 0;
                int end = 0;
                long startTime = System.nanoTime();
                long elapsedTime = 0;
                for(int j=0; j<numberOfMessages; ++j) {
                    end = start + messageSize;
                    out.write(bytes, start, end);
                    start = messageEnd + 1;

                    in.readBoolean();
                }

            } catch(IOException e) {
                System.out.println("IO Exception has occurred.");
            }
        }
    }

}
