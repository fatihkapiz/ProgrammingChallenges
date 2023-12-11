// WRITTEN BY FATIHKAPIZ - c2011064

// USAGE
// MAKE SURE TO COMPILE CLIENTHANDLER FIRST TO RUN Server.java
// javac ClientHandler.java
// java Server.java
// java Client.java

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
  private ServerSocket serverSocket;

  public Server(ServerSocket serverSocket) {
    System.out.println("Server started.");
    this.serverSocket = serverSocket;
  }

  public void StartServer() {
    try {
      while(!serverSocket.isClosed()) {
        Socket connectionSocket = serverSocket.accept();
        System.out.println("Client connected");
        ClientHandler clientConnection = new ClientHandler(connectionSocket);
        Thread connection = new Thread(clientConnection);
        connection.start();
      }
    }
    catch(IOException  ex) {

    }
  }

  public void closeServerSocket() {
    try {
      if (serverSocket != null) {
        serverSocket.close();
      }
    }
    catch(IOException ex) {
      ex.printStackTrace();
    }
  }

  public static void main(String[] args) throws IOException {
    ServerSocket serverSocket = new ServerSocket(1234);
    Server server = new Server(serverSocket);
    server.StartServer();
  }
}
