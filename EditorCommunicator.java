import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.Set;

/**
 * Handles communication to/from the server for the editor
 *
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Fall 2012
 * @author Chris Bailey-Kellogg; overall structure substantially revised Winter 2014
 * @author Travis Peters, Dartmouth CS 10, Winter 2015; remove EditorCommunicatorStandalone (use echo server for testing)
 * @author Tim Pierson Dartmouth CS 10, provided for Winter 2024
 */
public class EditorCommunicator extends Thread {
	public  Shape curr = null;

	private PrintWriter out;        // to server
	private BufferedReader in;        // from server
	protected Editor editor;        // handling communication for

	/**
	 * Establishes connection and in/out pair
	 */
	public EditorCommunicator(String serverIP, Editor editor) {
		this.editor = editor;
		System.out.println("connecting to " + serverIP + "...");
		try {
			Socket sock = new Socket(serverIP, 4242);
			out = new PrintWriter(sock.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			System.out.println("...connected");
		} catch (IOException e) {
			System.err.println("couldn't connect");
			System.exit(-1);
		}
	}

	/**
	 * Sends message to the server
	 */
	public  void send(String msg) {
		out.println(msg);
	}


	/**
	 * Keeps listening for and handling (your code) messages from the server
	 */
	public void run() {
		try {
			String line;
			while ((line = in.readLine()) != null) {
				System.out.println("received:" + line);
				handle(line);
				editor.repaint();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			System.out.println("server hung up");
		}
	}

	// Send editor requests to the server
	public  Shape addHandler(String[] check) {

		if (check[2].equals("ellipse")) {
			Color color = new Color(Integer.parseInt(check[7]));
			curr = new Ellipse(Integer.parseInt(check[3]), Integer.parseInt(check[4]), Integer.parseInt(check[5]), Integer.parseInt(check[6]), color);
		} else if (check[2].equals("rectangle")) {
			Color color = new Color(Integer.parseInt(check[7]));
			curr = new Rectangle(Integer.parseInt(check[3]), Integer.parseInt(check[4]), Integer.parseInt(check[5]), Integer.parseInt(check[6]), color);
		} else if (check[2].equals("segment")) {
			Color color = new Color(Integer.parseInt(check[7]));
			curr = new Segment(Integer.parseInt(check[3]), Integer.parseInt(check[4]), Integer.parseInt(check[5]), Integer.parseInt(check[6]), color);
		} else if (check[2].equals("polyline")) {
			Color color = new Color(Integer.parseInt(check[3]));
			Point point = new Point(Integer.parseInt(check[4]), Integer.parseInt(check[5]));
			curr = new Polyline(point, color);
			int i = 6;
			while (i < check.length - 1) {
				Point np = new Point(Integer.parseInt(check[i]), Integer.parseInt(check[i + 1]));
				i+= 2;
				((Polyline) curr).newPoint(np);
			}
		}
		return curr;
	}

	public void add(String[] s) {
		Shape curr = addHandler(s);
		editor.getSketch().addShape(Integer.parseInt(s[1]), curr);
	}
	public  void move(String[] s) {
		Point point = new Point(Integer.parseInt(s[2]), Integer.parseInt(s[3]));
		if (editor.getSketch() != null) {
			Set<Integer> descorderedkeys = editor.getSketch().descendingKeySet();
			for (Integer id : descorderedkeys) {
				if (editor.getSketch().getVal(id).contains(Integer.parseInt(s[2]), Integer.parseInt(s[3]))) {
					editor.setMoveFrom(point);
					editor.setCurr(editor.getSketch().getVal(id));
					break;
				}
			}
		}
		//move if clicked in shape
		if (editor.getCurr().contains(point.x, point.y)) {
			editor.setMoveFrom(point);
		}
	}
	public synchronized void recolor(String[] s) {
		Point point = new Point(Integer.parseInt(s[2]), Integer.parseInt(s[3]));
		if (editor.getSketch() != null) {
			Set<Integer> descorderedkeys = editor.getSketch().descendingKeySet();
			for (Integer id : descorderedkeys) {
				if (editor.getSketch().getVal(id).contains(Integer.parseInt(s[2]), Integer.parseInt(s[3]))) {
					editor.setMoveFrom(point);
					editor.setCurr(editor.getSketch().getVal(id));
					break;
				}
				;
			}
		}
		//recolor
		if (editor.getCurr().contains(point.x, point.y)) {
			editor.setColor();
		}
	}
	public synchronized void delete(String[] s) {
		Point point = new Point(Integer.parseInt(s[2]), Integer.parseInt(s[3]));
		if (editor.getSketch() != null) {
			Set<Integer> descorderedkeys = editor.getSketch().descendingKeySet();
			for (Integer id : descorderedkeys) {
				if (editor.getSketch().getVal(id).contains(Integer.parseInt(s[2]), Integer.parseInt(s[3]))) {
					editor.setMoveFrom(point);
					Shape delete = editor.getSketch().getVal(id);
					if (delete.contains(point.x, point.y)) {
						delete = null;
						editor.getSketch().remove(id);
						break;
					}
				}
				;
			}
			if (editor.getCurr() != null) {
				if (editor.getCurr().contains(point.x, point.y)) {
					editor.setCurr(null);
				}
			}
		}
	}
	public synchronized void dragMove(String[] s) {
		Point point = new Point(Integer.parseInt(s[2]), Integer.parseInt(s[3]));
		if (editor.getMoveFrom() != null && editor.getCurr()!= null) {
			editor.getCurr().moveBy(point.x - editor.getMoveFrom().x, point.y - editor.getMoveFrom().y);
			editor.setMoveFrom(point);
		}
	}
	public  void handle (String sk){
		String[] s = sk.split(" ");
		if (s[0].equals("add")) {
			add(s);
		}
		if (s[0].equals("move")) {
			move(s);
		}
		if (s[0].equals("recolor")) {
			recolor(s);
		}
		if (s[0].equals("delete")) {
			delete(s);
		}
		if (s[0].equals("dragmove")) {
			dragMove(s);
		}
	}
}
