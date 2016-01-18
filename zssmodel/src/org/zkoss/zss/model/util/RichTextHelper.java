/* RichTextHelper.java

	Purpose:
		
	Description:
		
	History:
		Thu, Aug 14, 2014  2:13:06 PM, Created by RaymondChao

Copyright (C) 2014 Potix Corporation. All Rights Reserved.

*/
package org.zkoss.zss.model.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.zkoss.util.Maps;
import org.zkoss.zss.model.SCell;
import org.zkoss.zss.model.SFont;
import org.zkoss.zss.model.SFont.Boldweight;
import org.zkoss.zss.model.SFont.TypeOffset;
import org.zkoss.zss.model.SFont.Underline;
import org.zkoss.zss.model.SRichText.Segment;
import org.zkoss.zss.model.SRichText;
import org.zkoss.zss.model.impl.RichTextImpl;
import org.zkoss.zss.range.SRange;

/**
 * 
 * @author RaymondChao
 * @since 3.6.0
 */
public class RichTextHelper {
	private static final String NEW_LINE = "\u4a3a\u0000\u9f98";
	private static final Pattern rgbPattern = 
			Pattern.compile("rgb *\\( *([0-9]+), *([0-9]+), *([0-9]+) *\\)");
	private SRange _range;
	private Stack<SFont> _stack;
	private SRichText _txt;
	public RichTextHelper() {
		
	}
	public SRichText parse(SRange range, String content) {
		_range = range;
		_stack = new Stack<SFont>();
		_stack.push(range.getCellStyle().getFont());
		_txt = new RichTextImpl();
		parseElement(br2nl(content));
		return _txt;
	}
	
	private void parseElement(Element element) {
		SFont font = toFont(element);
		_stack.push(font);
		List<Node> nodes = element.childNodes();
		for (Node node: nodes) {
			if (node instanceof TextNode) {
				TextNode textNode = (TextNode) node;
				    // use NEW_LINE instead of new line to make sure it won't be treated as blank if
					// TextNode only contains new line.
				if (!textNode.isBlank()) {
					_txt.addSegment(textNode.text().replaceAll(NEW_LINE, "\n"), font);
				}
			} else if (node instanceof Element) {
				parseElement((Element) node);
			}
		}
		_stack.pop();
	}
	
	@SuppressWarnings("unchecked")
	private SFont toFont(Element element) {
		SFont fontBase = _stack.peek();
		Boldweight boldweight = fontBase.getBoldweight();
		String fontColor = fontBase.getColor().getHtmlColor();
		int fontHeight = fontBase.getHeightPoints();
		String fontName = fontBase.getName();
		boolean isItalic = fontBase.isItalic();
		boolean isStrikeout = fontBase.isStrikeout();
		TypeOffset typeOffset = fontBase.getTypeOffset();
		Underline underline = fontBase.getUnderline();
		final Map<String, String> style = (Map<String, String>) Maps.parse(new HashMap<String, String>(),
				element.attr("style").toLowerCase(), ':', ';', '"');
		
		if (style.containsKey("font-weight")) {
			final String weight = style.get("font-weight");
			if ("bold".equals(weight) || "700".equals(weight)) {
				boldweight = Boldweight.BOLD;
			} else {
				boldweight = Boldweight.NORMAL;
			}
		}

		if (style.containsKey("color")) {
			final String color = style.get("color");
			final Matcher rgbMatcher = rgbPattern.matcher(color);
			if (rgbMatcher.matches()) {
				fontColor = String.format("#%02x%02x%02x",
					(byte) Integer.parseInt(rgbMatcher.group(1)),
					(byte) Integer.parseInt(rgbMatcher.group(2)),
					(byte) Integer.parseInt(rgbMatcher.group(3)));
			} else if (color.startsWith("#")) {
				fontColor = color; 
			}
		}
		
		if (style.containsKey("font-family")) {
			fontName = style.get("font-family");
		}
		
		if (style.containsKey("font-size")) {
			final String size = style.get("font-size");
			final int ptIndex = size.lastIndexOf("pt");
			final int pxIndex = size.lastIndexOf("px");
			if (ptIndex > 0) {
				fontHeight = Integer.parseInt(size.substring(0, ptIndex));
			} else if (pxIndex > 0) {
				fontHeight = Integer.parseInt(size.substring(0, pxIndex)) * 72 / 96;
			}
		}
		
		if (style.containsKey("font-style")) {
			final String fs = style.get("font-style");
			if ("italic".equals(fs)) {
				isItalic = true;
			} else if ("normal".equals(fs)) {
				isItalic = false;
			}
		}
		
		if (style.containsKey("text-decoration")) {
			final String decoration = style.get("text-decoration");
			if (decoration.contains("underline")) {
				underline = Underline.SINGLE;
			}
			if (decoration.contains("line-through")) {
				isStrikeout = true;
			} 
		}
		
		if ("b".equals(element.nodeName()) || "strong".equals(element.nodeName())) {
			boldweight = Boldweight.BOLD;
		} else if ("i".equals(element.nodeName()) || "em".equals(element.nodeName())) {
			isItalic = true;
		} else if ("u".equals(element.nodeName())) {
			underline = Underline.SINGLE;
		} else if ("strike".equals(element.nodeName())) {
			isStrikeout = true;
		} else if ("sub".equals(element.nodeName())) {
			typeOffset = TypeOffset.SUB;
		} else if ("sup".equals(element.nodeName())) {
			typeOffset = TypeOffset.SUPER;
		}
		
		return _range.getOrCreateFont(
			boldweight,
			fontColor,
			fontHeight,
			fontName, 
			isItalic,
			isStrikeout,
			typeOffset,
			underline
		);
	}
	private static Element br2nl(String html) {
		Element body = Jsoup.parseBodyFragment(html).body();
		body.select("div").prepend(NEW_LINE);
		body.select("br").append(NEW_LINE);
		body.select("p").append(NEW_LINE);
		return body;
	}

