import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;

public class Client {
    private DatagramSocket socket;
    private Inet4Address addressFour;
    private Inet6Address addressSix;
    private String hostname;
    private int port;
    private File file;
    private boolean datagramType; //true for IPv4, false for IPv6
    private String filename = null;

    public Client(String hostname, int port, boolean datagramType) {
        this.hostname = hostname;
        this.port = port;
        this.datagramType = datagramType;
    }

    public boolean openSocket() {
        try {
            socket = new DatagramSocket();
            socket.setSoTimeout(3000);
            if(datagramType) {
                addressFour.getByName(hostname);
            } else {
                addressSix.getByName(hostname);
            }
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
     * @param fileNumber a unique identifier if the user requests more than 1 url, incremented from the main class to tell the files apart from one another
     * @return true if the transfer was successful, false if the transfer was unsuccessful
     */
    public boolean requestURL(String url, int fileNumber) {
        boolean received = false; //used to tell if the transfer was completed
        boolean recievedImage = false;
        byte[] receiveByte = new byte[512]; //byte array for the incoming packets
        byte[] urlBytes = url.getBytes(); //the user requested url translated into a byte array for transfer.
        byte[] acknowledgementBytes = new byte[1]; //a byte array containing bytes representing the last packet received by the client
        byte packetNumber = 0;
        acknowledgementBytes[0] = packetNumber;
        DatagramPacket urlPacket;
        DatagramPacket receivePacket;
        DatagramPacket acknowledgementPacket;
        filename = "image" + fileNumber + ".jpeg";
        File file = new File(filename);
        BufferedOutputStream outputStream;

        //creates a bufferedOutputStream to write the incoming bytes to a file
        try{
            outputStream = new BufferedOutputStream(new FileOutputStream(file));
        } catch(FileNotFoundException e) { //if the file cannot be found return false and notify the user
            System.out.println("File: "+ filename + " was not found.");
            return false;
        }

        //send the url from the images come from to the server for processing
        try {
            if (datagramType) { // using IPv4 packets
                urlPacket = new DatagramPacket(urlBytes, urlBytes.length, addressFour, port);
            } else { // using IPv6 packets
                urlPacket = new DatagramPacket(urlBytes, urlBytes.length, addressSix, port);
            }
            socket.send(urlPacket); //sends the packet to the server

        } catch(IOException e) { //finds all IO errors with the socket and notifies the user that an error occurred. returns false because the transfer was unsuccessful.
            System.out.println("IO Exception while requesting URL.");
            return false;
        }
        while(!received) {
            try {
                receivePacket = new DatagramPacket(receiveByte, receiveByte.length);
                socket.receive(receivePacket);
                outputStream.write(receivePacket.getData());
                acknowledgementBytes[0] = 0;
                acknowledgementPacket = new DatagramPacket(acknowledgementBytes, acknowledgementBytes.length);
                socket.send(acknowledgementPacket);
                ++acknowledgementBytes[0];
                if (receivePacket.getLength() < 512)
                    received = true;
            } catch(SocketTimeoutException e) {
                System.out.println("Client timeout: Asking server for resend");
                try {
                    acknowledgementPacket = new DatagramPacket(acknowledgementBytes, acknowledgementBytes.length);
                    socket.send(acknowledgementPacket);
                }catch(IOException io) {
                    System.out.println("IO Exception during timeout.");
                    return false;
                }
            }catch (IOException e) {
                System.out.println("IO Exception while requesting url.");
                return false;
            }
        }
        return true;
    }

    private boolean isFilenameSet() {
    if(filename == null)
        return false;
    return true;
    }

    public String getFilename() {
        if(isFilenameSet())
            return filename;
        return "null";
    }

}
