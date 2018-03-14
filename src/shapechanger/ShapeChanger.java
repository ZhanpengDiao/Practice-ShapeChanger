package shapechanger;

import java.io.*;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.transform.Scale;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.PathElement;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;

/**
 * Draws free-hand shapes and morphs one into another.
 *
 * @assignment author Zhanpeng Diao - u5788688
 * Date: 13/05/2016
 * @version 1.0
 *
 * @see Morph
 */

public class ShapeChanger extends Application {

    private static final double useOfScreenFactor = 0.8;

    private static Map<Predicate<KeyEvent>, Consumer<KeyEvent>> keyEventSelectors =
            new HashMap<>();
    // these are for smoother-like part, for drawing and splotching
    private final Path onePath = new Path();
    private final Path twoPath = new Path();
    private final ArrayList<Point> points = new ArrayList<>();
    private Scale scale;
    private Morph oneMorph;
    private Morph twoMorph;
    private Point2D anchorPt;
    private Point currentPoint;
    private Point lastPoint;
    private State state = State.CLEAR; // at the start there is no paths
    private boolean normalised = false;
    
    private boolean selectionMode = false; // the selection function - Task 2-2
    private Path selectedPath;
    private final Path ShapedPath = new Path();
    private final String fileSignal = "SHAPECHANGER_SAVE_FILE";
    private int sideOfPolygon = 0;

    /**
     * The entry point of application.
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {

        launch(args);
    }

    /**
     * override start method which creates the scene
     * and all nodes and shapes in it (main window only),
     * and redefines how the nodes react to user inputs
     * and other events;
     *
     * @param primaryStage Stage (the top level container)
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
//        Parent root = FXMLLoader.load(getClass().getResource("view.fxml"));

        primaryStage.setTitle("Shape Changing Objects");
        
        /* reading the screen size and using it to set up all
              * necessary dimensions, scaling factor and locations 
              */
        Rectangle2D screenBound = Screen.getPrimary().getBounds();
        double screenWidth = screenBound.getWidth();
        double screenHeight = screenBound.getHeight();
        
        /* Task 1 - Menubar Creation */
        
        /*menu item: file - open*/
        MenuItem iMenuOpen = new MenuItem("Open");
        iMenuOpen.setOnAction(event -> {
            try {
                File selectedFile = openFile(primaryStage);
                System.out.println("File Read: " + selectedFile);
            } catch (IOException ex) {
                System.out.println("File open error");
            }
        });
        
        MenuItem iMenuSave = new MenuItem("Save");
        iMenuSave.setOnAction(event -> {
            
            try {
                File selectedFile = saveFile(primaryStage);
                System.out.println("File Saved: " + selectedFile);
            } catch (IOException e) {
                System.out.println("File save error");
            }
        });
        
        MenuItem iMenuQuit = new MenuItem("Quit"); 
        iMenuQuit.setOnAction(event -> Platform.exit()); 
        Menu fileMenu = new Menu("File");
        fileMenu.getItems().addAll(iMenuOpen, iMenuSave, iMenuQuit);
        
        /*menu item: edit - select*/
        MenuItem iMenuSelect = new MenuItem("Select");
        iMenuSelect.setOnAction(event -> {
            if(state != State.CLEAR) {selectionMode = true;
            System.out.println("Selection mode is on!");}
        });
        
        /*menu: edit*/
        Menu editMenu = new Menu("Edit");
        editMenu.getItems().addAll(iMenuSelect);
        
        /*menu item: morph - triangle*/
        MenuItem iMenuTriangle = new MenuItem("Triangle");
        iMenuTriangle.setOnAction(event -> {
            if(selectedPath == null) {System.out.println("No path is selected");return;}
            
            System.out.println("Shape transforming: triangle");
            if(selectedPath == onePath) {
                oneMorph = oneMorph.triangleMorph();
                adjustPath(ShapedPath, oneMorph);   
                final Timeline timeline = makeTimeline(onePath, ShapedPath);
                timeline.play();
            } else {
                twoMorph = twoMorph.triangleMorph();
                adjustPath(ShapedPath, twoMorph);
                final Timeline timeline = makeTimeline(twoPath, ShapedPath);
                timeline.play();
            }
        });
        
