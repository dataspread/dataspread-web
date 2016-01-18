<%@ page contentType="text/css;charset=UTF-8" %>
<%@ taglib uri="http://www.zkoss.org/dsp/web/core" prefix="c" %>
<%@ taglib uri="http://www.zkoss.org/dsp/zk/core" prefix="z" %>
<%@ taglib uri="http://www.zkoss.org/dsp/zss/core" prefix="s" %>

.zssheet {
	position: relative;
	width: 100px;
	height: 100px;
	background:#FFFFFF;
	overflow:hidden;
	border:1px solid #D8D8D8;
	box-sizing:border-box;
	-moz-box-sizing: border-box;
}

.zscave {
	background: #A4C3EB;
}

.zscenter {
	border:0;
	border-top:1px solid #C5C5C5;
}

.zsscroll {
	top: 0px;
	position: absolute;
	z-index: 0;
	overflow:auto;
	width: 100%;
	height:100%;
	left:0px;
	background-color:#FFFFFF;
}

.zsdata {
	left: 0px;
	top: 0px;
	display: block;
	position: relative;
	padding-top: 20px;
	padding-left: 36px;
	overflow: hidden;
	background-color:#FFFFFF;
}
.zsdatapad{
	position: relative;
	left: 0px;
	top: 0px;
	overflow: hidden;
	height:0px;
}
.zsleftpad{
	position: relative;
	left: 0px;
	top: 0px;
	overflow: hidden;
	height:0px;
}

.zsblock {
	left: 0px;
	top: 0px;
	display: block;
	position: relative;
	overflow: visible;
	background-color:#FFFFFF;
	user-select: none;
	-o-user-select:none;
	-moz-user-select: none;
	-khtml-user-select: none;
	-webkit-user-select: none;
}

.zsrow {
	position: relative;
	display: block;
	margin: 0px;
	padding: 0px;
	height: 20px; /*rowHeight*/
	overflow-y: visible;/*don't set hidden, otherwise sometime there will appear h-scrollbar in row*/
	white-space: nowrap;
	z-index: 1;
	<c:if test="${c:browser('ie') and not c:browser('ie11')}"> 
		font-size:0;
	</c:if>
	<c:if test="${c:isExplorer() and not c:isExplorer7() and not c:browser('ie11')}">
		position: static;
		overflow-y: hidden;
	</c:if>
}

.zscell {
	display: -moz-inline-box;
	-moz-box-sizing: border-box;
	box-sizing: border-box;
	border-right: 1px solid #CFCFCF;
	border-bottom: 1px solid #CFCFCF;
	padding: 0px 2px 0px 2px; /* cellPadding */
	height: 20px; /*rowHeight*/
	width: 64px; /*colWidth*/
	overflow: hidden;
	vertical-align: top;
	/*font-size: smaller;*/
	/*line-height: 20px;  lineHeight*/
	z-index:10;
	position: relative;
	cursor : default;
	display: inline-block;
	<c:if test="${c:browser('ie') and not c:browser('ie11')}">
		zoom: 1;
		*display: inline;
	</c:if>
	<c:if test="${c:isOpera()}">
		vertical-align:text-top;
		/*vertical-align: top;*//* DIFF : important, vertical align will cause opera scrollbar disappear*/
	</c:if>
}

/* ZSS-568: double border 15 combinations*/
.zscell.tlbr:before {
    content:"";
    position:absolute;
    z-index:-10;
    top:0px;left:0px;
    bottom:1px;right:1px;
    border-top:1px solid #f00;
    border-left:1px solid #f00;
    border-bottom:1px solid #f00;
    border-right:1px solid #f00;
    
    border-top-color:inherit;
    border-left-color:inherit;
    border-bottom-color:inherit;
    border-right-color:inherit;
}

.zscell._lbr:before {
    content:"";
    position:absolute;
    z-index:-10;
    top:0px;left:0px;
    bottom:1px;right:1px;
    border-left:1px solid #f00;
    border-bottom:1px solid #f00;
    border-right:1px solid #f00;
    
    border-left-color:inherit;
    border-bottom-color:inherit;
    border-right-color:inherit;
}

.zscell.t_br:before {
    content:"";
    position:absolute;
    z-index:-10;
    top:0px;left:0px;
    bottom:1px;right:1px;
    border-top:1px solid #f00;
    border-bottom:1px solid #f00;
    border-right:1px solid #f00;
    
    border-top-color:inherit;
    border-bottom-color:inherit;
    border-right-color:inherit;
}

.zscell.tl_r:before {
    content:"";
    position:absolute;
    z-index:-10;
    top:0px;left:0px;
    bottom:0px;right:1px;
    border-top:1px solid #f00;
    border-left:1px solid #f00;
    border-right:1px solid #f00;
    
    border-top-color:inherit;
    border-left-color:inherit;
    border-right-color:inherit;
}

.zscell.tlb_:before {
    content:"";
    position:absolute;
    z-index:-10;
    top:0px;left:0px;
    bottom:1px;right:0px;
    border-top:1px solid #f00;
    border-left:1px solid #f00;
    border-bottom:1px solid #f00;
    
    border-top-color:inherit;
    border-left-color:inherit;
    border-bottom-color:inherit;
}

.zscell.__br:before {
    content:"";
    position:absolute;
    z-index:-10;
    top:0px;left:0px;
    bottom:1px;right:1px;
    border-bottom:1px solid #f00;
    border-right:1px solid #f00;
    
    border-bottom-color:inherit;
    border-right-color:inherit;
}

.zscell._l_r:before {
    content:"";
    position:absolute;
    z-index:-10;
    top:0px;left:0px;
    bottom:0px;right:1px;
    border-left:1px solid #f00;
    border-right:1px solid #f00;
    
    border-left-color:inherit;
    border-right-color:inherit;
}

.zscell._lb_:before {
    content:"";
    position:absolute;
    z-index:-10;
    top:0px;left:0px;
    bottom:1px;right:0px;
    border-left:1px solid #f00;
    border-bottom:1px solid #f00;
    
    border-left-color:inherit;
    border-bottom-color:inherit;
}

.zscell.t__r:before {
    content:"";
    position:absolute;
    z-index:-10;
    top:0px;left:0px;
    bottom:0px;right:1px;
    border-top:1px solid #f00;
    border-right:1px solid #f00;
    
    border-top-color:inherit;
    border-right-color:inherit;
}

.zscell.t_b_:before {
    content:"";
    position:absolute;
    z-index:-10;
    top:0px;left:0px;
    bottom:1px;right:0px;
    border-top:1px solid #f00;
    border-bottom:1px solid #f00;
    
    border-top-color:inherit;
    border-bottom-color:inherit;
}

.zscell.tl__:before {
    content:"";
    position:absolute;
    z-index:-10;
    top:0px;left:0px;
    bottom:0px;right:0px;
    border-top:1px solid #f00;
    border-left:1px solid #f00;
    
    border-top-color:inherit;
    border-left-color:inherit;
}

