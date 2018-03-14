package shapechanger;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.Math.*;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;

/**
 * The model part of the ShapeChanger.
 * The morphing algorithm @link Morph.normalise
 * is not perfected -- for complex enough shapes
 * the results is a strongly diminished shape compared with
 * the expected one (TODO: 25/04/2016 needs fixing later)
 *
 * <p>
 *
 * @assignment author Zhanpeng Diao
 * Date: 13/05/2016
 *
 * @see ShapeChanger
 * </p>
 */

public class Morph {

    public List<Point> points;

    public Morph(List<Point> points) {

        this.points = new ArrayList<>();
        this.points.addAll(points
                .stream()
                .map(p -> Point.makePoint(p.x, p.y))
                .collect(Collectors.toList())
        );
    }

    public Point anchorPoint() {
        return points.get(0);
    }

    public Morph roundMorph() {
        if (points.size() < 3)
            return this;

        List<Point> newPoints = new ArrayList<>();
        double mx = medX();
        double my = medY();
        double firstX = points.get(0).x - mx;
        double firstY = points.get(0).y - my;
        double theta0 = /*0.5*PI*/ -atan2(firstY, firstX);
        double secondX = points.get(1).x - mx;
        double secondY = points.get(1).y - my;
        int orient = (int) signum(firstX * secondY - firstY * secondX);
        int n = points.size();
        double r = radius();
        double theta;

        for (int i = 0; i < n; i++) {
            theta = orient * 2 * PI * i / n - theta0;
            newPoints.add(Point.makePoint(mx + r * cos(theta),
                    my + r * sin(theta)));
        }
        //System.out.printf("Morph radius %.2f%n", r);
        return new Morph(newPoints);
    }
    
    /*
        * Morphing a shape to a triangle
        */
    public Morph rectangleMorph() {
        if (points.size() < 4)
            return this;
        
        List<Point> newPoints = new ArrayList<>();
        double mx = medX();
        double my = medY();
        double r = radius();
        
        double halfBase = 0;
        double halfHeight = 0;       
        for(int i = 0; i < points.size(); i++) {
            double tempBase = Math.abs(mx - points.get(i).x);       
            if(tempBase > halfBase) {halfBase = tempBase;}
            
            double tempHeight = Math.abs(my - points.get(i).y);
            if(tempHeight > halfHeight) {halfHeight = tempHeight;}          
        } 
        
        Point vertex0 = Point.makePoint(halfBase + mx, - halfHeight + my); 
        Point vertex1 = Point.makePoint(halfBase + mx, halfHeight + my);
        Point vertex2 = Point.makePoint(- halfBase + mx, halfHeight + my);
        Point vertex3 = Point.makePoint(- halfBase + mx, - halfHeight + my);
        
        for(Point pt: points) {
//            double distance0 = Math.sqrt(Math.pow((pt.x - mx - halfBase), 2)  
//                    + Math.pow((pt.y - my + halfHeight), 2));
//            double distance1 = Math.sqrt(Math.pow((pt.x - mx - halfBase), 2) 
//                    + Math.pow((pt.y - my - halfHeight), 2));
//            double distance2 = Math.sqrt(Math.pow((pt.x - mx + halfBase), 2) 
//                    + Math.pow((pt.y - my - halfHeight), 2));
//            double distance3 = Math.sqrt(Math.pow((pt.x - mx + halfBase), 2) 
//                    + Math.pow((pt.y - my + halfHeight), 2));
//            double minDistance = distance0;
//            if(distance1 < minDistance) {minDistance = distance1;}
//            if(distance2 < minDistance) {minDistance = distance2;}
//            if(distance3 < minDistance) {minDistance = distance3;}
//            
//            if(minDistance == distance0) {newPoints.add(vertex0);}
//            else if(minDistance == distance1) {newPoints.add(vertex1);}
//            else if(minDistance == distance2) {newPoints.add(vertex2);}
//            else if(minDistance == distance3) {newPoints.add(vertex3);}
            if(pt.x - mx >= 0 && pt.y - my < 0) {newPoints.add(vertex0);}
            else if(pt.x - mx >= 0 && pt.y - my >= 0) {newPoints.add(vertex1);}
            else if(pt.x - mx < 0 && pt.y - my >= 0) {newPoints.add(vertex2);}
            else if(pt.x - mx < 0 && pt.y - my < 0) {newPoints.add(vertex3);}
        }
        
        return new Morph(newPoints);
    }
    
