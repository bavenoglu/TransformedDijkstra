public class DijkstraResources {
    public int[] dijkstraDistance;
    public int[] dijkstraPath;
    public boolean[] visited;

    public DijkstraResources(int V) {
        this.dijkstraDistance = new int[V];
        this.dijkstraPath = new int[V];
        this.visited = new boolean[V];
    }

    // Method to reset arrays for the next Dijkstra run
    public void reset(int src) {
        java.util.Arrays.fill(dijkstraDistance, 999);
        java.util.Arrays.fill(visited, false);
        // Path and other arrays will be overwritten, so no need to fill them
    }
}
