import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Server {
    private DatagramSocket socket;
    private InetAddress address;
    private int port;
    private boolean slidingWindows;
    private boolean dropPackets;
    private URL urlToSend;
    private String pageDataString;
    private int windowSize;
    private AtomicInteger seq = new AtomicInteger();
    private boolean[] packetSent;

    Server(int port, boolean slidingWindows, boolean dropPackets) {
        this.port = port;
        this.slidingWindows = slidingWindows;
        this.dropPackets = dropPackets;
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
     * The default driver for the server. First the server listens for a url to connect to. Once the URL is received
     * the server will call getData to connect to the url via HTTPConnection, and receive the data from the website
     * in the form of a string. Once the data string is received, it will be parsed to look for urls that are
     * images via parseForImageURLs. All image urls will be stored in an array which will in turn be queried for via
     * getImages. Then the string for the page data will be sent to the client in Datagram Packets with a size of 512
     * bytes each except for the last packet, which will denote an end of transfer. Once the end of transfer's
     * acknowledgement packet has been received the server will send a number representing the number of images it must
     * send. The server will then send each of the images to the client in the same fashion as the page data.
     *
     */
    private void listen() {
        int port;
        byte[] bytesToSend;
        byte[] urlBytes = new byte[512];
        int packetNumber;
        byte[] acknowledgementFromClient = new byte[512];
        URL url;
        boolean clientReceivedNumberOfImages = false;
        DatagramPacket urlPacket;
        DatagramPacket dataPacket;
        DatagramPacket acknowledgementPacket;
        String urlString = null;
        String pageData = null;
        String packetNumberString;
        String[] imageURLs;
        int numberOfImages;
        int clientNumberOfImages;
        String numberOfImagesString;
        byte[] numberOfImagesBytes;

        while(true) {
            System.out.println("Waiting for new URL.");
            urlPacket = new DatagramPacket(urlBytes, urlBytes.length);
            port = 0;
            try {
                socket.receive(urlPacket);
                address = urlPacket.getAddress();
                port = urlPacket.getPort();

                urlString = new String(urlBytes);
                url = new URL(urlString);
                pageData = getData(url);
            } catch(IOException e) {
                System.out.println("IO Exception occurred during the reception of the URL.");
            }
            if(pageData != null) {
                bytesToSend = pageData.getBytes();
                int offset = 0;
                for (int i = 0, numberOfPackets = bytesToSend.length / 512; i < numberOfPackets; ++i) {
                    dataPacket = new DatagramPacket(bytesToSend, offset, 512, address, port);
                    acknowledgementPacket = new DatagramPacket(acknowledgementFromClient, acknowledgementFromClient.length);
                    try {
                        socket.send(dataPacket);
                        socket.receive(acknowledgementPacket);
                        packetNumberString = new String(acknowledgementPacket.getData());
                        packetNumber = Integer.parseInt(packetNumberString);
                        if (packetNumber == i - 1) { //if the client is requesting the previous packet, resend the last packet
                            i -= 1;
                        } else if (packetNumber == i) {
                            offset += 512;
                        } else {
                            System.out.println("Something weird is happening");
                        }
                    } catch(SocketTimeoutException e) {
                        i-=1;
                    } catch (IOException e) {
                        System.out.println("IO Exception occurred during data transmission.");
                    }

                }

                //looks for image urls within the page data
                imageURLs = parseForImageURLs(pageData);
                numberOfImages = imageURLs.length;
                numberOfImagesString = Integer.toString(numberOfImages);
                numberOfImagesBytes = numberOfImagesString.getBytes();

                //sending the client the number of images to expect
                while (!clientReceivedNumberOfImages) {
                    dataPacket = new DatagramPacket(numberOfImagesBytes, numberOfImagesBytes.length, address, port);
                    acknowledgementPacket = new DatagramPacket(acknowledgementFromClient, acknowledgementFromClient.length);
                    try {
                        socket.send(dataPacket);
                        socket.receive(acknowledgementPacket);

                        clientNumberOfImages = Integer.parseInt(new String(acknowledgementPacket.getData()));

                        if (clientNumberOfImages == numberOfImages) {
                            clientReceivedNumberOfImages = true;
                        }
                    } catch(SocketTimeoutException e) {
                        System.out.println("Server timed out, trying again.");
                    } catch(IOException e) {
                        System.out.println("IOException occurred while communicating the number of images with the client.");
                    }
                }

                //transmitting the images to the client
                for (String imageURL : imageURLs) {
                    try {
                        byte[] imageBytes = getImage(new URL(imageURL));

                        if (imageBytes != null) {
                            for (int i = 0, numberOfPackets = imageBytes.length / 512; i < numberOfPackets; ++i) {
                                DatagramPacket imagePacket = new DatagramPacket(bytesToSend, offset, 512, address, port);
                                acknowledgementPacket = new DatagramPacket(acknowledgementFromClient, acknowledgementFromClient.length);
                                try {
                                    socket.send(imagePacket);
                                    socket.receive(acknowledgementPacket);
                                    packetNumberString = new String(acknowledgementPacket.getData());
                                    packetNumber = Integer.parseInt(packetNumberString);
                                    if (packetNumber == i - 1) { //if the client is requesting the previous packet, resend the last packet
                                        i -= 1;
                                    } else if (packetNumber == i) {
                                        offset += 512;
                                    } else {
                                        System.out.println("Something weird occurred");
                                    }
                                } catch (SocketTimeoutException e) {
                                    System.out.println("Socket timed out. re-sending last packet.");
                                    i -= 1;
                                } catch (IOException e) {
                                    System.out.println("IO Exception occurred while transmitting the image: " + imageURL);
                                }
                            }
                        }
                    } catch (MalformedURLException e) {
                        System.out.println(imageURL + "was malformed");
                    }
                }
            } else {
                if(urlString != null)
                    System.out.println("No page Data found for " + urlString + ".");
                else
                    System.out.println("URL was never sent to the server.");
            }
        }
    }

    private void listenWithSlidingWindows() {
        Thread webDataThread;
        GetPageData getPageData;
        int port;
        byte[] bytesToSend;
        byte[] urlBytes = new byte[512];
        int packetNumber;
        byte[] acknowledgementFromClient = new byte[512];
        URL url;
        boolean clientReceivedNumberOfImages = false;
        DatagramPacket urlPacket;
        DatagramPacket dataPacket;
        DatagramPacket acknowledgementPacket;
        String pageData = null;
        String packetNumberString;
        String[] imageURLs;
        int numberOfImages;
        int clientNumberOfImages;
        String numberOfImagesString;
        byte[] numberOfImagesBytes;


        for(;;) {
            System.out.println("Waiting for URL from user...");
            urlPacket = new DatagramPacket(urlBytes, urlBytes.length);
            try {
                socket.receive(urlPacket);
                address = urlPacket.getAddress();
                port = urlPacket.getPort();


                final String urlString = new String(urlPacket.getData());
                getDataAndWindowSize(urlString);


            } catch(IOException e) {
                System.out.println("IO Exception has occurred while receiving url from user.");
            }
        }

    }

    private void listenAndDropPackets() {
        int port;
        byte[] bytesToSend;
        byte[] urlBytes = new byte[512];
        int packetNumber;
        byte[] acknowledgementFromClient = new byte[512];
        URL url;
        boolean clientReceivedNumberOfImages = false;
        DatagramPacket urlPacket;
        DatagramPacket dataPacket;
        DatagramPacket acknowledgementPacket;
        String urlString = null;
        String pageData = null;
        String packetNumberString;
        String[] imageURLs;
        int numberOfImages;
        int clientNumberOfImages;
        String numberOfImagesString;
        byte[] numberOfImagesBytes;

    }

    /**
     * A method to pick which driver to use.
     * Address Type: IPv4 vs IPv6
     * Transfer Type: Sequential acknowledgements vs TCP-like sliding windows
     * Quality Type: All data is successfully managed vs simulating 1% of the total packets being dropped
     *
     * The default driver is IPv4, with sequential acknowledgements, with no packets dropped.
     */
    public void pickListenType () {
        if(slidingWindows) {
            listenWithSlidingWindows();
        } else if(dropPackets) {
            listenAndDropPackets();
        } else {
            listen();
        }

    }

    /**
     * Method to get page data based on the url given by the user
     * @param url the url the method will get information for
     * @return A string containing all of the page's data
     * @throws IOException for HttpURLConnection
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

            int n;

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

    private void setURLToSendToThread(URL url) {
        urlToSend = url;
    }

    public URL getURL() {
        return urlToSend;
    }

    private void getDataAndWindowSize(String urlString) {
        new Thread(() -> {
            try {
                URL pageURL = new URL(urlString);
                sendDataFromThreadToServer(getData(pageURL));
            } catch (IOException e) {
                System.out.println("IO Exception occurred while getting the page data.");
            }
        },"Data").start();

        new Thread(()-> {
            boolean received = false;
            byte[] windowSizeBytes = new byte[512];
            while(!received) {
                DatagramPacket windowSizePacket = new DatagramPacket(windowSizeBytes, windowSizeBytes.length);
                try {
                    socket.receive(windowSizePacket);
                    String windowSizeString = new String(windowSizePacket.getData());
                    sendWindowSizeFromThreadToServer(Integer.parseInt(windowSizeString));
                    received = true;

                } catch (SocketTimeoutException e) {
                    System.out.println("Socket has timed out. Telling client to resend.");
                    int badAck = -1;
                    String badAckString = Integer.toString(badAck);
                    DatagramPacket badAckPacket = new DatagramPacket(badAckString.getBytes(), badAckString.getBytes().length);
                    try {
                        socket.send(badAckPacket);
                    }catch(IOException io) {
                        System.out.println("IO Exception occurred while asking client to resend the window size.");
                    }
                } catch(IOException e) {
                    System.out.println("IO Exception occurred while transmitting window size.");
                }
            }
        },"Window");
    }

    private void sendDataFromThreadToServer(String pageData) {
        pageDataString = pageData;
    }

    private void sendWindowSizeFromThreadToServer(int windowSize) {
        this.windowSize = windowSize;
    }

    private boolean transmitPageDataToClient(String pageData, int windowSize) {
        final byte[] pageDataArray = pageData.getBytes();
        int numberOfPackets = pageDataArray.length/512;
        packetSent = new boolean[numberOfPackets];
        for(int i=0; i<packetSent.length; ++i)
            packetSent[i] = false;
        for(int i=0; i<windowSize; ++i) {
            new Thread(()-> {

            },"Transmitter" + i).start();
        }
    }

    private byte[] getPacketToSend(String pageData) {
        byte[] pageDataBytes = pageData.getBytes();
        int numberOfPackets = pageDataBytes.length/512;

        //boolean array to
        packetSent = new boolean[numberOfPackets];

        int start = seq.get();
        if(start % 2 == 0) {
            if(seq.compareAndSet(start, start+1)){

            }
        }
    }

    private boolean[] sendBooleanArrayBackToThread(boolean[] booleanArray) {
        return
    }
}
