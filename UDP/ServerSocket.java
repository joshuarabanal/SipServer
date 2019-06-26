package UDP;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import android.util.Log;

public class ServerSocket  implements Runnable{
	private DatagramSocket serverSocket;
	private Thread t = new Thread(this);
	private boolean running = false;

	public ServerSocket(int port) throws SocketException {
		serverSocket = new DatagramSocket(port);
		t.start();
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		running = true;
		
		 
        while (running) {
        	try {
	        	byte[] buf = new byte[65507];
	            DatagramPacket packet 
	              = new DatagramPacket(buf, buf.length);
	            serverSocket.receive(packet);//blocks while we wait for the next message
	             
	            InetAddress address = packet.getAddress();
	            int port = packet.getPort();
	            packet = new DatagramPacket(buf, buf.length, address, port);
	            String received 
	              = new String(packet.getData(), 0, packet.getLength());
	             
	            Log.i("recieved", received);
	            serverSocket.send(packet);
        	}
        	catch(Exception e) {
        		e.printStackTrace();
        		System.exit(-1);
        	}
        }
        serverSocket.close();
	}
}
