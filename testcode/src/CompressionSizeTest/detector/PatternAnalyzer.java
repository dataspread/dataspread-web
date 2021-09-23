package CompressionSizeTest.detector;

import CompressionSizeTest.detector.Utils.*;

import java.io.*;
import java.util.HashSet;

public class PatternAnalyzer {

    static class StatCollector {

        public String fileName;
        public int totalEdges;
        public int[] totalCompressedEdges;
        public int[] maxCompressedEdges;
        public String[] lastComitted;
        public boolean rowWise;

        public StatCollector(
                String fileName,
                int totalEdges,
                int[] totalCompressedEdges,
                int[] maxCompressedEdges,
                String[] lastComitted,
                boolean rowWise) {
            this.fileName = fileName;
            this.totalEdges = totalEdges;
            this.totalCompressedEdges = totalCompressedEdges;
            this.maxCompressedEdges = maxCompressedEdges;
            this.lastComitted = lastComitted;
            this.rowWise = rowWise;
        }
    }

    public static void main(String[] args) {
        if (args.length < 3) {
            System.err.println("Need three parameter: path to xls file; directory of output file; number of rows to skip");
            System.exit(-1);
        }

        boolean rowWise = false;
        StatCollector colStat = new PatternAnalyzer().startAnalyzing(args, rowWise);

        rowWise = true;
        StatCollector rowStat = new PatternAnalyzer().startAnalyzing(args, rowWise);

        if (colStat.totalEdges != rowStat.totalEdges) {
            System.out.println("File " + args[0] + " totalEdges not match");
            System.exit(-1);
        }

        if (colStat.totalEdges > 0) {
            writeStats(args[1], colStat);
            writeStats(args[1], rowStat);
            writeComparison(args[1], colStat, rowStat);
        }
    }

    private StatCollector startAnalyzing(String[] args, boolean rowWise) {
        SpreadsheetParser spreadsheetParser = new SpreadsheetParser(args[0], rowWise);

        if (spreadsheetParser.skipParsing(Integer.parseInt(args[2])))
            System.exit(-1);

        spreadsheetParser.parseSpreadsheet();

        int totalEdges = 0;
        int[] totalCompressedEdges = null;
        int[] maxCompressedEdges = null;
        String[] lastComitted = null;
        for (SheetData sheetData : spreadsheetParser) {
            if (sheetData == null) continue;

            PatternManager patternManager = new PatternManager(args[0],
                    sheetData.getSheetName(), rowWise);
            patternManager.reset();

            CellLoc prevCellLoc = new CellLoc(-1, -1);

            for (Pair<HashSet<CellArea>, CellLoc> dep : sheetData) {
                totalEdges = totalEdges + dep.first.stream().mapToInt(CellArea::getCellNum).sum();
                CellLoc curCellLoc = dep.second;

                if (!prevCellLoc.isAdjacent(curCellLoc, rowWise)) {
                    if (patternManager.getCommittedCells() > 2) {
                        int[] blockCompressedEdges = patternManager.getCompressedEdges();
                        String[] blockLastComitted = patternManager.getLastComitted();
                        totalCompressedEdges = combineStat(blockCompressedEdges, totalCompressedEdges, true);
                        maxCompressedEdges = combineStat(blockCompressedEdges, maxCompressedEdges, false);
                        lastComitted = setLastComitted(blockCompressedEdges, maxCompressedEdges, blockLastComitted, lastComitted);
                    }
                    patternManager.reset();
                }

                boolean isValid = patternManager.addDeps(dep.first, dep.second);

                if (!isValid) {
                    int[] blockCompressedEdges = patternManager.getCompressedEdges();
                    String[] blockLastComitted = patternManager.getLastComitted();
                    totalCompressedEdges = combineStat(blockCompressedEdges, totalCompressedEdges, true);
                    maxCompressedEdges = combineStat(blockCompressedEdges, maxCompressedEdges, false);
                    lastComitted = setLastComitted(blockCompressedEdges, maxCompressedEdges, blockLastComitted, lastComitted);
                    patternManager.reset();
                    patternManager.addDeps(dep.first, dep.second);
                    patternManager.commitDeps();
                } else {
                    patternManager.commitDeps();
                }

                prevCellLoc = curCellLoc;
            }
            int[] blockCompressedEdges = patternManager.getCompressedEdges();
            String[] blockLastComitted = patternManager.getLastComitted();
            totalCompressedEdges = combineStat(blockCompressedEdges, totalCompressedEdges, true);
            maxCompressedEdges = combineStat(blockCompressedEdges, maxCompressedEdges, false);
            lastComitted = setLastComitted(blockCompressedEdges, maxCompressedEdges, blockLastComitted, lastComitted);
        }

        return new StatCollector(spreadsheetParser.getFileName(),
                totalEdges, totalCompressedEdges, maxCompressedEdges, lastComitted, rowWise);
        // if (totalEdges > 0) {
        //     writeStats(args[1], spreadsheetParser.getFileName(),
        //             totalEdges, totalCompressedEdges, maxCompressedEdges, lastComitted, rowWise);
        // }
    }

