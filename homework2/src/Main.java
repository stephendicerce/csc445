import java.util.Scanner;

public class Main {

    public static void main(String args[]) {
        int argsLength = args.length;
        int port;
        boolean ipvSixPackes = false; //default is IPv4
        boolean tcpSlidingWindows = false; //default is sequential acks
        boolean dropPackets = false; //default is no dropped packets
        boolean typeSet = false;
        boolean socketsOpened = false;
        boolean urlAndDataReceived;
        String input = "";
        String hostname;
        String userURL;
        Scanner kb = new Scanner(System.in);

        if(argsLength>0) { //if the user has entered command line arguments
            for (String arg : args) {
                arg = arg.trim();
                if (arg.equalsIgnoreCase("ipv6"))
                    ipvSixPackes = true;
                if (arg.equalsIgnoreCase("tcp"))
                    tcpSlidingWindows = true;
                if (arg.equalsIgnoreCase("drop"))
                    dropPackets = true;
            }
        }

        while(!input.equalsIgnoreCase("quit")) {
            System.out.println("You can quit at anytime by typing \"quit\" before the system asks for a port number");
            while(!typeSet) {
                System.out.println("Enter whether this is a Server (s) or a client (c).");
                input = kb.nextLine();
                if (!input.equalsIgnoreCase("quit")) {

                    if (input.equalsIgnoreCase("s")) {
                        while(!socketsOpened)
                        System.out.println("Enter the port number the server will use: ");
                        input = kb.nextLine();
                        if(!input.equalsIgnoreCase("quit")) {
                            port = Integer.parseInt(input);
                            Server server = new Server(port, tcpSlidingWindows, dropPackets);
                            socketsOpened = server.openSocket();
                            if (socketsOpened) {
                                server.pickListenType();
                                server.closeSocket();
                            } else {
                                System.out.println("The socket didn't open correctly, Please enter a different port number.");
                            }
                        }

                        typeSet = true;
                    } else if (input.equalsIgnoreCase("c")) {
                        while(!socketsOpened) {
                            System.out.println("Enter the port number the client will run on.");
                            input = kb.nextLine();
                            if(!input.equalsIgnoreCase("quit")) {
                                port = Integer.parseInt(input);
                                System.out.println("Enter the hostname for the server you would like to connect to.");
                                input = kb.nextLine();
                                if(!input.equalsIgnoreCase("quit")) {
                                    hostname = input;

                                    Client client = new Client(hostname, port, ipvSixPackes, tcpSlidingWindows, dropPackets);
                                    socketsOpened = client.openSocket();
                                    if(socketsOpened) {
                                        urlAndDataReceived = false;
                                        while(!urlAndDataReceived) {
                                            System.out.println("Enter a url for which you would like to receive page data and images from.");
                                            input = kb.nextLine();
                                            if (!input.equalsIgnoreCase("quit")) {
                                                userURL = input;
                                                urlAndDataReceived = client.requestURL(userURL);
                                            } else {
                                                client.closeSocket();
                                            }
                                        }
                                    } else {
                                        System.out.println("The socket didn't open correctly, please enter a different port number.");
                                    }
                                }
                            }
                        }

                        typeSet = true;
                    } else {
                        System.out.println("you have entered an invalid command, please try again.");
                        typeSet = false;
                    }
                } else {
                    typeSet = true;
                }
            }
        }
        System.out.println("Thank you for using the Image Transfer Proxy Program\nExiting...");
    }

}
