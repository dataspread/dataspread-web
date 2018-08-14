package org.zkoss.zss.model.impl;

import org.model.AutoRollbackConnection;
import org.model.DBContext;
import org.model.DBHandler;
import org.zkoss.zk.ui.UiException;
import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.ErrorValue;
import org.zkoss.zss.model.SCell;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.sys.EngineFactory;
import org.zkoss.zss.model.sys.formula.FormulaEngine;
import org.zkoss.zss.model.sys.formula.FormulaEvaluationContext;
import org.zkoss.zss.model.sys.formula.FormulaExpression;
import org.zkoss.zss.model.sys.formula.FormulaParseContext;

import java.io.IOException;
import java.io.Reader;
import java.util.*;
import java.util.function.Function;

public abstract class Model {
    protected String tableName;
    protected SSheet sheet;
    /**
     * Todo: get rid of navSbuckets since navS already contains it.
     */
    public ArrayList<Bucket> navSbuckets;
    public NavigationStructure navS;
    public String indexString;
    public HashMap<Integer, Integer> trueOrder;

    public static Model CreateModel(DBContext context, SSheet sheet, ModelType modelType, String tableName) {
        Model model = null;
        switch (modelType) {
            case RCV_Model:
                model = new RCV_Model(context, sheet, tableName);
                break;
            case ROM_Model:
                model = new ROM_Model(context, sheet, tableName);
                break;
            case COM_Model:
                model = new COM_Model(context, sheet, tableName);
                break;
            case HYBRID_Model:
                model = new Hybrid_Model(context, sheet, tableName);
                break;
            case RCV_Model_Simplified:
                model = new RCV_Model_Simplified(context, sheet, tableName);
                break;
            case TOM_Model:
                /* One tom instance for a table */
                model = TOM_Mapping.instance.createTOMModel(context, tableName);
                // model =  new TOM_Model(context, sheet, tableName);
                break;
        }
        model.sheet = sheet;
        return model;
    }

    public abstract ArrayList<Bucket> createNavS(String bucketName, int start, int count);

    // Drop the tables created.
    public abstract void dropSchema(DBContext context);


    // Schema is created by the constructor, if it does not exists.

    //Insert count empty rows starting at row(inclusive)
    public abstract void insertRows(DBContext context, int row, int count);

    // For all the functions below, use logical row/column number

    //Insert count empty columns starting at col(inclusive)
    public abstract void insertCols(DBContext context, int col, int count);

    //Delete count rows starting from row(inclusive)
    public abstract void deleteRows(DBContext context, int row, int count);

    //Delete count columns starting from col(inclusive)
    public abstract void deleteCols(DBContext context, int col, int count);

    //Update a range of cells -- Cells should exist in the sheet
    public abstract void updateCells(DBContext context, Collection<AbstractCellAdv> cells);

    //Delete cells
    public abstract void deleteCells(DBContext context, CellRegion cellRegion);

    public abstract void deleteCells(DBContext context, Collection<AbstractCellAdv> cells);

    public abstract boolean deleteTableRows(DBContext context, CellRegion cellRegion);

    //Get a range of cells
    public abstract Collection<AbstractCellAdv> getCells(DBContext context, CellRegion cellRegion);

    // Get all Cells
    public Collection<AbstractCellAdv> getCells(DBContext context) {
        return getCells(context, getBounds(context));
    }

    // Get size of sheet
    public abstract CellRegion getBounds(DBContext context);

    // Flush Cache and clearCache DB connection
    public abstract void clearCache(DBContext context);

    public String getTableName() {
        return tableName;
    }

    // Import a sheet from a inputStream
    // Gets a connection from handler and commits.
    public abstract void importSheet(Reader reader, char delimiter, boolean useNav) throws IOException;

    public abstract boolean deleteTableColumns(DBContext dbContext, CellRegion cellRegion);

    // Clone only the corresponding tables in postgres
    public abstract Model clone(DBContext dbContext, SSheet sheet, String modelName);

