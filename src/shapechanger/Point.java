package shapechanger;

/**
 *
 * @assignment author Zhanpeng Diao - u5788688
 * Date: 13/05/2016
 */

class Point {

    public final double x, y;

    Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public static Point makePoint(double x, double y) {
        return new Point(x, y);
    }
}
