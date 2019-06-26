package SIP.server;

import java.io.File;
import java.io.IOException;

import SIP.socket.SIP_Secure_serverSocket;
import SIP.socket.SIP_serverSocket;
import basicServer.ProcessRequest;
import basicServer.RequestsHandler;
import basicServer.serverSock.Http;
import basicServer.serverSock.Https;

public class ServerSock extends basicServer.ServerSock{
	private String trustStoreDir = "C:\\Users\\Joshua\\Google Drive\\program stuff\\music xml\\website\\SSL\\joshuarabanal.info\\joshuarabanal.info.jks",
			trustStorePass = "test12345";

	public ServerSock(ProcessRequest arg0, File arg1) {
		super(arg0, arg1);
		// TODO Auto-generated constructor stub
	}
	
	@Override protected Https initializeHTTPS(RequestsHandler requests, String ssFileDir, String sslPass) throws IOException {
		return new SIP_Secure_serverSocket(requests, trustStoreDir, trustStorePass);
    	
	}
	@Override protected Http initializeHTTP(RequestsHandler requests) throws IOException {
		//return new SIP_serverSocket(requests);
		throw new UnsupportedOperationException("this whole class should be deleted");
	}

}
