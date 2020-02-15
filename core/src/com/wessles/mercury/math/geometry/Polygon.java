package com.wessles.mercury.math.geometry;

import com.badlogic.gdx.math.Vector2;

/**
 * A figure with 3 or more sides.
 *
 * @author wessles
 */
public class Polygon extends Figure {
	/**
	 * Creates a new Polygon taking in the center position on the x/y axis, radius and number of sides.
	 *
	 * @param xCenter  The center x position.
	 * @param yCenter  The center y position.
	 * @param radius   The radius of the polygon.
	 * @param numSides The number of sides on the polygon.
	 */
	public Polygon(float xCenter, float yCenter, float radius, int numSides) {
		this(xCenter, yCenter, radius, radius, numSides);
	}

	/**
	 * Creates a new Polygon taking in the center position on the x/y axis, x/y radius and number of sides.
	 *
	 * @param xCenter  The center x position.
	 * @param yCenter  The center y position.
	 * @param xRadius  The x radius of the polygon.
	 * @param yRadius  The y radius of the polygon.
	 * @param numSides The number of sides on the polygon.
	 */
	public Polygon(float xCenter, float yCenter, float xRadius, float yRadius, int numSides) {
		this(getTrigVertices(xCenter, yCenter, xRadius, yRadius, numSides));

		radius = 0.5f * (xRadius + yRadius);
	}

	/**
	 * Creates a new Polygon from raw vertex data.
	 *
	 * @param vertices The vertex data.
	 */
	public Polygon(Vector2[] vertices) {
		super(vertices);
	}

	public Polygon(float[] fs) {
		super(fs);
	}

	/**
	 * @return basically the vertices for a whole bunch of triangles 'slices' that make up a 'pie.'
	 */
	protected static Vector2[] getTrigVertices(float x, float y, float xRadius, float yRadius, int numSides) {
		if (numSides < 3)
			throw new IllegalArgumentException("A polygon must have at least 3 sides!");

		Vector2[] vertices = new Vector2[numSides];

		// Start at 1.5PI, so that we have an upwards-facing
		// polygon. More fun that way.
		float angle = (float) (1.5 * Math.PI), step = (float) (2 * Math.PI / numSides);

		for (int a = 0; a < numSides; a++) {
			if (angle > 2 * Math.PI)
				angle %= 2 * Math.PI;

			vertices[a] = new Vector2(x + (float) Math.cos(angle) * xRadius, y + (float) Math.sin(angle) * yRadius);

			angle += step;
		}

		return vertices;
	}

	private float radius;

	@Override
	public boolean contains(Vector2 vertex) {
		float dx = vertex.x - getCenter().x;
		float dy = vertex.y - getCenter().y;
		return Math.sqrt(dx * dx + dy * dy) < radius*dilation;
	}
}
