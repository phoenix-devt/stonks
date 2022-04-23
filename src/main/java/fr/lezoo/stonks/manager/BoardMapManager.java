package fr.lezoo.stonks.manager;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.display.board.Board;
import fr.lezoo.stonks.display.board.BoardMapInfo;

import java.awt.image.BufferedImage;
import java.util.HashMap;

public class BoardMapManager {
    private HashMap<BoardMapInfo,BufferedImage> boardMap= new HashMap<BoardMapInfo, BufferedImage>();


    public BufferedImage getMapImage(BoardMapInfo info) {
        return boardMap.get(info);
    }

    /**
     * Add all the BufferedImages corresponding to the board
     * @param board
     */
    public void add(Board board) {
        BufferedImage image= board.getImage();
        for(int x=0; x< board.getWidth();x++) {
            for(int y=0;y<board.getHeight();y++) {
                boardMap.put(new BoardMapInfo(board,x,board.getHeight() - y - 1),image.getSubimage(128 * x, 128 * (board.getHeight() - y - 1), 128, 128));
            }
        }
    }

    public void refresh() {
        //We clear the old boardMap and add back all the new images
        boardMap.clear();
        for(Board board: Stonks.plugin.boardManager.getBoards())
            add(board);
    }

    /**
     * Must be loaded after the boards
     */
    public void load() {
        for(Board board: Stonks.plugin.boardManager.getBoards())
            add(board);
    }
}
