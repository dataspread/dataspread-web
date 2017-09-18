package org.zkoss.zss.ui.impl;

import org.zkoss.zss.model.*;
import org.zkoss.zss.model.impl.AbstractFontAdv;
import org.zkoss.zss.model.impl.CellStyleImpl;
import org.zkoss.zss.model.impl.ColorImpl;
import org.zkoss.zss.model.impl.FontImpl;
import org.zkoss.zss.model.util.CellStyleMatcher;

public class SemanticsHelper {
	public static SCellStyle styleForSemantics(SBook book, SSemantics.Semantics semantics) {
		if (semantics == SSemantics.Semantics.TABLE_HEADER ||
				semantics == SSemantics.Semantics.TABLE_CONTENT) {

			CellStyleMatcher matcher = new CellStyleMatcher();
			matcher.setBackColor("#99ccff");
			matcher.setFillPattern(SFill.FillPattern.SOLID);
			matcher.setBorderBottom(SBorder.BorderType.MEDIUM);
			matcher.setBorderLeft(SBorder.BorderType.MEDIUM);
			matcher.setBorderRight(SBorder.BorderType.MEDIUM);
			matcher.setBorderTop(SBorder.BorderType.MEDIUM);
			matcher.setBorderBottomColor("#000000");
			matcher.setBorderLeftColor("#000000");
			matcher.setBorderRightColor("#000000");
			matcher.setBorderTopColor("#000000");
			if (semantics == SSemantics.Semantics.TABLE_HEADER) {
				matcher.setFontBoldweight(SFont.Boldweight.BOLD);
			}
			SCellStyle style = book.searchCellStyle(matcher);

			if (style == null) {
				SColor backColor = book.createColor("#99ccff");
				SColor borderColor = ColorImpl.BLACK;
				AbstractFontAdv font = new FontImpl();
				if (semantics == SSemantics.Semantics.TABLE_HEADER)
					font.setBoldweight(SFont.Boldweight.BOLD);
				style = new CellStyleImpl(font);

				style.setBackColor(backColor);
				style.setFillPattern(SFill.FillPattern.SOLID);
				style.setBorderBottom(SBorder.BorderType.MEDIUM);
				style.setBorderLeft(SBorder.BorderType.MEDIUM);
				style.setBorderRight(SBorder.BorderType.MEDIUM);
				style.setBorderTop(SBorder.BorderType.MEDIUM);
				style.setBorderBottomColor(borderColor);
				style.setBorderLeftColor(borderColor);
				style.setBorderRightColor(borderColor);
				style.setBorderTopColor(borderColor);
			}
			return style;
		}
		return null;
	}
}
