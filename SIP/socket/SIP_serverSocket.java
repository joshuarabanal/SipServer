package SIP.socket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import SIP.server.RequestProcessor;
import android.util.Log;
import basicServer.RequestsHandler;

public class SIP_serverSocket   implements Runnable{
	private DatagramSocket serverSocket = new DatagramSocket(5060);
	private Thread t = new Thread(this);
	private boolean running = false;
	private RequestProcessor processor = new RequestProcessor();
	
	public SIP_serverSocket() throws IOException{
		t.start();
	}

	public void run() {
		// TODO Auto-generated method stub
		running = true;
		
		 
        while (running) {
        	try {
	        	byte[] buf = new byte[65507];
	            DatagramPacket packet 
	              = new DatagramPacket(buf, buf.length);
	            serverSocket.receive(packet);//blocks while we wait for the next message
	            Log.i("new udp packet", "address:"+packet.getAddress()+", port:"+packet.getPort()+", sock address:"+packet.getSocketAddress());
	            
	            processor.addRequest(new SIPSocket(packet, serverSocket));
	            
	            
	            /** 
	            InetAddress address = packet.getAddress();
	            int port = packet.getPort();
	            packet = new DatagramPacket(buf, buf.length, address, port);
	            String received 
	              = new String(packet.getData(), 0, packet.getLength());
	             
	            Log.i("recieved", received);
	            serverSocket.send(packet);
	            **/
        	}
        	catch(Exception e) {
        		e.printStackTrace();
        		System.exit(-1);
        	}
        }
        serverSocket.close();
	}
}
