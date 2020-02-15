package com.wessles.mercury.math.geometry;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * A class for all geometric objects made up of one or more vertices.
 *
 * @author wessles
 */
public class Figure {

	/**
	 * All of the vertices that make up the figure.
	 */
	protected Vector2[] vertices;
	protected float[] gdxVertices;
	protected short[] triangles;

	/**
	 * @return all vertices of the object.
	 */
	public Vector2[] getVertices() {
		return vertices;
	}

	/**
	 * @param vertices All vertices in the figure.
	 */
	public Figure(Vector2... vertices) {
		this.vertices = vertices;
		regen();
	}

	/**
	 * @param vertexCoordinates The coordinates of all vertices in the figure. Will be parsed for every 2 values into a Vector2.
	 */
	protected Figure(float... vertexCoordinates) {
		this(getVectors(vertexCoordinates));
	}

	/**
	 * Sorts an even number of float values into 2 dimensional vectors.
	 *
	 * @param coordinates The x/y pattern of floats
	 * @return an array of 2 dimensional vectors based on coordinates
	 */
	protected static Vector2[] getVectors(float... coordinates) {
		if (coordinates.length % 2 != 0)
			throw new IllegalArgumentException("Vertex coordinates must be even!");

		Vector2[] vectors = new Vector2[coordinates.length / 2];

		for (int vertexIndex = 0; vertexIndex < coordinates.length; vertexIndex += 2)
			vectors[vertexIndex / 2] = new Vector2(coordinates[vertexIndex], coordinates[vertexIndex + 1]);

		return vectors;
	}

	/**
	 * Duplicate constructor.
	 */
	public Figure(Figure s) {
		parent = s.parent;
		children = s.children;
		vertices = new Vector2[s.vertices.length];
		for (int _v = 0; _v < s.vertices.length; _v++)
			vertices[_v] = new Vector2(s.vertices[_v].x, s.vertices[_v].y);
		x = s.x;
		y = s.y;
		x2 = s.x2;
		y2 = s.y2;
		width = s.width;
		height = s.height;
		center = s.center;
		rotation = s.rotation;

		// Just to be safe
		regen();
	}

	/**
	 * @return a duplicate of the figure.
	 */
	public Figure duplicate() {
		return new Figure(this);
	}

	/**
	 * This method will regenerate all of the values after you directly modify things.
	 */
	public void regen() {
		x = vertices[0].x;
		y = vertices[0].y;
		x2 = x;
		y2 = y;

		float centerX = 0, centerY = 0;

		for (Vector2 vertex : vertices) {
			centerX += vertex.x;
			centerY += vertex.y;

			x = Math.min(vertex.x, x);
			y = Math.min(vertex.y, y);
			x2 = Math.max(vertex.x, x2);
			y2 = Math.max(vertex.y, y2);
		}

		centerX /= vertices.length;
		centerY /= vertices.length;

		center = new Vector2(centerX, centerY);

		width = Math.abs(x2 - x);
		height = Math.abs(y2 - y);

		gdxVertices = new float[vertices.length * 2];
		int index = 0;
		for (Vector2 vertex : vertices) {
			gdxVertices[index++] = vertex.x;
			gdxVertices[index++] = vertex.y;
		}

		ArrayList<Vector2> vertices = new ArrayList<Vector2>();
		Collections.addAll(vertices, this.vertices);
		ArrayList<Short> triangles = new ArrayList<Short>();

		short vertexIndex0 = 0, vertexIndex1 = 1;
		while (vertexIndex1 < vertices.size()) {
			triangles.add((short) 0);
			triangles.add(vertexIndex0++);
			triangles.add(vertexIndex1++);
		}

		this.triangles = new short[triangles.size()];
		this.triangles = convertShorts(triangles);
	}