.zscell.___r:before {
    content:"";
    position:absolute;
    z-index:-10;
    top:0px;left:0px;
    bottom:0px;right:1px;
    border-right:1px solid #f00;
    
    border-right-color:inherit;
}

.zscell.__b_:before {
    content:"";
    position:absolute;
    z-index:-10;
    top:0px;left:0px;
    bottom:1px;right:0px;
    border-bottom:1px solid #f00;
    
    border-bottom-color:inherit;
}

.zscell._l__:before {
    content:"";
    position:absolute;
    z-index:-10;
    top:0px;left:0px;
    bottom:0px;right:1px;
    border-left:1px solid #f00;
    
    border-left-color:inherit;
}

.zscell.t___:before {
    content:"";
    position:absolute;
    z-index:-10;
    top:0px;left:0px;
    bottom:1px;right:0px;
    border-top:1px solid #f00;
    
    border-top-color:inherit;
}

/* ZSS-901: auto filter border 15 combinations*/
.zscell.aftlbr:after {
    content:"";
    position:absolute;
    z-index:-10;
    top:0px;left:0px;
    bottom:0px;right:0px;
    border-top:2px dashed #0088B0;
    border-left:2px dashed #0088B0;
    border-bottom:2px dashed #0088B0;
    border-right:2px dashed #0088B0;
}

.zscell.af_lbr:after {
    content:"";
    position:absolute;
    z-index:-10;
    top:0px;left:0px;
    bottom:0px;right:0px;
    border-left:2px dashed #0088B0;
    border-bottom:2px dashed #0088B0;
    border-right:2px dashed #0088B0;
}

.zscell.aft_br:after {
    content:"";
    position:absolute;
    z-index:-10;
    top:0px;left:0px;
    bottom:0px;right:0px;
    border-top:2px dashed #0088B0;
    border-bottom:2px dashed #0088B0;
    border-right:2px dashed #0088B0;
}

.zscell.aftl_r:after {
    content:"";
    position:absolute;
    z-index:-10;
    top:0px;left:0px;
    bottom:0px;right:0px;
    border-top:2px dashed #0088B0;
    border-left:2px dashed #0088B0;
    border-right:2px dashed #0088B0;
}

.zscell.aftlb_:after {
    content:"";
    position:absolute;
    z-index:-10;
    top:0px;left:0px;
    bottom:0px;right:0px;
    border-top:2px dashed #0088B0;
    border-left:2px dashed #0088B0;
    border-bottom:2px dashed #0088B0;
}

.zscell.af__br:after {
    content:"";
    position:absolute;
    z-index:-10;
    top:0px;left:0px;
    bottom:0px;right:0px;
    border-bottom:2px dashed #0088B0;
    border-right:2px dashed #0088B0;
}

.zscell.af_l_r:after {
    content:"";
    position:absolute;
    z-index:-10;
    top:0px;left:0px;
    bottom:0px;right:0px;
    border-left:2px dashed #0088B0;
    border-right:2px dashed #0088B0;
}

.zscell.af_lb_:after {
    content:"";
    position:absolute;
    z-index:-10;
    top:0px;left:0px;
    bottom:0px;right:0px;
    border-left:2px dashed #0088B0;
    border-bottom:2px dashed #0088B0;
}

.zscell.aft__r:after {
    content:"";
    position:absolute;
    z-index:-10;
    top:0px;left:0px;
    bottom:0px;right:0px;
    border-top:2px dashed #0088B0;
    border-right:2px dashed #0088B0;
}

.zscell.aft_b_:after {
    content:"";
    position:absolute;
    z-index:-10;
    top:0px;left:0px;
    bottom:0px;right:0px;
    border-top:2px dashed #0088B0;
    border-bottom:2px dashed #0088B0;
}

.zscell.aftl__:after {
    content:"";
    position:absolute;
    z-index:-10;
    top:0px;left:0px;
    bottom:0px;right:0px;
    border-top:2px dashed #0088B0;
    border-left:2px dashed #0088B0;
}

.zscell.af___r:after {
    content:"";
    position:absolute;
    z-index:-10;
    top:0px;left:0px;
    bottom:0px;right:0px;
    border-right:2px dashed #0088B0;
}

.zscell.af__b_:after {
    content:"";
    position:absolute;
    z-index:-10;
    top:0px;left:0px;
    bottom:0px;right:0px;
    border-bottom:2px dashed #0088B0;
}

.zscell.af_l__:after {
    content:"";
    position:absolute;
    z-index:-10;
    top:0px;left:0px;
    bottom:0px;right:0px;
    border-left:2px dashed #0088B0;
}

.zscell.aft___:after {
    content:"";
    position:absolute;
    z-index:-10;
    top:0px;left:0px;
    bottom:0px;right:0px;
    border-top:2px dashed #0088B0;
}


.zscell-overflow {
	border-right: 1px;
	border-right-style:hidden;
	overflow: visible;
	position: relative;
    z-index: 11; /* ZSS-917 see zscell's z-index */ 
}

.zscell-overflow-b {
	overflow: visible;
	position: relative;
	z-index: 11; /* ZSS-917 see zscell's z-index */
}

.zscelltxt {
	font-family: Calibri;
	font-size: 11pt;
	text-align: left;
	width: 49px; /* colWidth - 2*cellPadding - 1 , 1 is border*/
	overflow: hidden;
	line-height: normal; /*zss-598, reset to avoid zk's effect*/
	<c:if test="${c:browser('ie7-') || c:browser('ie6-')}">
		position: absolute;
		zoom: 1;
	</c:if>
}

.zscelltxt-real {
	width: 100%;
	<c:if test="${c:browser('ie7-') || c:browser('ie6-')}">
		left: 0;
		position: absolute;
	</c:if>
}

/* ZSS-918: vertical text */
.zsvtxt {
    display: -moz-inline-box;
    width:1em;
    font-size:inherit;
    word-wrap:break-word;
    white-space:normal;
    letter-spacing:1em;
    display: inline-block;
    <c:if test="${c:browser('ie') and not c:browser('ie11')}">
        zoom: 1;
        *display: inline;
    </c:if>
}

.zscelltxt-wrap{
	word-wrap:break-word;
	width:inherit;
}

.zstop {
	position: absolute;
	z-index: 2;
	left: 36px; /*leftWidth*/
	top: 0px;
	height: 19px; /*topHeidht -2 , 1 is border*/
	/*line-height: 20px; lineHeight*/
	overflow: hidden;
	border-top: 1px solid #CFCFCF;
	/*border-bottom: 1px solid #7F9DB9;*/
	background: #FFFFFF none repeat scroll 0%;
}
.zscornertop{
	position: absolute;
	left: 36px; /*leftWidth*/
	top: 0px;
	height: 19px; /*topHeidht -2 , 1 is border*/
	overflow: hidden;
	border-top: 1px solid #CFCFCF;
	background: #FFFFFF none repeat scroll 0%;
}


