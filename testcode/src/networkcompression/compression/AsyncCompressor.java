package networkcompression.compression;

import networkcompression.runners.AsyncBaseTestRunner;
import networkcompression.AsyncPerformanceMain;
import networkcompression.tests.AsyncBaseTest;
import org.zkoss.zss.model.sys.dependency.Ref;
import org.model.AutoRollbackConnection;
import org.postgresql.geometric.PGbox;
import org.zkoss.zss.model.CellRegion;
import networkcompression.utils.Util;
import org.zkoss.zss.model.SSheet;
import org.model.DBHandler;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.*;

/**
 *
 * A class for performing the dependency compression algorithm described in the Anti-Freeze paper.
 */
public class AsyncCompressor implements Compressable {

    private final int COMPRESSION_CONSTANT;

    public AsyncCompressor (final int compressionConstant) {
        this.COMPRESSION_CONSTANT = compressionConstant;
    }

    @Override
    public int getCellsToUpdate (AsyncBaseTestRunner testRunner, AsyncBaseTest test) {
        SSheet sheet = test.getSheet();
        Collection<CellRegion> sheetCells = Util.getSheetCells(sheet, test.getRegion());
        ArrayList<Ref> dependencies = new ArrayList<>(sheet.getDependencyTable().getDependents(test.getCellToUpdate()));
        if (AsyncPerformanceMain.graphInDB) {
            int[] depSizes = this.compressPGGraphNode(
                    sheet.getBook().getBookName(),
                    sheet.getSheetName(),
                    new CellRegion(0, 0)
            );
            testRunner.metadata.startNumberOfDependents = depSizes[0];
            testRunner.metadata.finalNumberOfDependents = depSizes[1];
        } else {
            testRunner.metadata.startNumberOfDependents = dependencies.size();
            this.compressRefDependencies(dependencies);
            testRunner.metadata.finalNumberOfDependents = dependencies.size();
            sheet.getDependencyTable().addPreDep(test.getCellToUpdate(), new HashSet<>(dependencies));
        }
        dependencies.add(test.getCellToUpdate());
        int cellsToUpdate = 0;
        for (CellRegion sheetCell : sheetCells) {
            boolean matched = false;
            for (Ref dependency : dependencies) {
                CellRegion reg = new CellRegion(dependency);
                if (reg.contains(sheetCell)) {
                    cellsToUpdate++;
                    matched = true;
                }
            }
            if (matched) {
                testRunner.cellsToUpdateSet.add(sheetCell);
            }
        }

        return cellsToUpdate;
    }

