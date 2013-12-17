package graphics;

public class Line2D{
	Point2D p1, p2;
	
	public Line2D(double x1, double y1, double x2, double y2) {
		p1 = new Point2D(x1, y1);
		p2 = new Point2D(x2, y2);
	}
	
	public Line2D(Point2D p1, Point2D p2) {
		this.p1 = p1;
		this.p2 = p2;
	}
	
	public double deltaX(){
		return p1.x - p2.x;
	}
	
	public double deltaY(){
		return p1.y - p2.y;
	}
	
	public double getLineAngle(){
		return Math.toDegrees(Math.atan2(deltaY(),deltaX()));
	}
}