package com.ww.pim.common.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import com.ibm.pim.attribute.AttributeChanges;
import com.ibm.pim.attribute.AttributeInstance;
import com.ibm.pim.catalog.Catalog;
import com.ibm.pim.catalog.item.Item;
import com.ibm.pim.collaboration.CollaborationItem;
import com.ibm.pim.context.Context;
import com.ibm.pim.lookuptable.LookupTable;
import com.ibm.pim.lookuptable.LookupTableEntry;
import com.ibm.pim.utils.Logger;

public class UtilityMethods {
	
	public File[] listAllPriceFiles(String dirPath,Logger logger) {
		File[] files = null;
		try {
			logger.logInfo("Dirpath "+dirPath);
			WattsPriceFileNameFilter fileFilter = new WattsPriceFileNameFilter();
			File dir = new File(dirPath);
			files = dir.listFiles(fileFilter);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return files;
	}
	public File[] listAllQADFiles(String dirPath,Logger logger) {
		File[] files = null;
		try {
			logger.logInfo("Dirpath "+dirPath);
			WattsQADFileFilter fileFilter = new WattsQADFileFilter();
			File dir = new File(dirPath);
			files = dir.listFiles(fileFilter);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return files;
	}
	
	public void deleteAllFiles(String dirPath) {
		File[] files = null;
		try {
			File dir = new File(dirPath);
			files = dir.listFiles();
			for(File file : files){
				file.delete();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void readPriceFileContent(File file,String delimiter,java.sql.Statement stmt,String targetTempPath,  Logger logger){
		//List<PriceRecordPojo> priceList= new ArrayList<PriceRecordPojo>();
		String line=null;
		try{
			Path targetPath = Paths.get(targetTempPath+file.getName());
		if (null != file){
			//logger.logInfo("UtilityMethods:readPriceFileContent file obtained : [OK] ");
			BufferedReader br = new BufferedReader(new FileReader(file));
			br.readLine();
			while ((line = br.readLine()) !=null){
				//logger.logInfo("-----1-----");
				Scanner sc = new Scanner(line);
				//logger.logInfo("-----2-----");
				sc.useDelimiter(delimiter);
				//logger.logInfo("-----3-----"+delimiter);
				//PriceRecordPojo priceRecordPojoObj = new PriceRecordPojo();
				//logger.logInfo("-----4-----");
				String query = "INSERT INTO PIMDBUSER.LIST_PRICE_IMPORT (PART_NUMBER,LIST_PRICE_NEW_USA_DOLLARS," +
				"LIST_PRICE_PCT_CHANGE_USA_DOLLARS,LIST_PRICE_USA_DOLLARS,NEW_LIST_PRICE_EFF_DATE,SYSTEM_OF_RECORD,FILE_NAME) VALUES('" +
				sc.next()+"','"+
				sc.next()+"','"+
				sc.next()+"','"+
				sc.next()+"','"+
				sc.next()+"','"+
				sc.next()+"','"+
				file.getName() +"')";
				//logger.logInfo(query);
				stmt.addBatch(query);
			}
			Files.move(file.toPath(), targetPath , StandardCopyOption.REPLACE_EXISTING);
		}
		}catch(Exception e){
			logger.logInfo("Exception in UtilityMethods:readPriceFileContent method : "+e);
			e.printStackTrace();
		}
	}
	
	public FileOutputStream createDirectory(Logger logger){
		boolean dirCreated = false;
		FileOutputStream fos=null;
		try{
			logger.logInfo("UtilityMethods:createDirectory method called : [OK]");
			Date currentDate = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat(Constant.DATE_FORMAT_FOR_REPORT_FOLDER,Locale.getDefault());
			String convertedDate = sdf.format(currentDate);
			String dirName = Constant.MASS_ITEM_EXPORT_PATH + "MIE_"+convertedDate;
			logger.logInfo("UtilityMethods:createDirectory Folder Path "+dirName); 
			File file = new File(dirName);
			if (!file.exists()){
				logger.logInfo(convertedDate+" Directory does not exist creating ......");
				dirCreated = file.mkdir();
				logger.logInfo("Directory created : "+dirCreated);
				logger.logInfo("Directory Path : "+file.getAbsolutePath());
			}else{
				logger.logInfo("Directory already exists skipping creation ");
				dirCreated = true;
			}
			if (dirCreated){
				String convertedTime = new SimpleDateFormat(Constant.DATE_FORMAT_FOR_REPORT_FILES,Locale.getDefault()).format(new Date());
				String filePath = dirName+"/"+"Mass_Item_Export_"+convertedTime+".xlsx";
				logger.logInfo("Created file path "+filePath);
				File objFile = new File(filePath);
				if(!objFile.exists())
				{
					objFile.createNewFile();
				}				
				fos = new FileOutputStream(objFile);
				logger.logInfo("File created "+filePath);
			}
		}catch(Exception exp){
			logger.logInfo("Exception in UtilityMethods:createDirectory method : "+exp);
			exp.printStackTrace();
		}
		return fos;
	}
	
	public ArrayList<String> createMassUpdateHeader(Logger logger){
		ArrayList<String> headerRow = new ArrayList<String>();
		try{
			logger.logInfo("UtilityMethods:createMassUpdateHeader method Called: [OK]");
			
			logger.logInfo("UtilityMethods:createMassUpdateHeader method Executed: [OK]");
		}catch(Exception exp){
			logger.logInfo("Exception in UtilityMethods:createMassUpdateHeader method : "+exp);
			exp.printStackTrace();
		}
		return headerRow;
	}
	
	public  boolean specialChanracterCheck(String expression){
		boolean result = false;
		try{
			Pattern pattern = Pattern.compile(".*[@?!%*\"°™].*");
			Matcher matcher = pattern.matcher(expression);
			if(matcher.find()){
				result = true;
			}
		}catch(Exception exp1){
			exp1.printStackTrace();
		}
		return result;
	}
	
	public  boolean specialChanracterCheck(String expression,Logger logger){
		boolean result = false;
		try{
			Pattern pattern = Pattern.compile(".*[@?!%*\"°™].*");
			Matcher matcher = pattern.matcher(expression);
			if(!matcher.find()){
				result = true;
			}
		}catch(Exception exp1){
			logger.logInfo(exp1.getMessage());
		}
		return result;
	}
	
	public  String getLkpValueForKey(Context ctx,String lkpTblName, String lookupKey,Logger logger)
	{
		logger.logInfo("getLkpValueForKey method called ");
		String lookupValue = null;
		try
		{
			LookupTable lkpTble = ctx.getLookupTableManager().getLookupTable(lkpTblName);
			//String lkpSpecName = lkpTble.getSpec().getName();
			LookupTableEntry entry = lkpTble.getLookupTableEntry(lookupKey);
			if(null != entry)
			{
				lookupValue = (String)entry.getValues().toArray()[0];
				logger.logInfo(" lkpTblName : "+ lkpTblName + " LookUp Key : "+lookupKey + " look up value "+lookupValue);
			}


		}
		catch(Exception ex)
		{
			logger.logDebug("Within getLkpValueForKey method..exception caught.."+ex);
		}
		return lookupValue;
	}
	
	public  String[] getLkpValuesForKey(Context ctx,String lkpTblName, String lookupKey,Logger logger)
	{
		logger.logInfo("getLkpValueForKey method called ");
		String[] lookupValue = null;
		try
		{
			LookupTable lkpTble = ctx.getLookupTableManager().getLookupTable(lkpTblName);
			//String lkpSpecName = lkpTble.getSpec().getName();
			LookupTableEntry entry = lkpTble.getLookupTableEntry(lookupKey);
			if(null != entry)
			{
				lookupValue = (String[]) entry.getValues().toArray();
				logger.logInfo(" lkpTblName : "+ lkpTblName + " LookUp Key : "+lookupKey + " look up value "+lookupValue);
			}


		}
		catch(Exception ex)
		{
			logger.logDebug("Within getLkpValueForKey method..exception caught.."+ex);
		}
		return lookupValue;
	}
	
	public  boolean hasAttributeChanged(CollaborationItem colItem, String attribName,Logger logger)
	{
		logger.logDebug("Within hasAttributeChanged method..");
		boolean isAttributeChanged = false;
		String sModifiedPath ="";
		try
		{
			AttributeChanges attributeChanges = colItem.getAttributeChangesSinceLastSave();
			List<? extends AttributeInstance> modifiedAttributesWithnewDataList = attributeChanges.getModifiedAttributesWithNewData();

			if(modifiedAttributesWithnewDataList!=null && !modifiedAttributesWithnewDataList.isEmpty())
			{
				for(int i=0;i<modifiedAttributesWithnewDataList.size();i++)
				{ 
					sModifiedPath = modifiedAttributesWithnewDataList.get(i).getPath();
					if(sModifiedPath.contains(attribName))
					{
						isAttributeChanged = true;
						break;
					}
				}
			}
		}
		catch(Exception ex)
		{
			logger.logDebug("Within hasAttributeChanged method exception caught.."+ex);
			return false;
		}
		return isAttributeChanged;
	}
	
	public  boolean hasAttributeChangedCatalog(Item colItem, String attribName,Logger logger)
	{
		logger.logDebug("Within hasAttributeChanged method..");
		boolean isAttributeChanged = false;
		String sModifiedPath ="";
		try
		{
			AttributeChanges attributeChanges = colItem.getAttributeChangesSinceLastSave();
			List<? extends AttributeInstance> modifiedAttributesWithnewDataList = attributeChanges.getModifiedAttributesWithNewData();

			if(modifiedAttributesWithnewDataList!=null && !modifiedAttributesWithnewDataList.isEmpty())
			{
				for(int i=0;i<modifiedAttributesWithnewDataList.size();i++)
				{ 
					sModifiedPath = modifiedAttributesWithnewDataList.get(i).getPath();
					if(sModifiedPath.contains(attribName))
					{
						isAttributeChanged = true;
						break;
					}
				}
			}
		}
		catch(Exception ex)
		{
			logger.logDebug("Within hasAttributeChanged method exception caught.."+ex);
			return false;
		}
		return isAttributeChanged;
	}
	
	public boolean sendEmailToUser(File[] files,String environment,Logger logger)
	{

		logger.logInfo("***Send mail Called***");

		final String username = ""; //watts to provide
		final String password = ""; //watts to provide
		InternetAddress []recipientAddressList = null;
		Properties props = new Properties();
		boolean emailFlag = false;

		//props.put("mail.smtp.auth", "true");
		//props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", "smtp.watts.com");
		//props.put("mail.smtp.port", "25");
		//Session session = Session.getDefaultInstance(props);
		Session session = Session.getInstance(props);
		/*Session session = Session.getInstance(props,
				new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				System.out.println("***Inside Authentication***");
				return new PasswordAuthentication(username, password);
			}
		});*/

		try {
			
			recipientAddressList = getToAddressList("ProductDataServices@wattswater.com");
			//recipientAddressList = getToAddressList("Sinha.Kumar@wattswater.com");
			
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress("MDM_Notifications@wattswater.com"));
			message.setRecipients(Message.RecipientType.TO,recipientAddressList);
			message.setSubject("QAD to PIM Data Update Report. Environment : "+environment);
			
			//message.setContent(mailBody,"text/html;charset=utf-8");
			
			Multipart multipart = new MimeMultipart();
			for (File eachFile: files){
				logger.logInfo("File Name : "+eachFile.getName());
				MimeBodyPart attachMentPart = new MimeBodyPart();
			    DataSource source = new FileDataSource(eachFile.getPath());
			    attachMentPart.setDataHandler(new DataHandler(source));
			    attachMentPart.setFileName(eachFile.getName());
			    multipart.addBodyPart(attachMentPart);
			}
			
			
			String mailBody = "Hi,\n\n\nPlease find attached the error files generated at the last QAD to PIM File processing schedule. \n\n\nRegards,\nPIMTeam.";
		    MimeBodyPart htmlPart = new MimeBodyPart();
		    //htmlPart.setContent(mailBody, "text/html;charset=utf-8");
		    htmlPart.setContent(mailBody, "text/plain;charset=utf-8");
		    multipart.addBodyPart(htmlPart);
		    
		    message.setContent(multipart);
			
			Transport.send(message);
			emailFlag = true;
			logger.logInfo("Done and Sent");


		} catch (MessagingException me) {
			me.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();
		}
		return emailFlag;
	}
	
	private InternetAddress [] getToAddressList(String strReceivedAddressList)throws Exception
	{

		System.out.println("getToAddressList(..) called");
		String recipient = strReceivedAddressList;
		int count =0;

		InternetAddress []recipientAddressList = null;
		try{
			if(strReceivedAddressList.contains(";"))
			{
				String [] recipientList = recipient.split(";");	
				recipientAddressList = new InternetAddress[recipientList.length];
				for(String recp :recipientList)
				{
					recipientAddressList[count] = new InternetAddress(recp.trim());
					System.out.println("*********** recipientAddressList[count] value "+recipientAddressList[count]);
					count++;

				}
			}else
			{
				recipientAddressList = new InternetAddress[1];
				recipientAddressList[0] = new InternetAddress(recipient.trim());
				System.out.println("*********** recipientAddressList.[0] value "+recipientAddressList[0]);
			}

		}catch(Exception e)
		{
			e.printStackTrace();
		}
		System.out.println("end getToAddressList : "+recipientAddressList);
		return recipientAddressList;
	}
	
	public String[] getPossibleAssociationTypes(Context ctx,Logger logger){
		String[] associationTypes = null;
		try{
			logger.logInfo("getPossibleAssociationTypes method called11");
			int pimId =0;
			Item item=null;
			Catalog ctg= ctx.getCatalogManager().getCatalog("Product Catalog");
			while (item == null){
				pimId = pimId+1;
				item = ctg.getItemByPrimaryKey(pimId+"");
				if (null != item){
					AttributeInstance attrIns = item.getAttributeInstance("Product Catalog Global Spec/Associations");
					if(attrIns.getChildren().size()>0){
						AttributeInstance childAttrs = item.getAttributeInstance("Product Catalog Global Spec/Associations/Type");
						Set<String> possibleValues =childAttrs.getPossibleValues();
						for(String str:possibleValues){
							logger.logInfo(str);
						}
						associationTypes = possibleValues.toArray(new String[0]);
					}else{
						item = null;
					}
				}
			}
		}catch(Exception e){
			logger.logInfo("Exceptions :"+e.getMessage());
			e.printStackTrace();
		}
		return associationTypes;
	}
}
