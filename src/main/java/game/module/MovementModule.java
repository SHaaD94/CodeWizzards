package game.module;

import game.LaneTypeExt;
import game.Point;
import game.Utils;
import model.Game;
import model.Move;
import model.Wizard;
import model.World;

import static game.Constants.NOT_STUCK_TICKS;
import static game.Constants.TICKS_TO_STUCK;
import static game.LaneTypeExt.*;

public class MovementModule implements BehaviourModule {
    private final LaneTypeExt laneType;

    private Wizard prevWizard;
    private World prevWorld;
    private Move prevMove;

    private boolean isInitialized;

    private int stuckTicks;
    private int notStuckTicks;

    private Point halfWayPoint;

    public MovementModule(LaneTypeExt laneType) {
        this.laneType = laneType;
    }

    @Override
    public void updateMove(Wizard self, World world, Game game, Move move) {
        init(self, world, game, move);

        move.setSpeed(self.getLife() < self.getMaxLife() * 0.5 ? -game.getWizardForwardSpeed() : game.getWizardForwardSpeed());

        if (isStuck(self)) {
            move.setTurn(game.getWizardMaxTurnAngle());
            move.setSpeed(0);
        } else {
            double angle = getMoveAngle(self);
            System.out.println(angle);
            if (self.getAngle() != angle && notStuckTicks >= NOT_STUCK_TICKS) {
                move.setTurn(angle - self.getAngle());
            }
        }
        updateState(self, world, move);
    }

    private double getMoveAngle(Wizard self) {
        switch (laneType) {
            case BOTTOM:
                return self.getX() > halfWayPoint.getX() ? BOTTOM.getAngleAfterPoint() : BOTTOM.getAngleBeforePoint();
            case MIDDLE:
                // on middle it is always same
                return MIDDLE.getAngleAfterPoint();
            case TOP:
                return self.getY() > halfWayPoint.getY() ? TOP.getAngleAfterPoint() : TOP.getAngleBeforePoint();
        }
        throw new UnsupportedOperationException("Unknown lane type");
    }


    private void init(Wizard self, World world, Game game, Move move) {
        if (!isInitialized) {
            updateState(self, world, move);
            halfWayPoint = Utils.getHalfwayPoint(game.getMapSize(), laneType);
        }
        isInitialized = true;
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
