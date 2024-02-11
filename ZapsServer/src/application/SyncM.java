package application;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
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
	
	private Grupo g;
	private DatagramSocket serverSocket;
	private DatagramSocket serverSocket2;

	public SyncM(Grupo g){
		// TODO Auto-generated constructor stub
		this.g = g;
		serverSocket = null;
		serverSocket2 = null;
		try {
			serverSocket2 = new DatagramSocket(7040);
		} catch (SocketException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		try {
			serverSocket = new DatagramSocket(7030);
		} catch (SocketException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		
		
		
		while(true) {
			try {
				Thread.sleep(5000);
				
				Iterator<Cliente> i = g.getClientes().iterator();
				
				while(i.hasNext()) {
					Cliente c = i.next();
					if(!c.getAddr().equals(Application.main.localhost)) {
						int ttl = 0;
						
						byte[] buffer2 = new byte[1024];
						byte[] receiveData = new byte[2048];
						
						
						while(ttl<3) {
							try {
								JSONObject jsonPedido = new JSONObject();
								jsonPedido.put("com", 3);
								jsonPedido.put("grupo", g.getNome());
								jsonPedido.put("source", Application.main.localhost);
								
								
								InetAddress destiny2 = InetAddress.getByName(c.getAddr());
								String pacote = "type: com\nbody: "+ jsonPedido.toJSONString();
								buffer2 = pacote.getBytes(StandardCharsets.UTF_8);
								DatagramPacket sendPacket2 = new DatagramPacket(buffer2,buffer2.length,destiny2,7000);
								serverSocket2.send(sendPacket2);
							
								
								
								
								
								DatagramPacket receivePacket = new DatagramPacket(receiveData,
										receiveData.length);
								serverSocket.setSoTimeout(150);
								serverSocket.receive(receivePacket);
								String payload = new String(receivePacket.getData());
								
								int inicioJSON = payload.indexOf('{');
								int fimJSON = payload.lastIndexOf('}')+1;
								
								String resposta = payload.substring(inicioJSON, fimJSON);
								
				
								JSONParser parser = new JSONParser(); 
								JSONObject json = (JSONObject) parser.parse(resposta);
								
								Long idLong = (Long) json.get("id");
								int id = idLong.intValue();
								
								LinkedList<Mensagem> mensagens = g.getMensageSource(c.getAddr());
								if(mensagens.size()>0) {
									Iterator<Mensagem> j = mensagens.iterator();
									Collections.sort(mensagens, new MensagemComparatorID());
									
									LinkedList<Integer> fouls = new  LinkedList<Integer>();
									while(j.hasNext()) {
										Mensagem m = j.next();
										Mensagem m2 = null;
										if(j.hasNext()) {
											m2 = j.next();
										}else {
											break;
										}
										
										int dif = m2.getIdLocal() - m.getIdLocal();
										if(dif>1) {
											for(int g = m.getIdLocal()+1;g<m2.getIdLocal();g++) {
												fouls.add(g);
											}
										}
									}
									
									Mensagem last = mensagens.getLast();
									
									if(last.getIdLocal()<id) {
										for(int h = last.getIdLocal();h<id;h++) {
											fouls.add(h);
										}
										fouls.add(id);
									}
									
									
									JSONArray faltas = new JSONArray(); 
									JSONObject jsonFaltas = new JSONObject();
									if(fouls.size()>1) {
										Iterator<Integer> idNumber = fouls.iterator();
										while(idNumber.hasNext()) {
											int number = idNumber.next();
											faltas.add(number);
										}
										jsonFaltas.put("grupo", g.getNome());
										jsonFaltas.put("com", 4);
										jsonFaltas.put("source", Application.main.localhost);
										jsonFaltas.put("faltas", faltas);
										
										String pacote3 = "type: com\nbody: "+ jsonFaltas.toJSONString();
										byte[] buffer3 = pacote3.getBytes(StandardCharsets.UTF_8);
										DatagramPacket sendPacket3 = new DatagramPacket(buffer3,buffer3.length,destiny2,7000);
										serverSocket2.send(sendPacket3);
									}
									
								}
								
								
							}catch (SocketException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}catch (UnknownHostException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}catch (IOException e) {
								// TODO Auto-generated catch block
								
								if(e instanceof SocketTimeoutException ) {
									ttl++;
								}else {
									e.printStackTrace();
								}
								
							}
						}
						
					}
					
				}
				
				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public Grupo getG() {
		return g;
	}

	public void setG(Grupo g) {
		this.g = g;
	}

	protected void finalize() {
		serverSocket.close();;
		serverSocket2.close();;
	}

}
