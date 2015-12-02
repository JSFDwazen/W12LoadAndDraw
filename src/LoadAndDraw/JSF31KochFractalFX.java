/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package LoadAndDraw;

//import calculate.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.*;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import CalculateAndWrite.Edge;

/**
 *
 * @author Nico Kuijpers
 */
public class JSF31KochFractalFX extends Application implements Observer {

    private static File fileBtxt;
    private static File fileUtxt;
    private static File fileBbin;
    private static File fileUbin;

    private static ArrayList<Edge> edges = new ArrayList<>();

    // Zoom and drag
    private double zoomTranslateX = 0.0;
    private double zoomTranslateY = 0.0;
    private double zoom = 1.0;
    private double startPressedX = 0.0;
    private double startPressedY = 0.0;
    private double lastDragX = 0.0;
    private double lastDragY = 0.0;

    // Koch manager
    // TO DO: Create class KochManager in package calculate
    private KochManager kochManager;

    // Current level of Koch fractal
    private int currentLevel = 1;

    // Labels for level, nr edges, calculation time, and drawing time
    private Label labelLevel;
    private Label labelNrEdges;
    private Label labelNrEdgesText;
    private Label labelCalc;
    private Label labelCalcText;
    private Label labelDraw;
    private Label labelDrawText;

    // Koch panel and its size
    private Canvas kochPanel;
    private final int kpWidth = 500;
    private final int kpHeight = 500;

    // Progress bars
    private Label labelLeftEdge;
    private ProgressBar pbLeft;
    private Label labelLeftNrEdges;
    private Label labelBottomEdge;
    private ProgressBar pbBottom;
    private Label labelBottomNrEdges;
    private Label labelRightEdge;
    private ProgressBar pbRight;
    private Label labelRightNrEdges;

