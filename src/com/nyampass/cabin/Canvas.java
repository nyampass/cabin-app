package com.nyampass.cabin;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.ArcType;

@SuppressWarnings("unused")
public class Canvas {
    private final GraphicsContext graphicsContext;

    public Canvas(GraphicsContext graphicsContext) {
        this.graphicsContext = graphicsContext;
    }

    public float width() {
        return 200;
    }

    public float height() {
        return 200;
    }

    public void strokeWeight(float width) {
        this.graphicsContext.setLineWidth(width);
    }

    public void background(Color color) {
        Paint prevColor = this.graphicsContext.getFill();

        this.graphicsContext.setFill(color);
        this.graphicsContext.fillRect(0, 0, 200, 200);

        this.graphicsContext.setFill(prevColor);
    }

    public void fill(Color color) {
        this.graphicsContext.setFill(color);
    }

    public void stroke(Color color) {
        this.graphicsContext.setStroke(color);
    }

    public void ellipse(int x, int y, int width, int height) {
        this.graphicsContext.fillOval(x, y, width, height);
    }
}
