package SIP.server;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.ArrayList;

import SIP.socket.SIPSocket;
import SIP.socket.accounts.SocketHolder;
import android.util.Log;
import basicServer.ProcessRequest;
import basicServer.Request;
import requetsHandler.HandleSingleRequest;

public class RequestProcessor   {
	public ArrayList<SocketHolder> accounts = new ArrayList<SocketHolder>();

	
	
	public void addRequest(SIPSocket sock){
        new Thread(
        		new HandleSingleRequest(
        				sock
        			)
        		)
        .start();
	}
	private class HandleSingleRequest implements Runnable{
		private SIPSocket sock;
		public HandleSingleRequest(SIPSocket sock) {
			this.sock = sock;
		}
		
	@Override
    public void run() {
        try{
			processRequest(sock);
		}
		catch(Exception e){
			e.printStackTrace();
			Log.i("ending in debug mode", "finished");
			System.exit(-1);
		} 
		//request.close();
    }
	}

	public int processRequest(SIPSocket r) throws Exception {
		String header = r.getFirstHeader();
		if(header == null) {
			if(r.toString().trim().length()>0) {
				Log.i("null request", r.toString());
				System.out.println();
				System.out.println();
				System.out.println();	
			}
			return 0;
		}

		System.out.println();
		System.out.println();
		System.out.println();
		Log.i("new request", header);
		Log.i("call id", r.getHeaderByName("Call-ID"));
		Log.i("From", r.getHeaderByName("From"));
		
			if(header.contains("REGISTER")){ pushToAccount_To( r); }
			else if(header.contains("INVITE")){ sendInviteToAccount(r);	}
			else if(header.contains("PUBLISH")) { pushToAccount_To(r);	}
			//else if(header.contains(" 100 ")) {	Log.i("ignoring trying message", r.getHeaderByName("Call-ID"));}// routed or trying
			else if(header.contains(" 180" )) { pushToAccount_To(r); }//ringing
			else if(header.contains(" 200 ")) { pushToAccount_To(r); }//200 ok
			else if(header.contains(" 100 ")) { Log.i("ignoring trying", "ignoring trying"); }//trying
			else if(header.contains("CANCEL ")) { pushToAccount_To(r); }
			else if(header.contains("OPTIONS")){
				BufferedOutputStream out = r.getOut();
				SIPHelperFunctions.firstResponseLine(out,"200 OK");//SIP/2.0 200 OK
				SIPHelperFunctions.reitorateHeader(r, "Via");//Via: SIP/2.0/UDP there.com:5060
				SIPHelperFunctions.reitorateHeader(r, "From");//From: LittleGuy <sip:UserB@there.com>
				SIPHelperFunctions.reitorateHeader(r, "To");//To: LittleGuy <sip:UserB@there.com>
				SIPHelperFunctions.reitorateHeader(r, "Call-ID");//Call-ID: 123456789@there.com
				SIPHelperFunctions.reitorateHeader(r, "CSeq");//CSeq: 1 REGISTER
				SIPHelperFunctions.writeNameValuePair(out, "Content-Length","0");//Content-Length: 0
				SIPHelperFunctions.writeLN(out);
				out.close();
			}
			else { 
				Log.i("unable to process request", r.getFirstHeader());
				r.logValues();
				r.close();
				new Exception("bad request:"+header).printStackTrace();
				System.exit(-1);
		}
		return 0;
	}
	private void ok(Request r) {
		
	}
	private void sendInviteToAccount(Request r) throws Exception {
		String uname =  r.getHeaderByName("To");//To: LittleGuy <sip:UserB@there.com>
				uname = uname.substring(uname.indexOf("<sip:")+ ("<sip:").length(), uname.indexOf("@"));
		
		for(SocketHolder account : accounts) {
			if(account.equals(uname)) {
				Log.i("found account", uname);
				account.addInvite(r);
			 	Thread.sleep(2000);
				return;
			}
		}
		
		Log.i("unable to find account", uname);
		
		
		r.logValues();
		r.close();
		throw new IOException("unable to find account");
	}
	private void pushToAccount_From(Request r) throws Exception {
		String uname = r.getHeaderByName("From");
		uname = uname.substring(uname.indexOf("<sip:") +("<sip:").length(), uname.indexOf("@"));
		Log.i("username", uname);
		pushToAccount(r,uname);
	}
	private void pushToAccount_To(Request r) throws Exception {
		String uname = r.getHeaderByName("To");
		/**
		if(!uname.contains("<sip:") || !uname.contains("@")) {
			Log.i("unable to parse value", "unable to parse value");
			r.logValues();
			new Exception().printStackTrace();
			System.exit(-1);
		}
		**/
		uname = uname.substring(uname.indexOf("<sip:") +("<sip:").length(), uname.indexOf("@"));
		Log.i("username", uname);
		pushToAccount(r,uname);
	}
	private void pushToAccount(Request r, String uname) throws Exception {
		SIPSocket request = (SIPSocket) r;
		
		if(!uname.equals("me") && !uname.equals("you") && !uname.equals("them")) {
			Log.i("account doesnt exist", uname);
			r.close();
			return;
		}
		for(SocketHolder account : accounts) {
			if(account.equals(uname)) {
				Log.i("found account", uname);
				account.addRequest(request);
				return;
			}
		}
		Log.i("adding new account", uname);
		accounts.add(new SocketHolder(uname, request));
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public int processRequestl(Request r) {
		try {
			/**
			BufferedReader buf = r.getInputStream();
			char[] c = new char[1024];
			int howMany;
			while( (howMany = buf.read(c))>0 ) {
				Log.i("reading request", new String(c, 0, howMany));
			}
			r.close();
			if(true) {return 0; } 
			**/

			System.out.println();
			System.out.println();
			Log.i("new method", r.getMethodAsString());
			if(r.getMethodAsString() == null) {//null method error
				Log.i("unknown method", r.getMethodAsString());
				r.logValues();
				Log.i("file created of request", ""+r.toFile());
				r.close();
				return 0;
			}
			
			switch(r.getMethodAsString()) {
				case "INVITE":
					INVITE(r);
					break;
					
				case "REGISTER":
					REGISTER(r);
					break;
				case "OPTIONS":
					OPTIONS(r);
					break;
				default:
					Log.i("unknown method", r.getMethodAsString());
					r.logValues();
					Log.i("file created of request", ""+r.toFile());
					r.close();
					break;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
			
	}
	private void OPTIONS(Request r) throws Exception {
		Log.i("new request: OPTIONS", r.toString());
		SIPHelperFunctions.OPTIONS(r);
	}
	private void REGISTER(Request r) throws Exception {
		Log.i("new request: REGISTER", r.toString());
		SIPHelperFunctions.Register_to_Authenticate(r);
		
		
		r.close();
	}
	private int INVITE(Request r) throws Exception {
		// TODO Auto-generated method stub
		if(true) {
			SIPHelperFunctions.Invite_to_Ok(r, "mama mia");
			return 0;
		}
		
		Log.i("request recieved", r.toString());
		r.logValues();
		Log.i("file made from request", ""+r.toFile());
		BufferedOutputStream out = r.getOut();
		SIPHelperFunctions.Invite_to_Ok(r, "i dont know");
		BufferedReader in = r.getInputStream();
		char[] b = new char[1024];
		int howmany;
		while( (howmany = in.read(b)) > 0) {
			Log.i("response", new String(b,0,howmany) );
		}
		r.close();
		return 0;
	}

}
