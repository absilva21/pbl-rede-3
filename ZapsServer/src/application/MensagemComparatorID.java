package application;

import java.util.Comparator;

import dados.Mensagem;

public class MensagemComparatorID implements Comparator<Mensagem> {

	public MensagemComparatorID() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public int compare(Mensagem msg1, Mensagem msg2) {
		int result = 0;
		
		if(msg1.getIdLocal()<msg2.getIdLocal()) {
			result = -1;
		}else if(msg1.getIdLocal()>msg2.getIdLocal()) {
			result = 1;
		}
		
		return result;
	}
}
