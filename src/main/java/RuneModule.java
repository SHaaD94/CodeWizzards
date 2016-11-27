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

            //please don't simplify this if, it's too gross
            boolean runePickedUpOrDoesntExist;
            if (distanceToNearestRune <= self.getRadius()) {
                runePickedUpOrDoesntExist = true;
            } else if (distanceToNearestRune <= self.getVisionRange() && bonusCount != 0) {
                runePickedUpOrDoesntExist = false;
            } else if (runeMayAppearAfterReaching(self, world, game, distanceToNearestRune)) {
                runePickedUpOrDoesntExist = false;
            } else {
                runePickedUpOrDoesntExist = true;
            }

            if (runePickedUpOrDoesntExist) {
                State.setBehaviour(State.BehaviourType.NONE);
                State.increaseLastRuneIndex();
            } else {
                State.setBehaviour(State.BehaviourType.GOING_FOR_RUNE);
            }
        }

    }

    private boolean runeMayExistRightNow(World world, Game game) {
        return world.getTickIndex() / game.getBonusAppearanceIntervalTicks() > State.getLastRuneIndex();
    }

    private boolean runeMayAppearAfterReaching(Wizard self, World world, Game game, double distanceToNearestRune) {
        return (world.getTickIndex() + getTicksToReachRune(self, game, distanceToNearestRune)) + 10 /
                game.getBonusAppearanceIntervalTicks() > world.getTickIndex() / game.getBonusAppearanceIntervalTicks();
    }

    private boolean shouldGoForRune(Wizard self, World world, Game game) {
        if (State.getBehaviour() == State.BehaviourType.ESCAPING) {
            //todo check this out
            return false;
        }

        Point nearestRune = Utils.getNearestRune(lanePointsHolder, self);
        double distanceToNearestRune = self.getDistanceTo(nearestRune.getX(), nearestRune.getY());

        boolean nearAttractionPoint = lanePointsHolder.getRuneAttractionPoints()
                .stream()
                .anyMatch(x -> self.getDistanceTo(x.getX(), x.getY()) <= Constants.RUNE_ATTRACT_RADIUS);

        if (!nearAttractionPoint) {
            return false;
        }

        int runeInterval = game.getBonusAppearanceIntervalTicks();

        int currentTickWithTimeForReachingRune = world.getTickIndex() + getTicksToReachRune(self, game, distanceToNearestRune);

        return currentTickWithTimeForReachingRune / runeInterval > State.getLastRuneIndex()
                || world.getTickIndex() / runeInterval > State.getLastRuneIndex();
    }

    private int getTicksToReachRune(Wizard self, Game game, double distanceToNearestRune) {
        return (int) ((distanceToNearestRune - self.getRadius()) * 1.05 / game.getWizardForwardSpeed());
    }
}
