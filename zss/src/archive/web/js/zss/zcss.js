/*
zcss.js

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

/**
 * Css Utility namespace
 */
zcss = {
	p2saReg : /(-[a-z])/gi
};

zcss.nonNegativeAttr = ["width", "height"];

/**
 * get StyleSheet object by id
 * @param {String} id
 * @return {Object} StyleSheet object
 */
zcss.getSSheet = function (id) {
	var node = document.getElementById(id);
	if(node && node.type == "text/css"){
		return zcss._getElementSheet(node);
	}
	return null;
};


/**
 * Find the rule in the StyleSheet by selector and id.
 * if id is specified, then searh the rule in the StyleSheet only, 
 * if not find the last rule.
 * @param {String} selector selector text
 * @param {String} id style sheet id, null-able
 * @return {Object} Style Sheet Rule
 */
zcss.findRule = function (selector, id) {
	var ss;
	if (id) {
		ss = zcss.getSSheet(id);
		if(!ss) return null;
		ss = [ss];
	} else {
		ss = document.styleSheets;	
	}
	
	for (var i = ss.length - 1; i >= 0; i--) {
		var rules = zcss._getSSRules(ss[i]);
		if (!rules) continue;
		for (var j = rules.length - 1; j >= 0; j--) {
			var r = rules[j];
			if(selector.toLowerCase() == r.selectorText.toLowerCase()) {
				return r;
			}
		}
	}
	return null;
};


zcss._findRuleIndex = function (sheetobj, selector) {
	
	//TODO , should i cache index, and when should clear??
	var rules = zcss._getSSRules(sheetobj);
	if(!rules) return -1;
	for(var j=rules.length-1;j>=0;j--){
		var r = rules[j];
		if(selector.toLowerCase() == r.selectorText.toLowerCase()){
			return j; 
		}
	}
	return -1;
};


/**
 * Add new rule to the StyleSheet
 * If id is specified, then add the rule to the StyleSheet,
 * If not, then add the rule to the last StyleSheet 
 * 
 * @param {String,Array} selector the selector text
 * @param {String} props the properties(general css attribute format , e.g. font-size:20px;border 1px solid;), might null
 * @param {String} id the style element id, null-able
 * @param {String} before insert before the selector text, this param work with style element id only 
 * @return the new rule/rules depends on selector type. if the StyleSheet of id doesn't exist then return null;
 */
zcss.addRule = function (selector, props, id, before) {
	var sheetobj;
	if (id) {
		sheetobj = zcss.getSSheet(id);
		if(!sheetobj) return null;
	} else {
		sheetobj = document.styleSheets;
		sheetobj = sheetobj[sheetobj.length - 1];//get last
	}
	//insert to last
	var rules = zcss._getSSRules(sheetobj);
	if(!rules) return null; 
	var index;
	
	if (sheetobj && before && typeof before == "string") {
		index = zcss._findRuleIndex(sheetobj , before);
		if (index < 0)
			index = rules.length;

	} else
		index  = rules.length;
	
	if (!(selector instanceof Array)) {
		var rule = zcss._insertRule2Sheet(sheetobj, selector, (props ? props : ""), index);
		return rule;
	} else {
		var rules = [],
			size = selector.length;
		for (var i = 0; i < size; i++) {
			rules[i] = zcss._insertRule2Sheet(sheetobj, selector[i], (props ? props : ""), index);
			index++;
		}
		return rules;
	}
	
	//in sfa, we must retain the rules to get correct collection
	//so I add all must do this, since it is more stable when in other borwsers
	//rules = zcss._getSSRules(sheetobj)
};


/**
 * Set the style to the rule by following condition
 * If id specified, update rule  only in the StyleSheet with this id.
 * If id doesn't specified, search the last rule in all StyleSheet.
 * 
 * If create is ture, the create new one at the last StyleSheet or specified StyleSheet if no rule is found.
 * If create is false, the just update the rule when rule is found. 
 * @param {String,Array} selector the selector text
 * @param {String,Array} prop the style name, it colud be a array
 * @param {String,Array} value the style value, ic could be a array with same size as prop
 * @param {boolean,String} create create new one if rule not found, if the type is string (a selector) 
 * then I will try to insert new rule berfore this selector, null-able
 * @param {String} id the stylesheet, null-able
 * @return true if success, otherwise false, if selector is array the retrun the last result of selector.
 */
zcss.setRule = function (selector, prop, value, create, id) {
	//zssd("set CSS RULE:"+selector+",prop:"+prop+"value:"+value,"1227");
	if (!(selector instanceof Array))
		selector = [selector];

	var result = false;
	for (var i = 0; i < selector.length; i++) {
		var rule = zcss.findRule(selector[i], id);
		if (!rule && create)
			rule = zcss.addRule(selector[i], "", id, create);

		if (rule) {
			if (prop instanceof Array) {
				for (var j = 0; j < prop.length; j++) {
					zcss._setRuleProperty(rule, prop[j], value[j]);
				}
			} else {
				zcss._setRuleProperty(rule, prop, value);
			}
			result = true;
		} else {
			result = false;
		}
	}
	return result;
};

