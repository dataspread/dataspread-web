package org.zkoss.poi.ss.formula.functions;


import org.model.DBHandler;
import org.zkoss.poi.ss.formula.eval.*;

import java.sql.*;

public class SqlFunction extends Fixed1ArgFunction {

    @Override
    public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval arg0) {
        ValueEval evaluatedQueryArg = evaluateQueryArg(arg0, srcRowIndex, srcColumnIndex);

        if (evaluatedQueryArg instanceof StringEval) {
            try {
                String q = ((StringEval) evaluatedQueryArg).getStringValue();
                return new StringEval(query(q));
            } catch (SQLException e) {
                e.printStackTrace();
            }

        }
        if (evaluatedQueryArg == BlankEval.instance) {
            return null;
        }
        throw new RuntimeException("Unexpected type for query ("
                + evaluatedQueryArg.getClass().getName() + ")");
    }

    private String hackCompose(ResultSet rs) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        int numCols = metaData.getColumnCount();
        StringBuilder builder = new StringBuilder();
        while (rs.next()) {
            for (int i = 1; i <= numCols; i++) {
                builder.append(rs.getString(i));
                builder.append(",");
            }
            builder.append(";");
        }
        return builder.toString();
    }

    private String query(String sql) throws SQLException {

        Connection connection = DBHandler.instance.getConnection();
        Statement stmt = connection.createStatement();

        ResultSet queryResult = stmt.executeQuery(sql);
        String resultString = hackCompose(queryResult);
        connection.commit();
        connection.close();
        return resultString;
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

}
