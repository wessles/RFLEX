package com.wessles.mercury.math.geometry;

/**
 * An ellipse in which the length and width are the same.
 *
 * @author wessles
 */
public class Circle extends Ellipse {
	/**
	 * @param x The x position of the center.
	 * @param y The y position of the center.
	 */
	public Circle(float x, float y, float radius) {
		super(x, y, radius, radius);
	}

	@Override
	public boolean intersects(Figure figure) {
		if (figure instanceof Circle) {
			float dx = figure.getCenter().x - getCenter().x;
			float dy = figure.getCenter().y - getCenter().y;
			float dist = (float) Math.sqrt(dx * dx + dy * dy);

			if (dist <= ((Circle) figure).radiusX + radiusX)
				return true;
		} else
			return super.intersects(figure);

		return false;
	}
}
