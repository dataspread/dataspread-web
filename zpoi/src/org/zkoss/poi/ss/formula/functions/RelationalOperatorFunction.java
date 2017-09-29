package org.zkoss.poi.ss.formula.functions;

import org.zkoss.poi.ss.formula.OperationEvaluationContext;
import org.zkoss.poi.ss.formula.TwoDEval;
import org.zkoss.poi.ss.formula.WorkbookEvaluator;
import org.zkoss.poi.ss.formula.eval.*;
import org.zkoss.poi.ss.formula.ptg.Ptg;

import java.util.*;

import static org.zkoss.poi.ss.formula.functions.CountUtils.I_MatchPredicate;

/**
 * Abstract class which holds all of the relational operator functions.
 * Functions to be implemented here are:
 * Union, set difference, intersection, cartesian product, select, join, project, and rename.
 * Created by Danny on 9/22/2016.
 * Heavily modified by Tana.
 */
public abstract class RelationalOperatorFunction implements Function {


	private static final class RowEntry {
		private final Row _row;
		private short _mask;

		public RowEntry(Row row) {
			_row = row;
			_mask = 0;
		}

		public Row getRow() {
			return _row;
		}

		public short getMask() {
			return _mask;
		}

		public void addMask(short mask) {
			_mask |= mask;
		}
	}

	private static final class UniqueRowsContainer {

		private Map<Row, Integer> locator;
		private List<RowEntry> rowEntries;

		public UniqueRowsContainer() {
			locator = new HashMap<>();
			rowEntries = new ArrayList<>();
		}

		public void addRowsWithMask(List<Row> rows, short listMask) {
			for (Row row : rows) {
				int id = rowEntries.size();
				Integer result = locator.putIfAbsent(row, id);
				if (result == null) {
					rowEntries.add(new RowEntry(row));
					result = id;
				}
				rowEntries.get(result).addMask(listMask);
			}
		}

		public List<RowEntry> getRowEntries() {
			return rowEntries;
		}

	}

	private abstract static class MatchingSchemaFunction extends RelTable2ArgFunction {

		protected static short PRESENT1_MASK = 1;
		protected static short PRESENT2_MASK = 2;

		@Override
		protected final ValueEval evaluate(RelTableEval range1, RelTableEval range2, int srcRowIndex, int srcColumnIndex) {
			try {
				validateMatchingSchema(range1, range2);
				List<Row> rows1 = Row.getRowsFromArea(range1);
				List<Row> rows2 = Row.getRowsFromArea(range2);

				UniqueRowsContainer container = new UniqueRowsContainer();

				container.addRowsWithMask(rows1, PRESENT1_MASK);
				container.addRowsWithMask(rows2, PRESENT2_MASK);

				List<Row> resultRows = new ArrayList<>();
				List<RowEntry> rowEntries = container.getRowEntries();
				for (RowEntry rowEntry : rowEntries) {
					if (match(rowEntry.getMask())) {
						resultRows.add(rowEntry.getRow());
					}
				}
				return Row.getRelTableEval(resultRows, range1.getAttributes());
			} catch (EvaluationException e) {
				return e.getErrorEval();
			}
		}

		/**
		 * @param mask
		 * @return true if a row associated with this mask should be kept.
		 */
		protected abstract boolean match(short mask);

	}

	public static final Function UNION = new MatchingSchemaFunction() {

		@Override
		protected boolean match(short mask) {
			return true;
		}

	};

	public static final Function DIFFERENCE = new MatchingSchemaFunction() {

		@Override
		protected boolean match(short mask) {
			return (mask & PRESENT2_MASK) == 0;
		}

	};

	public static final Function INTERSECTION = new MatchingSchemaFunction() {

		@Override
		protected boolean match(short mask) {
			return (mask & PRESENT1_MASK) == PRESENT1_MASK &&
					(mask & PRESENT2_MASK) == PRESENT2_MASK;
		}

	};

