package client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class MyClient {
	public static void main(String[] args) throws IOException,
			InterruptedException {
		SocketChannel socketChannel = SocketChannel.open();
		Selector selector = Selector.open();
		socketChannel.configureBlocking(false);
		socketChannel.register(selector, SelectionKey.OP_CONNECT);
		socketChannel.connect(new InetSocketAddress(8200));
		while (true) {
			socketChannel.write(ByteBuffer.wrap("HEZK".getBytes()));
			Thread.sleep(100);
		}
	}
}