    /*
        * Morphing a shape to a triangle
        */
    public Morph triangleMorph() {
        if (points.size() < 3)
            return this;
        
        List<Point> newPoints = new ArrayList<>();
        double mx = medX();
        double my = medY();
        double r = radius();
        
        double halfBase = 0;
        double halfHeight = 0;       
        for(int i = 0; i < points.size(); i++) {
            double tempBase = Math.abs(mx - points.get(i).x);       
            if(tempBase > halfBase) {halfBase = tempBase;}
            
            double tempHeight = Math.abs(my - points.get(i).y);
            if(tempHeight > halfHeight) {halfHeight = tempHeight;}          
        } 
        
        /*three fixed points*/
        Point vertex0 = Point.makePoint(0 + mx, - halfHeight + my); 
        Point vertex1 = Point.makePoint(halfBase + mx, halfHeight + my);
        Point vertex2 = Point.makePoint(- halfBase + mx, halfHeight + my);
        
        for(Point pt: points) {
            double distance0 = Math.sqrt(Math.pow((pt.x - mx - 0), 2)  
                    + Math.pow((pt.y - my + halfHeight), 2));
            double distance1 = Math.sqrt(Math.pow((pt.x - mx - halfBase), 2) 
                    + Math.pow((pt.y - my - halfHeight), 2));
            double distance2 = Math.sqrt(Math.pow((pt.x - mx + halfBase), 2) 
                    + Math.pow((pt.y - my - halfHeight), 2));
            double minDistance = distance0;
            if(distance1 < minDistance) {minDistance = distance1;}
            if(distance2 < minDistance) {minDistance = distance2;}
            
            if(minDistance == distance0) {newPoints.add(vertex0);}
            else if(minDistance == distance1) {newPoints.add(vertex1);}
            else if(minDistance == distance2) {newPoints.add(vertex2);}
        }
        
        return new Morph(newPoints);
    }
    
    /*
        * Morphing a radnom shape to a ellipse
        */
    public Morph ellipseMorph() {
        if (points.size() < 3)
            return this;
        
        List<Point> newPoints = new ArrayList<>();
        double mx = medX();
        double my = medY();
        int n = points.size();
        double r = radius();
        
        /*find the X radius, Y radius*/
        double radiusX = 0;
        double radiusY = 0;       
        for(int i = 0; i < n; i++) {
            double tempRdsX = Math.abs(mx - points.get(i).x);       
            if(tempRdsX > radiusX) {radiusX = tempRdsX;}
            
            double tempRdsY = Math.abs(my - points.get(i).y);
            if(tempRdsY > radiusY) {radiusY = tempRdsY;}          
        } 
        
        if(radiusX >= radiusY) {
//            double foci = Math.sqrt(Math.pow(radiusX, 2) - Math.pow(radiusY, 2));
            double incrementX = 2 * radiusX / (n / 2);
            double positionX = - radiusX;
//            System.out.println(radiusX + " " + positionX + " " + incrementX + " " +n);
            for(int i = 0; i < n / 2; i ++) {
                double tempY = Math.sqrt((1 - (Math.pow(positionX, 2) / Math.pow(radiusX, 2))) 
                        * Math.pow(radiusY, 2)); 
//                System.out.println(positionX + " " + tempY);
                newPoints.add(Point.makePoint(positionX + mx, tempY + my));
                positionX += incrementX;
            }
            
            incrementX = -incrementX;
            positionX = radiusX;
//            System.out.println(radiusX + " " + positionX + " " + incrementX + " " +n);
            for(int i = 0; i < n / 2; i ++) {
                double tempY = Math.sqrt((1 - (Math.pow(positionX, 2) / Math.pow(radiusX, 2))) 
                        * Math.pow(radiusY, 2)); 
//                System.out.println(positionX + " " + tempY);
                newPoints.add(Point.makePoint(positionX + mx, -tempY + my));
                positionX += incrementX;
            }
            
            if(n % 2 == 1) { // handle the case if the value of the size are odd
                newPoints.add(Point.makePoint(-radiusX + mx, 0 + my));
            }
        } else {
            double incrementY = 2 * radiusY / (n / 2);
            double positionY = - radiusY;
//            System.out.println(radiusY + " " + positionY + " " + incrementY + " " +n);
            for(int i = 0; i < n / 2; i ++) {
                double tempX = Math.sqrt((1 - (Math.pow(positionY, 2) / Math.pow(radiusY, 2))) 
                        * Math.pow(radiusX, 2)); 
//                System.out.println(positionY + " " + tempX);
                newPoints.add(Point.makePoint(tempX + mx, positionY + my));
                positionY += incrementY;
            }
            
            incrementY = -incrementY;
            positionY = radiusY;
//            System.out.println(radiusY + " " + positionY + " " + incrementY + " " +n);
            for(int i = 0; i < n / 2; i ++) {
                double tempX = Math.sqrt((1 - (Math.pow(positionY, 2) / Math.pow(radiusY, 2))) 
                        * Math.pow(radiusX, 2)); 
//                System.out.println(positionY + " " + tempX);
                newPoints.add(Point.makePoint(-tempX + mx, positionY + my));
                positionY += incrementY;
            }
            
            if(n % 2 == 1) { // handle the case if the value of the size are odd
                newPoints.add(Point.makePoint(0 + mx, -radiusY + my));
            }
        }   
        
        return new Morph(newPoints);
    }
    
