package fi.utu.tech.telephonegame;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.UUID;
import fi.utu.tech.telephonegame.network.Network;
import fi.utu.tech.telephonegame.network.NetworkService;
import fi.utu.tech.telephonegame.network.Resolver;
import fi.utu.tech.telephonegame.network.Resolver.NetworkType;
import fi.utu.tech.telephonegame.network.Resolver.PeerConfiguration;
import fi.utu.tech.telephonegame.util.ConcurrentExpiringHashSet;

/**
 * MessageBroker should work as the "middle-man", the broker
 * that relays messages between different components of the application.

 */
public class MessageBroker extends Thread {

	/*
	 * No need to change these variables
	 */
	private Network network;
	private Resolver resolver;
	// Abstracted access to graphical interface input and output
	private GuiIO gui_io;
	// Defualt listening port
	private final int rootServerPort = 8050;
	// This might come in handy
	private ConcurrentExpiringHashSet<UUID> prevMessages = new ConcurrentExpiringHashSet<UUID>(1000, 5000);

	/*
	 * No need to edit the constructor
	 */
	public MessageBroker(GuiIO gui_io) {
		this.gui_io = gui_io;
		network = new NetworkService();
	}
	/**

	 *
	 * @param procMessage The object to be processed
	 *
	 * @return The message processed in the aforementioned ways
	 *
	 */
	private Message process(Object procMessage) {

		if (procMessage instanceof Message) {
			Message message = (Message) procMessage;

			// Check if the message has not been processed before
			if (!prevMessages.containsKey(message.getId())) {
				// Show the incoming message in the received message text area (GuiIO)
				gui_io.setReceivedMessage(message.getMessage());

				// Change the text and the color using the Refiner class
				String refinedMessage = Refiner.refineText(message.getMessage());
				int refinedColor = Refiner.refineColor(message.getColor());

				// Set the new color to the color area (see GuiIO)
				gui_io.setSignal(refinedColor);

				// Show the refined message in the refined message text area (GuiIO)
				gui_io.setRefinedMessage(refinedMessage);

				// Add the message to the set of processed messages
				prevMessages.put(message.getId());

				// Return the processed message
				return message = new Message(message.getId(), refinedMessage, refinedColor);
			}
		}
		return null;


	}

	/**
	 * This run method will be executed in a separate thread automatically by
	 * the template (see initialize() method in App.java)
	 *
	 * In the run method you need to check:
	 *
	 * 1. If there are any received objects offered by network package
	 * 2. When a new object is available it has to be processed using the "process"
	 * method
	 * 3. Send the processed message back to network (see send method in this class)
	 *
	 */
	public void run() {
		// TODO
		while (!Thread.interrupted()) {
			try {
				// Check if there are any received objects offered by the network package
				Object receivedObject = network.retrieveMessage(); // LISÄYS

				// When a new object is available, it has to be processed using the "process" method
				Message processedMessage = process(receivedObject);

				// Send the processed message back to the network (see send method in this class)
				if (processedMessage != null) {
					send(processedMessage);
				}

			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				System.err.println("Error running messagebroker:" + e);
			}
		}
	}

	/**
	 * Sends message to the peer network using methods provided by network
	 * interface
	 *
	 * You need to make changes here
	 * @param message The Message object to be sent
	 */
	public void send(Message message) {
		// TODO
		/*
		 * Currently sending null to suppress errors
		 * but obviously this has to change.
		 * For some reason we cannot (yet) post Message objects...
		 * Maybe take a look into the Message class and see if you
		 * need to make it implement something to make postMessage
		 * satisfied. Do not change the Network interface though.
		 */
		network.postMessage(message);
	}

	/**
	 * Wraps the String into a new Message object
	 * and adds it to the sending queue to be processed by the network component
	 * Called when sending a new message
	 * @param text The text to be wrapped and sent
	 */
	public void send(String text) {
		Message message = new Message(text, 0);
		this.send(message);
		prevMessages.put(message.getId());
	}




	/*
	 * Do not edit anything below this point.
	 */

	/**
	 * Determines which peer to connect to (or if none)
	 * Called when user wants to "Discover and connect" or "Start waiting for peers"
	 * Calls the appropriate methods in Network object with correct arguments
	 *
	 * @param netType
	 * @param rootNode
	 * @param rootIPAddress
	 */
	public void connect(NetworkType netType, boolean rootNode, String rootIPAddress) {
		resolver = new Resolver(netType, rootServerPort, rootIPAddress);
		if (rootNode) {
			System.out.println("Root node");
			// Use the default port for the server and start listening for peers
			network.startListening(rootServerPort);
			// As a root node, we are responsible for answering resolving requests - start resolver server
			resolver.startResolverServer();
			// No need to connect to anybody since we are the first node, the "root node"
		} else {
			System.out.println("Leaf node");
			try {
				// Broadcast a resolve request and wait for a resolver server (root node) to send peer configuration
				PeerConfiguration addresses = resolver.resolve();
				// Start listening for new peers on the port sent by the resolver server
				network.startListening(addresses.listeningPort);
				// Connect to a peer using addresses sent by the resolver server
				network.connect(addresses.peerAddr, addresses.peerPort);
			} catch (UnknownHostException | NumberFormatException e) {
				System.err.println("Peer discovery failed (maybe there are no root nodes or broadcast messages are not supported on current network)");
				gui_io.enableConnect();
			} catch (IOException e) {
				System.err.println("Error connecting to the peer");
				gui_io.enableConnect();
			}
		}
	}

}