.zstopi, .zscornertopi{
	position: relative;
	top: 0px;
	left: 0px;
	height: 100%;
	display: block;
	margin: 0px;
	padding: 0px;
	overflow-y: visible;
	white-space: nowrap;
}

.zstophead {
	overflow: visible;
}

.zstopblock {
	left: 0px;
	top: 0px;
	display: block;
	position: relative;
	overflow: visible;
	background-color:#FFFFFF;
}

.zstopcell {
	display: -moz-inline-box;
	-moz-box-sizing: border-box;
	box-sizing: border-box;
	border-right: 1px solid #CFCFCF;
	border-bottom: 1px solid #BBBBBB;
	padding: 0px 2px 0px 2px; /*cellPadding*/
	height: 19px; /*topHeigth*/
	width: 64px; /*cellWidth*/
	overflow: hidden;
	vertical-align: top;
	/*line-height: 20px; lineHeigth*/
	background: #FFFFFF;
	overflow: visible;
	position:relative;
	cursor : default;
	display: inline-block;
	color: #5E5E5E;
}

.zstopcelltxt {
	font-size: 12px;
	text-align: center;
	width: 49px;  /* colWidth - 2*cellPadding - 1 , 1 is border*/
	overflow: hidden;
}

.zsleft{
	position: absolute;
	z-index: 2;
	top: 20px; /* topHeight */
	left: 0px;
	width: 35px; /* leftWith - 2 , 2 is border */
	overflow: hidden;
	border-left: 1px solid #CFCFCF;
	background: #CFCFCF none repeat scroll 0%;
}

.zscornerleft  {
	position: absolute;
	z-index: 2;
	top: 20px; /* topHeight */
	left: 0px;
	width: 34px; /* leftWith - 2 , 2 is border */
	overflow: hidden;
	border-left: 1px solid #CFCFCF;
	background: #FFFFFF none repeat scroll 0%;
}

.zslefti, .zscornerlefti {
	position: relative;
	top: 0px;
	left: 0px;
}


.zsleftblock {
	left: 36px;
	top: 0px;
	display: block;
	position: absolute;
	overflow: visible;
	background-color:#FFFFFF;
}

.zslefthead {
	background-color: #FFFFFF;
	overflow: visible;
}

.zsleftcell {
	-moz-box-sizing: border-box;
	box-sizing: border-box;
	border-right: 1px solid #BBBBBB;
	/*line-height: 20px;*/
	height: 19px; /* rowHeight - 1, 1 is border */
	border-bottom: 1px solid #CFCFCF;
	text-align: center;
	overflow: visible;
	cursor : default;
	color: #5E5E5E;
}
.zsleftcelltxt {
	font-size: 12px;
	/* height: 19px;*/ /* rowHeight - 1, 1 is border */
	text-align: center;
	<c:if test="${c:browser('ie') and not c:browser('ie11')}">
		font-size: 10px;
	</c:if>
	overflow: hidden;
}

.zstop-sel,
.zsleft-sel {
	background-color: #E1E1E1;
	color: #008DB7;
	font-weight: bold;
}

/* Header Selected bar */
.zstop-sel:after,
.zsleft-sel:after {
	content: '';
	background: #008DB7;
	position: absolute;
}

.zstop-sel:after {
	bottom: -1px;
	left: 0;
	height: 2px;
	width: 100%;
	border-right: 1px solid #008DB7;
}

.zsleft-sel:after {
	top: 0;
	right: -1px;
	width: 2px;
	height: 100%;
	border-bottom: 1px solid #008DB7;
}

.zsscrollinfo{
	z-index:3;
	position: absolute;
	top:0px;
	left:0px;
	width:400px;
	padding-top:1px;
	padding-bottom:1px;
	display:none;
}
.zsscrollinfoinner{
	background-color: #E1E1E1;
	font-family: "Segoe UI", Tahoma, Thonburi, Arial, Verdana, sans-serif;
	font-size:x-small;
	padding-left:5px;
	padding-right:5px;
	color: #5E5E5E;
	border:1px outset #BBBBBB;
}

.zsinfo{
	z-index:4;
	position: absolute;
	top:1px;
	left:2px;
	width:400px;
	padding-top:2px;
	padding-bottom:2px;
	display:none;
}
.zsinfoinner{
	background-color:#FFD5AD;
	font-family: "Segoe UI", Tahoma, Thonburi, Arial, Verdana, sans-serif;
	font-size:small;
	padding-left:5px;
	padding-right:5px;
	border:1px outset #555555;
}

.zscorner {
	position: absolute;
	z-index: 2;
	width: 36px; /* leftWidth - 2, 2 is border */
	height: 20px; /* topHeight - 2, 2 is border */
	background-color: #CFCFCF;
	overflow: hidden;
	<c:if test="${c:browser('ie') and not c:browser('ie11')}">
		*font-size: 0;
	</c:if>
}

.zscorneri {
	border: 1px solid #CFCFCF;
	/*background-image: url(${c:encodeURL('~./zss/img/s_hd.gif')});*/
	width:34px;
	height:18px;
	background-color: #FFFFFF;
}

.zscornerblock {
	left: 20px;
	top: 36px;
	display: block;
	position: absolute;
	overflow: hidden;
	background-color:#FFFFFF;
}

.zsfocmark {
	-moz-box-sizing: border-box;
	box-sizing: border-box;
	position: absolute;
	z-index: 2;
	display: none;
	border: 3px solid #008DB7;
	<c:if test="${c:browser('ie') and not c:browser('ie11')}">
		font-size: 0;
	</c:if>
}
.zsfocmarki{
	position: absolute;
	width:100%;
	height:100%;
	opacity: 0; /* ZSS-286: it's for modern browser (including IE 9+) */ 
	<c:if test="${c:browser('ie') and not c:browser('ie11')}">
		font-size: 0;
		filter: alpha(opacity=0);
		background: #FFFFFF;
	</c:if>
}
.zsfocmarkl{
	position: relative;
	font-size: x-small;
	font-family: "Segoe UI", Tahoma, Thonburi, Arial, Verdana, sans-serif;
	padding: 1px 3px;
	top:-1px;
}
.zsfocmarkl-lite{
	color: #EEE;
}
.zsfocmarkl-dark{
	color: #222;
}
.zsselect {
	-moz-box-sizing: border-box;
	box-sizing: border-box;
	position: absolute;
	z-index: 3;
	display: none;
	cursor: move;
	border: 3px solid #008DB7;
	<c:if test="${c:browser('ie') and not c:browser('ie11')}">
		font-size: 0;
	</c:if>
	<c:if test="${c:isGecko3()}">
		-moz-user-select: none;
	</c:if>
}
.zsselecti{
	position: absolute;
	cursor: default;
	width:100%;
	height:100%;
	opacity: 0; /* ZSS-286: it's for modern browser (including IE 9+) */ 
	<c:if test="${c:browser('ie') and not c:browser('ie11')}">
		font-size: 0;
		filter: alpha(opacity=0);
		background: #FFFFFF;
	</c:if>
}

