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

import org.openxmlformats.schemas.drawingml.x2006.main.CTAdjPoint2D;

import java.awt.geom.GeneralPath;

/**
 * Date: 10/25/11
 *
 * @author Yegor Kozlov
 */
public class MoveToCommand implements PathCommand {
    private String arg1, arg2;

    MoveToCommand(CTAdjPoint2D pt){
        arg1 = pt.getX().toString();
        arg2 = pt.getY().toString();
    }

    MoveToCommand(String s1, String s2){
        arg1 = s1;
        arg2 = s2;
    }

    public void execute(GeneralPath path, Context ctx){
        double x = ctx.getValue(arg1);
        double y = ctx.getValue(arg2);
        path.moveTo((float)x, (float)y);
    }
}
