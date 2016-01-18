package org.zkoss.zss.ui;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.zkoss.zss.ui.impl.Focus;
/**
 * helper for spreadsheet friend focus
 * @author dennis
 *
 */
//code refer from BookCtrlImpl
/*package*/ class FriendFocusHelper implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private int _focusid;
	private transient WeakHashMap<Object, String> _focusMap = new WeakHashMap<Object, String>(20);
	private Map<String, String> _focusColors = new HashMap<String, String>(20); //id -> Focus
	
	private final static String[] FOCUS_COLORS = 
		new String[]{"#FFC000","#92D050","#00B050","#00B0F0","#0070C0","#002060","#7030A0", "#FFFF00",
					"#4F81BD","#F29436","#9BBB59","#8064A2","#4BACC6","#F79646","#C00000","#FF0000",
					"#0000FF","#008000","#9900CC","#800080","#800000","#FF6600","#CC0099","#00FFFF"};
	
	private int _colorIndex = 0;
	
	public FriendFocusHelper(){
	}
	
	public synchronized String nextFocusId() {
		return Integer.toString((++_focusid & 0x7FFFFFFF), 32);
	}

	private synchronized String nextFocusColor() {
		String color = FOCUS_COLORS[_colorIndex++ % FOCUS_COLORS.length];
		return color;
	}

	public synchronized void addFocus(Object focus) {
		if(focus instanceof Focus){
			String id = ((Focus)focus).getId();
			if(!_focusMap.containsKey(focus)){
				String color = _focusColors.get(id);
				if(color==null){
					_focusColors.put(id,color = nextFocusColor());
				}
				((Focus)focus).setColor(color);
			}
			_focusMap.put(focus, ((Focus)focus).getId());
		}else{
			_focusMap.put(focus, focus.toString());
		}
	}

	public synchronized void removeFocus(Object focus) {
		_focusMap.remove(focus);
	}

	public synchronized boolean containsFocus(Object focus) {
		syncFocus();
		return _focusMap.containsKey(focus);
	}
	
	public synchronized Set<Object> getAllFocus(){
		syncFocus();
		return new HashSet<Object>(_focusMap.keySet()); 
	}
	
	//if browser is closed directly
	private synchronized void syncFocus() { 
		for (final Iterator<Object> it = _focusMap.keySet().iterator(); it.hasNext(); ) {
			Object focus = it.next();
			if(focus instanceof Focus){
				if (((Focus)focus).isDetached()) {
					it.remove();
					_focusColors.remove(((Focus)focus).getId());
				}
			} 
		}
	}

	//ZSS-1094
	private synchronized void writeObject(java.io.ObjectOutputStream s)
	throws java.io.IOException {
		s.defaultWriteObject();
		
		s.writeObject(new HashMap<Object, String>(_focusMap));
	}
	
	//ZSS-1094
	private void readObject(java.io.ObjectInputStream s)
	throws java.io.IOException, ClassNotFoundException {
		s.defaultReadObject();
		
		Map<Object, String> focusMap = (Map<Object, String>) s.readObject();
		if (focusMap != null) {
			_focusMap = new WeakHashMap<Object, String>(focusMap);
		}
	}

}
