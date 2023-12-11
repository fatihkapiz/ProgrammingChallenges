import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client {
  private Socket socket;
  private BufferedReader reader;
  private BufferedWriter writer;
  private String username;

  public Client(Socket socket, String username) {
    try {
      this.socket = socket;
      this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
      this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      this.username = username;
    }
    catch(Exception ex) {
      closeClient();
    }
  }

  public void sendMessage() {
    try {
      writer.write(username);
      writer.newLine();
      writer.flush();

      Scanner scanner = new Scanner(System.in);
      while (socket.isConnected()) {
        writer.write(username + ": " + scanner.nextLine());
        writer.newLine();
        writer.flush();
      }
    }
    catch(IOException exception) {
      closeClient();
    }
  }

  public void listen() {
    new Thread(new Runnable() {
      @Override
      public void run() {
        String message;
        while (socket.isConnected()) {
          try {
            message = reader.readLine();
            System.out.println(message);
          }
          catch (IOException exception) {
            closeClient();
          }
        }
      }
    }).start();
  }

  public void closeClient() {
    try {
      writer.close();
      reader.close();
      socket.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in);
    System.out.println("Please enter username: ");
    String username = scanner.nextLine();
    Socket socket;
    try {
      socket = new Socket("localhost", 1234);
      Client client = new Client(socket, username);
      client.listen();
      client.sendMessage();
    } catch (UnknownHostException e) {
      System.out.println("connection could not be established");
    } catch (IOException e) {
      System.out.println("connection could not be established");
    }
  }
}
