package application;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

import dados.Grupo;
import dados.Mensagem;

public class GrupoCast extends Thread {
	private InetAddress groupaddr; 
	private MulticastSocket socket;
	private Grupo g;
	
	
	
	
	
	public InetAddress getGroupaddr() {
		return groupaddr;
	}





	public void setGroupaddr(InetAddress groupaddr) {
		this.groupaddr = groupaddr;
	}





	public MulticastSocket getSocket() {
		return socket;
	}





	public void setSocket(MulticastSocket socket) {
		this.socket = socket;
	}





	public Grupo getG() {
		return g;
	}





	public void setG(Grupo g) {
		this.g = g;
	}


	public GrupoCast(Grupo grupo) {
		this.g = grupo;
		
	}


	@Override
	public void run() {
		
		try {
			this.groupaddr = InetAddress.getByName(g.getAddr());
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		InetSocketAddress group = new InetSocketAddress(this.groupaddr, 6789);
		String IfName = null;
		NetworkInterface If = null;
		InetAddress hostAddr = null;
		Enumeration<NetworkInterface> netifs = null;
		
		try {
			 hostAddr = InetAddress.getByName(Application.main.localhost);
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		try {
			this.socket = new MulticastSocket(6789);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			 netifs = NetworkInterface.getNetworkInterfaces();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		while(netifs.hasMoreElements()) {
			NetworkInterface networkInterface = netifs.nextElement();
			Enumeration<InetAddress> inAddrs = networkInterface.getInetAddresses();
			while(inAddrs.hasMoreElements()) {
				InetAddress inAddr = inAddrs.nextElement();
				if (inAddr.equals(hostAddr)) {
					IfName = networkInterface.getName();
				}
			}
		}
		
		try {
			If = NetworkInterface.getByName(IfName);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			this.socket.joinGroup(group, If);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		while(true) {
			
			byte[] buf = new byte[64000];
			DatagramPacket packet = new DatagramPacket(buf,buf.length);
			try {
				this.socket.receive(packet);
				String payload = new String(packet.getData());
				Application.main.unpacker.payloadsDtg.add(payload);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	

	
	
}
