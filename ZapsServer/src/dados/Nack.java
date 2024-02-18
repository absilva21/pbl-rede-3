package dados;

import java.util.LinkedList;

public class Nack {
	
	private int sourceId;
	private int idNack;
	private LinkedList<int[]> fouls;
	private boolean resposta;
	
	public Nack(int sID, int idN, LinkedList<int[]> f, boolean r ) {
		this.sourceId = sID;
		this.idNack = idN;
		this.fouls = f;
		this.resposta = r;
	}

	public int getSourceId() {
		return sourceId;
	}

	public void setSourceId(int sourceId) {
		this.sourceId = sourceId;
	}

	public int getIdNack() {
		return idNack;
	}

	public void setIdNack(int idNack) {
		this.idNack = idNack;
	}

	public LinkedList<int[]> getFouls() {
		return fouls;
	}

	public void setFouls(LinkedList<int[]> fouls) {
		this.fouls = fouls;
	}

	public boolean isResposta() {
		return resposta;
	}

	public void setResposta(boolean resposta) {
		this.resposta = resposta;
	}
	
	
}
