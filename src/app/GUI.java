package app;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.time.Duration;
import java.time.Instant;

/**
 * The GUI class serves as a GUI for the board.
 * It contains the main method and runs an app.
 */
public class GUI extends Application {
    //Width can be as big as desired, but going below ~450 may screw up the bottom panel
    //For reference, 450 is super-compact, 500 is compact, 550 is medium, 600 is cozy
    private final double WIDTH = 600; //Window width (px), height will be 20 more
    private final Color[] colors = {Color.BLUE, Color.BLACK, Color.YELLOW};
    private GridPane center, bottom; //Color indexing and grid panes
    private boolean touched = false; //Was a tile touched yet?
    private boolean solving = false; //For blocking input
    private Instant startTime; //For solve timer

    /**
     * The start method for the application
     * @param stage Stage for the application
     * @throws Exception If black magic happens
     */
    @Override
    public void start(Stage stage) throws Exception {
        Board board = new Board(this); //Setup
        center = makeCenterPane(board);
        bottom = makeBottomPane(board);
        FlowPane flow = new FlowPane();

        //Adds the panes together and makes/sets the scene
        flow.getChildren().addAll(center, bottom);
        Scene scene = new Scene(flow);
        stage.setScene(scene);
        if(stage.getScene() != scene)
            throw new Exception("Black magic");
        stage.setTitle("15+ Mystic Lights Puzzle");
        board.initialize(); //End of initialization
        stage.show(); //Prepares and shows the stage
    }

    /**
     * Method used to set the "last move" text correctly
     * @param size The size of the board (usually below 27)
     * @param row The row that was last pressed/moved
     * @param col The column that was last pressed/moved
     */
    public void setMove(int size, int row, int col) {
        Node move = bottom.getChildren().get(4);
        assert move.getClass() == Text.class;
        if (size < 27) //Should be true, but check just in case
            ((Text)move).setText("Last: " + (char) (col + 'a') + (row + 1));
        else //To whoever triggers this case: you absolute madman, this is insanely big!
            ((Text)move).setText("Last: C" + (col + 1) + " R" + (row + 1));
    }

    /**
     * Helper method used to create the central grid pane
     * @param board The board to link to the grid pane
     * @return A grid pane with buttons for tiles
     */
    private GridPane makeCenterPane(Board board) {
        ToggleGroup group = new ToggleGroup();
        GridPane pane = new GridPane(); //Empty grid pane
        final double sSize = WIDTH / board.getSize(); //Square size
        for(int row = 0; row < board.getSize(); row++) { //For each tile in the board...
            for(int col = 0; col < board.getSize(); col++) { //Make a square with the right size+color...
                Rectangle square = new Rectangle(sSize, sSize, colors[board.getTiles()[row][col].getState() + 1]);
                ToggleButton button = new ToggleButton("", square); //Use that square to make a new button...
                button.setPadding(new Insets(0, 0, 0, 0)); //Remove the very evil padding...
                button.setToggleGroup(group); //Add the button to the 1 toggle group for all buttons...
                pane.add(button, col, row); //Add the button to the pane, and set its action.
                final int finalRow = row, finalCol = col; //(Lambdas only use finals.)
                button.setOnAction(event -> {
                    if(!solving) { //Blocked if solving
                        if (!touched) { //If this is the first press...
                            touched = true; //Mark the board as touched...
                            Node timer = bottom.getChildren().get(2);
                            assert timer.getClass() == Text.class;
                            ((Text) timer).setText("Time: Running!");
                            startTime = Instant.now(); //and go!
                        } //And no matter what, press the tile
                        board.press(finalRow, finalCol);
                    } //End check for solving variable
                }); //End button action setting
            } //End column-counting loop
        } //End row-counting loop
        return pane; //Returns the now-full grid pane
    }