.zsselecti-r{
	background-color: #E1E1E1;
	opacity:.5;
	<c:if test="${c:browser('ie') and not c:browser('ie11')}">
		filter: alpha(opacity=50);
	</c:if>
}

.zsseldot {
	position: absolute;
	width: 5px;
	height: 5px;
	right: -5px;
	bottom: -5px;
	border: 1px solid #FFFFFF;
	background-color: #008DB7;
	cursor: crosshair;
	<c:if test="${c:browser('ie') and not c:browser('ie11')}">
		font-size: 0;
	</c:if>
}

.zsselchg {
	-moz-box-sizing: border-box;
	box-sizing: border-box;
	position: absolute;
	z-index: 4;
	display: none;
	border: 3px solid #008DB7;
	
}

.zsselchgi{
	position: absolute;
	width:100%;
	height:100%;
}

.zshighlight {
	-moz-box-sizing: border-box;
	box-sizing: border-box;
	position: absolute;
	z-index: 2;
	display: none;
	border: 3px dotted #909090;
}

.zshighlight2 {
	border-color: #008DB7;
}

.zshighlighti{
	position: absolute;
	width:100%;
	height:100%;
}

.zsedit-info {
	position: absolute;
	display: none;
	font-family: "Segoe UI", Tahoma, Thonburi, Arial, Verdana, sans-serif;
	font-weight: bold;
	font-size: 12px;
	white-space: pre-wrap;
	padding: 4px 8px 2px 8px;
	background-color: #FFDF25;
	border-top-left-radius: 6px;
	border-top-right-radius: 6px;
	pointer-events: none;
	z-index: 4;
}

.zsedit-real {
	position: absolute;
	font-family: Calibri;
	font-size: 15px;
	border: none;
	//display: none;
	background-color: white;
	overflow: hidden;
	border: 1px solid #008DB7;
	white-space: pre-wrap;
	z-index: 4;
	top : -10005px;
	left : -10005px;
}

.zsfocus{
	position:absolute;
	left : -10005px; /* ZSS-662 */
	width: 9999px;  /* ZSS-462 workaround for wrong selection caused by mouse position */ 
	height: 0px;
}


.zshboun{
	display: -moz-inline-box;
	position:relative;
	height:20px;
	width:0px;
	left:-3px;
	z-index:2;
	vertical-align:top;
	display: inline-block;
	<c:if test="${c:browser('ie') and not c:browser('ie11')}">
		zoom: 1;
		*display: inline;
	</c:if>
}
.zshbouni{
	position: relative;
	height:100%;
	width:7px;
	display: inline-block;
	<c:if test="${c:browser('ie') and not c:browser('ie11')}">
		*position: absolute;
		zoom: 1;
		*display: inline;
	</c:if>
}
.zsvboun{
	position:relative;
	height:0px;
	top:-3px;
	z-index:2;
}
.zsvbouni{
	position: relative;
	height:7px;
	width:100%;
	<c:if test="${c:browser('ie') and not c:browser('ie11')}">
		*position: absolute;
		font-size: 0;
	</c:if>
}
.zshbouni-over{
	background: #CFCFCF;
	cursor: e-resize;
}
.zsvbouni-over{
	background: #CFCFCF;
	cursor: n-resize;
}
.zshbounw{
	position: relative;
	height:100%;
	width:3px;
	display: inline-block;
	<c:if test="${c:browser('ie') and not c:browser('ie11')}">
		zoom: 1;
		*display: inline;
		*position: absolute;
		*left:7px;
	</c:if>
}
.zshbounw-over{
	background: #A9C4E9;
	cursor: url(${c:encodeURL('~./zss/img/h_resize.cur')}), e-resize;
}
.zsvbounw{
	position: relative;
	height:3px;
	width:100%;
	<c:if test="${c:browser('ie') and not c:browser('ie11')}">
		font-size: 0;
		*position: absolute;
		*top:7px;
	</c:if>
}
.zsvbounw-over{
	background: #CFCFCF;
	cursor: url(${c:encodeURL('~./zss/img/v_resize.cur')}), n-resize;
}
.zsfztop{
	border-bottom : 2px #AAAAAA;
	border-bottom-style:none;
}

.zsfzleft{
	border-right : 2px #AAAAAA;
	border-right-style:none;
}

.zsfzcorner{
	border-right : 2px #AAAAAA;
	border-bottom : 2px #AAAAAA;
	border-right-style:none;
	border-bottom-style:none;
}
.zssmask{
	position:absolute;
	width:100%;
	height:100%;
	z-index:999;
	background-color : #CFCFCF;
	opacity:.75;
	display:table;
	<c:if test="${c:browser('ie') and not c:browser('ie11')}">
		filter:alpha(opacity=75);
	</c:if>
}
.zssmask2{
	display:table-cell;
	vertical-align:middle;
}
.zssmasktxt{
	font-size:20px;
	font-style:bold;
	color:white;
}

.zsmergee{
	display:none!important;
}

.zsmergeeu{
	visibility:hidden!important;
}

.zswidgetpanel{
	position:absolute;
	width:0px;
	height:0px;
	top:0px;
	left:0px;
	z-index:10;
	overflow:visible;
}

.zswidget{
	position:absolute;
	top:0px;
	left:0px;
	z-index:10;
	visibility:hidden;/* hidden when loading, javascript will show it after init*/
}

.zswidget-drag {
	background: #FFFFFF;
	border: 2px solid #333333;
	opacity:.5;
	<c:if test="${c:browser('ie') and not c:browser('ie11')}">
		filter: alpha(opacity=50);
	</c:if>
}

.zswidget-focus {
	border: 1px solid #CFDDDE;
	border-radius: 1px;
}

.zswidget-ifocus {
	position: absolute;	
}

.zswidget-real, .zswidget-real a:hover{
	cursor: move;
}

.zswidget-focus .zswidget-cave {
	border: 5px solid #E8FDFD;
}

.zswidget-chart .z-charts {
	border: 1px solid #868686;
}

//.zswidget-img { //ZSS-578: no border around picture
//	border: 1px solid transparent;
//}

.zsbtn {
	height:17px;
	width:17px;
	position: absolute;
	bottom: 2px;
	z-index: 12;
	background-repeat:no-repeat;
	background-size: 100%;
}

.zsdropdown {
	background-image: url(${c:encodeURL('~./zss/img/dropdown.gif')});
}

.zsfilter {
	background-image: url(${c:encodeURL('~./zss/img/filter.gif')});
}