	public static short[] convertShorts(List<Short> shorts) {
		short[] ret = new short[shorts.size()];
		Iterator<Short> iterator = shorts.iterator();
		for (short i = 0; i < ret.length; i++) {
			ret[i] = iterator.next();
		}
		return ret;
	}


	/* Intersection / containment testing */


	/**
	 * @return whether a figure intersects with this figure.
	 */
	public boolean intersects(Figure figure) {
		return getIntersectionPoints(figure).length != 0;
	}

	public Point[] getIntersectionPoints(Figure figure) {
		ArrayList<Point> intersectionPoints = new ArrayList<Point>();
		Point[] currentIntersections = null;

		for (Figure child : children)
			if ((currentIntersections = child.getIntersectionPoints(figure)) != null)
				Collections.addAll(intersectionPoints, currentIntersections);

		// Loop through all of the vertices
		for (int vertex0 = 0; vertex0 < vertices.length; ) {

			// For the second point, we want to make sure that we are not doing twice the work for a line, which is not a closed figure.
			Vector2 line1Vertex1 = vertices[vertex0], line1Vertex2 = vertices.length > 2 ? vertices[++vertex0 % vertices.length] : vertices[++vertex0];
			Line line1 = new Line(line1Vertex1, line1Vertex2);

			// If it is a line, the next bit of code breaks.
			if (figure instanceof Line) {

				Line line2 = (Line) figure;

				if ((currentIntersections = line1.getIntersectionPoints(line2)) != null)
					Collections.addAll(intersectionPoints, currentIntersections);

				continue;
			}

			// Now, for each line in this figure, we need to test all lines in the other figure.
			for (int vertex1 = 0; vertex1 < figure.vertices.length; ) {

				Vector2 line2Vertex1 = figure.vertices[vertex1], line2Vertex2 = figure.vertices.length > 2 ? figure.vertices[++vertex1 % figure.vertices.length] : figure.vertices[++vertex1];
				Line line2 = new Line(line2Vertex1, line2Vertex2);

				// Now we test.
				if ((currentIntersections = line1.getIntersectionPoints(line2)) != null)
					Collections.addAll(intersectionPoints, currentIntersections);
			}
		}

		return intersectionPoints.toArray(new Point[intersectionPoints.size()]);
	}

	/**
	 * @return whether all vertices of a figure is inside of this figure.
	 */
	public boolean contains(Figure figure) {
		for (Vector2 v : figure.vertices)
			if (contains(v))
				return true;

		return false;
	}

	/**
	 * @return whether vertex is inside of this figure.
	 */
	public boolean contains(Vector2 vertex) {
		for (Figure child : children)
			if (child.contains(vertex))
				return true;

		float sumAngle = 0;

		for (Vector2 vertex_ : vertices) {
			double deltaX = vertex.x - vertex_.x, deltaY = vertex.y - vertex_.x;
			float angle = (float) Math.atan2(deltaY, deltaX);

			sumAngle += angle;
		}

		return Math.round(sumAngle * 100) == Math.round(Math.PI * 100);
	}


	/* Transformations */


	/**
	 * Moves all vertices by x and y.
	 *
	 * @param x The amount every vertex should move on x.
	 * @param y The amount every vertex should move on y.
	 * @return the figure.
	 */
	public Figure translate(float x, float y) {
		for (Vector2 vertex : vertices) {
			vertex.x += x;
			vertex.y += y;
		}

		regen();

		for (Figure figure : children)
			figure.translate(x, y);

		return this;
	}

	/**
	 * Moves all vertices to x and y.
	 *
	 * @param x Where every vertex should move relative to the nearest point of the figure on x.
	 * @param y Where every vertex should move relative to the nearest point of the figure on y.
	 * @return the figure.
	 */
	public Figure translateTo(float x, float y) {
		return translate(x - this.x, y - this.y);
	}

	/**
	 * The x value of the vertex top-left-most point.
	 */
	protected float x;

	/**
	 * @return the x of the nearest vertex.
	 */
	public float getX() {
		return x;
	}

