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

package org.zkoss.poi.ss.formula.functions;

/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 *
 */
public final class Odd extends NumericFunction.OneArg {
	private static final long PARITY_MASK = 0xFFFFFFFFFFFFFFFEL;

	protected double evaluate(double d) {
		if (d==0) {
			return 1;
		}
		if (d>0) {
			return calcOdd(d);
		}
		return -calcOdd(-d);
	}

	private static long calcOdd(double d) {
		double dpm1 = d+1;
		long x = ((long) dpm1) & PARITY_MASK;
		if (x == dpm1) {
			return x-1;
		}
		return x + 1;
	}
}
