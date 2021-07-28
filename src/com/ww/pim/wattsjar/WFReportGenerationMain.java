package com.ww.pim.wattsjar;
/* Main Class : WFReportGenerationMain
 * Action     : Report will be generated based on reserved items in each step.
 * Author     : Vijayadurga V
 * Date       : 15/02/2018
 * Interface  : RichSearchReportFunction
 */
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.ibm.pim.attribute.AttributeCollection;
import com.ibm.pim.attribute.AttributeDefinition;
import com.ibm.pim.attribute.AttributeInstance;
import com.ibm.pim.attribute.AttributeDefinition.Type;
import com.ibm.pim.collaboration.CollaborationObject;
import com.ibm.pim.collaboration.CollaborationStep;
import com.ibm.pim.collaboration.ItemCollaborationArea;
import com.ibm.pim.spec.SecondarySpec;
import com.ibm.pim.spec.Spec;
import com.ibm.pim.utils.Logger;
import com.ibm.pim.workflow.WorkflowStep;
import com.ibm.pim.collection.*;
import com.ibm.pim.context.Context;
import com.ibm.pim.context.PIMContextFactory;
import com.ibm.pim.catalog.Catalog;
import com.ibm.pim.catalog.item.*;

import java.util.Iterator;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.ibm.pim.docstore.Document ;
import com.ibm.pim.extensionpoints.RichSearchReportFunction;
import com.ibm.pim.extensionpoints.RichSearchReportFunctionArguments;
import com.ibm.pim.hierarchy.Hierarchy;
import com.ibm.pim.hierarchy.category.Category;

