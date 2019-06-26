package SIP.socket;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import SIP.socket.sipSocket.OutputStream;
import basicServer.Request;

public class SIPSocket extends Request{
	private DatagramSocket out_socket;
	private DatagramPacket in_socket;
	private OutputStream out;
	private BufferedReader in;
	
	public SIPSocket(DatagramPacket pac, DatagramSocket serverSocket) {
		super(null);
		this.in_socket = pac;
		this.out_socket = serverSocket;
	}
	@Override
	public BufferedReader getInputStream() throws IOException {
		// TODO Auto-generated method stub
		 if(in == null) {
			 in =  new BufferedReader( 
					 new InputStreamReader(
						 new ByteArrayInputStream(this.in_socket.getData(),0,this.in_socket.getLength())
						 ,
						 "UTF-8"
					 )
				 );
		 }
		return in;
	}
	public BufferedOutputStream getOut() throws Exception{
		if(out == null) {
			out = new OutputStream(out_socket, in_socket);
		}
		
		return out;
	}
	public void close() {
		
	}
	
	public InetAddress getAddress() {
		return in_socket.getAddress();
	}
	public int getPort() {
		return in_socket.getPort();
	}
	public DatagramSocket getServer() {
		return out_socket;
	}
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return new String(in_socket.getData(), 0, in_socket.getLength());
	}

}
