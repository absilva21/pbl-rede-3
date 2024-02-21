package application;

import java.util.LinkedList;

public class ViewControl extends Thread {
	private LinkedList<int[]> men;
	
	public ViewControl(LinkedList<int[]> m) {
		this.men = m;
	}
	
	@Override
	public void run(){
		
	}

	public LinkedList<int[]> getMen() {
		return men;
	}

	public void setMen(LinkedList<int[]> men) {
		this.men = men;
	}

}
