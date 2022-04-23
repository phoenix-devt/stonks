package fr.lezoo.stonks.display.board;

public class BoardMapInfo {
    private final Board board;
    private final int x;
    private final int y;

    //x=0 and y=0 correspoinds to the top left corner of the board

    public BoardMapInfo(Board board, int x, int y) {
        this.board = board;
        this.x = x;
        this.y = y;
    }
}
