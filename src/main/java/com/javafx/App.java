package com.javafx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.io.IOException;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;


class vec2 {
    vec2(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double x;
    public double y;
}


public class App extends Application {
    enum CurrentMode {
        Draw,
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

    public CurrentMode currentMode = CurrentMode.Draw;
    public Color currentColor = Color.BLACK;

    public vec2 oldPosition = new vec2(0,0);
    public vec2 newPosition = new vec2(0,0);

    public Canvas myCanvas;
    public GraphicsContext draw;
    public Canvas sandboxCanvas;
    public GraphicsContext sandboxDraw;
    Stage primaryStage;

    private String currentPath = null;

    int historyLimit = 20;
    int currentCanvasIndex = -1;
    ArrayList<WritableImage> history;

    public void makeBlackAndWhite(Canvas canvas) {
        WritableImage writableImage = canvas.snapshot(null, null);
        PixelReader pixelReader = writableImage.getPixelReader();
        WritableImage outputImage = new WritableImage((int) writableImage.getWidth(), (int) writableImage.getHeight());
        PixelWriter pixelWriter = outputImage.getPixelWriter();

        for (int y = 0; y < writableImage.getHeight(); y++) {
            for (int x = 0; x < writableImage.getWidth(); x++) {
                Color color = pixelReader.getColor(x, y);
                double gray = (color.getRed() + color.getGreen() + color.getBlue()) / 3;
                pixelWriter.setColor(x, y, new Color(gray, gray, gray, color.getOpacity()));
            }
        }

        draw.drawImage(outputImage, 0, 0);
        writeScreen();
    }

    public void makeInverted(Canvas canvas) {
        WritableImage writableImage = canvas.snapshot(null, null);
        PixelReader pixelReader = writableImage.getPixelReader();
        WritableImage outputImage = new WritableImage((int) writableImage.getWidth(), (int) writableImage.getHeight());
        PixelWriter pixelWriter = outputImage.getPixelWriter();

        for (int y = 0; y < writableImage.getHeight(); y++) {
            for (int x = 0; x < writableImage.getWidth(); x++) {
                Color color = pixelReader.getColor(x, y);
                Color invertedColor = new Color(1.0 - color.getRed(), 1.0 - color.getGreen(), 1.0 - color.getBlue(), color.getOpacity());
                pixelWriter.setColor(x, y, invertedColor);
            }
        }

        draw.drawImage(outputImage, 0, 0);
        writeScreen();
    }

    public void makeSepia(Canvas canvas) {
        WritableImage writableImage = canvas.snapshot(null, null);
        PixelReader pixelReader = writableImage.getPixelReader();
        WritableImage outputImage = new WritableImage((int) writableImage.getWidth(), (int) writableImage.getHeight());
        PixelWriter pixelWriter = outputImage.getPixelWriter();

        for (int y = 0; y < writableImage.getHeight(); y++) {
            for (int x = 0; x < writableImage.getWidth(); x++) {
                Color color = pixelReader.getColor(x, y);
                double r = color.getRed();
                double g = color.getGreen();
                double b = color.getBlue();

                double tr = Math.min(1.0, 0.393 * r + 0.769 * g + 0.189 * b);
                double tg = Math.min(1.0, 0.349 * r + 0.686 * g + 0.168 * b);
                double tb = Math.min(1.0, 0.272 * r + 0.534 * g + 0.131 * b);

                pixelWriter.setColor(x, y, new Color(tr, tg, tb, color.getOpacity()));
            }
        }

        draw.drawImage(outputImage, 0, 0);
        writeScreen();
    }

    public void makeThreshold(Canvas canvas) {
        WritableImage writableImage = canvas.snapshot(null, null);
        PixelReader pixelReader = writableImage.getPixelReader();
        WritableImage outputImage = new WritableImage((int) writableImage.getWidth(), (int) writableImage.getHeight());
        PixelWriter pixelWriter = outputImage.getPixelWriter();

        double idealThreshold = 0;
        for (int y = 0; y < canvas.getHeight(); y++) {
            for (int x = 0; x < canvas.getWidth(); x++) {
                Color color = pixelReader.getColor(x, y);
                idealThreshold = idealThreshold + color.getRed() + color.getBlue() + color.getGreen();
            }
        }

        idealThreshold = idealThreshold / (canvas.getWidth() * canvas.getHeight() *3);
        for (int y = 0; y < canvas.getHeight(); y++) {
            for (int x = 0; x < canvas.getWidth(); x++) {
                Color color = pixelReader.getColor(x, y);
                Color writeColor =  (color.getRed() > idealThreshold || color.getGreen() > idealThreshold || color.getBlue() > idealThreshold) ? Color.WHITE : Color.BLACK;
                pixelWriter.setColor(x, y, writeColor);
            }
        }

        draw.drawImage(outputImage, 0, 0);
        writeScreen();
    }

