package fr.lezoo.stonks.display.board;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.util.Utils;
import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Allows to cast a ray and look for the display board
 * the player is looking at
 */
public class BoardRaycast {
    private final Board found;
    private final double verticalOffset, horizontalOffset;

    public BoardRaycast(Player player) {
        Location location = player.getEyeLocation();
        for (Board board : Stonks.plugin.boardManager.getBoards()) {
            // We get the perpendicular straight line
            Location boardLocation = board.getLocation().clone();
            Vector perpendicular = Utils.rotateAroundY(board.getBoardFace()).getDirection();
            double scalar = (boardLocation.clone().subtract(player.getLocation()).toVector().dot(perpendicular));

            // If the scalar product is positive we are behind the block and if it is too big we are too far
            if (scalar < 0 && scalar > -Stonks.plugin.configManager.maxInteractionDistance) {

                // Origine de l'offset arrondi a la valeur pour le board
                // Between  and 0 and 1, represents where the board is
                double verticalOffset = (boardLocation.getY() + board.getHeight() - location.getY()) / board.getHeight();

                // The same, we use a scalar product
                double horizontalOffset = location.subtract(boardLocation).toVector().dot(board.getBoardFace().getDirection()) / board.getWidth();

                // If we are really clicking on a board we check where it has been clicked and stop the method
                if (horizontalOffset >= 0 && horizontalOffset <= 1 && verticalOffset >= 0 && verticalOffset <= 1) {
                    found = board;
                    this.verticalOffset = verticalOffset;
                    this.horizontalOffset = horizontalOffset;
                    return;
                }
            }
        }

        found = null;
        verticalOffset = 0;
        horizontalOffset = 0;
    }

    public boolean hasHit() {
        return found != null;
    }

    @NotNull
    public Board getHit() {
        return Objects.requireNonNull(found);
    }

    public double getVerticalCoordinate() {
        Validate.notNull(found, "No display board found");
        return verticalOffset;
    }

    public double getHorizontalCoordinate() {
        Validate.notNull(found, "No display board found");
        return horizontalOffset;
    }
}
