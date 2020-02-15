package com.wessles.mercury.math.geometry;

import com.badlogic.gdx.math.Vector2;

/**
 * A figure of exactly 2 connected vertices.
 *
 * @author wessles
 */
public class Line extends Figure {

	/**
	 * @param x1
	 * 		The starting x position of the line.
	 * @param y1
	 * 		The starting y position of the line.
	 * @param x2
	 * 		The ending x position of the line.
	 * @param y2
	 * 		The ending y position of the line.
	 */
	public Line(float x1, float y1, float x2, float y2) {
		this(new Vector2(x1, y1), new Vector2(x2, y2));
	}

	/**
	 * @param p1
	 * 		The starting vector of the line.
	 * @param p2
	 * 		The ending vector of the line.
	 */
	public Line(Vector2 p1, Vector2 p2) {
		super(p1, p2);
	}

	@Override
	public void regen() {
		super.regen();

		// Get our two points.
		Vector2 p1 = vertices[0], p2 = vertices[1];

		// ----- y = mx + b
		// or,
		// ----- b = y - mx
		// Where:
		// ----- y is the y value of a point on the line
		// ----- x is the x value of said point on the line
		// ----- m is the slope of the line
		// ----- b is the y intercept of the line

		slope = (p2.y - p2.y) / (p2.x - p1.x);

		yIntercept = p1.y - slope * p1.x;
		xIntercept = -yIntercept / slope;
	}

	@Override
	public boolean intersects(Figure figure) {
		if (figure instanceof Line) {
			Vector2 m00 = vertices[0];
			Vector2 m01 = vertices[1];
			Vector2 m10 = figure.vertices[0];
			Vector2 m11 = figure.vertices[1];

			float UA = ((m11.x - m10.x) * (m00.y - m10.y) - (m11.y - m10.y) * (m00.x - m10.x)) / ((m11.y - m10.y) * (m01.x - m00.x) - (m11.x - m10.x) * (m01.y - m00.y));
			float UB = ((m01.x - m00.x) * (m00.y - m10.y) - (m01.y - m00.y) * (m00.x - m10.x)) / ((m11.y - m10.y) * (m01.x - m00.x) - (m11.x - m10.x) * (m01.y - m00.y));

			if (UA >= 0 && UA <= 1 && UB >= 0 && UB <= 1)
				return true;
		} else
			return super.intersects(figure);

		return false;
	}

	public Point[] getIntersectionPoints(Line line) {
		if (!intersects(line)) return null;

		float px = getX();
		float py = getY();
		float rx = getX2() - px;
		float ry = getY2() - py;

		float qx = line.getX();
		float qy = line.getY();
		float sx = line.getX2() - qx;
		float sy = line.getY2() - qy;

		double det = sx * ry - sy * rx;
		if (det == 0)
			return null;
		else {
			double z = (sx * (qy - py) + sy * (px - qx)) / det;

			if (z == 0 || z == 1) return null;  // intersection at end point!

			return new Point[]{new Point((float) (px + z * rx), (float) (py + z * ry))};
		}
	}

	@Override
	public boolean contains(Vector2 vertex) {
		// Plug the point into this formula, and see if it
		// checks out.
		// ----- b = y-mx

		return vertex.y - slope * vertex.x == yIntercept;
	}

	private float slope;

	public float getSlope() {
		return slope;
	}

	private float yIntercept;

	public float getYIntercept() {
		return yIntercept;
	}

	private float xIntercept;

	public float getXIntercept() {
		return xIntercept;
	}

	@Override
	public float getArea() {
		// Lines always have an area of 1. Proof:
		// http://math.stackexchange.com/questions/256803/is-the-area-of-a-line-1
		return 1;
	}
}