    private int[] combineStat(int[] blockCompressedEdges,
                              int[] resultCompressedEdges,
                              boolean isAdd) {
        int[] retCompressedEdges = resultCompressedEdges;
        if (resultCompressedEdges == null)
            retCompressedEdges = new int[blockCompressedEdges.length];

        for (int i = 0; i < retCompressedEdges.length; i++) {
            if (resultCompressedEdges == null) {
                retCompressedEdges[i] = blockCompressedEdges[i];
            } else {
                if (isAdd) {
                    retCompressedEdges[i] = resultCompressedEdges[i] + blockCompressedEdges[i];
                } else {
                    retCompressedEdges[i] = Math.max(resultCompressedEdges[i], blockCompressedEdges[i]);
                }
            }
        }
        return retCompressedEdges;
    }

    private String[] setLastComitted(int[] blockCompressedEdges,
                                     int[] maxCompressedEdges,
                                     String[] blockLastComitted,
                                     String[] lastComitted) {
        String[] retLastComitted = lastComitted;
        if (lastComitted == null)
            retLastComitted = new String[maxCompressedEdges.length];
        for (int i = 0; i < retLastComitted.length; i++) {
            if (retLastComitted[i] == null) retLastComitted[i] = "";
            if (blockCompressedEdges[i] == maxCompressedEdges[i] && maxCompressedEdges[i] != 0) {
                retLastComitted[i] = blockLastComitted[i];
            }
        }

        return retLastComitted;
    }

    static void writeStats(String outputDir,
                           StatCollector statCollector) {

        String fileName = statCollector.fileName;
        int totalEdges = statCollector.totalEdges;
        int[] totalCompressedEdges = statCollector.totalCompressedEdges;
        int[] maxCompressedEdges = statCollector.maxCompressedEdges;
        String[] lastComitted = statCollector.lastComitted;
        boolean rowWise = statCollector.rowWise;

        File dir = new File(outputDir);
        if (!dir.exists()) {
            if (!dir.mkdir()) {
                System.out.println("Creating the folder " + outputDir + " failed");
                System.exit(-1);
            }
        }

        String outputPath = rowWise ? outputDir + "/stat_row.csv" : outputDir + "/stat_col.csv";
        try(FileWriter fw = new FileWriter(outputPath, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {
            double compressionRatio = ((double) totalCompressedEdges[0])/((double) totalEdges);
            StringBuilder outpuStr = new StringBuilder(fileName + ",\t" + totalEdges);
            outpuStr.append(",\t").append(totalEdges - totalCompressedEdges[0]);
            for (int oneTypeCompEdge: totalCompressedEdges) {
                outpuStr.append(",\t").append(oneTypeCompEdge);
            }
            outpuStr.append(",\t").append(compressionRatio);
            for (int oneTypeCompEdge: maxCompressedEdges) {
                outpuStr.append(",\t").append(oneTypeCompEdge);
            }
            for (String oneLastCommit: lastComitted) {
                outpuStr.append(",\t").append(oneLastCommit);
            }
            out.println(outpuStr);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    static void writeComparison(String outputDir,
                                StatCollector colStat,
                                StatCollector rowStat) {

        String fileName = colStat.fileName;
        int totalEdges = colStat.totalEdges;
        int colRemainEdges = totalEdges - colStat.totalCompressedEdges[0];
        int rowRemainEdges = totalEdges - rowStat.totalCompressedEdges[0];
        int minRemainEdges = Math.max(0,
                totalEdges - colStat.totalCompressedEdges[0] - rowStat.totalCompressedEdges[0]);
        int maxRemainEdges = Math.min(colRemainEdges, rowRemainEdges);

        double colCompRatio = ((double) colStat.totalCompressedEdges[0])/((double) totalEdges);
        double rowCompRatio = ((double) rowStat.totalCompressedEdges[0])/((double) totalEdges);
        double minRatio = Math.max(colCompRatio, rowCompRatio);
        double maxRatio = Math.min(1.0, colCompRatio + rowCompRatio);

        String outputPath = outputDir + "/stat_comparison.csv";
        try(FileWriter fw = new FileWriter(outputPath, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {
            StringBuilder outputStr = new StringBuilder(fileName + ",\t" + totalEdges);
            outputStr
                    .append(",\t").append(colRemainEdges)
                    .append(",\t").append(rowRemainEdges)
                    .append(",\t").append(maxRemainEdges)
                    .append(",\t").append(minRemainEdges)
                    .append(",\t").append(colCompRatio)
                    .append(",\t").append(rowCompRatio)
                    .append(",\t").append(minRatio)
                    .append(",\t").append(maxRatio);
            out.println(outputStr);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

}