        /*menu item: morph - rectangle*/
        MenuItem iMenuRectangle = new MenuItem("Rectangle");
        iMenuRectangle.setOnAction(event -> {
            if(selectedPath == null) {System.out.println("No path is selected");return;}
            
            System.out.println("Shape transforming: rectangle");
            if(selectedPath == onePath) {
                oneMorph = oneMorph.rectangleMorph();
                adjustPath(ShapedPath, oneMorph);
                final Timeline timeline = makeTimeline(onePath, ShapedPath);
                timeline.play();
            } else {
                twoMorph = twoMorph.rectangleMorph();
                adjustPath(ShapedPath, twoMorph);
                final Timeline timeline = makeTimeline(twoPath, ShapedPath);
                timeline.play();
            }
        });
        
        /*menu item: morph - ellipse*/
        MenuItem iMenuEllipse = new MenuItem("Ellipse");
        iMenuEllipse.setOnAction(event -> {
            if(selectedPath == null) {System.out.println("No path is selected");return;}
            
            System.out.println("Shape transforming: ellipse");
            if(selectedPath == onePath) {
                oneMorph = oneMorph.ellipseMorph();
                adjustPath(ShapedPath, oneMorph);
                final Timeline timeline = makeTimeline(onePath, ShapedPath);
                timeline.play();
            } else {
                twoMorph = twoMorph.ellipseMorph();
                adjustPath(ShapedPath, twoMorph);
                final Timeline timeline = makeTimeline(twoPath, ShapedPath);
                timeline.play();
            }
        });
        
        MenuItem iMenuPolygon = new MenuItem("Polygon");
        iMenuPolygon.setOnAction(event -> {
            if(selectedPath == null) {System.out.println("No path is selected");return;}
            
            popupDialogPolygon(primaryStage);
            if(sideOfPolygon < 3) {System.out.println("Polygon morphing error");return;}
            
            System.out.println("Shape transforming: polygon");
            if(selectedPath == onePath) {
                oneMorph = oneMorph.polygonMorph(sideOfPolygon);
                adjustPath(ShapedPath, oneMorph);
                final Timeline timeline = makeTimeline(onePath, ShapedPath);
                timeline.play();
            } else {
                twoMorph = twoMorph.polygonMorph(sideOfPolygon);
                adjustPath(ShapedPath, twoMorph);
                final Timeline timeline = makeTimeline(twoPath, ShapedPath);
                timeline.play();
            }
        });
        
        Menu morphMenu = new Menu("Morph");
        morphMenu.getItems().addAll(iMenuTriangle, iMenuRectangle, iMenuEllipse, iMenuPolygon);
        
        MenuBar menuBar = new MenuBar(fileMenu, editMenu, morphMenu);
        menuBar.setPrefWidth(screenWidth * useOfScreenFactor);

        /* next two lines are needed to read command-line args
         * -- such are JavaFX's awkward ways
         */
//        Parameters parameters = getParameters();
//        String fontFileName = parameters.getRaw().get(0);

        final Group root = new Group();
        // add paths
        root.getChildren().addAll(onePath, twoPath, menuBar);

        final Scene scene = new Scene(root, screenWidth * useOfScreenFactor,
                screenHeight * useOfScreenFactor, Color.WHEAT);

