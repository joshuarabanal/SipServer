package SIP.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;

import android.util.Log;
import basicServer.Request;

public class SIPHelperFunctions {
	
	//private helpers
	
	public static void copyRequestToAnnother(Request from, Request to) throws Exception { copyRequestToAnnother(from, to.getOut()); }
	public static void copyRequestToAnnother(Request from, BufferedOutputStream out) throws Exception {
		ArrayList<String> headers = new ArrayList<String>();
		from.getHeaders( headers);
		
		for(String header: headers) {
			out.write(header.getBytes());
			writeLN(out);
		}
		writeLN(out);
		if(from.getData()!=null) {
			out.write(from.getData().getBytes());
		}
		out.flush();
		
		
	}
	public static void writeLN(BufferedOutputStream out) throws IOException { out.write(("\r\n").getBytes()); }
	public static void firstResponseLine(BufferedOutputStream out, String message) throws IOException {
		
		out.write(("SIP/2.0 "+message).getBytes());
		writeLN(out);
	}
	public static void writeNameValuePair(Request r, String name, String value) throws IOException, Exception {
		writeNameValuePair(r.getOut(), name, value);
	}
	public static void writeNameValuePair(BufferedOutputStream out, String name, String value) throws IOException {
		out.write((name+": "+value).getBytes());
		writeLN(out);
	}
	public static void reitorateHeader(Request r, String header) throws Exception {
		String value = r.getHeaderByName(header);
		if(value == null) { value = r.getHeaderByName( header.toLowerCase()); } //TODO remove this as it is only for debugging purposes
		if(value != null) {
			writeNameValuePair(r.getOut(), header, value);
		}
	}
	
	
	
	//public functions
	
	public static void OPTIONS(Request r) throws Exception {
		BufferedOutputStream out = r.getOut();
		
		firstResponseLine(out,"200 OK");//SIP/2.0 200 OK
		reitorateHeader(r, "Via");//Via: SIP/2.0/UDP there.com:5060
		reitorateHeader(r, "From");//From: LittleGuy <sip:UserB@there.com>
		reitorateHeader(r, "To");//To: LittleGuy <sip:UserB@there.com>
		reitorateHeader(r, "Call-ID");//Call-ID: 123456789@there.com
		reitorateHeader(r, "CSeq");//CSeq: 1 REGISTER
		writeNameValuePair(out, "Content-Length","0");//Content-Length: 0
		writeLN(out);
		out.flush();
		
		r.resetForNextRequest();
		if(r.getMethodAsString() == null) {
			r.close();
			return;
		}
		else if(r.getMethodAsString().equals("OPTIONS")){
			OPTIONS(r);
			return;
		}
		Log.i("final unknown request", r.getMethodAsString());
		r.logValues();
		r.close();
		
	}
	public static void Invite_to_Ok(Request r, String contactURI ) throws Exception {
		BufferedOutputStream out = r.getOut();
		firstResponseLine(out,"200 OK");//SIP/2.0 200 OK
		reitorateHeader(r, "From");
		reitorateHeader(r, "To");
		reitorateHeader(r, "Call-ID");
		reitorateHeader(r,"CSeq");
		writeNameValuePair(r.getOut(), "Content-Type", "application/sdp");
		writeNameValuePair(r.getOut(), "Contact", "<sip:"+contactURI+">");
		writeLN(out);
		out.flush();
		
		
		
		r.close();
	}
	
	public static void Register_to_Authenticate(Request r) throws Exception {
		String uname = r.getHeaderByName("From");
		if(uname == null) { uname = r.getHeaderByName("from"); }
		uname = uname.substring(uname.indexOf('<')+1, uname.indexOf('>'));
		if(uname.contains(";")) { uname = uname.substring(0,uname.indexOf(';')); }
		Log.i("Register request", "username:"+uname);
		
		BufferedOutputStream out = r.getOut();
		firstResponseLine(out,"401 Unauthorized");//SIP/2.0 401 Unauthorized
		reitorateHeader(r, "Via");//Via: SIP/2.0/UDP there.com:5060
		reitorateHeader(r, "From");//From: LittleGuy <sip:UserB@there.com>
		reitorateHeader(r, "To");//To: LittleGuy <sip:UserB@there.com>
		reitorateHeader(r, "Call-ID");//Call-ID: 123456789@there.com
		reitorateHeader(r, "CSeq");//CSeq: 1 REGISTER
		String digest = "Digest "+ 
				"realm=\"CallCenterLLCphones\", "+
				"domain=\"sip:joshuarabanal.info\", "+
				"nonce=\"ea9c8e88df84f1cec4341ae6cbe5a359\", "+
				"opaque=\"\", "+
				"stale=FALSE, "+
				"algorithm=MD5";
		
		writeNameValuePair(out, "WWW-Authenticate", digest);
			//Digest realm="MCI WorldCom SIP",domain="sip:ss2.wcom.com", nonce="ea9c8e88df84f1cec4341ae6cbe5a359", opaque="", stale=FALSE, algorithm=MD5
		writeNameValuePair(out, "Content-Length","0");//Content-Length: 0
		writeLN(out);
		out.flush();//need to flush all of the packet to finalize it being sent
		
		
		r.resetForNextRequest();
		Log.i("responded to request", r.getMethodAsString());
		Log.i("responded to login", r.getHeaderByName("Authorization"));
		
		
		firstResponseLine(out,"200 OK");//SIP/2.0 200 OK
		reitorateHeader(r, "Via");//Via: SIP/2.0/UDP there.com:5060
		reitorateHeader(r, "From");//From: LittleGuy <sip:UserB@there.com>
		reitorateHeader(r, "To");//To: LittleGuy <sip:UserB@there.com>
		reitorateHeader(r, "Call-ID");//Call-ID: 123456789@there.com
		reitorateHeader(r, "CSeq");//CSeq: 1 REGISTER
		writeNameValuePair(out, "Content-Length","0");//Content-Length: 0
		writeLN(out);
		out.flush();
		
		
		r.resetForNextRequest();
		if(r.getMethodAsString().equals("OPTIONS")) {
			OPTIONS(r);
			return;
		}
		else {
			Log.i("unknown request", "unknown request");
			r.logValues();
			r.close();
			throw new Exception("unknown response");
		}
		 
	}

}
