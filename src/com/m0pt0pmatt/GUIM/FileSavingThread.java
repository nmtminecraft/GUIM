package com.m0pt0pmatt.GUIM;

/**
 * class for separate thread. Saves accounts every once in a while
 * @author Matthew
 */
class FileSavingThread extends Thread{
	
	GUIM guim;
	
	public FileSavingThread(GUIM guim){
		super();
		this.guim = guim;
	}
	
	@Override
	public void run(){
		while(true){
			//save everything
			//guim.save();
			try {
				Thread.sleep(1000 * 60 * 1);
				//guim.save();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
