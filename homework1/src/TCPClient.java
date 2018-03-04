import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Random;

public class TCPClient {

    private Socket socket;
    private String hostName;
    private int port;
    private DataInputStream in = null;
    private DataOutputStream out = null;

    TCPClient(String hostName, int port) {
        this.hostName = hostName;
        this.port = port;
    }

    /**
     * A method to open a socket using the values from the constructor
     * @return true if the socket was successfully opened, false if it wasn't
     */
    public boolean openSocket() {
        try {
            socket = new Socket(hostName, port);
            return true;
        } catch (IOException e){
            System.out.println("IO Exception");
            return false;
        }
    }

    public void echo() {
        long startTime;
        long elapsedTime;
        boolean recievedValue = false;

        try {
            in = new DataInputStream(socket.getInputStream());
        } catch(IOException e) {
            System.out.println("Could not get the DataInputStream from the socket.");
        }
        try {
            out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            System.out.println("Couldn't get DataOutputStream from the socket.");
        }



        try {
            out.writeBoolean(true);
            startTime = System.nanoTime();
            Random r = new Random();
            int size;

            while(!recievedValue){
                recievedValue = in.readBoolean();
            }
            elapsedTime = System.nanoTime() - startTime;

            System.out.println("The amount of elapsed time for 1 byte is: " + elapsedTime);

            for(int k=0; k<2; ++k) {
                if(k==0)
                    size = 64;
                else
                    size = 1024;

                byte[] sendingArray;
                byte[] receivedArray;
                sendingArray = new byte[size];
                receivedArray = new byte[size];
                receivedArray[receivedArray.length-1] = 0;

                for (int i = 0, length = sendingArray.length; i < length; i = i + 2) {
                    int randomInt = r.nextInt(1);
                    if (randomInt == 0)
                        sendingArray[i] = 0;
                    if (randomInt == 1)
                        sendingArray[i] = 1;
                    randomInt = r.nextInt(1);
                    if (randomInt == 0)
                        sendingArray[i + 1] = 0;
                    if (randomInt == 1)
                        sendingArray[i + 1] = 1;
                }
                sendingArray[sendingArray.length-1] = 1;
                if(k==0) {
                    out.write(sendingArray, 0, 64);
                } else {
                    out.write(sendingArray,0, 1024);
                }
                startTime = System.nanoTime();


                while (receivedArray[receivedArray.length-1] == 0) {
                    if (in.read(receivedArray, 0, size) != -1) {
                        elapsedTime = System.nanoTime() - startTime;
                        if(k==0)
                            System.out.println("The amount of elapsed time for 64 bytes is: " + elapsedTime);
                        else
                            System.out.println("The amount of elapsed time for 1024 bytes is: " + elapsedTime);
                    }
                }

                Thread.currentThread().sleep(1000);
            }


        } catch(IOException e) {
            System.out.println("Caught an IO Exception.");
        } catch(InterruptedException e) {
            System.out.println("Thread interrupted.");
        }

        if(in != null && out != null) {
            try {
                in.close();
                out.close();
                socket.close();
            } catch (IOException e) {
                System.out.println("IOException");
            } catch (NullPointerException e) {
                System.out.println("Null pointer Exception");
            }
        }
    }

}
