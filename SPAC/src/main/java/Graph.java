import java.util.*;
public class Graph {
    public int V;
    public List<List<iPair>> adj;
    int dijkstraDistance[];
    int bellmanDistance[];
    int dijkstraArray[];
    int dijkstraLoopArray[][];
    int bellmanLoopArray[][];
    int floydWarshallArray[][];
    //int dijkstraPath[];
    int dijkstraArrayCount;
    int weight = 10;
    int graphEdgeCount = 0;
    PriorityQueue<iPair> pql = new PriorityQueue<>((a, b) -> Integer.compare(a.weight, b.weight));
    //neo n = new neo();
    Random random = new Random();
    Graph(int V) {
        this.V = V;
        adj = new ArrayList<>();
        dijkstraDistance = new int[V];
        bellmanDistance = new int[V];
        dijkstraArray = new int[V*(V+1)];
        dijkstraLoopArray = new int[V][V];
        bellmanLoopArray = new int[V][V];
        floydWarshallArray = new int[V][V];

        resetDijkstra();

        //n.connect();
        //n.deleteAll();

        for (int i = 0; i < V; i++) {
            adj.add(new ArrayList<>());
            //n.createNode('n' + String.valueOf(i));
        }
    }
    boolean addEdge(int u, int v, int w) {
        adj.get(u).add(new iPair(v, w));
        adj.get(v).add(new iPair(u, w));
        graphEdgeCount = graphEdgeCount + 1;
        //n.createRelationship('n' + String.valueOf(u), 'n' + String.valueOf(v), String.valueOf(w), 'r' + String.valueOf(u) + String.valueOf(v) + String.valueOf(w));
        //n.createRelationship('n' + String.valueOf(v), 'n' + String.valueOf(u), String.valueOf(w), 'r' + String.valueOf(v) + String.valueOf(u) + String.valueOf(w));
        return true;
    }
    public void createRandomGraphGnm(int N, int M) {
        Random rand = new Random();
        int maxEdges = N * (N - 1) / 2;
        // If M = 0 → choose random number of edges
        if (M == 0) {
            M = 1 + rand.nextInt(maxEdges);
        }
        if (M > maxEdges)
            throw new IllegalArgumentException("Too many edges");
        // Store edges to prevent duplicates
        HashSet<Long> edges = new HashSet<>();
        while (edges.size() < M) {
            int u = rand.nextInt(N);
            int v = rand.nextInt(N);
            if (u == v)
                continue;
            int a = Math.min(u, v);
            int b = Math.max(u, v);
            long edgeKey = ((long)a << 32) | b;
            if (!edges.contains(edgeKey)) {
                edges.add(edgeKey);
                int currentWeight = 1 + random.nextInt(weight);
                addEdge(a, b, currentWeight);
            }
        }
    }
    public void createRandomGraphGnp(int n, double p) {
        if (p == 0.0) {
            p = 0.001 + random.nextDouble() * (1.0 - 0.001);
        }
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (random.nextDouble() < p) {
                    int currentWeight = 1 + random.nextInt(weight);
                    addEdge(i, j, currentWeight);
                }
            }
        }
    }
    public void createRandomGraphConnected(int maxEdgesPerNode, int totalEdges) {
        int minEdges = V - 1;

        // adjust totalEdges
        if (totalEdges == 0) {
            int maxPossible = Math.min(V * maxEdgesPerNode / 2, V * (V - 1) / 2);
            totalEdges = random.nextInt(maxPossible - minEdges + 1) + minEdges;
        }
        if (totalEdges < minEdges) {
            totalEdges = minEdges;
        }

        int[] degree = new int[V];
        int edgesAdded = 0;

        // track edges in O(1)
        HashSet<Long> edgeSet = new HashSet<>();

        // ---- Step 1: ensure connectivity (spanning tree)
        List<Integer> nodes = new ArrayList<>();
        for (int i = 0; i < V; i++) nodes.add(i);
        Collections.shuffle(nodes);

        for (int i = 1; i < V; i++) {
            int u = nodes.get(i);
            int v = nodes.get(random.nextInt(i));

            int currentWeight = 1 + random.nextInt(weight);
            addEdge(u, v, currentWeight);

            degree[u]++;
            degree[v]++;
            edgesAdded++;

            edgeSet.add(encode(u, v)); // track edge
        }

        // ---- Step 2: add remaining edges
        while (edgesAdded < totalEdges) {
            int u = random.nextInt(V);
            int v = random.nextInt(V);

            if (u == v) continue;
            if (degree[u] >= maxEdgesPerNode || degree[v] >= maxEdgesPerNode) continue;

            long key = encode(u, v);
            if (edgeSet.contains(key)) continue; // O(1) duplicate check

            int currentWeight = 1 + random.nextInt(weight);
            addEdge(u, v, currentWeight);

            degree[u]++;
            degree[v]++;
            edgesAdded++;

            edgeSet.add(key); // store edge
        }
    }
    private long encode(int u, int v) {
        int a = (u < v) ? u : v;
        int b = (u < v) ? v : u;
        return ((long) a << 32) | b;
    }
    void resetDijkstra(){
        dijkstraArrayCount = V*V;
        int j = 0;
        for (int i = 0; i < V*(V+1); i++) {
            if (i == (V * j + j + j)) {
                //dijkstraArray[i] = 0;
                dijkstraArrayCount--;
            }
            else if(i == (j+1) * V + j) {
                dijkstraArray[(j + 1) * V + j] = V - 1;
                j++;
            }
            else dijkstraArray[i] = -1;
        }
    }
    /*
     * Generate a Barabási–Albert scale-free graph with N nodes.
     * Some nodes may have 0 edges.
     * @param N number of nodes
     * @param m0 number of edges to attach for non-isolated nodes
     * @param pSkip probability that a node will remain isolated
     * @return adjacency list graph
     */
    public void createBarabasiGraph(int N, int m0, double pSkip) {
        int[] degree = new int[N];
        ArrayList<Integer> pool = new ArrayList<>();
        HashSet<Long> edges = new HashSet<>();

        // Optional: start with small connected core
        int initialNodes = Math.min(m0, N);
        for (int i = 0; i < initialNodes; i++) {
            for (int j = i + 1; j < initialNodes; j++) {
                long key = ((long)i << 32) | j;
                edges.add(key);
                int currentWeight = 1 + random.nextInt(weight);
                addEdge(i, j, currentWeight);
                degree[i]++;
                degree[j]++;
            }
        }

        // Add initial nodes to pool
        for (int i = 0; i < initialNodes; i++) {
            for (int k = 0; k < degree[i]; k++)
                pool.add(i);
        }

        // Add remaining nodes
        for (int newNode = initialNodes; newNode < N; newNode++) {

            // Skip node with probability pSkip → node stays isolated
            //if (random.nextDouble() < pSkip)
            //    continue;

            //int edgesToAdd = Math.min(m0, newNode);
            //int edgesToAdd = 1 + random.nextInt(Math.min(m0, newNode));
            int edgesToAdd = 1 + (int)(Math.pow(random.nextDouble(), 2) * Math.min(m0, newNode));
            int attempts = 0;

            while (degree[newNode] < edgesToAdd && attempts < N * 10) {
                attempts++;

                if (pool.isEmpty())
                    break;

                // NEW: probabilistic edge skipping
                if (random.nextDouble() < pSkip && degree[newNode] > 0)
                    continue;

                int target = pool.get(random.nextInt(pool.size()));
                if (target == newNode)
                    continue;

                int a = Math.min(target, newNode);
                int b = Math.max(target, newNode);

                long key = ((long)a << 32) | b;
                if (edges.contains(key))
                    continue;

                edges.add(key);
                int currentWeight = 1 + random.nextInt(weight);
                addEdge(a, b, currentWeight);

                degree[a]++;
                degree[b]++;

                pool.add(a);
                pool.add(b);
            }
            for (int k = 0; k < degree[newNode]; k++)
                pool.add(newNode);
        }
    }
    double getNodeAverageWeight(int u){
        int total = 0;
        int i = 0;
        int size = adj.get(u).size();
        for (i = 0; i < size; i++) {
            total = total + adj.get(u).get(i).weight;
        }
        return (double) total / size;
    }
    double getNodeNeighboursAvgWeight(int u){
        int totalNeighbourWeight = 0;
        double avgNeighbourWeight = 0;
        for (iPair v : adj.get(u)) {
            totalNeighbourWeight = 0;
            for (iPair w : adj.get(v.vertex))
                if(w.vertex != u)
                    totalNeighbourWeight += w.weight;
            int size = adj.get(v.vertex).size();
            if(size > 1)
                avgNeighbourWeight += ((double) totalNeighbourWeight / (size-1));
            else avgNeighbourWeight += totalNeighbourWeight;
        }
        return avgNeighbourWeight;
    }
    static class iPair{
        int vertex, weight;

        iPair(int vertex, int weight) {
            this.vertex = vertex;
            this.weight = weight;
        }
    }
    static class PairSize {
         int first;
         int second;

        PairSize(int first, int second) {
            this.first = first;
            this.second = second;
        }
    }
    static class dPairSize {
        int first;
        double second;

        dPairSize(int first, double second) {
            this.first = first;
            this.second = second;
        }
    }
    void dijkstraAllPairsWithLoops(int src) {
        pql.clear();
        Arrays.fill(dijkstraDistance, 999);
        pql.add(new iPair(src, 0));
        dijkstraDistance[src] = 0;
        while (!pql.isEmpty()) {
            int u = pql.poll().vertex;
            for (iPair v : adj.get(u)) {
                int distSum = dijkstraDistance[u] + v.weight;
                int dD = dijkstraDistance[v.vertex];
                if (dD >= distSum) {
                    dijkstraDistance[v.vertex] = distSum;
                    pql.add(new iPair(v.vertex, distSum));
                }
            }
        }
        for (int j = 0; j < V; j++) {
            dijkstraLoopArray[src][j] = dijkstraDistance[j];
        }
    }
    void Dijkstra(int src,ThreadLocal<DijkstraResources> threadResources) {
        DijkstraResources res = threadResources.get();
        res.reset(src);
        int[] dijkstraDistance = res.dijkstraDistance;
        boolean[] visited = res.visited;
        int[] dijkstraPath = res.dijkstraPath;
        pql = new PriorityQueue<>((a, b) -> Integer.compare(a.weight, b.weight));
        //Arrays.fill(dijkstraDistance, 999);
        //dijkstraPath = new int[V];
        pql.add(new iPair(src, 0));
        dijkstraDistance[src] = 0;
        dijkstraPath[src] = src;
        //boolean[] visited = new boolean[V];
        while (!pql.isEmpty()) {
            int u = pql.poll().vertex;
            if (visited[u])
                continue;
            visited[u] = true;
            if(src != dijkstraPath[u])
                findAdditionalPaths(src, u, dijkstraPath);
            for (iPair v : adj.get(u)) {
                if(v.vertex != src && !visited[v.vertex]) {
                    int distSum = dijkstraDistance[u] + v.weight;
                    int dD = dijkstraDistance[v.vertex];
                    if (dD > distSum) {
                        dijkstraDistance[v.vertex] = distSum;
                        pql.add(new iPair(v.vertex, distSum));
                        dijkstraPath[v.vertex] = u;
                        int temp = dijkstraArray[v.vertex*(V+1)+src];
                        dijkstraArray[src*(V+1)+v.vertex] = distSum;
                        dijkstraArray[v.vertex*(V+1)+src] = distSum;
                        if (temp == -1) {
                            dijkstraArrayCount -= 2;
                            dijkstraArray[src*(V+1)+V]--;
                            dijkstraArray[v.vertex*(V+1)+V]--;
                        }
                    }
                }
            }
        }
    }
    void findAdditionalPaths(int src, int u, int[] dijkstraPath){
        int val = dijkstraPath[u];
        while (val != src && dijkstraArrayCount > 0) {
            if (dijkstraArray[u*(V+1)+val] == -1) {
                int weightSource = dijkstraArray[src*(V+1)+u];
                int weightDest = dijkstraArray[src*(V+1)+val];
                int dif = (weightDest > weightSource) ? (weightDest - weightSource) : (weightSource - weightDest);
                dijkstraArray[u*(V+1)+val] = dif;
                dijkstraArray[val*(V+1)+u] = dif;
                dijkstraArrayCount -= 2;
                dijkstraArray[u*(V+1)+V]--;
                dijkstraArray[val*(V+1)+V]--;
            }
            val = dijkstraPath[val];
        }
    }
    void BellmanFord(int src) {
        Arrays.fill(bellmanDistance, 999);
        bellmanDistance[src] = 0;
        for (int i = 0; i < V-1; i++) {
            boolean swapped = false;
            for (int j = 0; j < V; j++) {
                for (iPair p : adj.get(j)) {
                    int u = j, v = p.vertex, w = p.weight;
                    if (bellmanDistance[u] != 999 && bellmanDistance[v] > bellmanDistance[u] + w) {
                        bellmanDistance[v] = bellmanDistance[u] + w;
                        swapped = true;
                    }
                }
            }
            if (!swapped) break;
        }
        for (int j = 0; j < V; j++) {
            bellmanLoopArray[src][j] = bellmanDistance[j];
        }
        //checks if there exist negative cycles in graph G
       /* for (int i = 0; i < V; i++) {
            for (iPair p : adj.get(i)) {
                int u = i, v = p.first, w = p.second;
                if (distance[u] != Integer.MAX_VALUE && distance[v] > distance[u] + w) {
                    neg = true;
                }
            }
        }*/
    }
    void floydWarshall(int src) {
        for (int k = 0; k < V; k++) {
            // Pick all vertices as source one by one
            for (int i = 0; i < V; i++) {
                // Pick all vertices as destination for the
                // above picked source
                for (int j = 0; j < V; j++) {
                    // If vertex k is on the shortest path
                    // from i to j, then update the value of
                    // dist[i][j]
                    if (floydWarshallArray[i][k] + floydWarshallArray[k][j]
                            < floydWarshallArray[i][j])
                        floydWarshallArray[i][j]
                                = floydWarshallArray[i][k] + floydWarshallArray[k][j];
                }
            }
        }
    }
    void fillFloydWarshallArray() {
        for (int i = 0; i < V; i++) {
            for (int j = 0; j < V; j++) {
                if (i == j) {
                    floydWarshallArray[i][j] = 0;
                } else {
                    floydWarshallArray[i][j] = 999;
                }
            }
        }
        for (int i = 0; i < V; i++) {
            for (iPair p : adj.get(i)) {
                int u = i, v = p.vertex, w = p.weight;
                floydWarshallArray[u][v] = w;
            }
        }
    }
    int compareArraysDijkstraFloyd(){
        int differenceCount = 0;
        for (int i = 0; i < V; i++) {
            for (int j = 0; j < V; j++) {
                if (dijkstraArray[i*(V+1)+j] != floydWarshallArray[i][j]) {
                    differenceCount++;
                }
            }
        }
        return differenceCount;
    }
    int compareArraysBellmanFloyd(){
        int differenceCount = 0;
        for (int i = 0; i < V; i++) {
            for (int j = 0; j < V; j++) {
                if (bellmanLoopArray[i][j] != floydWarshallArray[i][j]) {
                    differenceCount++;
                }
            }
        }
        return differenceCount;
    }
    public void eigenvectorCentralityMaxPQ(PriorityQueue<Graph.PairSize> MaxPriorityQueue, int maxIterations, double tolerance) {

        int V = adj.size();

        double[] v = new double[V];
        double[] newV = new double[V];

        Arrays.fill(v, 1.0 / Math.sqrt(V));

        for (int iter = 0; iter < maxIterations; iter++) {

            Arrays.fill(newV, 0);

            // A * v
            for (int i = 0; i < V; i++) {
                for (Graph.iPair edge : adj.get(i)) {
                    newV[i] += edge.weight * v[edge.vertex];
                }
            }

            // normalize
            double norm = 0;
            for (double val : newV)
                norm += val * val;

            norm = Math.sqrt(norm);

            for (int i = 0; i < V; i++)
                newV[i] /= norm;

            // convergence check
            double diff = 0;
            for (int i = 0; i < V; i++)
                diff += Math.abs(newV[i] - v[i]);

            if (diff < tolerance)
                break;

            System.arraycopy(newV, 0, v, 0, V);
        }

        // scale values
        double max = 0;
        for (double val : v)
            max = Math.max(max, val);

        for (int i = 0; i < V; i++) {

            int score = (int) Math.round((v[i] / max) * 1000);

            MaxPriorityQueue.add(new Graph.PairSize(i, score));
        }
    }
    public void eigenvectorCentralityMinPQ(PriorityQueue<Graph.PairSize> MinPriorityQueue, int maxIterations, double tolerance) {

        int V = adj.size();

        double[] v = new double[V];
        double[] newV = new double[V];

        Arrays.fill(v, 1.0 / Math.sqrt(V));

        for (int iter = 0; iter < maxIterations; iter++) {

            Arrays.fill(newV, 0);

            // A * v
            for (int i = 0; i < V; i++) {
                for (Graph.iPair edge : adj.get(i)) {
                    newV[i] += edge.weight * v[edge.vertex];
                }
            }

            // normalize
            double norm = 0;
            for (double val : newV)
                norm += val * val;

            norm = Math.sqrt(norm);

            for (int i = 0; i < V; i++)
                newV[i] /= norm;

            // convergence check
            double diff = 0;
            for (int i = 0; i < V; i++)
                diff += Math.abs(newV[i] - v[i]);

            if (diff < tolerance)
                break;

            System.arraycopy(newV, 0, v, 0, V);
        }

        // scale values
        double max = 0;
        for (double val : v)
            max = Math.max(max, val);

        for (int i = 0; i < V; i++) {

            int score = (int) Math.round((v[i] / max) * 1000);

            MinPriorityQueue.add(new Graph.PairSize(i, score));
        }
    }
    //Matula and Beck algorithm
    public int[] calculateCoreNumbers(PriorityQueue<Graph.PairSize> PriorityQueue) {
        int V = this.V;
        int[] degrees = new int[V];
        int maxDegree = 0;

        for (int i = 0; i < V; i++) {
            degrees[i] = adj.get(i).size();
            maxDegree = Math.max(maxDegree, degrees[i]);
        }

        // 1. Group vertices by their degrees (Bucket Sort)
        int[] bin = new int[maxDegree + 1];
        for (int i = 0; i < V; i++) bin[degrees[i]]++;

        // 2. Find starting positions for each degree in a sorted array
        int start = 0;
        for (int d = 0; d <= maxDegree; d++) {
            int num = bin[d];
            bin[d] = start;
            start += num;
        }

        // 3. Sort vertices by degree: 'pos' maps index to vertex, 'vert' maps vertex to index
        int[] pos = new int[V];
        int[] vert = new int[V];
        for (int i = 0; i < V; i++) {
            pos[i] = bin[degrees[i]];
            vert[pos[i]] = i;
            bin[degrees[i]]++;
        }

        // Reset bins to the start of each degree range
        for (int d = maxDegree; d > 0; d--) bin[d] = bin[d - 1];
        bin[0] = 0;

        // 4. Main Decomposition
        for (int i = 0; i < V; i++) {
            int v = vert[i]; // Vertex with current lowest degree
            PriorityQueue.add(new Graph.PairSize(v, degrees[v]));
            for (iPair neighbor : adj.get(v)) {
                if (degrees[neighbor.vertex] > degrees[v]) {
                    // Update neighbor's position in the sorted list
                    int neighborDegree = degrees[neighbor.vertex];
                    int neighborPos = pos[neighbor.vertex];
                    int binStartPos = bin[neighborDegree];
                    int swapperVertex = vert[binStartPos];

                    if (neighbor.vertex != swapperVertex) {
                        // Swap neighbor with the vertex at the start of its degree bin
                        pos[neighbor.vertex] = binStartPos;
                        vert[neighborPos] = swapperVertex;
                        pos[swapperVertex] = neighborPos;
                        vert[binStartPos] = neighbor.vertex;
                    }

                    // Move bin start forward and decrement degree
                    bin[neighborDegree]++;
                    degrees[neighbor.vertex]--;
                }
            }
        }
        return degrees;
    }
    public int[] triangleCount() {
        int n = this.V;
        int[] triangles = new int[n];

        // Use a List of Sets for O(1) average lookup without N^2 memory
        List<Set<Integer>> lookup = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            lookup.add(new HashSet<>());
            for (iPair neighbor : adj.get(i)) {
                lookup.get(i).add(neighbor.vertex);
            }
        }

        for (int v = 0; v < n; v++) {
            List<iPair> neighbors = adj.get(v);
            for (int i = 0; i < neighbors.size(); i++) {
                int u = neighbors.get(i).vertex;
                for (int j = i + 1; j < neighbors.size(); j++) {
                    int w = neighbors.get(j).vertex;
                    // Check if the two neighbors of v are connected to each other
                    if (lookup.get(u).contains(w)) {
                        triangles[v]++;
                    }
                }
            }
        }
        return triangles;
    }
    public int[] twoHopDegree(PriorityQueue<Graph.PairSize> PriorityQueue) {
        int n = this.V;
        boolean[] visited = new boolean[n];
        List<Integer> touched = new ArrayList<>(); // To track what to reset
        int[] twoHop = new int[n];
        for (int v = 0; v < n; v++) {
            int count = 0;
            visited[v] = true; // Temporarily mark self to exclude from count
            touched.add(v);

            for (iPair u : adj.get(v)) {
                if (!visited[u.vertex]) {
                    visited[u.vertex] = true;
                    touched.add(u.vertex);
                    count++;
                }

                for (iPair w : adj.get(u.vertex)) {
                    if (!visited[w.vertex]) {
                        visited[w.vertex] = true;
                        touched.add(w.vertex);
                        count++;
                    }
                }
            }

            PriorityQueue.add(new Graph.PairSize(v, count));
            twoHop[v] = count;
            // Reset ONLY the visited indices we touched (O(degree^2) instead of O(V))
            for (int index : touched) {
                visited[index] = false;
            }
            touched.clear();
        }
        return twoHop;
    }
    public void clusteringCoefficient(PriorityQueue<Graph.dPairSize> pq) {
        int n = adj.size();

        for (int v = 0; v < n; v++) {
            List<iPair> neighbors = adj.get(v);
            int k = neighbors.size();
            if (k < 2) {
                pq.add(new dPairSize(v, 0.0));
                continue;
            }
            // Convert neighbors of v to HashSet for O(1) lookup
            HashSet<Integer> neighborSet = new HashSet<>();
            for (iPair p : neighbors) {
                neighborSet.add(p.vertex);
            }

            int links = 0;
            // Count edges between neighbors
            for (int i = 0; i < neighbors.size(); i++) {
                int u = neighbors.get(i).vertex;
                // Convert neighbors of u to HashSet temporarily
                HashSet<Integer> uNeighbors = new HashSet<>();
                for (iPair p : adj.get(u)) {
                    uNeighbors.add(p.vertex);
                }
                for (int j = i + 1; j < neighbors.size(); j++) {
                    int w = neighbors.get(j).vertex;
                    // Check if u and w are connected
                    if (uNeighbors.contains(w)) {
                        links++;
                    }
                }
            }
            double cc = (2.0 * links) / (k * (k - 1));
            pq.add(new dPairSize(v, cc));
        }
    }
}
