package com.ww.priceImport.PriceRecord;

public class PackageingLevelPojo {
	private String uom="";
	private String gdsStatus = "";
	private String labelLineArt = "";
	private String packageGTIN ="";
	
	public String getPackageGTIN() {
		return packageGTIN;
	}
	public void setPackageGTIN(String packageGTIN) {
		this.packageGTIN = packageGTIN;
	}
	
	public String getUom() {
		return uom;
	}
	public void setUom(String uom) {
		this.uom = uom;
	}
	public String getGdsStatus() {
		return gdsStatus;
	}
	public void setGdsStatus(String gdsStatus) {
		this.gdsStatus = gdsStatus;
	}
	public String getLabelLineArt() {
		return labelLineArt;
	}
	public void setLabelLineArt(String labelLineArt) {
		this.labelLineArt = labelLineArt;
	}
	

}