	//ZSS-725
	public static String getCellRichTextHtml(SCell cell) {
		final boolean wrap = cell.getCellStyle().isWrapText();
		final SRichText rstr = cell.getRichTextValue();
		return getCellRichTextHtml(rstr, wrap);
	}
	
	//ZSS-848
	public static String getCellRichTextHtml(SRichText rstr, boolean wrap) {
		StringBuilder sb = new StringBuilder();
		for(Segment seg: rstr.getSegments()) {
			sb.append(getFontTextHtml(escapeText(seg.getText(), wrap, true), seg.getFont()));
		}
		return sb.toString();
	}
	
	public static String escapeText(String text, boolean wrap, boolean multiline) {
		final StringBuffer out = new StringBuffer();
		for (int j = 0, tl = text.length(); j < tl; ++j) {
			char cc = text.charAt(j);
			switch (cc) {
			case '&': out.append("&amp;"); break;
			case '<': out.append("&lt;"); break;
			case '>': out.append("&gt;"); break;
			case ' ': out.append("&nbsp;"); break; //ZSS-916
			case '\n':
				if (wrap && multiline) {
					out.append("<br/>");
					break;
				}
			default:
				out.append(cc);
			}
		}
		return out.toString();
	}

	// ZSS-725
	public static String getFontTextHtml(String text, SFont font) {
		StringBuilder sb = new StringBuilder();
		final String startTag;
		final String endTag;
		if (font.getTypeOffset() == SFont.TypeOffset.SUPER) {
			startTag = "<sup>";
			endTag = "</sup>";
		} else if(font.getTypeOffset() == SFont.TypeOffset.SUB) {
			startTag = "<sub>";
			endTag = "</sub>";
		} else {
			startTag = "";
			endTag = "";
		}
		sb.append("<span style=\"")
			.append(getFontCSSStyle(font, false))
			.append("\">");
		sb.append(startTag).append(text).append(endTag);
		sb.append("</span>");
		return sb.toString();
	}
	
	//ZSS-725
	public static String getFontCSSStyle(SFont font, boolean displayTypeOffset) {
		final StringBuffer sb = new StringBuffer();
		
		String fontName = font.getName();
		if (fontName != null) {
			sb.append("font-family:").append(fontName).append(";");
		}
		
		String textColor = font.getColor().getHtmlColor();
		if (textColor != null) {
			sb.append("color:").append(textColor).append(";");
		}

		final SFont.Underline fontUnderline = font.getUnderline(); 
		final boolean strikeThrough = font.isStrikeout();
		boolean isUnderline = fontUnderline == SFont.Underline.SINGLE || fontUnderline == SFont.Underline.SINGLE_ACCOUNTING;
		if (strikeThrough || isUnderline) {
			sb.append("text-decoration:");
			if (strikeThrough)
				sb.append(" line-through");
			if (isUnderline)	
				sb.append(" underline");
			sb.append(";");
		}

		final SFont.Boldweight weight = font.getBoldweight();
		
		sb.append("font-weight:").append(weight==SFont.Boldweight.BOLD?"bold":"normal").append(";");
		
		final boolean italic = font.isItalic();
		if (italic)
			sb.append("font-style:").append("italic;");
		
		//ZSS-748
		//ZSS-725
		int fontSize = font.getHeightPoints();
		if (displayTypeOffset && font.getTypeOffset() != SFont.TypeOffset.NONE) {
			fontSize = (int) (0.7 * fontSize + 0.5);
		}
		sb.append("font-size:").append(fontSize).append("pt;");
		//ZSS-748
		if (displayTypeOffset) {
			if (font.getTypeOffset() == SFont.TypeOffset.SUPER)
				sb.append("vertical-align:").append("super;");
			else if (font.getTypeOffset() == SFont.TypeOffset.SUB)
				sb.append("vertical-align:").append("sub;");
		}
		return sb.toString();
	}
	
	//ZSS-918
	public static String escapeVText(String text, boolean wrap) {
		final String between = wrap ? "</div><div class=\"zsvtxt\">" : "&nbsp;"; 
		final StringBuffer out = new StringBuffer();
		out.append("<div class=\"zsvtxt\">");
		for (int j = 0, tl = text.length(); j < tl; ++j) {
			char cc = text.charAt(j);
			switch (cc) {
			case '&': out.append("&amp;"); break;
			case '<': out.append("&lt;"); break;
			case '>': out.append("&gt;"); break;
			case ' ': out.append("&nbsp;"); break;
			case '\n': out.append(between); break;
			default:
				out.append(cc);
			}
		}
		out.append("</div>");
		return out.toString();
	}
	
	//ZSS-918
	public static String getCellVRichTextHtml(SRichText rstr, boolean wrap) {
		final String between = wrap ? "</div><div class=\"zsvtxt\">" : "&nbsp;"; 
		StringBuilder sb = new StringBuilder();
		sb.append("<div class=\"zsvtxt\">");
		for(Segment seg: rstr.getSegments()) {
			final String text = seg.getText();
			if ("\n".equals(text)) { // segment with single \n
				sb.append(between);
				continue;
			}
			final String[] texts = text.split("\n");
			final SFont font = seg.getFont();
			int j = 0;
			for (int len = texts.length - 1; j < len; ++j) {
				sb.append(getFontTextHtml(escapeText(texts[j], wrap, true), font));
				sb.append(between);
			}
			sb.append(getFontTextHtml(escapeText(texts[j], wrap, true), font));
		}
		sb.append("</div>");
		return sb.toString();
	}
}

