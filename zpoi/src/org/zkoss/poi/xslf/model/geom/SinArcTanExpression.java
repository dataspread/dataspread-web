/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */

package org.zkoss.poi.xslf.model.geom;

import java.util.regex.Matcher;

/**
 * Sine ArcTan Formula:
 * <gd name="dy1" fmla="sat2 x y z"/>
 *
 * <p>
 *     Arguments: 3 (fmla="sat2 x y z")
 *     Usage: "sat2 x y z" = (x*sin(arctan(z / y))) = value of this guide
 * </p>
 *
 * @author Yegor Kozlov
 */
public class SinArcTanExpression implements Expression {
    private String arg1, arg2, arg3;

    SinArcTanExpression(Matcher m){
        arg1 = m.group(1);
        arg2 = m.group(2);
        arg3 = m.group(3);
    }

    public double evaluate(Context ctx){
        double x = ctx.getValue(arg1);
        double y = ctx.getValue(arg2);
        double z = ctx.getValue(arg3);
        return x*Math.sin(Math.atan(z / y));
    }

}
