package fi.utu.tech.telephonegame.network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;


public class ClientHandler extends Thread {
    private final ObjectInputStream inputStream;
    private final ObjectOutputStream outputStream;
    private final NetworkService networkService; // Add this reference

    public ClientHandler(Socket clientSocket, NetworkService networkService) throws IOException {

        this.outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
        this.inputStream = new ObjectInputStream(clientSocket.getInputStream());
        this.networkService = networkService; // Initialize the reference
    }
    @Override
    public void run() {
        try {
            while (true) {
                Serializable message = (Serializable) inputStream.readObject();
                System.out.println("Received message: " + message);

                // Pass the message to NetworkService
                networkService.handleReceivedMessage(message);
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(Serializable message) throws IOException {
        outputStream.writeObject(message);
        outputStream.flush();
    }
}
