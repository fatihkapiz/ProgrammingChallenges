import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.Map;
import java.time.LocalTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class Peer {
    private String host;
    public int port;
    private Set<Socket> peers = new HashSet<>();
    private Set<Integer> peerPorts = new HashSet<>();
    private ConcurrentHashMap<Integer, Socket> peersMap = new ConcurrentHashMap<Integer, Socket>();

    public Peer(String host, int port) {
        this.host = host;
        this.port = port;
    }

    // this method runs on the receiving end Peer
    // starts a thread to accept connections, sends its connected peers list to the connecting end
    public void startServer(int port) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ServerSocket serverSocket = new ServerSocket(port);
                    System.out.println("Server accepting connections on " + host + ":" + port);

                    while (true) {
                        Socket clientSocket = serverSocket.accept();
                        int connectedPeer = receivePortNumber(clientSocket);

                        Set<Integer> received = receiveKeys(clientSocket);
                        peers.add(clientSocket);
                        peerPorts.addAll(received);
                        System.out.println("Connected peer: " + connectedPeer);

                        peersMap.put(connectedPeer, clientSocket);

                        /*
                        peerPorts.add(connectedPeer);
                        peerPorts.addAll(deprocessKeys(receiveKeysString(clientSocket)));
                        */

                        sendKeys(clientSocket, peersMap);

                        System.out.println("Peer-socket mappings: " + peersMap.toString());
                        System.out.println();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    // this method runs on the connecting end
    // sends its own list to the receiver Peer
    // gets the connected peers of the receiver Peer
    // connects to those peers one by one
    // thanks to the recursive calls, also applies first 4 steps to the new peers its connecting to
    private void Connect(int port2Connect) {
        try {
            System.out.println("Trying to connect to peer at: " + port2Connect);
            Socket socket = new Socket("localhost", port2Connect);
            sendPortNumber(socket, this.port);

            sendKeys(socket, this.peersMap);
            peers.add(socket);
            peerPorts.add(port2Connect);
            peersMap.put(port2Connect, socket);
            
            Set<Integer> received = receiveKeys(socket);
            peerPorts.addAll(received);

            System.out.println("Connected to Peer at port " + port2Connect);
            System.out.println(peerPorts.toString());
            for (Integer integer : peerPorts) {
                if (!peersMap.containsKey(integer)) {
                    peersMap.put(integer, new Socket());
                }
            }

            for (Map.Entry<Integer, Socket> entry : peersMap.entrySet()) {
                Integer key = entry.getKey();
                Socket value = entry.getValue();
                if (!value.isConnected()) {
                    Connect(key);
                }
            }
            System.out.println("Updated peers list: " + peerPorts.toString());
        }
        catch (UnknownHostException e) {
            System.out.println("no port listening at that location");
        } 
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    // sends an identifier (aka in-program port number) to the connected peer
    // so receiving end knows which Peer they are connecting to
    private void sendPortNumber(Socket socket, int port) throws IOException {
        // Create a DataOutputStream to send data through the socket
        try {
            // Send the port number as a message
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
            outputStream.writeInt(port);
            outputStream.flush();
        } finally {
            
        }
    }

    // receiving end of the function sendPortNumber(Socket socket, int port)
    private int receivePortNumber(Socket socket) throws IOException {
        // Create a DataInputStream to receive data through the socket
        try {
            // Read the port number from the connecting end
            DataInputStream inputStream = new DataInputStream(socket.getInputStream());
            return inputStream.readInt();
        } finally {

        }
    }

    private void sendKeys(Socket socket, ConcurrentHashMap<Integer, Socket> clientHashMap) throws IOException {
        try {
            // Send the size of the keys set first
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
            outputStream.writeInt(clientHashMap.keySet().size());
            outputStream.flush();

            // Send each key in the set
            for (Integer key : clientHashMap.keySet()) {
                outputStream.writeInt(key);
                outputStream.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private Set<Integer> receiveKeys(Socket socket) throws IOException {
        Set<Integer> receivedKeys = new HashSet<>();

        try {
            // Read the size of the keys set first
            DataInputStream inputStream = new DataInputStream(socket.getInputStream());
            int size = inputStream.readInt();

            // Read each key in the set
            for (int i = 0; i < size; i++) {
                int key = inputStream.readInt();
                receivedKeys.add(key);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return receivedKeys;
    }

    // connect to a peer, then renew connection to get a renewed list of peers from 
    public static void main(String[] arg) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Please enter username: ");
        String username = scanner.nextLine();
        System.out.println("Enter a port number to accept connections: ");
        int portNumber = scanner.nextInt();
        scanner.nextLine();
        Peer peer = new Peer(username, portNumber);
        peer.startServer(peer.port);

        System.out.println("Please enter the port number of the peer you want to connect to: ");
        int p2Connect = scanner.nextInt();
        scanner.nextLine();

        peer.Connect(p2Connect);
    }

}