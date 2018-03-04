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

    public void measureTransferRates(int firstsize, int secondSize, int thirdSize, int fourthSize, int fifthSize) {
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
                        bytes = new byte[firstsize];
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

                while (bytes[bytes.length - 1] == 0) { // This line serves as a check to make sure the entire array was received. The client will always set the last value of the array to 1 before sending
                    if (in.read(bytes, 0, bytes.length) != -1) { // Reads a byte array in with an offset set to 0 and length set to the first size according to the user
                        System.out.println("receiving " + bytes.length + " bytes..");
                        receiveTime = System.nanoTime();
                        System.out.println("Received a message of " + bytes.length + "at " + receiveTime);
                    }
                    System.out.println(".................");
                }

                out.write(bytes, 0, bytes.length);
                sendTime = System.nanoTime();
                System.out.println("Sent back " + bytes.length + "to the client at " +System.nanoTime());
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



}
