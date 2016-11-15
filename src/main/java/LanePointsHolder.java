import model.LaneType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LanePointsHolder {
    private final Map<LaneType, ArrayList<Point>> lane2PointListMap;

    private final Point topRune;
    private final Point bottomRune;

    public LanePointsHolder(double mapSize) {
        topRune = new Point(0.3 * mapSize, 0.3 * mapSize);
        bottomRune = new Point(0.7 * mapSize, 0.7 * mapSize);

        lane2PointListMap = new HashMap<>();

        lane2PointListMap.put(LaneType.TOP, new ArrayList<Point>() {{
            add(new Point(0.05 * mapSize, 0.95 * mapSize));
            add(new Point(0.05 * mapSize, 0.85 * mapSize));
            add(new Point(0.05 * mapSize, 0.75 * mapSize));
            add(new Point(0.05 * mapSize, 0.65 * mapSize));
            add(new Point(0.05 * mapSize, 0.55 * mapSize));
            add(new Point(0.05 * mapSize, 0.45 * mapSize));
            add(new Point(0.05 * mapSize, 0.35 * mapSize));
            add(new Point(0.05 * mapSize, 0.25 * mapSize));
            add(new Point(0.05 * mapSize, 0.15 * mapSize));
            add(new Point(0.05 * mapSize, 0.05 * mapSize));
            //-------------------------------------
            add(new Point(0.15 * mapSize, 0.05 * mapSize));
            add(new Point(0.25 * mapSize, 0.05 * mapSize));
            add(new Point(0.35 * mapSize, 0.05 * mapSize));
            add(new Point(0.45 * mapSize, 0.05 * mapSize));
            add(new Point(0.55 * mapSize, 0.05 * mapSize));
            add(new Point(0.65 * mapSize, 0.05 * mapSize));
            add(new Point(0.75 * mapSize, 0.05 * mapSize));
            add(new Point(0.85 * mapSize, 0.05 * mapSize));
            add(new Point(0.95 * mapSize, 0.05 * mapSize));
        }});

        lane2PointListMap.put(LaneType.BOTTOM, new ArrayList<Point>() {{
            add(new Point(0.05 * mapSize, 0.95 * mapSize));
            add(new Point(0.10 * mapSize, 0.95 * mapSize));
            add(new Point(0.15 * mapSize, 0.95 * mapSize));
            add(new Point(0.25 * mapSize, 0.95 * mapSize));
            add(new Point(0.35 * mapSize, 0.95 * mapSize));
            add(new Point(0.45 * mapSize, 0.95 * mapSize));
            add(new Point(0.55 * mapSize, 0.95 * mapSize));
            add(new Point(0.65 * mapSize, 0.95 * mapSize));
            add(new Point(0.75 * mapSize, 0.95 * mapSize));
            add(new Point(0.85 * mapSize, 0.95 * mapSize));
            add(new Point(0.95 * mapSize, 0.95 * mapSize));
            //-----------------------------------------
            add(new Point(0.95 * mapSize, 0.85 * mapSize));
            add(new Point(0.95 * mapSize, 0.75 * mapSize));
            add(new Point(0.95 * mapSize, 0.65 * mapSize));
            add(new Point(0.95 * mapSize, 0.55 * mapSize));
            add(new Point(0.95 * mapSize, 0.45 * mapSize));
            add(new Point(0.95 * mapSize, 0.35 * mapSize));
            add(new Point(0.95 * mapSize, 0.25 * mapSize));
            add(new Point(0.95 * mapSize, 0.15 * mapSize));
            add(new Point(0.95 * mapSize, 0.05 * mapSize));
        }});

        lane2PointListMap.put(LaneType.MIDDLE, new ArrayList<Point>() {{
            add(new Point(0.10 * mapSize, 0.95 * mapSize));
            add(new Point(0.15 * mapSize, 0.85 * mapSize));
            add(new Point(0.25 * mapSize, 0.75 * mapSize));
            add(new Point(0.35 * mapSize, 0.65 * mapSize));
            add(new Point(0.45 * mapSize, 0.55 * mapSize));
            add(new Point(0.55 * mapSize, 0.45 * mapSize));
            add(new Point(0.65 * mapSize, 0.35 * mapSize));
            add(new Point(0.75 * mapSize, 0.25 * mapSize));
            add(new Point(0.85 * mapSize, 0.15 * mapSize));
            add(new Point(0.95 * mapSize, 0.05 * mapSize));
        }});
    }

    public ArrayList<Point> getControlPointsForLane(LaneType laneType) {
        return lane2PointListMap.get(laneType);
    }

    public Point getTopRune() {
        return topRune;
    }

    public Point getBottomRune() {
        return bottomRune;
    }
}
