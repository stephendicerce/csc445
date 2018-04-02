import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class Server {
    DatagramSocket socket;
    InetAddress address;
    int port;

    public Server(int port) {
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

    public void closeSocket() {
        socket.close();
    }

    /**
     * The driver for the server. First the server listens for a url to connect to. Once the URL is received
     * the server will call getData to connect to the url via HTTPConnection, and receive the data from the website
     * in the form of a string. Once the data string is received, it will be parsed to look for urls that are
     * images via parseForImageURLs. All image urls will be stored in an array which will in turn be queried for via
     * getImages. Then the string for the page data will be sent to the client in Datagram Packets with a size of 512
     * bytes each except for the last packet, which will denote an end of transfer. Once the end of transfer's
     * acknowledgement packet has been received the server will send a number representing the number of images it must
     * send. The server will then send each of the images to the client in the same fashion as the page data.
     *
     */
    public void listen() {
        byte[] bytesToSend;
        byte[] lastBytesSent;
        byte[] urlbytes = new byte[1024];
        int packetNumber = 0;
        byte[] acknowledgementFromClient = new byte[1024];
        URL url;
        boolean running = true;
        DatagramPacket urlPacket;
        DatagramPacket dataPacket;
        DatagramPacket acknowledgementPacket;
        String urlString;
        String pageData;
        String packetNumberString;
        String[] imageURLs;

        while(running) {
            urlPacket = new DatagramPacket(urlbytes, urlbytes.length);
            try {
                socket.receive(urlPacket);
                urlString = new String(urlbytes);
                url = new URL(urlString);
                pageData = getData(url);

                bytesToSend = pageData.getBytes();
                int offset = 0;
                for(int i=0, numberOfPackets=bytesToSend.length/512; i<numberOfPackets; ++i) {
                    dataPacket = new DatagramPacket(bytesToSend, offset, 512);
                    acknowledgementPacket = new DatagramPacket(acknowledgementFromClient, acknowledgementFromClient.length);
                    socket.send(dataPacket);
                    socket.receive(acknowledgementPacket);
                    packetNumberString = new String(acknowledgementPacket.getData());
                    packetNumber = Integer.parseInt(packetNumberString);
                    if (packetNumber == i-1){ //if the client is requesting the previous packet, resend the last packet
                        i-= 1;
                    } else if(packetNumber == i){
                        offset += 512;
                    } else {
                        System.out.println("Something weird is happening");
                    }
                }

                imageURLs = parseForImageURLs(pageData);
                for(int i=0, numberOfImagesFound=imageURLs.length; i<numberOfImagesFound; ++i) {
                    boolean receivedImage = false;
                    while(!receivedImage) {

                    }
                }


            }catch(IOException e) {
                System.out.println("IO Exception occurred while receiving url");
            }
        }

    }

    /**
     * Method to get page data based on the url given by the user
     * @param url the url the method will get information for
     * @return A string containing all of the page's data
     * @throws IOException
     */
    private String getData(URL url) throws IOException{
        String pageData = "";
        String lineString;
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Content-Type", "text/plain");
        connection.connect();

        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));

        while((lineString = br.readLine()) != null) {
            pageData += lineString;
        }
        return pageData;
    }

    /**
     * A method to look for image urls within the html for a website
     * @param pageData A string containing the html for a webpage
     * @return An array of urls for image files represented by Strings
     */
    private String[] parseForImageURLs(String pageData) {
        List<String> urlStringList = new ArrayList<>();
        int startingIndex = 0;

        while(pageData.contains("<img")){
            String img;
            String url;
            pageData = pageData.substring(pageData.indexOf("<img"));
            img = pageData.substring(startingIndex, pageData.indexOf(">"));
            pageData = pageData.substring((pageData.indexOf(">")));

            img = img.substring(img.indexOf("src=\""));
            url = img.substring(0,img.indexOf("\""));
            System.out.println("Image Url: " + url);
            urlStringList.add(url);
        }


        return urlStringList.toArray(new String[urlStringList.size()]);
    }

    /**
     * A method to get image data for the user-requested page.
     * @param url the image specific url
     * @return a byte array containing the image data
     */
    private byte[] getImage(URL url) {
        try {
            InputStream in = new BufferedInputStream(url.openStream());
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];

            int n =0;

            while(-1!=(n=in.read(buf))) {
                out.write(buf,0,n);
            }
            out.close();
            in.close();
            return out.toByteArray();

        } catch(IOException e) {
            System.out.println("IO Exception getting data from URL");
            return null;
        }
    }
}