    /*
        * Morphing a radnom shape to a ellipse
        */
    public Morph polygonMorph(int sideOfPolygon) {
        if (points.size() < 3)
            return this;
        
        List<Point> newPoints = new ArrayList<>();
        List<Point> vertexList = new ArrayList<>();
        double mx = medX();
        double my = medY();
        int n = points.size();
        double r = radius();
        
        /*create vertexs of the polygon*/
        double angleAccumulate = 0;
        double incrementRadian = 2 * Math.PI / sideOfPolygon;
        for(int i = 0; i < sideOfPolygon; i ++) {
            vertexList.add(Point.makePoint(r * Math.cos(angleAccumulate) + mx, 
                    r * Math.sin(angleAccumulate) + my));
            angleAccumulate += incrementRadian;
        }
        
//        vertexList.stream().forEach(ele -> System.out.println(ele.x + " " + ele.y));

        /*find the nearest point and then transfer*/
        for(Point pt: points) {
            double minDistance = 100000;
            Point minDistanceVertex = null;
            for(Point tempVertex: vertexList) {
                double distanceToVertex = Math.sqrt((Math.pow(pt.x - tempVertex.x, 2) +
                        Math.pow(pt.y - tempVertex.y, 2)));
                if(distanceToVertex < minDistance) {
                    minDistance = distanceToVertex;
                    minDistanceVertex = tempVertex;
                }
            }
            newPoints.add(minDistanceVertex);
        }
        
        return new Morph(newPoints);
    }

    public static Morph normalize(Morph source, int pointLimit) {
        if (source.points.size() < pointLimit)
            throw new AssertionError("Source oneMorph has less points than pointLimit");

        int sourcePoints = source.points.size();
        int excess = sourcePoints - pointLimit;
        int gap = sourcePoints / (excess + 1);
        List<Point> filteredPoints =
                IntStream.range(0, source.points.size())
                        .filter(i -> (i+1) % gap != 0 || i >= gap * excess)
                        .mapToObj(source.points::get)
                        .collect(Collectors.toList());

//        System.out.printf("source %d, limit %d and normalised morph %d%n",
//                source.points.size(), pointLimit, filteredPoints.size());

        return new Morph(filteredPoints);
    }

    private double medX() {
        double x = 0;
        for (Point p : points) {
            x += p.x;
        }
        return x / points.size();
    }

    private double medY() {
        double y = 0;
        for (Point p : points) {
            y += p.y;
        }
        return y / points.size();
    }

    private double area() {
        double res = 0;
        if (points.size() < 3)
            return res;
        double cx = medX();
        double cy = medY();
        double dx1, dx2, dy1, dy2;
        for (int i = 0; i < points.size() - 1; i++) {
            dx1 = points.get(i).x - cx;
            dy1 = points.get(i).y - cy;
            dx2 = points.get(i + 1).x - cx;
            dy2 = points.get(i + 1).y - cy;
            res += 0.5 * abs(dx1 * dy2 - dx2 * dy1);
        }
        dx1 = points.get(points.size() - 1).x - cx;
        dy1 = points.get(points.size() - 1).y - cy;
        dx2 = points.get(0).x - cx;
        dy2 = points.get(0).y - cy;
        res += 0.5 * abs(dx1 * dy2 - dx2 * dy1);

        return res;
    }

    private double radius() {
        return sqrt(area() / PI);
    }
}