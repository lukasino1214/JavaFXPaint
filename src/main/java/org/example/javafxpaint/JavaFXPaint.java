package org.example.javafxpaint;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.stage.Stage;


class vec2 {
    vec2(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double x;
    public double y;
}


public class JavaFXPaint extends Application {
    enum CurrentMode {
        Line,
        Circle,
        Rectangle,
        Triangle,
        None,
    }

    public static final int CANVAS_WIDTH = 1280;
    public static final int CANVAS_HEIGHT = 720;

    public static final int MENU_WIDTH = 96;
    public static final int MENU_HEIGHT = 24;

    public CurrentMode currentMode = CurrentMode.Line;
    public Color currentColor = Color.BLACK;

    public vec2 oldPosition = new vec2(0,0);
    public vec2 newPosition = new vec2(0,0);

    public void wrapPositions() {
        if(oldPosition.x > newPosition.x) {
            oldPosition.x = newPosition.x;
        }
        if(oldPosition.y > newPosition.y) {
            oldPosition.y = newPosition.y;
        }
    }

    @Override
    public void start(Stage primaryStage) {
        StackPane root = new StackPane();
        Canvas myCanvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
        GraphicsContext draw = myCanvas.getGraphicsContext2D();

        myCanvas.setOnMouseDragged(e -> {
            newPosition.x = e.getX();
            newPosition.y = e.getY();
            if (e.getX() >= 0 && e.getX() < CANVAS_WIDTH && e.getY() >= 0 && e.getY() < CANVAS_HEIGHT) {}

            if (currentMode == CurrentMode.Line) {
                draw.lineTo(e.getX(), e.getY());
                draw.stroke();
            }
        });

        myCanvas.setOnMouseReleased(e -> {
            newPosition.x = e.getX();
            newPosition.y = e.getY();

            if (currentMode == CurrentMode.Line) {
                draw.closePath();
            }
            else if(currentMode == CurrentMode.Circle) {
                double radius = (Math.abs(newPosition.x - oldPosition.x) + Math.abs(newPosition.y - oldPosition.y)) / 2;

                wrapPositions();

                draw.fillOval(oldPosition.x, oldPosition.y, radius, radius);
                draw.strokeOval(oldPosition.x, oldPosition.y, radius, radius);
            }
            else if(currentMode == CurrentMode.Rectangle) {
                double width = Math.abs(newPosition.x - oldPosition.x);
                double height = Math.abs(newPosition.y - oldPosition.y);

                wrapPositions();

                draw.fillRect(oldPosition.x, oldPosition.y, width, height);
                draw.strokeRect(oldPosition.x, oldPosition.y, width, height);
            }
            else if(currentMode == CurrentMode.Triangle) {
                double width = Math.abs(newPosition.x - oldPosition.x);
                double height = Math.abs(newPosition.y - oldPosition.y);

                wrapPositions();

                double x1 = oldPosition.x + width / 2;
                double y1 = oldPosition.y;

                double x2 = oldPosition.x + width;
                double y2 = oldPosition.y + height;

                double x3 = oldPosition.x;
                double y3 = oldPosition.y + height;

                draw.fillPolygon(new double[]{x1, x2, x3}, new double[]{y1, y2, y3}, 3);
                draw.strokePolygon(new double[]{x1, x2, x3}, new double[]{y1, y2, y3}, 3);
            }
        });

        myCanvas.setOnMousePressed(e -> {
            oldPosition.x = e.getX();
            oldPosition.y = e.getY();

            if (currentMode == CurrentMode.Line) {
                draw.beginPath();
                draw.setLineWidth(4);
                draw.setStroke(currentColor);
                draw.setLineCap(StrokeLineCap.ROUND);
                draw.lineTo(e.getX(), e.getY());
            } else {
                draw.setStroke(currentColor);
                draw.setFill(currentColor);
            }
        });

        TwoParameterFunction<String, CurrentMode, Button> createButton = (name, mode) -> {
            Button button = new Button(name);
            button.setPrefSize(MENU_WIDTH, MENU_HEIGHT);
            button.setOnAction(e -> {
                currentMode = mode;
            });
            return button;
        };

        VBox vbox = new VBox();
        Button lineButton = createButton.apply("Line", CurrentMode.Line);
        Button circleButton = createButton.apply("Circle", CurrentMode.Circle);
        Button rectangleButton = createButton.apply("Rectangle", CurrentMode.Rectangle);
        Button triangleButton = createButton.apply("Triangle", CurrentMode.Triangle);
        Button noneButton = createButton.apply("None", CurrentMode.None);
        Button clearButton = createButton.apply("Clear", CurrentMode.None);
        clearButton.setOnAction(e -> {
            draw.clearRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
        });

        ColorPicker colorPicker = new ColorPicker();
        colorPicker.setPrefSize(MENU_WIDTH, MENU_HEIGHT);
        colorPicker.setValue(currentColor);
        colorPicker.setOnAction(e -> {
            currentColor = colorPicker.getValue();
        });

        vbox.getChildren().addAll(lineButton, circleButton, rectangleButton, triangleButton, noneButton, clearButton, colorPicker);


        HBox hbox = new HBox();

        hbox.getChildren().addAll(myCanvas, vbox);
        root.getChildren().add(hbox);

        Scene scene = new Scene(root, CANVAS_WIDTH+MENU_WIDTH, CANVAS_HEIGHT);

        primaryStage.setTitle("Java FX Paint");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    public static void main(String[] args) {
        launch(args);
    }
}