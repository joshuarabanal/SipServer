package SIP.socket.accounts;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import SIP.server.SIPHelperFunctions;
import SIP.socket.SIPSocket;
import SIP.socket.sipSocket.OutputStream;
import android.util.Log;
import basicServer.Request;

public class SocketHolder   {
	private String accountName;
	private InetAddress callRecieverAddress;
	private int callRecieverPort;
	private DatagramSocket server;
	
	//current call information
	private String currentCallId;
	private InetAddress callSenderAddress;
	private int callSenderPort;
	/** the request for the other account trying to contact(call) this account **/
	private SIPSocket callerRequest;
	private int lastCallStep = -1;
	private static final int INVITE = 0, RINGING = 180, TRYING = 100;
	
	
	private Thread t;
	
	public SocketHolder(String accountName, SIPSocket r) throws Exception {
		server = (r).getServer();
		this.accountName = accountName;
		addRequest(r);
	}


	public void ok(Request r) throws Exception {
		if(lastCallStep == RINGING) {//the callee has accepted the call
			OutputStream out = new OutputStream(server,callSenderAddress, callSenderPort);
			SIPHelperFunctions.copyRequestToAnnother(r, out);
		}
		else{
			Log.i("cannot handle this message", r.getFirstHeader());
			new Exception().printStackTrace();
			System.exit(-1);
		}
	}
	private void cancel(SIPSocket sender) throws Exception {
		if(lastCallStep == INVITE || lastCallStep == RINGING) {
			copyRequestToOtherParty(sender);
		}
		else {
			new Exception("this has never happened before").printStackTrace();
			System.exit(-1);
		}
	}
	private void copyRequestToOtherParty(SIPSocket sender) throws Exception {
		OutputStream out;
		if( sender.getPort() == callSenderPort && sender.getAddress().equals(callSenderAddress) ) {
			out = new OutputStream(server,callRecieverAddress, callRecieverPort);
			SIPHelperFunctions.copyRequestToAnnother(sender, out);
		}
		else if( sender.getPort() == callRecieverPort  && sender.getAddress().equals( callRecieverAddress ) ) {
			out = new OutputStream(server, callSenderAddress, callSenderPort);
			SIPHelperFunctions.copyRequestToAnnother(sender, out);
		}
		else {
			new Exception("this has never happened before").printStackTrace();
			System.exit(-1);
		}
	}
	private void ringing(Request sender) throws Exception {
		lastCallStep = RINGING;
		copyRequestToOtherParty((SIPSocket) sender);
	}
	private void publish(Request r) throws IOException, Exception {
		BufferedOutputStream out = r.getOut();
		if(r.getHeaderByName("Content-Type").equals("application/pidf+xml")) {
			SIPHelperFunctions.firstResponseLine(out,"200 OK");//SIP/2.0 200 OK
			SIPHelperFunctions.reitorateHeader(r, "Via");//Via: SIP/2.0/UDP there.com:5060
			SIPHelperFunctions.reitorateHeader(r, "From");//From: LittleGuy <sip:UserB@there.com>
			SIPHelperFunctions.reitorateHeader(r, "To");//To: LittleGuy <sip:UserB@there.com>
			SIPHelperFunctions.reitorateHeader(r, "Call-ID");//Call-ID: 123456789@there.com
			SIPHelperFunctions.reitorateHeader(r, "CSeq");//CSeq: 1 REGISTER
			SIPHelperFunctions.writeNameValuePair(r, "SIP-ETag", "qwi982ks");//create an e-tag for the new state data
			SIPHelperFunctions.writeNameValuePair(r, "Expires", "3600");//this etag expires in 3600 seconds
			SIPHelperFunctions.writeNameValuePair(out, "Content-Length","0");//Content-Length: 0
			SIPHelperFunctions.writeLN(out);
			out.flush();
			r.resetForNextRequest();
		}
		else {
			Log.i("un handled request", r.getFirstHeader());
			System.exit(-1);
		}
	}
	public void addRequest(SIPSocket r) throws Exception {
		String method = r.getMethodAsString();

		System.out.println();
		System.out.println();
		String firstHeader = r.getFirstHeader();
		if(firstHeader == null) {
			//null packet
			Log.i("null packet", r.getFirstHeader());
		}
		else if(firstHeader.contains(" 180 ")) { ringing(r); }//180 ringing
		else {
			Log.i("socket holder.addRequest", "uname:"+accountName+", method:"+method+", first header:"+r.getFirstHeader());
			//r.logValues();
			switch(method) {
				case "REGISTER":
					register(r);
					break;
				case "OPTIONS":
					OPTIONS(r);
					break;
					
				case "PUBLISH":
					publish(r);
					break;
					
				case "CANCEL":
					cancel(r);
					break;
					
				default:
					Log.e("un handled request", r.getFirstHeader()+" = "+r.getMethod());
					r.logValues();
					
					
			}
		}
		r.close();
		r = null;

		System.out.println();
		System.out.println();
		System.out.println();
		System.out.println();
		System.out.println();
		System.out.println();
	}
	
