package game.module;

import game.LaneTypeExt;
import model.*;

import java.awt.*;

import static game.LaneTypeExt.BOTTOM;
import static game.LaneTypeExt.MIDDLE;
import static game.LaneTypeExt.TOP;
import static java.lang.Math.PI;

public class MovementModuleImpl implements MovementModule {
    private final static int TICKS_TO_STUCK = 2;
    private final static int NOT_STUCK_TICKS = 2;

    private final LaneTypeExt laneType;

    private Wizard prevWizard;
    private World prevWorld;
    private Move prevMove;

    private boolean isInitialized;

    private int stuckTicks;
    private int notStuckTicks;

    private Point halfWayPoint;

    public MovementModuleImpl(LaneTypeExt laneType) {
        this.laneType = laneType;
    }

    @Override
    public void changeMovement(Wizard self, World world, Game game, Move move) {
        init(self, world, game, move);
        move.setSpeed(game.getWizardForwardSpeed());

        if (isStuck(self)) {
            move.setTurn(game.getWizardMaxTurnAngle());
            move.setSpeed(0);
        } else {
            double angle = 0;
            switch (laneType) {
                case BOTTOM:
                    angle = self.getX() > halfWayPoint.getX() ? BOTTOM.getAngleAfterPoint() : BOTTOM.getAngleBeforePoint();
                    break;
                case MIDDLE:
                    // on middle it is always same
                    angle = MIDDLE.getAngleAfterPoint();
                    break;
                case TOP:
                    angle = self.getY() > halfWayPoint.getY() ? TOP.getAngleAfterPoint() : TOP.getAngleBeforePoint();
                    break;
            }
            if (self.getAngle() != angle && notStuckTicks >= NOT_STUCK_TICKS) {
                move.setTurn(angle - self.getAngle());
            }
        }
        updateState(self, world, move);
    }


    private void init(Wizard self, World world, Game game, Move move) {
        if (!isInitialized) {
            updateState(self, world, move);
            double mapSize = game.getMapSize();
            switch (laneType) {
                case BOTTOM:
                    halfWayPoint = new Point((int) (mapSize * 0.95), (int) (mapSize * 0.1));
                    break;
                case MIDDLE:
                    halfWayPoint = new Point((int) (mapSize * 0.5), (int) (mapSize * 0.5));
                    break;
                case TOP:
                    halfWayPoint = new Point((int) (mapSize * 0.1), (int) (mapSize * 0.95));
                    break;
                default:
                    throw new UnsupportedOperationException("Unknown lane type");
            }
            isInitialized = true;
        }
    }

    private void updateState(Wizard self, World world, Move move) {
        prevWizard = self;
        prevWorld = world;
        prevMove = move;
    }

    private boolean isStuck(Wizard self) {
        if (prevWizard.getY() == self.getY() && prevWizard.getX() == self.getX()) {
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
