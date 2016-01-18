/*
ss.js

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		January 10, 2008 4:58:31 PM , Created by Dennis Chen
}}IS_NOTE

Copyright (C) 2008 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/


/* common method */
zkS = {};

/**
 * a util method to check if(val || val==0), in many case of attribute, 
 * i want if scentance pass even val=0
 */
zkS.t = function (val) {
	return val || val === 0;
};

zkS._delayInit = false;/*should i init header and cell by dealy bath*/
zkS._delayQ = [];//delay and batch invoke queue
zkS._delayBatchSize = 50;
zkS._delaySched = false;
zkS.addDelayBatch = function(fn, front, arg0, arg1){
	if (front) {
		zkS._delayQ.unshift([fn, arg0, arg1]);
	} else {
		zkS._delayQ.push([fn, arg0, arg1]);
	}
	if (!zkS._delaySched) {
		zkS._delaySched = true;
		setTimeout(zkS._doDelayBatch, 250);
	}
};


zkS._doDelayBatch = function () {
	zkS._delaySched = false;	
	var size = zkS._delayQ.length;
	size = zkS._delayBatchSize > size ? size : zkS._delayBatchSize;
	var batch;

	for (var i = 0; i < size; i++) {
		batch = zkS._delayQ.shift();
		batch[0].call(this,batch[1], batch[2]);
	}
	batch = null;
	if (zkS._delayQ.length > 0 && !zkS._delaySched) {
		zkS._delaySched = true;
		setTimeout(zkS._doDelayBatch, 100);
	}
};


/* method that handle round-trip (a method needs be call after client -> server -> client )function*/
zkS._callbacks = {};
zkS._cbtoken = 0;
/**
 * 
 * @param {Object} fn
 * @param {Object} arg0
 * @param {Object} arg1
 * @return a round-trip token
 */
zkS.addCallback = function(fn, arg0, arg1){
	var token = (zkS._cbtoken++) + '';
	zkS._callbacks[token] = [fn, arg0, arg1];
	if(zkS._cbtoken > 9999) zkS._cbtoken = 0;
	return  token;
};
zkS.removeCallback = function (token) {
	var r = zkS._callbacks[token];
	if (r)
		delete zkS._callbacks[token];
};
zkS.doCallback = function (token) {
	var r = zkS._callbacks[token];
	if (r) {
		delete zkS._callbacks[token];
		r[0].call(this, r[1], r[2]);
	}
};

zkS.parentByZSType = function(el, type, pathlen) {
	if (el) {
		if (!(type instanceof Array))
			type = [type];
		var i = 0,
			size = type.length, 
			n = el;
		for(; !!n; n = n.parentNode) {
			if (!n.attributes) 
				continue;
			j = size;
			while (j--) {
				if (n.getAttribute('zs.t') == type[j])
					return n;
			}
			if (pathlen && i++ > pathlen)
				break;
		}
	}
	return null;
};

zkS.copyParm = function (src, dest, parms) {
	var i = parms.length;
	while(i--)
		dest[parms[i]] = src[parms[i]];	
};

/** Returns the data for onClick. */
zkS._getMouseData = function (evt, target) {
	var extra = "";
	if (evt.altKey) extra += "a";
	if (evt.ctrlKey) extra += "c";
	if (evt.shiftKey) extra += "s";
	
	var ofs = zk(target).cmOffset(target),
		x = evt.pageX - ofs[0],
		y = evt.pageY - ofs[1];

	return [x, y, extra];
};


zkS.isEvtKey = function (evt, flag) {
	if (evt && ((flag.indexOf("a") != -1 && evt.altKey) || 
		(flag.indexOf("c") != -1 && evt.ctrlKey) ||
		(flag.indexOf("s") !=- 1 && evt.shiftKey) )) return true;
	return false;
};


/**
 * check to see if cmp has scroll bar
 * @param {Object} cmp component to check
 * @param {Object} vert false to check horizontal, true for vertical
 * @return true or false.
 */
zkS._hasScrollBar = function (cmp, vert) {
	var off,
		client;
	if(vert){
		off = cmp.offsetWidth;
		client = cmp.clientWidth;
	}else{
		off = cmp.offsetHeight;
		client = cmp.clientHeight;  
	}
	return (off - client) > 3 ? true : false;
};

zkS._enterChar = String.fromCharCode(13);

/**
 * check if point(x, y) is overlap to element
 * @param element a dom element
 * @param x x of upper left corner
 * @param y y of upper left corner
 * @returns true or false
 */
zkS.isOverlapByPoint = function(element, x, y) {
	var zkElement = zk(element),
		offset = zkElement.revisedOffset(),
		x1 = offset[0],
		y1 = offset[1],
		x2 = x1 + zkElement.offsetWidth(),
		y2 = y1 + zkElement.offsetHeight();
	return x >= x1 && x <= x2 && y >= y1 && y <= y2;
}