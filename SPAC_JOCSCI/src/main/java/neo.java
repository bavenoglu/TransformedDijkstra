import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Query;

public class neo {
    private Driver driver;

    public void connect() {
        driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j", "12345678"));
    }

    public void deleteAll() {
        try (var session = driver.session()) {
            var greeting = session.executeWrite(tx -> {
                //var query = new Query("CREATE (a:Greeting) SET a.message = $message RETURN a.message + ', from node ' + id(a)", parameters("message", message));
                var query = new Query("MATCH (n) DETACH DELETE n");
                var result = tx.run(query);
                return true; //result.single().get(0).asString();
            });
            //System.out.println("All objects deleted!");
        }
    }
    public void createNode(String nodeName) {
        try (var session = driver.session()) {
            var greeting = session.executeWrite(tx -> {
                var query = new Query("CREATE (node:" + nodeName + " {name: \"" + nodeName + "\"})");
                var result = tx.run(query);
                return true; //result.single().get(0).asString();
            });
        }
    }
    public void createRelationship(String firstNode, String secondNode, String weigth, String label) {
        try (var session = driver.session()) {
            var greeting = session.executeWrite(tx -> {
                var query = new Query("MATCH (a:" + firstNode + "),(b:" + secondNode + ") WHERE a.name=\"" +
                           firstNode + "\" AND b.name=\"" + secondNode +"\"" +
                           " CREATE (a)-[r:w" + weigth + " {Weigth:'"+ label +"'}]->(b) RETURN a,b" );
                //System.out.println(query);
                var result = tx.run(query);
                return true; //result.single().get(0).asString();
            });
        }
        catch (Exception e) {
            System.out.println("error");
        }
    }

}
