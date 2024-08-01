import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Collections;
import scala.Tuple2;
import scala.Tuple3;

public class MvpTree implements Serializable {
    private List<double[]> pivots;
    private List<MvpTree> children;
    private List<Tuple2<Integer, double[]>> dataPoints;
    private DistanceFunction distFn;
    private static final int MAX_PIVOTS = 2; // Ajuste conforme necessário
    private static final int MAX_LEAF_SIZE = 10; // Ajuste conforme necessário

    private static final long serialVersionUID = 1L;

    public void printTree() {
        printTreeRecursive(this, 0);
    }

    private void printTreeRecursive(MvpTree node, int depth) {
        if (node == null) {
            return;
        }

        StringBuilder indent = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            indent.append("  "); // Use two spaces for each level of depth
        }

        System.out.println(indent.toString() + node); // Print the current node
        if (node.children != null) {
            for (MvpTree child : node.children) {
                printTreeRecursive(child, depth + 1);
            }
        }
    }

    public MvpTree(List<Tuple2<Integer, double[]>> points, DistanceFunction distFn) {
        this.distFn = distFn;
        this.children = new ArrayList<>();
        this.pivots = new ArrayList<>();
        this.dataPoints = new ArrayList<>();

        if (points == null || points.isEmpty()) {
            throw new IllegalArgumentException("Points cannot be empty.");
        }

        if (points.size() <= MAX_LEAF_SIZE) {
            this.dataPoints = points;
            return;
        }

        // Select pivots
        for (int i = 0; i < MAX_PIVOTS && !points.isEmpty(); i++) {
            Tuple2<Integer, double[]> vp = points.remove(0);
            pivots.add(vp._2());
        }

        // Partition the data based on the distance to the pivots
        List<List<Tuple2<Integer, double[]>>> partitions = new ArrayList<>();
        for (int i = 0; i <= MAX_PIVOTS; i++) {
            partitions.add(new ArrayList<>());
        }

        for (Tuple2<Integer, double[]> point : points) {
            double[] distances = new double[pivots.size()];
            for (int i = 0; i < pivots.size(); i++) {
                distances[i] = distFn.calculateDistance(point._2(), pivots.get(i));
            }

            int minIndex = 0;
            for (int i = 1; i < distances.length; i++) {
                if (distances[i] < distances[minIndex]) {
                    minIndex = i;
                }
            }

            partitions.get(minIndex).add(point);
        }

        // Recursively build the children nodes
        for (List<Tuple2<Integer, double[]>> partition : partitions) {
            if (!partition.isEmpty()) {
                children.add(new MvpTree(partition, distFn));
            }
        }
    }

    public Tuple3<Integer, double[], Double> getNearestNeighbor(double[] query) {
        Tuple3<Integer, double[], Double> nearestNeighbor = getNearestNeighborRecursive(query, Double.MAX_VALUE);
        if (nearestNeighbor != null) {
            return nearestNeighbor;
        } else {
            return null;
        }
    }

    private Tuple3<Integer, double[], Double> getNearestNeighborRecursive(double[] query, double currentBestDistance) {
        Tuple3<Integer, double[], Double> bestResult = null;

        for (double[] pivot : pivots) {
            double distanceToPivot = distFn.calculateDistance(query, pivot);
            if (bestResult == null || distanceToPivot < bestResult._3()) {
                bestResult = new Tuple3<>(-1, pivot, distanceToPivot); // -1 indica um pivô, não um ponto de dados real
                if (distanceToPivot < currentBestDistance) {
                    currentBestDistance = distanceToPivot;
                }
            }
        }

        for (MvpTree child : children) {
            Tuple3<Integer, double[], Double> childBest = child.getNearestNeighborRecursive(query, currentBestDistance);
            if (childBest != null && (bestResult == null || childBest._3() < bestResult._3())) {
                bestResult = childBest;
                currentBestDistance = childBest._3();
            }
        }

        if (dataPoints != null) {
            for (Tuple2<Integer, double[]> point : dataPoints) {
                double distance = distFn.calculateDistance(query, point._2());
                if (distance < currentBestDistance) {
                    bestResult = new Tuple3<>(point._1(), point._2(), distance);
                    currentBestDistance = distance;
                }
            }
        }

        return bestResult;
    }

    public List<Tuple2<Double, Tuple2<Integer, double[]>>> getAllInRange(double[] query, double maxDistance) {
        List<Tuple2<Double, Tuple2<Integer, double[]>>> neighbors = new ArrayList<>();
        getAllInRangeRecursive(query, maxDistance, neighbors);
        return neighbors;
    }

    private void getAllInRangeRecursive(double[] query, double maxDistance, List<Tuple2<Double, Tuple2<Integer, double[]>>> neighbors) {
        for (double[] pivot : pivots) {
            double distanceToPivot = distFn.calculateDistance(query, pivot);
            if (distanceToPivot <= maxDistance) {
                neighbors.add(new Tuple2<>(distanceToPivot, new Tuple2<>(-1, pivot))); // -1 indica um pivô, não um ponto de dados real
            }
        }

        for (MvpTree child : children) {
            child.getAllInRangeRecursive(query, maxDistance, neighbors);
        }

        if (dataPoints != null) {
            for (Tuple2<Integer, double[]> point : dataPoints) {
                double distance = distFn.calculateDistance(query, point._2());
                if (distance <= maxDistance) {
                    neighbors.add(new Tuple2<>(distance, point));
                }
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
        return children.isEmpty() && !dataPoints.isEmpty();
    }

    @Override
    public String toString() {
        return "MvpTree[pivots=" + pivots + "]";
    }
}
