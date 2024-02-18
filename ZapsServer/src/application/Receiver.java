package application;
import java.net.*;
import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import java.io.*;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import dados.Cliente;
import dados.Grupo;
import dados.Mensagem;


//representa o receptor de pacotes
public class Receiver extends Thread {
	
	public Receiver() {
		// TODO Auto-generated constructor stub
		
	}
	
	@Override
	public  void run() {
		int porta = 7000;
		DatagramSocket serverSocket;
		try {
			serverSocket = new DatagramSocket(porta);
			
			while(true) {
				byte[] receiveData = new byte[4096];
				DatagramPacket receivePacket = new DatagramPacket(receiveData,
						receiveData.length);
				
				serverSocket.receive(receivePacket);
				String data = new String(receivePacket.getData());
				String payload = data.replace("\\u0000", "");
				
				System.out.println("recebi: \n");
				System.out.println(payload+"\n");
				
				String[] params = payload.split(" ");
				
				if(params[0].equals("send")) {
					Cliente c = new Cliente(Application.main.localhost,params[1]);
					Mensagem m = new  Mensagem(params[2],new int [1],c);
					Application.main.grupo.send(m);
				}else if(params.length==1&&params[0].startsWith("input")) {
					String inputs = "		LARSID\n";
					Iterator<Mensagem> i = Application.main.grupo.getMensagens().iterator();
					
					while(i.hasNext()) {
						Mensagem m = i.next();
						inputs += m.getSource().getNome()+":\n"
						+"	 "+m.getBody()
						+"\n";	
					}
					
					byte[] sendData = new byte[4096];
					sendData = inputs.getBytes();
					InetAddress destiny = InetAddress.getLoopbackAddress();
					DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,destiny,9000);
					serverSocket.send(sendPacket);
				}else {
					System.out.println("Não entendi o comando");
				}
				
			
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
	}
	
	
}