    private void clearScreen() {
        currentColor = Color.WHITE;

        draw.setStroke(currentColor);
        draw.setFill(currentColor);
        draw.fillRect(0, 0, myCanvas.getWidth(), myCanvas.getHeight());

        sandboxDraw.setStroke(currentColor);
        sandboxDraw.setFill(currentColor);
        sandboxDraw.fillRect(0, 0, myCanvas.getWidth(), myCanvas.getHeight());

        draw.setStroke(currentColor);
        draw.setFill(currentColor);
        sandboxDraw.setStroke(currentColor);
        sandboxDraw.setFill(currentColor);

        currentColor = Color.BLACK;
    }

    private void writeScreen() {
        if(currentCanvasIndex + 1 < history.size()) {
            for(int i = history.size() - 1; i > currentCanvasIndex; i--) {
                history.remove(i);
            }
        }

        WritableImage outputImage = myCanvas.snapshot(null, null);
        if(history.size() < historyLimit) {
            history.add(outputImage);
            currentCanvasIndex++;
        } else {
            history.remove(0);
            history.add(outputImage);
        }

        sandboxCanvas.getGraphicsContext2D().drawImage(outputImage, 0, 0);
    }

    private void save(File file) {
        WritableImage writableImage = new WritableImage((int) myCanvas.getWidth(), (int) myCanvas.getHeight());
        myCanvas.snapshot(null, writableImage);

        PixelReader pixelReader = writableImage.getPixelReader();
        BufferedImage bufferedImage = new BufferedImage((int) myCanvas.getWidth(), (int) myCanvas.getHeight(), BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < (int) myCanvas.getHeight(); y++) {
            for (int x = 0; x < (int) myCanvas.getWidth(); x++) {
                int argb = pixelReader.getArgb(x, y);
                bufferedImage.setRGB(x, y, argb);
            }
        }

        try {
            ImageIO.write(bufferedImage, "png", file);
        } catch (IOException _e) {
            System.err.println("Failed to save canvas: " + _e.getMessage());
        }
    }

    private void saveScreen() {
        FileChooser saveFile = new FileChooser();
        saveFile.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files","*.png", "*.jpg", "*.jpeg", "*.bmp", "*.webp"));
        saveFile.setTitle("Save File");
        File file = saveFile.showSaveDialog(primaryStage);
        currentPath = file.getPath();
        save(file);
    }

    private void loadScreen() {
        FileChooser openFile = new FileChooser();
        openFile.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files","*.png", "*.jpg", "*.jpeg", "*.bmp", "*.webp"));
        openFile.setTitle("Open File");
        File file = openFile.showOpenDialog(primaryStage);
        if (file != null) {
            try (InputStream io = new FileInputStream(file)){
                Image image = new Image(io);
                sandboxDraw.clearRect(0, 0, myCanvas.getWidth(), myCanvas.getHeight());

                double imageWidth = image.getWidth();
                double imageHeight = image.getHeight();

                double scaleX = myCanvas.getWidth() / imageWidth;
                double scaleY = myCanvas.getHeight() / imageHeight;
                double scale = Math.min(scaleX, scaleY);

                double offsetX = (myCanvas.getWidth() - imageWidth * scale) / 2;
                double offsetY = (myCanvas.getHeight() - imageHeight * scale) / 2;

                draw.drawImage(image, 0, 0, imageWidth, imageHeight, 0, 0, imageWidth * scale, imageHeight * scale);
                writeScreen();
            } catch (IOException ex) {
                System.out.println("Error!");
            }
            currentPath = file.getPath();
        }
    }

    void undo() {
        if(currentCanvasIndex - 1 < history.size() && -1 < currentCanvasIndex - 1) {
            WritableImage image = history.get(--currentCanvasIndex);
            myCanvas.getGraphicsContext2D().drawImage(image, 0, 0);
            sandboxCanvas.getGraphicsContext2D().drawImage(image, 0, 0);
        }
    }

