import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Arrays;
/*
 * Acts as a server, waiting to receive packets from the intermediate host then sending back
 * a 4-byte response depending on request received
 */
public class Server {
	private DatagramSocket serverSock;
	private DatagramPacket fromIntHostPacket, toIntHostPacket;
	private ByteBuffer buf;
	private boolean running;
	private String filename;
	private String mode;
	private byte[] receivedData;
	
	
	private Server() {
		try {
			serverSock = new DatagramSocket(69);
		} catch (SocketException se) {
			se.printStackTrace();
			System.exit(1);
		}
		buf = ByteBuffer.allocate(100);
		running = true;
	}
	
	public void serverLoop() throws Exception {
		int i = 1;
		
		while(running) {	
			fromIntHostPacket = new DatagramPacket(buf.array(), buf.capacity());
			try {
				serverSock.receive(fromIntHostPacket);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
			receivedData = buf.array();
			
			System.out.println("Server received packet " + i);
			System.out.println("From host: " + fromIntHostPacket.getAddress());
			System.out.println("Host port: " + fromIntHostPacket.getPort());
			int len = buf.capacity();
			System.out.println("Length: " + len + "bytes");
			
			int j = 1;
			
			while(receivedData[j] != 0) {
				j++;
			}
			
			filename = new String(receivedData, 2, j);
			
			int k = j+1;
			while(receivedData[k] != 0) {
				k++;
			}
			mode = new String(receivedData, j, k);
			
			System.out.print("Containing filename: "+filename+"\nmode: "+mode);
			String s = new String(buf.array(),0,buf.capacity());
			String t = Arrays.toString(buf.array());
			System.out.println("Output as:\nString: "+ s + "\nBytes: " + t+"\n");
			
			byte[] response;
			
			if((receivedData[0]==0) && (receivedData[1]==1)) { //read request
				response = new byte[]{0,3,0,1};//if read request send back 0 3 0 1 (exactly four bytes)
			}
			
			else if((receivedData[0]==0) && (receivedData[1]==2)) { //write request
				response = new byte[]{0,4,0,0};//if write request send back 0 4 0 0 (also exactly four bytes)
			}
			
			else {
				throw new IOException();
			}
			
			toIntHostPacket = new DatagramPacket(response, response.length,fromIntHostPacket.getAddress(),fromIntHostPacket.getPort());

			System.out.println("Server sending packet " + i);//print response packet info
			System.out.println("To host: " + fromIntHostPacket.getAddress());
			System.out.println("Destination Host port: " + fromIntHostPacket.getPort());
			len = response.length;
			System.out.println("Length: " + len + "bytes");
			System.out.println("Containing filename: "+filename+"\nmode: "+mode);
			t = Arrays.toString(buf.array());
			System.out.println("Output as:\nString: "+ s + "\nBytes: " + t+"\n");
			
			DatagramSocket sendSocket = new DatagramSocket();//create datagramsocket for this response
			
			try {
				sendSocket.send(toIntHostPacket);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
			sendSocket.close();//send packet from new socket then close socket
			i++;
		}
	}
	
	public static void main(String[] args) throws Exception {
		Server s = new Server();
		System.out.println("Server started...");
		s.serverLoop();
	}
}
