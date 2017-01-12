package client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Random;

import com.talent.aio.common.utils.HelloPacket;

public class ClientTest {

	public static void main(String[] args) throws Exception {
		client();
	}

	public static void client() {
		SocketChannel channel = null;
		try {

			Selector selector = Selector.open();
			channel = SocketChannel.open();
			channel.configureBlocking(false);
			channel.connect(new InetSocketAddress(8200));
			channel.register(selector, SelectionKey.OP_CONNECT);

			while (true) {
				if (selector.select() > 0) {

					Iterator<SelectionKey> set = selector.selectedKeys().iterator();
					while (set.hasNext()) {
						SelectionKey key = set.next();
						set.remove();

						SocketChannel ch = (SocketChannel) key.channel();
						if (key.isConnectable()) {
							ch.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE, new Integer(1));
							ch.finishConnect();
						}

						if (key.isReadable()) {
							key.attach(new Integer(1));
							ByteArrayOutputStream output = new ByteArrayOutputStream();
							ByteBuffer buffer = ByteBuffer.allocate(1024);
							int len = 0;
							while ((len = ch.read(buffer)) != 0) {
								buffer.flip();
								byte by[] = new byte[buffer.remaining()];
								buffer.get(by);
								output.write(by);
								buffer.clear();
							}
							System.out.println(new String(output.toByteArray()));
							output.close();
						}

						if (key.isWritable()) {
//							synchronized (ClientTest.class) {
								key.attach(new Integer(1));
								String say = "ByteBufferbyteb=ByteBuffer.allocate(say.length()+4);";
								String tmp = "d"+say.substring(0, new Random().nextInt(say.length()));
								int bodyLen = tmp.length();
								int allLen = HelloPacket.HEADER_LENGHT + bodyLen;
								ByteBuffer byteb = ByteBuffer.allocate(allLen);
								byteb.order(ByteOrder.BIG_ENDIAN);
								byteb.putInt(bodyLen);
								byteb.put(tmp.getBytes());
								byteb.flip();
								ch.write(byteb);
//							}
//							ch.close();
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				channel.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

//	static class ClientRunnable implements Runnable {
//
//		private SocketChannel ch;
//
//		private ClientRunnable(SocketChannel ch) {
//			this.ch = ch;
//		}
//
//		@Override
//		public void run() {
//			try {
//				while (true) {
//					ch.write(ByteBuffer.wrap((("client say:hi")).getBytes()));
//					Thread.sleep(5000);
//				}
//			} catch (Exception e) {
//				e.printStackTrace();
//				try {
//					ch.close();
//				} catch (IOException e1) {
//					e1.printStackTrace();
//				}
//			}
//		}
//	}

}
