package fr.lezoo.stonks.api.quotation.board;

public class BoardItemFrame {
    private final Board board;
    private final int i, j;

    /**
     * The entity id of the item frame being used
     * to display the quotation board
     */
    private final int entityId;

    public BoardItemFrame(Board board, int i, int j) {
        this.board = board;
        this.i = i;
        this.j = j;
        this.entityId = 0;
    }

    // TODO
}
