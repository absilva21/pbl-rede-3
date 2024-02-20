package application;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import dados.Mensagem;
import dados.Nack;

public class NackRes extends Thread {
	private Unpacker unpacker;
	
	public NackRes(Unpacker u) {
		this.unpacker = u;
	}
	
	@Override
	public void run() {
		while(true) {
			Random random = new Random();
			try {
				Thread.sleep(random.nextInt(9, 100));
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			ReadWriteLock  readWriteLock = new ReentrantReadWriteLock();
			Lock lock = readWriteLock.writeLock();
			
			try {
				lock.lock();
				
				Nack n = this.unpacker.nacks.pollFirst();
				if(n!=null) {
					if(!n.isResposta()) {
						LinkedList<Mensagem> fouls = new LinkedList<Mensagem>();
						Iterator<int[]> i = n.getFouls().iterator();
						
						while(i.hasNext()) {
							int[] f = i.next();
							fouls = Application.main.grupo.getFouls(f, fouls);
						}
						
						JSONArray faltas = new JSONArray();
						Iterator<Mensagem> it = fouls.iterator();
						
						while(it.hasNext()) {
							Mensagem m = it.next();
							JSONObject JSONm = new JSONObject();
							JSONm.put("type", "men");
							JSONm.put("grupo", Application.main.grupo.getNome());
							JSONm.put("origem", m.getSource().getAddr());
							JSONm.put("nomeOrigem",m.getSource().getNome());
							JSONm.put("body", m.getBody());
							
							JSONArray relogioJson = new JSONArray();
							int[] relogio = m.getTime();
							for(int j = 0; j<relogio.length;j++) {
								String valor = Integer.toString(relogio[j]);
								relogioJson.add(valor);
							}
							
							JSONm.put("tempo", relogioJson);
							JSONm.put("id", m.getSource().getId());
							JSONm.put("idm",m.getIdLocal());
							
							faltas.add(JSONm);
						}
						
						JSONObject nackRes = new JSONObject();
						
						nackRes.put("type", "com");
						nackRes.put("comNumber", 3);
						nackRes.put("origem", Application.main.localhost);
						nackRes.put("nodeID", n.getSourceId());
						nackRes.put("nackId", n.getIdNack());
						nackRes.put("mens",faltas);
						
						String json = nackRes.toJSONString();
						
						String payload = json.replace("\\u0000", "");
						
						byte[] buffer = new byte[payload.length()]; 
						buffer = payload.getBytes(StandardCharsets.UTF_8);
						InetSocketAddress group = new InetSocketAddress("228.5.6.7", 6789);
						DatagramPacket sendPacket = new DatagramPacket(buffer,buffer.length,group);
						Application.main.grupo.getMulticast().getSocket().send(sendPacket);
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally {
				lock.unlock();
			}
		}
		
	}
}
