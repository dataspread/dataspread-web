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

import org.zkoss.zss.model.SRichText;
/**
 * 
 * @author dennis
 * @since 3.5.0
 */
public class CommentImpl extends AbstractCommentAdv {
	private static final long serialVersionUID = 1L;
	private Object _text;
	private String _author;
	private boolean _visible; //ZSS-1055
	
	@Override
	public String getText() {
		return _text instanceof String?(String)_text:null;
	}

	@Override
	public void setText(String text) {
		this._text = text;
	}

	@Override
	public void setRichText(SRichText text) {
		this._text = text;
	}

	@Override
	public SRichText setupRichText() {
		if(this._text instanceof SRichText){
			return (SRichText)this._text;
		}
		this._text = new RichTextImpl();
		return (SRichText)this._text;
	}

	@Override
	public SRichText getRichText() {
		return _text instanceof SRichText?(SRichText)_text:null;
	}

	@Override
	public boolean isVisible() {
		return _visible;
	}
	@Override
	public void setVisible(boolean visible) {
		this._visible = visible;
	}

	@Override
	public String getAuthor() {
		return _author;
	}

	@Override
	public void setAuthor(String author) {
		this._author = author;
	}

	@Override
	public AbstractCommentAdv clone() {
		CommentImpl comment = new CommentImpl();
		comment.setAuthor(_author);
		comment.setVisible(_visible);
		if(this._text instanceof SRichText){
			comment.setRichText(((AbstractRichTextAdv)_text).clone());
		}else if(this._text instanceof String){
			comment.setText((String)_text);
		}
		
		
		return comment;
	}

	@Override
	public boolean isRichText() {
		return _text instanceof SRichText;
	}

}
