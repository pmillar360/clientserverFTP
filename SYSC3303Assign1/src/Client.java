import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;
/*
 * Sends a message in format 01(or 02)<filename>0<mode>0 
 * then waits for a response from the intermediate host (appears as server)
 */
public class Client {
	private DatagramSocket sendReceiveSock;
	private DatagramPacket toIntHostPacket, fromIntHostPacket;
	private ByteBuffer buf;
	private InetAddress address;
	private String filename;
	private String mode;
	private boolean running;
	
	public Client() {
		try {
			sendReceiveSock = new DatagramSocket();
			address = InetAddress.getLocalHost();
		}
		catch (Exception se) {
			se.printStackTrace();
			System.exit(1);
		}
		buf = ByteBuffer.allocate(100);
		
		filename = "test.txt";
		mode = "asCiI";
		running = true;

	}
	
	public void sendPackets() {
		for(int i = 1; i <= 11; i++) {
			
			if(i==11) {
				buf.put((byte)2); //create invalid request
				buf.put((byte)2);
			}
			
			else if(i%2 == 0) { 
				buf.put((byte)0); //create read request
				buf.put((byte)1);
			}
			
			else {
				buf.put((byte)0); //create write request
				buf.put((byte)2);
			}
			
			buf.put(filename.getBytes());
			buf.put((byte)0);
			mode = mode.toLowerCase();
			buf.put(mode.getBytes());
			buf.put((byte)0);
			
			try {
				toIntHostPacket = new DatagramPacket(buf.array(), buf.position(), address, 68); //create packet to send
			} catch (Exception e) {
		         e.printStackTrace();
		         System.exit(1);
		    }
			
			try {
				sendReceiveSock.send(toIntHostPacket); //send packet
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}	
			
		System.out.println("Client Packet "+i+" sent");
		System.out.println("To host: " + toIntHostPacket.getAddress());
		System.out.println("Destination host port: " + toIntHostPacket.getPort());
		int len = toIntHostPacket.getLength();
		System.out.println("Length: " + len + " bytes");
		System.out.println("Containing:\nFilename: "+filename+"\nMode: "+mode);
		String s = new String(buf.array(),0,buf.capacity());
		String t = Arrays.toString(buf.array());
		System.out.println("Output as:\nString: "+ s + "\nBytes: " + t + "\n");
		buf.clear();
		}
	}
	
	public void receivePackets() {
		
		int j = 1;
		while(running) {
			fromIntHostPacket = new DatagramPacket(buf.array(),buf.position());

			try {
				sendReceiveSock.receive(fromIntHostPacket); // blocking receive
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}

			System.out.println("Client received packet " + j);
			System.out.println("From host: " + fromIntHostPacket.getAddress());
			System.out.println("Host port: " + fromIntHostPacket.getPort());
			int len = fromIntHostPacket.getLength();
			System.out.println("Length: " + len + "bytes");
			System.out.println("Containing filename: "+filename+"\nmode: "+mode);
			String s = new String(buf.array(),0,buf.capacity());
			String t = Arrays.toString(buf.array());
			System.out.println("Output as:\nString: "+ s + "\nBytes: " + t +"\n");
			buf.clear();
			
			if(j>12) {
				sendReceiveSock.close();
				System.exit(1);
			}
			j++;
		}
	}
	
	public static void main(String[] args) {
		Client c = new Client();
		System.out.println("Client started...");
		c.sendPackets();
		c.receivePackets();
	}
}
