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
 * Allows to cast a ray and look for the
 * display board the player is looking at
 * <p>
 * Source: https://stackoverflow.com/questions/5666222/3d-line-plane-intersection
 */
public class BoardRaycast {
    private final Board found;
    private final double verticalOffset, horizontalOffset;

    public BoardRaycast(Player player) {
        Location source = player.getEyeLocation();
        for (Board board : Stonks.plugin.boardManager.getBoards()) {
            Vector planePoint = board.getLocation().toVector(); // TODO
            Vector planeNormal = board.getBoardFace().getDirection();
            if (planeNormal.dot(source.getDirection()) == 0)
                continue;

            double t = (planeNormal.dot(planePoint) - planeNormal.dot(source.toVector())) / planeNormal.dot(source.getDirection());
            if (t < 0 || t > Stonks.plugin.configManager.maxInteractionDistance)
                continue;

            // Coordinates of intersection point relative to the board
            Vector relative = source.toVector().add(source.getDirection().multiply(t)).subtract(planePoint);
            double verticalOffset = 1 - relative.getY() / board.getHeight();
            double horizontalOffset = (relative.dot(board.getBoardFace().getDirection().rotateAroundY(Math.PI / 2))) / board.getWidth();

            // If we are really clicking on a board we check where it has been clicked and stop the method
            if (horizontalOffset >= 0 && horizontalOffset <= 1 && verticalOffset >= 0 && verticalOffset <= 1) {
                found = board;
                this.verticalOffset = verticalOffset;
                this.horizontalOffset = horizontalOffset;
                return;
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
