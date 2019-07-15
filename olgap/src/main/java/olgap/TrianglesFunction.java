package olgap;

import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.algebra.evaluation.ValueExprEvaluationException;
import org.eclipse.rdf4j.query.algebra.evaluation.function.Function;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

public class TrianglesFunction  implements Function{
	public RepositoryConnection conn;
	public Repository workingRep;
	public TrianglesFunction() {
		super();
		workingRep = new SailRepository(new MemoryStore());
	}
	public static final String NAMESPACE = "http://inova8.com/olgap/";
	@Override
	public String getURI() {
		return NAMESPACE + "triangles";
	}
	@Override
	public Value evaluate(ValueFactory valueFactory, Value... args) throws ValueExprEvaluationException {
		//Repository workingRep = new SailRepository(new MemoryStore());
		workingRep.init();
		Repository rep = new HTTPRepository(args[0].stringValue(),args[1].stringValue());//"http://localhost:8082/rdf4j-server/", "tfl");
		rep.init();	
		
		try (RepositoryConnection workingConn = workingRep.getConnection();RepositoryConnection conn = rep.getConnection(); ) {
			
			String graphQueryString = "CONSTRUCT{?vertex <label:connect> ?neighbor.}\r\n" 
					+ "WHERE{\r\n"
					+ "SELECT ?vertex ?neighbor\r\n" 
					+ "WHERE{{?vertex ?p ?neighbor.}\r\n"
					+ "    UNION{?neighbor ?p ?vertex.}FILTER(STR(?vertex) <STR(?neighbor))}\r\n" 
					+ "}";
			GraphQueryResult graphResult = conn.prepareGraphQuery(graphQueryString).evaluate();
			
			Resource graph = workingRep.getValueFactory().createIRI("ng:temp");
			while (graphResult.hasNext()) {
				Statement st = graphResult.next();
				// ... do something with the resulting statement here.
				workingConn.add(st, graph);
			}

			String queryString = "SELECT(COUNT(DISTINCT*) AS ?triangles)\r\n" 
					+ "WHERE{\r\n" 
					+ "  {\r\n" + "GRAPH<ng:temp>   {\r\n"
					+ "      ?x?p?y.\r\n" 
					+ "      ?y?p?z.\r\n" 
					+ "      ?x?p?z.\r\n" 
					+ "    }\r\n" 
					+ "  }\r\n" 
					+ "}";

			TupleQuery query = workingConn.prepareTupleQuery(queryString);

			try (TupleQueryResult result = query.evaluate()) {
				// we just iterate over all solutions in the result...
				while (result.hasNext()) {
					BindingSet solution = result.next();
					return valueFactory.createLiteral(solution.getValue("triangles").toString());

				}
			}
		}
		return null;

	}

}
