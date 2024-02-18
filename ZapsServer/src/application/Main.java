package application;

import java.net.InetAddress;
import java.net.UnknownHostException;

import dados.Cliente;
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
		
		unpacker = new Unpacker();
	    unpacker.start();
		receptor = new Receiver();
		receptor.start();
		Cliente c1 = new Cliente("172.16.103.11","");
		Cliente c2 = new Cliente("172.16.103.13","");
		Cliente c3 = new Cliente("172.16.103.14","");
		grupo = new Grupo("Larsid","172.16.103.12");
		
		/*Cliente c1 = new Cliente("192.168.0.106","");
		Cliente c2 = new Cliente("192.168.0.108","");
		Cliente c3 = new Cliente("192.168.0.107","");
		grupo = new Grupo("Larsid","192.168.0.109");*/
		grupo.addClient(c1);
		grupo.addClient(c2);
		grupo.addClient(c3);
		System.out.println("Serviço em execução...");
		
	}
}
