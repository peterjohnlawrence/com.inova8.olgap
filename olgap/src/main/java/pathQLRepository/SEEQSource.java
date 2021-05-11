/*
 * inova8 2020
 */
package pathQLRepository;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.seeq.ApiClient;
import com.seeq.ApiException;
import com.seeq.api.AuthApi;
import com.seeq.api.FormulasApi;
import com.seeq.api.SignalsApi;
import com.seeq.model.AuthInputV1;
import com.seeq.model.FormulaRunOutputV1;
import com.seeq.model.GetSampleOutputV1;

import Exceptions.HandledException;
import pathQLModel.Resource;

/**
 * The Class SEEQSource.
 */
public class SEEQSource {
	
	/** The Constant AGGREGATE. */
	private static final String AGGREGATE = "aggregate";
	
	/** The Constant START. */
	private static final String START = "start";
	
	/** The Constant END. */
	private static final String END = "end";
	
	/** The Constant TOTALIZED. */
	private static final String TOTALIZED = "Totalized";
	
	/** The Constant MINIMUM. */
	private static final String MINIMUM = "Minimum";
	
	/** The Constant MAXIMUM. */
	private static final String MAXIMUM = "Maximum";
	
	/** The Constant AVERAGE. */
	private static final String AVERAGE = "Average";
	
	/** The Constant INSTANT. */
	private static final String INSTANT = "Instant";
	
	/** The Constant logger. */
	static private final Logger logger = LogManager.getLogger(SEEQSource.class);
	
	/** The Constant INVALIDAGGREGATE_EXCEPTION. */
	private static final String INVALIDAGGREGATE_EXCEPTION = "**Invalid Aggregate**";
	
	/** The signals api. */
	static SignalsApi signalsApi;
	
	/** The formulas api. */
	private FormulasApi formulasApi;

	/**
	 * Gets the signals api.
	 *
	 * @return the signals api
	 */
	public static SignalsApi getSignalsApi() {
		return signalsApi;
	}

	/**
	 * Gets the formulas API.
	 *
	 * @return the formulas API
	 */
	public FormulasApi getFormulasAPI() {
		return formulasApi;
	}

	/**
	 * Instantiates a new SEEQ source.
	 *
	 * @param basePath the base path
	 * @param userName the user name
	 * @param password the password
	 */
	public SEEQSource(String basePath, String userName, String password) {
		ApiClient apiClient = new ApiClient();
		apiClient.setBasePath(basePath);
		AuthApi authApi = new AuthApi(apiClient);
		AuthInputV1 input = new AuthInputV1();
		input.setUsername("peter.lawrence@inova8.com");
		input.setPassword("lusterthief");
		authApi.login(input);
		signalsApi = new SignalsApi(apiClient);
		formulasApi = new FormulasApi(apiClient);
		logger.debug("Connection created at: {}",basePath);
	}

	/**
	 * Gets the signal.
	 *
	 * @param signal the signal
	 * @param customQueryOptions the custom query options
	 * @return the signal
	 * @throws HandledException the handled exception
	 */
	public Object getSignal(String signal, HashMap<String, pathQLModel.Resource> customQueryOptions) throws HandledException {
		String start = getStart(customQueryOptions);
		String end = getEnd(customQueryOptions);
		String aggregate = getAggregate(customQueryOptions);
		String formula;
		switch (aggregate) {
		case INSTANT:
			GetSampleOutputV1 signalValue = signalsApi.getSample(signal, end, null, null, null);
			return signalValue.getSample().getValue();
		case AVERAGE:
			formula = "$tx01.average(capsule ('" + start + "','" + end + "')) ";
			return executeFunction(signal, formula);
		case MAXIMUM:
			formula = "$tx01.maxValue(capsule ('" + start + "','" + end + "')) ";
			return executeFunction(signal, formula);
		case MINIMUM:
			formula = "$tx01.minValue(capsule ('" + start + "','" + end + "')) ";
			return executeFunction(signal, formula);
		case TOTALIZED:
			formula = "$tx01.totalized(capsule ('" + start + "','" + end + "')) ";
			return executeFunction(signal, formula);
		default:
			logger.error("SEEQ Invalid aggregate: {}",aggregate);
			throw new HandledException(INVALIDAGGREGATE_EXCEPTION,aggregate);
		}
		//return (olgap.Value) null;
	}

	/**
	 * Execute function.
	 *
	 * @param signal the signal
	 * @param formula the formula
	 * @return the object
	 * @throws ApiException the api exception
	 */
	private Object executeFunction(String signal, String formula) throws ApiException {
		String function = null;
		List<String> parameters = new ArrayList<String>();
		parameters.add("tx01=" + signal);
		List<String> fragments = null;
		Integer offset = null;
		Integer limit = null;
		FormulaRunOutputV1 formulaOutput = formulasApi.runFormula(null, null, formula, function, parameters,
				fragments, offset, limit);
		return formulaOutput.getScalar().getValue();
	}

	/**
	 * Gets the start.
	 *
	 * @param customQueryOptions the custom query options
	 * @return the start
	 */
	private String getStart(HashMap<String, pathQLModel.Resource> customQueryOptions) {
		if (customQueryOptions!=null && customQueryOptions.containsKey(START)) {
			Resource startDateTime = customQueryOptions.get(START);
			return startDateTime.getValue().stringValue();
		} else {
			return DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(ZonedDateTime.now().truncatedTo(ChronoUnit.HOURS).minusDays(1));// LocalDateTime.now().minusDays(1).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
		}
	}

	/**
	 * Gets the end.
	 *
	 * @param customQueryOptions the custom query options
	 * @return the end
	 */
	private String getEnd(HashMap<String, pathQLModel.Resource> customQueryOptions) {
		if (customQueryOptions!=null && customQueryOptions.containsKey(END)) {
			Resource endDateTime = customQueryOptions.get(END);
			return endDateTime.getValue().stringValue();
		} else {
			if(customQueryOptions!=null &&  customQueryOptions.containsKey(START) ) {
				Resource startDateTime = customQueryOptions.get(START);
				String start = startDateTime.getValue().stringValue();
				return start;
			}else {
				return  DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(ZonedDateTime.now().truncatedTo(ChronoUnit.HOURS));//   LocalDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);	
			}
		}		
	}

	/**
	 * Gets the aggregate.
	 *
	 * @param customQueryOptions the custom query options
	 * @return the aggregate
	 */
	private String getAggregate(HashMap<String, pathQLModel.Resource> customQueryOptions) {
		if (customQueryOptions!=null && customQueryOptions.containsKey(AGGREGATE)) {
			return customQueryOptions.get(AGGREGATE).getValue().stringValue();
		} else {
			return INSTANT;
		}
	}
}
