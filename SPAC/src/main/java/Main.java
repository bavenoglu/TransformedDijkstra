import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    static class LoopResult {
        int loop;
        double[][][] result;
        LoopResult(int loop, double[][][] result) {
            this.loop = loop;
            this.result = result;
        }
    }
    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
        for(int vnum = 8; vnum <= 2048; vnum*=2) {
            /****** Parameters for All Runs ******/
            int V = vnum;
            int testloop = 5003, avgloop = 3;
            char gtype = 'R'; // R-Random, S-Scale-free
            boolean runallmetrics = true;
            boolean runpermutation = false; // "true" means no permutation calculations, more than V=16 takes so long
            int permutationlimit = 16;
            /****** Parameters for All Runs ******/

            double result[][][] = new double[testloop][30][3];
            int TotalMinEdgeCntK = 0, TotalMaxEdgeCntK = 0, TotalMinEdgeWeightK = 0, TotalMaxEdgeWeightK = 0, TotalMinECntMaxINFK = 0, TotalMaxECntMaxINFK = 0, TotalMinEdWeiNeiWeiK = 0, TotalMaxEdWeiNeiWeiK = 0, TotalMinCoefK = 0, TotalMaxCoefK = 0, TotalMinEigenK = 0, TotalMaxEigenK = 0, TotalMinEcceK = 0, TotalMaxEcceK = 0, TotalMinKCoreK = 0, TotalMaxKCoreK = 0, TotalMinTriaK = 0, TotalMaxTriaK = 0, TotalMinTwoHopK = 0, TotalMaxTwoHopK = 0;
            int TotalMinEdgeCntFalse = 0, TotalMaxEdgeCntFalse = 0, TotalMinEdgeWeightFalse = 0, TotalMaxEdgeWeightFalse = 0, TotalMinECntMaxINFFalse = 0, TotalMaxECntMaxINFFalse = 0, TotalMinEdWeiNeiWeiFalse = 0, TotalMaxEdWeiNeiWeiFalse = 0, TotalMinCoefFalse = 0, TotalMaxCoefFalse = 0, TotalMinEigenFalse = 0, TotalMaxEigenFalse = 0, TotalMinEcceFalse = 0, TotalMaxEcceFalse = 0, TotalMinKCoreFalse = 0, TotalMaxKCoreFalse = 0, TotalMinTriaFalse = 0, TotalMaxTriaFalse = 0, TotalMinTwoHopFalse = 0, TotalMaxTwoHopFalse = 0;
            double TotalGraphTime = 0.0, TotalDijkstraLoopTime = 0.0, TotalBellmanFordTime = 0.0, TotalFloydWarshallTime = 0.0, TotalBestTime = 0.0, TotalMinEdgeCntTime = 0.0, TotalMaxEdgeCntTime = 0.0, TotalMinEdgeWeightTime = 0.0,
                    TotalMaxEdgeWeightTime = 0.0, TotalMinECntMaxINFTime = 0.0, TotalMaxECntMaxINFTime = 0.0, TotalMinEdWeiNeiWeiTime = 0.0, TotalMaxEdWeiNeiWeiTime = 0.0, TotalMinCoefTime = 0.0, TotalMaxCoefTime = 0.0, TotalMinEigenTime = 0.0, TotalMaxEigenTime = 0.0, TotalMinEcceTime = 0.0,
                    TotalMaxEcceTime = 0.0, TotalMinKCoreTime = 0.0, TotalMaxKCoreTime = 0.0, TotalMinTriaTime = 0.0, TotalMaxTriaTime = 0.0, TotalMinTwoHopTime = 0.0, TotalMaxTwoHopTime = 0.0, TotalpSkip = 0.0;
            int TotalPermLoopCounter = 0, TotalEdgeCount = 0;
            AtomicInteger TotalDijkstraError = new AtomicInteger();
            String fileName = "./results" + gtype + "/" + gtype + "_" + V + "_" + testloop + "_" + System.nanoTime() + ".csv";
            FileWriter fw = new FileWriter(fileName, true);
            BufferedWriter bw = new BufferedWriter(fw);
            String res = String.format("Type: %s, V: %3d, TotLoop: %d", gtype, V, testloop);
            System.out.print(res);
            System.out.print("\n");
            bw.write(",V,GraphT,DLoopT,BellmanT,FloydT,BestT,MinEdCT,MaxEdCT,MinEdWT,MaxEdWT,MiECnMaIn,MaECnMaIn,MiEdWeNeWe,MaEdWeNeWe,MinCoefT,MaxCoefT,MinEigT,MaxEigT,MinEcce,MaxEcce,MinKCore,MaxKCore,MinTria,MaxTria,MinTwoHop,MaxTwoHop,EdgeCnt,Perm,pSkip");
            bw.newLine();
            //System.out.printf("%32s%10s%10s%10s%10s%10s%10s%10s%10s%10s%10s%10s%10s%10s%10s%10s%10s%10s%10s%10s%10s%10s%10s%10s%10s%10s%8s%9s%9s", "", "GraphT", "DLoopT", "BellmanT", "FloydT", "BestT", "MiEdCT", "MaEdCT", "MiEdWT", "MaEdWT", "MiECnMaIn", "MaECnMaIn", "MiEdWNeW", "MaEdWNeW", "MiCoefT", "MaCoefT", "MiEigT", "MaEigT", "MiEcce", "MaEcce", "MiKCore", "MaKCore", "MiTria", "MaTria", "MiTwoHop", "MaTwoHop", "EdCnt", "Perm", "pSkip");

            ExecutorService pool = Executors.newFixedThreadPool(
                    Runtime.getRuntime().availableProcessors()
            );
            CompletionService<LoopResult> cs = new ExecutorCompletionService<>(pool);

            for (int loop = 0; loop < testloop; loop++) {
                final int currentLoop = loop;
                cs.submit(() -> {
                    ThreadLocal<DijkstraResources> threadResources =
                            ThreadLocal.withInitial(() -> new DijkstraResources(V));

                    class DescComparator implements Comparator<Graph.PairSize> {
                        @Override
                        public int compare(Graph.PairSize o1, Graph.PairSize o2) {
                            if (o1.second < o2.second)
                                return 1;
                            else if (o1.second > o2.second)
                                return -1;
                            return 0;
                        }
                    }
                    class DoubleDescComparator implements Comparator<Graph.dPairSize> {
                        @Override
                        public int compare(Graph.dPairSize o1, Graph.dPairSize o2) {
                            if (o1.second < o2.second)
                                return 1;
                            else if (o1.second > o2.second)
                                return -1;
                            return 0;
                        }
                    }
                    PriorityQueue<Graph.PairSize> MinPriorityQueue = new PriorityQueue<>(V, Comparator.comparingInt(o -> o.second));
                    PriorityQueue<Graph.PairSize> MaxPriorityQueue = new PriorityQueue<Graph.PairSize>(V, new DescComparator());
                    PriorityQueue<Graph.dPairSize> DoubleMinPriorityQueue = new PriorityQueue<>(V, Comparator.comparingDouble(o -> o.second));
                    PriorityQueue<Graph.dPairSize> DoubleMaxPriorityQueue = new PriorityQueue<>(V, new DoubleDescComparator());
                    PriorityQueue<Graph.dPairSize> ClusteringCoefficientPQDesc = new PriorityQueue<>((a, b) -> Double.compare(b.second, a.second));
                    PriorityQueue<Graph.dPairSize> ClusteringCoefficientPQAsc = new PriorityQueue<>((a, b) -> Double.compare(a.second, b.second));
                    int MinEdgeCntFalse = 0, MaxEdgeCntFalse = 0, MinEdgeWeightFalse = 0, MaxEdgeWeightFalse = 0, MinECntMaxINFFalse = 0, MaxECntMaxINFFalse = 0, MinEdWeiNeiWeiFalse = 0, MaxEdWeiNeiWeiFalse = 0, MinCoefFalse = 0, MaxCoefFalse = 0, MinEigenFalse = 0, MaxEigenFalse = 0, MinEcceFalse = 0, MaxEcceFalse = 0, MinKCoreFalse = 0, MaxKCoreFalse = 0, MinTriaFalse = 0, MaxTriaFalse = 0, MinTwoHopFalse = 0, MaxTwoHopFalse = 0;
                    int MinEdgeCntK = 0, MaxEdgeCntK = 0, MinEdgeWeightK = 0, MaxEdgeWeightK = 0, MinECntMaxINFK = 0, MaxECntMaxINFK = 0, MinEdWeiNeiWeiK = 0, MaxEdWeiNeiWeiK = 0, MinCoefK = 0, MaxCoefK = 0, MinEigenK = 0, MaxEigenK = 0, MinEcceK = 0, MaxEcceK = 0, MinKCoreK = 0, MaxKCoreK = 0, MinTriaK = 0, MaxTriaK = 0, MinTwoHopK = 0, MaxTwoHopK = 0;
                    double GraphTime, DijkstraLoopTime = 0.0, BellmanFordTime = 0.0, FloydWarshallTime = 0.0, BestTime = 0.0, MinEdgeCntTime = 0.0, MaxEdgeCntTime = 0.0, MinEdgeWeightTime = 0.0, MaxEdgeWeightTime = 0.0, MinECntMaxINFTime = 0.0, MaxECntMaxINFTime = 0.0, MinEdWeiNeiWeiTime = 0.0, MaxEdWeiNeiWeiTime = 0.0, MinCoefTime = 0.0, MaxCoefTime = 0.0, MinEigenTime = 0.0, MaxEigenTime = 0.0, MinEcceTime = 0.0, MaxEcceTime = 0.0, MinKCoreTime = 0.0, MaxKCoreTime = 0.0, MinTriaTime = 0.0, MaxTriaTime = 0.0, MinTwoHopTime = 0.0, MaxTwoHopTime = 0.0;
                    double[][][] localResult = new double[testloop][30][3];
                    int permLoopCounter = 0;
                    perm permutation = null;

                    /****** Parameters for Graphs ******/
                    int maxNodeEdgeCount = V-1;
                    double pSkip = 0.0;
                    int maxEdgeCount = 0; //(V * (V - 1)) / 2; // zero for random
                    /****** Parameters for Graphs ******/

                    long GraphStartTime = System.nanoTime();
                    Graph g = new Graph(V);
                    if (gtype == 'R')
                        g.createRandomGraphConnected(maxNodeEdgeCount, maxEdgeCount);
                    else if (gtype == 'W') g.createRandomGraphConnected(maxNodeEdgeCount, (V * (V - 1)) / 2);
                    else {
                        pSkip = 0.0; //random.nextDouble();
                        g.createBarabasiGraph(V, ThreadLocalRandom.current().nextInt(4), pSkip);
                    }
                    long GraphEndTime = System.nanoTime();
                    GraphTime = ((double) (GraphEndTime - GraphStartTime)) / 1000000;
                    /********** Permutation Computation **********/
                    {
                        if (runpermutation && V <= permutationlimit) {
                            //System.out.println();
                            permutation = new perm(g);
                            permutation.generatePerm();
                            for (int i = 0; i < permutation.correctPerm.length; i++) {
                                if (permutation.correctPerm[i] > 0) {
                                    permLoopCounter = i;
                                    break;
                                }
                            }
                        }
                    }
                    /********** Repeated Dijkstra **********/
                    {
                        // Apply Dijkstra algorithm to all vertices one by one.
                        long DijkstraLoopStartTime = System.nanoTime();
                        for (int i = 0; i < V; i++)
                            g.dijkstraAllPairsWithLoops(i);
                        long DijkstraLoopEndTime = System.nanoTime();
                        DijkstraLoopTime = ((double) (DijkstraLoopEndTime - DijkstraLoopStartTime)) / 1000000;
                    }
                    /********** Floyd-Warshall **********/
                    {

                        // Floyd-Warshall algorithm is already an all-pairs algorithm.
                        long FloydWarshallStartTime = System.nanoTime();
                        g.fillFloydWarshallArray();
                        g.floydWarshall(0);
                        long FloydWarshallEndTime = System.nanoTime();
                        FloydWarshallTime = ((double) (FloydWarshallEndTime - FloydWarshallStartTime)) / 1000000;
                    }
                    /********** Bellman-Ford **********/
                    {
                        // Apply Bellman-Ford algorithm to all vertices one by one.
                        long BellmanFordStartTime = System.nanoTime();
                        for (int i = 0; i < V; i++)
                            g.BellmanFord(i);
                        long BellmanFordEndTime = System.nanoTime();
                        BellmanFordTime = ((double) (BellmanFordEndTime - BellmanFordStartTime)) / 1000000;
                    }
                    /********** Best Case **********/
                    {
                        // The best case by using the permutation
                        if (runpermutation && V <= permutationlimit) {
                            g.resetDijkstra();
                            long BestStartTime = System.nanoTime();
                            for (int i : permutation.realList) {
                                g.Dijkstra(i,threadResources);
                            }
                            long BestEndTime = System.nanoTime();
                            BestTime = ((double) (BestEndTime - BestStartTime)) / 1000000;
                        }
                    }
                    /********** 1 - MinEdgeCnt **********/
                    {
                        if (runallmetrics || gtype == 'R') {
                            g.resetDijkstra();
                            MinPriorityQueue.clear();
                            MinEdgeCntK = 0;
                            MinEdgeCntFalse = 0;
                            long MinEdgeCntStartTime = System.nanoTime();
                            for (int j = 0; j < V; j++) {
                                Graph.PairSize ps = new Graph.PairSize(j, g.adj.get(j).size());
                                MinPriorityQueue.add(ps);
                            }
                            while (g.dijkstraArrayCount > 0) {
                                int returnVertex = MinPriorityQueue.poll().first;
                                g.Dijkstra(returnVertex,threadResources);
                                MinEdgeCntK++;
                            }
                            long MinEdgeCntEndTime = System.nanoTime();
                            MinEdgeCntTime = ((double) (MinEdgeCntEndTime - MinEdgeCntStartTime)) / 1000000;
                            if (runpermutation && V <= permutationlimit) {
                                for (int i = 0; i < MinEdgeCntK; i++) {
                                    if (permutation.correctPerm[i] > 0) {
                                        MinEdgeCntFalse++;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    /********** 2 - MaxEdgeCnt **********/
                    {
                        if (runallmetrics) {
                            g.resetDijkstra();
                            MaxPriorityQueue.clear();
                            MaxEdgeCntK = 0;
                            MaxEdgeCntFalse = 0;
                            long MaxEdgeCntStartTime = System.nanoTime();
                            for (int j = 0; j < V; j++) {
                                Graph.PairSize ps = new Graph.PairSize(j, g.adj.get(j).size());
                                MaxPriorityQueue.add(ps);
                            }
                            while (g.dijkstraArrayCount > 0) {
                                int returnVertex = MaxPriorityQueue.poll().first;
                                g.Dijkstra(returnVertex,threadResources);
                                MaxEdgeCntK++;
                            }
                            long MaxEdgeCntEndTime = System.nanoTime();
                            MaxEdgeCntTime = ((double) (MaxEdgeCntEndTime - MaxEdgeCntStartTime)) / 1000000;
                            if (runpermutation && V <= permutationlimit) {
                                for (int i = 0; i < MaxEdgeCntK; i++) {
                                    if (permutation.correctPerm[i] > 0) {
                                        MaxEdgeCntFalse++;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    /********** 3 - MinEdgeWeight **********/
                    {
                        if (runallmetrics) {
                            g.resetDijkstra();
                            DoubleMinPriorityQueue.clear();
                            MinEdgeWeightK = 0;
                            MinEdgeWeightFalse = 0;
                            long MinEdgeWeightStartTime = System.nanoTime();
                            for (int j = 0; j < V; j++) {
                                Graph.dPairSize ps = new Graph.dPairSize(j, g.getNodeAverageWeight(j));
                                DoubleMinPriorityQueue.add(ps);
                            }
                            while (g.dijkstraArrayCount > 0) {
                                int returnVertex = DoubleMinPriorityQueue.poll().first;
                                g.Dijkstra(returnVertex,threadResources);
                                MinEdgeWeightK++;
                            }
                            long MinEdgeWeightEndTime = System.nanoTime();
                            MinEdgeWeightTime = ((double) (MinEdgeWeightEndTime - MinEdgeWeightStartTime)) / 1000000;
                            if (runpermutation && V <= permutationlimit) {
                                for (int i = 0; i < MinEdgeWeightK; i++) {
                                    if (permutation.correctPerm[i] > 0) {
                                        MinEdgeWeightFalse++;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    /********** 4 - MaxEdgeWeight **********/
                    {
                        if (runallmetrics) {
                            g.resetDijkstra();
                            DoubleMaxPriorityQueue.clear();
                            MaxEdgeWeightK = 0;
                            MaxEdgeWeightFalse = 0;
                            long MaxEdgeWeightStartTime = System.nanoTime();
                            for (int j = 0; j < V; j++) {
                                Graph.dPairSize ps = new Graph.dPairSize(j, g.getNodeAverageWeight(j));
                                DoubleMaxPriorityQueue.add(ps);
                            }
                            while (g.dijkstraArrayCount > 0) {
                                int returnVertex = DoubleMaxPriorityQueue.poll().first;
                                g.Dijkstra(returnVertex,threadResources);
                                MaxEdgeWeightK++;
                            }
                            long MaxEdgeWeightEndTime = System.nanoTime();
                            MaxEdgeWeightTime = ((double) (MaxEdgeWeightEndTime - MaxEdgeWeightStartTime)) / 1000000;
                            if (runpermutation && V <= permutationlimit) {
                                for (int i = 0; i < MaxEdgeWeightK; i++) {
                                    if (permutation.correctPerm[i] > 0) {
                                        MaxEdgeWeightFalse++;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    /********** 7 - MinECntMaxINF **********/
                    {
                        if (runallmetrics) {
                            g.resetDijkstra();
                            MinPriorityQueue.clear();
                            MinECntMaxINFK = 0;
                            MinECntMaxINFFalse = 0;
                            long MinECntMaxINFStartTime = System.nanoTime();
                            for (int j = 0; j < V; j++) {
                                Graph.PairSize ps = new Graph.PairSize(j, g.adj.get(j).size());
                                MinPriorityQueue.add(ps);
                            }
                            int returnVertex = MinPriorityQueue.poll().first;
                            g.Dijkstra(returnVertex,threadResources);
                            MinECntMaxINFK++;
                            while (g.dijkstraArrayCount > 0) {
                                int max = Integer.MIN_VALUE;
                                int j = 0;
                                for (int i = V; i < g.dijkstraArray.length; i = (j+1) * V + j) {
                                    int arrayval = g.dijkstraArray[i]* 10000 + (g.adj.get(j).size() * (-1));
                                    if (arrayval > max) {
                                        max = arrayval;
                                        returnVertex = j;
                                    }
                                    j++;
                                }
                                g.Dijkstra(returnVertex,threadResources);
                                MinECntMaxINFK++;
                            }
                            long MinECntMaxINFEndTime = System.nanoTime();
                            MinECntMaxINFTime = ((double) (MinECntMaxINFEndTime - MinECntMaxINFStartTime)) / 1000000;
                            if (runpermutation && V <= permutationlimit) {
                                for (int i = 0; i < MinECntMaxINFK; i++) {
                                    if (permutation.correctPerm[i] > 0) {
                                        MinECntMaxINFFalse++;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    /********** 8 - MaxECntMaxINF **********/
                    {
                        if (runallmetrics) {
                            g.resetDijkstra();
                            MaxPriorityQueue.clear();
                            MaxECntMaxINFK = 0;
                            MaxECntMaxINFFalse = 0;
                            long MaxECntMaxINFStartTime = System.nanoTime();
                            for (int j = 0; j < V; j++) {
                                Graph.PairSize ps = new Graph.PairSize(j, g.adj.get(j).size());
                                MaxPriorityQueue.add(ps);
                            }
                            int returnVertex = MaxPriorityQueue.poll().first;
                            g.Dijkstra(returnVertex,threadResources);
                            MaxECntMaxINFK++;
                            while (g.dijkstraArrayCount > 0) {
                                int max = Integer.MIN_VALUE;
                                int j = 0;
                                for (int i = V; i < g.dijkstraArray.length; i = (j+1) * V + j) {
                                    int arrayval = g.dijkstraArray[i]* 10000 + (g.adj.get(j).size() * (-1));
                                    if (arrayval > max) {
                                        max = arrayval;
                                        returnVertex = j;
                                    }
                                    j++;
                                }
                                g.Dijkstra(returnVertex,threadResources);
                                MaxECntMaxINFK++;
                            }
                            long MaxECntMaxINFEndTime = System.nanoTime();
                            MaxECntMaxINFTime = ((double) (MaxECntMaxINFEndTime - MaxECntMaxINFStartTime)) / 1000000;
                            if (runpermutation && V <= permutationlimit) {
                                for (int i = 0; i < MaxECntMaxINFK; i++) {
                                    if (permutation.correctPerm[i] > 0) {
                                        MaxECntMaxINFFalse++;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    /********** 5 - MinEdWeiNeiWei **********/
                    {
                        if (runallmetrics) {
                            g.resetDijkstra();
                            DoubleMinPriorityQueue.clear();
                            MinEdWeiNeiWeiK = 0;
                            MinEdWeiNeiWeiFalse = 0;
                            long MinEdWeiNeiWeiStartTime = System.nanoTime();
                            for (int j = 0; j < V; j++) {
                                Graph.dPairSize ps = new Graph.dPairSize(j, g.getNodeAverageWeight(j) + g.getNodeNeighboursAvgWeight(j));
                                DoubleMinPriorityQueue.add(ps);
                            }
                            while (g.dijkstraArrayCount > 0) {
                                int returnVertex = DoubleMinPriorityQueue.poll().first;
                                g.Dijkstra(returnVertex,threadResources);
                                MinEdWeiNeiWeiK++;
                            }
                            long MinEdWeiNeiWeiEndTime = System.nanoTime();
                            MinEdWeiNeiWeiTime = ((double) (MinEdWeiNeiWeiEndTime - MinEdWeiNeiWeiStartTime)) / 1000000;
                            if (runpermutation && V <= permutationlimit) {
                                for (int i = 0; i < MinEdWeiNeiWeiK; i++) {
                                    if (permutation.correctPerm[i] > 0) {
                                        MinEdWeiNeiWeiFalse++;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    /********** 6 - MaxEdWeiNeiWei **********/
                    {
                        if (runallmetrics) {
                            g.resetDijkstra();
                            DoubleMaxPriorityQueue.clear();
                            MaxEdWeiNeiWeiK = 0;
                            MaxEdWeiNeiWeiFalse = 0;
                            long MaxEdWeiNeiWeiStartTime = System.nanoTime();
                            for (int j = 0; j < V; j++) {
                                Graph.dPairSize ps = new Graph.dPairSize(j, g.getNodeAverageWeight(j) + g.getNodeNeighboursAvgWeight(j));
                                DoubleMaxPriorityQueue.add(ps);
                            }
                            while (g.dijkstraArrayCount > 0) {
                                int returnVertex = DoubleMaxPriorityQueue.poll().first;
                                g.Dijkstra(returnVertex,threadResources);
                                MaxEdWeiNeiWeiK++;
                            }
                            long MaxEdWeiNeiWeiEndTime = System.nanoTime();
                            MaxEdWeiNeiWeiTime = ((double) (MaxEdWeiNeiWeiEndTime - MaxEdWeiNeiWeiStartTime)) / 1000000;
                            if (runpermutation && V <= permutationlimit) {
                                for (int i = 0; i < MaxEdWeiNeiWeiK; i++) {
                                    if (permutation.correctPerm[i] > 0) {
                                        MaxEdWeiNeiWeiFalse++;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    /********** 9 - MinCoef **********/
                    {
                        if (runallmetrics) {
                            g.resetDijkstra();
                            ClusteringCoefficientPQAsc.clear();
                            MinCoefK = 0;
                            MinCoefFalse = 0;
                            long MinCoefStartTime = System.nanoTime();
                            g.clusteringCoefficient(ClusteringCoefficientPQAsc);
                            while (g.dijkstraArrayCount > 0) {
                                int returnVertex = ClusteringCoefficientPQAsc.poll().first;
                                g.Dijkstra(returnVertex,threadResources);
                                MinCoefK++;
                            }
                            long MinCoefEndTime = System.nanoTime();
                            MinCoefTime = ((double) (MinCoefEndTime - MinCoefStartTime)) / 1000000;
                            if (runpermutation && V <= permutationlimit) {
                                for (int i = 0; i < MinCoefK; i++) {
                                    if (permutation.correctPerm[i] > 0) {
                                        MinCoefFalse++;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    /********** 10 - MaxCoef **********/
                    {
                        if (runallmetrics || gtype == 'R') {
                            g.resetDijkstra();
                            ClusteringCoefficientPQDesc.clear();
                            MaxCoefK = 0;
                            MaxCoefFalse = 0;
                            long MaxCoefStartTime = System.nanoTime();
                            g.clusteringCoefficient(ClusteringCoefficientPQDesc);
                            while (g.dijkstraArrayCount > 0) {
                                int returnVertex = ClusteringCoefficientPQDesc.poll().first;
                                g.Dijkstra(returnVertex,threadResources);
                                MaxCoefK++;
                            }
                            long MaxCoefEndTime = System.nanoTime();
                            MaxCoefTime = ((double) (MaxCoefEndTime - MaxCoefStartTime)) / 1000000;
                            if (runpermutation && V <= permutationlimit) {
                                for (int i = 0; i < MaxCoefK; i++) {
                                    if (permutation.correctPerm[i] > 0) {
                                        MaxCoefFalse++;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    TotalDijkstraError.addAndGet(g.compareArraysDijkstraFloyd());
                    /********** 11 - EigenMin **********/
                    {
                        if (runallmetrics || gtype == 'R') {
                            g.resetDijkstra();
                            MinPriorityQueue.clear();
                            MinEigenK = 0;
                            MinEigenFalse = 0;
                            long MinEigenStartTime = System.nanoTime();
                            g.eigenvectorCentralityMinPQ(MinPriorityQueue, 1000, 1e-6);
                            while (g.dijkstraArrayCount > 0) {
                                int returnVertex = MinPriorityQueue.poll().first;
                                g.Dijkstra(returnVertex,threadResources);
                                MinEigenK++;
                            }
                            long MinEigenEndTime = System.nanoTime();
                            MinEigenTime = ((double) (MinEigenEndTime - MinEigenStartTime)) / 1000000;
                            if (runpermutation && V <= permutationlimit) {
                                for (int i = 0; i < MinEigenK; i++) {
                                    if (permutation.correctPerm[i] > 0) {
                                        MinEigenFalse++;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    /********** 12 - EigenMax **********/
                    {
                        if (runallmetrics || gtype == 'R') {
                            g.resetDijkstra();
                            MaxPriorityQueue.clear();
                            MaxEigenK = 0;
                            MaxEigenFalse = 0;
                            long MaxEigenStartTime = System.nanoTime();
                            g.eigenvectorCentralityMaxPQ(MaxPriorityQueue, 1000, 1e-6);
                            while (g.dijkstraArrayCount > 0) {
                                int returnVertex = MaxPriorityQueue.poll().first;
                                g.Dijkstra(returnVertex,threadResources);
                                MaxEigenK++;
                            }
                            long MaxEigenEndTime = System.nanoTime();
                            MaxEigenTime = ((double) (MaxEigenEndTime - MaxEigenStartTime)) / 1000000;
                            if (runpermutation && V <= permutationlimit) {
                                for (int i = 0; i < MaxEigenK; i++) {
                                    if (permutation.correctPerm[i] > 0) {
                                        MaxEigenFalse++;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    /********** 13 - MinKCore **********/
                    {
                        if (runallmetrics || gtype == 'R') {
                            g.resetDijkstra();
                            MinPriorityQueue.clear();
                            MinKCoreK = 0;
                            MinKCoreFalse = 0;
                            long MinKCoreStartTime = System.nanoTime();
                            g.calculateCoreNumbers(MinPriorityQueue);
                            while (g.dijkstraArrayCount > 0) {
                                int returnVertex = MinPriorityQueue.poll().first;
                                g.Dijkstra(returnVertex,threadResources);
                                MinKCoreK++;
                            }
                            long MinKCoreEndTime = System.nanoTime();
                            MinKCoreTime = ((double) (MinKCoreEndTime - MinKCoreStartTime)) / 1000000;
                            if (runpermutation && V <= permutationlimit) {
                                for (int i = 0; i < MinKCoreK; i++) {
                                    if (permutation.correctPerm[i] > 0) {
                                        MinKCoreFalse++;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    /********** 14 - MaxKCore **********/
                    {
                        if (runallmetrics || gtype == 'R') {
                            g.resetDijkstra();
                            MaxPriorityQueue.clear();
                            MaxKCoreK = 0;
                            MaxKCoreFalse = 0;
                            long MaxKCoreStartTime = System.nanoTime();
                            g.calculateCoreNumbers(MaxPriorityQueue);
                            while (g.dijkstraArrayCount > 0) {
                                int returnVertex = MaxPriorityQueue.poll().first;
                                g.Dijkstra(returnVertex,threadResources);
                                MaxKCoreK++;
                            }
                            long MaxKCoreEndTime = System.nanoTime();
                            MaxKCoreTime = ((double) (MaxKCoreEndTime - MaxKCoreStartTime)) / 1000000;
                            if (runpermutation && V <= permutationlimit) {
                                for (int i = 0; i < MaxKCoreK; i++) {
                                    if (permutation.correctPerm[i] > 0) {
                                        MaxKCoreFalse++;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    /********** 15 - MinTria **********/
                    {
                        if (runallmetrics) {
                            g.resetDijkstra();
                            MinTriaK = 0;
                            MinTriaFalse = 0;
                            long MinTriaStartTime = System.nanoTime();
                            int[] core = g.triangleCount();
                            while (g.dijkstraArrayCount > 0) {
                                int minIndex = 0;
                                for (int i = 1; i < core.length; i++) {
                                    if (core[i] < core[minIndex]) {
                                        minIndex = i;
                                    }
                                }
                                core[minIndex] = Integer.MAX_VALUE;
                                int returnVertex = minIndex;
                                g.Dijkstra(returnVertex,threadResources);
                                MinTriaK++;
                            }
                            long MinTriaEndTime = System.nanoTime();
                            MinTriaTime = ((double) (MinTriaEndTime - MinTriaStartTime)) / 1000000;
                            if (runpermutation && V <= permutationlimit) {
                                for (int i = 0; i < MinTriaK; i++) {
                                    if (permutation.correctPerm[i] > 0) {
                                        MinTriaFalse++;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    /********** 16 - MaxTria **********/
                    {
                        if (runallmetrics) {
                            g.resetDijkstra();
                            MaxTriaK = 0;
                            MaxTriaFalse = 0;
                            long MaxTriaStartTime = System.nanoTime();
                            int[] core = g.triangleCount();
                            while (g.dijkstraArrayCount > 0) {
                                int maxIndex = 0;
                                for (int i = 1; i < core.length; i++) {
                                    if (core[i] > core[maxIndex]) {
                                        maxIndex = i;
                                    }
                                }
                                core[maxIndex] = Integer.MIN_VALUE;
                                int returnVertex = maxIndex;
                                g.Dijkstra(returnVertex,threadResources);
                                MaxTriaK++;
                            }
                            long MaxTriaEndTime = System.nanoTime();
                            MaxTriaTime = ((double) (MaxTriaEndTime - MaxTriaStartTime)) / 1000000;
                            if (runpermutation && V <= permutationlimit) {
                                for (int i = 0; i < MaxTriaK; i++) {
                                    if (permutation.correctPerm[i] > 0) {
                                        MaxTriaFalse++;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    /********** 17 - MinTwoHop **********/
                    {
                        if (runallmetrics) {
                            g.resetDijkstra();
                            MinPriorityQueue.clear();
                            MinTwoHopK = 0;
                            MinTwoHopFalse = 0;
                            long MinTwoHopStartTime = System.nanoTime();
                            g.twoHopDegree(MinPriorityQueue);
                            while (g.dijkstraArrayCount > 0) {
                                int returnVertex = MinPriorityQueue.poll().first;
                                g.Dijkstra(returnVertex,threadResources);
                                MinTwoHopK++;
                            }
                            long MinTwoHopEndTime = System.nanoTime();
                            MinTwoHopTime = ((double) (MinTwoHopEndTime - MinTwoHopStartTime)) / 1000000;
                            if (runpermutation && V <= permutationlimit) {
                                for (int i = 0; i < MinTwoHopK; i++) {
                                    if (permutation.correctPerm[i] > 0) {
                                        MinTwoHopFalse++;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    /********** 18 - MaxTwoHop **********/
                    {
                        if (runallmetrics) {
                            g.resetDijkstra();
                            MaxPriorityQueue.clear();
                            MaxTwoHopK = 0;
                            MaxTwoHopFalse = 0;
                            long MaxTwoHopStartTime = System.nanoTime();
                            g.twoHopDegree(MaxPriorityQueue);
                            while (g.dijkstraArrayCount > 0) {
                                int returnVertex = MaxPriorityQueue.poll().first;
                                g.Dijkstra(returnVertex,threadResources);
                                MaxTwoHopK++;
                            }
                            long MaxTwoHopEndTime = System.nanoTime();
                            MaxTwoHopTime = ((double) (MaxTwoHopEndTime - MaxTwoHopStartTime)) / 1000000;
                            if (runpermutation && V <= permutationlimit) {
                                for (int i = 0; i < MaxTwoHopK; i++) {
                                    if (permutation.correctPerm[i] > 0) {
                                        MaxTwoHopFalse++;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    localResult[currentLoop][0][0] = GraphTime;
                    localResult[currentLoop][1][0] = DijkstraLoopTime;
                    localResult[currentLoop][2][0] = BellmanFordTime;
                    localResult[currentLoop][3][0] = FloydWarshallTime;
                    localResult[currentLoop][4][0] = BestTime;
                    localResult[currentLoop][5][0] = MinEdgeCntTime;
                    localResult[currentLoop][6][0] = MaxEdgeCntTime;
                    localResult[currentLoop][7][0] = MinEdgeWeightTime;
                    localResult[currentLoop][8][0] = MaxEdgeWeightTime;
                    localResult[currentLoop][9][0] = MinECntMaxINFTime;
                    localResult[currentLoop][10][0] = MaxECntMaxINFTime;
                    localResult[currentLoop][11][0] = MinEdWeiNeiWeiTime;
                    localResult[currentLoop][12][0] = MaxEdWeiNeiWeiTime;
                    localResult[currentLoop][13][0] = MinCoefTime;
                    localResult[currentLoop][14][0] = MaxCoefTime;
                    localResult[currentLoop][15][0] = MinEigenTime;
                    localResult[currentLoop][16][0] = MaxEigenTime;
                    localResult[currentLoop][17][0] = MinEcceTime;
                    localResult[currentLoop][18][0] = MaxEcceTime;
                    localResult[currentLoop][19][0] = MinKCoreTime;
                    localResult[currentLoop][20][0] = MaxKCoreTime;
                    localResult[currentLoop][21][0] = MinTriaTime;
                    localResult[currentLoop][22][0] = MaxTriaTime;
                    localResult[currentLoop][23][0] = MinTwoHopTime;
                    localResult[currentLoop][24][0] = MaxTwoHopTime;
                    localResult[currentLoop][25][0] = g.graphEdgeCount;
                    localResult[currentLoop][26][0] = permLoopCounter;
                    localResult[currentLoop][27][0] = pSkip;

                    localResult[currentLoop][5][1] = MinEdgeCntK;
                    localResult[currentLoop][6][1] = MaxEdgeCntK;
                    localResult[currentLoop][7][1] = MinEdgeWeightK;
                    localResult[currentLoop][8][1] = MaxEdgeWeightK;
                    localResult[currentLoop][9][1] = MinECntMaxINFK;
                    localResult[currentLoop][10][1] = MaxECntMaxINFK;
                    localResult[currentLoop][11][1] = MinEdWeiNeiWeiK;
                    localResult[currentLoop][12][1] = MaxEdWeiNeiWeiK;
                    localResult[currentLoop][13][1] = MinCoefK;
                    localResult[currentLoop][14][1] = MaxCoefK;
                    localResult[currentLoop][15][1] = MinEigenK;
                    localResult[currentLoop][16][1] = MaxEigenK;
                    localResult[currentLoop][17][1] = MinEcceK;
                    localResult[currentLoop][18][1] = MaxEcceK;
                    localResult[currentLoop][19][1] = MinKCoreK;
                    localResult[currentLoop][20][1] = MaxKCoreK;
                    localResult[currentLoop][21][1] = MinTriaK;
                    localResult[currentLoop][22][1] = MaxTriaK;
                    localResult[currentLoop][23][1] = MinTwoHopK;
                    localResult[currentLoop][24][1] = MaxTwoHopK;

                    localResult[currentLoop][5][2] = MinEdgeCntFalse;
                    localResult[currentLoop][6][2] = MaxEdgeCntFalse;
                    localResult[currentLoop][7][2] = MinEdgeWeightFalse;
                    localResult[currentLoop][8][2] = MaxEdgeWeightFalse;
                    localResult[currentLoop][9][2] = MinECntMaxINFFalse;
                    localResult[currentLoop][10][2] = MaxECntMaxINFFalse;
                    localResult[currentLoop][11][2] = MinEdWeiNeiWeiFalse;
                    localResult[currentLoop][12][2] = MaxEdWeiNeiWeiFalse;
                    localResult[currentLoop][13][2] = MinCoefFalse;
                    localResult[currentLoop][14][2] = MaxCoefFalse;
                    localResult[currentLoop][15][2] = MinEigenFalse;
                    localResult[currentLoop][16][2] = MaxEigenFalse;
                    localResult[currentLoop][17][2] = MinEcceFalse;
                    localResult[currentLoop][18][2] = MaxEcceFalse;
                    localResult[currentLoop][19][2] = MinKCoreFalse;
                    localResult[currentLoop][20][2] = MaxKCoreFalse;
                    localResult[currentLoop][21][2] = MinTriaFalse;
                    localResult[currentLoop][22][2] = MaxTriaFalse;
                    localResult[currentLoop][23][2] = MinTwoHopFalse;
                    localResult[currentLoop][24][2] = MaxTwoHopFalse;

                    return new LoopResult(currentLoop, localResult);
                });
            }
            for (int i = 0; i < testloop; i++) {

                Future<LoopResult> f = cs.take();
                LoopResult lr = f.get();

                int wloop = lr.loop;
                result[wloop] = lr.result[wloop];

                System.out.println();
                res = String.format("i:%4d, Lp: %4d, Time: %6tT", i+1, wloop + 1,new Date());
                System.out.print(res);
                bw.write(wloop + 1 + "," + V + ",");
                for (int j = 0; j < 28; j++) {
                    if (j < 28)
                        bw.write(String.format("%.4f", result[wloop][j][0]) + ",");
                    else
                        bw.write(String.format("%.4f", result[wloop][j][0]));
                    /*if (j == 25)
                        System.out.printf("%8d", (int) result[wloop][j][0]);
                    else
                        System.out.printf("%10.4f", result[wloop][j][0]);*/
                }
                bw.newLine();
                if (wloop > avgloop - 1) {
                    TotalGraphTime += lr.result[wloop][0][0];//GraphTime;
                    TotalDijkstraLoopTime += lr.result[wloop][1][0];//DijkstraLoopTime;
                    TotalBellmanFordTime += lr.result[wloop][2][0];//BellmanFordTime;
                    TotalFloydWarshallTime += lr.result[wloop][3][0];//FloydWarshallTime;
                    TotalBestTime += lr.result[wloop][4][0];//BestTime;
                    TotalMinEdgeCntTime += lr.result[wloop][5][0];//MinEdgeCntTime;
                    TotalMaxEdgeCntTime += lr.result[wloop][6][0];//MaxEdgeCntTime;
                    TotalMinEdgeWeightTime += lr.result[wloop][7][0];//MinEdgeWeightTime;
                    TotalMaxEdgeWeightTime += lr.result[wloop][8][0];//MaxEdgeWeightTime;
                    TotalMinECntMaxINFTime += lr.result[wloop][9][0];//MinECntMaxINFTime;
                    TotalMaxECntMaxINFTime += lr.result[wloop][10][0];//MaxECntMaxINFTime;
                    TotalMinEdWeiNeiWeiTime += lr.result[wloop][11][0];//MinEdWeiNeiWeiTime;
                    TotalMaxEdWeiNeiWeiTime += lr.result[wloop][12][0];//MaxEdWeiNeiWeiTime;
                    TotalMinCoefTime += lr.result[wloop][13][0];//MinCoefTime;
                    TotalMaxCoefTime += lr.result[wloop][14][0];//MaxCoefTime;
                    TotalMinEigenTime += lr.result[wloop][15][0];//MinEigenTime;
                    TotalMaxEigenTime += lr.result[wloop][16][0];//MaxEigenTime;
                    TotalMinEcceTime += lr.result[wloop][17][0];//MinEcceTime;
                    TotalMaxEcceTime += lr.result[wloop][18][0];//MaxEcceTime;
                    TotalMinKCoreTime += lr.result[wloop][19][0];//MinKCoreTime;
                    TotalMaxKCoreTime += lr.result[wloop][20][0];//MaxKCoreTime;
                    TotalMinTriaTime += lr.result[wloop][21][0];//MinTriaTime;
                    TotalMaxTriaTime += lr.result[wloop][22][0];//MaxTriaTime;
                    TotalMinTwoHopTime += lr.result[wloop][23][0];//MinTwoHopTime;
                    TotalMaxTwoHopTime += lr.result[wloop][24][0];//MaxTwoHopTime;
                    TotalEdgeCount += lr.result[wloop][25][0];//g.graphEdgeCount;
                    TotalPermLoopCounter += lr.result[wloop][26][0];//permLoopCounter;
                    TotalpSkip += lr.result[wloop][27][0];//pSkip;

                    TotalMinEdgeCntK += lr.result[wloop][5][1];//MinEdgeCntK;
                    TotalMaxEdgeCntK += lr.result[wloop][6][1];//MaxEdgeCntK;
                    TotalMinEdgeWeightK += lr.result[wloop][7][1];//MinEdgeWeightK;
                    TotalMaxEdgeWeightK += lr.result[wloop][8][1];//MaxEdgeWeightK;
                    TotalMinECntMaxINFK += lr.result[wloop][9][1];//MinECntMaxINFK;
                    TotalMaxECntMaxINFK += lr.result[wloop][10][1];//MaxECntMaxINFK;
                    TotalMinEdWeiNeiWeiK += lr.result[wloop][11][1];//MinEdWeiNeiWeiK;
                    TotalMaxEdWeiNeiWeiK += lr.result[wloop][12][1];//MaxEdWeiNeiWeiK;
                    TotalMinCoefK += lr.result[wloop][13][1];//MinCoefK;
                    TotalMaxCoefK += lr.result[wloop][14][1];//MaxCoefK;
                    TotalMinEigenK += lr.result[wloop][15][1];//MinEigenK;
                    TotalMaxEigenK += lr.result[wloop][16][1];//MaxEigenK;
                    TotalMinEcceK += lr.result[wloop][17][1];//MinEcceK;
                    TotalMaxEcceK += lr.result[wloop][18][1];//MaxEcceK;
                    TotalMinKCoreK += lr.result[wloop][19][1];//MinKCoreK;
                    TotalMaxKCoreK += lr.result[wloop][20][1];//MaxKCoreK;
                    TotalMinTriaK += lr.result[wloop][21][1];//MinTriaK;
                    TotalMaxTriaK += lr.result[wloop][22][1];//MaxTriaK;
                    TotalMinTwoHopK += lr.result[wloop][23][1];//MinTwoHopK;
                    TotalMaxTwoHopK += lr.result[wloop][24][1];//MaxTwoHopK;

                    TotalMinEdgeCntFalse += lr.result[wloop][5][2];//MinEdgeCntFalse;
                    TotalMaxEdgeCntFalse += lr.result[wloop][6][2];//MaxEdgeCntFalse;
                    TotalMinEdgeWeightFalse += lr.result[wloop][7][2];//MinEdgeWeightFalse;
                    TotalMaxEdgeWeightFalse += lr.result[wloop][8][2];//MaxEdgeWeightFalse;
                    TotalMinECntMaxINFFalse += lr.result[wloop][9][2];//MinECntMaxINFFalse;
                    TotalMaxECntMaxINFFalse += lr.result[wloop][10][2];//MaxECntMaxINFFalse;
                    TotalMinEdWeiNeiWeiFalse += lr.result[wloop][11][2];//MinEdWeiNeiWeiFalse;
                    TotalMaxEdWeiNeiWeiFalse += lr.result[wloop][12][2];//MaxEdWeiNeiWeiFalse;
                    TotalMinCoefFalse += lr.result[wloop][13][2];//MinCoefFalse;
                    TotalMaxCoefFalse += lr.result[wloop][14][2];//MaxCoefFalse;
                    TotalMinEigenFalse += lr.result[wloop][15][2];//MinEigenFalse;
                    TotalMaxEigenFalse += lr.result[wloop][16][2];//MaxEigenFalse;
                    TotalMinEcceFalse += lr.result[wloop][17][2];//MinEcceFalse;
                    TotalMaxEcceFalse += lr.result[wloop][18][2];//MaxEcceFalse;
                    TotalMinKCoreFalse += lr.result[wloop][19][2];//MinKCoreFalse;
                    TotalMaxKCoreFalse += lr.result[wloop][20][2];//MaxKCoreFalse;
                    TotalMinTriaFalse += lr.result[wloop][21][2];//MinTriaFalse;
                    TotalMaxTriaFalse += lr.result[wloop][22][2];//MaxTriaFalse;
                    TotalMinTwoHopFalse += lr.result[wloop][23][2];//MinTwoHopFalse;
                    TotalMaxTwoHopFalse += lr.result[wloop][24][2];//MaxTwoHopFalse;

                }
            }

            int TestCount = testloop - avgloop;
            double AverageGraphTime = TotalGraphTime / TestCount;
            double AverageDijkstraLoopTime = TotalDijkstraLoopTime / TestCount;
            double AverageBellmanFordTime = TotalBellmanFordTime / TestCount;
            double AverageFloydWarshallTime = TotalFloydWarshallTime / TestCount;
            double AverageBestTime = TotalBestTime / TestCount;
            double AverageMinEdgeCntTime = TotalMinEdgeCntTime / TestCount;
            double AverageMaxEdgeCntTime = TotalMaxEdgeCntTime / TestCount;
            double AverageMinEdgeWeightTime = TotalMinEdgeWeightTime / TestCount;
            double AverageMaxEdgeWeightTime = TotalMaxEdgeWeightTime / TestCount;
            double AverageMinECntMaxINFTime = TotalMinECntMaxINFTime / TestCount;
            double AverageMaxECntMaxINFTime = TotalMaxECntMaxINFTime / TestCount;
            double AverageMinEdWeiNeiWeiTime = TotalMinEdWeiNeiWeiTime / TestCount;
            double AverageMaxEdWeiNeiWeiTime = TotalMaxEdWeiNeiWeiTime / TestCount;
            double AverageMinCoefTime = TotalMinCoefTime / TestCount;
            double AverageMaxCoefTime = TotalMaxCoefTime / TestCount;
            double AverageMinEigenTime = TotalMinEigenTime / TestCount;
            double AverageMaxEigenTime = TotalMaxEigenTime / TestCount;
            double AverageMinEcceTime = TotalMinEcceTime / TestCount;
            double AverageMaxEcceTime = TotalMaxEcceTime / TestCount;
            double AverageMinKCoreTime = TotalMinKCoreTime / TestCount;
            double AverageMaxKCoreTime = TotalMaxKCoreTime / TestCount;
            double AverageMinTriaTime = TotalMinTriaTime / TestCount;
            double AverageMaxTriaTime = TotalMaxTriaTime / TestCount;
            double AverageMinTwoHopTime = TotalMinTwoHopTime / TestCount;
            double AverageMaxTwoHopTime = TotalMaxTwoHopTime / TestCount;
            double AverageEdgeCount = (double) TotalEdgeCount / TestCount;
            double AveragePermLoopCounter = (double) TotalPermLoopCounter / TestCount;
            double AveragepSkip = (double) TotalpSkip / TestCount;

            /*System.out.printf("%n");
            System.out.printf("%32s%10s%10s%10s%10s%10s%10s%10s%10s%10s%10s%10s%10s%10s%10s%10s%10s%10s%10s%10s%10s%10s%10s%10s%10s%10s%8s%9s%9s%n", "", "GraphT", "DLoopT", "BellmanT", "FloydT", "BestT", "MiEdCT", "MaEdCT", "MiEdWT", "MaEdWT", "MiECnMaIn", "MaECnMaIn", "MiEdWNeW", "MaEdWNeW", "MiCoefT", "MaCoefT", "MiEigT", "MaEigT", "MiEcce", "MaEcce", "MiKCore", "MaKCore", "MiTria", "MaTria", "MiTwoHop", "MaTwoHop", "EdCnt", "Perm", "pSkip");

            System.out.printf("%32s%10.4f%10.4f%10.4f%10.4f%10.4f%10.4f%10.4f%10.4f%10.4f%10.4f%10.4f%10.4f%10.4f%10.4f%10.4f%10.4f%10.4f%10.4f%10.4f%10.4f%10.4f%10.4f%10.4f%10.4f%10.4f%10.4f%9.4f%9.4f%n", "", AverageGraphTime, AverageDijkstraLoopTime,
                    AverageBellmanFordTime, AverageFloydWarshallTime, AverageBestTime, AverageMinEdgeCntTime, AverageMaxEdgeCntTime, AverageMinEdgeWeightTime,
                    AverageMaxEdgeWeightTime, AverageMinECntMaxINFTime, AverageMaxECntMaxINFTime, AverageMinEdWeiNeiWeiTime, AverageMaxEdWeiNeiWeiTime, AverageMinCoefTime, AverageMaxCoefTime, AverageMinEigenTime,
                    AverageMaxEigenTime, AverageMinEcceTime, AverageMaxEcceTime, AverageMinKCoreTime, AverageMaxKCoreTime, AverageMinTriaTime, AverageMaxTriaTime,
                    AverageMinTwoHopTime, AverageMaxTwoHopTime, AverageEdgeCount, AveragePermLoopCounter, AveragepSkip);
            */
            System.out.printf("Dijkstra Error: %d \n", TotalDijkstraError.get());
            /*System.out.printf("Permutation: %5.2f \n", AveragePermLoopCounter);
            System.out.printf("MinEdgeCnt: %d %5.2f \n", TotalMinEdgeCntFalse, (double) TotalMinEdgeCntK / TestCount);
            System.out.printf("MaxEdgeCnt: %d %5.2f \n", TotalMaxEdgeCntFalse, (double) TotalMaxEdgeCntK / TestCount);
            System.out.printf("MinEdgeWeight: %d %5.2f \n", TotalMinEdgeWeightFalse, (double) TotalMinEdgeWeightK / TestCount);
            System.out.printf("MaxEdgeWeight: %d %5.2f \n", TotalMaxEdgeWeightFalse, (double) TotalMaxEdgeWeightK / TestCount);
            System.out.printf("MinECntMaxINF: %d %5.2f \n", TotalMinECntMaxINFFalse, (double) TotalMinECntMaxINFK / TestCount);
            System.out.printf("MaxECntMaxINF: %d %5.2f \n", TotalMaxECntMaxINFFalse, (double) TotalMaxECntMaxINFK / TestCount);
            System.out.printf("MinEdWeiNeiWei: %d %5.2f \n", TotalMinEdWeiNeiWeiFalse, (double) TotalMinEdWeiNeiWeiK / TestCount);
            System.out.printf("MaxEdWeiNeiWei: %d %5.2f \n", TotalMaxEdWeiNeiWeiFalse, (double) TotalMaxEdWeiNeiWeiK / TestCount);
            System.out.printf("MinCoef: %d %5.2f \n", TotalMinCoefFalse, (double) TotalMinCoefK / TestCount);
            System.out.printf("MaxCoef: %d %5.2f \n", TotalMaxCoefFalse, (double) TotalMaxCoefK / TestCount);
            System.out.printf("MinEigen: %d %5.2f \n", TotalMinEigenFalse, (double) TotalMinEigenK / TestCount);
            System.out.printf("MaxEigen: %d %5.2f \n", TotalMaxEigenFalse, (double) TotalMaxEigenK / TestCount);
            System.out.printf("MinEcce: %d %5.2f \n", TotalMinEcceFalse, (double) TotalMinEcceK / TestCount);
            System.out.printf("MaxEcce: %d %5.2f \n", TotalMaxEcceFalse, (double) TotalMaxEcceK / TestCount);
            System.out.printf("MinKCore: %d %5.2f \n", TotalMinKCoreFalse, (double) TotalMinKCoreK / TestCount);
            System.out.printf("MaxKCore: %d %5.2f \n", TotalMaxKCoreFalse, (double) TotalMaxKCoreK / TestCount);
            System.out.printf("MinTria: %d %5.2f \n", TotalMinTriaFalse, (double) TotalMinTriaK / TestCount);
            System.out.printf("MaxTria: %d %5.2f \n", TotalMaxTriaFalse, (double) TotalMaxTriaK / TestCount);
            System.out.printf("MinTwoHop: %d %5.2f \n", TotalMinTwoHopFalse, (double) TotalMinTwoHopK / TestCount);
            System.out.printf("MaxTwoHop: %d %5.2f \n", TotalMaxTwoHopFalse, (double) TotalMaxTwoHopK / TestCount);
            */
            bw.write(",V,GraphT,DLoopT,BellmanT,FloydT,BestT,MinEdCT,MaxEdCT,MinEdWT,MaxEdWT,MiECnMaIn,MaECnMaIn,MiEdWeNeWe,MaEdWeNeWe,MinCoefT,MaxCoefT,MinEigT,MaxEigT,MinEcce,MaxEcce,MinKCore,MaxKCore,MinTria,MaxTria,MinTwoHop,MaxTwoHop,EdgeCnt,Perm,pSkip");
            bw.newLine();
            bw.write(",");
            bw.write(",");
            bw.write(String.format("%.4f", AverageGraphTime));
            bw.write(",");
            bw.write(String.format("%.4f", AverageDijkstraLoopTime));
            bw.write(",");
            bw.write(String.format("%.4f", AverageBellmanFordTime));
            bw.write(",");
            bw.write(String.format("%.4f", AverageFloydWarshallTime));
            bw.write(",");
            bw.write(String.format("%.4f", AverageBestTime));
            bw.write(",");
            bw.write(String.format("%.4f", AverageMinEdgeCntTime));
            bw.write(",");
            bw.write(String.format("%.4f", AverageMaxEdgeCntTime));
            bw.write(",");
            bw.write(String.format("%.4f", AverageMinEdgeWeightTime));
            bw.write(",");
            bw.write(String.format("%.4f", AverageMaxEdgeWeightTime));
            bw.write(",");
            bw.write(String.format("%.4f", AverageMinECntMaxINFTime));
            bw.write(",");
            bw.write(String.format("%.4f", AverageMaxECntMaxINFTime));
            bw.write(",");
            bw.write(String.format("%.4f", AverageMinEdWeiNeiWeiTime));
            bw.write(",");
            bw.write(String.format("%.4f", AverageMaxEdWeiNeiWeiTime));
            bw.write(",");
            bw.write(String.format("%.4f", AverageMinCoefTime));
            bw.write(",");
            bw.write(String.format("%.4f", AverageMaxCoefTime));
            bw.write(",");
            bw.write(String.format("%.4f", AverageMinEigenTime));
            bw.write(",");
            bw.write(String.format("%.4f", AverageMaxEigenTime));
            bw.write(",");
            bw.write(String.format("%.4f", AverageMinEcceTime));
            bw.write(",");
            bw.write(String.format("%.4f", AverageMaxEcceTime));
            bw.write(",");
            bw.write(String.format("%.4f", AverageMinKCoreTime));
            bw.write(",");
            bw.write(String.format("%.4f", AverageMaxKCoreTime));
            bw.write(",");
            bw.write(String.format("%.4f", AverageMinTriaTime));
            bw.write(",");
            bw.write(String.format("%.4f", AverageMaxTriaTime));
            bw.write(",");
            bw.write(String.format("%.4f", AverageMaxTwoHopTime));
            bw.write(",");
            bw.write(String.format("%.4f", AverageMinTwoHopTime));
            bw.write(",");
            bw.write(String.format("%.4f", AverageEdgeCount));
            bw.write(",");
            bw.write(String.format("%.4f", AveragePermLoopCounter));
            bw.write(",");
            bw.write(String.format("%.4f", AveragepSkip));
            bw.newLine();
            bw.newLine();

            bw.write(String.format(",,Permutation:,, %5.2f \n", AveragePermLoopCounter));
            bw.newLine();
            bw.write(String.format(",,MinEdgeCnt:, %d, %5.2f \n", TotalMinEdgeCntFalse, (double) TotalMinEdgeCntK / TestCount));
            bw.write(String.format(",,MaxEdgeCnt:, %d, %5.2f \n", TotalMaxEdgeCntFalse, (double) TotalMaxEdgeCntK / TestCount));
            bw.write(String.format(",,MinEdgeWeight:, %d, %5.2f \n", TotalMinEdgeWeightFalse, (double) TotalMinEdgeWeightK / TestCount));
            bw.write(String.format(",,MaxEdgeWeight:, %d, %5.2f \n", TotalMaxEdgeWeightFalse, (double) TotalMaxEdgeWeightK / TestCount));
            bw.write(String.format(",,MinECntMaxINF:, %d, %5.2f \n", TotalMinECntMaxINFFalse, (double) TotalMinECntMaxINFK / TestCount));
            bw.write(String.format(",,MaxECntMaxINF:, %d, %5.2f \n", TotalMaxECntMaxINFFalse, (double) TotalMaxECntMaxINFK / TestCount));
            bw.write(String.format(",,MinEdWeiNeiWei:, %d, %5.2f \n", TotalMinEdWeiNeiWeiFalse, (double) TotalMinEdWeiNeiWeiK / TestCount));
            bw.write(String.format(",,MaxEdWeiNeiWei:, %d, %5.2f \n", TotalMaxEdWeiNeiWeiFalse, (double) TotalMaxEdWeiNeiWeiK / TestCount));
            bw.write(String.format(",,MinCoef:, %d, %5.2f \n", TotalMinCoefFalse, (double) TotalMinCoefK / TestCount));
            bw.write(String.format(",,MaxCoef:, %d, %5.2f \n", TotalMaxCoefFalse, (double) TotalMaxCoefK / TestCount));
            bw.write(String.format(",,MinEigen:, %d, %5.2f \n", TotalMinEigenFalse, (double) TotalMinEigenK / TestCount));
            bw.write(String.format(",,MaxEigen:, %d, %5.2f \n", TotalMaxEigenFalse, (double) TotalMaxEigenK / TestCount));
            bw.write(String.format(",,MinEcce:, %d, %5.2f \n", TotalMinEcceFalse, (double) TotalMinEcceK / TestCount));
            bw.write(String.format(",,MaxEcce:, %d, %5.2f \n", TotalMaxEcceFalse, (double) TotalMaxEcceK / TestCount));
            bw.write(String.format(",,MinKCore:, %d, %5.2f \n", TotalMinKCoreFalse, (double) TotalMinKCoreK / TestCount));
            bw.write(String.format(",,MaxKCore:, %d, %5.2f \n", TotalMaxKCoreFalse, (double) TotalMaxKCoreK / TestCount));
            bw.write(String.format(",,MinTria:, %d, %5.2f \n", TotalMinTriaFalse, (double) TotalMinTriaK / TestCount));
            bw.write(String.format(",,MaxTria:, %d, %5.2f \n", TotalMaxTriaFalse, (double) TotalMaxTriaK / TestCount));
            bw.write(String.format(",,MinTwoHop:, %d, %5.2f \n", TotalMinTwoHopFalse, (double) TotalMinTwoHopK / TestCount));
            bw.write(String.format(",,MaxTwoHop:, %d, %5.2f \n", TotalMaxTwoHopFalse, (double) TotalMaxTwoHopK / TestCount));
            bw.close();
            pool.shutdown();
            System.out.println("\nFinished");
        }
    }
}

