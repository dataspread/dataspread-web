/*
{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		2014/8/5, Created by henri
}}IS_NOTE

Copyright (C) 2014 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/

package org.zkoss.zss.api.model.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.zkoss.zss.api.model.Book;
import org.zkoss.zss.api.model.Font;
import org.zkoss.zss.api.model.RichText;
import org.zkoss.zss.model.SFont;
import org.zkoss.zss.model.SRichText;
import org.zkoss.zss.model.util.Validations;

/**
 * @author henri
 *
 */
public class RichTextImpl implements RichText {
	final Book _book;
	final SRichText _native;
	
	public RichTextImpl(Book book) {
		_book = book;
		_native = new org.zkoss.zss.model.impl.RichTextImpl();
	}
	
	public SRichText getNative() {
		return _native;
	}
	
	@Override
	public String getText() {
		
		return _native.getText();
	}

	@Override
	public Font getFont() {
		final SFont font0 = _native.getFont();
		return font0 == null ? null :
			new FontImpl(((BookImpl) _book).getRef(), new SimpleRef<SFont>(font0));
	}

	@Override
	public List<Segment> getSegments() {
		List<SRichText.Segment> modelsegs = _native.getSegments();
		if (modelsegs.isEmpty()) {
			return Collections.emptyList();
		}
		List<Segment> apisegs = new ArrayList<Segment>(modelsegs.size());
		for (SRichText.Segment seg : modelsegs) {
			final Font font = new FontImpl(((BookImpl) _book).getRef(), new SimpleRef<SFont>(seg.getFont()));
			apisegs.add(new SegmentImpl(seg.getText(), font));
		}
		return apisegs;
	}

	@Override
	public void addSegment(String text, Font font) {
		Validations.argNotNull(text);
		if("".equals(text)) return;
		_native.addSegment(text, ((FontImpl)font).getNative());
	}

	@Override
	public void clearSegments() {
		_native.clearSegments();
	}
}
