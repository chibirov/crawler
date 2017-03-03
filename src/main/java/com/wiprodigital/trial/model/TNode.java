package com.wiprodigital.trial.model;

import java.util.HashSet;
import java.util.Set;

public class TNode {

	private String url;
	public String getUrl() { return url; }
	public void setUrl(String url) { this.url = url; }

	public Set<TNode> getChildren() { return children; }
	public void setChildren(Set<TNode> children) { this.children = children; }
	private Set<TNode> children;

	public TNode(String url) {
		this.url = url;
	}

	public void addChild(TNode node) {
		if (children == null)
			children = new HashSet<TNode>();
		children.add(node);
	}	
}