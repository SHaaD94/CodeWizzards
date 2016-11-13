import model.CircularUnit;

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
}
