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

package org.zkoss.poi.hwpf.usermodel;

import org.zkoss.poi.util.POILogFactory;

import org.zkoss.poi.util.POILogger;

import org.zkoss.poi.hwpf.model.ListTables;

import org.zkoss.poi.util.Internal;

import org.zkoss.poi.hwpf.model.LFO;
import org.zkoss.poi.hwpf.model.LFOData;
import org.zkoss.poi.hwpf.model.ListData;
import org.zkoss.poi.hwpf.model.ListFormatOverrideLevel;
import org.zkoss.poi.hwpf.model.ListLevel;
import org.zkoss.poi.hwpf.model.StyleSheet;
import org.zkoss.poi.hwpf.sprm.CharacterSprmCompressor;
import org.zkoss.poi.hwpf.sprm.ParagraphSprmCompressor;

/**
 * This class is used to create a list in a Word document. It is used in
 * conjunction with
 * {@link org.zkoss.poi.hwpf.HWPFDocument#registerList(HWPFList) registerList}
 * in {@link org.zkoss.poi.hwpf.HWPFDocument HWPFDocument}.
 * 
 * In Word, lists are not ranged entities, meaning you can't actually add one to
 * the document. Lists only act as properties for list entries. Once you
 * register a list, you can add list entries to a document that are a part of
 * the list.
 * 
 * The only benefit of this that I see, is that you can add a list entry
 * anywhere in the document and continue numbering from the previous list.
 * 
 * @author Ryan Ackley
 */
public final class HWPFList
{
    private static POILogger log = POILogFactory.getLogger( HWPFList.class );

    private boolean _ignoreLogicalLeftIdentation = false;
    private LFO _lfo;
    private LFOData _lfoData;
    private ListData _listData;
    private ListTables _listTables;
    private boolean _registered;
    private StyleSheet _styleSheet;

    /**
     * 
     * @param numbered
     *            true if the list should be numbered; false if it should be
     *            bulleted.
     * @param styleSheet
     *            The document's stylesheet.
     */
    public HWPFList( boolean numbered, StyleSheet styleSheet )
    {
        _listData = new ListData(
                (int) ( Math.random() * System.currentTimeMillis() ), numbered );
        _lfo = new LFO();
        _lfo.setLsid( _listData.getLsid() );
        _lfoData = new LFOData();
        _styleSheet = styleSheet;
    }

    public HWPFList( StyleSheet styleSheet, ListTables listTables, int ilfo )
    {
        _listTables = listTables;
        _styleSheet = styleSheet;
        _registered = true;

        /* See documentation for sprmPIlfo (0x460B) */
        if ( ilfo == 0 || ilfo == 0xF801 )
        {
            throw new IllegalArgumentException( "Paragraph not in list" );
        }
        else if ( 0x0001 <= ilfo && ilfo <= 0x07FE )
        {
            _lfo = listTables.getLfo( ilfo );
            _lfoData = listTables.getLfoData( ilfo );
        }
        else if ( 0xF802 <= ilfo && ilfo <= 0xFFFF )
        {
            int nilfo = ilfo ^ 0xFFFF;
            _lfo = listTables.getLfo( nilfo );
            _lfoData = listTables.getLfoData( nilfo );
            _ignoreLogicalLeftIdentation = true;
        }
        else
        {
            throw new IllegalArgumentException( "Incorrect ilfo: " + ilfo );
        }

        _listData = listTables.getListData( _lfo.getLsid() );
    }

    @Internal
    public LFO getLFO()
    {
        return _lfo;
    }

    @Internal
    public LFOData getLFOData()
    {
        return _lfoData;
    }

    @Internal
    public ListData getListData()
    {
        return _listData;
    }

    public int getLsid()
    {
        return _lfo.getLsid();
    }

    @Internal
    ListLevel getLVL( char level )
    {
        if ( level >= _listData.numLevels() )
        {
            throw new IllegalArgumentException( "Required level "
                    + ( (int) level )
                    + " is more than number of level for list ("
                    + _listData.numLevels() + ")" );
        }
        ListLevel lvl = _listData.getLevels()[level];
        return lvl;
    }

    /**
     * An MSONFC, as specified in [MS-OSHARED] section 2.2.1.3, that specifies
     * the format of the level numbers that replace the placeholders for this
     * level in the xst fields of the LVLs in this list. This value MUST not be
     * equal to 0x08, 0x09, 0x0F, or 0x13. If this is equal to 0xFF or 0x17,
     * this level does not have a number sequence and therefore has no number
     * formatting. If this is equal to 0x17, the level uses bullets.
     */
    public int getNumberFormat( char level )
    {
        return getLVL( level ).getNumberFormat();
    }

    public String getNumberText( char level )
    {
        return getLVL( level ).getNumberText();
    }

    public int getStartAt( char level )
    {
        if ( isStartAtOverriden( level ) )
        {
            return _lfoData.getRgLfoLvl()[level].getIStartAt();
        }

        return getLVL( level ).getStartAt();
    }

    /**
     * "The type of character following the number text for the paragraph: 0 == tab, 1 == space, 2 == nothing."
     */
    public byte getTypeOfCharFollowingTheNumber( char level )
    {
        return getLVL( level ).getTypeOfCharFollowingTheNumber();
    }

    public boolean isIgnoreLogicalLeftIdentation()
    {
        return _ignoreLogicalLeftIdentation;
    }

    public boolean isStartAtOverriden( char level )
    {
        ListFormatOverrideLevel lfolvl = _lfoData.getRgLfoLvl().length > level ? _lfoData
                .getRgLfoLvl()[level] : null;

        return lfolvl != null && lfolvl.getIStartAt() != 0
                && !lfolvl.isFormatting();
    }

    public void setIgnoreLogicalLeftIdentation(
            boolean ignoreLogicalLeftIdentation )
    {
        this._ignoreLogicalLeftIdentation = ignoreLogicalLeftIdentation;
    }

    /**
     * Sets the character properties of the list numbers.
     * 
     * @param level
     *            the level number that the properties should apply to.
     * @param chp
     *            The character properties.
     */
    public void setLevelNumberProperties( int level, CharacterProperties chp )
    {
        ListLevel listLevel = _listData.getLevel( level );
        int styleIndex = _listData.getLevelStyle( level );
        CharacterProperties base = _styleSheet.getCharacterStyle( styleIndex );

        byte[] grpprl = CharacterSprmCompressor.compressCharacterProperty( chp,
                base );
        listLevel.setNumberProperties( grpprl );
    }

    /**
     * Sets the paragraph properties for a particular level of the list.
     * 
     * @param level
     *            The level number.
     * @param pap
     *            The paragraph properties
     */
    public void setLevelParagraphProperties( int level, ParagraphProperties pap )
    {
        ListLevel listLevel = _listData.getLevel( level );
        int styleIndex = _listData.getLevelStyle( level );
        ParagraphProperties base = _styleSheet.getParagraphStyle( styleIndex );

        byte[] grpprl = ParagraphSprmCompressor.compressParagraphProperty( pap,
                base );
        listLevel.setLevelProperties( grpprl );
    }

    public void setLevelStyle( int level, int styleIndex )
    {
        _listData.setLevelStyle( level, styleIndex );
    }
}
