package application;


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
import dados.Mensagem;
import dados.Nack;
// uma fila é usada para desempacotar o pacote 
public class Unpacker extends Thread {
	
	public LinkedList<String> payloadsDtg;
	public LinkedList<Nack> nacks; 

	public Unpacker() {
		this.payloadsDtg = new LinkedList<String>();
		this.nacks = new LinkedList<Nack>();
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
									JSONArray faltas =  (JSONArray) json.get("fouls");
									Long NackId = (Long) json.get("nackID");
									int NackIdInt = NackId.intValue();
									Long origemID = (Long) json.get("origem");
									int origemIDInt = origemID.intValue();
									LinkedList<int[]> fouls = new LinkedList<int[]>();
									for(int i = 0;i<faltas.size();i++) {
										int[] falta = (int[]) faltas.get(i);
										fouls.add(falta);
									}
									Nack nack = new Nack(origemIDInt,NackIdInt,fouls,false);
									nacks.add(nack);
									break;
									
								case 3:
									Long nodeId = (Long) json.get("nodeID");
									Long nackID = (Long) json.get("nackId");
									JSONArray mensa = (JSONArray) json.get("mens");
									boolean forMe = isLocalHost(nodeId.intValue());
									
									if(forMe) {
										mensa = (JSONArray) json.get("mens");
										ReadWriteLock  readWriteLock2 = new ReentrantReadWriteLock();
										Lock lock2 = readWriteLock.writeLock();
										try {
											lock2.lock();
											for(int i = 0;i<mensa.size();i++) {
												JSONObject m = (JSONObject) mensa.get(i);
												String mensagem = m.get("body").toString();
												String origem2 = m.get("origem").toString();
												String nomeorigem = m.get("nomeOrigem").toString();
												JSONArray tempoJson = (JSONArray) json.get("tempo");
												Long idLocal = (Long) m.get("idm");
												int idLocalValue = idLocal.intValue(); 
												int[] tempo = new int[tempoJson.size()];
												for(int j = 0; j<tempoJson.size();j++) {
													int valor2 = Integer.parseInt( (String) tempoJson.get(i));
													tempo[j] = valor2;
												}
												
												Mensagem nova = new Mensagem(mensagem,tempo,new Cliente(origem,nomeorigem));
												nova.setIdLocal(idLocalValue);
												Application.main.grupo.getMensagens().add(nova);
												
											}
											Application.main.grupo.ordenarMensagens();
											
										}finally {
											lock2.unlock();
										}
										
									}else {
										ReadWriteLock  readWriteLock2 = new ReentrantReadWriteLock();
										Lock lock2 = readWriteLock2.writeLock();
										Nack r = searchNack(nackID.intValue(), nodeId.intValue());
										if(!r.equals(null)) {
											try {
												lock2.lock();
												r.setResposta(true);
											}finally {
												lock2.unlock();
											}
										}
									}
									
									
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
	
	public boolean isLocalHost(int id) {
		boolean result = false;
		String localhost = Application.main.localhost;
		Cliente c  = Application.main.grupo.searchClient(localhost);
		
		if(!c.equals(null)&&c.getId()==id) {
			result = true;
		}
		
		return result;
	}
	
	public Nack searchNack(int idN,int id) {
		Nack n = null;
		ReadWriteLock  readWriteLock = new ReentrantReadWriteLock();
		Lock lock = readWriteLock.writeLock();
		
		try {
			lock.lock();
			Iterator<Nack> it = nacks.iterator();
			
			while(it.hasNext()) {
				Nack m = it.next();
				if(m.getIdNack()==idN&&m.getSourceId()==id) {
					n = m;
					break;
				}
			}
		}finally {
			lock.unlock();
		}

		
		return n;
	}

}
