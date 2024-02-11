package application;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import dados.Cliente;
import dados.Grupo;
import dados.Mensagem;


//uma thread que envia dados ou comandos para que os nós  sincronizem
/*
 add - avisa um nó que ele foi adicionado a um grupo
 */

public class Sync extends Thread {
	
	private Cliente destino;
	private int tipo;
	private Object mensagem;
	
	
	

	public Cliente getDestino() {
		return destino;
	}



	public void setDestino(Cliente destino) {
		this.destino = destino;
	}



	public int getTipo() {
		return tipo;
	}



	public void setTipo(int tipo) {
		this.tipo = tipo;
	}



	public Object getMensagem() {
		return mensagem;
	}



	public void setMensagem(Object mensagem) {
		this.mensagem = mensagem;
	}

	public Sync(Cliente d, int t, Object m) {
		this.destino = d;
		this.tipo = t;
		this.mensagem = m;
	}
	
	@Override
	public void run() {
		try {
			int porta = 7020;
			
			DatagramSocket serverSocket;

			serverSocket = new DatagramSocket(porta);
			
			JSONObject body = new JSONObject();
			
			
			//mapear as informações em json
			if(this.mensagem instanceof Grupo) {
				
				Grupo g = (Grupo) this.mensagem;
				JSONArray mensagens = new JSONArray();
				JSONArray clientes = new JSONArray();
				Iterator<Mensagem> i = g.getMensagens().iterator();
				Iterator<Cliente> j = g.getClientes().iterator(); 
				
				body.put("adm",g.getAdm());
				
				body.put("nome", g.getNome());
				
				while(i.hasNext()) {
					Mensagem m = (Mensagem) i.next();
					JSONObject jsonMensagem = new JSONObject();
					jsonMensagem.put("origem", m.getSource().getAddr());
					jsonMensagem.put("nomeOrigem", m.getSource().getNome());
					jsonMensagem.put("body", m.getBody());
					int[] tempo = m.getTime();
					JSONArray tempoJson = new JSONArray(); 
					for(int it = 0; it<tempo.length;it++) {
						tempoJson.add(Integer.toString(tempo[it]));
					}
					jsonMensagem.put("temp", m.getTime());
					jsonMensagem.put("idm", m.getIdLocal());
					mensagens.add(jsonMensagem);
				}
				
				while(j.hasNext()) {
					Cliente c = (Cliente) j.next();
					JSONObject jsonCliente = new JSONObject();
					jsonCliente.put("addr", c.getAddr());
					jsonCliente.put("nome", c.getNome());
					jsonCliente.put("id",c.getId());
					clientes.add(jsonCliente);
				}
				
				body.put("mensagens", mensagens);
				body.put("clientes", clientes);
				JSONObject json = new JSONObject();
				json.put("type", "com");
				json.put("com", this.tipo);
				json.put("body", body);
				
				String pacote = json.toJSONString();
				
				byte[] buffer = new byte[1024];
				
				buffer = pacote.getBytes(StandardCharsets.UTF_8);
				InetAddress destiny = InetAddress.getByName(this.destino.getAddr());
				DatagramPacket sendPacket = new DatagramPacket(buffer,buffer.length,destiny,7000);
				
				serverSocket.send(sendPacket);
				
				
				
				Iterator<Cliente> it = g.getClientes().iterator();
				
				while(it.hasNext()) {
					Cliente c = (Cliente) it.next();
					JSONObject jsonCliente = new JSONObject();
					jsonCliente.put("addr", this.destino.getAddr());
					jsonCliente.put("nome",this.destino.getNome());
					jsonCliente.put("grupo",g.getNome());
					JSONObject jsonMen = new JSONObject();
					jsonMen.put("type","com");
					jsonMen.put("com", 2);
					jsonMen.put("body", jsonCliente);
					
					if(!c.getAddr().equals(this.destino.getAddr())&&!c.getAddr().equals(Application.main.localhost)) {
						String pacote2 = jsonMen.toJSONString();
						byte[] buffer2 = new byte[2048];
						buffer2 = pacote2.getBytes(StandardCharsets.UTF_8);
						InetAddress destiny2 = InetAddress.getByName(c.getAddr());
						DatagramPacket sendPacket2 = new DatagramPacket(buffer2,buffer2.length,destiny2,7000);
						serverSocket.send(sendPacket2);
					}
	
				}
				serverSocket.close();
			}
			

			
			
			
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	

}
