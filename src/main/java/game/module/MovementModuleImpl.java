package game.module;

import model.Game;
import model.Move;
import model.Wizard;
import model.World;

/**
 * Created by SHaaD on 12.11.2016.
 */
public class MovementModuleImpl implements MovementModule {
    private final static int TICKS_TO_STUCK = 2;
    private final static int NOT_STUCK_TICKS = 2;
    private double prevX, prevY;

    private boolean isInitialized;
    private int stuckTicks;

    private int notStuckTicks;

    @Override
    public void changeMovement(Wizard self, World world, Game game, Move move) {
        checkCoordsInitialized(self);
        move.setSpeed(game.getWizardForwardSpeed());

        if (isStuck(self)) {
            move.setTurn(game.getWizardMaxTurnAngle());
            move.setSpeed(0);
        } else {
            if (self.getAngle() != 0 && notStuckTicks >= NOT_STUCK_TICKS) {
                move.setTurn(-self.getAngle());
            }
        }

        updateCoords(self);
    }


    private void checkCoordsInitialized(Wizard self) {
        if (!isInitialized) {
            updateCoords(self);
            isInitialized = true;
        }
    }

    private void updateCoords(Wizard self) {
        prevX = self.getX();
        prevY = self.getY();
    }

    private boolean isStuck(Wizard self) {
        if (prevY == self.getY() && prevX == self.getX()) {
            if (stuckTicks == TICKS_TO_STUCK) {
                stuckTicks = 0;
                notStuckTicks = 0;
                return true;
            } else {
                stuckTicks++;
                return false;
            }
        }
        notStuckTicks++;
        return false;
    }
}
