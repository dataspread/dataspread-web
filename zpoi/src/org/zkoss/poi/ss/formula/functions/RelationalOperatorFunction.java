package org.zkoss.poi.ss.formula.functions;

import org.zkoss.poi.ss.formula.OperationEvaluationContext;
import org.zkoss.poi.ss.formula.TwoDEval;
import org.zkoss.poi.ss.formula.WorkbookEvaluator;
import org.zkoss.poi.ss.formula.eval.*;
import org.zkoss.poi.ss.formula.ptg.Ptg;

import java.lang.reflect.Array;
import java.util.*;

import static org.zkoss.poi.ss.formula.functions.CountUtils.I_MatchPredicate;

/**
 * Abstract class which holds all of the relational operator functions.
 * Functions to be implemented here are:
 * Union, set difference, intersection, cartesian product, select, join, project, and rename.
 * Created by Danny on 9/22/2016.
 */
public abstract class RelationalOperatorFunction implements Function {


	private abstract static class MatchingSchemaFunction extends RelTable2ArgFunction {

		protected static short PRESENT1_MASK = 1;
		protected static short PRESENT2_MASK = 2;

		private final class RowEntry {
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

		private final void populateRowsAll(List<Row> rows, short listMask, Map<Row, Integer> locator, List<RowEntry> rowEntries) {
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

		@Override
		protected final ValueEval evaluate(RelTableEval range1, RelTableEval range2, int srcRowIndex, int srcColumnIndex) {
			try {
				validateMatchingSchema(range1, range2);
				List<Row> rows1 = Row.getRowsFromArea(range1);
				List<Row> rows2 = Row.getRowsFromArea(range2);
				Map<Row, Integer> locator = new HashMap<>();
				List<RowEntry> rowEntries = new ArrayList<>();
				populateRowsAll(rows1, PRESENT1_MASK, locator, rowEntries);
				populateRowsAll(rows2, PRESENT2_MASK, locator, rowEntries);

				List<Row> resultRows = new ArrayList<>();
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


	public static final Function CROSSPRODUCT = new RelTable2ArgFunction() {

		@Override
		protected ValueEval evaluate(RelTableEval range1, RelTableEval range2, int srcRowIndex, int srcColumnIndex) {
			List<Row> rows1 = Row.getRowsFromArea(range1);
			List<Row> rows2 = Row.getRowsFromArea(range2);
			List<Row> resultRows = new ArrayList<>();

			for (Row aRows1 : rows1) {
				for (Row aRows2 : rows2) {
					resultRows.add(Row.combineRows(aRows1, aRows2));
				}
			}

			int nColumns1 = range1.getWidth();
			int nColumns2 = range2.getWidth();
			String[] resultAttributes = new String[nColumns1+nColumns2];
			System.arraycopy(range1.getAttributes(), 0, resultAttributes, 0, nColumns1);
			System.arraycopy(range2.getAttributes(), 0, resultAttributes, nColumns1, nColumns2);

			return Row.getRelTableEval(resultRows, resultAttributes);
		}

	};


	public static final Function SELECT = new SelectFunction() {

		protected ValueEval evaluate(RelTableEval table, SelectHelperEval helper, int srcRowIndex, int srcColumnIndex) {
			WorkbookEvaluator evaluator = helper._evaluator;
			OperationEvaluationContext ec = helper._ec;
			Ptg[] ptgs = helper._ptgs;
			boolean ignoreDereference = helper._ignoreDereference;

			int nRows = table.getHeight();
			List<ValueEval> evalList = new ArrayList<>();
			for (int i = 0; i < nRows; i++) {
				ValueEval result = evaluator.evaluateFormula(ec, ptgs, true, ignoreDereference, table.getRow(i));
				evalList.add(result);
			}
			List<Row> rows = Row.getRowsFromArea(table, evalList);
			return Row.getRelTableEval(rows, table.getAttributes());
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
			return range.getColumns(attributeIndices);
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


	public static final Function JOIN = new JoinFunction() {

		@Override
		protected ValueEval evaluate(AreaEval range1, AreaEval range2, ValueEval[] args, int srcRowIndex, int srcColumnIndex) {
			throw new NotImplementedException("not implemented");
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

		private boolean compareEvals(ValueEval e1, ValueEval e2) {
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
		public static Row combineRows(Row row1, Row row2) {
			int nColumns1 = row1.getLength();
			int nColumns2 = row2.getLength();
			ValueEval[] combinedRow = new ValueEval[nColumns1+nColumns2];

			for (int i = 0; i < nColumns1; i++) {
				combinedRow[i] = row1.getValue(i);
			}
			for (int i = 0; i < nColumns2; i++) {
				combinedRow[nColumns1+i] = row2.getValue(i);
			}

			return new Row(combinedRow);
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


		/**
		 * Static method to create an array of Row's from an AreaEval
		 *
		 * @param range,conditions
		 * @return
		 */
		public static List<Row> getRowsFromArea(RelTableEval range, ValueEval conditions) {
			List<Row> rows = new ArrayList<>();
			if (conditions instanceof BoolEval) {
				if (!((BoolEval) conditions).getBooleanValue()) return rows;
				else return getRowsFromArea(range);
			} else if (conditions instanceof ArrayEval) {
				ArrayEval cds = (ArrayEval) conditions;
				for (int r = 0; r < range.getHeight(); r++) {
					ValueEval select = cds.getValue(r, 0);
					if (select instanceof BoolEval) {
						if (((BoolEval) select).getBooleanValue()) {
							//get all of the ValueEval's for this row
							TwoDEval row = range.getRow(r);
							ValueEval[] rowEvals = new ValueEval[row.getWidth()];
							for (int c = 0; c < row.getWidth(); c++) {
								rowEvals[c] = row.getValue(0, c);
							}
							rows.add(new Row(rowEvals));
						}
					}
				}
			}
			return rows;
		}

		public boolean matches(Row row2) {

			Row row1 = this;

			if (row1.getLength() != row2.getLength()) {
				return false;
			}

			int width = row1.getLength();
			boolean matches = true;

			//Iterate over columns
			for (int c = 0; c < width; c++) {

				ValueEval value1 = row1.getValue(c);
				ValueEval value2 = row2.getValue(c);

				//arguments for srcCellRow and srcCellCol not used in createCriteriaPredicate, so just use 0, 0
				I_MatchPredicate matchPredicate = Countif.createCriteriaPredicate(value1, 0, 0);

				if (!matchPredicate.matches(value2)) {
					matches = false;
					break;
				}
			}

			return matches;

		}

	}//end Row class

}
