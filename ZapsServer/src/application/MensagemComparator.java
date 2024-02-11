package application;

import dados.Mensagem;
import java.util.Comparator;

public class MensagemComparator implements Comparator<Mensagem> {

	public MensagemComparator() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public int compare(Mensagem msg1, Mensagem msg2) {
		int[] time1 = msg1.getTime();
        int[] time2 = msg2.getTime();
        
        // Comparar cada componente do relógio vetorial
        for (int i = 0; i < time1.length; i++) {
            if (time1[i] < time2[i]) {
                return -1;
            } else if (time1[i] > time2[i]) {
                return 1;
            }
            
        }
        
        // Se todos os componentes forem iguais, comparar pela origem da mensagem
        return Integer.compare(msg1.getSource().getId(), msg2.getSource().getId());

	}

}
