/* Hightlight.js

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Mon Apr 23, 2007 17:29:18 AM , Created by sam
}}IS_NOTE

Copyright (C) 2007 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/

(function () {

	this.animations = [];
	this.timeout = 600;
	this.disabled = false;
	this.running = false;
	this.animate = _animate;

	function _animate () {
		 if (this.disabled) return;
			
		 this.running = false;
			var animations = this.animations,
				size = animations.length;
			for (var i = 0; i < size; i++) {
				animations[i].animate();
			}
			if (size > 0) {
				this.running = true;
				setTimeout(_animate, 600);
			}
	}
	 
	function _addAnimate (ctrl) {
		var animations = this.animations,
			size = animations.length;
		for (var i = 0; i < size; i++) {
			if(animations[i] == ctrl) return;
		}
		animations.push(ctrl);
		if (!this.running) {
			this.running = true;
			setTimeout(_animate, 600);
		}
	}
	 
	function _removeAnimate (ctrl) {
		var animations = this.animations,
			size = animations.length;
		for (var i = 0; i < size; i++) {
			if (animations[i] == ctrl){
				var tail = animations.slice(i+1);
				animations.length = i;
				animations.push.apply(animations, tail);
			}
		}
	}
	 
	function _objAnimate (obj) {
		if(!obj.invalid && obj.animated){
			if (!obj.flash){
				jq(obj.comp).addClass("zshighlight2");
				obj.flash = true;
			} else {
				jq(obj.comp).removeClass("zshighlight2");
				obj.flash = false;
			}
		}
	}
	 
	 function _objDoAnimation (obj, start) {
		if (start && !obj.animated){
			_addAnimate(obj);
			obj.animated = true;
		} else if(!start && obj.animated) {
			jq(obj.comp).removeClass("zshighlight2");
			_removeAnimate(obj);
			obj.animated = false;
		}
	 }
/**
 *  Highlight animate highlight area
 */
zss.Highlight = zk.$extends(zss.AreaCtrl, {
	animate: function () {
		_objAnimate(this);
	},
	doAnimation : function (start){
		_objDoAnimation(this, start);
	}
});


/**
 *  HighlightTop animate highlight area at top panel
 */
zss.HighlightTop = zk.$extends(zss.AreaCtrlTop, {
	animate: function () {
		_objAnimate(this);
	},
	doAnimation : function (start) {
		_objDoAnimation(this, start);
	}
});

/**
 *  HighlightLeft animate highlight area at left panel
 */
zss.HighlightLeft = zk.$extends(zss.AreaCtrlLeft, {
	animate: function () {
		_objAnimate(this);
	},
	doAnimation: function (start) {
		_objDoAnimation(this, start);
	}
});

/**
 *  HighlightCorner animate highlight area at corner panel
 */
zss.HighlightCorner = zk.$extends(zss.AreaCtrlCorner, {
	animate: function () {
		_objAnimate(this);
	},
	doAnimation: function (start) {
		_objDoAnimation(this, start);
	}
});
})();