package fi.utu.tech.telephonegame.network;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;

public class NetworkService extends Thread implements Network {

	private BlockingQueue<Serializable> outgoingMessages = new LinkedBlockingQueue<>();
	private BlockingQueue<Serializable> incomingMessages = new LinkedBlockingQueue<>();
	private CopyOnWriteArrayList<ClientHandler> clientHandlers = new CopyOnWriteArrayList<>();

	private ServerSocket serverSocket;
	private Socket socket;

	public NetworkService() {
		this.start();
	}

	public void startListening(int serverPort) {
		try {
			System.out.printf("Starting server and listening on port %d%n", serverPort);
			serverSocket = new ServerSocket(serverPort);
			Thread listenThread = new Thread(() -> {
				while (true) {
					try {
						Socket clientSocket = serverSocket.accept();
						System.out.println("New connection established");
						ClientHandler clientHandler = new ClientHandler(clientSocket, this); // Pass the reference to NetworkService
						clientHandlers.add(clientHandler);
						new Thread(clientHandler).start();
					} catch (IOException e) {
						e.printStackTrace();
						System.err.println("Error listening port:" + serverPort);
					}
				}
			});
			listenThread.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private void sendToAllNeighbours(Serializable out) {
		System.out.println("Using sendToAllNeighbours");
		for (ClientHandler clientHandler : clientHandlers) {
			try {
				clientHandler.sendMessage(out);
			} catch (IOException e) {
				// Handle communication error with the specific client handler
				e.printStackTrace();
			}
		}
	}
	public void postMessage(Serializable out) {
		System.out.println("Using postMessage");

		try {
			outgoingMessages.put(out);
			System.out.println(out);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	public Serializable retrieveMessage() throws InterruptedException {
		System.out.println("Using retrieveMessage " + incomingMessages);
		return incomingMessages.take();
	}

	public void connect(String peerIP, int peerPort) throws IOException, UnknownHostException {
		System.out.printf("Connecting to %s, TCP port %d%n", peerIP, peerPort);
		socket = new Socket(peerIP, peerPort);
		ClientHandler clientHandler = new ClientHandler(socket, this); // Pass the reference to NetworkService
		clientHandlers.add(clientHandler);
		new Thread(clientHandler).start();
	}

	// Method for handling a received message from the ClientHandler and adding it to the private incomingMessages list
	public void handleReceivedMessage(Serializable message) {
		try {
			incomingMessages.put(message);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	public void run() {
		while (true) {
			try {
				Serializable message = outgoingMessages.take();
				sendToAllNeighbours(message);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
