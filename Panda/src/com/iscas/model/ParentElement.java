package com.iscas.model;

import java.util.ArrayList;

public class ParentElement {

	private String parentId;
	private ArrayList<Integer> mids;

	public ParentElement(String parentId) {
		this.parentId = parentId;
		this.mids = new ArrayList<>();
	}

	public String getParentId() {
		return parentId;
	}

	public void addMid(int mid) {
		if (!mids.contains((Integer) mid)) {
			mids.add(mid);
		}
	}

	public void removeMid(int mid) {
		mids.remove((Integer) mid);
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public ArrayList<Integer> getMids() {
		return mids;
	}

	public void setMids(ArrayList<Integer> mids) {
		this.mids = mids;
	}

}
