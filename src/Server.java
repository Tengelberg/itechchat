import java.net.*;
import java.io.*;
import java.util.ArrayList;

public class Server {

	private ArrayList<ClientInfo> clients;
	private DatagramSocket server;
	private DatagramPacket packet;
	private byte[] buffer;

	public Server() {
		clients = new ArrayList<ClientInfo>();
		buffer = new byte[1024];
		try {
			server = new DatagramSocket(4711);
			recieve();
		} catch (SocketException e) {
			System.out.println("can't open Socket");
			System.exit(1);
		} catch (IOException e2) {
			System.out.println("Packet konnte nicht empfangen werden");
		} finally {
			server.close();
		}
	}

	public static void main(String[] args) {
		new Server();
	}

	public void recieve() throws IOException {
		while (true) {
			packet = new DatagramPacket(buffer, buffer.length);
			server.receive(packet);
			new ServerThread(server, packet, clients).start();
		}

	}

}

class ServerThread extends Thread {
	private DatagramSocket socket;
	private DatagramPacket packet;
	private ArrayList<ClientInfo> clients;

	public ServerThread(DatagramSocket socket, DatagramPacket packet,
			ArrayList<ClientInfo> clients) {
		this.socket = socket;
		this.packet = packet;
		this.clients = clients;
	}

	public void run() {
		int port = packet.getPort();
		int len = packet.getLength();
		InetAddress address = packet.getAddress();
		byte[] data = packet.getData();

		String message = new String(data, 0, len);
		System.out.println("SO wird die nachricht dem thread gegeben: "
				+ message + " mit der Laenge:" + len);
		int opcode = Integer.parseInt(Character.toString(message.charAt(0)));
		String news = message.substring(1);
		try {
			if (opcode == 0) {
				sendTo(news, address, port);
			} else if (opcode == 1) {
				changeName(address, port, news);
			} else if (opcode == 2) {
				displayOnline(address, port);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println(opcode);
	}

	public void sendTo(String message, InetAddress aaddress, int aport)
			throws IOException {
		String tag = "<server>";
		String name = "";
		boolean found = false;
		for (ClientInfo client : clients) {
			if (client.getAddress().equals(aaddress)
					&& client.getPort() == aport) {
				name = client.getName();
				found = true;
			}
		}
		if (found) {
			for (ClientInfo info : clients) {
				InetAddress address = info.getAddress();
				int port = info.getPort();
				tag = "<" + name + ">";
				byte[] data = tag.concat(message).getBytes();
				packet = new DatagramPacket(data, data.length, address, port);
				socket.send(packet);
				System.out.println("package send to client! :"
						+ new String(data));
			}
		} else {
			byte[] data = new String(tag + "please choose a name first!")
					.getBytes();
			packet = new DatagramPacket(data, data.length, aaddress, aport);
			socket.send(packet);
		}

	}

	public void changeName(InetAddress address, int port, String name)
			throws IOException {
		String tag = "<server>";
		System.out.println(name);
		boolean found = false;
		boolean free = true;
		byte[] data = new byte[1024];
		if (!clients.isEmpty()) {
			for (ClientInfo info : clients) {
				if (info.getAddress().equals(address) && info.getPort() == port
						&& !(info.getName().equals(name))) {
					info.setName(name);
					found = true;
					data = new String(tag + "your name was changed into: "
							+ name).getBytes();
				} else if (info.getName().equals(name))
					free = false;

			}
		}

		if (!found && free) {
			clients.add(new ClientInfo(name, address, port));
			data = new String(tag + "your name was changed into: " + name)
					.getBytes();
		} else if(!free){
			data = new String(tag + "NAME: " + name + " IS ALREADY IN USE")
					.getBytes();
		}

		packet = new DatagramPacket(data, data.length, address, port);
		socket.send(packet);
	}

	public void displayOnline(InetAddress address, int port) throws IOException {
		String tag = "<server>";
		String message = tag + clients.size() + "user online : \n";
		for (ClientInfo info : clients) {
			message = message.concat(tag + info.getName() + "\n");
			System.out.println("Oha");
		}
		byte[] data = message.getBytes();
		packet = new DatagramPacket(data, data.length, address, port);
		socket.send(packet);
	}
}
