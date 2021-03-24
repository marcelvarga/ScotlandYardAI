package uk.ac.bris.cs.scotlandyard.ui.ai;

public class Node implements Comparable<Node> {
    private final int location;
    private final int distance;

    Node(int location, int distance){
        this.location = location;
        this.distance = distance;
    }

    @Override
    public int compareTo(Node o) {
        return this.distance - o.distance;
    }

    public int getDistance() {
        return distance;
    }

    public int getLocation() {
        return location;
    }
}
