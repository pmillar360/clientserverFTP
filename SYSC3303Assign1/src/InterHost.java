import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Arrays;
/*
 * Receives a message from the client in format 01(or 02)<filename>0<mode>0
 * and passes that message on to the server.
 * The server then sends back a 4 byte response that is passed on to the client
 */
public class InterHost {
	private DatagramSocket sendSock, receiveSock, sendAndReceiveSock;
	private DatagramPacket toClientPacket, fromClientPacket, toServerPacket, fromServerPacket;
	private ByteBuffer buf;
	private boolean running;
	private String filename;
	private String mode;
	private byte[] receivedData;
	
	public InterHost() {
		running = true;
		try {
			receiveSock = new DatagramSocket(68);
			sendAndReceiveSock = new DatagramSocket();
		} catch (SocketException se) {
			se.printStackTrace();
			System.exit(1);
		}
		buf = ByteBuffer.allocate(100);
		running = true;
	}

	public void sendAndReceive() throws Exception {
		int i = 1;
		
		while(running) {
			fromClientPacket = new DatagramPacket(buf.array(), buf.capacity());
			
			try {
				receiveSock.receive(fromClientPacket); // receive packet from client
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
			receivedData = buf.array();
			
			System.out.println("Intermediate Host received packet "+i); //print out packet info
			System.out.println("From client: " + fromClientPacket.getAddress());
			System.out.println("Client port: " + fromClientPacket.getPort());
			int len = fromClientPacket.getLength();
			System.out.println("Length: " + len + " bytes");
			
			int j = 1;
			while(receivedData[j] != 0) { //locate strings for filename and mode
				j++;
			}
			filename = new String(receivedData,2,j);
			
			int k = j+1;
			while(receivedData[k] !=0) {
				k++;
			}
			mode = new String(buf.array(),j,k);

			System.out.println("Containing filename: "+filename+"\nmode: "+mode);
			String s = new String(buf.array(),0,buf.capacity());
			String t = Arrays.toString(buf.array());
			System.out.println("Output as:\nString: "+ s + "\nBytes: " + t +"\n");
			buf.clear();	
			
			try {
				toServerPacket = new DatagramPacket(receivedData,receivedData.length,69);//form new packet to server
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
			
			try {
				sendAndReceiveSock.send(toServerPacket); //send same packet to server
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
			
			System.out.println("Intermediate Host Packet "+i+" sent"); //print packet details sent
			System.out.println("To host: " + toServerPacket.getAddress());
			System.out.println("Destination host port: " + toServerPacket.getPort());
			len = toServerPacket.getLength();
			System.out.println("Length: " + len + " bytes");
			System.out.println("Containing:\nFilename: "+filename+"\nMode: "+mode);
			s = new String(receivedData,0,receivedData.length);
			t = Arrays.toString(buf.array());
			System.out.println("Output as:\nString: "+ s + "\nBytes: " + t + "\n");
			
			buf.clear();
			
			fromServerPacket = new DatagramPacket(buf.array(), buf.capacity());//wait for response
			
			try {
				sendAndReceiveSock.receive(fromServerPacket);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
			
			System.out.println("Intermediate Host received response packet " + i);//print out received info
			System.out.println("From server: " + fromServerPacket.getAddress());
			System.out.println("Server port: " + fromServerPacket.getPort());
			len = fromServerPacket.getLength();
			System.out.println("Length: " + len + " bytes");
			System.out.println("Containing:\nBytes: " + Arrays.toString(buf.array())+"\n");
			
			toClientPacket = new DatagramPacket(buf.array(),buf.capacity());//create packet to send back to client 
			
			sendSock = new DatagramSocket();
			
			try {
				sendSock.send(toClientPacket); //send server's response back to client
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
			
			System.out.println("Intermediate Host sending packet " + i + " to Client");
			System.out.println("From intermediate host: " + toClientPacket.getAddress());
			System.out.println("Intermediate host port: " + toClientPacket.getPort());
			len = buf.capacity();
			System.out.println("Length: " + len + "bytes");
			System.out.println("Containing:\nBytes: "+Arrays.toString(buf.array())+"\n");
			sendSock.close();
			i++;
		}
	}
	
	public static void main(String[] args) throws Exception {
		InterHost ih = new InterHost();
		System.out.println("Intermediate Host started...");
		ih.sendAndReceive();
	}
}