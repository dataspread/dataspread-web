/* BorderImpl.java

	Purpose:
		
	Description:
		
	History:
		Mar 31, 2015 6:10:17 PM, Created by henrichen

	Copyright (C) 2015 Potix Corporation. All Rights Reserved.
*/

package org.zkoss.zss.model.impl;

import org.zkoss.lang.Objects;
import org.zkoss.zss.model.SBorderLine;
import org.zkoss.zss.model.SColor;

/**
 * @author henri
 * @since 3.8.0
 */
public class BorderImpl extends AbstractBorderAdv {
	private static final long serialVersionUID = 1L;

	private SBorderLine _leftLine;
	private SBorderLine _topLine;
	private SBorderLine _rightLine;
	private SBorderLine _bottomLine;
	private SBorderLine _diagonalLine;
	private SBorderLine _verticalLine;
	private SBorderLine _horizontalLine;
	
//	private BorderType _borderLeft;
//	private BorderType _borderTop;
//	private BorderType _borderRight;
//	private BorderType _borderBottom;
//	private BorderType _borderVertical;
//	private BorderType _borderHorizontal;
//	private BorderType _borderDiagonal;
//	private SColor _borderTopColor;
//	private SColor _borderLeftColor;
//	private SColor _borderBottomColor;
//	private SColor _borderRightColor;
//	private SColor _borderVerticalColor;
//	private SColor _borderHorizontalColor;
//	private SColor _borderDiagonalColor;
//	private boolean _showDiagonalDown;
//	private boolean _showDiagonalUp;
	
	public BorderImpl(){}
	//ZSS-977
	public BorderImpl(SBorderLine left, SBorderLine top, 
			SBorderLine right, SBorderLine bottom, SBorderLine diagonal, 
			SBorderLine vertical, SBorderLine horizontal) {
		_leftLine = left;
		_topLine = top;
		_rightLine = right;
		_bottomLine = bottom;
		_diagonalLine = diagonal;
		_verticalLine = vertical;
		_horizontalLine = horizontal;
	}

	@Override
	public BorderType getBorderLeft() {
		return _leftLine == null ? BorderType.NONE : _leftLine.getBorderType();
	}

	@Override
	public BorderType getBorderTop() {
		return _topLine == null ? BorderType.NONE : _topLine.getBorderType();
	}

	@Override
	public BorderType getBorderRight() {
		return _rightLine == null ? BorderType.NONE : _rightLine.getBorderType();
	}

	@Override
	public BorderType getBorderBottom() {
		return _bottomLine == null ? BorderType.NONE : _bottomLine.getBorderType();
	}

	@Override
	public BorderType getBorderVertical() {
		return _verticalLine == null ? BorderType.NONE : _verticalLine.getBorderType();
	}

	@Override
	public BorderType getBorderHorizontal() {
		return _horizontalLine == null ? BorderType.NONE : _horizontalLine.getBorderType();
	}

	@Override
	public BorderType getBorderDiagonal() {
		return _diagonalLine == null ? BorderType.NONE : _diagonalLine.getBorderType();
	}

	@Override
	public SColor getBorderTopColor() {
		return _topLine == null ? ColorImpl.BLACK : _topLine.getColor();
	}

	@Override
	public SColor getBorderLeftColor() {
		return _leftLine == null ? ColorImpl.BLACK : _leftLine.getColor();
	}

	@Override
	public SColor getBorderBottomColor() {
		return _bottomLine == null ? ColorImpl.BLACK : _bottomLine.getColor();
	}

	@Override
	public SColor getBorderRightColor() {
		return _rightLine == null ? ColorImpl.BLACK : _rightLine.getColor();
	}

	@Override
	public SColor getBorderVerticalColor() {
		return _verticalLine == null ? ColorImpl.BLACK : _verticalLine.getColor();
	}

	@Override
	public SColor getBorderHorizontalColor() {
		return _horizontalLine == null ? ColorImpl.BLACK : _horizontalLine.getColor();
	}

	@Override
	public SColor getBorderDiagonalColor() {
		return _diagonalLine == null ? ColorImpl.BLACK : _diagonalLine.getColor();
	}

	@Override
	public boolean isShowDiagonalUpBorder() {
		return _diagonalLine == null ? false : _diagonalLine.isShowDiagonalUpBorder();
	}

	@Override
	public boolean isShowDiagonalDownBorder() {
		return _diagonalLine == null ? false : _diagonalLine.isShowDiagonalDownBorder();
	}

	@Override
	String getStyleKey() {
		return new StringBuilder()
			.append(_leftLine == null ? "" : ((AbstractBorderLineAdv)_leftLine).getStyleKey())
			.append(_topLine == null ? "" : ((AbstractBorderLineAdv)_topLine).getStyleKey())
			.append(_rightLine == null ? "" : ((AbstractBorderLineAdv)_rightLine).getStyleKey())
			.append(_bottomLine == null ? "" : ((AbstractBorderLineAdv)_bottomLine).getStyleKey())
			.append(_diagonalLine == null ? "" : ((AbstractBorderLineAdv)_diagonalLine).getStyleKey())
			.append(_verticalLine == null ? "" : ((AbstractBorderLineAdv)_verticalLine).getStyleKey())
			.append(_horizontalLine == null ? "" : ((AbstractBorderLineAdv)_horizontalLine).getStyleKey()).toString();
	}

	@Override
	public void setBorderLeft(BorderType type) {
		if (_leftLine == null) {
			_leftLine = new BorderLineImpl(type, ColorImpl.BLACK);
		}
		_leftLine.setBorderType(type);
	}