    @Override
    public void start(Stage primaryStage) throws IOException {
        this.fileBtxt = new File("/media/Fractal/Btxt.txt");
        this.fileUtxt = new File("/media/Fractal/Utxt.txt");
        this.fileBbin = new File("/media/Fractal/Bbin.bin");
        this.fileUbin = new File("/media/Fractal/Ubin.bin");

        kochManager = new KochManager(this);
        // Define grid pane
        GridPane grid;
        grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        // For debug purposes
        // Make de grid lines visible
        // grid.setGridLinesVisible(true);
        // Drawing panel for Koch fractal
        kochPanel = new Canvas(kpWidth, kpHeight);
        grid.add(kochPanel, 0, 3, 25, 1);

        // Labels to present number of edges for Koch fractal
        labelNrEdges = new Label("Nr edges:");
        labelNrEdgesText = new Label();
        grid.add(labelNrEdges, 0, 0, 4, 1);
        grid.add(labelNrEdgesText, 3, 0, 22, 1);

        // Labels to present time of calculation for Koch fractal
        labelCalc = new Label("Calculating:");
        labelCalcText = new Label();
        grid.add(labelCalc, 0, 1, 4, 1);
        grid.add(labelCalcText, 3, 1, 22, 1);

        // Labels to present time of drawing for Koch fractal
        labelDraw = new Label("Drawing:");
        labelDrawText = new Label();
        grid.add(labelDraw, 0, 2, 4, 1);
        grid.add(labelDrawText, 3, 2, 22, 1);

        // Label to present current level of Koch fractal
        labelLevel = new Label("Level: " + currentLevel);
        grid.add(labelLevel, 0, 6);

        ArrayList<Edge> f;
        f = readEdges();
        try {
            f = readEdgesstring();
        } catch (IOException ex) {
            Logger.getLogger(JSF31KochFractalFX.class.getName()).log(Level.SEVERE, null, ex);
        }
        for (Edge e : f) {
            kochManager.addEdge(e);
        }
        kochManager.drawEdges(edges);

        // Button to fit Koch fractal in Koch panel
        Button buttonFitFractal = new Button();
        buttonFitFractal.setText("Fit Fractal");
        buttonFitFractal.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                fitFractalButtonActionPerformed(event);
            }

        });
        grid.add(buttonFitFractal, 14, 6);

        // Add mouse clicked event to Koch panel
        kochPanel.addEventHandler(MouseEvent.MOUSE_CLICKED,
                new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        kochPanelMouseClicked(event);
                    }
                });

        // Add mouse pressed event to Koch panel
        kochPanel.addEventHandler(MouseEvent.MOUSE_PRESSED,
                new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        kochPanelMousePressed(event);
                    }
                });

        // Add mouse dragged event to Koch panel
        kochPanel.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                kochPanelMouseDragged(event);
            }
        });

        // Create Koch manager and set initial level
        resetZoom();
        kochManager = new KochManager(this);

        // Create the scene and add the grid pane
        Group root = new Group();
        Scene scene = new Scene(root, kpWidth + 50, kpHeight + 180);
        root.getChildren().add(grid);

        // Define title and assign the scene for main window
        primaryStage.setTitle("Koch Fractal");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public void clearKochPanel() {
        GraphicsContext gc = kochPanel.getGraphicsContext2D();
        gc.clearRect(0.0, 0.0, kpWidth, kpHeight);
        gc.setFill(Color.BLACK);
        gc.fillRect(0.0, 0.0, kpWidth, kpHeight);
    }

    public void drawEdge(Edge e) {
        // Graphics
        GraphicsContext gc = kochPanel.getGraphicsContext2D();

        // Adjust edge for zoom and drag
        Edge e1 = edgeAfterZoomAndDrag(e);

        currentLevel = e1.level;
        labelLevel.setText("Level: " + currentLevel);

        // Set line color
        gc.setStroke(Color.web(e1.color));

        // Set line width depending on level
        if (currentLevel <= 3) {
            gc.setLineWidth(2.0);
        } else if (currentLevel <= 5) {
            gc.setLineWidth(1.5);
        } else {
            gc.setLineWidth(1.0);
        }

        // Draw line
        gc.strokeLine(e1.X1, e1.Y1, e1.X2, e1.Y2);
    }

    //Binding voor left-Edge
    public void bindLeftTask(Task leftTask) {
        pbLeft.progressProperty().bind(leftTask.progressProperty());
        labelLeftNrEdges.textProperty().bind(leftTask.messageProperty());
    }

    //Binding voor bottom-Edge
    public void bindBottomTask(Task bottomTask) {
        pbBottom.progressProperty().bind(bottomTask.progressProperty());
        labelBottomNrEdges.textProperty().bind(bottomTask.messageProperty());
    }

    //Binding voor right-Edge
    public void bindRightTask(Task rightTask) {
        pbRight.progressProperty().bind(rightTask.progressProperty());
        labelRightNrEdges.textProperty().bind(rightTask.messageProperty());
    }

    public void setTextNrEdges(String text) {
        labelNrEdgesText.setText(text);
    }

    public void setTextCalc(String text) {
        labelCalcText.setText(text);
    }

    public void setTextDraw(String text) {
        labelDrawText.setText(text);
    }

    public void requestDrawEdges() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                kochManager.drawEdges(edges);
            }
        });
    }

    private void increaseLevelButtonActionPerformed(ActionEvent event) {
        if (currentLevel < 12) {
            // resetZoom();
            currentLevel++;
            labelLevel.setText("Level: " + currentLevel);

        }
    }

    private void decreaseLevelButtonActionPerformed(ActionEvent event) {
        if (currentLevel > 1) {
            // resetZoom();
            currentLevel--;
            labelLevel.setText("Level: " + currentLevel);

        }
    }

    private void fitFractalButtonActionPerformed(ActionEvent event) {
        resetZoom();
        kochManager.drawEdges(edges);
    }

    private void kochPanelMouseClicked(MouseEvent event) {
        if (Math.abs(event.getX() - startPressedX) < 1.0
                && Math.abs(event.getY() - startPressedY) < 1.0) {
            double originalPointClickedX = (event.getX() - zoomTranslateX) / zoom;
            double originalPointClickedY = (event.getY() - zoomTranslateY) / zoom;
            if (event.getButton() == MouseButton.PRIMARY) {
                zoom *= 2.0;
            } else if (event.getButton() == MouseButton.SECONDARY) {
                zoom /= 2.0;
            }
            zoomTranslateX = (int) (event.getX() - originalPointClickedX * zoom);
            zoomTranslateY = (int) (event.getY() - originalPointClickedY * zoom);
            kochManager.drawEdges(edges);
        }
    }

    private void kochPanelMouseDragged(MouseEvent event) {
        zoomTranslateX = zoomTranslateX + event.getX() - lastDragX;
        zoomTranslateY = zoomTranslateY + event.getY() - lastDragY;
        lastDragX = event.getX();
        lastDragY = event.getY();
        kochManager.drawEdges(edges);
    }

    private void kochPanelMousePressed(MouseEvent event) {
        startPressedX = event.getX();
        startPressedY = event.getY();
        lastDragX = event.getX();
        lastDragY = event.getY();
    }

    private void resetZoom() {
        int kpSize = Math.min(kpWidth, kpHeight);
        zoom = kpSize;
        zoomTranslateX = (kpWidth - kpSize) / 2.0;
        zoomTranslateY = (kpHeight - kpSize) / 2.0;
    }

    private Edge edgeAfterZoomAndDrag(Edge e) {
        return new Edge(
                e.X1 * zoom + zoomTranslateX,
                e.Y1 * zoom + zoomTranslateY,
                e.X2 * zoom + zoomTranslateX,
                e.Y2 * zoom + zoomTranslateY,
                e.color, e.level);
    }

    public static ArrayList<Edge> readEdgesstring() throws FileNotFoundException, IOException {
        edges = new ArrayList<Edge>();
        TimeStamp timeStamp = new TimeStamp();
        timeStamp.setBegin("text");
        FileReader in = new FileReader(fileBtxt);
        BufferedReader br = new BufferedReader(in);

        String edgestring;
        while ((edgestring = br.readLine()) != null) {
            //String edgestring = br.readLine();
            String[] parts = edgestring.split(" ");
            Edge e = new Edge(Double.parseDouble(parts[0]), Double.parseDouble(parts[1]), Double.parseDouble(parts[2]), Double.parseDouble(parts[3]), parts[4], Integer.parseInt(parts[5]));
            edges.add(e);
        }
        in.close();
        timeStamp.setEnd();
        System.out.println(timeStamp.toString());
        return edges;
    }

    public static ArrayList<Edge> readEdges() {
        edges = new ArrayList<Edge>();
        TimeStamp timeStamp = new TimeStamp();
        timeStamp.setBegin("bin");
        try {
            ObjectInputStream Ois = new ObjectInputStream(new FileInputStream(fileBbin));
            int i = Ois.readInt();
            for (int x = 0; x < i; x++) {
                edges.add((Edge) Ois.readObject());
            }

        } catch (IOException ex) {
            Logger LOG = Logger.getLogger(JSF31KochFractalFX.class.getName());
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(JSF31KochFractalFX.class.getName()).log(Level.SEVERE, null, ex);
        }
        timeStamp.setEnd();
        System.out.println(timeStamp.toString());
        return edges;
    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Observer o = new JSF31KochFractalFX();
        launch(args);
        KochManager km = new KochManager(new JSF31KochFractalFX());
        ArrayList<Edge> f;
        f = readEdges();
        for (Edge e : f) {
            km.addEdge(e);
        }
        km.drawEdges(edges);
    }

    @Override
    public void update(Observable o, Object o1) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void requestDrawEdge(final Edge e) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                kochManager.drawEdge(e);
            }
        });
    }
}