    void redo() {
        if(currentCanvasIndex + 1 < history.size() && currentCanvasIndex != -1) {
            WritableImage image = history.get(++currentCanvasIndex);
            myCanvas.getGraphicsContext2D().drawImage(image, 0, 0);
            sandboxCanvas.getGraphicsContext2D().drawImage(image, 0, 0);
        }
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage =  primaryStage;
        myCanvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
        sandboxCanvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
        draw = myCanvas.getGraphicsContext2D();
        sandboxDraw = sandboxCanvas.getGraphicsContext2D();
        history = new ArrayList<WritableImage>();

        clearScreen();
        writeScreen();

        myCanvas.setOnMouseDragged(e -> {
            newPosition.x = e.getX();
            newPosition.y = e.getY();

            WritableImage snap = sandboxCanvas.snapshot(null, null);
            myCanvas.getGraphicsContext2D().drawImage(snap, 0, 0);

            if (currentMode == CurrentMode.Draw) {
                draw.lineTo(newPosition.x, newPosition.y);
                draw.stroke();
            }
            else if(currentMode == CurrentMode.Line) {
                draw.strokeLine(oldPosition.x, oldPosition.y, newPosition.x, newPosition.y);
            }
            else if(currentMode == CurrentMode.Circle) {
                double centerX = (oldPosition.x + newPosition.x) / 2.0;
                double centerY = (oldPosition.y + newPosition.y) / 2.0;
                double radius = Math.max(Math.abs(newPosition.x - oldPosition.x), Math.abs(newPosition.y - oldPosition.y)) / 2.0;

                double topLeftX = centerX - radius;
                double topLeftY = centerY - radius;

                draw.fillOval((int)topLeftX, (int)topLeftY, (int)(2 * radius), (int)(2 * radius));
                draw.strokeOval((int)topLeftX, (int)topLeftY, (int)(2 * radius), (int)(2 * radius));
            }
            else if(currentMode == CurrentMode.Rectangle) {
                double width = Math.abs(newPosition.x - oldPosition.x);
                double height = Math.abs(newPosition.y - oldPosition.y);

                double topLeftX = Math.min(oldPosition.x, newPosition.x);
                double topLeftY = Math.min(oldPosition.y, newPosition.y);

                draw.fillRect((int)topLeftX, (int)topLeftY, (int)width, (int)height);
                draw.strokeRect((int)topLeftX, (int)topLeftY, (int)width, (int)height);
            }
            else if(currentMode == CurrentMode.Triangle) {
                double width = Math.abs(newPosition.x - oldPosition.x);
                double height = Math.abs(newPosition.y - oldPosition.y);

                double topLeftX = Math.min(oldPosition.x, newPosition.x);
                double topLeftY = Math.min(oldPosition.y, newPosition.y);

                double x1 = topLeftX + width / 2;
                double y1 = topLeftY;

                double x2 = topLeftX + width;
                double y2 = topLeftY + height;

                double x3 = topLeftX;
                double y3 = topLeftY + height;

                draw.fillPolygon(new double[]{x1, x2, x3}, new double[]{y1, y2, y3}, 3);
                draw.strokePolygon(new double[]{x1, x2, x3}, new double[]{y1, y2, y3}, 3);
            }
        });

        myCanvas.setOnMouseReleased(e -> {
            newPosition.x = e.getX();
            newPosition.y = e.getY();

            if (currentMode == CurrentMode.Draw) {
                draw.closePath();
            }
            else if(currentMode == CurrentMode.Line) {
                draw.strokeLine(oldPosition.x, oldPosition.y, newPosition.x, newPosition.y);
            }
            else if(currentMode == CurrentMode.Circle) {
                double centerX = (oldPosition.x + newPosition.x) / 2.0;
                double centerY = (oldPosition.y + newPosition.y) / 2.0;
                double radius = Math.max(Math.abs(newPosition.x - oldPosition.x), Math.abs(newPosition.y - oldPosition.y)) / 2.0;

                double topLeftX = centerX - radius;
                double topLeftY = centerY - radius;

                draw.fillOval((int)topLeftX, (int)topLeftY, (int)(2 * radius), (int)(2 * radius));
                draw.strokeOval((int)topLeftX, (int)topLeftY, (int)(2 * radius), (int)(2 * radius));
            }
            else if(currentMode == CurrentMode.Rectangle) {
                double width = Math.abs(newPosition.x - oldPosition.x);
                double height = Math.abs(newPosition.y - oldPosition.y);

                double topLeftX = Math.min(oldPosition.x, newPosition.x);
                double topLeftY = Math.min(oldPosition.y, newPosition.y);

                draw.fillRect((int)topLeftX, (int)topLeftY, (int)width, (int)height);
                draw.strokeRect((int)topLeftX, (int)topLeftY, (int)width, (int)height);
            }
            else if(currentMode == CurrentMode.Triangle) {
                double width = Math.abs(newPosition.x - oldPosition.x);
                double height = Math.abs(newPosition.y - oldPosition.y);

                double topLeftX = Math.min(oldPosition.x, newPosition.x);
                double topLeftY = Math.min(oldPosition.y, newPosition.y);

                double x1 = topLeftX + width / 2;
                double y1 = topLeftY;

                double x2 = topLeftX + width;
                double y2 = topLeftY + height;

                double x3 = topLeftX;
                double y3 = topLeftY + height;

                draw.fillPolygon(new double[]{x1, x2, x3}, new double[]{y1, y2, y3}, 3);
                draw.strokePolygon(new double[]{x1, x2, x3}, new double[]{y1, y2, y3}, 3);
            }

            writeScreen();
        });

        myCanvas.setOnMousePressed(e -> {
            oldPosition.x = e.getX();
            oldPosition.y = e.getY();
            draw.setStroke(currentColor);
            draw.setFill(currentColor);
            draw.setLineWidth(4);
            draw.setLineCap(StrokeLineCap.ROUND);

            if (currentMode == CurrentMode.Draw) {
                draw.beginPath();
                draw.lineTo(oldPosition.x, oldPosition.y);
                draw.fillOval(oldPosition.x, oldPosition.y, 5, 5);
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

        VBox toolMenu = new VBox();
        Button drawButton = createButton.apply("Draw", CurrentMode.Draw);
        Button lineButton = createButton.apply("Line", CurrentMode.Line);
        Button circleButton = createButton.apply("Circle", CurrentMode.Circle);
        Button rectangleButton = createButton.apply("Rectangle", CurrentMode.Rectangle);
        Button triangleButton = createButton.apply("Triangle", CurrentMode.Triangle);
        Button noneButton = createButton.apply("None", CurrentMode.None);
        Button clearButton = createButton.apply("Clear", CurrentMode.None);
        clearButton.setOnAction(e -> {
            clearScreen();
        });

        ColorPicker colorPicker = new ColorPicker();
        colorPicker.setPrefSize(MENU_WIDTH, MENU_HEIGHT);
        colorPicker.setValue(currentColor);
        colorPicker.setOnAction(e -> {
            currentColor = colorPicker.getValue();
        });

        Button undoButton = new Button("Undo");
        undoButton.setPrefSize(MENU_WIDTH, MENU_HEIGHT);
        undoButton.setOnAction(e -> { undo(); });

        Button redoButton = new Button("Redo");
        redoButton.setPrefSize(MENU_WIDTH, MENU_HEIGHT);
        redoButton.setOnAction(e -> { redo(); });

        toolMenu.getChildren().addAll(drawButton, lineButton, circleButton, rectangleButton, triangleButton, clearButton, colorPicker, noneButton, undoButton, redoButton);

        MenuBar menuBar = new MenuBar();
        // File Menu
        {
            Menu m = new Menu("File");
            menuBar.getMenus().add(m);

            MenuItem m1 = new MenuItem("New");
            m1.setOnAction(e -> {
                currentPath = null;
                clearScreen();
            });
            MenuItem m2 = new MenuItem("Open");
            m2.setOnAction(e -> {
                loadScreen();
            });
            MenuItem m3 = new MenuItem("Save");
            m3.setOnAction(e -> {
                if(currentPath == null) { saveScreen(); }
                else { save(new File(currentPath)); }
            });
            MenuItem m4 = new MenuItem("Save As");
            m4.setOnAction(e -> {
                saveScreen();
            });
            m.getItems().addAll(m1, m2, m3, m4);
        }

        // Filter menu
        {
            Menu m = new Menu("Filters");
            menuBar.getMenus().add(m);

            MenuItem m1 = new MenuItem("Invert");
            m1.setOnAction(e -> {
                makeInverted(myCanvas);
            });

            MenuItem m2 = new MenuItem("Black and White");
            m2.setOnAction(e -> {
                makeBlackAndWhite(myCanvas);
            });

            MenuItem m3 = new MenuItem("Sepia");
            m3.setOnAction(e -> {
                makeSepia(myCanvas);
            });

            MenuItem m4 = new MenuItem("Threshold");
            m4.setOnAction(e -> {
                makeThreshold(myCanvas);
            });

            m.getItems().addAll(m1, m2, m3, m4);
        }

        // About Menu
        {
            Label aboutLabel = new Label("About");
            aboutLabel.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    Stage dialog = new Stage();
                    dialog.initModality(Modality.APPLICATION_MODAL);
                    dialog.initStyle(StageStyle.UTILITY);
                    dialog.setTitle("Modal Dialog");

                    Label titleLabel = new Label("JavaPaint");
                    Label creatorsLabel = new Label("Creators: Censored");

                    VBox dialogLayout = new VBox(20);
                    dialogLayout.setStyle("-fx-alignment: center;");
                    dialogLayout.getChildren().addAll(titleLabel, creatorsLabel);

                    VBox.setMargin(titleLabel, new javafx.geometry.Insets(20, 0, 0, 0));

                    Button closeButton = new Button("Close");
                    closeButton.setOnAction(e -> dialog.close());

                    dialogLayout.getChildren().addAll(closeButton);
                    Scene dialogScene = new Scene(dialogLayout, 360, 150);
                    dialog.setScene(dialogScene);
                    dialog.showAndWait();
                }
            });
            Menu aboutMenuButton = new Menu();
            aboutMenuButton.setGraphic(aboutLabel);
            menuBar.getMenus().add(aboutMenuButton);
        }

        // Exit menu
        {
            Label exitLabel = new Label("Exit");
            exitLabel.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    primaryStage.close();
                    Platform.exit();
                    System.exit(0);
                }
            });
            Menu exitMenuButton = new Menu();
            exitMenuButton.setGraphic(exitLabel);
            menuBar.getMenus().add(exitMenuButton);
        }
        primaryStage.setOnCloseRequest(e -> Platform.exit());

