package com.wessles.mercury.math.geometry;

import com.badlogic.gdx.math.Vector2;

/**
 * A figure of exactly 1 point.
 *
 * @author wessles
 */
public class Point extends Figure {

	public Point(float x, float y) {
		super(x, y);
	}

	@Override
	public boolean intersects(Figure figure) {
		return figure.contains(new Vector2(getX(), getY()));
	}

	@Override
	public boolean contains(Vector2 vertex) {
		return vertex.x == getX() && vertex.y == getY();
	}


	@Override
	public float getArea() {
		return 1;
	}

	/**
	 * @return the point in the form of a vector.
	 */
	public Vector2 toVector() {
		return new Vector2(getX(), getY());
	}

	@Override
	public String toString() {
		return "(Point) " + toVector().toString();
	}
}