/**
 * 
 * @param {String,Array} selector 
 * @param {String} id StyleSheet id, id must not null
 */
zcss.removeRule = function (selector, id) {
	if(!id) return;
	var sheetobj = zcss.getSSheet(id);
	if (!sheetobj) return;
	
	if (!(selector instanceof Array))
		selector = [selector];

	zcss._removeRulesFromSheet(sheetobj,selector);
};


//** private method
/**
 * Transfer css property value to style attribute value. i.e : font-size to fontSize
 */
zcss._porp2attr = function (prop) {
	return prop.$camel();//a prototype inplementation.
};

//** browser dependence method.
/**
 * get Rules for a stylesheet
 */
zcss._getSSRules = function (){
	if ((zk.ie && zk.ie < 11) || zk.safari) {
		return function(styleSheet) {
			return styleSheet.rules;
		};
	} else {
		return function(styleSheet){
			try {
				return styleSheet.cssRules;
			} catch (e) {//avoid cssRules Access to restricted URI denied" code: "1012
				return null;
			}
		};
	}
}();
/**
 * get stylesheet object from a element
 */
zcss._getElementSheet = function () {
	if (zk.ie && zk.ie < 11) {
		return function(element){
			return element.styleSheet;
		};
	} else {
		return function(element){
			return element.sheet;
		};
	}
}();

/**
 * insert rule to sheet, return the inserted rule
 */
zcss._insertRule2Sheet = function () {
	if (zk.ie && zk.ie < 11) {
		return function(sheetobj, selector, style, index) {
			sheetobj.addRule(selector, "{"+style+"}", index);
			return sheetobj.rules[index];
		};
	}
	if (zk.opera) {
		return function(sheetobj, selector, style, index) {
			//Opera bug, it cannot inert rule with index, it always insert it at the end;
			var len = sheetobj.cssRules.length;
			sheetobj.insertRule(selector+" {"+style+"}",index);
			return sheetobj.cssRules[index];
		};
	} else {
		return function(sheetobj, selector, style, index) {
			sheetobj.insertRule(selector + "{" + style + "}", index);
			return sheetobj.cssRules[index];
		};
	}
}();

/**
 * insert rule to sheet, return the inserted rule
 */
zcss._removeRulesFromSheet = function () {
	if (zk.ie && zk.ie < 11) {
		return function(sheetobj, selectors) {
			//sheetobj.removeRule(selector);
			var rules = zcss._getSSRules(sheetobj);
			if (!rules) return;
			for (var i = rules.length - 1; i >= 0; i--) {
				for (var j = selectors.length - 1; j >= 0; j--) {
					if (selectors[j] == rules[i].selectorText) {
						sheetobj.removeRule(i);
						break;
					}
				}
			}
		};
	} else {
		return function(sheetobj, selectors) {
			var rules = zcss._getSSRules(sheetobj);
			if (!rules) return;
			for (var i = rules.length - 1; i >= 0; i--) {
				for(var j = selectors.length - 1; j >= 0; j--) {
					if (selectors[j] == rules[i].selectorText){
						sheetobj.deleteRule(i);
						break;
					}
				}
			}
		};
	}
}();

/**
 * set rule property
 */
zcss._setRuleProperty = function (){
	if (zk.ie && zk.ie < 11) {
		return function (rule, prop, value) {
			prop = zcss._porp2attr(prop);
			rule.style[prop]=value;
		};
	} else {
		return function (rule, prop, value) {
			rule.style.setProperty(prop, value, "");
		};
	}
}();

zcss._setStyleProperty = function(){
	if (zk.ie && zk.ie < 11) {
		return function (style, prop, value, comp) {
			if (zcss.nonNegativeAttr.$contains(prop) && typeof value == "string" && value.indexOf("-") == 0) {
				value = "0" + zcss._trimPrefixNumber(value);
			}
			prop = zcss._porp2attr(prop);
			style[prop] = value;
		};
	} else {
		return function (style, prop, value, comp) {
			if (zcss.nonNegativeAttr.$contains(prop) &&  typeof value == "string" && value.indexOf("-") == 0) {
				value = "0" + zcss._trimPrefixNumber(value);
			}
			style.setProperty(prop, value, "");
		};
	}
}();

zcss._trimPrefixNumber = function (str) {
	var regexp = /[^-+0-9]/,
		index = str.search(regexp);
	if (index > 0) {
		return str.substr(index, str.length);
	}
	return str;
};

zcss.setStyle = function (cmp, prop, value) {
	if (prop instanceof Array) {
		for(var j = 0; j < prop.length; j++) {
			zcss._setStyleProperty(cmp.style, prop[j], value[j], cmp);
		}
	} else {
		zcss._setStyleProperty(cmp.style, prop, value, cmp);
	}
};

zcss.copyStyle = function (src, target, styles, clear) {
	var size = styles.length,
		st,
		nm;
	for (var i = 0; i < size; i++) {
		nm = zcss._porp2attr(styles[i]);
		st = src.style[nm];
		if (st) {
			target.style[nm] = st;
		} else if(clear) {
			target.style[nm] = "";
		}
	}	
};