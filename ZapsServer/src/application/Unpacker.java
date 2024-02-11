package application;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import dados.Cliente;
import dados.Delivery;
import dados.Grupo;
import dados.Mensagem;
// uma fila é usada para desempacotar o pacote 
public class Unpacker extends Thread {
	
	public LinkedList<String> payloadsDtg;

	public Unpacker() {
		this.payloadsDtg = new LinkedList<String>();
	}
	
	@Override
	public void run() {
		while(true){
			String payload;
			ReadWriteLock  readWriteLock = new ReentrantReadWriteLock();
			Lock lock = readWriteLock.writeLock();
			try {
				lock.lock();
				payload = payloadsDtg.pollFirst();
			}finally {
				lock.unlock();
			}
			
			
			if(payload!=null) {
				System.out.println(payload);
				int fimJson = payload.lastIndexOf('}')+1;
				String jsonBody = payload.substring(0, fimJson);	
				
				JSONParser parser = new JSONParser(); 
				
				try {
					
					JSONObject json = (JSONObject) parser.parse(jsonBody);
					String tipo = (String)json.get("type");
					
					
					if(tipo.equals("com")) {
						
						Long tipoComLong = (Long) json.get("com");
						int tipoComInt = tipoComLong.intValue();
						
						switch(tipoComInt) {
							case 1:
								JSONObject grupoJson = (JSONObject) json.get("body");
								String nomeGrupo = (String) grupoJson.get("nome");
								String admGrupo = (String) grupoJson.get("adm");
								Grupo novoGrupo = new Grupo(nomeGrupo,admGrupo);
								JSONArray mens = (JSONArray)  grupoJson.get("mensagens");
								JSONArray partici =  (JSONArray) grupoJson.get("clientes");
								
								for(int i=0;i<partici.size();i++) {
									JSONObject par = (JSONObject) partici.get(i);
									String addr = (String) par.get("addr");
									String nome = (String) par.get("nome");
									Long idLong = (Long) par.get("id");
									int id = idLong.intValue();
									Cliente c = new Cliente(addr,nome);
									c.setId(id);
									novoGrupo.addClient(c);	
								}
								
								
								for(int i = 0;i<mens.size();i++) {
									JSONObject mensa = (JSONObject) mens.get(i);
									String bodyMen = (String) mensa.get("body");
									JSONArray tempoMen = (JSONArray) mensa.get("temp");
									String addrOri = (String) mensa.get("origem");
									Long idLocal = (Long) mensa.get("idm");
									int idLocalValue = idLocal.intValue();
									int[] tempo = new int[tempoMen.size()];
									for(int j = 0;j<tempo.length;i++) {
										int valor = Integer.parseInt((String)tempoMen.get(j));
										tempo[j] = valor;
									}
									
									Cliente c = novoGrupo.searchClient(addrOri);
									
									Mensagem men = new Mensagem(bodyMen,tempo,c);
									men.setIdLocal(idLocalValue);
									novoGrupo.receive(men);
									
								}
								
								
								
								ReadWriteLock  readWriteLock2 = new ReentrantReadWriteLock();
								Lock lock2 = readWriteLock2.writeLock();
								try {
									lock2.lock();
									Application.main.grupos.add(novoGrupo);
								}finally {
									lock2.unlock();
								}
								break;
								
							case 2:
								JSONObject jsonBody2 = (JSONObject) json.get("body");
								String addr = (String) jsonBody2.get("addr");
								String nome = (String) jsonBody2.get("nome");
								String grupo = (String) jsonBody2.get("grupo");
								Cliente c = new Cliente(addr,nome);
								
								
								Iterator<Grupo> it = Application.main.grupos.iterator();
								
								while(it.hasNext()) {
									Grupo g = (Grupo) it.next();
									if(g.getNome().equals(grupo)) {
										g.addClient(c);
									}
								}
								break;
								
							case 3:
								Iterator<Grupo> i = Application.main.grupos.iterator();
								String nomeGrupo2 = (String) json.get("grupo");
								String destino = (String) json.get("source");
								while(i.hasNext()) {
									Grupo g = (Grupo) i.next();
									if(g.getNome().equals(nomeGrupo2)) {
										int id = 0;
										ReadWriteLock  readWriteLock3 = new ReentrantReadWriteLock();
										Lock lock3 = readWriteLock3.writeLock();
										try {
											lock3.lock();
											id = g.getIdIndex();
										}finally {
											lock3.unlock();
										}
										
										JSONObject jsonResposta = new JSONObject();
										jsonResposta.put("id", id);
										String jsonString = jsonResposta.toJSONString();
										DatagramSocket serverSocket;
										serverSocket = new DatagramSocket(7050);
										byte[] buffer = new byte[1024];
										buffer = jsonString.getBytes(StandardCharsets.UTF_8);
										InetAddress destiny = InetAddress.getByName(destino);
										DatagramPacket sendPacket = new DatagramPacket(buffer,buffer.length,destiny,7030);
										serverSocket.send(sendPacket);
										serverSocket.close();
										
									}
								}
								break;
								
							case 4:
								String nomeGrupo3 = (String) json.get("grupo");
								String origem = (String) json.get("source");
								JSONArray faltas = (JSONArray) json.get("faltas");
								int[] idFaltas = new int[faltas.size()];
								
								for(int j = 0;j<faltas.size();j++) {
									Long nLong = (Long) faltas.get(j);
									int n = nLong.intValue();
									idFaltas[j] = n;
								}
								LinkedList<Mensagem> mensf = null;
								Iterator<Grupo> itg = Application.main.grupos.iterator();
								Grupo grupof = null;
								while(itg.hasNext()) {
									Grupo g = itg.next();
									
									if(g.getNome().equals(nomeGrupo3)) {
										mensf = g.getFouls(idFaltas);
										grupof = g;
										break;
									}
								}
								
								if(!mensf.equals(null)) {
									Iterator<Mensagem> itf = mensf.iterator();
									
									while(itf.hasNext()) {
										Mensagem m = itf.next();
										LinkedList<Cliente> cli = new LinkedList<Cliente>();
										Cliente cliente = grupof.searchClient(origem);
										cli.add(cliente);
										Delivery d = new Delivery(cli.iterator(),grupof,m,7080);
										d.start();
									}
								}
								
								break;
						}
						
					}
					
					if(tipo.equals("men")) {
						String mensagem = json.get("body").toString();
						String origem = json.get("origem").toString();
						String destino = json.get("grupo").toString();
						JSONArray tempoJson = (JSONArray) json.get("tempo");
						Long idLocal = (Long) json.get("idm");
						int idLocalValue = idLocal.intValue(); 
						int[] tempo = new int[tempoJson.size()];
						for(int i = 0; i<tempoJson.size();i++) {
							int valor = Integer.parseInt( (String) tempoJson.get(i));
							tempo[i] = valor;
						}
						Grupo grupoDestino = grupoExiste(destino);
						Grupo viewGroup = null;
						if(!origem.equals(Application.main.localhost)) {
							if(grupoDestino!=null) {
								
								boolean participante = grupoDestino.isPart(origem);
								
								if(Application.main.grupoView>0&&participante) {
									viewGroup = Application.main.grupos.get(Application.main.grupoView-1);
									if(viewGroup.getNome().equals(grupoDestino.getNome())) {
										Mensagem nova = new Mensagem(mensagem,tempo,viewGroup.searchClient(origem));
										nova.setIdLocal(idLocalValue);
										viewGroup.receive(nova);
										Iterator<Mensagem> i = viewGroup.getMensagens().iterator();
										
										for(int j = 0; j<50;j++) {
											System.out.println("");
										}
										
										 System.out.println("     "+viewGroup.getNome());
										 System.out.print(" seu relógio local: {");
								    	 for(int index = 0;index<viewGroup.getRelogio().length;index++) {
								    		 if(index==viewGroup.getRelogio().length-1) {
								    			 System.out.print(viewGroup.getRelogio()[index]);
								    		 }else {
								    			 System.out.print(viewGroup.getRelogio()[index]+",");
								    		 }
								    	 }
								    	 System.out.print("}\n");
										    while(i.hasNext()) {
										    	Mensagem m = i.next();
										    	
										    	if(m.getSource().getAddr().equals(Application.main.localhost)) {
										    		Mensagem out = (Mensagem) m;
										    		System.out.println(" \n                  você"+": \n                  		"+out.getBody()+"\n");
										    	}else {
										    		Mensagem IN = (Mensagem) m;
										    		System.out.println(" \n"+IN.getSource().getAddr()+": \n		"+IN.getBody()+" "+IN.getTime());
										    		 System.out.print(" relógio do remetente: {");
											    	 for(int index = 0;index<IN.getTime().length;index++) {
											    		 if(index==viewGroup.getRelogio().length-1) {
											    			 System.out.print(viewGroup.getRelogio()[index]);
											    		 }else {
											    			 System.out.print(viewGroup.getRelogio()[index]+",");
											    		 }
											    		 
											    	 }
											    	 System.out.print("}\n");
										    		
										    	}
										    	
										    	
										    }
										    
										    System.out.println("\ndigite uma mensagem para o grupo ou ENTER para sair:");
										
									}else {
										Mensagem nova = new Mensagem(mensagem,tempo,viewGroup.searchClient(origem));
										nova.setIdLocal(idLocalValue);
										grupoDestino.receive(nova);
									}
								}else {
									if(participante) {
										Mensagem nova = new Mensagem(mensagem,tempo,grupoDestino.searchClient(origem));
										nova.setIdLocal(idLocalValue);
										grupoDestino.receive(nova);
									}
								}
							}
						}
						
					}
					
					
				}catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SocketException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	public Grupo grupoExiste(String grupo) {
			
			Grupo result = null;
			Iterator<Grupo> it = Application.main.grupos.iterator();
			
			while(it.hasNext()) {
				Grupo g = (Grupo) it.next();
				if(g.getNome().equals(grupo)) {
					result = g;
					break;
				}
			}
			
		
			return result;
			
		}
	

}
