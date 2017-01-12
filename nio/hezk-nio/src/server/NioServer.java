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

import com.talent.aio.common.utils.ByteBufferUtils;
import com.talent.aio.common.utils.HelloPacket;

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
