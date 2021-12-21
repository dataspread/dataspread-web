package CompressionSizeTest;

import CompressionSizeTest.detector.SheetData;
import CompressionSizeTest.detector.SpreadsheetParser;
import CompressionSizeTest.detector.Utils.CellArea;
import CompressionSizeTest.detector.Utils.CellLoc;
import CompressionSizeTest.detector.Utils.Pair;
import org.zkoss.zss.model.impl.RefImpl;
import org.zkoss.zss.model.impl.sys.DependencyTableComp;
import org.zkoss.zss.model.sys.dependency.Ref;
import org.zkoss.zss.model.impl.sys.utils.PatternType;

import java.io.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicLong;

public class SizeTestMain {

    /**
     * DB Connection Configuration
     * */
    public static String      url         = "jdbc:postgresql://127.0.0.1:5432/SizeTest";
    public static String      dbDriver    = "org.postgresql.Driver";
    public static String      userName    = "dataspreaduser";
    public static String      password    = "password";


    public static void main (String[] args) {
        if (args.length < 2) {
            System.err.println("Need two parameter: path to xls file; directory of output file");
            System.exit(-1);
        }

        String xlsPath = args[0];
        String outFolder = args[1];
        boolean rowWise =false;

        Utils.connectToDBIfNotConnected();
        SpreadsheetParser spreadsheetParser = new SpreadsheetParser(xlsPath, rowWise);
        spreadsheetParser.parseSpreadsheet();

        int patternLength = PatternType.values().length;
        long[] compEdges = new long[patternLength];
        long[] uncompEdges = new long[patternLength];
        AtomicLong tacoEdges = new AtomicLong();
        AtomicLong tacoNodes = new AtomicLong();
        AtomicLong tacoFormulae = new AtomicLong();

        long totalEdges = 0;
        long totalNodes = 0;
        long totalFormulae = 0;
        for (SheetData sheetData : spreadsheetParser) {
            if (sheetData == null) continue;

            HashSet<CellArea> cellAreaHashSet = new HashSet<>();
            DependencyTableComp depTblComp = new DependencyTableComp();

            LinkedList<org.zkoss.util.Pair<Ref, Ref>> edgeBatch = new LinkedList<>();
            String bookName = "dummy";
            String sheetName = "dummy";

            for (Pair<HashSet<CellArea>, CellLoc> formulaCell : sheetData) {
                totalEdges += formulaCell.first.size();
                totalFormulae += 1;
                CellLoc curCellLoc = formulaCell.second;

                CellArea formulaArea = new CellArea(curCellLoc.getRow(), curCellLoc.getColumn(),
                        curCellLoc.getRow(), curCellLoc.getColumn());
                cellAreaHashSet.add(formulaArea);

                Ref depRef = cellLocToRef(curCellLoc, bookName, sheetName);
                for (CellArea cellArea: formulaCell.first) {
                    cellAreaHashSet.add(cellArea);
                    Ref precRef = cellAreaToRef(cellArea, bookName, sheetName);
                    edgeBatch.add(new org.zkoss.util.Pair<>(precRef, depRef));
                }
            }

            totalNodes += cellAreaHashSet.size();

            HashSet<Ref> refSet = new HashSet<>();
            depTblComp.addBatch(bookName, sheetName, edgeBatch);
            depTblComp.refreshCache(bookName, sheetName);
            depTblComp.getAllEdges().forEach((dep, precsWithMeta) -> {
                refSet.add(dep);
                tacoFormulae.addAndGet(1);

                precsWithMeta.forEach(precMeta -> {
                    tacoEdges.addAndGet(1);
                    Ref prec = precMeta.getRef();
                    refSet.add(prec);

                    int patternIdx = precMeta.getEdgeMeta().patternType.ordinal();
                    compEdges[patternIdx] = compEdges[patternIdx] + 1;

                    int compressCount = (dep.getCellCount() + precMeta.getEdgeMeta().gapLength) /
                            (precMeta.getEdgeMeta().gapLength + 1);

                    uncompEdges[patternIdx] = uncompEdges[patternIdx] + compressCount;
                });
            });
            tacoNodes.addAndGet(refSet.size());

            Utils.cleanDB();
        }

        if (totalEdges > 10) {
            writeStats(outFolder, spreadsheetParser.getFileName(), totalEdges, totalFormulae,
                    totalNodes, tacoEdges.get(), tacoFormulae.get(),
                    tacoNodes.get(), compEdges, uncompEdges);
        }
    }

    private static Ref cellLocToRef(CellLoc cellLoc, String bookName, String sheetName) {
        return new RefImpl(bookName, sheetName, cellLoc.getRow(),
                cellLoc.getColumn(), cellLoc.getRow(), cellLoc.getColumn());
    }

    private static Ref cellAreaToRef(CellArea cellArea, String bookName, String sheetName) {
        return new RefImpl(bookName, sheetName,
                cellArea.getStart().getRow(),
                cellArea.getStart().getColumn(),
                cellArea.getEnd().getRow(),
                cellArea.getEnd().getColumn());
    }


    private static void writeStats(String outputDir,
                                   String fileName,
                                   long totalEdges,
                                   long totalFormulae, long totalNodes,
                                   long tacoEdges,
                                   long tacoFormulae, long tacoNodes,
                                   long[] compEdges,
                                   long[] uncompEdges) {

        long deDuplicateEdges = Arrays.stream(uncompEdges).sum();

        File dir = new File(outputDir);
        if (!dir.exists()) {
            if (!dir.mkdir()) {
                System.out.println("Creating the folder " + outputDir + " failed");
                System.exit(-1);
            }
        }

        String outputPath = outputDir + "/stat.csv";
        try(FileWriter fw = new FileWriter(outputPath, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw)) {

            StringBuilder outpuStr = new StringBuilder(fileName + ",\t" + deDuplicateEdges);
            outpuStr.append(",\t").append(totalFormulae);
            outpuStr.append(",\t").append(totalNodes);
            outpuStr.append(",\t").append(tacoEdges);
            outpuStr.append(",\t").append(tacoFormulae);
            outpuStr.append(",\t").append(tacoNodes);
            for (int i = 0; i < compEdges.length; i++) {
                outpuStr.append(",\t").append(compEdges[i]);
                outpuStr.append(",\t").append(uncompEdges[i]);
            }
            outpuStr.append(",\t").append(totalEdges);
            out.println(outpuStr);

        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

}
