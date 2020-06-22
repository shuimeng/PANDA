package com.iscas.model;

public class Mapper {

	private String orginalId;
	private String mapId;

	public Mapper(String orId, String mapId) {
		this.orginalId = orId;
		this.mapId = mapId;
	}

	public String getOrginalId() {
		return orginalId;
	}

	public void setOrginalId(String orginalId) {
		this.orginalId = orginalId;
	}

	public String getMapId() {
		return mapId;
	}

	public void setMapId(String mapId) {
		this.mapId = mapId;
	}

}
