/*

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		2013/12/01 , Created by dennis
}}IS_NOTE

Copyright (C) 2013 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
package org.zkoss.zss.model.impl;

import org.model.AutoRollbackConnection;
import org.model.DBContext;
import org.model.DBHandler;
import org.zkoss.lang.Objects;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zss.model.*;
import org.zkoss.zss.model.impl.sys.DependencyTableAdv;
import org.zkoss.zss.model.impl.sys.formula.ParsingBook;
import org.zkoss.zss.model.sys.BookBindings;
import org.zkoss.zss.model.sys.EngineFactory;
import org.zkoss.zss.model.sys.dependency.DependencyTable;
import org.zkoss.zss.model.sys.dependency.Ref;
import org.zkoss.zss.model.sys.dependency.Ref.RefType;
import org.zkoss.zss.model.sys.formula.EvaluationContributor;
import org.zkoss.zss.model.sys.formula.FormulaClearContext;
import org.zkoss.zss.model.util.CellStyleMatcher;
import org.zkoss.zss.model.util.FontMatcher;
import org.zkoss.zss.model.util.Strings;
import org.zkoss.zss.model.util.Validations;
import org.zkoss.zss.range.impl.StyleUtil;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * @author dennis
 * @since 3.5.0
 */
public class BookImpl extends AbstractBookAdv{
	/**
	 * the sheet which is destroying now.
	 */
	/*package*/ final static ThreadLocal<SSheet> destroyingSheet = new ThreadLocal<SSheet>();
	private static final Logger logger = Logger.getLogger(BookImpl.class.getName());
	private static final long serialVersionUID = 1L;
	private final static Random _random = new Random(System.currentTimeMillis());
	private final static AtomicInteger _bookCount = new AtomicInteger();
	private final List<AbstractSheetAdv> _sheets = new ArrayList<AbstractSheetAdv>();
	private final List<SCellStyle> _cellStyles = new ArrayList<SCellStyle>();
	private final Map<String, SNamedStyle> _namedStyles = new HashMap<String, SNamedStyle>(); //ZSS-854
	private final List<SCellStyle> _defaultCellStyles = new ArrayList<SCellStyle>(); //ZSS-854
	private final List<AbstractFontAdv> _fonts = new ArrayList<AbstractFontAdv>();
	private final AbstractFontAdv _defaultFont;
	private final HashMap<AbstractColorAdv,AbstractColorAdv> _colors = new LinkedHashMap<AbstractColorAdv,AbstractColorAdv>();
	private final HashMap<String,AtomicInteger> _objIdCounter = new HashMap<>();
	private final int _maxRowSize = Integer.MAX_VALUE;
	private final int _maxColumnSize = Integer.MAX_VALUE;
	private boolean schemaPresent = false;
	private String _bookName;
	private String _shareScope;
	private SBookSeries _bookSeries;
	private List<AbstractNameAdv> _names;
	private String _bookId;
	private EventListenerAdaptor _listeners;
	private EventListenerAdaptor _queueListeners;
	private HashMap<String,Object> _attributes;
	private EvaluationContributor _evalContributor;
	private ArrayList<SPictureData> _picDatas; //since 3.6.0
	private boolean _dirty = false;
	//ZSS-855
	private HashMap<String, STable> _tables; //since 3.8.0
	
	public BookImpl(String bookName){
		Validations.argNotNull(bookName);
		this._bookName = bookName;
		_bookSeries = new SimpleBookSeriesImpl(this);
		_fonts.add(_defaultFont = new FontImpl());
		initDefaultCellStyles();
		_colors.put(ColorImpl.WHITE,ColorImpl.WHITE);
		_colors.put(ColorImpl.BLACK,ColorImpl.BLACK);
		_colors.put(ColorImpl.RED,ColorImpl.RED);
		_colors.put(ColorImpl.GREEN,ColorImpl.GREEN);
		_colors.put(ColorImpl.BLUE,ColorImpl.BLUE);
		
		_bookId = ((char)('a'+_random.nextInt(26))) + Long.toString(System.currentTimeMillis()+_bookCount.getAndIncrement(), Character.MAX_RADIX) ;
		_tables = new HashMap<String, STable>(0);
		//zekun.fan@gmail.com added bindings
		//BookBindings.put(bookName, this);
	}

    public BookImpl(String bookName, String bookId){
        Validations.argNotNull(bookName);
        this._bookName = bookName;
        _bookSeries = new SimpleBookSeriesImpl(this);
        _fonts.add(_defaultFont = new FontImpl());
        initDefaultCellStyles();
        _colors.put(ColorImpl.WHITE,ColorImpl.WHITE);
        _colors.put(ColorImpl.BLACK,ColorImpl.BLACK);
        _colors.put(ColorImpl.RED,ColorImpl.RED);
        _colors.put(ColorImpl.GREEN,ColorImpl.GREEN);
        _colors.put(ColorImpl.BLUE,ColorImpl.BLUE);

        _bookId = bookId;
        _tables = new HashMap<String, STable>(0);
        //zekun.fan@gmail.com added bindings
        //BookBindings.put(bookName, this);
    }

