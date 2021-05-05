package math;

import entities.Entity;
import views.AnimatePath;

public class Point {
	
	private float x, y, length;
	AnimatePath view;

	public Point(float x, float y) {
		this.x = x;
		this.y = y;
		this.length = length(x, y);
	}

	public Point(Entity e) {
		length = length(x = e.getGx(), y = e.getGy());
	}
	
	public Point(AnimatePath view) {
		this.view = view;
	}
	
	public Point() {
		
	}
	
	public Point toGridCoords() {
		return new Point(x - view.getXoffset() / view.getDd(), y - view.getYoffset() / view.getDd());
	}
	
	public float length(float x, float y) {
		return (float) Math.sqrt(x * x + y * y);
	}
	
	public Point subtract(Point v1, Point v2) {
		float x1, x2, y1, y2;
		
		x1 = v1.getX();
		x2 = v2.getX();
		y1 = v1.getY();
		y2 = v2.getY();
		
		Point v = new Point();
		v.setX(x2 - x1);
		v.setY(y1 - y2);
		v.setLength(v.length(v.getX(), v.getY()));
		
		return v;
	}
	
	public Point sum(Point v1, Point v2) {
		float x1, x2, y1, y2;
		
		x1 = v1.getX();
		x2 = v2.getX();
		y1 = v1.getY();
		y2 = v2.getY();
		
		Point v = new Point();
		v.setX(x2 + x1);
		v.setY(y1 + y2);
		v.setLength(v.length(v.getX(), v.getY()));
		
		return v;
	}
	
	public float dot(Point v1, Point v2) {
		return v1.getX() * v2.getX() + v1.getY() * v2.getY();
	}

	public float getX() {
		return x;
	}

	public void setX(float x) {
		this.x = x;
	}

	public float getY() {
		return y;
	}

	public void setY(float y) {
		this.y = y;
	}

	public float getLength() {
		return length;
	}

	public void setLength(float length) {
		this.length = length;
	}

}
