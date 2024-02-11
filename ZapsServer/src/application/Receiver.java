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
				byte[] receiveData = new byte[2048];
				DatagramPacket receivePacket = new DatagramPacket(receiveData,
						receiveData.length);
				
				serverSocket.receive(receivePacket);
			
				String payload = new String(receivePacket.getData());
				
				System.out.println("recebi: \n");
				
				String[] params = payload.split(" ");
				
				if(params[1].equals("send")) {
				//	Mensagem m = new  Mensagem();
				}
				
				for(int i = 0;i<params.length;i++) {
					System.out.println("\n"+params[i]);
				}

			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
	}
	
	
}
