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


/**
 * Represent many {@link ValueEval}s; used in 3d area or reference(e.g. Sheet1:Sheet3!A1:B1).
 * @author	Henri Chen (henrichen at zkoss dot org) - Sheet1:Sheet3!xxx 3d reference
 */
public class ValuesEval implements ValueEval {
	private final ValueEval[] _evals;
	public ValuesEval(ValueEval[] evals) {
		_evals = evals;
	}
	/**
	 * Return the containing ValueEvals.
	 * @return the containing ValueEvals.
	 */
	public ValueEval[] getValueEvals() {
		return _evals;
	}
}
