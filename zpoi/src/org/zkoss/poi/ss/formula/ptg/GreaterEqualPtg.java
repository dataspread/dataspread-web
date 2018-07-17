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

package org.zkoss.poi.ss.formula.ptg;


/**
 * PTG class to implement greater or equal to
 *
 * @author  fred at stsci dot edu
 */
public final class GreaterEqualPtg extends ComparisonPtg {
    public final static int  SIZE = 1;
    public final static byte sid  = 0x0c;

    public static final ValueOperatorPtg instance = new GreaterEqualPtg();

    private GreaterEqualPtg() {
    	// enforce singleton
    }

    @Override
    public OperationPtg getInstance() {
        return instance;
    }


    protected byte getSid() {
    	return sid;
    }

    public int getNumberOfOperands() {
        return 2;
    }

    public String toFormulaString(String[] operands) {
         StringBuffer buffer = new StringBuffer();

        buffer.append(operands[ 0 ]);

        buffer.append(">=");
        buffer.append(operands[ 1 ]);

        return buffer.toString();
    }
}
