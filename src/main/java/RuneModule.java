import model.Game;
import model.Move;
import model.Wizard;
import model.World;

import java.util.Arrays;

/**
 * Created by SHaaD on 20.11.2016.
 */
class RuneModule implements BehaviourModule {

    private final LanePointsHolder lanePointsHolder;

    RuneModule(LanePointsHolder lanePointsHolder) {
        this.lanePointsHolder = lanePointsHolder;
    }

    @Override
    public void updateMove(Wizard self, World world, Game game, Move move) {
        if (shouldGoForRune(self, world, game)) {
            Point nearestRune = Utils.getNearestRune(lanePointsHolder, self);

            double distanceToNearestRune = self.getDistanceTo(nearestRune.getX(), nearestRune.getY());

            long bonusCount = Arrays.stream(world.getBonuses())
                    .filter(x -> self.getDistanceTo(x) <= Constants.RUNE_SCAN_DISTANCE).count();

            boolean runePickedUpOrDoesntExist = distanceToNearestRune == 0 ||
                    (distanceToNearestRune <= self.getVisionRange() - 20 && bonusCount == 0);

            if (runePickedUpOrDoesntExist && getTicksToReachRune(self, game, distanceToNearestRune) < 1) {
                State.setBehaviour(State.BehaviourType.NONE);
                State.increaseLastRuneIndex();
            } else {
                State.setBehaviour(State.BehaviourType.GOING_FOR_RUNE);
            }
        }
    }

    private boolean shouldGoForRune(Wizard self, World world, Game game) {
        Point nearestRune = Utils.getNearestRune(lanePointsHolder, self);
        double distanceToNearestRune = self.getDistanceTo(nearestRune.getX(), nearestRune.getY());

        if (distanceToNearestRune > Constants.RUNE_ATTRACT_RADIUS) {
            return false;
        }

        int runeInterval = game.getBonusAppearanceIntervalTicks();

        int currentTickWithTimeForReachingRune = world.getTickIndex() + getTicksToReachRune(self, game, distanceToNearestRune);

        return currentTickWithTimeForReachingRune / runeInterval > State.getLastRuneIndex()
                || world.getTickIndex() / runeInterval > State.getLastRuneIndex();
    }

    private int getTicksToReachRune(Wizard self, Game game, double distanceToNearestRune) {
        return (int) ((distanceToNearestRune - self.getRadius()) / game.getWizardForwardSpeed()) + 1;
    }
}
