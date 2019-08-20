import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

public class SAutil {
    // coordinates2 - coordinates1 差值面积
    public static double getDifference(Coordinate[] coordinates1, Coordinate[] coordinates2) {
        // TODO Auto-generated method stub
        Geometry g1 = new GeometryFactory().createPolygon(coordinates1);
        Geometry g2 = new GeometryFactory().createPolygon(coordinates2);
//        System.out.println("全面积:" + g1.getArea());
//        System.out.println("代理区域面积" + g2.getArea());
        Geometry difference = g2.difference(g1);
        return difference.getArea();
    }

    public static double getArea(Coordinate[] coordinates) {
        // TODO Auto-generated method stub
        Geometry g = new GeometryFactory().createPolygon(coordinates);
        return g.getArea();
    }

    public static double getIntersection(Coordinate[] coordinates1, Coordinate[] coordinates2) {
        // TODO Auto-generated method stub
        Geometry g1 = new GeometryFactory().createPolygon(coordinates1);
        Geometry g2 = new GeometryFactory().createPolygon(coordinates2);
        Geometry intersection = g1.intersection(g2);
        return intersection.getArea();
    }

    public static void main(String[] args) {

    }
}
