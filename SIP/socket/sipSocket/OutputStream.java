package SIP.socket.sipSocket;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import android.util.Log;

public class OutputStream  extends BufferedOutputStream{
	private DatagramSocket serv;
	private File temp;
	private FileOutputStream out;
	private InetAddress address;
	private int port;

	public OutputStream(DatagramSocket src, DatagramPacket out) throws FileNotFoundException, IOException {
		this(src, out.getAddress(), out.getPort());
	}
	public OutputStream(DatagramSocket src,InetAddress addr, int port) throws FileNotFoundException, IOException {
		this(src, File.createTempFile("socket", ".txt"));
		Log.i("new outputStream", addr.toString()+":"+port);
		address = addr;
        this.port = port;
	}
	private OutputStream(DatagramSocket src,File f) throws FileNotFoundException, IOException {
		this(src, new FileOutputStream(f),f);
	}
	private OutputStream(DatagramSocket src, FileOutputStream out, File f) {
		super(out);
		this.serv = src;
		this.out = out;
		this.temp = f;
	}
	
	@Override
	public void write(byte[] b) throws IOException {
		// TODO Auto-generated method stub
		out.write(b);
	}
	
	@Override
	public synchronized void write(byte[] b, int off, int len) throws IOException {
		// TODO Auto-generated method stub
		out.write(b, off, len);
	}
	
	@Override
	public synchronized void write(int b) throws IOException {
		// TODO Auto-generated method stub
		out.write(b);
	}
	
	@Override
	public synchronized void flush() throws IOException {
		// TODO Auto-generated method stub
		super.flush();
		out.close();
		byte[] b = new byte[(int) temp.length()];
		FileInputStream in = new FileInputStream(temp);
		in.read(b);
		in.close();
		Log.i("sending paxcket", new String(b));
		
		
        DatagramPacket packet = new DatagramPacket(b, b.length, address, port);
        serv.send(packet);
        
        temp = File.createTempFile("socket", ".txt");
        out = new FileOutputStream(temp);
	}
	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
		flush();
		out.close();
		
	}
}
