package dados;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

import org.json.simple.JSONArray;

import application.Application;
import application.Main;

//aqui é feita a entrega de mensagem
public class Delivery extends Thread  {
	
	private Iterator<Cliente> destinos;
	private Grupo grupo; 
	private Mensagem mensagem;
	private int porta;
	
	
	
	

	public int getPorta() {
		return porta;
	}




	public void setPorta(int porta) {
		this.porta = porta;
	}




	public Iterator<Cliente> getDestinos() {
		return destinos;
	}




	public void setDestinos(Iterator<Cliente> destinos) {
		this.destinos = destinos;
	}




	public Grupo getGrupo() {
		return grupo;
	}




	public void setGrupo(Grupo grupo) {
		this.grupo = grupo;
	}




	public Mensagem getMensagem() {
		return mensagem;
	}




	public void setMensagem(Mensagem mensagem) {
		this.mensagem = mensagem;
	}




	public Delivery(Iterator<Cliente> d, Grupo g,Mensagem m, int port) {
		this.destinos = d;
		this.grupo = g;
		this.mensagem = m;
		this.porta = port;
	}
	
	
	

	@Override
	public void run() {
		
		try {
			
			DatagramSocket serverSocket;
			serverSocket = new DatagramSocket(porta);
			while(destinos.hasNext()) {

				Cliente c = (Cliente) destinos.next();
				if(!c.getAddr().equals(Application.main.localhost)) {
					int[] relogio = mensagem.getTime();
					JSONArray relogioJson = new JSONArray();
					for(int i = 0; i<relogio.length;i++) {
						String valor = Integer.toString(relogio[i]);
						relogioJson.add(valor);
					}
					
					
					byte[] buffer = new byte[2048];
					InetAddress destiny = InetAddress.getByName(c.getAddr());
					String payload = "type: men\nbody: {\"grupo\":\""+grupo.getNome()+"\",\"origem\":\""+Application.main.localhost+"\",\"body\":\""+ mensagem.getBody()+"\",\"tempo\":"+relogioJson.toJSONString()+",\"id\":\""+mensagem.getSource().getId()+"\",\"idm\":\""+mensagem.getIdLocal()+"\"}";
					buffer = payload.getBytes(StandardCharsets.UTF_8);
					DatagramPacket sendPacket = new DatagramPacket(buffer,buffer.length,destiny,7000);
					serverSocket.send(sendPacket);
					
					
				}
				
			}
			
			serverSocket.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
