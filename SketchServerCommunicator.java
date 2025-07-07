import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.awt.*;
import java.util.ArrayList;
import java.util.*;
import java.util.List;

/**
 * Handles communication between the server and one client, for SketchServer
 *
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Fall 2012; revised Winter 2014 to separate SketchServerCommunicator
 * @author Tim Pierson Dartmouth CS 10, provided for Winter 2024
 */
public class SketchServerCommunicator extends Thread {
	public static Shape curr = null;
	private Socket sock;                    // to talk with client
	private BufferedReader in;              // from client
	private PrintWriter out;         // to client
	private static SketchServer server;            // handling communication for

	public SketchServerCommunicator(Socket sock, SketchServer server) {
		this.sock = sock;
		this.server = server;
	}

	/**
	 * Sends a message to the client
	 *
	 * @param msg
	 */
	public void send(String msg) {out.println(msg);
	}

	/**
	 * Keeps listening for and handling (your code) messages from the client
	 */
	public void run() {
		try {
			System.out.println("someone connected");

			// Communication channel
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			out = new PrintWriter(sock.getOutputStream(), true);

			// Tell the client the current state of the world
			out.println(server.getSketch().toString());

			// Keep getting and handling messages from the client
			String line;
			while ((line = in.readLine()) != null) {
				System.out.println("received:" + line);
				String[] check = line.split(" ");
				handle(check);
				System.out.println(server.getSketch().getMap());
			}

			// Clean up -- note that also remove self from server's list so it doesn't broadcast here
			server.removeCommunicator(this);
			out.close();
			in.close();
			sock.close();

		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			System.out.println("server hung up");
		}
	}

	public static Shape addHandler(String[] check) {

		if (check[1].equals("ellipse")) {
			Color color = new Color(Integer.parseInt(check[6]));
			curr = new Ellipse(Integer.parseInt(check[2]), Integer.parseInt(check[3]), Integer.parseInt(check[4]), Integer.parseInt(check[5]), color);
		} else if (check[1].equals("rectangle")) {
			Color color = new Color(Integer.parseInt(check[6]));
			curr = new Rectangle(Integer.parseInt(check[2]), Integer.parseInt(check[3]), Integer.parseInt(check[4]), Integer.parseInt(check[5]), color);
		} else if (check[1].equals("segment")) {
			Color color = new Color(Integer.parseInt(check[6]));
			curr = new Segment(Integer.parseInt(check[2]), Integer.parseInt(check[3]), Integer.parseInt(check[4]), Integer.parseInt(check[5]), color);
		} else if (check[1].equals("polyline")) {
			Color color = new Color(Integer.parseInt(check[2]));
			Point point = new Point(Integer.parseInt(check[3]), Integer.parseInt(check[4]));
			curr = new Polyline(point, color);
			int i = 5;
			while (i < check.length - 1) {
				Point np = new Point(Integer.parseInt(check[i]), Integer.parseInt(check[i + 1]));
				i+= 2;
				((Polyline) curr).newPoint(np);
			}
		}
		return curr;
	}

	public synchronized static int getID(Shape s){
		return server.getSketch().getsize() + 1;

	}

	public synchronized static void add(String[] s) {
		Shape curr = addHandler(s);
		int movingId = getID(curr);
		server.getSketch().addShape(movingId, curr);
		String st = "add ";
		st += movingId + " " + curr.toString();
		broadcastall(st);
	}


	public synchronized static void move(String[] s) {
		Point point = new Point(Integer.parseInt(s[1]), Integer.parseInt(s[2]));
		Point movefrom = new Point(Integer.parseInt(s[3]), Integer.parseInt(s[4]));
		if (server.getSketch() != null) {
			Set<Integer> descorderedkeys = server.getSketch().descendingKeySet();
			for (Integer id : descorderedkeys) {
				if (server.getSketch().getVal(id).contains(Integer.parseInt(s[1]), Integer.parseInt(s[2]))) {
					movefrom = point;
					String st = "move ";
					st += id + " " +point.x + " " + point.y;
					broadcastall(st);
					break;
				}
			}
		}
	}

	public synchronized static void recolor(String[] s) {
		Point point = new Point(Integer.parseInt(s[1]), Integer.parseInt(s[2]));
		Color color = new Color(Integer.parseInt(s[3]));
		if (server.getSketch() != null) {
			Set<Integer> descorderedkeys = server.getSketch().descendingKeySet();
			for (Integer id : descorderedkeys) {
				if (server.getSketch().getVal(id).contains(Integer.parseInt(s[1]), Integer.parseInt(s[2]))) {
						server.getSketch().getVal(id).setColor(color);
					}
					String st = "recolor ";
					st += id + " "+ point.x + " " + point.y;;
					broadcastall(st);
					break;
				}
			}
		}

	public synchronized static void delete(String[] s) {
		Point point = new Point(Integer.parseInt(s[1]), Integer.parseInt(s[2]));
		if (server.getSketch() != null) {
			Set<Integer> descorderedkeys = server.getSketch().descendingKeySet();
			for (Integer id : descorderedkeys) {
				if (server.getSketch().getVal(id).contains(Integer.parseInt(s[1]), Integer.parseInt(s[2]))) {
					Shape delete = server.getSketch().getVal(id);
					if (delete.contains(point.x, point.y)) {
						delete = null;
						server.getSketch().remove(id);
						String st = "delete ";
						st += id + " " + point.x + " " + point.y;
						broadcastall(st);
						break;
					}
				}
				;
			}
		}
	}
	public synchronized static void dragMove(String[] s) {
		Point point = new Point(Integer.parseInt(s[1]), Integer.parseInt(s[2]));
		Point movefrom = new Point(Integer.parseInt(s[3]), Integer.parseInt(s[4]));
		String st = "dragmove ";
		for (Integer id: server.getSketch().descendingKeySet()){
			if (server.getSketch().getVal(id).contains(point.x, point.y)){
				server.getSketch().getVal(id).moveBy(point.x - movefrom.x, point.y - movefrom.y);
				st += id + " "+ point.x + " " + point.y;
				broadcastall(st);
				}
			}
	movefrom = point;
	}

	public synchronized static void broadcastall(String s){
		SketchServer.broadcast(s);
	}



	public  static void handle(String[] s) {
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