/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.zkoss.poi.hssf.extractor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import org.zkoss.poi.POIOLE2TextExtractor;
import org.zkoss.poi.hssf.usermodel.HSSFCell;
import org.zkoss.poi.hssf.usermodel.HSSFCellStyle;
import org.zkoss.poi.hssf.usermodel.HSSFComment;
import org.zkoss.poi.hssf.usermodel.HSSFDataFormatter;
import org.zkoss.poi.hssf.usermodel.HSSFRichTextString;
import org.zkoss.poi.hssf.usermodel.HSSFRow;
import org.zkoss.poi.hssf.usermodel.HSSFSheet;
import org.zkoss.poi.hssf.usermodel.HSSFWorkbook;
import org.zkoss.poi.poifs.filesystem.DirectoryNode;
import org.zkoss.poi.poifs.filesystem.POIFSFileSystem;
import org.zkoss.poi.ss.formula.eval.ErrorEval;
import org.zkoss.poi.ss.usermodel.HeaderFooter;

/**
 * A text extractor for Excel files.
 * <p>
 * Returns the textual content of the file, suitable for
 *  indexing by something like Lucene, but not really
 *  intended for display to the user.
 * </p>
 * <p>
 * To turn an excel file into a CSV or similar, then see
 *  the XLS2CSVmra example
 * </p>
 * <link href="http://svn.apache.org/repos/asf/poi/trunk/src/examples/src/org/apache/poi/hssf/eventusermodel/examples/XLS2CSVmra.java">
 * http://svn.apache.org/repos/asf/poi/trunk/src/examples/src/org/apache/poi/hssf/eventusermodel/examples/XLS2CSVmra.java</link>
 */
public class ExcelExtractor extends POIOLE2TextExtractor implements org.zkoss.poi.ss.extractor.ExcelExtractor {
	private HSSFWorkbook _wb;
	private HSSFDataFormatter _formatter;
	private boolean _includeSheetNames = true;
	private boolean _shouldEvaluateFormulas = true;
	private boolean _includeCellComments = false;
	private boolean _includeBlankCells = false;
	private boolean _includeHeadersFooters = true;

	public ExcelExtractor(HSSFWorkbook wb) {
		super(wb);
		_wb = wb;
		_formatter = new HSSFDataFormatter();
	}
	public ExcelExtractor(POIFSFileSystem fs) throws IOException {
		this(fs.getRoot());
	}
	/**
     * @deprecated Use {@link #ExcelExtractor(DirectoryNode)} instead
     */
    @Deprecated
    @SuppressWarnings( "unused" )
    public ExcelExtractor(DirectoryNode dir, POIFSFileSystem fs) throws IOException {
        this( dir );
    }
    public ExcelExtractor(DirectoryNode dir) throws IOException {
		this(new HSSFWorkbook(dir, true));
	}

	private static final class CommandParseException extends Exception {
		public CommandParseException(String msg) {
			super(msg);
		}
	}
	private static final class CommandArgs {
		private final boolean _requestHelp;
		private final File _inputFile;
		private final boolean _showSheetNames;
		private final boolean _evaluateFormulas;
		private final boolean _showCellComments;
		private final boolean _showBlankCells;
		private final boolean _headersFooters;
		public CommandArgs(String[] args) throws CommandParseException {
			int nArgs = args.length;
			File inputFile = null;
			boolean requestHelp = false;
			boolean showSheetNames = true;
			boolean evaluateFormulas = true;
			boolean showCellComments = false;
			boolean showBlankCells = false;
			boolean headersFooters = true;
			for (int i=0; i<nArgs; i++) {
				String arg = args[i];
				if ("-help".equalsIgnoreCase(arg)) {
					requestHelp = true;
					break;
				}
				if ("-i".equals(arg)) {
					 // step to next arg
					if (++i >= nArgs) {
						throw new CommandParseException("Expected filename after '-i'");
					}
					arg = args[i];
					if (inputFile != null) {
						throw new CommandParseException("Only one input file can be supplied");
					}
					inputFile = new File(arg);
					if (!inputFile.exists()) {
						throw new CommandParseException("Specified input file '" + arg + "' does not exist");
					}
					if (inputFile.isDirectory()) {
						throw new CommandParseException("Specified input file '" + arg + "' is a directory");
					}
					continue;
				}
				if ("--show-sheet-names".equals(arg)) {
					showSheetNames = parseBoolArg(args, ++i);
					continue;
				}
				if ("--evaluate-formulas".equals(arg)) {
					evaluateFormulas = parseBoolArg(args, ++i);
					continue;
				}
				if ("--show-comments".equals(arg)) {
					showCellComments = parseBoolArg(args, ++i);
					continue;
				}
				if ("--show-blanks".equals(arg)) {
					showBlankCells = parseBoolArg(args, ++i);
					continue;
				}
				if ("--headers-footers".equals(arg)) {
					headersFooters = parseBoolArg(args, ++i);
					continue;
				}
				throw new CommandParseException("Invalid argument '" + arg + "'");
			}
			_requestHelp = requestHelp;
			_inputFile = inputFile;
			_showSheetNames = showSheetNames;
			_evaluateFormulas = evaluateFormulas;
			_showCellComments = showCellComments;
			_showBlankCells = showBlankCells;
			_headersFooters = headersFooters;
		}
		private static boolean parseBoolArg(String[] args, int i) throws CommandParseException {
			if (i >= args.length) {
				throw new CommandParseException("Expected value after '" + args[i-1] + "'");
			}
			String value = args[i].toUpperCase();
			if ("Y".equals(value) || "YES".equals(value) || "ON".equals(value) || "TRUE".equals(value)) {
				return true;
			}
			if ("N".equals(value) || "NO".equals(value) || "OFF".equals(value) || "FALSE".equals(value)) {
				return false;
			}
			throw new CommandParseException("Invalid value '" + args[i] + "' for '" + args[i-1] + "'. Expected 'Y' or 'N'");
		}
		public boolean isRequestHelp() {
			return _requestHelp;
		}
		public File getInputFile() {
			return _inputFile;
		}
		public boolean shouldShowSheetNames() {
			return _showSheetNames;
		}
		public boolean shouldEvaluateFormulas() {
			return _evaluateFormulas;
		}
		public boolean shouldShowCellComments() {
			return _showCellComments;
		}
		public boolean shouldShowBlankCells() {
			return _showBlankCells;
		}
		public boolean shouldIncludeHeadersFooters() {
			return _headersFooters;
		}
	}

