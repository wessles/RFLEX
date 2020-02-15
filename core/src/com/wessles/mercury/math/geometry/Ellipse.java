package com.wessles.mercury.math.geometry;

import com.badlogic.gdx.math.Vector2;

/**
 * An ellipse that can have a length and width.
 *
 * @author wessles
 */
public class Ellipse extends Polygon {
	/**
	 * Maximum amount of sides that can be rendered when rendering an ellipse.
	 */
	public static int MAX_SIDES = 40;

	/**
	 * The radius in the respective axis.
	 */
	public float radiusX, radiusY;

	/**
	 * @param x
	 * 		The x position of the center.
	 * @param y
	 * 		The y position of the center.
	 * @param radiusX
	 * 		The radius of the circle in the x axis.
	 * @param radiusY
	 * 		The radius of the circle in the y axis.
	 */
	public Ellipse(float x, float y, float radiusX, float radiusY) {
		super(x, y, radiusX, radiusY, MAX_SIDES);
		this.radiusX = radiusX;
		this.radiusY = radiusY;
	}

	// They are round, with a lot of vertices. This isn't
	// pixel-perfect, but it
	// is good enough.
	@Override
	public boolean intersects(Figure figure) {
		if (figure instanceof Ellipse)
			for (Vector2 v : figure.vertices)
				if (contains(v))
					return true;

		return false;
	}

	@Override
	public boolean contains(Vector2 vertex) {
		float test = (vertex.x - getCenter().x) * (vertex.x - getCenter().x) / (radiusX * radiusX) + (vertex.y - getCenter().y) * (vertex.y - getCenter().y) / (radiusY * radiusY);

		return test <= 1;
	}

	@Override
	public float getArea() {
		return 3.14f * radiusX * radiusY;
	}
}