	private static final int[] getAttrMapping(String[] attrs1, String[] attrs2) {
		Map<String, Integer> locator = new HashMap<>();
		int[] attrMapping = new int[attrs2.length];
		for (int i = 0; i < attrs1.length; i++) {
			locator.put(attrs1[i], i);
		}
		for (int i = 0; i < attrs2.length; i++) {
			attrMapping[i] = locator.getOrDefault(attrs2[i], -1);
		}
		return attrMapping;
	}

	private static final RelTableEval doCrossProduct(RelTableEval range1, RelTableEval range2, boolean doNaturalJoin) throws EvaluationException {
		String[] attrs1 = range1.getAttributes();
		String[] attrs2 = range2.getAttributes();
		int[] attrMapping = getAttrMapping(attrs1, attrs2);
		List<String> attrsNewList = new ArrayList<>();
		for (int i = 0; i < attrs2.length; i++) {
			if (attrMapping[i] == -1) {
				attrsNewList.add(attrs2[i]);
			} else if (!doNaturalJoin) {
				throw EvaluationException.invalidValue();
			}
		}

		int nColumns1 = attrs1.length;
		int nColumnsN = attrsNewList.size();
		String[] resultAttributes = new String[nColumns1+nColumnsN];
		String[] attrsN = attrsNewList.toArray(new String[0]);
		System.arraycopy(attrs1, 0, resultAttributes, 0, nColumns1);
		System.arraycopy(attrsN, 0, resultAttributes, nColumns1, nColumnsN);

		List<Row> rows1 = Row.getRowsFromArea(range1);
		List<Row> rows2 = Row.getRowsFromArea(range2);
		List<Row> resultRows = new ArrayList<>();

		for (Row row1 : rows1) {
			for (Row row2 : rows2) {
				Row combinedRow = Row.combineRows(row1, row2, attrMapping);
				if (combinedRow != null) {
					resultRows.add(combinedRow);
				}
			}
		}
		return Row.getRelTableEval(resultRows, resultAttributes);
	}

	private static final RelTableEval doSelect(RelTableEval range, FilterHelperEval helper) {
		WorkbookEvaluator evaluator = helper._evaluator;
		OperationEvaluationContext ec = helper._ec;
		Ptg[] ptgs = helper._ptgs;
		boolean ignoreDereference = helper._ignoreDereference;

		int nRows = range.getHeight();
		List<ValueEval> evalList = new ArrayList<>();
		for (int i = 0; i < nRows; i++) {
			ValueEval result = evaluator.evaluateFormula(ec, ptgs, true, ignoreDereference, range.getRow(i));
			evalList.add(result);
		}
		List<Row> rows = Row.getRowsFromArea(range, evalList);
		return Row.getRelTableEval(rows, range.getAttributes());
	}

	public static final Function CROSSPRODUCT = new RelTable2ArgFunction() {

		@Override
		protected ValueEval evaluate(RelTableEval range1, RelTableEval range2, int srcRowIndex, int srcColumnIndex) {
			try {
				return doCrossProduct(range1, range2, false);
			} catch (EvaluationException e) {
				return e.getErrorEval();
			}
		}

	};

	public static final Function SELECT = new Var1or2ArgFunction() {

		@Override
		public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval arg0) {
			try {
				return RelTableUtils.getRelTableArg(arg0);
			} catch (EvaluationException e) {
				return e.getErrorEval();
			}
		}

