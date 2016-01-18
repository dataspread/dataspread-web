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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.zkoss.zss.model.SFont;
import org.zkoss.zss.model.util.Validations;
/**
 * 
 * @author dennis
 * @since 3.5.0
 */
public class RichTextImpl extends AbstractRichTextAdv {
	private static final long serialVersionUID = 1L;

	List<SegmentImpl> _segments = new ArrayList<SegmentImpl>();


	@Override
	public String getText() {
		if (_segments.size() == 0) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		for (Segment s : _segments) {
			sb.append(s.getText());
		}
		return sb.toString();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public List<Segment> getSegments() {
		return Collections.unmodifiableList((List) _segments);
	}

	@Override
	public void addSegment(String text, SFont font) {
		Validations.argNotNull(text,font);
		if("".equals(text)) return;
		
		if (!_segments.isEmpty()) {
			final int idx = _segments.size() - 1;
			final Segment seg = _segments.get(idx);
			// text merge to previous segment if text is \n' or fonts are the same
			if ("\n".equals(text) || seg.getFont().equals(font)) { 
				_segments.set(idx, new SegmentImpl(seg.getText()+text, seg.getFont()));
				return;
			}
		}
		
		_segments.add(new SegmentImpl(text, font));
	}

	@Override
	public void clearSegments() {
		_segments.clear();
	}

	@Override
	public SFont getFont() {
		if (_segments.size() == 0) {
			return null;
		}
		return _segments.get(0).getFont();
	}

	@Override
	public AbstractRichTextAdv clone() {
		RichTextImpl richText = new RichTextImpl();
		for(Segment s:_segments){
			richText.addSegment(s.getText(), s.getFont());
		}
		return richText;
	}
	
	@Override
	public int getHeightPoints() {
		int highest = 0;
		for (Segment ss : getSegments()) {
			int p = ss.getFont().getHeightPoints();
			if(p > highest) 
				highest = p;
		}
		return highest;
	}

}
