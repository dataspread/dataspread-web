function (out) {
	var tbp = this._toolbarPanel;
	
	out.push('<div ', this.domAttrs_(), '>', tbp ? tbp.redrawHTML_() : '');
	out.push(this.cave.redraw(out));//sheet
	
	for (var w = this.firstChild; w; w = w.nextSibling){
		if(w.ghost){//allow to draw ghost to main body
			w.redraw(out);
		}
	}
	
    out.push('</div>');
}