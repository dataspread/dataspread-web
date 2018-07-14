package org.zkoss.poi.ss.formula.functions;


import org.model.AutoRollbackConnection;
import org.model.DBHandler;
import org.zkoss.poi.ss.formula.eval.*;

import java.sql.*;
import java.util.ArrayList;

public class SqlFunction implements Function {

	@Override
	public ValueEval evaluate(ValueEval[] args, int srcRowIndex, int srcColumnIndex) {
		int nArgs = args.length;
		if (nArgs < 1) {
			// too few arguments
			return ErrorEval.VALUE_INVALID;
		}

		if (nArgs > 30) {
			// too many arguments
			return ErrorEval.VALUE_INVALID;
		}

		ValueEval evaluatedQuery = evaluateQueryArg(args[0], srcRowIndex, srcColumnIndex);
		ValueEval[] evaluatedParameters = evaluateParameterArgs(args, srcRowIndex, srcColumnIndex);


		if (evaluatedQuery instanceof StringEval) {
			String queryString = ((StringEval) evaluatedQuery).getStringValue();
			return makeQuery(queryString, evaluatedParameters);
		}
		throw new RuntimeException("Unexpected type for query ("
				+ evaluatedQuery.getClass().getName() + ")");
	}

	private RelTableEval parseResult(ResultSet rs) throws SQLException {
		ResultSetMetaData metaData = rs.getMetaData();
		int numCols = metaData.getColumnCount();

		String[] attributes = new String[numCols];
		for (int i = 0; i < numCols; i++) {
			attributes[i] = metaData.getColumnName(i+1);
		}

		ArrayList<StringEval[]> rows = new ArrayList<>();

		while (rs.next()) {
			StringEval[] row = new StringEval[numCols];
			for (int i = 0; i < numCols; i++) {
				row[i] = new StringEval(rs.getString(i+1));
			}
			rows.add(row);
		}

		int numRows = rows.size();

		StringEval[][] evalSourceArray = new StringEval[rows.size()][];
		for (int i = 0; i < numRows; i++) {
			evalSourceArray[i] = rows.get(i);
		}

		RelTableEval eval = new RelTableEval(evalSourceArray, attributes, numRows, numCols);

		return eval;
	}

	private ValueEval makeQuery(String queryString, ValueEval[] parameters) {

		ValueEval resultEval;

		try (AutoRollbackConnection connection = DBHandler.instance.getConnection();
			 PreparedStatement stmt = connection.prepareStatement(queryString)) {
			for (int i = 0; i < parameters.length; i++) {
				if (parameters[i] instanceof StringEval) {
					String paramString = ((StringEval) parameters[i]).getStringValue();
					stmt.setString(i+1, paramString);
				}
			}

			ResultSet result = stmt.executeQuery();
			resultEval = parseResult(result);
			connection.commit();
		} catch (SQLException e) {
			resultEval = ErrorEval.VALUE_INVALID;
		}
		return resultEval;

	}

	/**
	 *
	 * @return the de-referenced query arg (possibly {@link ErrorEval})
	 */
	private static ValueEval evaluateQueryArg(ValueEval arg, int srcRowIndex, int srcColumnIndex) {
		try {
			return OperandResolver.getSingleValue(arg, srcRowIndex, (short)srcColumnIndex);
		} catch (EvaluationException e) {
			return e.getErrorEval();
		}
	}

	/**
	 *
	 * @return the de-referenced parameter arg (possibly {@link ErrorEval})
	 */
	private static ValueEval[] evaluateParameterArgs(ValueEval[] args, int srcRowIndex, int srcColumnIndex) {
		ValueEval[] evaluatedArgs = new ValueEval[args.length-1];
		for (int i = 1; i < args.length; i++) {
			try {
				evaluatedArgs[i-1] = OperandResolver.getSingleValue(args[i], srcRowIndex, (short)srcColumnIndex);
			} catch (EvaluationException e) {
				evaluatedArgs[i-1] = e.getErrorEval();
			}
		}
		return evaluatedArgs;
	}


}
