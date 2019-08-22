import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

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

    public static double getIntersection(Coordinate[] coordinates1, Coordinate[] coordinates2) { //交集
        // TODO Auto-generated method stub
        Geometry g1 = new GeometryFactory().createPolygon(coordinates1);
        Geometry g2 = new GeometryFactory().createPolygon(coordinates2);
        Geometry intersection = g1.intersection(g2);
        return intersection.getArea();
    }

    public static boolean withinGeo(Coordinate[] room, Coordinate[] points) { // 点在多边形内
        // TODO Auto-generated method stub
        Geometry g = new GeometryFactory().createPolygon(room);
        Point point1 = new GeometryFactory().createPoint(points[0]);
        Point point2 = new GeometryFactory().createPoint(points[1]);
        return point1.within(g) && point2.within(g);
    }

    public static double getDistance(Coordinate[] room, Coordinate[] area, double angle) {
        // TODO Auto-generated method stub
        Coordinate[] line = new Coordinate[2];
        double distance = Double.MAX_VALUE;
        double k = 0;
        double tmp;
        switch((int)angle) {
            case 0: {
                line[0] = area[0];
                line[1] = area[1];
                break;
            }
            case 1: {
                line[0] = area[1];
                line[1] = area[2];
                break;
            }
            case 2: {
                line[0] = area[2];
                line[1] = area[3];
                break;
            }
            case 3: {
                line[0] = area[3];
                line[1] = area[4];
                break;
            }
        }
        if(line[0].x == line[1].x) k = 1;
        if(line[0].y == line[1].y) k = 0;
        for(int i = 0; i < room.length - 1; i++) {
            if(room[i].x == room[i + 1].x && k == 1 && withinGeo(room, line)) {
                tmp = Math.abs(line[0].x - room[i].x);
                if(tmp < distance) distance = tmp;
            } else if(room[i].y == room[i + 1].y && k == 0 && withinGeo(room, line)) {
                tmp = Math.abs(line[0].y - room[i].y);
                if(tmp < distance) distance = tmp;
            }
        }
        return distance;
    }

    public static void main(String[] args) {

    }
}