	/**
	 * The y value of the vertex top-left-most point.
	 */
	protected float y;

	/**
	 * @return the y of the nearest vertex.
	 */
	public float getY() {
		return y;
	}

	/**
	 * The x value of the vertex bottom-right-most point.
	 */
	protected float x2;

	/**
	 * @return the x of the farthest vertex.
	 */
	public float getX2() {
		return x2;
	}

	/**
	 * The y value of the vertex bottom-right-most point.
	 */
	protected float y2;

	/**
	 * @return the y of the farthest vertex.
	 */
	public float getY2() {
		return y2;
	}

	/**
	 * The absolute value of the difference of x and x2.
	 */
	protected float width;

	/**
	 * @return the difference between the nearest x and the farthest x.
	 */
	public float getWidth() {
		return Math.abs(x2 - x);
	}

	/**
	 * The absolute value of the difference of y and y2.
	 */
	protected float height;

	/**
	 * @return the difference between the nearest y and the farthest y.
	 */
	public float getHeight() {
		return Math.abs(y2 - y);
	}

	/**
	 * @return a boxed approximation of the figure.
	 */
	public Rectangle getBoundingBox() {
		return new Rectangle(getX(), getY(), getWidth(), getHeight());
	}

	/**
	 * @return a rough estimate of area.
	 */
	public float getArea() {
		return getWidth() * getHeight();
	}

	/**
	 * The mean center of all vertices.
	 */
	protected Vector2 center;

	/**
	 * @return the center of the object.
	 */
	public Vector2 getCenter() {
		return center;
	}

	/**
	 * The rotation angle (in radians).
	 */
	protected float rotation = 0;

	/**
	 * Rotate the figure relative to a origin.
	 *
	 * @param originX The origin's x.
	 * @param originY The origin's y.
	 * @param angle   The angle by which the object will rotate relative to the origin.
	 * @return the figure.
	 */
	public Figure rotate(float originX, float originY, float angle) {
		for (Vector2 vertex : vertices) {
			float s = (float) Math.sin(angle);
			float c = (float) Math.cos(angle);

			vertex.x -= originX;
			vertex.y -= originY;

			float xNew = vertex.x * c - vertex.y * s;
			float yNew = vertex.x * s + vertex.y * c;

			vertex.x = xNew + originX;
			vertex.y = yNew + originY;
		}

		rotation += angle;

		regen();

		for (Figure figure : children)
			figure.rotate(originX, originY, angle);

		return this;
	}

	/**
	 * Rotate the object relative to the center of the object.
	 *
	 * @param angle The angle of rotation.
	 * @return the figure.
	 */
	public Figure rotate(float angle) {
		return rotate(center.x, center.y, angle);
	}

	/**
	 * Rotate the object to a point in rotation relative to a origin.
	 *
	 * @param originX The origin's x.
	 * @param originY The origin's y.
	 * @param angle   The angle by which the object will rotate to relative to the origin.
	 * @return the figure.
	 */
	public Figure setRotation(float originX, float originY, float angle) {
		return rotate(originX, originY, angle - rotation);
	}

	/**
	 * Rotate the object to a point in rotation relative to the center of the object.
	 *
	 * @param angle The angle of rotation that the object will rotate to.
	 * @return the figure.
	 */
	public Figure setRotation(float angle) {
		return setRotation(center.x, center.y, angle);
	}

	/**
	 * @return the rotation of the object.
	 */
	public float getRotation() {
		return rotation;
	}

	/**
	 * The dilation of the object.
	 */
	protected float dilation = 1;

	/**
	 * Dilates the figure from a point.
	 *
	 * @return the figure.
	 */
	public Figure dilate(Vector2 point, float dilate) {
		translate(-point.x, -point.y);

		for (Vector2 v : vertices) {
			v.x *= dilate;
			v.y *= dilate;
		}

		translate(point.x, point.y);

		this.dilation *= dilate;

		regen();

		for (Figure figure : children)
			figure.dilate(point, dilate);

		return this;
	}

