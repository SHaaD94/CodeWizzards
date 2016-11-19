import model.Wizard;

import java.util.ArrayList;

/**
 * Created by SHaaD on 19.11.2016.
 */
public class Utils {
    public static Point getNearestRune(LanePointsHolder lanePointsHolder, Wizard self) {
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

    public static int getNearestPoint(Wizard self, ArrayList<Point> points) {
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

}
