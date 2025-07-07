import java.awt.*;
import java.util.ArrayList;
import java.util.List;


/**
 * A multi-segment Shape, with straight lines connecting "joint" points -- (x1,y1) to (x2,y2) to (x3,y3) ...
 *
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Spring 2016
 * @author CBK, updated Fall 2016
 * @author Tim Pierson Dartmouth CS 10, provided for Winter 2024
 */
public class Polyline implements Shape {
	List<Point> points = new ArrayList<>();
	private Color color;

	/**
	 * Polyline 0 constructor
	 */
	public Polyline(Point point, Color color) {
		points.add(point);
		this.color = color;
	}
	public void newPoint(Point point){
		points.add(point);
	}

	@Override
	public void moveBy(int dx, int dy) {
		for (Point point: points){
			point.x = point.x +dx;
			point.y = point.y +dy;
		}
	}

	@Override
	public Color getColor() {
		return color;
	}

	@Override
	public void setColor(Color color) {
		this.color = color;
		}

	@Override
	public boolean contains(int x, int y) {
		boolean contain = false;
		int size = points.size();
		for (int i = 0; i < size-1; i++) {
			Segment segment = new Segment(points.get(i).x, points.get(i).y, points.get(i+1).x, points.get(i+1).y, color);
			if (segment.pointToSegmentDistance(x, y, segment.x1, segment.y1, segment.x2, segment.y2) <= 3) {
				contain = true;
				return contain;
			}
		}
		return contain;
	}
	@Override
	public void draw(Graphics g) {
		int size = points.size();
		for(int i = 0; i < size-1; i++){
			Segment segment = new Segment(points.get(i).x, points.get(i).y, points.get(i+1).x, points.get(i+1).y, color);
			segment.draw(g);
		}
	}
	@Override
	public String toString() {
		String s = "polyline ";
		s += color.getRGB() + " ";
		for (Point point: points){
			s += point.x + " " + point.y + " ";
		}
return s;}
}

