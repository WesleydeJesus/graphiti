package net.sf.graphiti.ui.figure.shapes;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.Polygon;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;

public class ShapeHexagon extends Polygon implements IShape {
	
	private GradientPattern fill;

	private Label labelName;

	public ShapeHexagon() {
		super();
		this.setLayoutManager(new XYLayout());
		labelName = new Label();
		labelName.setForegroundColor(ColorConstants.black);
		this.add(labelName);
	}

	@Override
	public ConnectionAnchor getConnectionAnchor() {
		return new PolygonConnectionAnchor(this);
	}
	
	public void paintFigure(Graphics graphics) {
		if (fill == null) {
			fill = new GradientPattern(getBackgroundColor());
		}
		fill.setPattern(getBounds(), graphics);
		super.paintFigure(graphics);
		fill.restorePattern(graphics);
	}

	@Override
	public void setColor(Color color) {
		this.setBackgroundColor(color);
	}

	@Override
	public void setDimension(Dimension dim) {
		int quantx = dim.width / 3;
		// int quanty = height/3 ;
		this.setStart(new Point(0, dim.height / 2));
		this.addPoint(new Point(quantx, 0));
		this.addPoint(new Point(2 * quantx, 0));
		this.addPoint(new Point(dim.width, dim.height / 2));
		this.addPoint(new Point(dim.width - quantx, dim.height));
		this.addPoint(new Point(dim.width - (2 * quantx), dim.height));
		this.addPoint(new Point(0, dim.height / 2));
		this.setFill(true);
	}

	@Override
	public void setName(String name) {
		labelName.setText(name);
		int hDecal = 5;
		int wDecal = (name.length() * 5) / 2;
		Rectangle bounds = this.getParent().getBounds();
		this.setConstraint(labelName, new Rectangle(
				(bounds.width / 2) - wDecal, (bounds.height / 2) - hDecal, -1,
				-1));
	}
}