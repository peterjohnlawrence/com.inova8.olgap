package PathPattern;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

public class PathPatternRunner {
	public static void main( String[] args) throws Exception 
	{

		ANTLRInputStream input = new ANTLRInputStream( System.in);

		PathPatternLexer lexer = new PathPatternLexer(input);

		CommonTokenStream tokens = new CommonTokenStream(lexer);

		PathPatternParser parser = new PathPatternParser(tokens);
		ParseTree tree = parser.graphPattern(); 
		System.out.println(tree.toStringTree(parser)); 
	}

}