	/**
	 * Dilates the figure from the center of the figure.
	 *
	 * @return the figure.
	 */
	public Figure dilate(float dilate) {
		return dilate(getCenter(), dilate);
	}

	/**
	 * Dilates the figure from a point.
	 *
	 * @return the figure.
	 */
	public Figure setDilation(Vector2 point, float dilate) {
		return dilate(point, dilate / this.dilation);
	}

	/**
	 * Dilates the figure from the center.
	 *
	 * @return the figure.
	 */
	public Figure setDilation(float dilation) {
		return setDilation(getCenter(), dilation);
	}

	/**
	 * @return the dilation of the object.
	 */
	public float getDilation() {
		return dilation;
	}


	/* Geometrical hierarchy */


	protected ArrayList<Figure> children = new ArrayList<Figure>();

	/**
	 * Adds a child figure.
	 *
	 * @return the figure.
	 */
	public Figure addChild(Figure... child) {
		for (Figure s : child) {
			s.parent = this;

			children.add(s);
		}

		return this;
	}

	/**
	 * @return all child figures.
	 */
	public ArrayList<Figure> getChildren() {
		return children;
	}

	protected Figure parent = null;

	/**
	 * Sets the parent of this figure.
	 *
	 * @return the figure.
	 */
	public Figure setParent(Figure parent) {
		parent.addChild(this);

		return this;
	}

	/**
	 * Parent will lose this from it's ArrayList of children.
	 *
	 * @return the figure.
	 */
	public Figure clearParent() {
		parent.children.remove(this);

		parent = null;

		return this;
	}

	/**
	 * @return the parent figure.
	 */
	public Figure getParent() {
		return parent;
	}

	public float[] getGdxVertices() {
		return gdxVertices;
	}

	public short[] getGdxTriangles() {
		return triangles;
	}

	public static float lineWidth = 1f;

	public void render(ShapeRenderer shapeRenderer) {
		if (!(this instanceof Line)) {
			ArrayList<Vector2> vertices = new ArrayList<Vector2>();
			Collections.addAll(vertices, this.vertices);

			int vertexIndex0 = 0, vertexIndex1 = 1;
			while (vertexIndex1 < vertices.size()) {
				Vector2 base = vertices.get(0), vertex0 = vertices.get(vertexIndex0++), vertex1 = vertices.get(vertexIndex1++);
				shapeRenderer.triangle(base.x, base.y, vertex0.x, vertex0.y, vertex1.x, vertex1.y);
			}
		} else {

			Line line = (Line) this;

			float dx = line.getVertices()[0].x - line.getVertices()[1].x;
			float dy = line.getVertices()[0].y - line.getVertices()[1].y;
			float angle = (float) (Math.atan2(dy, dx) - Math.PI / 2);

			Vector2 p1 = new Vector2(line.getVertices()[0].x - (float) Math.cos(angle) * lineWidth / 2, line.getVertices()[0].y - (float) Math.sin(angle) * lineWidth / 2);
			Vector2 p2 = new Vector2(line.getVertices()[0].x + (float) Math.cos(angle) * lineWidth / 2, line.getVertices()[0].y + (float) Math.sin(angle) * lineWidth / 2);
			Vector2 p3 = new Vector2(line.getVertices()[1].x + (float) Math.cos(angle) * lineWidth / 2, line.getVertices()[1].y + (float) Math.sin(angle) * lineWidth / 2);
			Vector2 p4 = new Vector2(line.getVertices()[1].x - (float) Math.cos(angle) * lineWidth / 2, line.getVertices()[1].y - (float) Math.sin(angle) * lineWidth / 2);

			new Rectangle(p1.x, p1.y, p2.x, p2.y, p3.x, p3.y, p4.x, p4.y).render(shapeRenderer);
		}
	}
}
