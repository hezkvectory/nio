package client;

import java.io.IOException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * 
 * @author wait
 * @date 2014年12月15日 上午10:55:30
 */
public class MainUI {

	public static final int PORT = 8200;

	private static MainUI mainUI;

	private Shell shell;

	public Shell getShell() {
		return shell;
	}

	/** ip */
	private Text ipText;
	/** 端口 */
	private Text portText;
	/** 连接按钮 */
	private Button connectButton;
	/** 发送按钮 */
	private Button sendButton;
	/** 关闭 */
	private Button closeButton;
	/** 发送内容 */
	private Text sendText;
	/** 接收内容 */
	private Text receiveText;

	private NioClient nioClient;

	// static methods...
	public static MainUI getInstance() {
		if (mainUI == null) {
			mainUI = new MainUI();
		}
		return mainUI;
	}

	// swt open and run
	public void open() throws IOException {
		Display display = new Display();
		shell = new Shell(display);
		shell.setText("x-nio");
		shell.setSize(800, 600);
		shell.setLayout(new GridLayout(2, false));

		final Image logoImage = new Image(display, "resources/logo.png");
		shell.setImage(logoImage);

		initTop(shell);

		initLeft(shell);

		initRight(shell);

		initAddListener();

		Monitor primary = display.getPrimaryMonitor();
		Rectangle bounds = primary.getBounds();
		Rectangle rect = shell.getBounds();
		int x = bounds.x + (bounds.width - rect.width) / 2;
		int y = bounds.y + (bounds.height - rect.height) / 2;
		shell.setLocation(x, y);

		shell.open();
	}

	public void run() {
		Display display = shell.getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	// logic methods...
	public void connSuc() {
		connectButton.setEnabled(false);
		sendButton.setEnabled(true);
		closeButton.setEnabled(true);
	}

	public void startConn() {
		connectButton.setEnabled(true);
		sendButton.setEnabled(false);
		closeButton.setEnabled(false);

		nioClient = null;
	}

	// getter and setters...
	public void setReceivedData(String data) {
		receiveText.setText(data);
	}

	public String getSendData() {
		String data = sendText.getText();
		if (data == null || data.equals("")) {
			data = "test";
		}
		data = data.trim();
		return data;
	}

	// private methods...
	private void initTop(Composite parent) {
		GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL
				| GridData.VERTICAL_ALIGN_FILL);
		gridData.horizontalSpan = 2;
		gridData.grabExcessHorizontalSpace = true;

		Group group = new Group(parent, SWT.NONE);
		group.setLayout(new GridLayout(7, false));
		group.setLayoutData(gridData);

		// ip
		Label label = new Label(group, SWT.NONE);
		label.setText("ip:");
		ipText = new Text(group, SWT.SINGLE | SWT.BORDER | SWT.CENTER);
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL
				| GridData.VERTICAL_ALIGN_FILL);
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		ipText.setLayoutData(gridData);
		ipText.setText("127.0.0.1");

		// port
		label = new Label(group, SWT.NONE);
		label.setText("port:");
		portText = new Text(group, SWT.SINGLE | SWT.BORDER | SWT.CENTER);
		portText.setLayoutData(gridData);
		portText.setText(String.valueOf(NioClient.PORT));

		// conn
		connectButton = new Button(group, SWT.PUSH);
		connectButton.setText("conn");
		connectButton.setLayoutData(gridData);

		// send button
		sendButton = new Button(group, SWT.PUSH);
		sendButton.setText("send");
		sendButton.setLayoutData(gridData);
		sendButton.setEnabled(false);

		// close
		closeButton = new Button(group, SWT.PUSH);
		closeButton.setText("close");
		closeButton.setLayoutData(gridData);
		closeButton.setEnabled(false);
	}

	private void initLeft(Composite parent) {
		Group group = new Group(parent, SWT.NONE);
		group.setLayout(new GridLayout(1, false));

		GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL
				| GridData.VERTICAL_ALIGN_FILL);
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		group.setLayoutData(gridData);

		Label label = new Label(group, SWT.NONE);
		label.setText("send");

		sendText = new Text(group, SWT.WRAP | SWT.MULTI | SWT.BORDER
				| SWT.H_SCROLL | SWT.V_SCROLL);
		sendText.setLayoutData(gridData);
	}

	private void initRight(Composite parent) {
		Group group = new Group(parent, SWT.NONE);
		group.setLayout(new GridLayout(1, false));

		GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL
				| GridData.VERTICAL_ALIGN_FILL);
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		group.setLayoutData(gridData);

		Label label = new Label(group, SWT.NONE);
		label.setText("received");

		receiveText = new Text(group, SWT.WRAP | SWT.MULTI | SWT.BORDER
				| SWT.H_SCROLL | SWT.V_SCROLL);
		receiveText.setLayoutData(gridData);
	}

	private void initAddListener() {
		connectButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (nioClient == null) {
					try {
						int port = Integer.parseInt(portText.getText());
						nioClient = new NioClient(port, mainUI);
						new Thread(nioClient).start();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		});

		sendButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				try {
					sendMsg();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});

		closeButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				closeConn();
			}
		});

		shell.addListener(SWT.Close, new Listener() {
			public void handleEvent(Event event) {
				if (nioClient != null) {
					nioClient.close();
				}
				System.exit(0);
			}
		});
	}

	private void sendMsg() throws IOException {
		String data = sendText.getText();
		if (data == null || data.equals("")) {
			return;
		}
		nioClient.send();
	}

	private void closeConn() {
		nioClient.close();
	}

}
