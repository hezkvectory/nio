package client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * 客户端通信主要处理类, 其中字节序列协议为: 长度 + 内容, 其中长度为4个bit</br> 通信结束处理:客户端发送"bye"到服务器,
 * 然后自己断开连接, 服务器收到"bye", 关掉该连接
 * 
 * @author wait
 * @date 2014年12月15日 上午11:29:31
 */
public class NioClient implements Runnable {

	public static final int PORT = 8200;

	private MainUI mainUI;
	private SocketChannel socketChannel;

	private Selector selector;

	private int port;

	private ByteBuffer buffer = ByteBuffer.allocate(1024);

	private volatile boolean isStop = false;

	// constructors...
	public NioClient(int port, MainUI mainUI) throws IOException {
		socketChannel = SocketChannel.open();
		selector = Selector.open();

		this.port = port;
		this.mainUI = mainUI;

		initiateConnection();
	}

	// logic methods...
	public void send() {
		String data = "";
		if (mainUI != null) {
			data = mainUI.getSendData();
		}

		if (data == null || data.equals("")) {
			return;
		}

		if (data.equals("bye")) {
			close();
		} else {
			byte[] byteData = data.getBytes();
			System.err.println("send-->" + data);

			ByteBuffer innerBuffer = ByteBuffer.allocate(4 + byteData.length);
			innerBuffer.putInt(byteData.length);
			innerBuffer.put(byteData);
			innerBuffer.flip();

			try {
				socketChannel.write(innerBuffer);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	public void close() {
		if (isStop || (socketChannel != null && !socketChannel.isConnected())) {
			return;
		}
		try {

			byte[] bye = "bye".getBytes();

			ByteBuffer innerBuffer = ByteBuffer.allocate(4 + bye.length);
			innerBuffer.putInt(bye.length);
			innerBuffer.put(bye);
			innerBuffer.flip();

			socketChannel.write(innerBuffer);

			socketChannel.close();

			try {
				socketChannel.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			mainUI.getShell().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					mainUI.startConn();
				}
			});

			isStop = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// run...
	@Override
	public void run() {
		while (!isStop) {
			try {
				int num = this.selector.select();
				if (num < 0) {
					continue;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			Iterator<SelectionKey> it = this.selector.selectedKeys().iterator();
			while (it.hasNext()) {
				SelectionKey key = (SelectionKey) it.next();

				if (!key.isValid()) {
					continue;
				}

				if (key.isAcceptable()) {
					System.err.println("accept");
				}

				if (key.isReadable()) {
					this.read(key);
				}

				it.remove();
			}
		}

		System.err.println("client close");
	}

	// getter and setterss..
	public SocketChannel getSocketChannel() {
		return socketChannel;
	}

	public Selector getSelector() {
		return selector;
	}

	// private methods...
	private SocketChannel initiateConnection() throws IOException {
		SocketChannel socketChannel = SocketChannel.open();
		socketChannel.configureBlocking(false);

		socketChannel.connect(new InetSocketAddress(port));

		this.socketChannel = socketChannel;

		try {
			while (!socketChannel.finishConnect()) {
				Thread.sleep(1000L);
				System.err.println("try 1s later...");
			}

			socketChannel.register(selector, SelectionKey.OP_READ);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		if (mainUI != null) {
			mainUI.connSuc();
		}

		return socketChannel;
	}

	private void read(SelectionKey key) {
		SocketChannel socketChannel = (SocketChannel) key.channel();

		buffer.clear();

		int numRead = 0;
		try {
			numRead = socketChannel.read(buffer);
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (numRead == -1) {
			key.cancel();
			return;
		}

		buffer.flip();

		int len = buffer.getInt();
		if (len == 0) {
			return;
		}

		byte[] data = new byte[len];
		buffer.get(data);

		final String dataStr = new String(data);

		mainUI.getShell().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				mainUI.setReceivedData(dataStr);
			}
		});
	}

}