public class WFReportGenerationMain implements RichSearchReportFunction{
	private Context ctx = null;
	private static Logger rpLogger1 = null;
	private ItemCollaborationArea itemCollabArea=null;	
	CollaborationStep collabStep=null;	
	String keyAttribValue =null;
	ArrayList<String> directPath;
	ArrayList<String> eachLevelPath;
	ArrayList<String> requiedAttributes;
	ArrayList<String> higherPackagingLevelPath;
	ArrayList<String> associationsPath;
	ArrayList<String> secondarySpecPath;
	ArrayList<String> higherUnitOfMessure;
	ArrayList<String> EAunitOfMessure;
	ArrayList<String> secondarySpecVal;
	ArrayList<String> associationType;
	Set collabStepAttribPath=new HashSet<String>();
	public static FileOutputStream outputStream = null;
	XSSFWorkbook book = new XSSFWorkbook();
	Document wattsWaterExcelDoc = null;
	XSSFSheet sheet=null;
	XSSFRow headerRow=null;
	XSSFRow valueRow=null;
	XSSFCell cell=null;
	Date date = null;
	Hierarchy hierarchyOfItem = null;
	public void richSearchReport(RichSearchReportFunctionArguments arg0) {
	try
	{
		this.directPath = new ArrayList<String>();
		this.higherUnitOfMessure = new ArrayList<String>();
		this.EAunitOfMessure = new ArrayList<String>();
		this.secondarySpecVal = new ArrayList<String>();
		this.associationType = new ArrayList<String>();
		this.secondarySpecPath = new ArrayList<String>();
		this.eachLevelPath = new ArrayList<String>();
		this.associationsPath = new ArrayList<String>();
		this.higherPackagingLevelPath = new ArrayList<String>();
		this.requiedAttributes = new ArrayList<String>();
		this.directPath.add("Product Catalog Global Spec/PIM_ID");
		String docPath = arg0.getDocName();
		String viewStepName=null;		
		this.ctx = PIMContextFactory.getCurrentContext();		
		this.rpLogger1= this.ctx.getLogger("WattsReportLog");	
		Catalog ctg=arg0.getCatalog();
		String ctgName=ctg.getName();	
		String viewName=arg0.getViewName();
		DateFormat dateFormat = null;
		dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		this.date = new Date();
		String[] splitValues=viewName.split("__");
		viewStepName = splitValues[0];
		//this.rpLogger1.logDebug(viewStepName);
		this.rpLogger1.logDebug(arg0.getDocName());
		this.itemCollabArea=(ItemCollaborationArea) this.ctx.getCollaborationAreaManager().getCollaborationArea(ctgName);
		this.collabStep=this.itemCollabArea.getStep(viewStepName);
		this.collabStepAttribPath=functiongetCollabStepAttrib(this.collabStep);
		getAttributePath(this.collabStepAttribPath);
		this.requiedAttributes=functiongetCollabStepRequiredAttrib(this.collabStep);
		this.rpLogger1.logDebug("this.requiedAttributes========================="+this.requiedAttributes);
		PIMCollection reportSetCollection=arg0.getReportSet();
		Iterator reportSetCollectionIterator=reportSetCollection.iterator();		
		ArrayList<HashMap<String, String>> mapListOfVal = new ArrayList<HashMap<String, String>>();
		String   headerValue =null;
		String UnitOfMessure=null;
		String EAUnitOfMessure=null;
		while(reportSetCollectionIterator.hasNext())
		{
			Item item =(Item) reportSetCollectionIterator.next();			
			String itemPrimaryKey=item.getPrimaryKey();	
			Collection<CollaborationObject> reservedCollabObjetList=this.collabStep.getReservedObjectsForCurrentUser();
			Iterator reservedCollabObjetListIterator=reservedCollabObjetList.iterator();	
			while(reservedCollabObjetListIterator.hasNext())
			{
				CollaborationObject reservedCollabObject=(CollaborationObject) reservedCollabObjetListIterator.next();
				String reservedItemPrimaryKeyFromCurrentUser=reservedCollabObject.toString();
				//this.rpLogger1.logDebug("reservedItemPrimaryKeyFromCurrentUser"+reservedItemPrimaryKeyFromCurrentUser);
				String [] reservedItemPKeyFromCUserArray=reservedItemPrimaryKeyFromCurrentUser.split(":");
				String reservedItemPKeyFromCUser=reservedItemPKeyFromCUserArray[1];
				if(reservedItemPKeyFromCUser.equals(itemPrimaryKey))
				{
					HashMap<String,String> itemAttribValue=new HashMap<String,String>();
					Spec primarySpec=item.getCatalog().getSpec();	
					String primarySpecName=primarySpec.getName();					
					for (String DirectAttribPath : this.directPath)
					{
						AttributeDefinition attribDef=primarySpec.getAttributeDefinition(DirectAttribPath);
						Type attribType= attribDef.getType();					
						String attribTypeName=attribType.toString();						
						this.keyAttribValue=fnGetAttributeValue(item,DirectAttribPath,attribTypeName);						
						String [] splitHeaderValues =   DirectAttribPath.split("/");
						if(splitHeaderValues.length > 1)
						{
							headerValue = splitHeaderValues[splitHeaderValues.length-1];							
						}						
						itemAttribValue.put(headerValue,this.keyAttribValue);						
					}					
					if(this.secondarySpecPath!=null)
					{						
						for (String secondSpecPath : this.secondarySpecPath)		
						{						
							String secondSpecName=null;
							String attribCollectionSecondSpecName=null;
							SecondarySpec attribCollectionSecondSpec=null;
							 SecondarySpec secondSpec=null;
							Hierarchy hierarchyOfItem = this.ctx.getHierarchyManager().getHierarchy(
							"Templates Hierarchy");
							Collection<Category> collabItemCatCollec = item.getCategories();
								Iterator collabItemCatCollecIterator = collabItemCatCollec.iterator();
									while (collabItemCatCollecIterator.hasNext()) {
						Category collabItemCategory = (Category) collabItemCatCollecIterator.next();
						String collabItemCategoryName = collabItemCategory
								.getName();						
						PIMCollection<Category> hierCategoryCollection = hierarchyOfItem
								.getCategories();
						Iterator hierCategoryCollectionIterator = hierCategoryCollection
								.iterator();
						while (hierCategoryCollectionIterator.hasNext()) {
							Category hierCategory = (Category) hierCategoryCollectionIterator
									.next();
							if (hierCategory.getName().equals(
									collabItemCategoryName)) {							
							Collection<SecondarySpec> secondSpecList=collabItemCategory.getItemSecondarySpecs();
							Iterator secondSpecListIterator = secondSpecList
							.iterator();
							while (secondSpecListIterator.hasNext()) {
							 secondSpec=(SecondarySpec)secondSpecListIterator.next();
							 secondSpecName=secondSpec.getName();							
							 String [] splitHeaderValues =   secondSpecPath.split("/");
							 attribCollectionSecondSpecName = splitHeaderValues[0];						
							if(attribCollectionSecondSpecName.equals(secondSpecName))
								{
									this.rpLogger1.logDebug("Inside secondarySpecPath======================"+secondSpec.getName());
									AttributeDefinition attribDef=secondSpec.getAttributeDefinition(secondSpecPath);
									Type attribType= attribDef.getType();					
							String attribTypeName=attribType.toString();						
							this.keyAttribValue=fnGetAttributeValue(item,secondSpecPath,attribTypeName);		
							itemAttribValue.put(secondSpecPath,this.keyAttribValue);
							this.secondarySpecVal.add(secondSpecPath);
						}
							}
							}
						}
									}
						}
						Set<String> secondarySpecPathSet = new LinkedHashSet<String>(this.secondarySpecVal);					
						this.secondarySpecVal.clear();
						if(secondarySpecPathSet.contains(null))
						{
							secondarySpecPathSet.remove(null);
						}
						this.rpLogger1.logDebug("secondarySpecPathSet"+secondarySpecPathSet);
						this.secondarySpecVal.addAll(secondarySpecPathSet);
						
					}
					for (String eachLevelAttribPath : this.eachLevelPath)
					{
						AttributeDefinition attribDef=primarySpec.getAttributeDefinition(eachLevelAttribPath);
						Type attribType= attribDef.getType();					
						String attribTypeName=attribType.toString();						
						this.keyAttribValue=fnGetAttributeValue(item,eachLevelAttribPath,attribTypeName);						
						if(eachLevelAttribPath.contains("Unit of Measure"))
						{
							EAUnitOfMessure=fnGetAttributeValue(item,eachLevelAttribPath,attribTypeName);													
						}
						if(EAUnitOfMessure!=null){
						String [] splitHeaderValues =   eachLevelAttribPath.split("/");
						if(splitHeaderValues.length > 1)
						{
							headerValue = EAUnitOfMessure+":"+splitHeaderValues[splitHeaderValues.length-1];							
						}					
							itemAttribValue.put(headerValue,this.keyAttribValue);
							this.EAunitOfMessure.add(EAUnitOfMessure);
						}
						
					}
					Set<String> EAunitOfMessureSet = new LinkedHashSet<String>(this.EAunitOfMessure);					
					this.EAunitOfMessure.clear();
					if(EAunitOfMessureSet.contains(null))
					{
						EAunitOfMessureSet.remove(null);
					}					
					this.EAunitOfMessure.addAll(EAunitOfMessureSet);					
					AttributeInstance pathAttribInstance= item.getAttributeInstance(primarySpecName+"/Packaging Hierarchy#0"+"/Paths");
					int pathChildrenSize = pathAttribInstance.getChildren().size();
					if(pathChildrenSize > 0)
					{
						for(int i=0; i<pathChildrenSize;i++)
						{
						 AttributeInstance pathHierarchyLevelAttribInstance= item.getAttributeInstance(primarySpecName+"/Packaging Hierarchy#0"+"/Paths#"+i+"/Higher Packaging Levels");
						 int pathHierarchyLevelSize = pathHierarchyLevelAttribInstance.getChildren().size();
						 for(int k=0; k<pathHierarchyLevelSize;k++)
						 {
							 for (String itemChildNodeAttribName : this.higherPackagingLevelPath)
							 {	
								String childNodePath=primarySpecName+"/Packaging Hierarchy#0"+"/Paths#"+i+"/Higher Packaging Levels#"+k+"/"+itemChildNodeAttribName;						    
								AttributeDefinition attribDef1=primarySpec.getAttributeDefinition(primarySpecName+"/Packaging Hierarchy/Paths/Higher Packaging Levels/"+itemChildNodeAttribName);
								if(attribDef1!=null)
								{
									Type attribType1= attribDef1.getType();
									String attribTypeName1=attribType1.toString();									  
									this.keyAttribValue=fnGetAttributeValue(item,childNodePath,attribTypeName1);
									if(itemChildNodeAttribName.equals("Unit of Measure"))
									{
										UnitOfMessure=fnGetAttributeValue(item,childNodePath,attribTypeName1);										
										
									}
									String childNodeSinglePath=UnitOfMessure+":"+itemChildNodeAttribName;
									if(UnitOfMessure!=null){								
									itemAttribValue.put(childNodeSinglePath,this.keyAttribValue);								
									}
								 }
								 this.higherUnitOfMessure.add(UnitOfMessure);
							  }						 
							}
						 	Set<String> unitOfMessureSet = new LinkedHashSet<String>(this.higherUnitOfMessure);
							this.higherUnitOfMessure.clear();	
							if(unitOfMessureSet.contains(null))
							{
								unitOfMessureSet.remove(null);
							}							
							this.higherUnitOfMessure.addAll(unitOfMessureSet);
						}			  			   
					}
					AttributeInstance associationAttribInstance= item.getAttributeInstance(primarySpecName+"/Associations");
					int associationChildrenSize = associationAttribInstance.getChildren().size();			
					if(associationChildrenSize > 0)
					{
						String type=null;
						for(int i=0; i<associationChildrenSize;i++)
						{
							for(String associationAttrib: this.associationsPath)
							{
								if(associationAttrib.equals("Type"))
								{
									String typeAttribPath=primarySpecName+"/Associations#"+i+"/Type";
									AttributeDefinition attribDef2=primarySpec.getAttributeDefinition(primarySpecName+"/Associations/Type");
									String associationTypePath="Type";
									if(attribDef2!=null)
									{
										Type attribType2= attribDef2.getType();
										String attribTypeName2=attribType2.toString();
										this.keyAttribValue=fnGetAttributeValue(item,typeAttribPath,attribTypeName2);
										itemAttribValue.put(associationTypePath,this.keyAttribValue);
									}
									 type=this.keyAttribValue;
									 this.rpLogger1.logDebug("type"+type);
								}
								else
								{
									
									AttributeInstance associatedPartsAttribInstance= item.getAttributeInstance(primarySpecName+"/Associations#"+i+"/AssociatedParts");
									int associatedPartsSize = associatedPartsAttribInstance.getChildren().size();
									for(int k=0; k<associatedPartsSize;k++)
									{
										String associatedProductAttribPath=primarySpecName+"/Associations#"+i+"/AssociatedParts#"+k;
										AttributeDefinition attribDef3=primarySpec.getAttributeDefinition(primarySpecName+"/Associations/AssociatedParts");
										String associationProductPath=type+":"+"AssociatedParts";
										if(attribDef3!=null)
										{
											Type attribType3= attribDef3.getType();
											String attribTypeName3=attribType3.toString();
											this.keyAttribValue=fnGetAttributeValue(item,associatedProductAttribPath,attribTypeName3);
											if(type!=null){
											itemAttribValue.put(associationProductPath,this.keyAttribValue);
											}
										}					 
									}
								}
								this.associationType.add(type);
							}
						}
						Set<String> associationTypeSet = new LinkedHashSet<String>(this.associationType);
						this.associationType.clear();	
						if(associationTypeSet.contains(null))
						{
							associationTypeSet.remove(null);
						}						
						this.associationType.addAll(associationTypeSet);
					}
					mapListOfVal.add(itemAttribValue);
				}
				
			}
			
			
		}		
		this.rpLogger1.logDebug(mapListOfVal);
		excelCreation(mapListOfVal,docPath);
		
	}	
	catch(Exception e)
	{
		this.rpLogger1.logDebug("Exception in richSearchReport"+e);
}  
	}
	public static ArrayList<String> functiongetCollabStepRequiredAttrib(CollaborationStep collabStep)
	{
		ArrayList<String> requiredAttribArrayList=new ArrayList<String>();
		try
		{			
			WorkflowStep currentCollabStepWFStep=collabStep.getWorkflowStep();
			Collection<AttributeCollection> currentCollabStepRequiredAttribCollection=(Collection<AttributeCollection>) currentCollabStepWFStep.getRequiredAttributeCollections();
			Iterator currentCollabStepRequiredAttribCollectionIterator=currentCollabStepRequiredAttribCollection.iterator();
			while(currentCollabStepRequiredAttribCollectionIterator.hasNext())
			{
				AttributeCollection  currentCollabStepRequiredAttrib=(AttributeCollection) currentCollabStepRequiredAttribCollectionIterator.next();
				rpLogger1.logDebug("Attribute Collection Name ======="+currentCollabStepRequiredAttrib.getName());
				Collection<AttributeDefinition> collabStepRequiredAttribDefinition=(Collection<AttributeDefinition>) currentCollabStepRequiredAttrib.getAttributes();
				Iterator collabStepRequiredAttribDefinitionIterator=collabStepRequiredAttribDefinition.iterator();
				while(collabStepRequiredAttribDefinitionIterator.hasNext())
				{
					AttributeDefinition collabStepRequiredAttribDef=(AttributeDefinition) collabStepRequiredAttribDefinitionIterator.next();
					String collabStepRequiredAttribDefName=collabStepRequiredAttribDef.getName();					
					Type attribtype= collabStepRequiredAttribDef.getType();					
					String attribTypeName=attribtype.name();
					if(!attribTypeName.equals("GROUPING"))
					{
						requiredAttribArrayList.add(collabStepRequiredAttribDefName);						
					}
				}
			}
		}
			catch(Exception e)
			{
				rpLogger1.logDebug("Exception in functiongetCollabStepRequiredAttrib"+e);
			}
			
			return requiredAttribArrayList;
	}

