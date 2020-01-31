package app;

import javafx.scene.text.Text;

/**
 * The solver class is used to auto-solve the board.
 * Note that a 2*2 board is too small for some algorithms.
 */
public class Solver {
    //WARNING: Very short delays may cause errors! (20 seems to work)
    private static final long DELAY = 1000;
    private boolean done = false;
    private Tile[][] tiles;
    private Board board;
    private Text step;
    private int size;
    private int blackRow;
    private int blackCol;

    /**
     * Sets up the board/related info, finds the black square
     * @param board The board to be used for solving
     * @param step Text to use for feedback
     */
    public Solver(Board board, Text step) {
        this.board = board; //Setup of fields
        this.step = step; //Shows the current step
        size = board.getSize(); //Stored for speed
        tiles = board.getTiles(); //Finds black
        for(int row = 0; row < size; row++) {
            for(int col = 0; col < size; col++) {
                if(tiles[row][col].getState() == 0) {
                    blackRow = row;
                    blackCol = col;
                    return;
                } //End black-checking if block
            } //End column-counting loop
        } //End row-counting loop
    } //End constructor

    /**
     * Helper method used to press a square and check for solve
     * @param row The row of the tile to be pressed
     * @param col The column of the tile to be pressed
     * @return Whether the board is solved
     */
    private boolean pressCheck(int row, int col) {
        board.press(row, col); //Press the tile
        sleep(); //Sleep after all presses
        return done = board.isSolved();
    } //Could get lucky

    /**
     * Helper method used to wait and handle any interruptions
     */
    private void sleep() {
        try { Thread.sleep(DELAY); } //Try to sleep...
        catch (InterruptedException ignored) { System.exit(1); }
    } //If interrupted, end the entire program with an exit status of 1

    /**
     * Helper method used to finish solving, or close if unsolved
     */
    private void finish() {
        if(!done) { //Close if it didn't solve
            step.setText("ERROR: CLOSING");
            for(int i = 0; i < 5; i++)
                sleep(); //5* delay
            System.exit(1);
        } else step.setText("Input unlocked");
    } //Unlock input if it did solve (which it should)

    /**
     * Helper method used by solve() when the board size is 2
     * (The algorithm is different thanks to small size)
     */
    private void solve2() { //Step 1: get state diagonal to black
        int dState = tiles[1 - blackRow][1 - blackCol].getState();
        step.setText("2*2: Memorized"); //Step 2: check side of black
        sleep(); //Step 3: cover the three cases of a wrong light
        if(tiles[1 - blackRow][blackCol].getState() != dState) {
            if(!pressCheck(blackRow, 1 - blackCol)) //2 of 3 cases
                pressCheck(1 - blackRow, 1 - blackCol);
        } else pressCheck(1 - blackRow, blackCol); //3rd case
        finish(); //Finish the solve
    }

    /**
     * Main public-facing method for solving any size board
     */
    public void solve() {
        if(board.isSolved()) { //Don't double-solve
            step.setText("Already solved");
            return; //Input unlocks on check
        } //End already-solved check
        step.setText("Solving board");
        sleep(); //Sleep to show message
        if(size == 2) { //Special case
            solve2(); //Separate algorithm
            return; //End after solving the 2*2
        } //If it's not a 2*2, use the main algorithm

        step.setText("Aligning black");
        //Step 1: move black to top-left corner for consistency
        while(!done && blackRow != 0) { //Move up first
            pressCheck(blackRow - 1, blackCol);
            blackRow--; } //End of moving black up
        while(!done && blackCol != 0) { //Move left next
            pressCheck(blackRow, blackCol - 1);
            blackCol--; } //End of moving black left
        if(done) { finish(); return; } //Finish if done by luck

        step.setText("Raking rows up");
        //Step 2: move all the off-color lights up to the top
        int goal = 0; //Sign = frequenter last row color
        Tile[] lastRow = tiles[size - 1];
        for(Tile tile : lastRow) {
            goal += tile.getState();
        } //Goal -1 if negative, else 1
        goal = goal < 0 ? -1 : 1; //For all but the top row...
        for(int row = size - 1; row > 0; row--) { //Go right to left
            for(int col = size - 1; col >= 0; col--) {
                if(tiles[row][col].getState() != goal) {

                    //If a tile is the wrong state, rake it up
                    if(row == 1 && col == blackCol) {
                        //If it's right below black, move black RLR
                        if(pressCheck(row - 1, col + 1)
                                || pressCheck(row - 1, col)
                                || pressCheck(row - 1, col + 1)) {
                            finish(); //Finish if solved
                            return; //End of special movement solve check
                        } blackCol++; //Black moves 1 right
                    } else { //For generic movements, just press the above square
                        if(pressCheck(row - 1, col)) {
                            finish(); //Finish if solved
                            return;
                        } //End of generic movement solve check
                        if(row == 1 && Math.abs(blackCol - col) == 1)
                            blackCol = col; //If black moved sideways, update
                    } //If the generic movement moved black down, put it back up
                    if(row == 2 && col == blackCol) {
                        if(pressCheck(row - 2, col)) {
                            finish(); //Finish if solved
                            return;
                        } //End of put-back movement solve check
                    } //End of black-moved-down case

                } //End of difference-checking if
            } //End of column-counting loop
        } //End of row-counting loop

        step.setText("Working the top");
        //Step 3: solve the top row w/ back-and-forth movements
        //This step should ALWAYS solve the board if things go right
        boolean workLeft = true; //Stores potential of unfinished "work"
        int offset = 0; //Stores offset from black column to toggle column
        while(workLeft) { //While the top row could still have work to do...
            workLeft = false; //Assume there's nothing until something shows up
            if(blackCol != 0 && tiles[0][blackCol - 1].getState() != goal) {
                //If the tile to the left is of the wrong state...
                workLeft = true; //That tile must be changed
                offset = -1; //Use an offset of -1 to go left
            } else if(blackCol != size - 1 && tiles[0][blackCol + 1].getState() != goal) {
                //Otherwise, if the tile to the right is of the wrong state...
                workLeft = true; //That tile must be changed
                offset = 1; //Use an offset of +1 to go right
            } //End potential toggle offset detection

            if(workLeft) { //If toggling work was found, toggle
                if(pressCheck(1, blackCol + offset)
                        || pressCheck(0, blackCol + offset)
                        || pressCheck(1, blackCol + offset)
                        || pressCheck(0, blackCol + offset))
                    break; //Break to finish if solved
                blackCol += offset; //Black moves 1 in direction of offset
            } else if(blackCol  < size - 2) { //Otherwise, if black can move right 2...
                workLeft = true; //Make it do that to find more work
                if(pressCheck(0, blackCol + 1)
                        || pressCheck(0, blackCol + 2)
                        || pressCheck(0, blackCol + 1)
                        || pressCheck(0, blackCol + 2))
                    break; //Break to finish if solved
                blackCol += 2; //Black moves 2 right (faster than 1)
            } //End double if/else chain (nothing applies means no work left)
        } //After the board is finally solved (or broken)...
        finish(); //Finish the solve
    } //End of solve method
} //End of class
