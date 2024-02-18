package application;


import java.util.LinkedList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import dados.Cliente;
import dados.Mensagem;
// uma fila é usada para desempacotar o pacote 
public class Unpacker extends Thread {
	
	public LinkedList<String> payloadsDtg;
	public LinkedList<JSONObject> nacks; 

	public Unpacker() {
		this.payloadsDtg = new LinkedList<String>();
		this.nacks = new LinkedList<JSONObject>();
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
				payload = payload.substring(0,payload.indexOf('}')+1);
				System.out.println(payload);
				JSONParser parser = new JSONParser(); 
				
				try {
					
					JSONObject json = (JSONObject) parser.parse(payload);
					String tipo = (String)json.get("type");
					
					if(tipo.equals("com")) {
						String origem = (String)json.get("origem");
						
						if(!origem.equals(Application.main.localhost)) {
							Long tipoComLong = (Long) json.get("comNumber");
							int tipoComInt = tipoComLong.intValue();
							
							switch(tipoComInt) {
								case 1:
									Long valor = (Long) json.get("valor");
									int valorInt = valor.intValue();
									Long index = (Long) json.get("index");
									int indexInt = index.intValue();
									Application.main.grupo.syncClock(indexInt, valorInt);
									break;
									
									
								case 2:
									
									break;
									
								case 3:
									/*Iterator<Grupo> i = Application.main.grupos.iterator();
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
									}*/
									break;
									
								case 4:
									/*String nomeGrupo3 = (String) json.get("grupo");
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
									}*/
									
									break;
							}
						}
						
						
						
					}
					
					if(tipo.equals("men")) {
						String mensagem = json.get("body").toString();
						String origem = json.get("origem").toString();
						String nomeorigem = json.get("nomeOrigem").toString();
						String destino = json.get("grupo").toString();
						JSONArray tempoJson = (JSONArray) json.get("tempo");
						Long idLocal = (Long) json.get("idm");
						int idLocalValue = idLocal.intValue(); 
						int[] tempo = new int[tempoJson.size()];
						for(int i = 0; i<tempoJson.size();i++) {
							int valor = Integer.parseInt( (String) tempoJson.get(i));
							tempo[i] = valor;
						}
						/*Grupo grupoDestino = grupoExiste(destino);
						Grupo viewGroup = null;*/
						Mensagem nova = new Mensagem(mensagem,tempo,new Cliente(origem,nomeorigem));
						nova.setIdLocal(idLocalValue);
						if(!origem.equals(Application.main.localhost)) {
							Application.main.grupo.receive(nova);
						}
						
						/*if(!origem.equals(Application.main.localhost)) {
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
						}*/
						
					}
					
					
				}catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
			}
		}
	}
	
	/*
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
			
		}*/
	

}