		@Override
		public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval arg0, ValueEval arg1) {
			try {
				RelTableEval table = RelTableUtils.getRelTableArg(arg0);
				if (arg1 instanceof FilterHelperEval) {
					FilterHelperEval helper = (FilterHelperEval) arg1;
					return doSelect(table, helper);
				} else {
					throw EvaluationException.invalidValue();
				}
			} catch (EvaluationException e) {
				return e.getErrorEval();
			}
		}

	};


	public static final Function PROJECT = new RelTableSchemaFunction() {

		@Override
		protected ValueEval evaluate(RelTableEval range, String[] attributes, int srcRowIndex, int srcColumnIndex) {
			List<Integer> attributeIndicesList = new ArrayList<>();
			for (String attribute : attributes) {
				int index = range.indexOfAttribute(attribute);
				if (index != -1) {
					attributeIndicesList.add(index);
				}
			}
			int[] attributeIndices = attributeIndicesList.stream().mapToInt(i->i).toArray();
			RelTableEval rangeNew = range.getColumns(attributeIndices);

			// Must collapse. Project can produce duplicates.
			List<Row> rows = Row.getRowsFromArea(rangeNew);
			UniqueRowsContainer container = new UniqueRowsContainer();
			container.addRowsWithMask(rows, (short) 0);

			List<Row> resultRows = new ArrayList<>();
			List<RowEntry> rowEntries = container.getRowEntries();
			for (RowEntry rowEntry : rowEntries) {
				resultRows.add(rowEntry.getRow());
			}
			return Row.getRelTableEval(resultRows, rangeNew.getAttributes());
		}

	};


	public static final Function RENAME = new RelTableSchemaFunction() {

		@Override
		protected ValueEval evaluate(RelTableEval range, String[] attributes, int srcRowIndex, int srcColumnIndex) {
			if (attributes.length != 2) {
				return ErrorEval.VALUE_INVALID;
			}
			String[] tgtAttributes = range.getAttributes();
			int index = range.indexOfAttribute(attributes[0]);
			if (index != -1) {
				tgtAttributes[index] = attributes[1];
			}
			return range.rename(tgtAttributes);
		}

	};


	public static final Function JOIN = new Var2or3ArgFunction() {

		@Override
		public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval arg0, ValueEval arg1) {
			try {
				RelTableEval table1 = RelTableUtils.getRelTableArg(arg0);
				RelTableEval table2 = RelTableUtils.getRelTableArg(arg1);
				return doCrossProduct(table1, table2, true);
			} catch (EvaluationException e) {
				return e.getErrorEval();
			}
		}

		@Override
		public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval arg0, ValueEval arg1, ValueEval arg2) {
			try {
				RelTableEval table1 = RelTableUtils.getRelTableArg(arg0);
				RelTableEval table2 = RelTableUtils.getRelTableArg(arg1);
				if (arg2 instanceof FilterHelperEval) {
					FilterHelperEval helper = (FilterHelperEval) arg2;
					RelTableEval tableTmp = doCrossProduct(table1, table2, false);
					return doSelect(tableTmp, helper);
				} else {
					throw EvaluationException.invalidValue();
				}
			} catch (EvaluationException e) {
				return e.getErrorEval();
			}
		}

	};


	/**
	 * Method to check that the two ranges have matching schema.
	 * @param range1
	 * @param range2
	 * @throws EvaluationException
	 */
	private static void validateMatchingSchema(RelTableEval range1, RelTableEval range2) throws EvaluationException {
		//TODO: Compare schemas
		if (range1.getWidth() != range2.getWidth()) {
			throw EvaluationException.invalidValue();
		}
	}


	/**
	 * Private class used to represent a row (array of ValueEval's)
	 */
	private static class Row {

		private final ValueEval[] _values;

		public Row(ValueEval[] values) {
			_values = values;
		}

		public ValueEval getValue(int col) {
			return _values[col];
		}

		public int getLength() {
			return _values.length;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof Row) {
				Row row1 = this;
				Row row2 = (Row) obj;
				if (row1.getLength() != row2.getLength()) {
					return false;
				}
				for (int i = 0; i < row1.getLength(); i++) {
					ValueEval eval1 = row1.getValue(i);
					ValueEval eval2 = row2.getValue(i);
					if (!compareEvals(eval1, eval2)) {
						return false;
					}
				}
				return true;
			}
			return false;
		}

		@Override
		public int hashCode() {
			int hashCode = 1;
			for (ValueEval e : _values) {
				hashCode = 31 * 31 * hashCode + (e == null ? 0 : makeshiftHashcode(e));
			}
			return hashCode;
		}

		private int makeshiftHashcode(ValueEval e) {
			if (e instanceof NumberEval) {
				return 0 * 31 + ((Double) ((NumberEval) e).getNumberValue()).hashCode();
			} else if (e instanceof StringEval) {
				return 1 * 31 + ((StringEval) e).getStringValue().hashCode();
			} else if (e instanceof BoolEval) {
				return 4 * 31 + ((Boolean) ((BoolEval) e).getBooleanValue()).hashCode();
			}
			return 0;
		}

		private static boolean compareEvals(ValueEval e1, ValueEval e2) {
			if (e1 instanceof NumberEval && e2 instanceof NumberEval) {
				double val1 = ((NumberEval) e1).getNumberValue();
				double val2 = ((NumberEval) e2).getNumberValue();
				return val1 == val2;
			} else if (e1 instanceof StringEval && e2 instanceof StringEval) {
				String val1 = ((StringEval) e1).getStringValue();
				String val2 = ((StringEval) e2).getStringValue();
				return val1.equals(val2);
			} else if (e1 instanceof BoolEval && e2 instanceof BoolEval) {
				boolean val1 = ((BoolEval) e1).getBooleanValue();
				boolean val2 = ((BoolEval) e2).getBooleanValue();
				return val1 == val2;
			}
			return false;
		}

		/**
		 * Static method to combine row1 (a1, ..., an) and row2 (b1, ... , bn) into row3 (a1, ... , an, b1, ... , bn)
		 * @param row1
		 * @param row2
		 * @return
		 */
		public static Row combineRows(Row row1, Row row2, int[] attrMapping) {
			int nColumns1 = row1.getLength();
			int nColumns2 = row2.getLength();
			List<ValueEval> combinedRow = new ArrayList<>();
			for (int i = 0; i < nColumns1; i++) {
				combinedRow.add(row1.getValue(i));
			}

			for (int i = 0; i < nColumns2; i++) {
				if (attrMapping[i] == -1) {
					combinedRow.add(row2.getValue(i));
				} else if (!compareEvals(row1.getValue(attrMapping[i]), row2.getValue(i))) {
					return null;
				}
			}

			return new Row(combinedRow.toArray(new ValueEval[0]));
		}


		public static RelTableEval getRelTableEval(List<Row> rows, String[] schema) {
			int nRows = rows.size();
			int nColumns = schema.length;
			ValueEval[][] values = new ValueEval[nRows][];

			for (int r = 0; r < nRows; r++) {
				Row row = rows.get(r);
				values[r] = new ValueEval[nColumns];
				for (int c = 0; c < nColumns; c++) {
					values[r][c] = row.getValue(c);
				}
			}

			return new RelTableEval(values, schema, nRows, nColumns);
		}


		/**
		 * Static method to create an array of Row's from an AreaEval
		 * @param range
		 * @return
		 */
		public static List<Row> getRowsFromArea(RelTableEval range) {
			List<Row> rows = new ArrayList<>();
			for (int r = 0; r < range.getHeight(); r++) {
				//get all of the ValueEval's for this row
				TwoDEval row = range.getRow(r);
				ValueEval[] rowEvals = new ValueEval[row.getWidth()];
				for (int c = 0; c < row.getWidth(); c++) {
					rowEvals[c] = row.getValue(0, c);
				}
				rows.add(new Row(rowEvals));
			}
			return rows;
		}

		/**
		 * Static method to create an array of Row's from an AreaEval
		 * @param range
		 * @return
		 */
		public static List<Row> getRowsFromArea(RelTableEval range, List<ValueEval> conds) {
			List<Row> rows = new ArrayList<>();
			for (int r = 0; r < range.getHeight(); r++) {
				ValueEval cond = conds.get(r);
				if (cond instanceof BoolEval &&
						((BoolEval) cond).getBooleanValue()) {
					//get all of the ValueEval's for this row
					TwoDEval row = range.getRow(r);
					ValueEval[] rowEvals = new ValueEval[row.getWidth()];
					for (int c = 0; c < row.getWidth(); c++) {
						rowEvals[c] = row.getValue(0, c);
					}
					rows.add(new Row(rowEvals));
				}
			}
			return rows;
		}

	}//end Row class

}
