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

package org.zkoss.poi.xslf.usermodel;

import org.zkoss.poi.POIXMLDocumentPart;
import org.zkoss.poi.POIXMLException;
import org.zkoss.poi.POIXMLFactory;
import org.zkoss.poi.POIXMLRelation;
import org.zkoss.poi.openxml4j.opc.PackagePart;
import org.zkoss.poi.openxml4j.opc.PackageRelationship;
import org.zkoss.poi.util.Beta;
import org.zkoss.poi.util.POILogFactory;
import org.zkoss.poi.util.POILogger;

import java.lang.reflect.Constructor;

/**
 * Instantiates sub-classes of POIXMLDocumentPart depending on their relationship type
 *
 * @author Yegor Kozlov
 */
@Beta
public final class XSLFFactory extends POIXMLFactory  {
    private static final POILogger logger = POILogFactory.getLogger(XSLFFactory.class);

    private XSLFFactory(){

    }

    private static final XSLFFactory inst = new XSLFFactory();

    public static XSLFFactory getInstance(){
        return inst;
    }

    @Override
    public POIXMLDocumentPart createDocumentPart(POIXMLDocumentPart parent, PackageRelationship rel, PackagePart part){
        POIXMLRelation descriptor = XSLFRelation.getInstance(rel.getRelationshipType());
        if(descriptor == null || descriptor.getRelationClass() == null){
            logger.log(POILogger.DEBUG, "using default POIXMLDocumentPart for " + rel.getRelationshipType());
            return new POIXMLDocumentPart(part, rel);
        }

        try {
            Class<? extends POIXMLDocumentPart> cls = descriptor.getRelationClass();
            Constructor<? extends POIXMLDocumentPart> constructor = cls.getDeclaredConstructor(PackagePart.class, PackageRelationship.class);
            return constructor.newInstance(part, rel);
        } catch (Exception e){
            throw new POIXMLException(e);
        }
    }

    @Override
    public POIXMLDocumentPart newDocumentPart(POIXMLRelation descriptor){
        try {
            Class<? extends POIXMLDocumentPart> cls = descriptor.getRelationClass();
            Constructor<? extends POIXMLDocumentPart> constructor = cls.getDeclaredConstructor();
            return constructor.newInstance();
        } catch (Exception e){
            throw new POIXMLException(e);
        }
    }

}
