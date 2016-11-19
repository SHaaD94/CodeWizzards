import org.junit.Assert;
import org.junit.Test;

/**
 * Created by SHaaD on 15.11.2016.
 */
public class GeometryUtilsTest {
    @Test
    public void areCollidesEqual() throws Exception {
        double x1 = 1, y1 = 1, r1 = 1;
        double x2 = 1, y2 = 1, r2 = 1;

        boolean areCollides = GeometryUtil.areCollides(x1, y1, r1, x2, y2, r2);

        Assert.assertTrue(areCollides);
    }

    @Test
    public void areCollidesIntersecting() throws Exception {
        double x1 = 0, y1 = 0, r1 = 1;
        double x2 = 2, y2 = 2, r2 = 2;

        boolean areCollides = GeometryUtil.areCollides(x1, y1, r1, x2, y2, r2);

        Assert.assertTrue(areCollides);
    }

    @Test
    public void areCollidesIntersectingBelowZero() throws Exception {
        double x1 = 0, y1 = 0, r1 = 1;
        double x2 = -2, y2 = -2, r2 = 2;

        boolean areCollides = GeometryUtil.areCollides(x1, y1, r1, x2, y2, r2);

        Assert.assertTrue(areCollides);
    }

    @Test
    public void shouldNotCollide() throws Exception {
        double x1 = 0, y1 = 0, r1 = 1;
        double x2 = 5, y2 = 5, r2 = 1;

        boolean areCollides = GeometryUtil.areCollides(x1, y1, r1, x2, y2, r2);

        Assert.assertFalse(areCollides);
    }

    @Test
    public void shouldCollide() throws Exception {
        double x1 = 264.4620171830669, y1 = 3584.823934093803, r1 = 35;
        double x2 = 400, y2 = 3600, r2 = 100;

        boolean areCollides = GeometryUtil.areCollides(x1, y1, r1, x2, y2, r2);

        Assert.assertFalse(areCollides);
    }

}