        // starting initial path
        scene.onMousePressedProperty().set(event ->
        {
            if(selectionMode == true) {return;}
            
            anchorPt = new Point2D(event.getX(), event.getY());
            // clean points which comprise a path to be drawn and start anew
            points.clear();
            points.add(Point.makePoint(anchorPt.getX(), anchorPt.getY()));
            if (state == State.BOTH) {
                state = State.CLEAR;
                selectionMode = false;
                selectedPath = null;
            }

            if (state == State.CLEAR) {
                state = State.ONE;
                normalised = false;
                // clear both paths
                onePath.getElements().clear();
                onePath.setOpacity(1);
                onePath.setFill(null);
                twoPath.getElements().clear();
                twoPath.setOpacity(1);
                twoPath.setFill(null);
                // start collecting points into path one
                onePath.setStrokeWidth(3);
                onePath.setStrokeDashOffset(0.7);
                onePath.setStroke(Color.BLACK);
                onePath.getElements()
                        .add(new MoveTo(anchorPt.getX(), anchorPt.getY()));
            } else {
                state = State.BOTH;
                // start collecting points into path two
                twoPath.setStrokeWidth(3);
                twoPath.setStrokeDashOffset(0.7);
                twoPath.setStroke(Color.BLACK);
                twoPath.getElements()
                        .add(new MoveTo(anchorPt.getX(), anchorPt.getY()));
            }
        });

        // dragging creates lineTos added to the path
        scene.onMouseDraggedProperty().set(event ->
        {
            if(selectionMode == true) {return;}
            
            currentPoint = Point.makePoint(event.getX(), event.getY());
            points.add(currentPoint);
            if (state == State.ONE) {
                onePath.getElements()
                        .add(new LineTo(currentPoint.x, currentPoint.y));
            } else if (state == State.BOTH) {
                twoPath.getElements()
                        .add(new LineTo(currentPoint.x, currentPoint.y));
            }
        });

        // end onePath or twoPath (depending on which
        // is being drawn) when mouse released event
        scene.onMouseReleasedProperty().set(event ->
        {
            if(selectionMode == true) {return;}
            
//            System.out.printf("Switching from %s -> ", state);
            lastPoint = Point.makePoint(event.getX(), event.getY());
            points.add(lastPoint);
            if (state == State.ONE) {
                onePath.getElements().add(new LineTo(lastPoint.x, lastPoint.y));
                onePath.getElements().add(new LineTo(anchorPt.getX(), anchorPt.getY()));
                onePath.setStrokeWidth(1);
                onePath.setFill(Color.DARKGRAY);
                oneMorph = new Morph(points);
                System.out.printf("morph one has %d points %n", oneMorph.points.size());
            } else if (state == State.BOTH) {
                twoPath.getElements().add(new LineTo(lastPoint.x, lastPoint.y));
                twoPath.getElements().add(new LineTo(anchorPt.getX(), anchorPt.getY()));
                twoPath.setStrokeWidth(1);
                twoPath.setFill(Color.DARKGRAY);
                twoMorph = new Morph(points);
                System.out.printf("morph two has %d points %n", twoMorph.points.size());
            }
            System.out.printf("%s%n", state);
            System.out.printf("The size of path %s is %d%n",
                    state == State.ONE ? "one" : "two", points.size());
        });
        
        /*Events under the selecion mode*/
        onePath.setOnMouseMoved(event -> {
            if(state != State.CLEAR && selectionMode == true) {
            onePath.setStrokeWidth(3);
            onePath.setStroke(Color.RED);}
        });
        
        onePath.setOnMouseExited(event -> {
            if(selectedPath == onePath) {onePath.setStrokeWidth(5); return;}
            if(state != State.CLEAR && selectionMode == true) {
            onePath.setStrokeWidth(1);
            onePath.setStroke(Color.BLACK);}
        });
        
        onePath.setOnMouseClicked(event -> {
            if(selectionMode == true) {
                if(selectedPath == twoPath) {
                    twoPath.setStrokeWidth(1);
                    twoPath.setStroke(Color.BLACK);}
                selectedPath = onePath;
                System.out.println("Selection Mode: Selected path is set: onePath");
                System.out.println("Selection mode is off!");
                event.consume();
            }
            selectionMode = false;
        });
        
