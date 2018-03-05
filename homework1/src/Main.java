import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner kb = new Scanner(System.in);
        String type, hostname, communicationType;
        int port;
        boolean systemSet = false;
        boolean tcpTypeBoolean = false;
        boolean udpTypeBoolean = false;
        TCPClient client;
        TCPServer server;
        UDPClient udpClient;
        UDPServer udpServer;


        System.out.println("Do you want to use TCP or UDP?");
        communicationType = kb.nextLine();

        if(communicationType.equalsIgnoreCase("t")) {
            String tcpType;
            do {
                System.out.println("Do you want to check the interaction between 1MB of data sent with different message sizes(i), measure throughput (t) or simple echo (e)?");
                tcpType = kb.nextLine();
                if (tcpType.equalsIgnoreCase("e")) {
                    tcpTypeBoolean = true;
                    do {
                        System.out.println("Enter whether this computer is a server or a client.(Start the server first)");
                        type = kb.nextLine();

                        if (type.equalsIgnoreCase("c")) {
                            systemSet = true;
                            System.out.println("Please enter the hostname of the server you wish to connect to.");
                            hostname = kb.nextLine();
                            System.out.println("Enter the port number to operate on.");
                            port = kb.nextInt();
                            kb.nextLine();
                            client = new TCPClient(hostname, port);
                            if (client.openSocket())
                                client.echo();
                            else
                                System.out.println("Since the socket couldn't be opened, the program will now exit.");
                        } else if (type.equalsIgnoreCase("s")) {
                            systemSet = true;
                            System.out.println("Please enter which port number the server should operate on.");
                            port = kb.nextInt();
                            kb.nextLine();
                            server = new TCPServer(port);
                            if (server.openSockets())
                                server.listening(64, 1024);
                            else
                                System.out.println("Since the server couldn't be started, the program will now exit.");
                        } else {
                            System.out.println("You didn't enter client or server. Please try again.");
                        }
                    } while (!systemSet);

                } else if (tcpType.equalsIgnoreCase("t")) {
                    tcpTypeBoolean = true;
                    System.out.println("Enter whether this computer is a server or a client.(Start the server first)");
                    type = kb.nextLine();

                    if (type.equalsIgnoreCase("c")) {
                        systemSet = true;
                        System.out.println("Please enter the hostname of the server you wish to connect to.");
                        hostname = kb.nextLine();
                        System.out.println("Enter the port number to operate on.");
                        port = kb.nextInt();
                        kb.nextLine();
                        client = new TCPClient(hostname, port);
                        if (client.openSocket())
                            client.measureTransferRates(1000, 16000, 64000, 256000, 1000000);
                        else
                            System.out.println("Since the socket couldn't be opened, the program will now exit.");
                    } else if (type.equalsIgnoreCase("s")) {
                        systemSet = true;
                        System.out.println("Please enter which port number the server should operate on.");
                        port = kb.nextInt();
                        kb.nextLine();
                        server = new TCPServer(port);
                        if (server.openSockets())
                            server.measureTransferRates(1000, 16000, 64000, 256000, 1000000);
                        else
                            System.out.println("Since the server couldn't be started, the program will now exit.");
                    } else {
                        System.out.println("You didn't enter client or server. Please try again.");
                    }
                } else if(tcpType.equalsIgnoreCase("i")) {
                    tcpTypeBoolean = true;
                    System.out.println("Enter whether this computer is a server or a client.(Start the server first)");
                    type = kb.nextLine();

                    if (type.equalsIgnoreCase("c")) {
                        systemSet = true;
                        System.out.println("Please enter the hostname of the server you wish to connect to.");
                        hostname = kb.nextLine();
                        System.out.println("Enter the port number to operate on.");
                        port = kb.nextInt();
                        kb.nextLine();
                        client = new TCPClient(hostname, port);
                        if (client.openSocket())
                            client.interactionForMByte(2000,512, 2000, 512, 1000, 1024);
                        else
                            System.out.println("Since the socket couldn't be opened, the program will now exit.");
                    } else if (type.equalsIgnoreCase("s")) {
                        systemSet = true;
                        System.out.println("Please enter which port number the server should operate on.");
                        port = kb.nextInt();
                        kb.nextLine();
                        server = new TCPServer(port);
                        if (server.openSockets())
                            server.interactionForMByte(4000, 256, 2000, 512, 1000, 1024);
                        else
                            System.out.println("Since the server couldn't be started, the program will now exit.");
                    } else {
                        System.out.println("You didn't enter client or server. Please try again.");
                    }
                }

            } while(!tcpTypeBoolean);

        } else if(communicationType.equalsIgnoreCase("u")) {
            String udpType;
            do {
                System.out.println("Simple echo (e) or inspect a 1MB transfer(i)");
                udpType = kb.nextLine();
                if(udpType.equalsIgnoreCase("e")) {
                    udpTypeBoolean = true;
                    do {
                        System.out.println("Enter whether this computer is a server or a client.(Start the server first)");
                        type = kb.nextLine();

                        if (type.equalsIgnoreCase("c")) {
                            systemSet = true;
                            System.out.println("Please enter the hostname of the server you wish to connect to.");
                            hostname = kb.nextLine();
                            System.out.println("Enter the port number to operate on.");
                            port = kb.nextInt();
                            kb.nextLine();
                            udpClient = new UDPClient(hostname, port);
                            if (udpClient.openSocket())
                                udpClient.echo(1, 64, 1024);
                            else
                                System.out.println("Since the socket couldn't be opened, the program will now exit.");
                        } else if (type.equalsIgnoreCase("s")) {
                            systemSet = true;
                            System.out.println("Please enter which port number the server should operate on.");
                            port = kb.nextInt();
                            kb.nextLine();
                            udpServer = new UDPServer(port);
                            if (udpServer.openSocket())
                                udpServer.listening(1, 64, 1024);
                            else
                                System.out.println("Since the server couldn't be started, the program will now exit.");
                        } else {
                            System.out.println("You didn't enter client or server. Please try again.");
                        }
                    } while (!systemSet);
                } else if(udpType.equalsIgnoreCase("i")){
                    udpTypeBoolean = true;
                    do {
                        System.out.println("Enter whether this computer is a server or a client.(Start the server first)");
                        type = kb.nextLine();

                        if (type.equalsIgnoreCase("c")) {
                            systemSet = true;
                            System.out.println("Please enter the hostname of the server you wish to connect to.");
                            hostname = kb.nextLine();
                            System.out.println("Enter the port number to operate on.");
                            port = kb.nextInt();
                            kb.nextLine();
                            udpClient = new UDPClient(hostname, port);
                            if (udpClient.openSocket())
                                udpClient.interactionForMBye(4000, 256, 2000, 512, 1000, 1024);
                            else
                                System.out.println("Since the socket couldn't be opened, the program will now exit.");
                        } else if (type.equalsIgnoreCase("s")) {
                            systemSet = true;
                            System.out.println("Please enter which port number the server should operate on.");
                            port = kb.nextInt();
                            kb.nextLine();
                            udpServer = new UDPServer(port);
                            if (udpServer.openSocket())
                                udpServer.interactionForMBye(4000, 256, 2000, 512, 1000, 1024);
                            else
                                System.out.println("Since the server couldn't be started, the program will now exit.");
                        } else {
                            System.out.println("You didn't enter client or server. Please try again.");
                        }
                    } while (!systemSet);
                } else {
                    System.out.println("Incorrect input. Try again.");
                }
            }while(!udpTypeBoolean);

        } else {
            System.out.println("You need to pick either TCP or UDP. " + communicationType + "is not a correct input.");
        }


    }
}
