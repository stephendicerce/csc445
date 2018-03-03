import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner kb = new Scanner(System.in);
        String type, hostname, communicationType;
        int port;
        boolean systemSet = false;
        TCPClient client;
        TCPServer server;


        System.out.println("Do you want to use TCP or UDP?");
        communicationType = kb.nextLine();

        if(communicationType.equalsIgnoreCase("tcp")) {
            do {
                System.out.println("Enter whether this computer is a server or a client.(Start the server first)");
                type = kb.nextLine();

                if (type.equalsIgnoreCase("client")) {
                    systemSet = true;
                    System.out.println("Please enter the client's hostname.");
                    hostname = kb.nextLine();
                    System.out.println("Enter the port number to operate on.");
                    port = kb.nextInt();
                    kb.nextLine();
                    client = new TCPClient(hostname, port);
                    if (client.openSocket())
                        client.echo();
                    else
                        System.out.println("Since the socket couldn't be opened, the program will now exit.");
                } else if (type.equalsIgnoreCase("server")) {
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

        } else if(communicationType.equalsIgnoreCase("udp")) {

        } else {
            System.out.println("You need to pick either TCP or UDP. " + communicationType + "is not a correct input.");
        }


    }
}
