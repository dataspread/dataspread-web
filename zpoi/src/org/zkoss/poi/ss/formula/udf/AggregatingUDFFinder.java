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

package org.zkoss.poi.ss.formula.udf;

import org.zkoss.poi.ss.formula.functions.FreeRefFunction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * Collects add-in libraries and VB macro functions together into one UDF finder
 *
 * @author PUdalau
 */
public class AggregatingUDFFinder implements UDFFinder {

	private final Collection<UDFFinder> _usedToolPacks;

	public AggregatingUDFFinder(UDFFinder ... usedToolPacks) {
        _usedToolPacks = new ArrayList<UDFFinder>(usedToolPacks.length);
		_usedToolPacks.addAll(Arrays.asList(usedToolPacks));
	}

	/**
	 * Returns executor by specified name. Returns <code>null</code> if
	 * function isn't contained by any registered tool pack.
	 *
	 * @param name Name of function.
	 * @return Function executor. <code>null</code> if not found
	 */
	public FreeRefFunction findFunction(String name) {
		FreeRefFunction evaluatorForFunction;
		for (UDFFinder pack : _usedToolPacks) {
			evaluatorForFunction = pack.findFunction(name);
			if (evaluatorForFunction != null) {
				return evaluatorForFunction;
			}
		}
		return null;
	}

    /**
     * Add a new toolpack
     *
     * @param toolPack the UDF toolpack to add
     */
    public void add(UDFFinder toolPack){
        _usedToolPacks.add(toolPack);
    }
    
    //20130424, dennischen@zkoss.org
    /**
     * Insert a toolpack
     * @param index the index to insert
     * @param toolPack the UDF toolpack to insert
     */
    public void insert(int index,UDFFinder toolPack){
    	((ArrayList)_usedToolPacks).add(index, toolPack);
    }
}
