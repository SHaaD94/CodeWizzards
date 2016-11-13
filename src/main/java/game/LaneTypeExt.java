package game;

import static java.lang.Math.PI;

/**
 * Created by SHaaD on 12.11.2016.
 */
public enum LaneTypeExt {
    //up and right, down and left
    TOP(-PI / 2, 0),
    //up right or down left
    MIDDLE(-PI / 4, -PI / 4),
    //right and up, left and down
    BOTTOM(0, -PI / 2);

    private final double angleBeforePoint;
    private final double angleAfterPoint;

    LaneTypeExt(double angleBeforePoint, double angleAfterPoint) {
        this.angleBeforePoint = angleBeforePoint;
        this.angleAfterPoint = angleAfterPoint;
    }

    public double getAngleBeforePoint() {
        return angleBeforePoint;
    }

    public double getAngleAfterPoint() {
        return angleAfterPoint;
    }
}
