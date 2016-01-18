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

package org.zkoss.poi.ss.formula.eval;

import org.zkoss.poi.ss.formula.ptg.Ptg;
import org.zkoss.poi.ss.formula.ptg.StringPtg;
import org.zkoss.poi.ss.usermodel.Hyperlink;

/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 * @author henrichen@zkoss.org: handle HYPERLINK function
 */
public final class StringEval implements StringValueEval, HyperlinkEval {

	public static final StringEval EMPTY_INSTANCE = new StringEval("");

	private final String _value;

	public StringEval(Ptg ptg) {
		this(((StringPtg) ptg).getValue());
	}

	public StringEval(String value) {
		if (value == null) {
			throw new IllegalArgumentException("value must not be null");
		}
		_value = value;
	}

	public String getStringValue() {
		return _value;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder(64);
		sb.append(getClass().getName()).append(" [");
		sb.append(_value);
		sb.append("]");
		return sb.toString();
	}
	
	//20100720, henrichen@zkoss.org: handle HYPERLINK function
	private Hyperlink _hyperlink;
	public void setHyperlink(Hyperlink hyperlink) {
		_hyperlink = hyperlink;
	}
	
	public Hyperlink getHyperlink() {
		return _hyperlink;
	}
}