.zspopuppanel {
	position:absolute;
	width:0px;
	height:0px;
	top:0px;
	left:0px;
	overflow:visible;
	z-index:10;
}

.zscellpopup {
	overflow:hidden;
	position:absolute;
	top:0px;
	left:0px;
	z-index:10;
	background: #FFFFFF;
	border: 1px solid #868686;
	box-shadow: 1px 1px 2px rgba(0, 0, 0, 0.5);
}

.zsdv-popup-cave {
	margin: 0;
	padding: 0;
}

.zsdv-item {
	list-style: none;
	padding: 2px;
	margin: 0;
	font-size: 12px;
	height: 12px;
}

.zsdv-item a.zsdv-item-text {
	text-decoration: none !important;
	white-space: nowrap;
	color: #000000;
}

.zsdv-item-over, .zsdv-item-over a.zsdv-item-text {
	color: #FFFFFF;
	background: #3399FF;
} 

.zsdvp-popup {
	background: #FFFFE1;
	<c:if test="${c:browser('ie7-') || c:browser('ie6-') || c:isOpera()}">
		width: 200px;
		height: 90px;
		overflow: auto;
	</c:if>
}

.zsdvp-popup-cave {
	padding: 1px 2px;
	background: #FFFFE1;
	display: block;
	overflow: auto;
}

.zsdvp-title {
	font-weight: bold;
	color: #333333;
	padding: 2px 2px 0;
	word-wrap: break-word;
	zoom: 1;
}

.zsdvp-text {
	padding: 0 2px 2px;
	color: #555555;
	word-wrap: break-word;
	zoom: 1;
}

.zsafp-popup, .zsafp-popup-cave {
	height: 235px;
	width: 240px;
}

.zsafp-search {
	border-bottom: 1px solid #D8D8D8;
	width: 100%;
	padding: 2px;
}

.zsafp-search-inp {
	background: #FFFFFF;
	border: none;
	font-size: 12px;
	width: 215px;
}

.zsafp-items {
	overflow-y: auto;
	overflow-x: hidden;
	height: 158px;
	width: 100%;
	border-bottom: 1px solid #D8D8D8;
	position: relative;
}

.zsafp-item {
	padding: 2px 2px 2px 20px;
	position: relative;
	background-position: left center;
	font-size: 13px;
	cursor: pointer;
	<c:if test="${c:browser('ie6-')}">
		padding-left: 2px;
	</c:if>
}

.zsafp-item-over {
	background: #D6F0FD;
}

.zsafp-item-hide {
	display: none;
}

.zsafp-item-checkbox {
	height: 16px;
	width: 16px;
	background-repeat: no-repeat;
	position: absolute;
	left: 3px;
	top: 2px;
	background: url(${c:encodeURL('~./zss/img/ui-check-box-uncheck.png')}) no-repeat transparent;
	<c:if test="${c:browser('ie6-')}">
		background-image: url(${c:encodeURL('~./zss/img/ui-check-box-uncheck.gif')});
	</c:if>
}

<c:if test="${c:browser('ie6-')}">
.zsafp-item-text {
	margin-left: 22px;
}
</c:if>

.zsafp-item-seld .zsafp-item-checkbox {
	background: url(${c:encodeURL('~./zss/img/ui-check-box.png')}) no-repeat transparent;
	<c:if test="${c:browser('ie6-')}">
		background-image: url(${c:encodeURL('~./zss/img/ui-check-box.gif')});
	</c:if>
}

.zsafp-item-seld-mix .zsafp-item-checkbox {
	background: url(${c:encodeURL('~./zss/img/ui-check-box-mix.png')}) no-repeat transparent;
	<c:if test="${c:browser('ie6-')}">
		background-image: url(${c:encodeURL('~./zss/img/ui-check-box-mix.gif')});
	</c:if>
}
	
.zsafp-search-icon {
	background: url(${c:encodeURL('~./zss/img/magnifier-zoom.png')}) no-repeat transparent;
	display: inline-block;
	position: absolute;
	right: 4px;
	top: 4px;
	width: 16px;
	height: 16px;
	<c:if test="${c:browser('ie6-')}">
		background-image: url(${c:encodeURL('~./zss/img/magnifier-zoom.gif')});
	</c:if>
}

.zsafp-btns {
	padding: 10px 12px;
	position: absolute;
	right: 0;
	<c:if test="${c:isExplorer() and not c:browser('ie8') and not c:browser('ie11')}">
		width: 150px;
		height: 50px;
	</c:if>
}

.zsafp-btn {
	cursor: pointer;
	border-radius: 2px;
	height: 27px;
	width: 50px;
	padding: 0 8px;
	display: inline-block;
	<c:if test="${c:isExplorer() and not c:browser('ie8') and not c:browser('ie11')}">
		position: absolute;
	</c:if>
	border: 1px solid #C7C7C7;
	margin: 0 8px;
}

.zsafp-btn-disd {
	cursor: default;
	opacity: 0.6;
	<c:if test="${c:browser('ie') and not c:browser('ie11')}">
		filter: alpha(opacity=60);
	</c:if>
}

.zsafp-ok-btn {
	background: url(${c:encodeURL('~./zss/img/tick.png')}) no-repeat 25px 5px #EFEFEF;
	<c:if test="${c:isExplorer() and not c:browser('ie8') and not c:browser('ie11')}">
		left: 0;
	</c:if>
	<c:if test="${c:browser('ie6-')}">
		background-image: url(${c:encodeURL('~./zss/img/tick.gif')});
	</c:if>
}

.zsafp-cancel-btn {
	background: url(${c:encodeURL('~./zss/img/cross.png')}) no-repeat 25px 5px #EFEFEF;
	<c:if test="${c:isExplorer() and not c:browser('ie8') and not c:browser('ie11')}">
		right: 8px;
	</c:if>
	<c:if test="${c:browser('ie6-')}">
		background-image: url(${c:encodeURL('~./zss/img/cross.gif')});
	</c:if>
}

.zsnamebox {
	width: 100%;
	position: relative;
	background: #FFFFFF;
	border: 1px solid #D8D8D8;
	border-top: 0;
	border-left: 0;
	padding-bottom: 1px;
	<c:if test="${c:browser('ie7-') || c:browser('ie6-')}">
		height: 24px;
	</c:if>
}
.zsnamebox-inp {
	font-size: 14px;
	border: 0;
	height: 14px;
	padding: 5px 4px;
	text-align: center;
}
.zsnamebox-disd .zsnamebox-inp {
	cursor: default;
}
.zsnamebox-icon {
	background: url(${c:encodeURL('~./zss/img/namebox-arrow.png')}) no-repeat;
	position: absolute;
	right: 0;
	top: 0;
	width: 24px;
	height: 24px;
	cursor: pointer;
	<c:if test="${c:browser('ie6-')}">
		background-image: url(${c:encodeURL('~./zss/img/namebox-arrow.gif')});
	</c:if>
}
.zsnamebox-disd .zsnamebox-icon {
	cursor: default;
	opacity: 0.5;
	<c:if test="${c:browser('ie') and not c:browser('ie11')}">
		filter: alpha(opacity=50);
	</c:if>
}
.zsnamebox-pp {
	position:absolute;
	background: #FFFFFF;
	border: 1px solid #868686;
	box-shadow: 1px 1px 2px rgba(0, 0, 0, 0.5);
	z-index: 20;
}
.zsnamebox-item-over {
	background: #3399FF;
	color: #FFFFFF;
}
.zsformulabar-blank {
	width: 100%;
	height: 100%;
  	background-color: #F9F9F9;
}