        twoPath.setOnMouseMoved(event -> {
            if(state == State.BOTH && selectionMode == true) {
            twoPath.setStrokeWidth(3);
            twoPath.setStroke(Color.RED);}
        });
        
        twoPath.setOnMouseExited(event -> {
            if(selectedPath == twoPath) {twoPath.setStrokeWidth(5); return;}
            if(state == State.BOTH && selectionMode == true) {
            twoPath.setStrokeWidth(1);
            twoPath.setStroke(Color.BLACK);}
        });
        
        twoPath.setOnMouseClicked(event -> {
            if(selectionMode == true) {
                if(selectedPath == onePath) {
                    onePath.setStrokeWidth(1);
                    onePath.setStroke(Color.BLACK);}
                selectedPath = twoPath;
                System.out.println("Selection Mode: Selected path is set: twoPath");
                System.out.println("Selection mode is off!");
                event.consume();
            }
            selectionMode = false;
        });

        // simple event handlers (key board inputs which initiate transitions
        scene.onKeyPressedProperty().set(keyEvent ->
        {
            if (keyEvent.isMetaDown() && keyEvent.getCode() == KeyCode.M) {
                if (state != State.BOTH) {
                    System.out.println("Need BOTH paths to perform morphing");
                    return;
                }
                if (!normalised) {
                    System.out.println("Normalising before morphing can be attempted");
                    normalisePaths(onePath, twoPath, oneMorph, twoMorph);
                }
                if (onePath.getElements().size() > 0
                        && twoPath.getElements().size() > 0) {
                    //twoPath.setFill(Color.GRAY);
                    //twoPath.setOpacity(0.5);
//                    normalisePaths(onePath, twoPath);
                    final Timeline timeline = makeTimeline(onePath, twoPath);
                    timeline.play();
                    System.out.println("Morphing should be seen now");
                } else {
                    System.out.println("Paths are empty");
                }
            } else if (keyEvent.isMetaDown()
                    && keyEvent.getCode() == KeyCode.N
                    && state == State.BOTH) {
                System.out.println("Attempt to Normalise...");
                System.out.printf("oneMorph: %d, twoMorph: %d%n",
                        oneMorph.points.size(), twoMorph.points.size());
                // normalise longest morph to shortest
                normalisePaths(onePath, twoPath, oneMorph, twoMorph);
            } else if (keyEvent.isMetaDown() && keyEvent.getCode() == KeyCode.Q) {
//                System.exit(0);
                Platform.exit(); // better, JavaFX's way
            }
        });    

        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.setOnCloseRequest(e -> Platform.exit());

    }

    private void normalisePaths(Path p1, Path p2, Morph m1, Morph m2) {
        if (normalised) return;
        if (p1.getElements().size() > m2.points.size()) {
            p1.setOpacity(0.5);
            m1 = Morph.normalize(m1, m2.points.size());
            p1.setFill(null);
            adjustPath(p1, m1);
        } else if (p1.getElements().size() < m2.points.size()) {
            p2.setOpacity(0.5);
            m2 = Morph.normalize(m2, m1.points.size());
            p2.setFill(null);
            adjustPath(p2, m2);
            p2.setFill(Color.CORNSILK);
        }
        p1.setFill(Color.CORNSILK);
        p2.setOpacity(0.5);
        normalised = true;
    }

    private void adjustPath(Path path, Morph morph) {
        System.out.printf("size of path %d, size of morph %d%n",
                path.getElements().size(), morph.points.size());
        path.getElements().clear();
        double x0 = morph.anchorPoint().x;
        double y0 = morph.anchorPoint().y;
        path.getElements().add(new MoveTo(x0, y0));
        Point p;
        for (int i = 1; i < morph.points.size(); i++) {
            p = morph.points.get(i);
            path.getElements().add(new LineTo(p.x, p.y));
        }
        path.getElements().add(new LineTo(x0, y0));
    }

    private Timeline makeTimeline(Path p1, Path p2) {
        assert p1.getElements().size() == p2.getElements().size() : "uneven paths";
        final Timeline timeline = new Timeline();
        timeline.setCycleCount(1);//(Timeline.INDEFINITE);
        timeline.setAutoReverse(false);
        int n = p1.getElements().size();
        KeyValue kvx, kvy;
        KeyFrame kf;
        MoveTo ap1, ap2;
        LineTo pe1, pe2;
        ap1 = (MoveTo) p1.getElements().get(0);
        ap2 = (MoveTo) p2.getElements().get(0);
        kvx = new KeyValue(ap1.xProperty(), ap2.getX());
        kvy = new KeyValue(ap1.yProperty(), ap2.getY());
        kf = new KeyFrame(Duration.millis(5000), kvx, kvy);
        timeline.getKeyFrames().add(kf);
        for (int i = 1; i < n; i++) {
            pe1 = (LineTo) p1.getElements().get(i);
            pe2 = (LineTo) p2.getElements().get(i);
            kvx = new KeyValue(pe1.xProperty(), pe2.getX());
            kvy = new KeyValue(pe1.yProperty(), pe2.getY());
            kf = new KeyFrame(Duration.millis(5000), kvx, kvy);
            timeline.getKeyFrames().add(kf);
        }
        return timeline;
    }
    
    /* Task 2 - This method is used to open current shapes
        */
    private File openFile(Stage primaryStage) throws IOException {
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        fileChooser.getExtensionFilters().addAll(
            new ExtensionFilter("Text Files", "*.txt"));
        File selectedFile = fileChooser.showOpenDialog(primaryStage);
        FileReader fr;
        
        try {
            fr = new FileReader(selectedFile);
        } catch (NullPointerException e) {
            System.out.println("ERROR: File cannot be read");
            return null;
        }
        BufferedReader br = new BufferedReader(fr);
        
        /*verifying*/
        String stateStr = br.readLine();
        if(!stateStr.equals(fileSignal)) {
            System.out.println("ERROR: File cannot be read");
            return null;
        }
        /*cleaning all current states*/
        onePath.getElements().clear();
        twoPath.getElements().clear();
        state = State.CLEAR;
        selectedPath = null;
        selectionMode = false;
        
        stateStr = br.readLine();
        if(stateStr.equals("ONE")) {state = State.ONE;}
        else {state = State.BOTH;}
        
        onePath.getElements().clear();
        points.clear();
        String tempStr = br.readLine(); // read the first point (MoveTo)
        String codArray[] = tempStr.split("[^0-9.]+");
        double tempX = Double.valueOf(codArray[1]);
        double tempY = Double.valueOf(codArray[2]);
        onePath.getElements().add(new MoveTo(tempX, tempY));
        points.add(Point.makePoint(tempX, tempY));
        
        while(true) {
            tempStr = br.readLine();
            if(tempStr.equals("ONE_PATH_END")) {break;}
            codArray = tempStr.split("[^0-9.]+");
            tempX = Double.valueOf(codArray[1]);
            tempY = Double.valueOf(codArray[2]);
            onePath.getElements().add(new LineTo(tempX, tempY));
            points.add(Point.makePoint(tempX, tempY));
        }
        onePath.setStrokeWidth(1);
        onePath.setStroke(Color.BLACK);
        onePath.setFill(Color.DARKGRAY);
        oneMorph = new Morph(points);
        
        /*the case that the path two exists*/
        if(state == State.BOTH) {
            twoPath.getElements().clear();
            points.clear();
            tempStr = br.readLine(); // read the first point (MoveTo)
            codArray = tempStr.split("[^0-9.]+");
            tempX = Double.valueOf(codArray[1]);
            tempY = Double.valueOf(codArray[2]);
            twoPath.getElements().add(new MoveTo(tempX, tempY));
            points.add(Point.makePoint(tempX, tempY));
            
            while(true) {
            tempStr = br.readLine();
            if(tempStr.equals("TWO_PATH_END")) {break;}
            codArray = tempStr.split("[^0-9.]+");
            tempX = Double.valueOf(codArray[1]);
            tempY = Double.valueOf(codArray[2]);
            twoPath.getElements().add(new LineTo(tempX, tempY));
            points.add(Point.makePoint(tempX, tempY));
            }
            
            twoPath.setStrokeWidth(1);
            twoPath.setStroke(Color.BLACK);
            twoPath.setFill(Color.DARKGRAY);
            twoMorph = new Morph(points);
            
            selectedPath = null;
            br.close();
        }
        
        return selectedFile;
    }

    /* Task 2 - This method is used to save current shapes, into TXT file.
        */
    private File saveFile(Stage primaryStage) throws IOException {
        if(state == State.CLEAR) {
            System.out.println("No drwan shape is found");
            return null;
        }
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        fileChooser.getExtensionFilters().addAll(
            new ExtensionFilter("Text Files", "*.txt"));
        File savedFile = fileChooser.showSaveDialog(primaryStage);
        FileWriter fw;
        
        try {
            fw = new FileWriter(savedFile.getAbsoluteFile());
        } catch (NullPointerException e) {
            System.out.println("ERROR: Shapes cannot be saved");
            return null;
        }    
        
        BufferedWriter bw = new BufferedWriter(fw);
        
        ObservableList<PathElement> elementList = onePath.getElements();
        bw.write(fileSignal + "\n");
        bw.write(state.toString() + "\n");
        for (PathElement ele : elementList) {
            bw.write(ele.toString() + "\n");
        }
        bw.write("ONE_PATH_END\n");
        
        if(state == State.BOTH) {
            elementList = twoPath.getElements();
            for (PathElement ele : elementList) {
            bw.write(ele.toString() + "\n");
            }
            bw.write("TWO_PATH_END\n");
        }
        bw.write("FINISHED\n");
        bw.flush();
        bw.close();
        
        return savedFile;
    }
    
    /*
        * Task 1 - this popup dialogue window request the number of sides of a polygon
        */
    private void popupDialogPolygon(Stage primaryStage) {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(15, 15, 15, 15));
        
        Text sceneTitle = new Text("Polygon");
        sceneTitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        grid.add(sceneTitle, 0, 0);
        
        Label userName = new Label("Input the number of sides:");
        grid.add(userName, 0, 1);

        TextField userTextField = new TextField();
        grid.add(userTextField, 0, 2);
        
        Button btn = new Button("OK");
        btn.setAlignment(Pos.BOTTOM_RIGHT);
        grid.add(btn, 1, 5);
        
        final Text actiontarget = new Text();
        grid.add(actiontarget, 0, 6);

        final Stage currentStage = new Stage();
        //Initialize the Stage with type of modal
        currentStage.initModality(Modality.APPLICATION_MODAL);
        //Set the owner of the Stage 
        currentStage.initOwner(primaryStage);
        currentStage.setTitle("Polygon Morphing");
        
        btn.setOnAction(event -> {
            String inputStr = userTextField.getText();
            int input;
            try {
                input = Integer.valueOf(inputStr);
                if(input < 3) {
                    actiontarget.setFill(Color.FIREBRICK);
                    actiontarget.setText("Input Should Be Greater Than 3");
                    return;
                }
                this.sideOfPolygon = input;
                currentStage.hide();
            } catch (NumberFormatException ne) {
                actiontarget.setFill(Color.FIREBRICK);
                actiontarget.setText("Invalid Input");
            }
        });

        Scene scene = new Scene(grid, 300, 250);
        currentStage.setScene(scene);
        currentStage.showAndWait();
    }
        
    enum State {CLEAR, ONE, BOTH} /* to control the keyboard/mouse input */

}
