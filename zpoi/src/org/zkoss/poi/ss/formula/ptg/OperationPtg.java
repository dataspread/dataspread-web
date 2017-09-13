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
 * defines a Ptg that is an operation instead of an operand
 * @author  andy
 */
public abstract class OperationPtg extends Ptg {
    public final static int TYPE_UNARY    = 0;
    public final static int TYPE_BINARY   = 1;
    public final static int TYPE_FUNCTION = 2;
    private boolean isOverrided = false;
    private int overrideTableNum = 0;

    public abstract OperationPtg getInstance();

    /**
     *  returns a string representation of the operations
     *  the length of the input array should equal the number returned by 
     *  @see #getNumberOfOperands
     *  
     */
    public abstract String toFormulaString(String[] operands);
    
    /**
     * The number of operands expected by the operations
     */
    public abstract int getNumberOfOperands();

    public boolean isOverrided() {
        return this.isOverrided;
    }

    public void setOverrided(boolean isOverrided) {
        this.isOverrided = isOverrided;
    }
    
    public byte getDefaultOperandClass() {
        return Ptg.CLASS_VALUE;
    }
    
    //ZSS-565
    public String toInternalFormulaString(String[] operands) {
    	return toFormulaString(operands);
    }

    public int getOverrideTableNum() {
        return this.overrideTableNum;
    }

    public void setOverrideTableNum(int numOperatorTables) {
        this.overrideTableNum = numOperatorTables;
    }
}
