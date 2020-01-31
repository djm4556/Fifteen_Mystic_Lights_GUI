package app;

import java.security.InvalidParameterException;

/**
 * The Tile class represents a single tile of the puzzle.
 * Its state can be forcibly set during board generation or
 * tile "movement", which just swaps two adjacent tiles' states.
 */
public class Tile {
    private final Board board; //The board this tile belongs to
    private int state; /** 1=yellow, -1=blue, 0=black */
    private int row; //Location in board
    private int col;

    /**
     * Creates a new tile for the board
     * @param board Board with tile
     * @param row Row in board
     * @param col Column in board
     */
    public Tile(Board board, int row, int col, int state) {
        this.board = board;
        this.row = row;
        this.col = col;
        setState(state);
    }

    /**
     * Sets the tile's state manually
     * @param state The state to set
     */
    public void setState(int state) {
        if(state < -1 || state > 1) //Validation (should be -1 to 1)
            throw new InvalidParameterException("Invalid tile state received");
        this.state = state; //If validation fails, error out, else set this tile's state
    }

    /**
     * Get method to access state
     * @return The tile's state
     */
    public int getState() {
        return state;
    }

    /**
     * Flips the tile's state and checks if it's black
     * @return Whether the square was black
     */
    public boolean flipCheck() {
        return (state *= -1) == 0;
    }

    /**
     * Presses the tile, which flips it and adjacent ones,
     * "moving" the black tile here if it's adjacent
     */
    public void press() { //Flip this tile, and if it's not the black tile...
        if (!flipCheck()) { //Toggle the adjacent tiles
            toggle(row + 1, col);
            toggle(row - 1, col);
            toggle(row, col + 1);
            toggle(row, col - 1);
        }
    }

    /**
     * Toggles a targeted tile and handles "movement" if needed
     * @param row Row of the target tile
     * @param col Column of the target tile
     */
    public void toggle(int row, int col) { //If the row and column are valid...
        if(row > -1 && row < board.getSize() && col > -1 && col < board.getSize()) {
            Tile target = board.getTiles()[row][col]; //Get the tile from the board
            if (target.flipCheck()) { //Flip it, and if it's the black tile...
                target.setState(state); //Move this tile's state over
                setState(0); //And the black tile's state back
            } //End tile movement mechanic
        } //End validation
    } //End method
} //End class