.zsformulabar-okbtn .z-toolbarbutton-content, .zsformulabar-cancelbtn .z-toolbarbutton-content {
	width: 20px;
	height: 17px;
	font-size: 14px;
	text-align: center;
	padding-top: 0;
	padding-bottom: 4px;
	<c:if test="${c:browser('ie') and not c:browser('ie11')}">
		padding-top: 1px;
		padding-bottom: 3px;
	</c:if>
	<c:if test="${c:browser('ie6-')}">
		font-family: Lucida Sans Unicode;
	</c:if>
}
.zsformulabar-insertbtn .z-toolbarbutton-content {
	font-size: 14px;
	font-style: italic;
	text-align: center;
	padding-top: 0;
	padding-bottom: 4px;
	<c:if test="${c:browser('ie') and not c:browser('ie11')}">
		padding-top: 1px;
		padding-bottom: 3px;
	</c:if>
}
.zsformulabar-insertbtn .zsformulabar-insertbtn-body-over .z-toolbarbutton-content,
.zsformulabar-okbtn .zsformulabar-okbtn-body-over .z-toolbarbutton-content,
.zsformulabar-cancelbtn .zsformulabar-cancelbtn-body-over .z-toolbarbutton-content {
	padding-top: 0;
	padding-bottom: 4px;
	<c:if test="${c:browser('ie') and not c:browser('ie11')}">
		padding-top: 1px;
		padding-bottom: 3px;
	</c:if>
}
.zsformulabar-buttons {
	position: absolute;
	top: 0;
	right: -1px;
	padding-left: 4px;
	padding-top: 1px;
	height: 24px;
	background: #F7F7F7;
	border-bottom: 1px solid #D8D8D8;
}
.zsformulabar-west {
	border-right: 0;
}
.zsformulabar-center-cave-colbtn {
	position: absolute;
	right: 0;
	top: 0;
	height: 100%;
	padding-top: 1px;
	border-left: 1px solid #D8D8D8;
	background: #F5F5F5;
}
.zsformulabar-expandbtn-body-over {
	background: url(${c:encodeURL('~./zss/img/toolbarbtn-ctr.gif')}) 0 -500px;
}
.zsformulabar-expandbtn .z-toolbarbutton-content {
	display: inline-block;
	width: 24px;
	height: 21px;
	padding: 2px;
	background: url(${c:encodeURL('~./zss/img/arrow-270-small.png')}) no-repeat 4px 2px transparent;
	<c:if test="${c:browser('ie6-')}">
		background-image: url(${c:encodeURL('~./zss/img/arrow-270-small.gif')});
	</c:if>	
}
.zsformulabar-expandbtn-expanded .z-toolbarbutton-content {
	background: url(${c:encodeURL('~./zss/img/arrow-090-small.png')}) no-repeat 4px 2px transparent;
	<c:if test="${c:browser('ie6-')}">
		background-image: url(${c:encodeURL('~./zss/img/arrow-090-small.gif')});
	</c:if>		
}
.zsformulabar-editor {
	border: 1px solid #D8D8D8;
	border-top: 0;
	overflow-x: hidden;
	overflow-y: auto;
	margin-top: -1px;
}
.zsformulabar-editor-real, .zsformulabar-editor-support {
	border: 0;
	font-size: 12px;
	line-height: 1.5;
	padding: 3px 3px 0 3px; /* ZSS-205: make padding be consistent */
	margin: 0; /* make sure zero margin (or Firefox will append 2px margin) */
	font-family: arial,sans-serif;
	position: relative;
	top: 0;
	left: 0;
	/* ZSS-205: hide scrollbar*/
	overflow: hidden; 
	white-space: pre-wrap;
	word-wrap: break-word;
	word-break: break-all;
	/* ZSS-205: no necessary to adjust padding for different browsers */
}

.zsformulabar-editor-support {
	visibility: hidden;
	width: 0;
	height: 0;
	min-width: 0;
	min-height: 0;
}

.zsformulabar-editor-wrap-support {
	visibility: hidden;
	width: 0;
	height: 0;
	overflow: scroll;
	position: relative;
	top: -9000px; /* make it out of formula bar */
	left: -9000px; 
}

.zssheetbar,
.zssheetselector .z-tabs-content,
.zssheetselector .z-tab-hl,
.zssheetselector .z-tab-hr,
.zssheetselector .z-tab-hm {
	background: #F9F9F9;
}

.zssheetbar .z-tabbox-left-scroll, .zssheetbar .z-tabbox-right-scroll {
	padding-top: 2px;
}

.zssheetselector .zstbtn-addSheet .z-toolbarbutton-content {
	padding: 0 2px;
}

.zssheetselector .z-tab {
	border: 1px solid #D4D4D4;
	border-top: 0;
}

.zssheetselector .z-tab-text {
	padding: 0px 16px 1px;
	line-height: 24px;
	font-size: 13px;
}

.zssheettab-rename-textbox {
	font-size: 13px;
	line-height: 14px;
	padding: 0 5px;
	min-height: 20px;
}

.zssheetselector .z-tab-seld, 
.zssheetselector .z-tab-seld .z-tab-hl, 
.zssheetselector .z-tab-seld .z-tab-hr, 
.zssheetselector .z-tab-seld .z-tab-hm {
	background-color: #FFFFFF;
}

.zssheetselector .z-tabs-left-scroll,
.zssheetselector .z-tabs-right-scroll {
	margin-top: -3px;
}

<c:if test="${c:isGecko()}">
.zssheetselector .z-tabs-cnt {
	-moz-user-select: text;
}
.zssheetselector .z-tab {
	-moz-user-select: text;
}
</c:if>	

.zssheet-editor {
	padding: 1px 0;
}

.zstbtn {
	height: 28px;
	padding-top: 0;
	padding-bottom: 0;
}

.zstbtn-real {
	margin-right: 4px;
	font-size: 12px;
}

.zstbtn .z-toolbarbutton-content {
	overflow: hidden;
	display: inline-block;
	width: 100%;
	height: 26px;
}

/* ZSS-1076 */
.zstbtn img {
    width: 16px;
}

