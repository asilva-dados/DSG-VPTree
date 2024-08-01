import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Collections;
import scala.Tuple2;
import scala.Tuple3;

public class VpTree implements Serializable{
    private VpTree left;
    private VpTree right;
    private DistanceFunction distFn;
    private int pivotIndex;
    private double[] pivot;
    private Tuple3<Integer, double[], Double> vp;
    private double median;


    private static final long serialVersionUID = 1L;
    
    public void printTree() {
        printTreeRecursive(this, 0);
    }

    private void printTreeRecursive(VpTree node, int depth) {
        if (node == null) {
            return;
        }

        StringBuilder indent = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            indent.append("  "); // Use two spaces for each level of depth
        }

        System.out.println(indent.toString() + node); // Print the current node
        printTreeRecursive(node.left, depth + 1);      // Recursively print the left subtree
        printTreeRecursive(node.right, depth + 1);     // Recursively print the right subtree
    }

    public VpTree(List<Tuple2<Integer, double[]>> points, DistanceFunction distFn) {
        this.distFn = distFn;

        if (points == null || points.isEmpty()) {
            throw new IllegalArgumentException("Points cannot be empty.");
        }

        Tuple2<Integer, double[]> vp = points.get(0);
        pivotIndex = vp._1();
        pivot = vp._2();
        this.vp = new Tuple3<>(pivotIndex, pivot, 0.0);
        points = points.subList(1, points.size());
        
        if (points.isEmpty()) {
            return;
        }

        List<Double> distances = new ArrayList<>();
        List<Tuple2<Integer, double[]>> pivotsList = new ArrayList<>();
        for (Tuple2<Integer, double[]> point : points) {
            distances.add(distFn.calculateDistance(pivot, point._2()));
            pivotsList.add(point);
        }
  
        Collections.sort(distances);
        median = distances.get(distances.size() / 2);
        
        List<Tuple2<Integer, double[]>> leftPoints = new ArrayList<>();
        List<Tuple2<Integer, double[]>> rightPoints = new ArrayList<>();
        
        for (Tuple2<Integer, double[]> point : pivotsList) {
            double distance = distFn.calculateDistance(pivot, point._2());

            if (distance < median) {
                leftPoints.add(point);
            } else if (distance > median) {
                rightPoints.add(point);
            } else {
                // If the distance is equal to the median
                leftPoints.add(point);
            }
        }
        

        if (!leftPoints.isEmpty()) {
            left = new VpTree(leftPoints, distFn);
        }

        if (!rightPoints.isEmpty()) {
            right = new VpTree(rightPoints, distFn);
        }
        
    }
    

    public Tuple3<Integer, double[], Double> getNearestNeighbor(double[] query) {
        Tuple3<Integer, double[], Double> nearestNeighbor = getNearestNeighborRecursive(query, Double.MAX_VALUE);
        //System.out.println("nearestNeighbor: " + nearestNeighbor);
        if (nearestNeighbor != null) {
            return nearestNeighbor;
        } else {
            return null;
        }
    }


    private Tuple3<Integer, double[], Double> getNearestNeighborRecursive(double[] query, double currentBestDistance) {
        double distanceToPivot = distFn.calculateDistance(query, pivot);
        
        //System.out.println("Pivot: " + Arrays.toString(pivot));
        //System.out.println("distanceToPivot: " + distanceToPivot);
        //System.out.println("Media: " + this.median);

        Tuple3<Integer, double[], Double> bestResult = new Tuple3<>(pivotIndex, pivot, distanceToPivot);

        if (distanceToPivot <= 0.0) {
            // Query equals current pivot, return immediately
            return bestResult;
        }

        if (distanceToPivot < currentBestDistance) {
            currentBestDistance = distanceToPivot;
        }
        
        VpTree closerNode;
        VpTree fartherNode;

        if (distanceToPivot <= median) {
            closerNode = left;
            fartherNode = right;
        } else {
            closerNode = right;
            fartherNode = left;
        }
    
        if (closerNode != null) {
            Tuple3<Integer, double[], Double> closerNeighbor = closerNode.getNearestNeighborRecursive(query, currentBestDistance);
            if (closerNeighbor != null) {
                double closestDistance = closerNeighbor._3();
                if (closestDistance < currentBestDistance) {
                    bestResult = closerNeighbor;
                    currentBestDistance = closestDistance;
                }
            }
        }

        // Here the condition is checked to search for the most distant node correctly
        if (fartherNode != null && (distanceToPivot - currentBestDistance) <= median) {
            Tuple3<Integer, double[], Double> fartherNeighbor = fartherNode.getNearestNeighborRecursive(query, currentBestDistance);
            if (fartherNeighbor != null) {
                double farthestDistance = fartherNeighbor._3();
                if (farthestDistance < currentBestDistance) {
                    bestResult = fartherNeighbor;
                }
            }
        }
        
        if (isLeaf()) {
            return bestResult;
        }
        
        return bestResult;
    }




    public List<Tuple2<Double, Tuple2<Integer, double[]>>> getAllInRange(double[] query, double maxDistance) {
        List<Tuple2<Double, Tuple2<Integer, double[]>>> neighbors = new ArrayList<>();
        getAllInRangeRecursive(query, maxDistance, neighbors);
        return neighbors;
    }

    private void getAllInRangeRecursive(double[] query, double maxDistance, List<Tuple2<Double, Tuple2<Integer, double[]>>> neighbors) {
        double distanceToPivot = distFn.calculateDistance(query, pivot);

        // If the query point is within the pivot radius, include the pivot with its distance and index.
        if (distanceToPivot <= maxDistance) {
            neighbors.add(new Tuple2<>(distanceToPivot, new Tuple2<>(pivotIndex, pivot)));
        }

        // Check the subset closest to the query point.
        if (distanceToPivot < median) {
            if (left != null) {
                left.getAllInRangeRecursive(query, maxDistance, neighbors);
            }

            // If the search circle intersects the median, check the furthest subset as well.
            if (distanceToPivot + maxDistance >= median && right != null) {
                right.getAllInRangeRecursive(query, maxDistance, neighbors);
            }
        } else {
            if (right != null) {
                right.getAllInRangeRecursive(query, maxDistance, neighbors);
            }

            // Check if we need to look on the left side.
            if (distanceToPivot - maxDistance <= median && left != null) {
                left.getAllInRangeRecursive(query, maxDistance, neighbors);
            }
        }
        
        
    }


    public interface DistanceFunction {
        double calculateDistance(double[] point1, double[] point2);
    }

    public static class EuclideanDistance implements DistanceFunction, Serializable {
        @Override
        public double calculateDistance(double[] point1, double[] point2) {
            if (point1.length != point2.length) {
                throw new IllegalArgumentException("Points must have the same dimension.");
            }

            double sum = 0.0;
            double diff = 0.0;
            for (int i = 0; i < point1.length; i++) {
                diff = point1[i] - point2[i];
                sum += diff * diff;
            }

            return Math.sqrt(sum);
        }
    }

    private boolean isLeaf() {
        return left == null && right == null;
    }
    
    @Override
    public String toString() {
        return "VpTree[pivotIndex=" + pivotIndex + ", pivot=" + Arrays.toString(pivot) + "]";
    }
}