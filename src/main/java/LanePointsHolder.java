import model.LaneType;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

class LanePointsHolder {
    private final Map<LaneType, ArrayList<Point>> lane2PointListMap;
    private final Map<LaneType, Point> lane2CreepSpawnPoint;
    private final List<Point> runeAttractionPoints;

    private final Point topRune;
    private final Point bottomRune;

    LanePointsHolder(double mapSize) {
        topRune = new Point(0.3 * mapSize, 0.3 * mapSize);
        bottomRune = new Point(0.7 * mapSize, 0.7 * mapSize);

        lane2PointListMap = new EnumMap<LaneType, ArrayList<Point>>(LaneType.class) {
            {
                put(LaneType.TOP, new ArrayList<Point>() {{
                    add(new Point(0.05 * mapSize, 0.95 * mapSize));
                    add(new Point(0.05 * mapSize, 0.90 * mapSize));
                    add(new Point(0.05 * mapSize, 0.85 * mapSize));
                    add(new Point(0.05 * mapSize, 0.80 * mapSize));
                    add(new Point(0.05 * mapSize, 0.75 * mapSize));
                    add(new Point(0.05 * mapSize, 0.70 * mapSize));
                    add(new Point(0.05 * mapSize, 0.65 * mapSize));
                    add(new Point(0.05 * mapSize, 0.60 * mapSize));
                    add(new Point(0.05 * mapSize, 0.55 * mapSize));
                    add(new Point(0.05 * mapSize, 0.50 * mapSize));
                    add(new Point(0.05 * mapSize, 0.45 * mapSize));
                    add(new Point(0.05 * mapSize, 0.40 * mapSize));
                    add(new Point(0.05 * mapSize, 0.35 * mapSize));
                    add(new Point(0.05 * mapSize, 0.30 * mapSize));
                    add(new Point(0.05 * mapSize, 0.25 * mapSize));
                    add(new Point(0.05 * mapSize, 0.20 * mapSize));
                    add(new Point(0.05 * mapSize, 0.15 * mapSize));
                    add(new Point(0.10 * mapSize, 0.15 * mapSize));
                    add(new Point(0.15 * mapSize, 0.15 * mapSize));
                    //-------------------------------------
                    add(new Point(0.15 * mapSize, 0.05 * mapSize));
                    add(new Point(0.20 * mapSize, 0.05 * mapSize));
                    add(new Point(0.25 * mapSize, 0.05 * mapSize));
                    add(new Point(0.30 * mapSize, 0.05 * mapSize));
                    add(new Point(0.35 * mapSize, 0.05 * mapSize));
                    add(new Point(0.40 * mapSize, 0.05 * mapSize));
                    add(new Point(0.45 * mapSize, 0.05 * mapSize));
                    add(new Point(0.50 * mapSize, 0.05 * mapSize));
                    add(new Point(0.55 * mapSize, 0.05 * mapSize));
                    add(new Point(0.60 * mapSize, 0.05 * mapSize));
                    add(new Point(0.65 * mapSize, 0.05 * mapSize));
                    add(new Point(0.70 * mapSize, 0.05 * mapSize));
                    add(new Point(0.75 * mapSize, 0.05 * mapSize));
                    add(new Point(0.80 * mapSize, 0.05 * mapSize));
                    add(new Point(0.85 * mapSize, 0.05 * mapSize));
                    add(new Point(0.95 * mapSize, 0.05 * mapSize));
                }});
                put(LaneType.BOTTOM, new ArrayList<Point>() {{
                    add(new Point(0.05 * mapSize, 0.95 * mapSize));
                    add(new Point(0.10 * mapSize, 0.95 * mapSize));
                    add(new Point(0.15 * mapSize, 0.95 * mapSize));
                    add(new Point(0.20 * mapSize, 0.95 * mapSize));
                    add(new Point(0.25 * mapSize, 0.95 * mapSize));
                    add(new Point(0.30 * mapSize, 0.95 * mapSize));
                    add(new Point(0.35 * mapSize, 0.95 * mapSize));
                    add(new Point(0.40 * mapSize, 0.95 * mapSize));
                    add(new Point(0.45 * mapSize, 0.95 * mapSize));
                    add(new Point(0.50 * mapSize, 0.95 * mapSize));
                    add(new Point(0.55 * mapSize, 0.95 * mapSize));
                    add(new Point(0.60 * mapSize, 0.95 * mapSize));
                    add(new Point(0.65 * mapSize, 0.95 * mapSize));
                    add(new Point(0.70 * mapSize, 0.95 * mapSize));
                    add(new Point(0.75 * mapSize, 0.95 * mapSize));
                    add(new Point(0.80 * mapSize, 0.95 * mapSize));
                    add(new Point(0.85 * mapSize, 0.95 * mapSize));
                    add(new Point(0.9 * mapSize, 0.9 * mapSize));
                    //-----------------------------------------
                    add(new Point(0.95 * mapSize, 0.9 * mapSize));
                    add(new Point(0.95 * mapSize, 0.85 * mapSize));
                    add(new Point(0.95 * mapSize, 0.80 * mapSize));
                    add(new Point(0.95 * mapSize, 0.75 * mapSize));
                    add(new Point(0.95 * mapSize, 0.70 * mapSize));
                    add(new Point(0.95 * mapSize, 0.65 * mapSize));
                    add(new Point(0.95 * mapSize, 0.60 * mapSize));
                    add(new Point(0.95 * mapSize, 0.55 * mapSize));
                    add(new Point(0.95 * mapSize, 0.50 * mapSize));
                    add(new Point(0.95 * mapSize, 0.45 * mapSize));
                    add(new Point(0.95 * mapSize, 0.40 * mapSize));
                    add(new Point(0.95 * mapSize, 0.30 * mapSize));
                    add(new Point(0.95 * mapSize, 0.35 * mapSize));
                    add(new Point(0.95 * mapSize, 0.20 * mapSize));
                    add(new Point(0.95 * mapSize, 0.25 * mapSize));
                    add(new Point(0.95 * mapSize, 0.10 * mapSize));
                    add(new Point(0.95 * mapSize, 0.15 * mapSize));
                    add(new Point(0.95 * mapSize, 0.05 * mapSize));
                }});

                put(LaneType.MIDDLE, new ArrayList<Point>() {{
                    add(new Point(0.10 * mapSize, 0.95 * mapSize));
                    add(new Point(0.15 * mapSize, 0.85 * mapSize));
                    add(new Point(0.20 * mapSize, 0.80 * mapSize));
                    add(new Point(0.25 * mapSize, 0.75 * mapSize));
                    add(new Point(0.30 * mapSize, 0.70 * mapSize));
                    add(new Point(0.35 * mapSize, 0.65 * mapSize));
                    add(new Point(0.30 * mapSize, 0.60 * mapSize));
                    add(new Point(0.45 * mapSize, 0.55 * mapSize));
                    add(new Point(0.5 * mapSize, 0.5 * mapSize));
                    add(new Point(0.55 * mapSize, 0.45 * mapSize));
                    add(new Point(0.60 * mapSize, 0.40 * mapSize));
                    add(new Point(0.65 * mapSize, 0.35 * mapSize));
                    add(new Point(0.70 * mapSize, 0.30 * mapSize));
                    add(new Point(0.75 * mapSize, 0.25 * mapSize));
                    add(new Point(0.80 * mapSize, 0.20 * mapSize));
                    add(new Point(0.85 * mapSize, 0.15 * mapSize));
                    add(new Point(0.90 * mapSize, 0.10 * mapSize));
                    add(new Point(0.95 * mapSize, 0.05 * mapSize));
                }});
            }
        };

        runeAttractionPoints = new ArrayList<Point>() {
            {
                add(new Point(0.05 * mapSize, 0.05 * mapSize));
                add(new Point(0.15 * mapSize, 0.15 * mapSize));
                add(new Point(0.15 * mapSize, 0.05 * mapSize));
                add(new Point(0.25 * mapSize, 0.05 * mapSize));
                add(new Point(0.35 * mapSize, 0.05 * mapSize));
                add(new Point(0.05 * mapSize, 0.15 * mapSize));
                add(new Point(0.05 * mapSize, 0.25 * mapSize));

                add(new Point(0.1 * mapSize, 0.1 * mapSize));
                add(new Point(0.2 * mapSize, 0.1 * mapSize));
                add(new Point(0.2 * mapSize, 0.2 * mapSize));
                add(new Point(0.3 * mapSize, 0.3 * mapSize));
                add(new Point(0.4 * mapSize, 0.4 * mapSize));

                add(new Point(0.45 * mapSize, 0.55 * mapSize));
                add(new Point(0.5 * mapSize, 0.5 * mapSize));
                add(new Point(0.53 * mapSize, 0.47 * mapSize));

                add(new Point(0.6 * mapSize, 0.6 * mapSize));
                add(new Point(0.7 * mapSize, 0.7 * mapSize));
                add(new Point(0.8 * mapSize, 0.8 * mapSize));
                add(new Point(0.9 * mapSize, 0.9 * mapSize));
                add(new Point(0.65 * mapSize, 0.95 * mapSize));
                add(new Point(0.75 * mapSize, 0.95 * mapSize));
                add(new Point(0.85 * mapSize, 0.95 * mapSize));
                add(new Point(0.95 * mapSize, 0.85 * mapSize));

                add(new Point(0.95 * mapSize, 0.65 * mapSize));
                add(new Point(0.95 * mapSize, 0.75 * mapSize));
                add(new Point(0.95 * mapSize, 0.85 * mapSize));

                add(new Point(0.95 * mapSize, 0.95 * mapSize));
                add(new Point(0.93 * mapSize, 0.93 * mapSize));
            }
        };

        lane2CreepSpawnPoint = new EnumMap<LaneType, Point>(LaneType.class) {{
            put(LaneType.TOP, new Point(0.95 * mapSize, 0.3 * mapSize));
            put(LaneType.MIDDLE, new Point(0.8 * mapSize, 0.2 * mapSize));
            put(LaneType.BOTTOM, new Point(0.7 * mapSize, 0.05 * mapSize));
        }};
    }

    ArrayList<Point> getControlPointsForLane(LaneType laneType) {
        return lane2PointListMap.get(laneType);
    }

    Point getCreepSpawnPoint(LaneType laneType) {
        return lane2CreepSpawnPoint.get(laneType);
    }

    public List<Point> getRuneAttractionPoints() {
        return runeAttractionPoints;
    }

    Point getTopRune() {
        return topRune;
    }

    Point getBottomRune() {
        return bottomRune;
    }
}
