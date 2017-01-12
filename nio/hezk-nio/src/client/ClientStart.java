package client;

import java.io.IOException;


/**
 * 
 * @author wait 
 * @date 2014年12月15日 上午10:56:04
 */
public class ClientStart {

	public static void main(String[] args) throws IOException {
		MainUI.getInstance().open();
		MainUI.getInstance().run();
	}

}
