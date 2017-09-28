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
 * This PTG implements the standard binomial divide "/"
 * @author  Andrew C. Oliver acoliver at apache dot org
 * @author Jason Height (jheight at chariot dot net dot au)
 */
public final class SelectHelperPtg extends ValueOperatorPtg {
    public final static byte sid  = 0x70;

    public static final ValueOperatorPtg instance = new SelectHelperPtg();

    private SelectHelperPtg() {
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
        return 0;
    }

     public String toFormulaString(String[] operands) {
        return "";
    }      
}
