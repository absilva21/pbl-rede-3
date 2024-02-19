package dados;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.Collections;
import application.Application;
import application.GrupoCast;
import application.MensagemComparator;
import application.SyncM;
//import application.SyncM;
	
	public class Grupo {
	private String nome;
	private String adm;
	private int[] relogio;
	private int idIndex;
	private int nackID;
	private GrupoCast multicast;
	private String addr;
	private LinkedList<Mensagem> mensagens;
	private LinkedList<Cliente> clientes;

	public String getAdm() {
		return adm;
	}

	public void setAdm(String adm) {
		this.adm = adm;
	}

	
	public String getNome() {
		return nome;
	}
	
	public int[] getRelogio() {
		return relogio;
	}

	public void setRelogio(int[] relogio) {
		this.relogio = relogio;
	}


	public void setNome(String nome) {
		this.nome = nome;
	}
	
	public LinkedList<Cliente> getClientes() {
		return clientes;
	}

	public void setClientes(LinkedList<Cliente> clientes) {
		this.clientes = clientes;
	}

	public LinkedList<Mensagem> getMensagens() {
		return mensagens;
	}

	public void setMensagens(LinkedList<Mensagem> mensagens) {
		this.mensagens = mensagens;
	}
	
	public int getIdIndex() {
		return idIndex;
	}

	public void setIdIndex(int idIndex) {
		this.idIndex = idIndex;
	}
	
	
	
	@Deprecated
	public void addMessage(Mensagem m) {
		Cliente localHost = this.searchClient(Application.main.localhost);
		int index = localHost.getId();
		for(int i=0;i<this.relogio.length;i++) {
			if(i==index) {
				this.relogio[index]++;
			}else {
				this.relogio[i] = m.getTime()[i];
			}
		}
		
		m.setTime(this.relogio);
		this.mensagens.add(m);
	}

	public GrupoCast getMulticast() {
		return multicast;
	}

	public void setMulticast(GrupoCast multicast) {
		this.multicast = multicast;
	}

	public void removeClient(int index) throws UnknownHostException {
		InetAddress localhost = InetAddress.getLocalHost();
		String ip = new String(localhost.getAddress()); 
		if(ip.equals(adm)) {
			this.clientes.remove(index);
		}
		
	}
	
	public boolean addClient(Cliente c) {
		boolean result = false;
		
		Iterator<Cliente> i = this.clientes.iterator();
		
		while(i.hasNext()) {
			Cliente search = i.next();
			if(search.getAddr().equals(c.getAddr())) {
				break;
			}
			
			if((search.equals(this.clientes.getLast()))&&(!search.getAddr().equals(c.getAddr()))) {
				result = true;
				break;
			}
		}
		
		if(this.clientes.size()==0) {
			result = true;
		}
		
		if(result) {
			
			this.relogio = Arrays.copyOf(this.relogio, this.relogio.length+1);
			c.setId(this.relogio.length-1);
			this.clientes.add(c);
			

		}
		
		return result;
	}
	
	public LinkedList<Mensagem> getMensageSource(String addr){
		LinkedList<Mensagem> result = new LinkedList<Mensagem>();
		Iterator<Mensagem> i = this.mensagens.iterator();
		while(i.hasNext()) {
			Mensagem m = i.next();
			String source = m.getSource().getAddr();
			if(source.equals(addr)) {
				result.add(m);
			}
		}
		return result;
	}
	
	public LinkedList<Mensagem> getFouls(int[] f,LinkedList<Mensagem> r ){
		LinkedList<Mensagem> result = r;
		for(int i = 0;i<f.length;i++) {
			Mensagem m = this.getMensage(f[i]);
			if(!m.equals(null)) {
				result.add(m);
			}
		}
		
		return result;
	}
	
	public Mensagem getMensage(int id) {
		Mensagem result = null;
		
		Iterator<Mensagem> i = this.mensagens.iterator();
		
		while(i.hasNext()) {
			Mensagem m = i.next();
			if(id==m.getIdLocal()) {
				result = m;
				break;
			}
		}
		
		
		return result;
	}
	
	public void receive(Mensagem m) {
		Cliente c = this.searchClient(Application.main.localhost);
		Cliente cOrigem = this.searchClient(m.getSource().getAddr());
		for(int i=0;i<this.relogio.length;i++) {
			if(i!=c.getId()) {
				this.relogio[i] = m.getTime()[i];
			}
			
			
		}
		
		this.relogio[c.getId()] = Math.max(this.relogio[c.getId()], this.relogio[cOrigem.getId()]) + 1;
		
		this.mensagens.add(m);
		
		this.ordenarMensagens();
		ReadWriteLock  readWriteLock = new ReentrantReadWriteLock();
		Lock lock = readWriteLock.writeLock();
		try {
			lock.lock();
			JSONObject JSONm = new JSONObject();
			JSONm.put("type", "com");
			JSONm.put("comNumber", 1);
			JSONm.put("index", c.getId());
			JSONm.put("valor",this.relogio[c.getId()]);
			JSONm.put("origem", Application.main.localhost);
			
			String payload = JSONm.toJSONString();
			
			byte[] buffer = new byte[payload.length()]; 
			buffer = payload.getBytes(StandardCharsets.UTF_8);
			InetSocketAddress group = new InetSocketAddress(this.addr, 6789);
			DatagramPacket sendPacket = new DatagramPacket(buffer,buffer.length,group);
			try {
				this.multicast.getSocket().send(sendPacket);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}finally{
			lock.unlock();
		}
		
		

		
	}
	
	public void send(Mensagem m) {
		Cliente localHost = this.searchClient(Application.main.localhost);
		this.idIndex++;
		m.setIdLocal(this.idIndex);
		int index = localHost.getId();
	    this.relogio[index]++;
		m.setTime(this.relogio);
		this.mensagens.add(m);
		
		int[] relogio = m.getTime();
		JSONArray relogioJson = new JSONArray();
		for(int i = 0; i<relogio.length;i++) {
			String valor = Integer.toString(relogio[i]);
			relogioJson.add(valor);
		}
		JSONObject JSONm = new JSONObject();
		JSONm.put("type", "men");
		JSONm.put("grupo", this.nome);
		JSONm.put("origem", m.getSource().getAddr());
		JSONm.put("nomeOrigem",m.getSource().getNome());
		JSONm.put("body", m.getBody());
		JSONm.put("tempo", relogioJson);
		JSONm.put("id", m.getSource().getId());
		JSONm.put("idm",m.getIdLocal());
		
		String json = JSONm.toJSONString();
		
		String payload = json.replace("\\u0000", "");
		
		byte[] buffer = new byte[payload.length()]; 
		buffer = payload.getBytes(StandardCharsets.UTF_8);
		InetSocketAddress group = new InetSocketAddress(this.addr, 6789);
		DatagramPacket sendPacket = new DatagramPacket(buffer,buffer.length,group);
		try {
			this.multicast.getSocket().send(sendPacket);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public Cliente searchClient(String ip) {
		Cliente result = null;
		
		
		Iterator<Cliente> i =this.clientes.iterator();
		
		while(i.hasNext()) {
			Cliente c = (Cliente) i.next();
			
			if(c.getAddr().equals(ip)) {
				result = c;
				break;
			}
		}
		
		return result;
	}
	
	public boolean isPart(String ip) {
		boolean result = false;
		
		Cliente c = this.searchClient(ip);
		
		if(c!=null) {
			result = true;
		}
		
		return result;
	}
	
	public void ordenarMensagens() {
		 Collections.sort(this.mensagens, new MensagemComparator());
	}
	
	public void syncClock(int index, int value) {
		ReadWriteLock  readWriteLock = new ReentrantReadWriteLock();
		Lock lock = readWriteLock.writeLock();
		try {
			lock.lock();
			this.relogio[index] = value;
		}finally {
			lock.unlock();
		}
	}
	
	public String getAddr() {
		return addr;
	}

	public void setAddr(String addr) {
		this.addr = addr;
	}
	
	public Grupo(String name, String adm) {
		// TODO Auto-generated constructor stub
		this.nome = name;
		this.adm = adm;
		this.clientes = new LinkedList<Cliente>();
		this.mensagens = new LinkedList<Mensagem>();
		this.relogio = new int[0];
		this.addClient(new Cliente(adm,"você"));
		this.idIndex = 0;
		this.addr = "228.5.6.7";
		this.multicast = new GrupoCast(this);
		this.multicast.start();
		this.nackID = 0;
		
	}

	public int getNackID() {
		return nackID;
	}

	public void setNackID(int nackID) {
		this.nackID = nackID;
	}

}
