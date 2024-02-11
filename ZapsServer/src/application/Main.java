package application;

import java.net.InetAddress;
import java.net.UnknownHostException;

import dados.Grupo;

public class Main extends Thread {
	public  Unpacker unpacker;
	public  String localhost;
	public  Receiver receptor;
	public  Grupo grupo;
	
	@Override
	public void run() {
		
		InetAddress ip = null;
		
		try {
			ip = InetAddress.getLocalHost();
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		localhost = ip.getHostAddress();
		
		//unpacker = new Unpacker();
	    //unpacker.start();
		receptor = new Receiver();
		receptor.start();
		
	}
}
