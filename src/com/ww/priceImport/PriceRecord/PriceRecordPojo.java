package com.ww.priceImport.PriceRecord;
/*Part Number|ListPriceNew-USADollars|ListPricePCTChange-USADollars|ListPrice-USADollars|
 * NewListPriceEffectiveDate|SystemOfRecord|
 */
public class PriceRecordPojo {
	private String partNumber="";
	private String listPriceNew_USADollars="";
	private String listPricePCTChange_USADollars="";
	private String listPrice_USADollars="";
	private String newListPriceEffectiveDate="";
	private String systemOfRecord="";
	private String fileName="";
	private int status;
	public PriceRecordPojo(){
		
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public String getPartNumber() {
		return partNumber;
	}
	public void setPartNumber(String partNumber) {
		this.partNumber = partNumber;
	}
	public String getListPriceNew_USADollars() {
		return listPriceNew_USADollars;
	}
	public void setListPriceNew_USADollars(String listPriceNew_USADollars) {
		this.listPriceNew_USADollars = listPriceNew_USADollars;
	}
	public String getListPricePCTChange_USADollars() {
		return listPricePCTChange_USADollars;
	}
	public void setListPricePCTChange_USADollars(
			String listPricePCTChange_USADollars) {
		this.listPricePCTChange_USADollars = listPricePCTChange_USADollars;
	}
	public String getListPrice_USADollars() {
		return listPrice_USADollars;
	}
	public void setListPrice_USADollars(String listPrice_USADollars) {
		this.listPrice_USADollars = listPrice_USADollars;
	}
	public String getNewListPriceEffectiveDate() {
		return newListPriceEffectiveDate;
	}
	public void setNewListPriceEffectiveDate(String newListPriceEffectiveDate) {
		this.newListPriceEffectiveDate = newListPriceEffectiveDate;
	}
	public String getSystemOfRecord() {
		return systemOfRecord;
	}
	public void setSystemOfRecord(String systemOfRecord) {
		this.systemOfRecord = systemOfRecord;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
}