/* ZSS-1082 */
.zstbtn-addRow {
    margin:0;
}
/* ZSS-1082 */
.zstbtn-addCol {
    margin:0;
}
/* ZSS-1082 */
.zstbtn-addRow.z-toolbarbutton {
    padding: 2px 0px;
}
/* ZSS-1082 */
.zstbtn-addCol.z-toolbarbutton {
    padding: 2px 0px;
}
/* ZSS-1082 */
.zstbtn-addRow .z-toolbarbutton-content {
    padding: 2px 0px;
}
/* ZSS-1082 */
.zstbtn-addCol .z-toolbarbutton-content {
    padding: 2px 0px;
}
    
<c:if test="${c:browser('ie6-')}">
.zstbtn .zstbtn-cave {
	margin-right: -12px;
}
.zstbtn .z-toolbarbutton-content {
	overflow: visible;
	display: inline-block;
	width: 100%;
}
.zschktbtn {
	font-size: 0;
}
.zschktbtn IMG {
	margin-right: -16px;
}
</c:if>	

.zstbtn-cave {
	position: absolute;
	top: 0;
	right: 0;
	height: 26px;
	padding: 0;
	width: 10px;
}

.zstbtn-clk-disd .zstbtn-real {
	cursor: default;
}

.zstbtn-clk-disd .zstbtn-cave {
	cursor: pointer;
}

.zstbtn-seld .z-toolbarbutton-content {
	background: #FCDE9A;
	padding: 1px;
	border: 1px solid #B7C6C9;
}

.zstbtn-seld .zstbtn-cave {
	padding: 0;
}

.z-toolbarbutton-over .z-toolbarbutton-content {
	padding: 2px;
	border: 0;
}

.z-toolbarbutton-over .zstbtn-cave {
	padding: 2px 1px 2px 0;
}

.zstbtn-cave-over {
	border-left:1px solid #CFCFCF;
}

/* cell comment */
.zscellcomment {
	position: absolute;
	width: 0;
	height: 0;
	border-style: solid;
	border-width: 3px;
	right: 0;
	top: 0;
	border-color: red red transparent transparent;
}
.zscellcomment-hidden {
	display: none;
}
.zscellcomment-popup .z-popup-content {
	word-wrap: break-word;
	width: 160px;
	line-height: normal;
	padding: 2px;
}

<c:if test="${c:browser('ie6-')}">
.zstbtn .z-toolbarbutton-content,
.zstbtn .zstbtn-cave  {
	font-size: 0;
}
</c:if>

.zstbtn-arrow {
	width: 9px;
	margin-top: 6px;
	height: 16px;
	background: url(${c:encodeURL('~./zss/img/arrow-down.gif')}) no-repeat;
	<c:if test="${c:browser('ie6-')}">
		font-size: 0;
	</c:if>
}

.zstbtn-sep {
	border: 0;
	padding: 1px 0;
	cursor: default;
	width: 2px;
}

.zstbtn-sep .z-toolbarbutton-content {
	display: inline-block;
	border: 0;
	padding: 0 1px;
}

/* ZSS-1072 */
.zsfontfamily-calibri .z-comboitem-text {
    font-family:Calibri;
    font-size: 18px;
}

.zsfontfamily-arial .z-comboitem-text {
	font-family:Arial;
	font-size: 18px;
}

.zsfontfamily-arial-black .z-comboitem-text {
	font-family:Arial Black;
	font-size: 18px;
}

.zsfontfamily-comic-sans-ms .z-comboitem-text {
	font-family:Comic Sans MS;
	font-size: 18px;
}

.zsfontfamily-courier-new .z-comboitem-text {
	font-family:courier new;
	font-size: 18px;
}

.zsfontfamily-georgia .z-comboitem-text {
	font-family:Georgia;
	font-size: 18px;
}

.zsfontfamily-impact .z-comboitem-text {
	font-family:Impact;
	font-size: 18px;
}

.zsfontfamily-lucida-console .z-comboitem-text {
	font-family:Lucida Console;
	font-size: 18px;
}

.zsfontfamily-lucida-sans-unicode .z-comboitem-text {
	font-family:Lucida Sans Unicode;
	font-size: 18px;
}

.zsfontfamily-palatino-linotype .z-comboitem-text {
	font-family:Palatino Linotype;
	font-size: 18px;
}

.zsfontfamily-tahoma .z-comboitem-text {
	font-family:Tahoma;
	font-size: 18px;
}

.zsfontfamily-times-new-roman .z-comboitem-text {
	font-family:Times New Roman;
	font-size: 18px;
}

.zsfontfamily-trebuchet-ms .z-comboitem-text {
	font-family:Trebuchet MS;
	font-size: 18px;
}

.zsfontfamily-verdana .z-comboitem-text {
	font-family:Verdana;
	font-size: 18px;
}

.zsfontfamily-ms-sans-serif .z-comboitem-text {
	font-family:MS Sans Serif;
	font-size: 18px;
}

.zsfontfamily-ms-serif .z-comboitem-text {
	font-family:MS Serif;
	font-size: 18px;
}

.zsfontsize .z-comboitem-text {
	font-size:13px;
}

.zsfontfamily,
.zsfontsize {
	vertical-align: middle;
	margin: 0 2px;
}

.zsfontfamily .z-combobox-input,
.zsfontsize .z-combobox-input {
	font-size: 12px;
}

.zstbtn-color {
	width: 14px;
	height: 3px;
	left: 3px;
	bottom: 2px;
	position: absolute;
	<c:if test="${c:browser('ie6-')}">
		font-size: 0;
	</c:if>
}

.zschktbtn .z-toolbarbutton-content {
	display: inline-block;
	width: 36px;
}
.zschktbtn img {
	position: absolute;
	right: 3px;
	top: 2px;
}
.zschktbtn-check {
	width: 16px;
	height: 16px;
}

.zscolormenu .z-menu-cnt .z-menu-img{
	margin: 0 16px 0 -4px;
	width: 14px;
}

.zsstylepanel-toolbar {
	background: #F5F5F5;	
}

.zsstylepanel  {
	background-color: transparent;
	-ms-filter: "progid:DXImageTransform.Microsoft.gradient(startColorstr=#00000000,endColorstr=#00000000)"; /* IE8 */
	background-image: none;
	box-shadow: none;
	-moz-box-shadow: none;
	-webkit-box-shadow: none;
	border: none;
	pointer-events: none;
}

.zsstylepanel-upper {
	border: 1px solid #CFCFCF;
}

.zsstylepanel-upper,
.zsstylepanel-menu {
	pointer-events: auto;
}

.zsstylepanel .z-popup-content {
	padding: 0;
}

.zsstylepanel-toolbar {
	border: 0;
}

.zsstylepanel-menu {
	margin-top: 15px;
	margin-right: 60px;
	position: relative;
	left: 0px;
	top: 0px;
}

