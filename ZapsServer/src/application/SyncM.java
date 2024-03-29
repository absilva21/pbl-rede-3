package application;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.net.SocketTimeoutException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import dados.Cliente;
import dados.Grupo;
import dados.Mensagem;
//aqui é sincronizado as mensagens
public class SyncM extends Thread {
	
	private LinkedList<Mensagem> clienteMens;
	private Grupo g;

	public LinkedList<Mensagem> getClienteMens() {
		return clienteMens;
	}

	public void setClienteMens(LinkedList<Mensagem> clienteMens, Grupo grupo) {
		this.clienteMens = clienteMens;
		this.g = grupo;
	}

	public SyncM(LinkedList<Mensagem> ms, Grupo grupo){
		// TODO Auto-generated constructor stub
		this.clienteMens = ms;
		this.g = grupo;
		
	}
	
	@Override
	public void run() {
		Iterator<Mensagem> it = this.clienteMens.iterator();
		LinkedList<int[]> allFouls = new LinkedList<int[]>();
		
		while(it.hasNext()) {
			int[] fouls = new int[0];
			Mensagem men = it.next();
			if(it.hasNext()) {
				Mensagem men2 = it.next();
				int difId = men2.getIdLocal() - men.getIdLocal();
				if(difId>1) {
					fouls = new int[difId-1];
					int index = -1;
					for(int i = men.getIdLocal() + 1;i<men2.getIdLocal();i++){
						index++;
						fouls[index] = i;
					}
					allFouls.add(fouls);
				}else {
					if(men.getIdLocal()>1) {
						fouls = new int[men.getIdLocal()-1];
						int index = -1;
						for(int i = 1;i<men.getIdLocal();i++){
							index++;
							fouls[index] = i;
						}
						allFouls.add(fouls);
					}
				}
			}else if(this.clienteMens.size()==1) {
				if(men.getIdLocal()>1) {
					fouls = new int[men.getIdLocal()-1];
					int index = -1;
					for(int j = 1;j<men.getIdLocal();j++){
						index++;
						fouls[index] = j;
					}
					allFouls.add(fouls);
				}
			}
			else {
				
				break;
			}
		}
		
		if(!allFouls.isEmpty()) {
			JSONArray allFoulsArray = new JSONArray();
			JSONObject JSONack = new JSONObject();
			int idNack = this.g.getIdIndex() + 1;
			this.g.setNackID(idNack);
	
			JSONack.put("type", "com");
			JSONack.put("comNumber", 2);
			JSONack.put("origem", Application.main.localhost);
			JSONack.put("nackID", idNack);
			
			Iterator<int[]> it2 = allFouls.iterator();
			
			while(it2.hasNext()) {
				JSONArray foulArray = new JSONArray();
				int[] fouls = it2.next();
				for(int i = 0;i<fouls.length;i++) {
					foulArray.add(fouls[i]);
				}
				allFoulsArray.add(foulArray);
			}
			
			JSONack.put("fouls", allFoulsArray);
			Cliente localHost = this.g.searchClient(Application.main.localhost);
			int index = localHost.getId();
			JSONack.put("origemID",index);
			String json = JSONack.toJSONString();
			String payload = json.replace("\\u0000", "");
			byte[] buffer = new byte[payload.length()]; 
			buffer = payload.getBytes(StandardCharsets.UTF_8);
			InetSocketAddress group = new InetSocketAddress(this.g.getAddr(),6789);
			DatagramPacket sendPacket = new DatagramPacket(buffer,buffer.length,group);
			
			try {
				this.g.getMulticast().getSocket().send(sendPacket);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
	}


}
