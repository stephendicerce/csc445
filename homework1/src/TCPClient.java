import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.TimeUnit;

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

    }

    public void measureTransferRates(int firstSize, int secondSize, int thirdSize, int fourthSize, int fifthSize) {
        long startTime;
        long endTime;

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

        try{
            for(int i=0; i<5; ++i) {
                byte[] bytes;
                byte[] receivedBytes;
                switch (i) {
                    case 0: bytes = new byte[firstSize];
                        receivedBytes = new byte[firstSize];
                        //System.out.println("working on " + firstSize + "bytes.");
                        break;
                    case 1: bytes = new byte[secondSize];
                        receivedBytes = new byte[secondSize];
                        //System.out.println("working on " + secondSize + "bytes.");
                        break;
                    case 2: bytes = new byte[thirdSize];
                        receivedBytes = new byte[thirdSize];
                        //System.out.println("working on " + thirdSize + "bytes.");
                        break;
                    case 3: bytes = new byte[fourthSize];
                        receivedBytes = new byte[fourthSize];
                        //System.out.println("working on " + fourthSize + "bytes.");
                        break;
                    default: bytes = new byte[fifthSize];
                        receivedBytes = new byte[fifthSize];
                        //System.out.println("working on " + fifthSize + "bytes.");
                        break;
                }

                for(int j=0, length = bytes.length; j<length; ++j) {
                    Random r = new Random();
                    int randomInt = r.nextInt(1);
                    if(randomInt == 0) {
                        bytes[j] = 0;
                    } else {
                        bytes[j] = 1;
                    }
                }
                out.write(bytes, 0, bytes.length);
                //System.out.println(bytes.length + "written to server.");
                startTime = System.nanoTime();
                //System.out.println("sending time for packet " + count + " for " + i + ": " + startTime + ".");
                int remainingBytesToTransfer = bytes.length;
                int totalAmountPossibleToReceive;
                if(i==4) {
                     totalAmountPossibleToReceive = 91392;
                } else {
                     totalAmountPossibleToReceive = 43520;
                }
                //System.out.println("Amount of bytes that can be transferred: " + totalAmountPossibleToReceive);
                int bytesReceived = 0;
                boolean finished = false;

                while (!finished) {
                    int bytesTransferred;
                    if(remainingBytesToTransfer - totalAmountPossibleToReceive < 0) {
                        bytesTransferred = remainingBytesToTransfer;
                        finished = true;
                    } else {
                        bytesTransferred = totalAmountPossibleToReceive;
                    }
                    remainingBytesToTransfer -= totalAmountPossibleToReceive;
                    if(in.read(receivedBytes, bytesReceived ,bytesTransferred) != -1) {
                        endTime = System.nanoTime();
                        bytesReceived += bytesTransferred;
                        //System.out.println("The time to transfer " + bytesTransferred + " bytes from the server is: " + (endTime - startTime) + ".");
                        double elapsedTime = (endTime - startTime) / 1000000000.0;
                        double throughput = bytesTransferred / elapsedTime;

                        System.out.println("Throughput: " + throughput);
                        //Thread.currentThread().sleep(1000);
                        //out.writeBoolean(true); //acknowledgement byte
                       // System.out.println("Sent ack");
                    } else {
                        System.out.println("spinning...");
                    }
                }

                //System.out.println("Finished " + i + ".");
                Thread.currentThread().sleep(1000);
            }
        } catch(IOException e) {
            System.out.println("IO Exception");
        } catch(InterruptedException e) {
            System.out.println("Thread Interrupted");
        }



    }


    public void interactionForMByte(int firstSize, int firstNumber, int secondSize, int secondNumber, int thirdSize, int thirdNumber){
        byte[] bytes = new byte[1024000];
        int messageSize;
        int numberOfMessages;

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

                for(int j=0; j<bytes.length; ++j) {
                    Random r = new Random();
                    int randomInt = r.nextInt(1);
                    if(randomInt==0) {
                        bytes[j] = 0;
                    } else {
                        bytes[j] = 1;
                    }
                }

                int messageStart = 0;
                int messageEnd = 0;
                long startTime = System.nanoTime();
                long elapsedTime = 0;
                for(int k=0; k<numberOfMessages; ++k) {
                    messageEnd = messageStart + messageSize;
                    out.write(bytes, messageStart, messageSize);
                    messageStart = messageEnd;
                    //System.out.println("before boolean");
                    in.readBoolean();
                    System.out.println(k);
                }

                //System.out.println("Finished sending messages for " + i + ".");
                boolean finished = false;

                messageStart = 0;
                messageEnd = 0;
                int count = 0;
                byte[] receivedBytes = new byte[bytes.length];
                while(!finished) {
                    messageEnd = messageStart + messageSize;
                    if(in.read(receivedBytes, messageStart, messageSize) != -1) {
                        ++count;
                        //System.out.println(count + " messages received.");
                        out.writeBoolean(true);
                    } else {
                        System.out.println("...........");
                    }
                    if(count>=numberOfMessages) {
                        finished = true;
                        elapsedTime = System.nanoTime()-startTime;
                    }

                }
                out.writeBoolean(true);
                System.out.println("elapsed time for " + numberOfMessages + " messages that are " + messageSize + "bytes long: " + elapsedTime);

            } catch(IOException e) {
                System.out.println("IO Exception has occurred.");
            }
        }
    }

    public boolean close() {
        if(in != null && out != null) {
            try {
                in.close();
                out.close();
                socket.close();
                return true;
            } catch (IOException e) {
                System.out.println("IOException");
            } catch (NullPointerException e) {
                System.out.println("Null pointer Exception");
            }
        }
        return false;
    }

}
