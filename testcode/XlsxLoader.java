package org.zkoss.zss.model.sys.formula.Test;

import org.model.DBHandler;
import org.zkoss.poi.POIXMLTextExtractor;
import org.zkoss.poi.xssf.extractor.XSSFExcelExtractor;
import org.zkoss.zss.model.impl.sys.DependencyTableImplV2;
import org.zkoss.zss.model.sys.EngineFactory;

import com.google.common.collect.ImmutableMap;
import org.model.AutoRollbackConnection;
import org.model.DBHandler;
import org.postgresql.geometric.PGbox;
import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.SBook;
import org.zkoss.zss.model.SCell;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.impl.CellImpl;
import org.zkoss.zss.model.impl.FormulaCacheCleaner;
import org.zkoss.zss.model.impl.GraphCompressor;
import org.zkoss.zss.model.impl.SheetImpl;
import org.zkoss.zss.model.impl.sys.DependencyTableImplV2;
import org.zkoss.zss.model.impl.sys.formula.FormulaAsyncListener;
import org.zkoss.zss.model.impl.sys.formula.FormulaAsyncSchedulerSimple;
import org.zkoss.zss.model.sys.BookBindings;
import org.zkoss.zss.model.sys.EngineFactory;
import org.zkoss.zss.model.sys.dependency.Ref;
import org.zkoss.zss.model.sys.formula.DirtyManagerLog;
import org.zkoss.zss.model.sys.formula.FormulaAsyncScheduler;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;
import java.util.stream.Collectors;

public class XlsxLoader {

    private static void connect(){
        String url = "jdbc:postgresql://127.0.0.1:5432/ibd";
        String driver = "org.postgresql.Driver";
        String userName = "dbuser";
        String password = "dbadmin";
        EngineFactory.dependencyTableClazz = DependencyTableImplV2.class;

        DBHandler.connectToDB(url, driver, userName, password);
    }

    public static void main(String[] args) throws Exception {
//        if(args.length < 1) {
//            System.err.println("Use:");
//            System.err.println("  XSSFExcelExtractor <filename.xlsx>");
//            System.exit(1);
//        }
        connect();
        String prefix = "L:\\Project\\DataSpread\\We_want_your_spreadsheets__70684530474156\\" +
                "We_want_your_spreadsheets__70684530474156\\";
        String[] files = {
                "smallFormula\\CS_465_User_Evaulations.xlsx"
        };

        for (String file: files) {
            POIXMLTextExtractor extractor =
                    new XSSFExcelExtractor(prefix + file);
            String text = extractor.getText();
            String sheetName = text.substring(0,text.indexOf("\n"));
            String content = text.substring(text.indexOf("\n") + 1);
            System.out.println("Sheet name: " + sheetName);
            System.out.println("Content: " + content);
        }
    }

}
