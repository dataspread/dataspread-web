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

/**
 * known preset shape geometries in PresentationML
 *
 * @author Yegor Kozlov
 */
public enum XSLFShapeType {
    LINE(1),
    LINE_INV(2),
    TRIANGLE(3),
    RT_TRIANGLE(4),
    RECT(5),
    DIAMOND(6),
    PARALLELOGRAM(7),
    TRAPEZOID(8),
    NON_ISOSCELES_TRAPEZOID(9),
    PENTAGON(10),
    HEXAGON(11),
    HEPTAGON(12),
    OCTAGON(13),
    DECAGON(14),
    DODECAGON(15),
    STAR_4(16),
    STAR_5(17),
    STAR_6(18),
    STAR_7(19),
    STAR_8(20),
    STAR_10(21),
    STAR_12(22),
    STAR_16(23),
    STAR_24(24),
    STAR_32(25),
    ROUND_RECT(26),
    ROUND_1_RECT(27),
    ROUND_2_SAME_RECT(28),
    ROUND_2_DIAG_RECT(29),
    SNIP_ROUND_RECT(30),
    SNIP_1_RECT(31),
    SNIP_2_SAME_RECT(32),
    SNIP_2_DIAG_RECT(33),
    PLAQUE(34),
    ELLIPSE(35),
    TEARDROP(36),
    HOME_PLATE(37),
    CHEVRON(38),
    PIE_WEDGE(39),
    PIE(40),
    BLOCK_ARC(41),
    DONUT(42),
    NO_SMOKING(43),
    RIGHT_ARROW(44),
    LEFT_ARROW(45),
    UP_ARROW(46),
    DOWN_ARROW(47),
    STRIPED_RIGHT_ARROW(48),
    NOTCHED_RIGHT_ARROW(49),
    BENT_UP_ARROW(50),
    LEFT_RIGHT_ARROW(51),
    UP_DOWN_ARROW(52),
    LEFT_UP_ARROW(53),
    LEFT_RIGHT_UP_ARROW(54),
    QUAD_ARROW(55),
    LEFT_ARROW_CALLOUT(56),
    RIGHT_ARROW_CALLOUT(57),
    UP_ARROW_CALLOUT(58),
    DOWN_ARROW_CALLOUT(59),
    LEFT_RIGHT_ARROW_CALLOUT(60),
    UP_DOWN_ARROW_CALLOUT(61),
    QUAD_ARROW_CALLOUT(62),
    BENT_ARROW(63),
    UTURN_ARROW(64),
    CIRCULAR_ARROW(65),
    LEFT_CIRCULAR_ARROW(66),
    LEFT_RIGHT_CIRCULAR_ARROW(67),
    CURVED_RIGHT_ARROW(68),
    CURVED_LEFT_ARROW(69),
    CURVED_UP_ARROW(70),
    CURVED_DOWN_ARROW(71),
    SWOOSH_ARROW(72),
    CUBE(73),
    CAN(74),
    LIGHTNING_BOLT(75),
    HEART(76),
    SUN(77),
    MOON(78),
    SMILEY_FACE(79),
    IRREGULAR_SEAL_1(80),
    IRREGULAR_SEAL_2(81),
    FOLDED_CORNER(82),
    BEVEL(83),
    FRAME(84),
    HALF_FRAME(85),
    CORNER(86),
    DIAG_STRIPE(87),
    CHORD(88),
    ARC(89),
    LEFT_BRACKET(90),
    RIGHT_BRACKET(91),
    LEFT_BRACE(92),
    RIGHT_BRACE(93),
    BRACKET_PAIR(94),
    BRACE_PAIR(95),
    STRAIGHT_CONNECTOR_1(96),
    BENT_CONNECTOR_2(97),
    BENT_CONNECTOR_3(98),
    BENT_CONNECTOR_4(99),
    BENT_CONNECTOR_5(100),
    CURVED_CONNECTOR_2(101),
    CURVED_CONNECTOR_3(102),
    CURVED_CONNECTOR_4(103),
    CURVED_CONNECTOR_5(104),
    CALLOUT_1(105),
    CALLOUT_2(106),
    CALLOUT_3(107),
    ACCENT_CALLOUT_1(108),
    ACCENT_CALLOUT_2(109),
    ACCENT_CALLOUT_3(110),
    BORDER_CALLOUT_1(111),
    BORDER_CALLOUT_2(112),
    BORDER_CALLOUT_3(113),
    ACCENT_BORDER_CALLOUT_1(114),
    ACCENT_BORDER_CALLOUT_2(115),
    ACCENT_BORDER_CALLOUT_3(116),
    WEDGE_RECT_CALLOUT(117),
    WEDGE_ROUND_RECT_CALLOUT(118),
    WEDGE_ELLIPSE_CALLOUT(119),
    CLOUD_CALLOUT(120),
    CLOUD(121),
    RIBBON(122),
    RIBBON_2(123),
    ELLIPSE_RIBBON(124),
    ELLIPSE_RIBBON_2(125),
    LEFT_RIGHT_RIBBON(126),
    VERTICAL_SCROLL(127),
    HORIZONTAL_SCROLL(128),
    WAVE(129),
    DOUBLE_WAVE(130),
    PLUS(131),
    FLOW_CHART_PROCESS(132),
    FLOW_CHART_DECISION(133),
    FLOW_CHART_INPUT_OUTPUT(134),
    FLOW_CHART_PREDEFINED_PROCESS(135),
    FLOW_CHART_INTERNAL_STORAGE(136),
    FLOW_CHART_DOCUMENT(137),
    FLOW_CHART_MULTIDOCUMENT(138),
    FLOW_CHART_TERMINATOR(139),
    FLOW_CHART_PREPARATION(140),
    FLOW_CHART_MANUAL_INPUT(141),
    FLOW_CHART_MANUAL_OPERATION(142),
    FLOW_CHART_CONNECTOR(143),
    FLOW_CHART_PUNCHED_CARD(144),
    FLOW_CHART_PUNCHED_TAPE(145),
    FLOW_CHART_SUMMING_JUNCTION(146),
    FLOW_CHART_OR(147),
    FLOW_CHART_COLLATE(148),
    FLOW_CHART_SORT(149),
    FLOW_CHART_EXTRACT(150),
    FLOW_CHART_MERGE(151),
    FLOW_CHART_OFFLINE_STORAGE(152),
    FLOW_CHART_ONLINE_STORAGE(153),
    FLOW_CHART_MAGNETIC_TAPE(154),
    FLOW_CHART_MAGNETIC_DISK(155),
    FLOW_CHART_MAGNETIC_DRUM(156),
    FLOW_CHART_DISPLAY(157),
    FLOW_CHART_DELAY(158),
    FLOW_CHART_ALTERNATE_PROCESS(159),
    FLOW_CHART_OFFPAGE_CONNECTOR(160),
    ACTION_BUTTON_BLANK(161),
    ACTION_BUTTON_HOME(162),
    ACTION_BUTTON_HELP(163),
    ACTION_BUTTON_INFORMATION(164),
    ACTION_BUTTON_FORWARD_NEXT(165),
    ACTION_BUTTON_BACK_PREVIOUS(166),
    ACTION_BUTTON_END(167),
    ACTION_BUTTON_BEGINNING(168),
    ACTION_BUTTON_RETURN(169),
    ACTION_BUTTON_DOCUMENT(170),
    ACTION_BUTTON_SOUND(171),
    ACTION_BUTTON_MOVIE(172),
    GEAR_6(173),
    GEAR_9(174),
    FUNNEL(175),
    MATH_PLUS(176),
    MATH_MINUS(177),
    MATH_MULTIPLY(178),
    MATH_DIVIDE(179),
    MATH_EQUAL(180),
    MATH_NOT_EQUAL(181),
    CORNER_TABS(182),
    SQUARE_TABS(183),
    PLAQUE_TABS(184),
    CHART_X(185),
    CHART_STAR(186),
    CHART_PLUS(187);

    private int _idx;

    XSLFShapeType(int idx){
        _idx = idx;
    }

    /**
     *
     * @return index in the STShapeType enum
     */
    int getIndex(){
        return _idx;
    }

    static XSLFShapeType forInt(int idx){
        for(XSLFShapeType t : values()){
            if(t._idx == idx) return t;
        }
        throw new IllegalArgumentException("Unknown shape type: " + idx);
    }
}
