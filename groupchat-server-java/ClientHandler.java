import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler implements Runnable {
  private Socket connectionSocket;
  private BufferedReader reader;
  private BufferedWriter writer;
  private static ArrayList<ClientHandler> connections = new ArrayList<>();
  private String clientUsername;

  public ClientHandler(Socket socket) throws IOException {
    super();
    this.connectionSocket = socket;
    reader = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
    writer = new BufferedWriter(new OutputStreamWriter(connectionSocket.getOutputStream()));
    this.clientUsername = reader.readLine();
    connections.add(this); 
    String connectedMessage = "User " + clientUsername + " has connected.";
    broadcastMessage(connectedMessage);
  }

  public void broadcastMessage(String message) {
    for (ClientHandler connection : connections) {
      try {
        if (!connection.clientUsername.equals(this.clientUsername)) {
          connection.writer.write(message);
          connection.writer.newLine();
          connection.writer.flush();
        }
      } catch(Exception exception) {
        closeConnection();
      }
    }
  }

  @Override
  public void run() {
    // keep a connection open with the client
    // listen to a client
    // direct message to all other clients
    
    String clientMessage;
    while (connectionSocket.isConnected()) {
      try {
        clientMessage = reader.readLine();
        broadcastMessage(clientMessage);
      } catch (IOException e) {
        closeConnection();
        break;
      }
    }
  }

  public void closeConnection() {
    try {
      broadcastMessage(clientUsername + "has left the chat.");
      connectionSocket.close();
      reader.close();
      writer.close();
      connections.remove(this);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
