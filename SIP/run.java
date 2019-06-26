package SIP;


import java.io.File;

import SIP.server.RequestProcessor;
import SIP.server.ServerSock;
import SIP.socket.SIP_serverSocket;
import UDP.ServerSocket;
import android.util.Log;

public class run {

	public static void main(String[] args) throws Exception{

		System.setErr(System.out);
		SIP_serverSocket serv = new SIP_serverSocket();
		
		
		/**
		File root = new File("C:\\Users\\Joshua\\Google Drive\\program stuff\\starmatic\\publicFIlesDirectory");
		
		RequestProcessor req = new RequestProcessor();
		ServerSock serv = new ServerSock(req,root);
		serv.startServer();
		**/
		
		
		/**
		Socket s = new Socket("sip5060.net"	, 5060);
		OutputStream out = s.getOutputStream();
		FileInputStream in = new FileInputStream(new File("C:\\Users\\Joshua\\Downloads\\SIP\\Request01.txt"));
		byte[] b = new byte[1024];
		int howmany;
		while( (howmany = in.read(b))>0) {
			out.write(b,0,howmany);
		}
		in.close();
		out.write("\r\n".getBytes());
		

		BufferedReader bufin =  new BufferedReader( new InputStreamReader(s.getInputStream()) );
		String st;
		while( (st = bufin.readLine()) !=null ) {
			Log.i("line", st);
		}
		
		**/
		
		
		
	}
}
