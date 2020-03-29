package com.xidian.server.data;

import java.io.IOException;
import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

public class JsonMsgSystem implements Runnable{
	private SSLServerSocket serverSocket;
	private SSLSocket clientSocket;
	private boolean isShutdown=false;
	public void run(){
		initSSL();
		serverSocket=getSSLServerSocket();
		while(!isShutdown){
			try{
				synchronized (serverSocket) {
					clientSocket=(SSLSocket)serverSocket.accept();
					//System.out.println("\nLog:"+clientSocket.getInetAddress().getHostAddress()+":"+clientSocket.getPort());
				}
				new Thread(new JsonMsgSystemThread(clientSocket)).start();
			}catch(Exception e){
				e.printStackTrace();
				System.out.println("Log:Server Shutdown!");
				isShutdown=true;
			}
		}
	}
	
	public void initSSL(){
		System.setProperty("javax.net.ssl.keyStore", PackageConstants.keystorePath);  
        System.setProperty("javax.net.ssl.keyStorePassword", PackageConstants.keystorePassword);  
        System.setProperty("javax.net.ssl.trustStore", PackageConstants.trustKeystorePath);  
        System.setProperty("javax.net.ssl.trustStorePassword",PackageConstants.keystorePassword);
	}
	public SSLServerSocket getSSLServerSocket() {
		ServerSocketFactory factory =  SSLServerSocketFactory.getDefault();
		SSLServerSocket serverSocket=null;
		try {
			serverSocket = (SSLServerSocket) factory.createServerSocket(PackageConstants.SERVER_PORT);
			serverSocket.setNeedClientAuth(false);
		} catch (IOException e) {
			e.printStackTrace();
		} 
		return serverSocket;
	}
}

/*//Abandoned Method
System.out.println("Log:Json Server Open\nWaiting for connect...");
try{
	serverSocket=new ServerSocket(PackageConstants.SERVER_PORT);
}catch(Exception e){
	e.printStackTrace();
	return ;
}
while(!isShutdown){
	try{
		synchronized (serverSocket) {
			clientSocket=serverSocket.accept();
			//clientSocket.setSoTimeout(3000);
			System.out.println("\nLog1:"+clientSocket.getInetAddress().getHostAddress()+":"+clientSocket.getPort());
		}
		
		System.out.println("\nLog:"+clientSocket.getInetAddress().getHostAddress()+":"+clientSocket.getPort());
		new Thread(new JsonMsgSystemThread(clientSocket)).start();
	}catch(Exception e){
		e.printStackTrace();
		System.out.println("Log:Server Shutdown!");
		isShutdown=true;
	}
}*/
