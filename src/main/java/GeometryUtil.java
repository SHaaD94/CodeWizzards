import model.CircularUnit;

import static java.lang.StrictMath.hypot;

/**
 * Created by SHaaD on 13.11.2016.
 */
public class GeometryUtil {
    public static boolean areCollides(CircularUnit u1, CircularUnit u2) {
        return areCollides(u1.getX(), u1.getY(), u1.getRadius(), u2.getX(), u2.getY(), u2.getRadius());
    }

    public static boolean areCollides(double x1, double y1, double r1, double x2, double y2, double r2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double rr = r1 + r2;
        return Math.pow(dx, 2) + Math.pow(dy, 2) < Math.pow(rr, 2);
    }

    public static Point rotateByAngle(double angle, double x, double y, double xc, double yc) {
        double newX = Math.cos(angle) * (x - xc) - Math.sin(angle) * (y - yc) + xc;
        double newY = Math.sin(angle) * (x - xc) + Math.cos(angle) * (y - yc) + yc;
        return new Point(newX, newY);
    }

    public static Point getNextIterationPosition(double angle, double x, double y) {
        return GeometryUtil.rotateByAngle(angle, x + 4, y, x, y);
    }

    public static double getDistanceBetweenPoints(Point p1, Point p2) {
        return hypot(p1.getX() - p2.getX(), p1.getY() - p2.getY());
    }
}
