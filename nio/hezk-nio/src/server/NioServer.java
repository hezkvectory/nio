package server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

import utils.ByteBufferUtils;
import utils.HelloPacket;
/**
 * NIO server端
 * 
 * server端解决了socket粘包拆包问题<br/>
 * 目前值显示一个server端和一个client端的拆包粘包，后期完善，实现server端对应N个client拆包粘包问题<br/>
 * 主要修改的是<code>private ByteBuffer lastByteBuffer = null;</code>这里需要改成SocketChannel对应lastByteBuffer</br>
 * lastByteBuffer主要是记录拆包粘包时没有处理完的信息
 * @author hezhengkui
 *
 */
public class NioServer {

	public static final int PORT = 8200;
	public static final int BUFFER_SIZE = 1024;
	private ServerSocketChannel serverChannel;
	private Selector selector;
	private ByteBuffer lastByteBuffer = null;

	public void init() throws IOException {
		serverChannel = ServerSocketChannel.open();
		serverChannel.configureBlocking(false);
		selector = Selector.open();
		serverChannel.register(selector, SelectionKey.OP_ACCEPT);

		ServerSocket serverSocket = serverChannel.socket();
		serverSocket.bind(new InetSocketAddress(PORT));
	}

	public void listen() throws IOException {
		while (true) {
			int count = selector.select();
			if (count <= 0) {
				continue;
			}
			Set<SelectionKey> selectedKeys = selector.selectedKeys();
			for (Iterator<SelectionKey> it = selectedKeys.iterator(); it
					.hasNext();) {
				SelectionKey selectedKey = (SelectionKey) it.next();

				if (!selectedKey.isValid()) {
					continue;
				}

				handleKey(selectedKey);

				it.remove();
			}
		}
	}

	private void handleKey(SelectionKey key) throws IOException {
		ServerSocketChannel server = null;
		SocketChannel client = null;
		if (key.isAcceptable()) {
			server = (ServerSocketChannel) key.channel();

			client = server.accept();

			client.configureBlocking(false);

			client.register(selector, SelectionKey.OP_READ);

			System.err.println("accept...");
		} else if (key.isConnectable()) {
			System.err.println("connect...");
		} else if (key.isReadable()) {
			client = (SocketChannel) key.channel();
			ByteBuffer byteBuffer = ByteBuffer.allocate(BUFFER_SIZE);
			client.read(byteBuffer);
			byteBuffer.limit(byteBuffer.position());
			byteBuffer.position(0);
			if (byteBuffer != null) {
				if (lastByteBuffer != null) {
					byteBuffer.position(0);
					byteBuffer = ByteBufferUtils.composite(lastByteBuffer, byteBuffer);
					lastByteBuffer = null;
				}
				byteBuffer.position(0);
				while (true) {
					int initPosition = byteBuffer.position();
					String b = decode(byteBuffer, client);
					if (b == null || "".equals(b)) {
						byteBuffer.position(initPosition);
						lastByteBuffer = byteBuffer;
						break;
					} else {
						System.out.println(b);
					}
					if (byteBuffer.hasRemaining()) {
						continue;
					} else {
						lastByteBuffer = null;
						break;
					}
				}
			}
		} else if (key.isWritable()) {
			System.err.println("writable");
		}
	}

	/**
	 * 报文解密，目前简单的实现了从报文中抽取内容，并没有给予封装，后期实现
	 * @param buffer
	 * @param client
	 * @return
	 * @throws IOException
	 */
	private String decode(ByteBuffer buffer, SocketChannel client)
			throws IOException {
		int readableLength = buffer.limit() - buffer.position();
		if (readableLength < HelloPacket.HEADER_LENGHT) {
			return null;
		}
		int bodyLength = buffer.getInt();
		if (bodyLength < 0) {
			return null;
		}
//		System.out.println("bodyLength:" + bodyLength);
		int neededLength = HelloPacket.HEADER_LENGHT + bodyLength;
		int test = readableLength - neededLength;
		if (test < 0) {
			return null;
		} else {
			if (bodyLength > 0) {
				byte[] dst = new byte[bodyLength];
				buffer.get(dst);
				return new String(dst);
			}
			return null;
		}
	}

	public static void main(String[] args) throws IOException {
		NioServer server = new NioServer();
		server.init();
		server.listen();
	}
}