	/**
	 * invites are always sent to the account ascociated with the "To" header
	 * @param r
	 * @throws Exception
	 */
	public void addInvite(Request r) throws Exception {

		String uname =  r.getHeaderByName("To");//To: LittleGuy <sip:UserB@there.com>
				uname = uname.substring(uname.indexOf("<sip:")+ ("<sip:").length(), uname.indexOf("@"));
		if(!uname.equals(this.accountName)) {
			Log.i("from", r.getHeaderByName("From"));
			Log.i("To", r.getHeaderByName("To"));
			Log.i("this account", this.accountName);
			new Exception("call forwarded to wrong account, you must push invites to the account in the \"To\" header") .printStackTrace();
			System.exit(-1);
		}
		
		
		this.currentCallId = r.getHeaderByName("Call-ID");
		this.callerRequest = ((SIPSocket)r);
		this.callSenderAddress = callerRequest.getAddress();
		this.callSenderPort = callerRequest.getPort();
		inviteRequest();
		
	}
	
	
	private String via,url,From,To,CallID, CSeq, Contact, ContactName, ContentLength, ContentType, body;
	private void inviteRequest() throws Exception {
		Log.i("invite request", "invite request");
		lastCallStep = INVITE;
		callerRequest.logValues();
		
		 		via = callerRequest.getHeaderByName("Via");
				url = callerRequest.getURL();
				From = callerRequest.getHeaderByName("From");
				To = callerRequest.getHeaderByName("To");
				CallID = callerRequest.getHeaderByName("Call-ID");
				CSeq = callerRequest.getHeaderByName("CSeq");
				Contact = callerRequest.getHeaderByName("Contact");
				if(Contact != null && Contact.contains("<") && Contact.contains(">")) {
					ContactName = Contact.substring(Contact.indexOf("<")+1, Contact.indexOf(">"));
				}
				else {
					Log.i("malformed contact field", Contact);
				}
				ContentLength = callerRequest.getHeaderByName("Content-Length");
				ContentType = callerRequest.getHeaderByName("Content-Type");
				body = callerRequest.getData();
		

		System.out.println();
		System.out.println();
		System.out.println();
		System.out.println();
		Log.i("starting new invite", To);
		
		//tell the call maker that we are attempting to connect
			BufferedOutputStream inviteOut = callerRequest.getOut();
			SIPHelperFunctions.firstResponseLine(inviteOut, "100 Trying");//SIP/2.0 100 Trying
			SIPHelperFunctions.writeNameValuePair(inviteOut, "Via", "SIP/2.0/UDP joshuarabanal.info:5060");//Via: SIP/2.0/UDP here.com:5060
			SIPHelperFunctions.writeNameValuePair(inviteOut, "From", From);//From: BigGuy <sip:UserA@here.com>
			SIPHelperFunctions.writeNameValuePair(inviteOut, "To", To);//To: LittleGuy <sip:UserB@there.com>
			SIPHelperFunctions.writeNameValuePair(inviteOut, "Call-ID", CallID);//Call-ID: 12345601@here.com
			SIPHelperFunctions.writeNameValuePair(inviteOut, "CSeq",CSeq);//CSeq: 1 INVITE
			SIPHelperFunctions.writeNameValuePair(inviteOut, "Content-Length", "0");//Content-Length: 0
			SIPHelperFunctions.writeLN(inviteOut);
			inviteOut.flush();
			callerRequest.resetForNextRequest();
			
			OutputStream out = new OutputStream(server,callRecieverAddress, callRecieverPort);
			//send the call invite to the recieving client
			Log.i("sending invite to client", accountName);
		 	out.write(("INVITE "+url+" SIP/2.0\r\n").getBytes());//INVITE sip:UserB@there.com SIP/2.0
		 	SIPHelperFunctions.writeNameValuePair(out, "Via", via);//Via: SIP/2.0/UDP ss1.wcom.com:5060;branch=2d4790.1
		 	SIPHelperFunctions.writeNameValuePair(out, "Via", "SIP/2.0/TCP joshuarabanal.info:5060");//Via: SIP/2.0/UDP here.com:5060
		 	SIPHelperFunctions.writeNameValuePair(out, "Record-Route", "<sip:"+accountName+"@joshuarabanal.info>");//Record-Route: <sip:UserB@there.com;maddr=ss1.wcom.com>
		 	SIPHelperFunctions.writeNameValuePair(out, "From", From);//From: BigGuy <sip:UserA@here.com>
		 	SIPHelperFunctions.writeNameValuePair(out, "To", To);//To: LittleGuy <sip:UserB@there.com>
		 	SIPHelperFunctions.writeNameValuePair(out, "Call-ID", CallID);//Call-ID: 12345601@here.com
		 	SIPHelperFunctions.writeNameValuePair(out, "CSeq", CSeq);//CSeq: 1 INVITE
		 	SIPHelperFunctions.writeNameValuePair(out, "Contact", Contact);//Contact: <sip:UserA@100.101.102.103>
		 	SIPHelperFunctions.writeNameValuePair(out, "Content-Type", ContentType);//Content-Type: application/sdp
		 	SIPHelperFunctions.writeNameValuePair(out, "Content-Length", ContentLength);//Content-Length: 147
			SIPHelperFunctions.writeLN(out);
		 	if(!ContentLength.equals("0")) {
		 		out.write(body.getBytes());
		 	}
		 	out.flush();
		 	
		 	/**
		 	if(true) { return; }
		 	
		 	if(r.getFirstHeader() == null) {
		 		Log.i("blank response from client", "logging whole response");
		 		r.logValues();
		 		System.exit(-1);
		 	}
		 	if(r.getFirstHeader().contains("Ringing")) {
		 		Log.i("call reciever is ringing", "sending ringing to the caller");
		 		SIPHelperFunctions.copyRequestToAnnother(r,inviteRequest);//send the ringing response to the client
		 		inviteOut.flush();
			 	inviteRequest.resetForNextRequest();
			 	
			 	if(r.getFirstHeader().contains("486 Busy here")) {//call rejected
			 		Log.i("call was rejected", r.getFirstHeader());
			 		
			 		//acknowlege to the recieving client that the call is canceled
			 		r.getOut().write(("ACK "+ContactName+" SIP/2.0").getBytes());//ACK sip:UserB@110.111.112.113 SIP/2.0
			 		SIPHelperFunctions.writeNameValuePair(r.getOut(), "From", From);//From: BigGuy <sip:UserA@here.com>
			 		SIPHelperFunctions.writeNameValuePair(r.getOut(), "To", To);//To: LittleGuy <sip:UserB@there.com>;tag=314159
			 		SIPHelperFunctions.writeNameValuePair(r.getOut(), "Call-ID", CallID);//Call-ID: 12345600@here.com
			 		SIPHelperFunctions.writeNameValuePair(r.getOut(), "CSeq", CSeq);//CSeq: 1 ACK
			 		SIPHelperFunctions.writeNameValuePair(r.getOut(), "Content-Length", ContentLength);//Content-Length: 0
			 		SIPHelperFunctions.writeLN(r.getOut());
			 		r.getOut().flush();
			 		
			 		SIPHelperFunctions.copyRequestToAnnother(r, inviteRequest);//tell the call initiator that the session has ended
			 		
			 	}
			 	
			 	Log.i("next request ", r.getFirstHeader());
			 	r.logValues();
			 	r.resetForNextRequest();
			 	
			 	System.out.println();
			 	System.out.println();
			 	System.out.println();

		 	}
		 	else {
		 		Log.i("caller did not ring as response", r.getFirstHeader());
		 	}
		 	Log.i("response to invite", r.getFirstHeader());
		 	r.logValues();
		 	r.close();
		 	r = null;
		 	inviteRequest.close();
		 	inviteRequest = null;

		 	Thread.sleep(2000);
			System.exit(-1);
		 	**/
		 	
	}
	