    public abstract ArrayList<String> getHeaders();

    public abstract void setIndexString(String str);

    /**
     * Sort a bucket based on a given attribute. Calls the navigation structure to get starting / ending row number and then call the {@link #navigationSortRangeByAttribute(SSheet, int, int, int[], int)}
     *
     * @param currentSheet
     * @param paths
     * @param attr_indices
     * @param order        0: ascending, 1: descending
     */
    public void navigationSortBucketByAttribute(SSheet currentSheet, int[] paths, int[] attr_indices, int order) {
        return;
    }

    /**
     * Sort a block based on input starting and ending row number.
     *
     * @param currentSheet
     * @param startRow
     * @param endRow
     * @param attr_indices
     * @param order
     */
    public void navigationSortRangeByAttribute(SSheet currentSheet, int startRow, int endRow, int[] attr_indices, int order) {
        return;
    }

    //TODO: move this code to TableSheetModel to support multiple column aggregates
    public Map<String, Object> getColumnAggregate(SSheet currentSheet, int startRow, int endRow, int columnIndex, String agg_id, List<String> paras, boolean translateOnly) {
        Function<Integer, String> colToAlphabet = number -> {
            StringBuilder sb = new StringBuilder();
            /*
             * {@see org.ds.api.controller.NavigationController:113}
             */
            number++; // Since 0 corresponds to the 'A' column in BE
            while (number-- > 0) {
                sb.append((char) ('A' + (number % 26)));
                number /= 26;
            }
            return sb.reverse().toString();
        };

        Map<String, Object> agg = new HashMap<>();
        try (AutoRollbackConnection connection = DBHandler.instance.getConnection()) {
            this.navS.typeConvertColumnIfHavent(connection, columnIndex);

            String colStr = colToAlphabet.apply(columnIndex);
            String regionStr = colStr + String.valueOf(startRow + 1) + ":" + colStr + String.valueOf(endRow + 1);
            String formula;
            {
                StringBuilder fBuilder = new StringBuilder();
                fBuilder.append(agg_id).append("(");
                boolean notFirst = false;
                for (String token : paras) {
                    if (notFirst) {
                        fBuilder.append(",");
                    } else {
                        notFirst = true;
                    }
                    if (token.isEmpty()) {
                        fBuilder.append(regionStr);
                    } else {
                        fBuilder.append(token);
                    }
                }
                fBuilder.append(")");
                formula = fBuilder.toString();
            }
            agg.put("formula", formula);
            if (translateOnly)
                return agg;

            String aggStr;
            if (true) {
                FormulaEngine engine = EngineFactory.getInstance().createFormulaEngine();
                FormulaExpression expr = null;
                expr = engine.parse(formula, new FormulaParseContext(currentSheet, null));
                if (expr.hasError()) {
                    throw new RuntimeException(expr.getErrorMessage());
                }
                System.out.println("Computing " + formula);
                FormulaResultCellValue result = new FormulaResultCellValue(engine.evaluate(expr, new FormulaEvaluationContext(currentSheet, null)));
                Object evalResult = result.getValue();
                if (evalResult instanceof ErrorValue) {
                    aggStr = ((ErrorValue) evalResult).getMessage();
                    agg.put("value", aggStr);
                } else {
                    aggStr = String.valueOf(evalResult);
                    agg.put("value", Double.parseDouble(aggStr));
                }
            } else {// TODO: future feature, make aggregation results actual cells.
                SCell naviCell = currentSheet.getCell(columnIndex, 20); // TODO: Change this cell to another sheet designated to navigation.
                naviCell.setFormulaValue(formula, connection, true);
                Object evalResult = naviCell.getValueSync();
                if (evalResult instanceof ErrorValue) {
                    aggStr = ((ErrorValue) evalResult).getMessage();
                    agg.put("value", aggStr);
                } else {
                    aggStr = String.valueOf(evalResult);
                    agg.put("value", Double.parseDouble(aggStr));
                }
            }
        }
        return agg;
    }