    private int[] compressPGGraphNode(String bookName, String sheetname, CellRegion cellRegion) {
        int startSize = -1, finalSize = -1;
        Util.connectToDBIfNotConnected();
        try (AutoRollbackConnection autoRollbackConnection = DBHandler.instance.getConnection()) {
            PGbox region = this.cellRegionToPGbox(cellRegion);
            ArrayList<CellRegion> deps = this.getUncompressedDependents(autoRollbackConnection, bookName, sheetname, region);
            startSize = deps.size();
            this.compressCellRegionDependencies(deps);
            finalSize = deps.size();
            this.deleteUncompressedDependents(autoRollbackConnection, bookName, sheetname, region);
            this.insertCompressedDependents(autoRollbackConnection, bookName, sheetname, region, deps);
            autoRollbackConnection.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new int[]{ startSize, finalSize };
    }

    private void compressRefDependencies (ArrayList<Ref> dependencies) {
        while (dependencies.size() > this.COMPRESSION_CONSTANT) {
            //System.out.println("dependencies.size() " + dependencies.size());
            int best_i = 0, best_j = 0, best_area = Integer.MAX_VALUE;
            Ref best_bounding_box = null;
            for (int i = 0; i < dependencies.size() - 1; i++) {
                for (int j = i + 1; j < dependencies.size(); j++) {
                    Ref bounding = dependencies.get(i).getBoundingBox(dependencies.get(j));
                    int new_area = bounding.getCellCount() -
                            dependencies.get(i).getCellCount() - dependencies.get(j).getCellCount();
                    Ref overlap = dependencies.get(i).getOverlap(dependencies.get(j));
                    if (overlap != null)
                        new_area += overlap.getCellCount();
                    if (new_area==0) {
                        best_area = new_area;
                        best_i = i;
                        best_j = j;
                        best_bounding_box = bounding;
                        i=dependencies.size();
                        break;
                    }


                    if (new_area < best_area) {
                        best_area = new_area;
                        best_i = i;
                        best_j = j;
                        best_bounding_box = bounding;
                    }

                    if (best_area == 0) {
                        break;
                    }
                }
                if (best_area == 0) {
                    break;
                }
            }
            // Merge i,j
            dependencies.remove(best_j);
            dependencies.remove(best_i);
            dependencies.add(best_bounding_box);
        }
    }

    private PGbox cellRegionToPGbox (CellRegion cellRegion) {
        return new PGbox(
                cellRegion.getRow(),
                cellRegion.getColumn(),
                cellRegion.getLastRow(),
                cellRegion.getLastColumn()
        );
    }

    private CellRegion pgBoxToCellRegion (PGbox pgBox) {
        // The order of points is based on how postgres stores them
        return new CellRegion(
                (int) pgBox.point[1].x,
                (int) pgBox.point[1].y,
                (int) pgBox.point[0].x,
                (int) pgBox.point[0].y
        );
    }

    private void compressCellRegionDependencies (ArrayList<CellRegion> dependencies) {
        while (dependencies.size() > this.COMPRESSION_CONSTANT) {
            int best_i = 0, best_j = 0, best_area = Integer.MAX_VALUE;
            CellRegion best_bounding_box = null;
            for (int i = 0; i < dependencies.size() - 1; i++) {
                for (int j = i + 1; j < dependencies.size(); j++) {
                    CellRegion bounding = dependencies.get(i).getBoundingBox(dependencies.get(j));
                    int new_area = bounding.getCellCount() -
                            dependencies.get(i).getCellCount() - dependencies.get(j).getCellCount();
                    CellRegion overlap = dependencies.get(i).getOverlap(dependencies.get(j));
                    if (overlap != null)
                        new_area += overlap.getCellCount();
                    if (new_area==0) {
                        best_area = new_area;
                        best_i = i;
                        best_j = j;
                        best_bounding_box = bounding;
                        i=dependencies.size();
                        break;
                    }


                    if (new_area < best_area) {
                        best_area = new_area;
                        best_i = i;
                        best_j = j;
                        best_bounding_box = bounding;
                    }
                }
            }
            // Merge i,j
            // System.out.println(best_i + " " + best_j + " " + dependencies.get(best_i) + " " + dependencies.get(best_j) + " " + best_bounding_box);
            dependencies.remove(best_j);
            dependencies.remove(best_i);
            dependencies.add(best_bounding_box);
        }
    }

    private ArrayList<CellRegion> getUncompressedDependents (AutoRollbackConnection autoRollbackConnection, String bookName, String sheetname, PGbox pgBox) {
        String selectSql = "WITH RECURSIVE deps AS ( "
                +   "SELECT bookname::text"
                +       ", sheetname::text"
                +       ", range::text"
                +       ", dep_bookname"
                +       ", dep_sheetname"
                +       ", dep_range::text"
                +       ", must_expand "
                +   "FROM full_dependency "
                +   "WHERE bookname = ? "
                +       "AND sheetname = ? "
                +       "AND range && ?  "
                +   "UNION"
                +   "SELECT t.bookname"
                +       ", t.sheetname"
                +       ", t.range"
                +       ", d.dep_bookname"
                +       ", d.dep_sheetname"
                +       ", d.dep_range::text"
                +       ", d.must_expand "
                +   "FROM full_dependency d "
                +   "INNER JOIN deps t ON "
                +       "d.bookname = t.dep_bookname    AND "
                +       "t.must_expand                  AND "
                +       "d.sheetname = t.dep_sheetname  AND "
                +       "d.range && t.dep_range::box"
                + ") "
                + "SELECT bookname"
                +   ", sheetname"
                +   ", range"
                +   ", dep_bookname"
                +   ", dep_sheetname"
                +   ", dep_range::box "
                + "FROM deps";

        ArrayList<CellRegion> deps = new ArrayList<>();
        try (PreparedStatement stmtSelect = autoRollbackConnection.prepareStatement(selectSql)) {
            stmtSelect.setString(1, bookName);
            stmtSelect.setString(2, sheetname);
            stmtSelect.setObject(3, pgBox, Types.OTHER);
            ResultSet rs = stmtSelect.executeQuery();
            while (rs.next()) {
                deps.add(this.pgBoxToCellRegion((PGbox) rs.getObject(6)));
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return deps;
    }

    private void deleteUncompressedDependents (AutoRollbackConnection autoRollbackConnection, String bookName, String sheetname, PGbox pgBox) {
        String deleteSql = "DELETE FROM dependency WHERE bookname = ? AND sheetname = ? AND range && ? ";
        try (PreparedStatement stmtDelete = autoRollbackConnection.prepareStatement(deleteSql)) {
            stmtDelete.setString(1, bookName);
            stmtDelete.setString(2, sheetname);
            stmtDelete.setObject(3, pgBox, Types.OTHER);
            stmtDelete.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void insertCompressedDependents (AutoRollbackConnection autoRollbackConnection, String bookName, String sheetname, PGbox pgBox, ArrayList<CellRegion> deps) {
        String insertSql = "INSERT INTO dependency VALUES (?,?,?,?,?,?,?)";
        try (PreparedStatement stmtInsert = autoRollbackConnection.prepareStatement(insertSql)) {
            stmtInsert.setString(1, bookName);
            stmtInsert.setString(2, sheetname);
            stmtInsert.setObject(3, pgBox, Types.OTHER);
            stmtInsert.setString(4, bookName);
            stmtInsert.setString(5, sheetname);
            stmtInsert.setBoolean(7, false);
            for (CellRegion dependant : deps) {
                stmtInsert.setObject(6, this.cellRegionToPGbox(dependant), Types.OTHER);
                stmtInsert.execute();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
