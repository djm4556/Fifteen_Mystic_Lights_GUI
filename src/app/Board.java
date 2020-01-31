package app;

import java.security.InvalidParameterException;
import java.time.Instant; //Timed RNG
import java.util.Random;

/**
 * The Board class represents a full board of tiles.
 * All tile logic is delegated to the individual tiles,
 * which may refer back to their board. This class is only
 * for linking to the GUI and generating/organizing tiles.
 */
public class Board {
    private final Random random; //Timed RNG
    private Tile[][] tiles; //Tile "board"
    private boolean guiReady = false;
    private final int size; //2+
    private final GUI gui;

    /**
     * Delegates to the below constructor w/ size 4
     * @param gui The GUI to link to the board
     */
    public Board(GUI gui) {
        this(gui, 4);
    } //4 size = 15 lights

    /**
     * Creates and generates randomization and a board
     * @param gui The GUI to link to the board
     * @param size The size of the board
     */
    public Board(GUI gui, int size) {
        if(size < 2) //If size is too small, error out
            throw new InvalidParameterException("Board size below 2 received");
        this.size = size; //Otherwise, set the size and continue doing things
        this.gui = gui; //GUI stored to allow for updating its shown board
        random = new Random(Instant.now().toEpochMilli());
        tiles = new Tile[size][size];
        generate();
    }

    /**
     * Randomly generates a new board with one black tile
     */
    public void generate() {
        for(int row = 0; row < size; row++) {
            for(int col = 0; col < size; col++) {
                tiles[row][col] = new Tile(this, row, col,
                        random.nextBoolean() ? 1 : -1);
            } //End column-counting loop
        } //End row-counting loop
        int blackRow = random.nextInt(size);
        int blackCol = random.nextInt(size);
        tiles[blackRow][blackCol].setState(0);
        if(isSolved()) { //If the new board is solved...
            tiles[0][0].flipCheck();
            tiles[0][1].flipCheck();
        } //Flip two tiles so it won't be solved
        if(guiReady) //If the GUI is initialized, update
            gui.update(this, true);
    } //End generate method

    /**
     * Initialization method to communicate GUI readiness
     */
    public void initialize() {
        guiReady = true;
    }

    /**
     * Presses a tile and updates the GUI
     * @param row The row of the tile to press
     * @param col The column of the tile to press
     */
    public void press(int row, int col) { //Validation (should be in bounds)
        if(row < 0 || row >= size || col < 0 || col >= size) //(SIZE is 1 over)
            throw new InvalidParameterException("Invalid tile location received");
        tiles[row][col].press(); //If validation fails, error out, else press the tile
        gui.update(this, false); //Updates the GUI
        gui.setMove(size, row, col); //Updates the move
    }

    /**
     * Checks the board for being solved, meaning all
     * tiles (except the black tile) are the same state
     * @return Whether the board is solved
     */
    public boolean isSolved() {
        int first = tiles[0][0].getState();
        if(first == 0) //First non-black state
            first = tiles[0][1].getState();
        //All tiles must match first or be the black tile to solve
        for(int row = 0; row < size; row++) {
            for(int col = 0; col < size; col++) {
                int current = tiles[row][col].getState();
                if(current != first && current != 0)
                    return false; //Invalid tile for solving
            } //End column-counting loop
        } //End row-counting loop
        gui.stopSolving(); //Stops solver
        return true; //All tiles valid for solving
    }

    /**
     * Get method to access all tiles
     * @return The full board of tiles
     */
    public Tile[][] getTiles() {
        return tiles;
    }

    /**
     * Get method to access size
     * @return The board's size
     */
    public int getSize() {
        return size;
    }
}