    /**
     * Helper method used to create an additional grid pane
     * @param board The board to link to the grid pane
     * @return A grid pane with miscellaneous info
     */
    private GridPane makeBottomPane(Board board) {
        GridPane pane = new GridPane();
        for(int i = 0; i < 5; i++) //Ensure the cells are all sized OK
            pane.getColumnConstraints().add(new ColumnConstraints(WIDTH / 5));
        pane.getRowConstraints().add(new RowConstraints(25));

        Text status = new Text("Board generated");
        pane.add(status, 0, 0); //Status (left)
        Button regen = new Button("REGENERATE");
        regen.setPrefWidth(WIDTH / 5); //Preferred width
        regen.setOnAction(event -> {
            if(!solving) //Blocked if solving
                board.generate();
        }); //Regenerate (mid-left)
        pane.add(regen, 1, 0);

        Text timer = new Text("Time: Waiting...");
        pane.add(timer, 2, 0); //Timer (middle)
        Button solve = new Button("AUTO SOLVE");
        solve.setPrefWidth(WIDTH / 5); //Preferred width
        solve.setOnAction(event -> {
            if(!solving) { //No duplicates
                solving = true; //Begin solving
                touched = true; //Lock timer's text
                startTime = Instant.now(); //Set timer
                Solver solver = new Solver(board, timer);
                Thread solveThread = new Thread(solver::solve);
                solveThread.start(); //Actually solve
            } //End solver action setting
        }); //Auto-solver (mid-right)
        pane.add(solve, 3, 0);
        Text move = new Text("No moves made");
        pane.add(move, 4, 0); //Move (right)

        return pane; //Returns the now-full grid pane
    }

    /**
     * Remotely unlocks input after the board is done solving
     */
    public void stopSolving() {
        solving = false;
    }

    /**
     * Method called by the board to keep the GUI caught up
     * @param board The board to reference when updating
     * @param newBoard Whether the board is brand new
     */
    public void update(Board board, boolean newBoard) {
        for (int row = 0; row < board.getSize(); row++) { //Then, iteratively:
            for (int col = 0; col < board.getSize(); col++) { //Get the target color...
                int color = board.getTiles()[row][col].getState() + 1; //the toggle button...
                Object tile = center.getChildren().toArray()[row * board.getSize() + col];
                assert tile.getClass() == ToggleButton.class; //(1D representation of board)
                Node square = ((ToggleButton) tile).getGraphic(); //the graphic...
                assert square.getClass() == Rectangle.class; //and update.
                ((Rectangle) square).setFill(colors[color]);
            } //End of column-counting loop
        } //End of row-counting loop
        Node status = bottom.getChildren().get(0);
        assert status.getClass() == Text.class; //Set status text
        if(newBoard) { //If the board is brand new from generation...
            touched = false; //Get it back to new condition
            ((Text) status).setText("Board generated");
            Node timer = bottom.getChildren().get(2);
            assert timer.getClass() == Text.class;
            ((Text) timer).setText("Time: Waiting...");
            Node move = bottom.getChildren().get(4);
            assert move.getClass() == Text.class;
            ((Text) move).setText("No moves made");
        } else if(board.isSolved()) { //Otherwise, if it's solved...
            Instant finishTime = Instant.now(); //Stop timing, and convert
            long minutes = Duration.between(startTime, finishTime).toMinutes();
            int seconds = Duration.between(startTime, finishTime).toSecondsPart();
            int millis = Duration.between(startTime, finishTime).toMillisPart();
            String time = String.format("%d:%02d.%03d", minutes, seconds, millis);
            Node timer = bottom.getChildren().get(2); //Now display the time...
            assert timer.getClass() == Text.class;  //And mark solved
            ((Text) timer).setText("Time: " + time);
            ((Text) status).setText("Board solved :D"); //If it's unsolved, set...
        } else ((Text) status).setText("Getting there..."); //an encouraging message.
    }

    /**
     * The main method, which launches the application
     * @param args Command line args (ignored)
     */
    public static void main(String[] args) {
        Application.launch();
    }
}