	private static void printUsageMessage(PrintStream ps) {
		ps.println("Use:");
		ps.println("    " + ExcelExtractor.class.getName() + " [<flag> <value> [<flag> <value> [...]]] [-i <filename.xls>]");
		ps.println("       -i <filename.xls> specifies input file (default is to use stdin)");
		ps.println("       Flags can be set on or off by using the values 'Y' or 'N'.");
		ps.println("       Following are available flags and their default values:");
		ps.println("       --show-sheet-names  Y");
		ps.println("       --evaluate-formulas Y");
		ps.println("       --show-comments     N");
		ps.println("       --show-blanks       Y");
		ps.println("       --headers-footers   Y");
	}

	/**
	 * Command line extractor.
	 */
	public static void main(String[] args) {

		CommandArgs cmdArgs;
		try {
			cmdArgs = new CommandArgs(args);
		} catch (CommandParseException e) {
			System.err.println(e.getMessage());
			printUsageMessage(System.err);
			System.exit(1);
			return; // suppress compiler error
		}

		if (cmdArgs.isRequestHelp()) {
			printUsageMessage(System.out);
			return;
		}

		try {
			InputStream is;
			if(cmdArgs.getInputFile() == null) {
				is = System.in;
			} else {
				is = new FileInputStream(cmdArgs.getInputFile());
			}
			HSSFWorkbook wb = new HSSFWorkbook(is);

			ExcelExtractor extractor = new ExcelExtractor(wb);
			extractor.setIncludeSheetNames(cmdArgs.shouldShowSheetNames());
			extractor.setFormulasNotResults(!cmdArgs.shouldEvaluateFormulas());
			extractor.setIncludeCellComments(cmdArgs.shouldShowCellComments());
			extractor.setIncludeBlankCells(cmdArgs.shouldShowBlankCells());
			extractor.setIncludeHeadersFooters(cmdArgs.shouldIncludeHeadersFooters());
			System.out.println(extractor.getText());
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	/**
	 * Should sheet names be included? Default is true
	 */
	public void setIncludeSheetNames(boolean includeSheetNames) {
		_includeSheetNames = includeSheetNames;
	}
	/**
	 * Should we return the formula itself, and not
	 *  the result it produces? Default is false
	 */
	public void setFormulasNotResults(boolean formulasNotResults) {
		_shouldEvaluateFormulas = !formulasNotResults;
	}
	/**
	 * Should cell comments be included? Default is false
	 */
	public void setIncludeCellComments(boolean includeCellComments) {
		_includeCellComments = includeCellComments;
	}
	/**
	 * Should blank cells be output? Default is to only
	 *  output cells that are present in the file and are
	 *  non-blank.
	 */
	public void setIncludeBlankCells(boolean includeBlankCells) {
		_includeBlankCells = includeBlankCells;
	}
	/**
	 * Should headers and footers be included in the output?
	 * Default is to include them.
	 */
	public void setIncludeHeadersFooters(boolean includeHeadersFooters) {
		_includeHeadersFooters = includeHeadersFooters;
	}

	/**
	 * Retrieves the text contents of the file
	 */
	public String getText() {
		StringBuffer text = new StringBuffer();

		// We don't care about the difference between
		//  null (missing) and blank cells
		_wb.setMissingCellPolicy(HSSFRow.RETURN_BLANK_AS_NULL);

		// Process each sheet in turn
		for(int i=0;i<_wb.getNumberOfSheets();i++) {
			HSSFSheet sheet = _wb.getSheetAt(i);
			if(sheet == null) { continue; }

			if(_includeSheetNames) {
				String name = _wb.getSheetName(i);
				if(name != null) {
					text.append(name);
					text.append("\n");
				}
			}

			// Header text, if there is any
			if(_includeHeadersFooters) {
				text.append(_extractHeaderFooter(sheet.getHeader()));
			}

			int firstRow = sheet.getFirstRowNum();
			int lastRow = sheet.getLastRowNum();
			for(int j=firstRow;j<=lastRow;j++) {
				HSSFRow row = sheet.getRow(j);
				if(row == null) { continue; }

				// Check each cell in turn
				int firstCell = row.getFirstCellNum();
				int lastCell = row.getLastCellNum();
				if(_includeBlankCells) {
					firstCell = 0;
				}

				for(int k=firstCell;k<lastCell;k++) {
					HSSFCell cell = row.getCell(k);
					boolean outputContents = true;

					if(cell == null) {
						// Only output if requested
						outputContents = _includeBlankCells;
					} else {
						switch(cell.getCellType()) {
							case HSSFCell.CELL_TYPE_STRING:
								text.append(cell.getRichStringCellValue().getString());
								break;
							case HSSFCell.CELL_TYPE_NUMERIC:
								text.append(
								      _formatter.formatCellValue(cell)
								);
								break;
							case HSSFCell.CELL_TYPE_BOOLEAN:
								text.append(cell.getBooleanCellValue());
								break;
							case HSSFCell.CELL_TYPE_ERROR:
								text.append(ErrorEval.getText(cell.getErrorCellValue()));
								break;
							case HSSFCell.CELL_TYPE_FORMULA:
								if(!_shouldEvaluateFormulas) {
									text.append(cell.getCellFormula());
								} else {
									switch(cell.getCachedFormulaResultType()) {
										case HSSFCell.CELL_TYPE_STRING:
											HSSFRichTextString str = cell.getRichStringCellValue();
											if(str != null && str.length() > 0) {
												text.append(str.toString());
											}
											break;
										case HSSFCell.CELL_TYPE_NUMERIC:
										   HSSFCellStyle style = cell.getCellStyle();
										   if(style == null) {
										      text.append( cell.getNumericCellValue() );
										   } else {
	                                 text.append(
	                                       _formatter.formatRawCellContents(
	                                             cell.getNumericCellValue(),
	                                             style.getDataFormat(),
	                                             style.getDataFormatString()
	                                       )
	                                 );
										   }
											break;
										case HSSFCell.CELL_TYPE_BOOLEAN:
											text.append(cell.getBooleanCellValue());
											break;
										case HSSFCell.CELL_TYPE_ERROR:
											text.append(ErrorEval.getText(cell.getErrorCellValue()));
											break;

									}
								}
								break;
							default:
								throw new RuntimeException("Unexpected cell type (" + cell.getCellType() + ")");
						}

						// Output the comment, if requested and exists
						HSSFComment comment = cell.getCellComment();
						if(_includeCellComments && comment != null) {
							// Replace any newlines with spaces, otherwise it
							//  breaks the output
							String commentText = comment.getString().getString().replace('\n', ' ');
							text.append(" Comment by "+comment.getAuthor()+": "+commentText);
						}
					}

					// Output a tab if we're not on the last cell
					if(outputContents && k < (lastCell-1)) {
						text.append("\t");
					}
				}

				// Finish off the row
				text.append("\n");
			}

			// Finally Footer text, if there is any
			if(_includeHeadersFooters) {
				text.append(_extractHeaderFooter(sheet.getFooter()));
			}
		}

		return text.toString();
	}

	public static String _extractHeaderFooter(HeaderFooter hf) {
		StringBuffer text = new StringBuffer();

		if(hf.getLeft() != null) {
			text.append(hf.getLeft());
		}
		if(hf.getCenter() != null) {
			if(text.length() > 0)
				text.append("\t");
			text.append(hf.getCenter());
		}
		if(hf.getRight() != null) {
			if(text.length() > 0)
				text.append("\t");
			text.append(hf.getRight());
		}
		if(text.length() > 0)
			text.append("\n");

		return text.toString();
	}
}