        HBox canvasAndToolMenu = new HBox(myCanvas, toolMenu);

        VBox CanvasAndMenuBar = new VBox(menuBar, canvasAndToolMenu);
        AnchorPane anchorPane = new AnchorPane(CanvasAndMenuBar);
        AnchorPane.setTopAnchor(CanvasAndMenuBar, 0.0);
        AnchorPane.setLeftAnchor(CanvasAndMenuBar, 0.0);

        primaryStage.widthProperty().addListener(e -> {
            WritableImage snap = myCanvas.snapshot(null, null);

            menuBar.setMinWidth(primaryStage.getWidth());
            myCanvas.setWidth(primaryStage.getWidth() - MENU_WIDTH - 16);
            sandboxCanvas.setWidth(primaryStage.getWidth() - MENU_WIDTH - 16);
            clearScreen();

            myCanvas.getGraphicsContext2D().drawImage(snap, 0, 0);
            sandboxCanvas.getGraphicsContext2D().drawImage(snap, 0, 0);
        });

        primaryStage.heightProperty().addListener(e -> {
            WritableImage snap = myCanvas.snapshot(null, null);

            myCanvas.setHeight(primaryStage.getHeight() - 24);
            sandboxCanvas.setHeight(primaryStage.getHeight());

            clearScreen();

            myCanvas.getGraphicsContext2D().drawImage(snap, 0, 0);
            sandboxCanvas.getGraphicsContext2D().drawImage(snap, 0, 0);
        });

        Scene scene = new Scene(anchorPane, CANVAS_WIDTH+MENU_WIDTH, CANVAS_HEIGHT + 24);

        KeyCombination ctrlS = new KeyCodeCombination(javafx.scene.input.KeyCode.S, KeyCombination.CONTROL_DOWN);
        scene.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (ctrlS.match(event)) {
                if(currentPath == null) { saveScreen(); }
                else { save(new File(currentPath)); }
            }
        });

        KeyCombination ctrlZ = new KeyCodeCombination(javafx.scene.input.KeyCode.Z, KeyCombination.CONTROL_DOWN);
        scene.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (ctrlZ.match(event)) { undo(); }
        });

        KeyCombination ctrlY = new KeyCodeCombination(javafx.scene.input.KeyCode.Y, KeyCombination.CONTROL_DOWN);
        scene.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (ctrlY.match(event)) { redo(); }
        });

        primaryStage.setTitle("Java FX Paint");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}