package org.zkoss.zss.app.ui;

import org.zkoss.zul.DefaultTreeNode;

public class BucketTreeNode<T> extends DefaultTreeNode<T> {
	private static final long serialVersionUID = -8085873079938209759L;
	
	// Node Control the default open
	private boolean open = false;

	public BucketTreeNode(T data, BucketTreeNodeCollection<T> children, boolean open) {
		super(data, children);
		this.setOpen(open);
	}

	public BucketTreeNode(T data, BucketTreeNodeCollection<T> children) {
		super(data, children);
	}

	public BucketTreeNode(T data) {
		super(data);
	}

	public boolean isOpen() {
		return open;
	}

	public void setOpen(boolean open) {
		this.open = open;
	}
}