	@Override
	public void setBorderTop(BorderType type) {
		if (_topLine == null) {
			_topLine = new BorderLineImpl(type, ColorImpl.BLACK);
		}
		_topLine.setBorderType(type);
	}

	@Override
	public void setBorderRight(BorderType type) {
		if (_rightLine == null) {
			_rightLine = new BorderLineImpl(type, ColorImpl.BLACK);
		}
		_rightLine.setBorderType(type);
	}

	@Override
	public void setBorderBottom(BorderType type) {
		if (_bottomLine == null) {
			_bottomLine = new BorderLineImpl(type, ColorImpl.BLACK);
		}
		_bottomLine.setBorderType(type);
	}

	@Override
	public void setBorderVertical(BorderType type) {
		if (_verticalLine == null) {
			_verticalLine = new BorderLineImpl(type, ColorImpl.BLACK);
		}
		_verticalLine.setBorderType(type);
	}

	@Override
	public void setBorderHorizontal(BorderType type) {
		if (_horizontalLine == null) {
			_horizontalLine = new BorderLineImpl(type, ColorImpl.BLACK);
		}
		_horizontalLine.setBorderType(type);
	}

	@Override
	public void setBorderDiagonal(BorderType type) {
		if (_diagonalLine == null) {
			_diagonalLine = new BorderLineImpl(type, ColorImpl.BLACK);
		}
		_diagonalLine.setBorderType(type);
	}

	@Override
	public void setBorderTopColor(SColor color) {
		if (_topLine == null) {
			_topLine = new BorderLineImpl(BorderType.NONE, color);
		} else {
			_topLine.setColor(color);
		}
	}

	@Override
	public void setBorderLeftColor(SColor color) {
		if (_leftLine == null) {
			_leftLine = new BorderLineImpl(BorderType.NONE, color);
		} else {
			_leftLine.setColor(color);
		}
	}

	@Override
	public void setBorderBottomColor(SColor color) {
		if (_bottomLine == null) {
			_bottomLine = new BorderLineImpl(BorderType.NONE, color);
		} else {
			_bottomLine.setColor(color);
		}
	}

	@Override
	public void setBorderRightColor(SColor color) {
		if (_rightLine == null) {
			_rightLine = new BorderLineImpl(BorderType.NONE, color);
		} else {
			_rightLine.setColor(color);
		}
	}

	@Override
	public void setBorderVerticalColor(SColor color) {
		if (_verticalLine == null) {
			_verticalLine = new BorderLineImpl(BorderType.NONE, color);
		} else {
			_verticalLine.setColor(color);
		}
	}

	@Override
	public void setBorderHorizontalColor(SColor color) {
		if (_horizontalLine == null) {
			_horizontalLine = new BorderLineImpl(BorderType.NONE, color);
		} else {
			_horizontalLine.setColor(color);
		}
	}

	@Override
	public void setBorderDiagonalColor(SColor color) {
		if (_diagonalLine == null) {
			_diagonalLine = new BorderLineImpl(BorderType.NONE, color);
		} else {
			_diagonalLine.setColor(color);
		}
	}

	@Override
	public void setShowDiagonalUpBorder(boolean show) {
		if (_diagonalLine == null) {
			_diagonalLine = new BorderLineImpl(BorderType.NONE, ColorImpl.BLACK, show, false);
		} else {
			_diagonalLine.setShowDiagonalUpBorder(show);
		}
	}

	@Override
	public void setShowDiagonalDownBorder(boolean show) {
		if (_diagonalLine == null) {
			_diagonalLine = new BorderLineImpl(BorderType.NONE, ColorImpl.BLACK, false, show);
		} else {
			_diagonalLine.setShowDiagonalDownBorder(show);
		}
	}
	
	public SBorderLine getLeftLine() {
		return _leftLine;
	}

	public SBorderLine getTopLine() {
		return _topLine;
	}

	public SBorderLine getRightLine() {
		return _rightLine;
	}

	public SBorderLine getBottomLine() {
		return _bottomLine;
	}

	public SBorderLine getDiagonalLine() {
		return _diagonalLine;
	}

	public SBorderLine getVerticalLine() {
		return _verticalLine;
	}
	
	public SBorderLine getHorizontalLine() {
		return _horizontalLine;
	}
	
	//--Object--//
	public int hashCode() {
		int hash = _leftLine == null ? 0 : _leftLine.hashCode();
		hash = hash * 31 + (_topLine == null ? 0 : _topLine.hashCode());
		hash = hash * 31 + (_rightLine == null ? 0 : _rightLine.hashCode());
		hash = hash * 31 + (_bottomLine == null ? 0 : _bottomLine.hashCode());
		hash = hash * 31 + (_diagonalLine == null ? 0 : _diagonalLine.hashCode());
		hash = hash * 31 + (_verticalLine == null ? 0 : _verticalLine.hashCode());
		hash = hash * 31 + (_horizontalLine == null ? 0 : _horizontalLine.hashCode());
		
		return hash;
	}
	
	public boolean equals(Object other) {
		if (other == this) return true;
		if (!(other instanceof BorderImpl)) return false;
		BorderImpl o = (BorderImpl) other;
		return Objects.equals(this._leftLine, o._leftLine)
				&& Objects.equals(this._topLine, o._topLine)
				&& Objects.equals(this._rightLine, o._rightLine)
				&& Objects.equals(this._bottomLine, o._bottomLine)
				&& Objects.equals(this._diagonalLine, o._diagonalLine)
				&& Objects.equals(this._verticalLine, o._verticalLine)
				&& Objects.equals(this._horizontalLine, o._horizontalLine);
	}
}