<%-- Colorbutton --%>
.zstbtn-pp, .zscolormenu-cnt-pp {
	border: 1px solid #86A4BE;
	background: #FFF;
	position: absolute;
	width: 180px;
	height: 120px;
}
.zstbtn-pp .zstbtn-cell, .zscolormenu-cnt-pp .zscolormenu-cell {
	display: inline;
	float: left;
	height: 18px;
	overflow: hidden;
	padding: 2px;
	width: 18px;
}

.zstbtn-pp .zstbtn-cell-over, .zscolormenu-cnt-pp .zscolormenu-cell-over {
	padding: 1px;
	border: 1px solid #666666;
}

.zstbtn-pp .zstbtn-cell-cnt, .zscolormenu-cnt-pp .zscolormenu-cell-cnt {
	border: 1px solid #808080;
	height: 10px;
	overflow: hidden;
	width: 11px;
	margin: 2px;
	cursor: pointer;
}

.zstbtn:hover,
[class^="zsformulabar-"].z-toolbarbutton:hover,
.zschktbtn:hover,
.zsfontfamily .z-combobox-button:hover,
.zsfontsize .z-combobox-button:hover {
	border-color: #CFCFCF;
	background: none;
	-ms-filter: "progid:DXImageTransform.Microsoft.gradient(enabled = false))";
	filter: "progid:DXImageTransform.Microsoft.gradient(enabled = false))";
	box-shadow: none;
	-moz-box-shadow: none;
	-webkit-box-shadow: none;
}

.zstbtn:active {
	border-color: #CFCFCF;
	background: #CFCFCF;
}

.zstbtn-seld .z-toolbarbutton-content {
	background: #E1E1E1;
	border-color: #CFCFCF;
}

.zsfontfamily .z-comboitem:hover,
.zsfontsize .z-comboitem:hover,
.zsfontfamily .z-comboitem-selected,
.zsfontsize .z-comboitem-selected {
	background: #E1E1E1;
}

.zsfontfamily .z-combobox-button:active,
.zsfontsize .z-combobox-button:active {
	border-color: #CFCFCF;
	background: #E1E1E1;
	box-shadow: none;
	-moz-box-shadow: none;
	-webkit-box-shadow: none;
}

.zsfontfamily .z-comboitem-selected:hover,
.zsfontsize .z-comboitem-selected:hover {
	background: #CFCFCF;
}

[class^="zsmenu-"].z-menu-hover > .z-menu-content,
[class^="zsmenu-"] > .z-menu-content:hover,
[class^="zsmenu-"] > .z-menu-content:focus,
.zscolormenu.z-menu-hover > .z-menu-content,
.zscolormenu.z-menu > .z-menu-content:hover,
.zscolormenu.z-menu > .z-menu-content:focus,
[class^="zsmenuitem-"].z-menuitem-hover > .z-menuitem-content,
[class^="zsmenuitem-"].z-menuitem > .z-menuitem-content:hover,
[class^="zsmenuitem-"].z-menuitem > .z-menuitem-content:focus {
	background: #E1E1E1;
	border-color: #E1E1E1;
}

.zstbtabpanel {
	background: #EEEEEE;
	padding: 0;
}

.zssheettab.z-tab-selected .zssheettab-text{
	color: #333;
}

.zssheettab:hover {
	background: #E1E1E1;
	-ms-filter: "progid:DXImageTransform.Microsoft.gradient(enabled = false))";
	filter: "progid:DXImageTransform.Microsoft.gradient(enabled = false))";
}
.zssheettab.z-tab-selected:hover {
	background: #FFFFFF;
}

/* zss tab selection box begin*/
.zstab-option {
	display: block;
	padding: 3px 10px 2px 10px;
	background-color: white;
	border-width: 0px;
	font-size: 13px;
	line-height: 14px;
	width: 100%;
	text-align: left;
}

.zstab-option:hover, .zstab-option-select {
	background-color: #DDDDDD;
}

.zstab-popup {
	max-height: 200px;
	overflow-y: scroll;
}

.zstab-popup .z-popup-content {
	padding: 0px;
}
/* zss tab selection box end*/

.z-initing {
	background-image: url('//www.zkoss.org/zk/img/${s:encodeWithZSS("zsspowered.png")}');
}

/* hide toolbar tab */
.z-north .zstbtabpanel {
	border-width: 0;
}

.zssheet .z-north .z-tabs,
.zssheet .z-north .z-toolbar-tabs {
	display: none;
}

/* format dialog item text should show in red */
.redItem .z-listcell-content {
    color: red;
}
.redItem:hover>.z-listcell>.z-listcell-content {
    color: red;
}
.redItem.z-listitem-selected>.z-listcell>.z-listcell-content {
    color: red;
}

/* change default font family to Segoe UI */
.zstbtn-real,
.zsfontfamily .z-combobox-input,
.zsfontsize .z-combobox-input,
[class^="zsmenu-"] .z-menu-text,
[class^="zsmenuitem-"] .z-menuitem-text,
.zssheettab-text,
.zstopcell,
.zsleftcell {
	font-family: "Segoe UI", Tahoma, Thonburi, Arial, Verdana, sans-serif;
}
.cleditorMain {border:1px solid #999; padding:0 1px 1px; background-color:white}
.cleditorMain,
.cleditorPopup,
.cleditorMain * {-moz-box-sizing:content-box; -webkit-box-sizing:content-box; box-sizing:content-box}
.cleditorMain iframe {border:none; margin:0; padding:0}
.cleditorMain textarea {overflow-y:scroll; font:10pt Arial,Verdana; resize:none; outline:none}
.cleditorToolbar {background: url(${c:encodeURL('~./zss/img/toolbar.gif')}) repeat}
.cleditorGroup {float:left; height:26px}
.cleditorButton {float:left; width:24px; height:24px; margin:1px 0 1px 0; background: url(${c:encodeURL('~./zss/img/buttons.gif')})}
.cleditorDisabled {opacity:0.3; filter:alpha(opacity=30)}
.cleditorDivider {float:left; width:1px; height:23px; margin:1px 0 1px 0; background:#CCC}
.cleditorPopup {border:solid 1px #999; background-color:white; color:#333333; position:absolute; font:10pt Arial,Verdana; cursor:default; z-index:10000}
.cleditorList div {padding:2px 4px 2px 4px}
.cleditorList p,
.cleditorList h1,
.cleditorList h2,
.cleditorList h3,
.cleditorList h4,
.cleditorList h5,
.cleditorList h6,
.cleditorList font {padding:0; margin:0; background-color:Transparent}
.cleditorColor {width:150px; padding:1px 0 0 1px}
.cleditorColor div {float:left; width:14px; height:14px; margin:0 1px 1px 0}
.cleditorPrompt {background-color:#F6F7F9; padding:8px; font-size:8.5pt}
.cleditorPrompt label {padding-bottom:8px}
.cleditorMsg {background-color:#FDFCEE; width:150px; padding:4px; font-size:8.5pt}