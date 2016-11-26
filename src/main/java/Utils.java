import model.Faction;
import model.LivingUnit;
import model.Wizard;
import model.World;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * Created by SHaaD on 19.11.2016.
 */
class Utils {
    static Point getNearestRune(LanePointsHolder lanePointsHolder, Wizard self) {
        double distanceToTopRune = self.getDistanceTo(lanePointsHolder.getTopRune().getX(), lanePointsHolder.getTopRune().getY());
        double distanceToBottomRune = self.getDistanceTo(lanePointsHolder.getBottomRune().getX(), lanePointsHolder.getBottomRune().getY());
        Point nearestRune;
        if (distanceToBottomRune < distanceToTopRune) {
            nearestRune = lanePointsHolder.getBottomRune();
        } else {
            nearestRune = lanePointsHolder.getTopRune();
        }
        return nearestRune;
    }

    static int getNearestControlPointIndex(Wizard self, List<Point> points) {
        int minIndex = 0;
        double minDistance = Double.MAX_VALUE;
        for (int i = 0; i < points.size(); i++) {
            double currentDistance = self.getDistanceTo(points.get(i).getX(), points.get(i).getY());
            if (currentDistance < minDistance) {
                minDistance = currentDistance;
                minIndex = i;
            }
        }
        return minIndex;
    }

    static int getNearestSafeControlPointIndex(Wizard self, World world, List<Point> points) {
        int minIndex = 0;
        double minDistance = Double.MAX_VALUE;
        for (int i = 0; i < points.size(); i++) {
            double currentDistance = self.getDistanceTo(points.get(i).getX(), points.get(i).getY());
            if (!isSafe(self, world, points.get(i))) {
                break;
            }
            if (currentDistance < minDistance) {
                minDistance = currentDistance;
                minIndex = i;
            }
        }
        return minIndex;
    }

    private static boolean isSafe(Wizard self, World world, Point point) {
        boolean wizardIsPresent = Arrays.stream(world.getWizards())
                .filter(x -> x.getFaction() != self.getFaction())
                .filter(x -> GeometryUtil.getDistanceBetweenPoints(point, x) <= Constants.POINT_SAFE_RANGE)
                .findFirst().isPresent();
        boolean buildingIsPresent = Arrays.stream(world.getBuildings())
                .filter(x -> x.getFaction() != self.getFaction())
                .filter(x -> GeometryUtil.getDistanceBetweenPoints(point, x) <= Constants.POINT_SAFE_RANGE)
                .findFirst().isPresent();
        long enemyMinionCount = Arrays.stream(world.getMinions())
                .filter(x -> x.getFaction() != Faction.NEUTRAL && x.getFaction() != self.getFaction())
                .filter(x -> GeometryUtil.getDistanceBetweenPoints(point, x) <= Constants.POINT_SAFE_RANGE)
                .count();
        return !(wizardIsPresent || buildingIsPresent || enemyMinionCount >= 2);
    }

    static Stream<LivingUnit> getLivingUnitStream(World world) {
        Stream<LivingUnit> units = Arrays.stream(world.getWizards());
        units = Stream.concat(units, Arrays.stream(world.getBuildings()));
        units = Stream.concat(units, Arrays.stream(world.getMinions())
                .filter(x -> !(x.getFaction() == Faction.NEUTRAL && x.getRemainingActionCooldownTicks() == 0)));
        return units;
    }

}
