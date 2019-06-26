package SIP.socket;

import java.io.IOException;

import basicServer.RequestsHandler;
import basicServer.serverSock.Https;

public class SIP_Secure_serverSocket extends Https{
	
	public SIP_Secure_serverSocket(RequestsHandler requests, String trustStoreDirectory,String sslPassword) throws IOException{
		super(requests,trustStoreDirectory,sslPassword);
               this.trustStoreDir = trustStoreDirectory;
               this.SSLPassword = sslPassword;
		this.portNum = 5061;
	}

}