	private void OPTIONS(Request r) throws Exception {
		
		BufferedOutputStream out = r.getOut();
		SIPHelperFunctions.firstResponseLine(out,"200 OK");//SIP/2.0 200 OK
		SIPHelperFunctions.reitorateHeader(r, "Via");//Via: SIP/2.0/UDP there.com:5060
		SIPHelperFunctions.reitorateHeader(r, "From");//From: LittleGuy <sip:UserB@there.com>
		SIPHelperFunctions.reitorateHeader(r, "To");//To: LittleGuy <sip:UserB@there.com>
		SIPHelperFunctions.reitorateHeader(r, "Call-ID");//Call-ID: 123456789@there.com
		SIPHelperFunctions.reitorateHeader(r, "CSeq");//CSeq: 1 REGISTER
		SIPHelperFunctions.writeNameValuePair(out, "Content-Length","0");//Content-Length: 0
		SIPHelperFunctions.writeLN(out);
		out.flush();

		r.resetForNextRequest();
	}
	private boolean authenticate(String authorization) throws NoSuchAlgorithmException {
		if(authorization == null) { return false; }
		if(true) { return true; }
		Log.i("authorization", authorization);
		Log.i("nonce", "ea9c8e88df84f1cec4341ae6cbe5a359");
		Log.i("realm", "CallCenterLLCphones");
		new Exception().printStackTrace();
		MessageDigest m = MessageDigest.getInstance("MD5");
		byte[] Ha1 = m.digest(("me:CallCenterLLCphones:SuckMyDick").getBytes());//username:realm:password
		System.exit(-1);
		return true;
		
	}
	/**
	 * resonds to register requests to log in
	 * @param r
	 * @throws Exception 
	 */
	private void register(Request r) throws Exception {
		String uname = r.getHeaderByName("From");
		if(uname == null) { uname = r.getHeaderByName("from"); }
		uname = uname.substring(uname.indexOf('<')+1, uname.indexOf('>'));
		if(uname.contains(";")) { uname = uname.substring(0,uname.indexOf(';')); }
		Log.i("Register request", "username:"+uname);
		String authentication = r.getHeaderByName("Authorization");
		BufferedOutputStream out = r.getOut();
		if( !authenticate(authentication)) {//if the user is not authenticated
			ArrayList<String> registers = r.getAllHeadersWithName("Contact");
			
			Log.i("register request", "user needs to authenticate");
			SIPHelperFunctions.firstResponseLine(out,"401 Unauthorized");//SIP/2.0 401 Unauthorized
			SIPHelperFunctions.reitorateHeader(r, "Via");//Via: SIP/2.0/UDP there.com:5060
			SIPHelperFunctions.reitorateHeader(r, "From");//From: LittleGuy <sip:UserB@there.com>
			SIPHelperFunctions.reitorateHeader(r, "To");//To: LittleGuy <sip:UserB@there.com>
			SIPHelperFunctions.reitorateHeader(r, "Call-ID");//Call-ID: 123456789@there.com
			SIPHelperFunctions.reitorateHeader(r, "CSeq");//CSeq: 1 REGISTER
			String digest = "Digest "+ 
					"realm=\"CallCenterLLCphones\", "+
					"domain=\"sip:joshuarabanal.info\", "+
					"nonce=\"ea9c8e88df84f1cec4341ae6cbe5a359\", "+
					"opaque=\"\", "+
					"stale=FALSE, "+
					"algorithm=MD5";
			
			SIPHelperFunctions.writeNameValuePair(out, "WWW-Authenticate", digest);
				//Digest realm="MCI WorldCom SIP",domain="sip:ss2.wcom.com", nonce="ea9c8e88df84f1cec4341ae6cbe5a359", opaque="", stale=FALSE, algorithm=MD5
			SIPHelperFunctions.writeNameValuePair(out, "Content-Length","0");//Content-Length: 0
			SIPHelperFunctions.writeLN(out);
			out.flush();//need to flush all of the packet to finalize it being sent
			
			Log.i("told client to authenticate before registering", digest);
			return;
			/**
			r.resetForNextRequest();
			Log.i("responded to request", r.getMethodAsString());
			authentication = r.getHeaderByName("Authorization");
			Log.i("responded to login", authentication);
			r.logValues();
			System.out.println();
			**/
		}
		else {
		
				Log.i("register request", "with authentication:"+authentication);
		
				if(r instanceof SIPSocket) {
					this.callRecieverAddress = ((SIPSocket) r).getAddress();
					this.callRecieverPort = ((SIPSocket) r).getPort();
				}
				//request fully authenticated
				String contact = r.getHeaderByName("Contact");
				SIPHelperFunctions.firstResponseLine(out,"200 OK");//SIP/2.0 200 OK
				SIPHelperFunctions.reitorateHeader(r, "Via");//Via: SIP/2.0/UDP there.com:5060
				SIPHelperFunctions.reitorateHeader(r, "From");//From: LittleGuy <sip:UserB@there.com>
				SIPHelperFunctions.reitorateHeader(r, "To");//To: LittleGuy <sip:UserB@there.com>
				SIPHelperFunctions.reitorateHeader(r, "Call-ID");//Call-ID: 123456789@there.com
				SIPHelperFunctions.reitorateHeader(r, "CSeq");//CSeq: 1 REGISTER
				if(contact == null) {//if the user is requesting the contact list be updated 
					SIPHelperFunctions.writeNameValuePair(r, "Contact", "<sip:you@joshuarabanal.info>;expires=4294967295");
				}
				SIPHelperFunctions.writeNameValuePair(out, "Content-Length","0");//Content-Length: 0
				SIPHelperFunctions.writeLN(out);
				out.flush();
		}
	}

	public boolean equals(String username) {
		if(this.accountName.equals(username)) {
			return true;
		}
		else {
			return false;
		}
	}
}
