package game;

/**
 * Created by SHaaD on 13.11.2016.
 */
public class Utils {
    private Utils() {
    }

    public static Point getHalfwayPoint(double mapSize, LaneTypeExt laneType) {
        switch (laneType) {
            case BOTTOM:
                return new Point((int) (mapSize * 0.95), (int) (mapSize * 0.1));
            case MIDDLE:
                return new Point((int) (mapSize * 0.5), (int) (mapSize * 0.5));
            case TOP:
                return new Point((int) (mapSize * 0.1), (int) (mapSize * 0.95));
        }
        throw new UnsupportedOperationException("Unknown lane type");
    }

}