	public static SBook getBookById(String bookId){
        String getBookEntry = "SELECT bookname FROM books WHERE booktable = ?";
        try (AutoRollbackConnection connection = DBHandler.instance.getConnection();
             PreparedStatement getBookStmt = connection.prepareStatement(getBookEntry)) {
            getBookStmt.setString(1, bookId);
            ResultSet rs = getBookStmt.executeQuery();
            if(rs.next()) {
                String bookName = rs.getString(1);
                if (BookBindings.contains(bookName))
                    return BookBindings.get(bookName);
                else {
                    SBook book = new BookImpl(bookName, bookId);
                    if (!book.setNameAndLoad(bookName, bookId)){
                        return null;
                    }
                    BookBindings.put(bookName, book);
                    return book;
                }

            }
            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

	public static void deleteBook(String bookName, String bookTable) {
		String deleteBookEntry = "DELETE FROM books WHERE booktable = ?";
		try (AutoRollbackConnection connection = DBHandler.instance.getConnection();
			 PreparedStatement deleteBookStmt = connection.prepareStatement(deleteBookEntry)) {
			deleteBookStmt.setString(1, bookTable);
			deleteBookStmt.execute();
			connection.commit();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void initDefaultCellStyles() {
		AbstractCellStyleAdv defaultCellStyle = new CellStyleImpl(_defaultFont);
		_cellStyles.add(defaultCellStyle); //ZSS-854
		_defaultCellStyles.add(defaultCellStyle); //ZSS-854
	}
	
	@Override
	public SBookSeries getBookSeries(){
		return _bookSeries;
	}

	@Override
	void setBookSeries(SBookSeries bookSeries) {
		this._bookSeries = bookSeries;
	}

	@Override
	public String getBookName(){
		return _bookName;
	}

	@Override
	public boolean setBookName(String bookName) {
		BookBindings.remove(this._bookName);
		BookBindings.put(bookName, this);
		if (schemaPresent)
		{
			String updateBookName = "UPDATE books SET bookname = ? WHERE booktable = ?";
			try (AutoRollbackConnection connection = DBHandler.instance.getConnection();
				 PreparedStatement updateBookNameStmt = connection.prepareStatement(updateBookName))
			{
				updateBookNameStmt.setString(1, bookName);
				updateBookNameStmt.setString(2, _bookId);
				updateBookNameStmt.execute();
				connection.commit();
			}
			catch (SQLException e)
			{
				return false;
			}
			this._bookName = bookName;
		}
		else {
			this._bookName = bookName;
			checkDBSchema();
		}
		return true;
	}

	@Override
	public void checkDBSchema() {
		if (schemaPresent)
			return;
		String bookTable = getId();

		try (AutoRollbackConnection connection = DBHandler.instance.getConnection();
			 Statement stmt = connection.createStatement()) {
			DBContext dbContext = new DBContext(connection);

			//add check bookname
			String checkBook = "SELECT bookname FROM books WHERE bookname like ?" +
					" AND LENGTH(bookname) = (SELECT MAX(LENGTH(bookname))" +
					" FROM books WHERE bookname like ?) LIMIT 1";
			PreparedStatement checkBookStmt = connection.prepareStatement(checkBook);
			checkBookStmt.setString(1, getBookName() + "%");
			checkBookStmt.setString(2, getBookName() + "%");
			ResultSet rs = checkBookStmt.executeQuery();
			if(rs.next()) {
				_bookName = rs.getString(1) + "_";
				BookBindings.put(_bookName, this);
			}
			checkBookStmt.close();

			String insertBook = "INSERT INTO books(bookname, booktable, lastmodified, createdtime) VALUES (?, ?, now(), now())";
			PreparedStatement insertBookStmt = connection.prepareStatement(insertBook);
			insertBookStmt.setString(1, getBookName());
			insertBookStmt.setString(2, getId());
			insertBookStmt.execute();
            insertBookStmt.close();

			String insertSheets = "INSERT INTO sheets VALUES(?, ?, ?, ?, ?, ?)";
			PreparedStatement insertSheetStmt = connection.prepareStatement(insertSheets);
			for (SSheet sheet:getSheets()) {
				String modelName = bookTable + sheet.getDBId();
				sheet.createModel(dbContext, modelName);
				insertSheetStmt.setString(1, getId());
				insertSheetStmt.setInt(2, sheet.getDBId());
				insertSheetStmt.setInt(3, _sheets.indexOf(sheet));
				insertSheetStmt.setString(4, sheet.getBook().getBookName());
				insertSheetStmt.setString(5, sheet.getSheetName());
				insertSheetStmt.setString(6, modelName);
				insertSheetStmt.execute();
			}
			insertSheetStmt.close();
			connection.commit();
			schemaPresent = true;
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}

	}
	
	@Override
	public SSheet getSheet(int i){
		return _sheets.get(i);
	}
	
	@Override
	public int getNumOfSheet(){
		return _sheets.size();
	}
	
	@Override
	public SSheet getSheetByName(String name){
		for(SSheet sheet:_sheets){
			if(sheet.getSheetName().equalsIgnoreCase(name)){
				return sheet;
			}
		}
		return null;
	}
	
	@Override
	public SSheet getSheetById(String id){
		for(SSheet sheet:_sheets){
			if(sheet.getId().equals(id)){
				return sheet;
			}
		}
		return null;
	}

	protected void checkOwnership(SSheet sheet){
		if(!_sheets.contains(sheet)){
			throw new IllegalStateException("doesn't has ownership "+ sheet);
		}
	}

	protected void checkOwnership(SName name){
		if(_names==null || !_names.contains(name)){
			throw new IllegalStateException("doesn't has ownership "+ name);
		}
	}

	@Override
	public void sendModelEvent(ModelEvent event){
		if(_listeners!=null){
			_listeners.sendModelEvent(event);
		}

		if(_queueListeners!=null &&
			// System thread doesn't have execution so that it will throw IllegalStateException
			// e.g. Background Thread created by Executor
			Executions.getCurrent() != null){
			_queueListeners.sendModelEvent(event);
		}

		if(!ModelEvents.isCustomEvent(event)) {
			if(!_dirty) {
				_dirty = true;
				// ZSS-942, By Jerry 2015/3/5
				// ATTENTION: ModelEvents.ON_MODEL_DIRTY_CHANGE is a custom event.
				// Dirty change is a special case for calling sendModelEvent inside model.
				// In normal model event case, we should do it in Range level.
				sendModelEvent(ModelEvents.createModelEvent(ModelEvents.ON_MODEL_DIRTY_CHANGE, event.getBook(), event.getSheet(),
						ModelEvents.createDataMap(ModelEvents.PARAM_CUSTOM_DATA, _dirty)));
			}
		}
	}
	
	private SSheet createExistingSheet(String name, int dbId) {
		AbstractSheetAdv sheet = new SheetImpl(this,nextObjId("sheet"));
		sheet.setDBId(dbId);
		sheet.setSheetName(name, false);
		_sheets.add(sheet);

		//create formula cache for any sheet, sheet name, position change
		EngineFactory.getInstance().createFormulaEngine().clearCache(new FormulaClearContext(this));

		ModelUpdateUtil.handlePrecedentUpdate(getBookSeries(), sheet, new RefImpl(sheet, -1));
		return sheet;
	}
	
	@Override
	public SSheet createSheet(String name) {
		return createSheet(name,null);
	}

	@Override
	String nextObjId(String type){
		StringBuilder sb = new StringBuilder(_bookId);
		sb.append("_").append(type).append("_");
		AtomicInteger i = _objIdCounter.get(type);
		if(i==null){
			_objIdCounter.put(type, i = new AtomicInteger(0));
		}
		sb.append(i.getAndIncrement());
		return sb.toString();
	}

	int nextIntObjId(String type){
		AtomicInteger i = _objIdCounter.get(type);
		if(i==null){
			_objIdCounter.put(type, i = new AtomicInteger(0));
		}
		return i.getAndIncrement();
	}

	@Override
	public SSheet createSheet(String name,SSheet src) {
		checkLegalSheetName(name);
		if(src!=null)
			checkOwnership(src);


		AbstractSheetAdv sheet = new SheetImpl(this,nextObjId("sheet"));
		sheet.setSheetName(name, false);
		_sheets.add(sheet);

		// Update to DB
		if (hasSchema())
		{
			String bookTable=getId();
			String insertSheets = "INSERT INTO sheets " +
					" SELECT ?, max(sheetid) +  1, ?, ?, ?, '" + bookTable + "' || (max(sheetid) +  1)" +
					" FROM sheets " +
					" WHERE booktable = ? " +
					" RETURNING sheetid";
			try (AutoRollbackConnection connection = DBHandler.instance.getConnection();
				 PreparedStatement stmt = connection.prepareStatement(insertSheets))
			{
				stmt.setString(1, getId());
				stmt.setInt(2,_sheets.indexOf(sheet));
				stmt.setString(3, getBookName());
				stmt.setString(4, sheet.getSheetName());
				stmt.setString(5, getId());
				ResultSet rs = stmt.executeQuery();
				if (rs.next())
					sheet.setDBId(rs.getInt("sheetid"));
				rs.close();
				DBContext dbContext = new DBContext(connection);
				String modelName = bookTable + sheet.getDBId();
				if (src==null)
                	sheet.createModel(dbContext, modelName);
				else
					sheet.cloneModel(dbContext, modelName, src);
				connection.commit();
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			sheet.setDBId(nextIntObjId("dbsheet"));
		}


		//TODO: Mangesh -- Fix the code below
		if(src instanceof AbstractSheetAdv){
			((AbstractSheetAdv)src).copyTo(sheet, null, false);

			if (hasSchema()) {
				String bookTable=getId();
				String insertSheets = "INSERT INTO " + bookTable + "_sheetdata " +
						" (sheetid, row, col, value) " +
						" SELECT ?, row, col, value " +
						" FROM " +  bookTable + "_sheetdata " +
						" WHERE sheetid = ? ";
				try (AutoRollbackConnection connection = DBHandler.instance.getConnection();
					 PreparedStatement stmt = connection.prepareStatement(insertSheets))
				{
					stmt.setInt(1, sheet.getDBId());
					stmt.setInt(2, src.getDBId());
					stmt.execute();
					connection.commit();
				}
				catch (SQLException e)
				{
					e.printStackTrace();
				}
			}
		}

		//create formula cache for any sheet, sheet name, position change
		EngineFactory.getInstance().createFormulaEngine().clearCache(new FormulaClearContext(this));

		ModelUpdateUtil.handlePrecedentUpdate(getBookSeries(), sheet, new RefImpl(sheet, -1));

		return sheet;
	}

	protected Ref getRef() {
		return new RefImpl(this);
	}
	
	@Override
	public void setSheetName(SSheet sheet, String newname) {
		checkLegalSheetName(newname);
		checkOwnership(sheet);

		int index = getSheetIndex(sheet);
		String oldname = sheet.getSheetName();
		((AbstractSheetAdv) sheet).setSheetName(newname, true);

		//create formula cache for any sheet, sheet name, position change
		EngineFactory.getInstance().createFormulaEngine().clearCache(new FormulaClearContext(this));

		ModelUpdateUtil.handlePrecedentUpdate(getBookSeries(), (AbstractSheetAdv) sheet, new RefImpl(this.getBookName(),newname, index));//to clear the cache of formula that has unexisted name
		ModelUpdateUtil.handlePrecedentUpdate(getBookSeries(), (AbstractSheetAdv) sheet, new RefImpl(this.getBookName(),oldname, index));

		renameSheetFormula(oldname,newname,index);
	}

	private void renameSheetFormula(String oldName, String newName, int index){
		AbstractBookSeriesAdv bs = (AbstractBookSeriesAdv)getBookSeries();
		DependencyTable dt = bs.getDependencyTable();
		Set<Ref> dependents = dt.getDirectDependents(new RefImpl(getBookName(),oldName,index));
		if(dependents.size()>0){

			//clear the dependents dependency before rename it's sheet name
			for(Ref dependent:dependents){
				dt.clearDependents(dependent);
			}

			//rebuild the the formula by tuner
			FormulaTunerHelper tuner = new FormulaTunerHelper(bs);
			tuner.renameSheet(this,oldName,newName,dependents);
		}
	}
	
	private void checkLegalSheetName(String name) {
		if(Strings.isBlank(name)){
			throw new InvalidModelOpException("sheet name '"+name+"' is not legal");
		}
		if(getSheetByName(name)!=null){
			throw new InvalidModelOpException("sheet name '"+name+"' is duplicated");
		}
	}

	private void checkLegalNameName(String name,String sheetName) {
		if(Strings.isBlank(name)){
			throw new InvalidModelOpException("name '"+name+"' is not legal");
		}
		//ZSS-966
		if (getTable(name) != null) {
			throw new InvalidModelOpException("name '"+name+"' is duplicated with Table name");
		}
		if(getNameByName(name,sheetName)!=null){ //must be unique in the scope
			throw new InvalidModelOpException("name '"+name+"' "+(sheetName==null?"":" in '"+sheetName+"'")+" is duplicated");
		}
		if(sheetName!=null && getSheetByName(sheetName)==null){
			throw new InvalidModelOpException("no such sheet "+sheetName);
		}
		//ZSS-660: valid name
		//@see  http://office.microsoft.com/en-us/excel-help/define-and-use-names-in-formulas-HA010147120.aspx
		//length must less than or equals to 255
		if (name.length() > 255) {
			throw new InvalidModelOpException("name '"+name+"' is not legal: cannot exceed 255 characters");
		}

		//1st character must be a letter, underscore, or backslash
		char c1 = name.charAt(0);
		if (!Character.isLetter(c1) && c1 != '_' && c1 != '\\') {
			throw new InvalidModelOpException("name '"+name+"' is not legal: first character must be a letter, an underscore, or a backslash");
		}

		boolean invalid = c1 == '_' || c1 == '\\' || c1 == '?' || c1 == '.'; //impossible be a valid cell reference
		int colIndex = invalid ? -2 : Character.getNumericValue(c1) - 9;
		if (!invalid) {
			invalid = colIndex < 0;
		}
		int rowIndex = -1;
		//remaining characters must be letters, digits, periods, or underscores.
		for (int j = 1, len = name.length(); j < len; ++j) {
			char ch = name.charAt(j);
			if (Character.isLetter(ch)) { //analyze colIndex
				if (invalid) continue;
				if (rowIndex >= 0) { //letter -> digit -> letter
					invalid = true;
					continue;
				}
				int c = Character.getNumericValue(ch) - 9;
				if (c < 0) {
					invalid = true;
					continue;
				}
				colIndex = colIndex * 26 + c;
			} else if (Character.isDigit(ch)) { //analyze rowIndex
				if (invalid) continue;
				if (rowIndex < 0) {
					rowIndex = Character.getNumericValue(ch);
				} else {
					rowIndex = rowIndex * 10 + Character.getNumericValue(ch);
				}
			} else if (ch != '.' && ch != '_' && ch != '?' && ch != '\\') {
				throw new InvalidModelOpException("name '"+name+"' is not legal: the character '"+ ch+ "' at index "+ j + " must be a letter, a digit, an underscore, a period, a question mark, or a backslash");
			} else { //ZSS-792
				invalid = true; // '.' or '-' or '?' or '\', impossible to be a valid cell reference
			}
		}

		//cannot be a valid cell reference address
		if (!invalid && colIndex >= 0 && colIndex <= getMaxColumnSize() && rowIndex >= 0 && rowIndex < getMaxRowSize()) {
			throw new InvalidModelOpException("name '"+name+"' is not legal: cannot be a cell reference");
		}

		//cannot be 'C' or 'R'
		if (name.equalsIgnoreCase("C") || name.equalsIgnoreCase("R")) {
			throw new InvalidModelOpException("name '"+name+"' is not legal: cannot be 'C', 'c', 'R', or 'r'");
		}
	}

	@Override
	public void deleteSheet(SSheet sheet) {
		checkOwnership(sheet);

		final String bookName = sheet.getBook().getBookName();

		destroyingSheet.set(sheet);
		try{
			((AbstractSheetAdv)sheet).destroy();
		}finally{
			destroyingSheet.set(null);
		}
		String oldName = sheet.getSheetName();
		int index = _sheets.indexOf(sheet);
		_sheets.remove(index);

		//create formula cache for any sheet, sheet name, position change
		EngineFactory.getInstance().createFormulaEngine().clearCache(new FormulaClearContext(this));

//		sendModelInternalEvent(ModelInternalEvents.createModelInternalEvent(ModelInternalEvents.ON_SHEET_DELETED,
//				this,ModelInternalEvents.createDataMap(ModelInternalEvents.PARAM_SHEET_OLD_INDEX, index)));

		ModelUpdateUtil.handlePrecedentUpdate(getBookSeries(), (AbstractSheetAdv) sheet, new RefImpl(this.getBookName(),sheet.getSheetName(), index));

		renameSheetFormula(oldName, null, index);

		//ZSS-815
		// adjust sheet index
		adjustSheetIndex(bookName, index);
		// update DB

		// Update to DB
		if (hasSchema())
		{
			String bookTable=getId();
			String deleteSheet = "DELETE FROM sheets WHERE booktable = ? AND sheetid = ?";
			try (AutoRollbackConnection connection = DBHandler.instance.getConnection();
				 PreparedStatement deleteSheetstmt = connection.prepareStatement(deleteSheet))
			{
				deleteSheetstmt.setString(1, getId());
				deleteSheetstmt.setInt(2,sheet.getDBId());
				deleteSheetstmt.execute();
				DBContext dbContext = new DBContext(connection);
				sheet.deleteModel(dbContext);
				connection.commit();
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
		}
	}

	//ZSS-815
	private void adjustSheetIndex(String bookName, int index) {
		AbstractBookSeriesAdv bs = (AbstractBookSeriesAdv)getBookSeries();
		DependencyTableAdv dt = (DependencyTableAdv) bs.getDependencyTable();
		dt.adjustSheetIndex(bookName, index, -1);
	}

	@Override
	public void moveSheetTo(SSheet sheet, int index) {
		checkOwnership(sheet);
		if(index<0|| index>=_sheets.size()){
			throw new InvalidModelOpException("new position out of bound "+_sheets.size() +"<>" +index);
		}
		int oldindex = _sheets.indexOf(sheet);
		if(oldindex==index){
			return;
		}

		//ZSS-820
		reorderSheetFormula(getSheet(oldindex).getSheetName(), oldindex, index);
		_sheets.remove(oldindex);
		_sheets.add(index, (AbstractSheetAdv)sheet);

		//Update to DB
		if (hasSchema()) {
			String bookTable = getId();
			String updateSheetIndex = "UPDATE sheets " +
					" SET sheetindex = ? WHERE booktable = ? AND sheetid = ?";
			try (AutoRollbackConnection connection = DBHandler.instance.getConnection();
				 PreparedStatement updateSheetIndexStmt = connection.prepareStatement(updateSheetIndex)) {
				for (SSheet s:_sheets)
				{
					updateSheetIndexStmt.setInt(1, _sheets.indexOf(s));
					updateSheetIndexStmt.setString(2, getId());
					updateSheetIndexStmt.setInt(3, s.getDBId());
					updateSheetIndexStmt.execute();
				}
				connection.commit();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		//create formula cache for any sheet, sheet name, position change
		EngineFactory.getInstance().createFormulaEngine().clearCache(new FormulaClearContext(this));

		ModelUpdateUtil.handlePrecedentUpdate(getBookSeries(), (AbstractSheetAdv) sheet, new RefImpl(this.getBookName(),sheet.getSheetName(), index));
		//ZSS-1049: should consider formulas that referred to the old index
		ModelUpdateUtil.handlePrecedentUpdate(getBookSeries(), (AbstractSheetAdv) sheet, new RefImpl(this.getBookName(),sheet.getSheetName(), oldindex));

		// adjust sheet index
		moveSheetIndex(getBookName(), oldindex, index);
	}

	//ZSS-820
	private void moveSheetIndex(String bookName, int oldIndex, int newIndex) {
		AbstractBookSeriesAdv bs = (AbstractBookSeriesAdv)getBookSeries();
		DependencyTableAdv dt = (DependencyTableAdv) bs.getDependencyTable();
		dt.moveSheetIndex(bookName, oldIndex, newIndex);
	}

	public void dump(StringBuilder builder) {
		for(AbstractSheetAdv sheet:_sheets){
			if(sheet instanceof SheetImpl){
				((SheetImpl)sheet).dump(builder);
			}else{
				builder.append("\n").append(sheet);
			}
		}
	}

	@Override
	public SCellStyle getDefaultCellStyle() {
		return getDefaultCellStyle(0);
	}

	@Override
	public void setDefaultCellStyle(SCellStyle cellStyle) {
		if (cellStyle == null) return;
		AbstractCellStyleAdv defaultCellStyle = (AbstractCellStyleAdv) cellStyle;
		_defaultCellStyles.set(0, defaultCellStyle);
		_cellStyles.set(0, defaultCellStyle);
	}

	//ZSS-854
	@Override
	public SCellStyle getDefaultCellStyle(int index) {
		return _defaultCellStyles.get(index);
	}

	@Override
	public SCellStyle createCellStyle(boolean inStyleTable) {
		return createCellStyle(null,inStyleTable);
	}

	@Override
	public SCellStyle createCellStyle(SCellStyle src,boolean inStyleTable) {
		if(src!=null){
			Validations.argInstance(src, AbstractCellStyleAdv.class);
		}
		AbstractCellStyleAdv style = new CellStyleImpl(_defaultFont);
		if(src!=null){
			style.copyFrom(src);
		}

		if(inStyleTable){
			_cellStyles.add(style);
		}

		return style;
	}
	
	@Override
	public SCellStyle searchCellStyle(CellStyleMatcher matcher) {
		for(SCellStyle style:_cellStyles){
			if(matcher.match(style)){
				return style;
			}
		}
		return null;
	}

	@Override
	public SFont getDefaultFont() {
		return _defaultFont;
	}

	@Override
	public SFont createFont(boolean inFontTable) {
		return createFont(null,inFontTable);
	}

	@Override
	public SFont createFont(SFont src,boolean inFontTable) {
		if(src!=null){
			Validations.argInstance(src, AbstractFontAdv.class);
		}
		AbstractFontAdv font = new FontImpl();
		if(src!=null){
			font.copyFrom(src);
		}

		if(inFontTable){
			_fonts.add(font);
		}

		return font;
	}
	
	@Override
	public SFont searchFont(FontMatcher matcher) {
		for(SFont font:_fonts){
			if(matcher.match(font)){
				return font;
			}
		}
		return null;
	}

	@Override
	public int getMaxRowSize() {
		return _maxRowSize;
	}

	@Override
	public int getMaxColumnSize() {
		return _maxColumnSize;
	}

	@Override
	public void optimizeCellStyle() {
		HashMap<String,SCellStyle> stylePool = new LinkedHashMap<String,SCellStyle>();
		_cellStyles.clear();
		_fonts.clear();

		SCellStyle defaultStyle = getDefaultCellStyle();
		SFont defaultFont = getDefaultFont();
		stylePool.put(((AbstractCellStyleAdv)defaultStyle).getStyleKey(), defaultStyle);

		for(SSheet sheet:_sheets) {
			Iterator<SRow> rowIter = sheet.getRowIterator();
			while(rowIter.hasNext()){
				SRow row = rowIter.next();

				row.setCellStyle(hitStyle(defaultStyle,row.getCellStyle(),stylePool));
				Iterator<SCell> cellIter = sheet.getCellIterator(row.getIndex());
				while(cellIter.hasNext()){
					SCell cell = cellIter.next();
					cell.setCellStyle(hitStyle(defaultStyle,cell.getCellStyle(),stylePool));
				}
			}
			Iterator<SColumnArray> colIter = sheet.getColumnArrayIterator();
			while(colIter.hasNext()){
				SColumnArray colarr = colIter.next();
				colarr.setCellStyle(hitStyle(defaultStyle,colarr.getCellStyle(),stylePool));
			}
		}

		_cellStyles.addAll(stylePool.values());
		String key;
		HashMap<String,SFont> fontPool = new LinkedHashMap<String,SFont>();

		fontPool.put(((AbstractFontAdv)defaultFont).getStyleKey(), defaultFont);
		for(SCellStyle style:_cellStyles){
			SFont font = style.getFont();
			key = ((AbstractFontAdv)font).getStyleKey();
			if(fontPool.get(key)==null){
				fontPool.put(key, font);
			}
		}

		_fonts.addAll((Collection)fontPool.values());

		_colors.clear();//color is immutable, just clear it.
	}

	@SuppressWarnings("unchecked")
	public List<SCellStyle> getCellStyleTable(){
		return Collections.unmodifiableList((List)_cellStyles);
	}

	@SuppressWarnings("unchecked")
	public List<SFont> getFontTable(){
		return Collections.unmodifiableList((List)_fonts);
	}

	private SCellStyle hitStyle(SCellStyle defaultStyle,SCellStyle currSytle,
			HashMap<String, SCellStyle> stylePool) {
		String key;
		SCellStyle hit;
		if(currSytle==defaultStyle){//quick case for most cell use default style
			return defaultStyle;
		}else{
			key = ((AbstractCellStyleAdv)currSytle).getStyleKey();
			hit = stylePool.get(key);
			if(hit==null){
				stylePool.put(key, hit = currSytle);
			}
		}
		return hit;
	}

	@Override
	public void addEventListener(ModelEventListener listener){
		if(listener instanceof EventQueueModelEventListener){
			if(_queueListeners==null){
				String scope = getShareScope();
				if(scope==null){
					scope = "desktop";//default desktop
				}
				_queueListeners = new EventQueueListenerAdaptor(scope,getId());
			}
			_queueListeners.addEventListener(listener);
		}else{
			if(_listeners==null){
				_listeners = new DirectEventListenerAdaptor();
			}
			_listeners.addEventListener(listener);
		}
	}

	@Override
	public void removeEventListener(ModelEventListener listener){
		if(listener instanceof EventQueueModelEventListener && _queueListeners!=null){
			_queueListeners.removeEventListener(listener);
			if(_queueListeners.size()==0){
				_queueListeners = null;//clean up, so user can change share-scope then.
			}
		}else if(_listeners!=null){
			_listeners.removeEventListener(listener);
		}
	}

	@Override
	public Object getAttribute(String name) {
		return _attributes==null?null:_attributes.get(name);
	}

	@Override
	public Object setAttribute(String name, Object value) {
		if(_attributes==null){
			_attributes = new HashMap<String, Object>();
		}
		return _attributes.put(name, value);
	}

	@Override
	public Map<String, Object> getAttributes() {
		return _attributes==null?Collections.EMPTY_MAP:Collections.unmodifiableMap(_attributes);
	}

	@Override
	public SColor createColor(byte r, byte g, byte b) {
		AbstractColorAdv newcolor = new ColorImpl(r,g,b);
		AbstractColorAdv color = _colors.get(newcolor);//reuse the existed color object
		if(color==null){
			_colors.put(newcolor, color = newcolor);
		}
		return color;
	}

	@Override
	public SColor createColor(String htmlColor) {
		AbstractColorAdv newcolor = new ColorImpl(htmlColor);
		AbstractColorAdv color = _colors.get(newcolor);//reuse the existed color object
		if(color==null){
			_colors.put(newcolor, color = newcolor);
		}
		return color;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public List<SSheet> getSheets() {
		return Collections.unmodifiableList((List)_sheets);
	}

	@Override
	public SName createName(String namename) {
		return createName(namename,null);
	}

	@Override
	public SName createName(String namename,String sheetName) {
		checkLegalNameName(namename,sheetName);

		AbstractNameAdv name = new NameImpl(this,nextObjId("name"),namename,sheetName);

		if(_names==null){
			_names = new ArrayList<AbstractNameAdv>();
		}

		_names.add(name);
		return name;
	}

	@Override
	public void setNameName(SName name, String newname) {
		setNameName(name,newname,null);
	}

	public void setNameName(SName name, String newname, String sheetName) {
		checkLegalNameName(newname,sheetName);
		checkOwnership(name);

		//create formula cache for name, currently, we can just clear all of book.
		EngineFactory.getInstance().createFormulaEngine().clearCache(new FormulaClearContext(this));

		//notify the (old) name is change before update name
		//TODO - Fix the below line by finding a sheet instance
		//ModelUpdateUtil.handlePrecedentUpdate(getBookSeries(), new NameRefImpl((AbstractNameAdv)name));

		final String oldName = name.getName(); // ZSS-661

		//ZSS-966: notify the (old) table name is change before update name
		if (name instanceof TableNameImpl) {
			//TODO - Fix the below line by finding a sheet instance
			//ModelUpdateUtil.handlePrecedentUpdate(getBookSeries(), new TablePrecedentRefImpl(this.getBookName(), oldName));
		}

		((AbstractNameAdv)name).setName(newname,sheetName); //will change Table's name if the name is a TableName
		//don't need to notify new name precedent update, since Name handle it itself

		//Rename formula that contains this name
		renameNameFormula(name, oldName, newname, sheetName); // ZSS-661

		//Rename formula that contains this table name
		if (name instanceof TableNameImpl) {
			//ZSS-966: reput Table; must do this first or renameTableName()
			//cannot find the correct table
			STable tb = removeTable(oldName);
			if (tb != null)
				addTable(tb);

			renameTableNameFormula(name, oldName, newname);
		}
	}

	private void renameNameFormula(SName name, String oldName, String newName, String sheetName) {
		AbstractBookSeriesAdv bs = (AbstractBookSeriesAdv)getBookSeries();
		FormulaTunerHelper tuner = new FormulaTunerHelper(bs);
		DependencyTable dt = bs.getDependencyTable();
		final
		Ref ref = new NameRefImpl(name.getBook().getBookName(),name.getApplyToSheetName(), oldName); //old name
		Set<Ref> dependents = dt.getDirectDependents(ref);
		if(dependents.size()>0){
			//clear the dependents dependency before rename it's Name name
			for(Ref dependent:dependents){
				dt.clearDependents(dependent);
			}

			int sheetIndex = sheetName == null ? -1 : getSheetIndex(sheetName);
			//rebuild the the formula by tuner
			tuner.renameName(this, oldName, newName, dependents, sheetIndex);
		}
	}

	@Override
	public void deleteName(SName name) {
		checkOwnership(name);

		((AbstractNameAdv)name).destroy();

		int index = _names.indexOf(name);
		_names.remove(index);

//		sendEvent(ModelEvents.ON_NAME_DELETED,
//				ModelEvents.PARAM_NAME, sheet,
//				ModelEvents.PARAM_SHEET_OLD_INDEX, index);
	}

	@Override
	public int getNumOfName() {
		return _names==null?0:_names.size();
	}

	@Override
	public SName getName(int idx) {
		if(_names==null){
			throw new ArrayIndexOutOfBoundsException(idx);
		}
		return _names.get(idx);
	}

	@Override
	public SName getNameByName(String namename) {
		return getNameByName(namename,null);
	}

	public SName getNameByName(String namename, String sheetName) {
		if(_names==null)
			return null;
		for(SName name:_names){
			//ZSS-436
			final String scopeSheetName = name.getApplyToSheetName();
			if ((sheetName == scopeSheetName || (sheetName != null && sheetName.equalsIgnoreCase(scopeSheetName)))
					&& name.getName().equalsIgnoreCase(namename)){
				return name;
			}
		}
		return null;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public List<SName> getNames() {
		return _names==null?Collections.EMPTY_LIST:Collections.unmodifiableList((List)_names);
	}

	@Override
	public int getSheetIndex(SSheet sheet) {
		return _sheets.indexOf(sheet);
	}

	@Override
	public int getSheetIndex(String sheetName) {
		int i=0;
		for(SSheet sheet:_sheets){
			if(sheet.getSheetName().equals(sheetName)){
				return i;
			}
			i++;
		}
		return -1;
	}

	@Override
	public String getShareScope() {
		return _shareScope;
	}

	@Override
	public void setShareScope(String scope) {
		if(!Objects.equals(this._shareScope,scope)){

			if("disable".equals(scope)){
				if(_listeners!=null){
					_listeners.clear();
				}
				if(_queueListeners!=null){
					_queueListeners.clear();
				}
				return;
			}

			if(_queueListeners!=null && _queueListeners.size()>0){
				throw new IllegalStateException("can't change share scope after registed any queue model event listener");
			}

			this._shareScope = scope;
		}
	}

	@Override
	public EvaluationContributor getEvaluationContributor() {
		return _evalContributor;
	}

	@Override
	public void setEvaluationContributor(EvaluationContributor contributor) {
		this._evalContributor = contributor;
	}

	@Override
	public int getMaxRowIndex() {
		return getMaxRowSize()-1;
	}

	@Override
	public int getMaxColumnIndex() {
		return getMaxColumnSize()-1;
	}

	@Override 
	public String getId(){
		return _bookId; 
	}

	@Override
	public boolean setNameAndLoad(String _bookName, String _bookId){
		this._bookName = _bookName;
        this._bookId = _bookId;
		this._sheets.clear();

		// Load Schema
		//String bookTable = getId();
		logger.info("Loading " + getBookName());

		String bookQuery = "UPDATE books SET lastmodified = now() WHERE booktable = ? RETURNING booktable";
		String sheetsQuery = "SELECT * FROM sheets WHERE booktable = ? ORDER BY sheetindex";

		try (AutoRollbackConnection connection = DBHandler.instance.getConnection();
			 PreparedStatement bookStmt = connection.prepareStatement(bookQuery);
			 PreparedStatement sheetsStmt = connection.prepareStatement(sheetsQuery)) {


			bookStmt.setString(1, _bookId);
			ResultSet rs = bookStmt.executeQuery();
			if (!rs.next()) {
			    logger.info(getBookName() + "does not exist");
			    rs.close();
			    return false;
            }
            rs.close();

			sheetsStmt.setString(1, getId());
			ResultSet rsSheets = sheetsStmt.executeQuery();
			while (rsSheets.next())
			{
				SSheet sheet = createExistingSheet(rsSheets.getString("sheetname"),
						rsSheets.getInt("sheetid"));
				sheet.setDataModel(rsSheets.getString("modelname"));
			}
			rsSheets.close();
			connection.commit();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			return false;
		}
		schemaPresent = true;
		return true;
	}

	@Override
	public boolean hasSchema() {
		return schemaPresent;
	}

	@Override
	public SPictureData addPictureData(SPicture.Format format, byte[] data) {
		if (_picDatas == null) {
			_picDatas = new ArrayList<SPictureData>(4);
		}
		int index = _picDatas.size();
		SPictureData picData = new PictureDataImpl(index, format, data);
		_picDatas.add(picData);
		return picData;
	}

	@Override
	public SPictureData getPictureData(int index) {
		if (index < 0 || _picDatas == null || index >= _picDatas.size())
			return null;
		return _picDatas.get(index);
	}

	@Override
	public Collection<SPictureData> getPicturesDatas() {
		if (_picDatas == null) return Collections.emptyList();
		
		final List<SPictureData> list = new ArrayList<SPictureData>(_picDatas.size());
		for (SPictureData picData : _picDatas) {
			if (picData != null) {
				list.add(picData);
			}
		}
		return list;
	}

	//ZSS-820
	private void reorderSheetFormula(String sheetName, int oldIndex, int newIndex) {
		AbstractBookSeriesAdv bs = (AbstractBookSeriesAdv)getBookSeries();
		DependencyTable dt = bs.getDependencyTable();
		Set<Ref> dependents = dt.getDirectDependents(new RefImpl(getBookName(),sheetName, oldIndex));
		if(dependents.size()>0){
			final String bookName = getBookName();
			final int low = oldIndex < newIndex ? oldIndex : newIndex;
			final int high = oldIndex < newIndex ? newIndex : oldIndex;

			Set<String> bookNames = new HashSet<String>();
			//filter out dependents that does not need to do reorder
			for(final Iterator<Ref> it = dependents.iterator(); it.hasNext();) {
				Ref dependent = it.next();
				Set<Ref> precedents = ((DependencyTableAdv)dt).getDirectPrecedents(dependent);
				boolean candidate = false;
				for (Ref p : precedents) {
					if (p.getType() != RefType.AREA && p.getType() != RefType.CELL)
						continue;
					final String bookName0 = p.getBookName();
					final String sheet1 = p.getSheetName();
					final String sheet2 = p.getLastSheetName();
					
					final int low0 = getSheetIndex(sheet1);
					int high0 = getSheetIndex(sheet2);
					if (high0 < 0) high0 = low0;
					if (low0 == high0) continue; // single sheet, as is.
							
					// no intersection; as is.
					if (high0 < low || low0 > high) continue;

					if (low0 == oldIndex) {
						if (low0 != high0 && newIndex >= high0) { //2. move beyond original high end
							//must change low end sheet name! (_map & _remap must be remapped)
							candidate = true;
							
							// adjust extern sheet name
							if (!bookNames.contains(bookName0)) {
								ParsingBook parsingBook = new ParsingBook(bs.getBook(bookName0));
								parsingBook.reorderSheet(bookName, oldIndex, newIndex);
								bookNames.add(bookName0);
							}
							break;
						}
					}
					
					if (high0 == oldIndex) {
						if (low0 != high0 && newIndex <= low0) { //4. move beyond original low end
							// high0 index not change but sheet name changed
							candidate = true;
							
							// adjust extern sheet name
							if (!bookNames.contains(bookName0)) {
								ParsingBook parsingBook = new ParsingBook(bs.getBook(bookName0));
								parsingBook.reorderSheet(bookName, oldIndex, newIndex);
								bookNames.add(bookName0);
							}
							break;
						}
					}
				}
				if (!candidate) {
					it.remove();
				}
			}

			if (!dependents.isEmpty()) {
				for (Ref dependent : dependents) {
					dt.clearDependents(dependent);
				}
			
				//rebuild the the formula by tuner
				FormulaTunerHelper tuner = new FormulaTunerHelper(bs);
				tuner.reorderSheet(this, oldIndex, newIndex, dependents);
			}
		}
	}
	
	//ZSS-854
	public SNamedStyle getNamedStyle(String name) {
		return _namedStyles.get(name);
	}

	//ZSS-854
	@Override
	public int addDefaultCellStyle(SCellStyle cellStyle) {
		_defaultCellStyles.add(cellStyle);
		return _defaultCellStyles.size() - 1;
	}
	
	//ZSS-854
	@Override
	public Collection<SCellStyle> getDefaultCellStyles() {
		return _defaultCellStyles;
	}

	//ZSS-854
	@Override
	public void addNamedCellstyle(SNamedStyle namedStyle) {
		_namedStyles.put(namedStyle.getName(), namedStyle);
	}

	//ZSS-854
	@Override
	public Collection<SNamedStyle> getNamedStyles() {
		return _namedStyles.values();
	}

	//ZSS-854
	@Override
	public void clearDefaultCellStyles() {
		_cellStyles.clear();
		_defaultCellStyles.clear();
	}

	//ZSS-854
	@Override
	public void clearNamedStyles() {
		_namedStyles.clear();		
	}

	//ZSS-923
	@Override
	public boolean isDirty() {
		return _dirty;
	}

	//ZSS-923
	@Override
	public void setDirty(boolean dirty) {
		_dirty = dirty;
	}
	
	//ZSS-855
	@Override
	public SName createTableName(STable table) {
		final String namename = table.getName();
		checkLegalNameName(namename, null);

		AbstractNameAdv name = new TableNameImpl(this,table,nextObjId("name"),namename);
		
		if(_names==null){
			_names = new ArrayList<AbstractNameAdv>();
		}
		
		_names.add(name);
		return name;
	}
	
	//ZSS-855
	@Override
	public void addTable(STable table) {
		_tables.put(table.getName().toUpperCase(), table);
	}

	//ZSS-855
	@Override
	public STable getTable(String name) {
		return _tables.get(name.toUpperCase());
	}
	
	//ZSS-855
	@Override
	public STable removeTable(String name) {
		final STable tb = _tables.remove(name.toUpperCase());
		//ZSS-988: should consider table filter
		if (tb != null) {
			((AbstractTableAdv)tb).refreshFilter();
		}
		return tb; 
	}
	
	//ZSS-966
	private void renameTableNameFormula(SName name, String oldName, String newName) {
		AbstractBookSeriesAdv bs = (AbstractBookSeriesAdv)getBookSeries();
		FormulaTunerHelper tuner = new FormulaTunerHelper(bs);
		DependencyTable dt = bs.getDependencyTable();
		final Ref ref = new TablePrecedentRefImpl(name.getBook().getBookName(), oldName); //old name
		Set<Ref> dependents = dt.getDirectDependents(ref);
		if(dependents.size()>0){
			//clear the dependents dependency before rename it's Name name
			for(Ref dependent:dependents){
				dt.clearDependents(dependent);
			}
			
			//rebuild the the formula by tuner
			tuner.renameTableName(this, oldName, newName, dependents);
		}
	}
	
	//ZSS-967
	public String setTableColumnName(STable table, String oldName, String newName) {
		if (Objects.equals(oldName, newName)) return newName;
		
		// locate the STableColumn of oldName
		List<STableColumn> tbCols = table.getColumns();
		STableColumn tbCol = null;
		STableColumn tbColDup = null;
		Set<String> set = new HashSet<String>(tbCols.size() * 4 / 3);
		for (STableColumn tbCol0 : tbCols) {
			final String tbColName = tbCol0.getName().toUpperCase(); 
			if (tbColName.equalsIgnoreCase(oldName)) {
				tbCol = tbCol0;
			} else if (tbColName.equalsIgnoreCase(newName)) {
				tbColDup = tbCol0;
			} else {
				set.add(tbColName);
			}
		}
		if (tbCol == null) return null;
		
		String newName0 = null;
		if (newName == null) {
			// Generate a newer name if want to clear the cell
			newName0 = "Column";
			final String newNameUpper = newName0.toUpperCase();
			for (int j = tbCols.size(); j > 0; --j) {
				if (!set.contains(newNameUpper + j)) {
					newName0 = newName0 + j;
					break;
				}
			}
		} else if (tbColDup != null) {
			// Generate a newer name if found duplicate new name;
			newName0 = newName;
			final String newNameUpper = newName0.toUpperCase();
			for (int j = 2, len = tbCols.size() + 2; j < len; ++j) {
				if (!set.contains(newNameUpper + j)) {
					newName0 = newName0 + j;
					break;
				}
			}
		}
		
		final String newName1 = newName0 != null ? newName0 : newName; 
		tbCol.setName(newName1);
		
		renameColumnNameFormula(table, oldName, newName1);
		
		return newName0;
	}
	
	//ZSS-967
	private void renameColumnNameFormula(STable table, String oldName, String newName) {
		AbstractBookSeriesAdv bs = (AbstractBookSeriesAdv)getBookSeries();
		FormulaTunerHelper tuner = new FormulaTunerHelper(bs);
		DependencyTable dt = bs.getDependencyTable();
		final String tableName = table.getName();
		final Ref ref = new ColumnPrecedentRefImpl(table.getBook().getBookName(), tableName, oldName); //old name
		Set<Ref> dependents = dt.getDirectDependents(ref);
		if(dependents.size()>0){
			//clear the dependents dependency before rename it's Name name
			for(Ref dependent:dependents){
				dt.clearDependents(dependent);
			}
			
			//rebuild the the formula by tuner
			tuner.renameColumnName(table, oldName, newName, dependents);
		}
	}
	
	//ZSS-1041
	@Override
	public SCellStyle getOrCreateDefaultHyperlinkStyle() {
		final SFont defaultFont = this.getDefaultFont();
		final FontMatcher fontMatcher = new FontMatcher(defaultFont);
		fontMatcher.setColor("0000FF");
		fontMatcher.setUnderline(SFont.Underline.SINGLE);
		SFont linkFont = this.searchFont(fontMatcher);
		
		if (linkFont == null) {
			linkFont = this.createFont(defaultFont, true);
			linkFont.setColor(this.createColor("#0000FF"));
			linkFont.setUnderline(SFont.Underline.SINGLE);
		}
		final SCellStyle defaultStyle = this.getDefaultCellStyle();
		final CellStyleMatcher matcher = new CellStyleMatcher(defaultStyle);
		matcher.setFont(linkFont);
		SCellStyle linkStyle = this.searchCellStyle(matcher);
		if (linkStyle == null) {
			linkStyle = StyleUtil.cloneCellStyle(this, defaultStyle); //will store into book's styleTable
			linkStyle.setFont(linkFont);
		}
		return linkStyle;
	}
}

