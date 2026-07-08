import java.util.Vector;
public class perm {
    Graph g;
    Vector<Integer> tmp = new Vector<Integer>();
    Vector<Integer> realList = new Vector<Integer>();
    int[] correctPerm;
    boolean letExit = false;
    perm(Graph g){
        this.g = g;
        correctPerm = new int[g.V];
    }
    void makeCombiUtil(int n, int left, int k) {
        if (k == 0) {
            for(int i = 0; i < tmp.size(); i++)
            {
                //this.g.Dijkstra(tmp.get(i));
                //System.out.print(tmp);System.out.println(tmp.get(i));
                //System.out.print(tmp.get(i) + " ");
                if(g.dijkstraArrayCount == 0){
                    correctPerm[tmp.size()]++;
                    //letExit = true;
                    realList = new Vector<>(tmp);
                    //System.out.print(tmp); //to print the list of permutations open this
                    break;
                }
            }
            g.resetDijkstra();
            return;
        }
        if(!letExit)
            for (int i = left; i <= n; ++i) {
                tmp.add(i);
                if(!letExit)
                    makeCombiUtil(n, i + 1, k - 1);
                // Popping out last inserted element
                // from the vector
                tmp.remove(tmp.size() - 1);
            }
    }
    void makeCombi(int n, int k) {
        makeCombiUtil(n-1, 0, k);
    }
    public void generatePerm()
    {
        g.resetDijkstra();
        for(int i = 0; i < g.V; i++){
            if(letExit)
                break;
           makeCombi(g.V, i);
        }
    }
}
