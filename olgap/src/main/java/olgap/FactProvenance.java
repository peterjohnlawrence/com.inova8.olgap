/*
 * inova8 2020
 */
package olgap;

import java.util.Arrays;
import java.util.HashMap;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.query.algebra.evaluation.ValueExprEvaluationException;
import org.eclipse.rdf4j.query.algebra.evaluation.function.Function;

import pathCalc.EvaluationContext;
import pathCalc.Evaluator;
import pathCalc.Thing;
import pathPatternElement.PredicateElement;
import pathQLRepository.PathQLRepository;

import org.eclipse.rdf4j.query.algebra.evaluation.TripleSource;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.ParameterizedMessage;
import org.apache.logging.log4j.LogManager;

// TODO: Auto-generated Javadoc
/**
 * The Class FactProvenance.
 */
public class FactProvenance extends Evaluator implements Function {
	
	/** The logger. */
	private final Logger logger = LogManager.getLogger(FactProvenance.class);

	/**
	 * Instantiates a new fact provenance.
	 */
	public FactProvenance()  {
		super();
		logger.info(new ParameterizedMessage("Initiating FactProvenance"));
	}
	
	/**
	 * Gets the uri.
	 *
	 * @return the uri
	 */
	@Override
	public String getURI() {
		return  OLGAPNAMESPACE + "factProvenance";
	}

	/**
	 * Evaluate.
	 * 
	 * Returns the calculation trace (if calculated) of the value of the predicate of the subject
	 *
	 * @param tripleSource the triple source
	 * @param args the args, args[0] subject, args[1] predicate 
	 * @return the value
	 * @throws ValueExprEvaluationException the value expr evaluation exception
	 */
	@Override
	public Value evaluate(TripleSource tripleSource, Value... args) throws ValueExprEvaluationException {
		logger.debug(new ParameterizedMessage("Trace Evaluate for <{}>, {} with args <{}>",tripleSource, tripleSource.getValueFactory(),args));
		if(args.length <2) {
			ParameterizedMessage message = new ParameterizedMessage("At least subject, and predicate arguments required");
			logger.error(message);
			return tripleSource.getValueFactory().createLiteral(message.toString());
		}else {

			IRI subject ;
			IRI predicate;
			try {
				subject = (IRI) args[0];
				predicate = (IRI) args[1];
			} catch(Exception e) {
				ParameterizedMessage message = new ParameterizedMessage("Subject and predicate must be valid IRI");
				logger.error(message);
				return tripleSource.getValueFactory().createLiteral(message.toString());
			}
			try{
				Value[] argumentArray = Arrays.copyOfRange(args,2, args.length);
				PathQLRepository source = sources.getSource(tripleSource, argumentArray );
				HashMap<String, pathQLModel.Resource> customQueryOptions = source.getCustomQueryOptions(argumentArray);
				EvaluationContext evaluationContext = new EvaluationContext(customQueryOptions);
				evaluationContext.setTracing(true);
				Thing subjectThing = Thing.create(source, subject, evaluationContext);
				//olgap.Value fact = 
				subjectThing.getFact( new PredicateElement(source,predicate));
				logger.debug("Trace\r\n"+evaluationContext.getTrace());
				return tripleSource.getValueFactory().createLiteral(evaluationContext.getTrace());		

			}catch(Exception e) {
				return tripleSource.getValueFactory().createLiteral(e.getMessage());
			}
		}
	}

	/**
	 * Evaluate.
	 *
	 * @param valueFactory the value factory
	 * @param args the args
	 * @return the value
	 * @throws ValueExprEvaluationException the value expr evaluation exception
	 */
	@Override
	public Value evaluate(ValueFactory valueFactory, Value... args) throws ValueExprEvaluationException {
		return null;
	}

}
