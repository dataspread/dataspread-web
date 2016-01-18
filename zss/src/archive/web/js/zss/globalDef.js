/*
 * global definition of zss
 */
(function () {
	zss.SEL = {
		CELL:"cell",
		ROW:"row",
		COL:"col",
		ALL:"all"
	};
	zss.SELDRAG = {
		MOVE: "move",
		RESIZE: "resize"
	};
	zss.SCROLL_DIR = { // ZSS-475: add direction enum. for scrolling to visible
		BOTH: "both",
		HORIZONTAL: "horizontal",
		VERTICAL: "vertical"
	};
	zss.SCROLL_POS = { // ZSS-664: add position enum. for scrolling to position
		NONE: 'none',
		TOP: 'top',
		BOTTOM: 'bottom'
	};
	zss.clientCopy = {
			maxRowCount: 400,
			maxColumnCount: 40
	};
	zss.colorPalette = {
		width:180,
		height:120,
		color:
			["#000000","#993300","#333300","#003300","#000080",
	      	"#003366","#660066","#333333","#800000","#FF8080",
	      	"#808000","#008000","#008080","#0000FF","#800080",
	      	"#969696","#FF0000","#FF6600","#FFFF66","#99CC00",
	      	"#33CCCC","#0066CC","#6666FD","#808080","#FF00FF",
	      	"#FF9900","#FFFF00","#00FF00","#00FFFF","#00CCFF",
	      	"#CC99FF","#C0C0C0","#FF99CC","#FFCB90","#FFFF99",
	      	"#CCFFCC","#CCFFFF","#99CCFF","#9999FF","#FFFFFF"]
	};
})();