	public static Set functiongetCollabStepAttrib(CollaborationStep collabStep)
	{
		Set<String>collabStepItemAttrib=new HashSet<String>();	
		try
		{			
			WorkflowStep currentCollabStepWFStep=collabStep.getWorkflowStep();		
			Collection<AttributeCollection> currentCollabStepAttribCollection=(Collection<AttributeCollection>) currentCollabStepWFStep.getAttributeCollections();
			Iterator currentCollabStepAttribCollectionIterator=currentCollabStepAttribCollection.iterator();
			while(currentCollabStepAttribCollectionIterator.hasNext())
			{
				AttributeCollection collabStepAttribColl=(AttributeCollection) currentCollabStepAttribCollectionIterator.next();
				Collection<AttributeDefinition> collabStepAttribDefinition=(Collection<AttributeDefinition>) collabStepAttribColl.getAttributes();
				Iterator collabStepAttribDefinitionIterator=collabStepAttribDefinition.iterator();
				while(collabStepAttribDefinitionIterator.hasNext())
				{
					AttributeDefinition collabStepAttribDef=(AttributeDefinition) collabStepAttribDefinitionIterator.next();
					String currentItemSpec=collabStepAttribDef.getPath();
					Type attribtype= collabStepAttribDef.getType();
				
					String attribTypeName=attribtype.name();
					if(!attribTypeName.equals("GROUPING"))
					{
						collabStepItemAttrib.add(currentItemSpec);							
					}
					
				}
			}			
		}
		catch(Exception e)
		{
			rpLogger1.logDebug("Exception in functiongetCollabStepAttrib"+e);
		}
		//rpLogger1.logDebug("collabStepItemAttrib"+collabStepItemAttrib);
		return collabStepItemAttrib;
	}
	public void getAttributePath(Set<String> collabStepAttribPath)
	{
		try
		{
			boolean flag=false;			
			Iterator collabStepAttribArrayIterator=collabStepAttribPath.iterator();
			while(collabStepAttribArrayIterator.hasNext()){
				String collabStepAttribFulPath=(String) collabStepAttribArrayIterator.next();
				//this.rpLogger1.logDebug("collabStepAttribFulPath"+collabStepAttribFulPath);
				if(collabStepAttribFulPath.startsWith("Product Catalog Global Spec/Packaging Hierarchy/Paths/Higher Packaging Levels"))
				{
					flag=true;
					String [] collabStepAttribPathArray=collabStepAttribFulPath.split("/");
					if(collabStepAttribPathArray.length>1)
					{
						String collabStepAttribName=collabStepAttribPathArray[collabStepAttribPathArray.length-1];				
						this.higherPackagingLevelPath.add(collabStepAttribName);						
					}
										
				}
				else if((collabStepAttribFulPath.startsWith("Product Catalog Global Spec/Associations")&& (flag==false)))
				{
					flag=true;
					String [] collabStepAttribPathArray=collabStepAttribFulPath.split("/");
					if(collabStepAttribPathArray.length>1)
					{
						String collabStepAttribName=collabStepAttribPathArray[collabStepAttribPathArray.length-1];				
						this.associationsPath.add(collabStepAttribName);
					}
								
				}
				else if((collabStepAttribFulPath.startsWith("Product Catalog Global Spec/Packaging Hierarchy/Each Level")&& (flag==false)))
				{
					flag=true;
					this.eachLevelPath.add(collabStepAttribFulPath);					
				}
				else if((!collabStepAttribFulPath.startsWith("Product Catalog Global Spec")) && (flag==false))
				{
					flag=true;
					this.secondarySpecPath.add(collabStepAttribFulPath);					
				}
				else
				{
					if((flag==false) && (collabStepAttribFulPath.startsWith("Product Catalog Global Spec")))			
					{												
						this.directPath.add(collabStepAttribFulPath);	
						}			
				}
				flag=false;		
			}
			
		}
		catch(Exception e)
		{
			rpLogger1.logDebug("Exception in getAttributePath "+e);
		}
		
	}
	public String fnGetAttributeValue(Item ctgCurrentItem, String DirectAttribPath, String attribTypeName)
	{
		boolean flag=false;
		Object keyValueObj =null;
		String keyValue = null;
		if(attribTypeName.equals("INTEGER") && (null!=(ctgCurrentItem.getAttributeValue(DirectAttribPath))))
		{
			flag=true;						 		
			int keyVal1;
			keyVal1 = (Integer) ctgCurrentItem.getAttributeValue(DirectAttribPath);
			keyValue =  String.valueOf(keyVal1);
		}
		else if(attribTypeName.equals("NUMBER"))
		{
			if((flag == false) && (null!=(ctgCurrentItem.getAttributeValue(DirectAttribPath))))
			{	 
				flag=true;								 		
				keyValue = (String) ctgCurrentItem.getAttributeValue(DirectAttribPath).toString();
			}
		}
		else if((attribTypeName.equals("IMAGE_URL"))||(attribTypeName.equals("FLAG"))||(attribTypeName.equals("URL"))||(attribTypeName.equals("DATE"))||(attribTypeName.equals("SEQUENCE")))
		{
			if((flag == false)){
			flag=true;								 		
			keyValueObj = (Object) ctgCurrentItem.getAttributeValue(DirectAttribPath);
			if(null!=keyValueObj){
			keyValue=keyValueObj.toString();
			}
			else
			{
				keyValue="";
			}
			}
		}
		else if(attribTypeName.equals("RELATIONSHIP") && (flag == false))
		{
			flag=true;								 		
			Item relatedItemObj = (Item) ctgCurrentItem.getAttributeValue(DirectAttribPath);
			if(relatedItemObj!=null){
			String relatedSpec=relatedItemObj.getCatalog().getSpec().getName();
			keyValue=(String)relatedItemObj.getAttributeValue(relatedSpec+"/UniqueProductQualifier");
			}
		}
		else
		{
			if(flag == false)
			{	
				keyValue= (String) ctgCurrentItem.getAttributeValue(DirectAttribPath);
			}						 
		}	
		flag=false;
		return keyValue;
	}	
	public void excelCreation(ArrayList<HashMap<String, String>> mapListOfVal,String docPath)
	{
		try 
	       {
			   this.rpLogger1.logDebug("Excel sheet creation is started .....");
			   String FILE=null;
	    	   int columnCount=0;
	    	   int headerColumnCount=0;	    	   
	    	   int countflag=1;	    	      	   
	    	   HashMap<String,String> maxHashKey=new  HashMap<String,String>();	   	 
	    	   this.outputStream = new FileOutputStream("/opt/IBM/MDMCSv115/Reports/WFSelectedItemReport/Search Result Report -"+this.date+".xlsx");
	    	   this.rpLogger1.logDebug(this.ctx.getCurrentUser().getName());
	    	   this.wattsWaterExcelDoc=PIMContextFactory.getCurrentContext().getDocstoreManager().createAndPersistDocument(docPath);
	    	   this.book=new XSSFWorkbook();
	    	   this.sheet = this.book.createSheet(this.collabStep.getName());
	    	   this.rpLogger1.logDebug(this.sheet.getSheetName());
	    	   XSSFCellStyle style = this.book.createCellStyle();
			    style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
			    style.setFillPattern(CellStyle.SOLID_FOREGROUND);
			    XSSFCellStyle styleRed = this.book.createCellStyle();
			    styleRed.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
			    styleRed.setFillPattern(CellStyle.SOLID_FOREGROUND);
			    XSSFFont font = this.book.createFont();
		        font.setColor(IndexedColors.BLACK.getIndex());	
		        font.setBoldweight(XSSFFont.BOLDWEIGHT_BOLD);
		        style.setFont(font);
		        XSSFFont  fontRed = this.book.createFont();		       
		        XSSFCell headerCell=null;
		        int maxKeySize=0;		       
		        this.headerRow = this.sheet.createRow(0);	
		        ArrayList<String> tempHeadRowList=new ArrayList<String>();
		        ArrayList<String> directtempHeadRowList=new ArrayList<String>();
		        ArrayList<String> EAtempHeadRowList=new ArrayList<String>();
		        ArrayList<String> highertempHeadRowList=new ArrayList<String>();
		        ArrayList<String> associationtempHeadRowList=new ArrayList<String>();
		        ArrayList<String> secondarySpecHeadRowList=new ArrayList<String>();
		        Iterator<HashMap<String, String>> mapListIterator = mapListOfVal.iterator();
		        for(int j=0;j<mapListOfVal.size();j++)
				{
		            HashMap<String,String> hashValues = mapListOfVal.get(j); 
		           	Iterator hashkeysiterator = hashValues.keySet().iterator();
		           	while(hashkeysiterator.hasNext())
		           	{			   	   			
		           		String key = (String) hashkeysiterator.next();
		           		if(j==0)
		           		{
		           			tempHeadRowList.add(key);		           				           			
		           		}
		           		else
		           		{	           				
		           			for(int k=0;k < tempHeadRowList.size();k++){		           			
		           				if(!tempHeadRowList.contains(key)){
		           					    tempHeadRowList.add(key);      	
		           					}
		           			
		           				}		           					           			
		           		}		           	       	  	               		
	             }	           		           
				}
		        columnCount=3;	
		        for(String eachLevel: this.EAunitOfMessure)
				{
		        	for(int n=0;n<tempHeadRowList.size();n++)
		        	{ 	      				
		        		if(tempHeadRowList.get(n).contains(":")){		        		 		
		        			if(tempHeadRowList.get(n).startsWith(eachLevel)){		        				
		        				EAtempHeadRowList.add(tempHeadRowList.get(n));		        				
		        				}
		        		}
  					}
		        }
		       for(String higherLevel: this.higherUnitOfMessure)
  			   {
		         for(int n=0;n<tempHeadRowList.size();n++)
				 { 	      				
				 		if(tempHeadRowList.get(n).contains(":")){
				    			if(tempHeadRowList.get(n).contains(higherLevel)){		        				
				    				highertempHeadRowList.add(tempHeadRowList.get(n));	
				      			}
				       		}
		        		}
  					}
		      if(!this.associationType.equals(null)){
		      for(String associationType: this.associationType)
  		      {
		    	  for(int n=0;n<tempHeadRowList.size();n++)
		        	{ 	      				
		        		if(tempHeadRowList.get(n).contains(":")){
		        			if(tempHeadRowList.get(n).contains(associationType)){		        			
		        				associationtempHeadRowList.add(tempHeadRowList.get(n));		        				
		        			}
  						}
		        		}
		        	}
		      }	 
		      if(!this.secondarySpecVal.equals(null)){
			      for(String secondarySpetType: this.secondarySpecVal)
	  		      {
			    	  for(int n=0;n<tempHeadRowList.size();n++)
			        	{ 	      				
			        		if(!tempHeadRowList.get(n).contains(":")){
			        			if(tempHeadRowList.get(n).contains(secondarySpetType)){		        			
			        				secondarySpecHeadRowList.add(tempHeadRowList.get(n));		        				
			        			}
	  						}
			        		}
			        	}
			      }	      
		for(int n=0;n<tempHeadRowList.size();n++)
    	{ 	      				
    		if(!tempHeadRowList.get(n).contains(":")){ 		        		
		        		directtempHeadRowList.add(tempHeadRowList.get(n));
		       	}
		 }		      
		     	for(String directHead : directtempHeadRowList){		        	
		        		if(directHead.equals("PIM_ID"))
	                	{	      							
	      					headerCell = this.headerRow.createCell(0);
	      					headerCell.setCellValue(directHead); 	      					
	      			
	      				}
	      				else if(directHead.equals("Part Number"))
	      				{	      							
	      					headerCell = this.headerRow.createCell(1);
	      					headerCell.setCellValue(directHead);	      					
	      				}
	      				else if(directHead.equals("SystemOfRecord"))
	      				{	      							
	      					headerCell = this.headerRow.createCell(2);
	      					headerCell.setCellValue(directHead);
	      					
	      				}	      				
	      				else
	      				{	      					
	      					headerCell = this.headerRow.createCell(columnCount++);
	      					headerCell.setCellValue(directHead);	      					
	      				}
		        		headerCell.setCellStyle(style);	
		        	}  			      		    
		        	for(String eachLevelHead: EAtempHeadRowList)
		        	{
		        		headerCell = this.headerRow.createCell(columnCount++);
      					headerCell.setCellValue(eachLevelHead);	
      					headerCell.setCellStyle(style);      					
		        	}
		        	if(!highertempHeadRowList.equals(null)){	
		        	for(String highLevelHead: highertempHeadRowList)
		        	{
		        		headerCell = this.headerRow.createCell(columnCount++);
      					headerCell.setCellValue(highLevelHead);	     				
      					
		        	}
		        	headerCell.setCellStyle(style);	
		        	}
		        	if(!associationtempHeadRowList.equals(null)){		        	
		        	for(String associationHead: associationtempHeadRowList)
		        	{
		        		headerCell = this.headerRow.createCell(columnCount++);
      					headerCell.setCellValue(associationHead);	      					
		        	}
		        	headerCell.setCellStyle(style);	
		        	}
		        	if(!secondarySpecHeadRowList.equals(null)){		        	
			        	for(String secondarySpecHead: secondarySpecHeadRowList)
			        	{
			        		headerCell = this.headerRow.createCell(columnCount++);
	      					headerCell.setCellValue(secondarySpecHead);	
	      					this.rpLogger1.logDebug("secondarySpecHead======"+secondarySpecHead+"columnCount==="+columnCount);
			        	}
			        	headerCell.setCellStyle(style);	
			        	}		        	
		        for(int j=0;j<tempHeadRowList.size();j++) {		        	
            		this.cell = this.headerRow.getCell(j);       		
            		String headerName=this.cell.getStringCellValue();            		
            		for(int k=0;k<this.requiedAttributes.size();k++){            			
            		 	if(headerName.contains(this.requiedAttributes.get(k)))
      					{         				
            			   	this.cell.setCellValue(headerName+":REQUIRED_IN_STEP");
            				fontRed.setColor(IndexedColors.RED.getIndex());
      						fontRed.setBoldweight(XSSFFont.BOLDWEIGHT_BOLD);      						
      						styleRed.setFont(fontRed);
      						this.cell.setCellStyle(styleRed);        												
      						break;
      					}		                			
            		}            		
            }
		        Iterator<HashMap<String, String>> mapTotalListIterator = mapListOfVal.iterator();
		    	while(mapTotalListIterator.hasNext()){
		    			HashMap<String,String> hashValues = mapTotalListIterator.next();
		    		    this.valueRow = this.sheet.createRow(countflag);
		    		    countflag++;   
		    		    headerColumnCount=3;
		                for(int j=0;j<tempHeadRowList.size();j++) {	           		  
	                		Iterator hashValuesiterator = hashValues.entrySet().iterator();
	                		this.cell = this.headerRow.getCell(j);
	                		String headerName=validateCellValue(cell,countflag);//this.cell.getStringCellValue();
	                		if(headerName.contains(":REQUIRED_IN_STEP"))
	                		{
	                			String[] headerNameSplit=headerName.split(":REQUIRED_IN_STEP");
	                			headerName=headerNameSplit[0];	
	                       		}
		                		
		                	while(hashValuesiterator.hasNext())
		                	   {
		                		   Map.Entry<String,String> e = (Map.Entry) hashValuesiterator.next();
		                		   String key = e.getKey();		                		  
		                		   String value = e.getValue(); 	   
		                     		   if(key.equals(headerName)){	
		                			   if(key.equals("PIM_ID"))
		                			   {
		                				    this.valueRow.createCell(0).setCellValue(value);
		                				    
		                			   }
		                			   else if(key.equals("Part Number"))
		                			   {		                				   
		                				   this.valueRow.createCell(1).setCellValue(value);
		                			   }
		                			   else  if(key.equals("SystemOfRecord"))
		                			   {
		                				   this.valueRow.createCell(2).setCellValue(value);
		                			   }
		                			   else
		                			   {
		                			   this.valueRow.createCell(headerColumnCount).setCellValue(value);
		                			  
		                			   headerColumnCount++;
		                			   }			                			   
		                		   	}
		                		   else
		                		   {
		                			   continue;
		                		   }
		                	   }	                	
		                   }		              
		    		   }  	
		      this.book.write(this.outputStream);			    	 
   	    	  this.outputStream.close();   	    	     	    	
		      this.rpLogger1.logDebug("XL sheet created successfully");  
		      FileInputStream fileIn=new FileInputStream("/opt/IBM/MDMCSv115/Reports/WFSelectedItemReport/Search Result Report -"+this.date+".xlsx");
		      this.rpLogger1.logDebug("Document Path"+this.wattsWaterExcelDoc.getPath());
		      this.wattsWaterExcelDoc.setContent(fileIn);		      	
		    	   	    	  
	       }catch(Exception e)
	           {
	    	   this.rpLogger1.logDebug("Exception in excelCreation---->"+e.getMessage()); 
	    	   StringWriter sw = new StringWriter ();
	    	   e.printStackTrace(new PrintWriter(sw));
	    	//   this.rpLogger1.logDebug(sw.toString());
	    	   try {
				sw.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	           }	       
	       
  	    	    	   	
	    	}
	private String validateCellValue(Cell cell, int rowNumber) throws Exception 
    {
          try
          {
               if(null!=cell)
               {
            	   //this.rpLogger1.logDebug("Cell type is :" +cell.getCellType()); 
               }
                    
               else
               {
                    this.rpLogger1.logDebug("Cell is blank...");
                    return "";
               }
                    
               switch(cell.getCellType())
               {
               case Cell.CELL_TYPE_NUMERIC:
               
               double value = cell.getNumericCellValue();
               //value = Math.round(value);
               if(DateUtil.isCellDateFormatted(cell)) 
               {
                    if(DateUtil.isValidExcelDate(value))
                    {
                          Date date = DateUtil.getJavaDate(value);
                          SimpleDateFormat dateFormat = new SimpleDateFormat("mm/dd/yyyy"); 
                          //this.rpLogger1.logDebug("Cell type Date...value is :"+dateFormat.format(date));
                          return dateFormat.format(date);                                       
                    }
                    else 
                    {
                          throw new Exception("Invalid Date value found at row number " +
                                    rowNumber +" and column number "+cell.getColumnIndex());}
               } 
               else  
               {
                    //this.rpLogger1.logDebug("Cell type Numeric...value is :"+String.valueOf(value));
                    return String.valueOf(value) ;
               }
               
               
               case Cell.CELL_TYPE_STRING:
                    //this.rpLogger1.logDebug("Cell type String...value is :"+cell.getStringCellValue());
                    return cell.getStringCellValue();  
                           
               case Cell.CELL_TYPE_BLANK:
                    //this.rpLogger1.logDebug("Cell type Blank...value is :");
                   return "";  
               default:
                    return ""; 
               
               }
          }
          catch(Exception ex)
          {
               return "";
          }
         
    }
							
}