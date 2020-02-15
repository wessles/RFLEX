package com.wessles.mercury.math.geometry;

import com.badlogic.gdx.math.Vector2;

/**
 * @author wessles
 */
public class Star extends Polygon {
	/**
	 * Creates a new star taking in the center x/y positions, the inner/outer radius and number of sides.
	 *
	 * @param xCenter
	 * 		The center x position.
	 * @param yCenter
	 * 		The center y position.
	 * @param innerRadius
	 * 		The inner radius of the star.
	 * @param outerRadius
	 * 		The outer radius of the star.
	 * @param numSides
	 * 		The number of sides on the star.
	 */
	public Star(float xCenter, float yCenter, float innerRadius, float outerRadius, int numSides) {
		this(xCenter, yCenter, innerRadius, innerRadius, outerRadius, outerRadius, numSides);
	}

	/**
	 * Creates a new star taking in the center x/y positions, the x/y inner/outer radius and number of sides.
	 *
	 * @param xCenter
	 * 		The center x position.
	 * @param yCenter
	 * 		The center y position.
	 * @param innerXRadius
	 * 		The inner x radius of the star.
	 * @param innerYRadius
	 * 		The inner y radius of the star.
	 * @param outerXRadius
	 * 		The outer x radius of the star.
	 * @param outerYRadius
	 * 		The outer y radius of the star.
	 * @param numSides
	 * 		The number of sides on the star.
	 */
	public Star(float xCenter, float yCenter, float innerXRadius, float innerYRadius, float outerXRadius, float outerYRadius, int numSides) {
		super(xCenter, yCenter, innerXRadius, innerYRadius, numSides);

		if (numSides % 2 != 0)
			throw new IllegalArgumentException("Number of sides needs to be even for a star.");

		extendPoints(outerXRadius - innerXRadius, outerYRadius - innerYRadius);
	}

	/**
	 * Just goes through every second vertex and shoots it outward to the outer radius, forming points.
	 */
	protected void extendPoints(float pushRadiusX, float pushRadiusY) {
		for (int v0 = 0; v0 < vertices.length; v0 += 2) {
			Vector2 v1 = vertices[v0];

			float angleToCenter = (float) Math.atan2(center.y - v1.y, center.x - v1.x);

			v1.add(new Vector2(-(float) Math.cos(angleToCenter) * pushRadiusX, -(float) Math.sin(angleToCenter) * pushRadiusY));
		}
	}
}
