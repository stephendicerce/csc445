import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;

public class Client {
    private DatagramSocket socket;
    private InetAddress address;
    private String hostname;
    private int port;
    private boolean datagramType; //false for IPv4, true for IPv6
    private boolean slidingWindows; //false for sequential acks, true for sliding windows
    private boolean dropPacketSimulation; // false for no dropped packets, true to simulate dropping 1% of the packets received

    Client(String hostname, int port, boolean datagramType, boolean slidingWindows, boolean dropPacketSimulation) {
        this.hostname = hostname;
        this.port = port;
        this.datagramType = datagramType;
        this.slidingWindows = slidingWindows;
        this.dropPacketSimulation = dropPacketSimulation;
    }

    public boolean openSocket() {
        try {
            if(!datagramType) {
                address = Inet4Address.getByName(hostname);
            } else {
                address = Inet6Address.getByName(hostname);
            }
            socket = new DatagramSocket(port, address);
            socket.setSoTimeout(3000);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public void closeSocket() {
        socket.close();
    }

    /**
     * A method that requests a URL from the Server, and receives the response from the server.
     * The response includes the first JPEG from that particular URL.
     * @param url the url that the user is requesting the JPEG from
     * @return true if the transfer was successful, false if the transfer was unsuccessful
     */
    public boolean requestURL(String url) {
        boolean received = false; //used to tell if the transfer was completed
        boolean receivedNumberOfImages = false; //used to tell if the client received the number of images that the server is going to send
        boolean  receivedImage; //used to tell if the client successfully received the image sent by the server
        byte[] receiveBytes = new byte[512]; //byte array for the incoming packets
        byte[] cachedBytes = new byte[512];
        byte[] urlBytes = url.getBytes(); //the user requested url translated into a byte array for transfer.
        byte[] acknowledgementBytes; //a byte array containing bytes representing the last packet received by the client
        int packetNumber = 0;
        int numberOfImages = 0;
        String numberOfImagesString;
        String packetNumberString;
        DatagramPacket urlPacket;
        DatagramPacket receivePacket;
        DatagramPacket acknowledgementPacket;
        DatagramPacket numberOfImagesPacket;
        File pageDataFile = new File("page_data.txt");
        File imageFile;
        BufferedOutputStream outputStream;
        BufferedOutputStream imageStream;

        //creates a bufferedOutputStream to write the incoming bytes to a file
        try{
            outputStream = new BufferedOutputStream(new FileOutputStream(pageDataFile));
        } catch(FileNotFoundException e) { //if the file cannot be found return false and notify the user
            System.out.println("File: \"page_data.txt\" was not found.");
            return false;
        }

        //send the url server for processing and requesting the page data
        try {
            if (datagramType) { // using IPv4 packets
                urlPacket = new DatagramPacket(urlBytes, urlBytes.length, address, port);
            } else { // using IPv6 packets
                urlPacket = new DatagramPacket(urlBytes, urlBytes.length, address, port);
            }
            socket.send(urlPacket); //sends the packet to the server

        } catch(IOException e) { //finds all IO errors with the socket and notifies the user that an error occurred. returns false because the transfer was unsuccessful.
            System.out.println("IO Exception while requesting URL.");
            return false;
        }
        //receiving the page data
        while(!received) {
            try {
                receivePacket = new DatagramPacket(receiveBytes, receiveBytes.length);
                socket.receive(receivePacket); //packet n
                outputStream.write(receivePacket.getData());
                outputStream.flush();

                //determining if the server timed out and is sending the last packet sent
                if(receivePacket.getData() != cachedBytes) { //The server is sending a new packet
                    packetNumberString = Integer.toString(packetNumber); //packetNumber n
                    acknowledgementBytes = packetNumberString.getBytes();
                    cachedBytes = receivePacket.getData();
                    acknowledgementPacket = new DatagramPacket(acknowledgementBytes, acknowledgementBytes.length);
                    socket.send(acknowledgementPacket);
                    ++packetNumber; //packetNumber n+1
                } else { //The server is has timed out and resent the last packet
                    --packetNumber;
                    packetNumberString = Integer.toString(packetNumber);
                    acknowledgementBytes = packetNumberString.getBytes();
                    acknowledgementPacket = new DatagramPacket(acknowledgementBytes, acknowledgementBytes.length);
                    socket.send(acknowledgementPacket);
                    ++packetNumber;
                }

                if (receivePacket.getLength() < 512) //Will only happen on the last packet
                    received = true;
            } catch(SocketTimeoutException e) { //if no response is received from the server, ask for the same packet again
                System.out.println("Client timeout: Asking server for resend.");
                if(packetNumber < 0) {
                    packetNumber = -1;
                } else {
                    --packetNumber;
                }
                try {
                    packetNumberString = Integer.toString(packetNumber); //packetNumber n-1
                    acknowledgementBytes = packetNumberString.getBytes();
                    acknowledgementPacket = new DatagramPacket(acknowledgementBytes, acknowledgementBytes.length);
                    socket.send(acknowledgementPacket);
                    ++packetNumber;
                }catch(IOException io) {
                    System.out.println("IO Exception during timeout.");
                    return false;
                }
            }catch (IOException e) {
                System.out.println("IO Exception while requesting url.");
                return false;
            }
        }

        //Receiving the number of images to be sent from the server. The server will inform the client how many images
        //to expect before actually sending the images to ensure that the client doesn't stop listening for packets
        //before the server is finished. The client will respond with the same number that the server sent.
        while(!receivedNumberOfImages) {
            try {
                numberOfImagesPacket = new DatagramPacket(receiveBytes = new byte[512], receiveBytes.length);
                socket.receive(numberOfImagesPacket);
                receiveBytes = numberOfImagesPacket.getData();
                numberOfImagesString = new String(receiveBytes);
                numberOfImages = Integer.parseInt(numberOfImagesString);
                System.out.println("The Server will send " + numberOfImages + ".");
                acknowledgementPacket = new DatagramPacket(receiveBytes, receiveBytes.length);
                socket.send(acknowledgementPacket);
                receivedNumberOfImages = true;
            } catch (SocketTimeoutException e) { // if the client times out it will send -1 to the server to show that it
                // didn't receive a correct number of images.
                System.out.println("Socket timed out. Requesting that the server resend the number of images that it will send.");
                numberOfImages = -1;
                numberOfImagesString = Integer.toString(numberOfImages);
                acknowledgementBytes = numberOfImagesString.getBytes();
                acknowledgementPacket = new DatagramPacket(acknowledgementBytes, acknowledgementBytes.length);
                try {
                    socket.send(acknowledgementPacket);
                } catch (IOException io) {
                    System.out.println("IO Exception occurred while asking the server to resend the number of images.");
                    return false;
                }
            } catch (IOException e) {
                System.out.println("IO Exception occurred while exchanging the number of images.");
            }
        }

        if(numberOfImages == 0) { //If there are no images to send end the method here.Client has received everything of importance
            System.out.println("There were no images for the client to send to the client.");
            return true;
        } else if(numberOfImages < 0) { //The server sent a nonsense number. End the method here. This should never happen.
            System.out.println("An error occurred: Quitting data transfer");
            return false;
        } else { //this branch should run if everything is correct up till this point.
            for(int i=0; i<numberOfImages; ++i) {
                packetNumber = 0;

                imageFile = new File("image" + i + ".jpg");
                //creates a bufferedOutputStream to write the incoming bytes to a file
                try{
                    imageStream = new BufferedOutputStream(new FileOutputStream(imageFile));
                } catch(FileNotFoundException e) { //if the file cannot be found return false and notify the user
                    System.out.println("File: \"image" + i + ".jpg\" was not found.");
                    return false;
                }

                receivedImage = false;
                while(!receivedImage) { //stay on this image until it is completely received.
                    try {
                        receivePacket = new DatagramPacket(receiveBytes, receiveBytes.length);
                        socket.receive(receivePacket);

                        if(receivePacket.getData() != cachedBytes) { //server sent a new packet
                            cachedBytes = receivePacket.getData();
                            imageStream.write(receivePacket.getData());
                            imageStream.flush();
                            acknowledgementBytes = Integer.toString(packetNumber).getBytes();
                            acknowledgementPacket = new DatagramPacket(acknowledgementBytes, acknowledgementBytes.length);
                            socket.send(acknowledgementPacket);
                            ++packetNumber;
                        } else { //server timed out and resent the previous packet
                            --packetNumber;
                            acknowledgementBytes = Integer.toString(packetNumber).getBytes();
                            acknowledgementPacket = new DatagramPacket(acknowledgementBytes, acknowledgementBytes.length);
                            socket.send(acknowledgementPacket);
                            ++packetNumber;
                        }
                        if(receivePacket.getData().length<512) {
                            receivedImage = true;
                        }
                    } catch(SocketTimeoutException e) {
                        --packetNumber;
                        acknowledgementBytes = Integer.toString(packetNumber).getBytes();
                        acknowledgementPacket = new DatagramPacket(acknowledgementBytes, acknowledgementBytes.length);
                        ++packetNumber;
                        try {
                            socket.send(acknowledgementPacket);
                        }catch(IOException io) {
                            System.out.println("IO Exception occurred while sending acknowledgement packet.");
                            return false;
                        }
                    }catch(IOException e) {
                        System.out.println("IO Exception has occurred during image " + i + " transfer.");
                        return false;
                    }
                }
                System.out.println("Received image " + i + ".");
            }
            System.out.println("Finished receiving images.");
        }
        return true;
    }
}
