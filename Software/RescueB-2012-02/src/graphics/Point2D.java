package graphics;

public class Point2D{
	double x = 0;
	double y = 0;
	
	public Point2D(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	public Point2D() {
	}
	
	/**
	 * Returns a new {@link Point2D} created based on the conversion
	 * from a polar scan (given distante and heading) to a cartesian plan;
	 * 
	 * @param distance the distance from the origin to the point
	 * @param heading in degrees, the angle from origin to the pont (0 = Right)
	 * @return a new {@link Point2D}
	 */
	public static Point2D fromPolar(double distance, double heading){
		double x, y, rads;
		rads = Math.toRadians(heading);
		
		x = Math.cos(rads) * distance;
		y = Math.sin(rads) * distance;
		
		return new Point2D(x, y);
	}
}