    public static class CombinedEntry {
        static int keyCount;

        public static void setKeyCount(int count) {
            keyCount = count;
        }

        final private int _index; //original row/column index
        final private Object[] _values; //cell value
        private int keyIndex;

        public CombinedEntry(int rowId) {
            _values = new Object[keyCount];
            this._index = rowId;
            this.keyIndex = 0;
        }

        public int getIndex() {
            return _index;
        }

        public Object[] getValues() {
            return _values;
        }

        public void appendEntry(Object extra) {
            this._values[keyIndex++] = extra;
        }

    }

    public static class KeyComparator implements Comparator<CombinedEntry> {
        final private boolean[] _descs;
        final private boolean _matchCase;
//		final private int _sortMethod; //TODO byNumberOfStroks, byPinyYin
//		final private int _type; //TODO PivotTable only: byLabel, byValue

        public KeyComparator(boolean[] descs, boolean matchCase) {
            _descs = descs;
            _matchCase = matchCase;
        }

        @Override
        public int compare(CombinedEntry o1, CombinedEntry o2) {
            final Object[] values1 = o1.getValues();
            final Object[] values2 = o2.getValues();
            return compare(values1, values2);
        }

        private int compare(Object[] values1, Object[] values2) {
            final int len = values1.length;
            for (int j = 0; j < len; ++j) {
                int result = compareValue(values1[j], values2[j], _descs[j]);
                if (result != 0) {
                    return result;
                }
            }
            return 0;
        }

        //1. null is always sorted at the end
        //2. Error(Byte) > Boolean > String > Double
        private int compareValue(Object val1, Object val2, boolean desc) {
            if (val1 == val2) {
                return 0;
            }
            final int order1 = val1 instanceof Byte ? 4 : val1 instanceof Boolean ? 3 : val1 instanceof String ? 2 : val1 instanceof Number ? 1 : desc ? 0 : 5;
            final int order2 = val2 instanceof Byte ? 4 : val2 instanceof Boolean ? 3 : val2 instanceof String ? 2 : val2 instanceof Number ? 1 : desc ? 0 : 5;
            int ret = 0;
            if (order1 != order2) {
                ret = order1 - order2;
            } else { //order1 == order2
                switch (order1) {
                    case 4: //error, no order among different errors
                        ret = 0;
                        break;
                    case 3: //Boolean
                        ret = ((Boolean) val1).compareTo((Boolean) val2);
                        break;
                    case 2: //RichTextString
                        ret = compareString(val1.toString(), val2.toString());
                        break;
                    case 1: //Double
                        ret = ((Double) val1).compareTo((Double) val2);
                        break;
                    default:
                        throw new UiException("Unknown value type: " + val1.getClass());
                }
            }
            return desc ? -ret : ret;
        }

        private int compareString(String s1, String s2) {
            return _matchCase ? compareString0(s1, s2) : s1.compareToIgnoreCase(s2);
        }

        //bug 59 Sort with case sensitive should be in special spreadsheet order
        private int compareString0(String s1, String s2) {
            final int len1 = s1.length();
            final int len2 = s2.length();
            final int len = len1 > len2 ? len2 : len1;
            for (int j = 0; j < len; ++j) {
                final int ret = compareChar(s1.charAt(j), s2.charAt(j));
                if (ret != 0) {
                    return ret;
                }
            }
            return len1 - len2;
        }

        private int compareChar(char ch1, char ch2) {
            final char uch1 = Character.toUpperCase(ch1);
            final char uch2 = Character.toUpperCase(ch2);
            return uch1 == uch2 ?
                    (ch2 - ch1) : //yes, a < A
                    (uch1 - uch2); //yes, a < b, a < B, A < b, and A < B
        }
    }

    //
    public enum ModelType {
        ROM_Model, COM_Model, RCV_Model, HYBRID_Model, TOM_Model, RCV_Model_Simplified
    }
}