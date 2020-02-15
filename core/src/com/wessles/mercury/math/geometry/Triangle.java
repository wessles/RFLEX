package com.wessles.mercury.math.geometry;

import com.badlogic.gdx.math.Vector2;

/**
 * A triangle shape; 3 sides.
 *
 * @author wessles
 */
public class Triangle extends Polygon {
	/**
	 * Creates a new triangle taking in the x, y, width and height values.
	 *
	 * @param x
	 * 		The x position.
	 * @param y
	 * 		The y position.
	 * @param w
	 * 		The width of the triangle.
	 * @param h
	 * 		The height of the triangle.
	 */
	public Triangle(float x, float y, float w, float h) {
		this(x, y, x + w, y, x, y + h);
	}

	/**
	 * Creates a new triangle taking in the location data for the top-center, bottom-left and bottom-right parts of the
	 * triangle.
	 *
	 * @param x1
	 * 		The top-center x position.
	 * @param y1
	 * 		The top-center y position.
	 * @param x2
	 * 		The bottom-left x position.
	 * @param y2
	 * 		The bottom-left y position.
	 * @param x3
	 * 		The bottom-right x position.
	 * @param y3
	 * 		The bottom-right y position.
	 */
	public Triangle(float x1, float y1, float x2, float y2, float x3, float y3) {
		super(new float[]{x1, y1, x2, y2, x3, y3});
	}

	@Override
	public boolean contains(Vector2 vertex) {
		// Source:
		// http://stackoverflow.com/questions/2049582/how-to-determine-a-point-in-a-triangle

		boolean b1, b2, b3;

		b1 = sign(vertex, vertices[0], vertices[1]) < 0.0f;
		b2 = sign(vertex, vertices[1], vertices[2]) < 0.0f;
		b3 = sign(vertex, vertices[2], vertices[0]) < 0.0f;

		return b1 == b2 && b2 == b3;
	}

	private float sign(Vector2 p1, Vector2 p2, Vector2 p3) {
		return (p1.x - p3.x) * (p2.y - p3.y) - (p2.x - p3.x) * (p1.y - p3.y);
	}

	@Override
	public float getArea() {
		return getWidth() * getHeight() / 2;
	}
}
