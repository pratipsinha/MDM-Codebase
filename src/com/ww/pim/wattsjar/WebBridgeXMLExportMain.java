package com.ww.pim.wattsjar;

import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;



import com.ibm.pim.attribute.AttributeDefinition;
import com.ibm.pim.attribute.AttributeInstance;
import com.ibm.pim.attribute.AttributeDefinition.Type;
import com.ibm.pim.catalog.Catalog;
import com.ibm.pim.catalog.item.Item;
import com.ibm.pim.collection.PIMCollection;
import com.ibm.pim.common.exceptions.PIMInternalException;
import com.ibm.pim.context.Context;
import com.ibm.pim.context.PIMContextFactory;
import com.ibm.pim.extensionpoints.CatalogExportFunction;
import com.ibm.pim.extensionpoints.CatalogExportFunctionArguments;
import com.ibm.pim.hierarchy.Hierarchy;
import com.ibm.pim.hierarchy.category.Category;
import com.ibm.pim.spec.Spec;
import com.ibm.pim.utils.Logger;
import com.ibm.pim.collaboration.CollaborationArea;
import com.ibm.pim.collaboration.CollaborationItem;
import com.ibm.pim.collaboration.CollaborationObject;
import com.ibm.pim.collaboration.CollaborationStep;
import com.ww.pim.common.utils.Constant;


import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import java.io.File;

public class WebBridgeXMLExportMain implements CatalogExportFunction  {
	private Context ctx = null;
	private Logger rpLogger = null;
	Catalog prodCtg = null;
	String keyAttribValue =null;	
	HashMap<String,Integer> maxKeyHeaderSizeFinder=new HashMap<String,Integer>();
	ArrayList<HashMap<String, String>> mapListOfVal = new ArrayList<HashMap<String, String>>();	
	ArrayList<String> itemAttribDirectPath=new ArrayList<String>();
	ArrayList<String> itemAttribChildName=new ArrayList<String>();
	ArrayList<HashMap<String, Integer>> maxKeyHeaderSizeArray=new ArrayList<HashMap<String, Integer>>();
	List<AttributeDefinition> childAttribCollection = null;
	public static FileOutputStream outputStream = null;	
	CollaborationArea QADPartsCollabArea=null;	
	Hierarchy hierarchyOfItem=null;
	public boolean catalogExport(CatalogExportFunctionArguments arg0)
	{
		try
		{
			PIMCollection<Item> ctgCurrentItem=null;
			ArrayList<CollaborationItem>collabCurrentItem=new ArrayList<CollaborationItem>();
			this.ctx = PIMContextFactory.getCurrentContext();
			this.rpLogger= this.ctx.getLogger("WattsReportLog");	
			this.prodCtg=arg0.getCatalog();
			this.rpLogger.logDebug("Web Bridge Export --- XML Generation Started..");	
			QADPartsCollabArea=this.ctx.getCollaborationAreaManager().getCollaborationArea(Constant.QADPARTSMAINTCOLAREANAME);
			getPrimaryAttribPath();
			PIMCollection<Item> ctgItemCollection =arg0.getItems();		
			getCtgItemAttribValue(ctgItemCollection);
			List<CollaborationStep> QADCollabStepList=QADPartsCollabArea.getNonEmptySteps();
			Iterator QADCollabStepListIterator=QADCollabStepList.iterator();
			while(QADCollabStepListIterator.hasNext())
			{
				CollaborationStep QADCollabStep =(CollaborationStep) QADCollabStepListIterator.next();
				PIMCollection<CollaborationObject>QADCollabObjectList=QADCollabStep.getContents();
				Iterator QADCollabObjectListIterator=QADCollabObjectList.iterator();
				while(QADCollabObjectListIterator.hasNext())
					{
					 CollaborationObject QADCollabObject=(CollaborationObject) QADCollabObjectListIterator.next();
					 CollaborationItem QADCollabItem=(CollaborationItem) QADCollabObject;
					 String QADCollabItemPrimeKey=QADCollabItem.getPrimaryKey();					
					 if(this.prodCtg.containsItem(QADCollabItemPrimeKey)== false)
					 {
						collabCurrentItem.add(QADCollabItem);
					 }
				}
			}	
			getCollabItemAttribValue(collabCurrentItem);
			xmlCreation(this.mapListOfVal);
		
					
		}catch (PIMInternalException e)
	    {
		      this.rpLogger.logDebug("Exception...." + e);
		}
		catch (NullPointerException e)
		{
			this.rpLogger.logDebug("NullPointerException...." + e);
		}
			return false;
	}
		public void getPrimaryAttribPath()
		{
			String primarySpecDirectAttribPath =null;
			List<AttributeDefinition> specAttributeDefCollection = null;
			this.hierarchyOfItem=this.ctx.getHierarchyManager().getHierarchy("Templates Hierarchy");
			try
			{
				Spec primarySpec=this.prodCtg.getSpec();
				String primarySpecName=primarySpec.getName();
				specAttributeDefCollection= primarySpec.getAttributeDefinitions();
				Iterator specAttributeDefCollectionIterator = specAttributeDefCollection.iterator();
				while(specAttributeDefCollectionIterator.hasNext() )
				{
					AttributeDefinition itemSpecAttrib=(AttributeDefinition) specAttributeDefCollectionIterator.next();
					Type attribtype= itemSpecAttrib.getType();
					String attribTypeName=attribtype.name();
					boolean flag=false;
					AttributeDefinition itemSpecDirectAttrib = itemSpecAttrib;
					if(!attribTypeName.equals("GROUPING"))
					{
						primarySpecDirectAttribPath= itemSpecDirectAttrib.getPath();						
						String attribName = itemSpecDirectAttrib.getName();
						if(!attribName.equals("PIM_ID")){
						if(primarySpecDirectAttribPath.startsWith("Product Catalog Global Spec/Packaging Hierarchy/Paths/Higher Packaging Levels"))
						{
							flag=true;
							this.itemAttribChildName.add(itemSpecDirectAttrib.getName());							
						}
						else
						{
							if((flag == false)&& !(primarySpecDirectAttribPath.startsWith("Product Catalog Global Spec/Associations")))
							{
								this.itemAttribDirectPath.add(primarySpecDirectAttribPath);									
							}
						}
						}
						flag=false;						
					}
				}				
		}
		catch (PIMInternalException e)
	    {
	      this.rpLogger.logDebug("Exception...." + e);
	    }
				
	}
		public void getCollabItemAttribValue(ArrayList<CollaborationItem>collabCurrentItemList)
		{
			try
			{
				Spec primarySpec=this.prodCtg.getSpec();
				this.rpLogger.logDebug("Inside Collaboration Item Start");		
				String primarySpecName=primarySpec.getName();
				Iterator collabCurrentItemIterator = collabCurrentItemList.iterator();
				this.prodCtg = this.ctx.getCatalogManager().getCatalog("Product Catalog");
				String   headerValue =null;
				String UnitOfMessure=null;
				int test=0;
				while(collabCurrentItemIterator.hasNext() && test < 5 )
				{
					test++;
					HashMap<String,String> itemAttribValue=new HashMap<String,String>();
					CollaborationItem collabCurrentItem = (CollaborationItem) collabCurrentItemIterator.next();
					String hashPimPrimaryKey = collabCurrentItem.getPrimaryKey();					
					String systemOfRecord=(String)collabCurrentItem.getAttributeValue(primarySpecName+"/SystemOfRecord");
					if(systemOfRecord=="US-QAD"){	
					this.rpLogger.logDebug("Collab hashPimPrimaryKey======"+hashPimPrimaryKey);
					Collection<Category>collabItemCatCollec=collabCurrentItem.getCategories();
					Iterator collabItemCatCollecIterator=collabItemCatCollec.iterator();
					while(collabItemCatCollecIterator.hasNext())
					{
						Category collabItemCategory=(Category)collabItemCatCollecIterator.next();
						String collabItemCategoryName=collabItemCategory.getName();
						PIMCollection<Category> hierCategoryCollection=this.hierarchyOfItem.getCategories();
						Iterator hierCategoryCollectionIterator=hierCategoryCollection.iterator();
						while(hierCategoryCollectionIterator.hasNext()){
							Category hierCategory=(Category) hierCategoryCollectionIterator.next();
							if(hierCategory.getName().equals(collabItemCategoryName))
							{							
							String headerName="TempCategory"+"_"+collabItemCategoryName;
							itemAttribValue.put(headerName,collabItemCategoryName);
						}
						}
						
					}				
					
					for (String DirectAttribPath : this.itemAttribDirectPath)
					{
						AttributeDefinition attribDef=primarySpec.getAttributeDefinition(DirectAttribPath);
						Type attribType= attribDef.getType();					
						String attribTypeName=attribType.toString();					
						this.keyAttribValue=fnGetCollabAttributeValue(collabCurrentItem,DirectAttribPath,attribTypeName);
						String [] splitHeaderValues =   DirectAttribPath.split("/");
						if(splitHeaderValues.length > 1)
						{
							headerValue = splitHeaderValues[splitHeaderValues.length-1];						
						}
						if(this.keyAttribValue!=null){
						itemAttribValue.put(headerValue,this.keyAttribValue);		
						}
					}				
					AttributeInstance pathAttribInstance= collabCurrentItem.getAttributeInstance(primarySpecName+"/Packaging Hierarchy#0"+"/Paths");
					int pathChildrenSize = pathAttribInstance.getChildren().size();
					if(pathChildrenSize > 0)
					{
						for(int i=0; i<pathChildrenSize;i++)
						{
						 AttributeInstance pathHierarchyLevelAttribInstance= collabCurrentItem.getAttributeInstance(primarySpecName+"/Packaging Hierarchy#0"+"/Paths#"+i+"/Higher Packaging Levels");
						 int pathHierarchyLevelSize = pathHierarchyLevelAttribInstance.getChildren().size();
						 for(int k=0; k<pathHierarchyLevelSize;k++)
						 {
							 for (String itemChildNodeAttribName : this.itemAttribChildName)
							 {	
								String childNodePath=primarySpecName+"/Packaging Hierarchy#0"+"/Paths#"+i+"/Higher Packaging Levels#"+k+"/"+itemChildNodeAttribName;						    
								AttributeDefinition attribDef1=primarySpec.getAttributeDefinition(primarySpecName+"/Packaging Hierarchy/Paths/Higher Packaging Levels/"+itemChildNodeAttribName);
								if(attribDef1!=null)
								{
									Type attribType1= attribDef1.getType();
									String attribTypeName1=attribType1.toString();									  
									this.keyAttribValue=fnGetCollabAttributeValue(collabCurrentItem,childNodePath,attribTypeName1);
									if(itemChildNodeAttribName.equals("Unit of Measure"))
									{
										UnitOfMessure=fnGetCollabAttributeValue(collabCurrentItem,childNodePath,attribTypeName1);
										
									}
									String childNodeSinglePath=UnitOfMessure+"_"+itemChildNodeAttribName;
									if(this.keyAttribValue!=null){
									itemAttribValue.put(childNodeSinglePath,this.keyAttribValue);	
									}
								 }
							  }						 
							}					
						}			  			   
					}
					AttributeInstance associationAttribInstance= collabCurrentItem.getAttributeInstance(primarySpecName+"/Associations");
					int associationChildrenSize = associationAttribInstance.getChildren().size();			
					if(associationChildrenSize > 0)
					{
						for(int i=0; i<associationChildrenSize;i++)
						{
							String typeAttribPath=primarySpecName+"/Associations#"+i+"/Type";
							AttributeDefinition attribDef2=primarySpec.getAttributeDefinition(primarySpecName+"/Associations/Type");
							String associationTypePath="Type";
							if(attribDef2!=null)
							{
								Type attribType2= attribDef2.getType();
								String attribTypeName2=attribType2.toString();
								this.keyAttribValue=fnGetCollabAttributeValue(collabCurrentItem,typeAttribPath,attribTypeName2);
								if(this.keyAttribValue!=null){
								itemAttribValue.put(associationTypePath,this.keyAttribValue);
								}
							}
							String type=this.keyAttribValue;
							AttributeInstance associatedPartsAttribInstance= collabCurrentItem.getAttributeInstance(primarySpecName+"/Associations#"+i+"/AssociatedParts");
							int associatedPartsSize = associatedPartsAttribInstance.getChildren().size();
							for(int k=0; k<associatedPartsSize;k++)
							{
								String associatedProductAttribPath=primarySpecName+"/Associations#"+i+"/AssociatedParts#"+k;
								AttributeDefinition attribDef3=primarySpec.getAttributeDefinition(primarySpecName+"/Associations/AssociatedParts");
								String associationProductPath=type+"#"+k+"_"+"AssociatedParts";
								if(attribDef3!=null)
								{
									Type attribType3= attribDef3.getType();
									String attribTypeName3=attribType3.toString();
									this.keyAttribValue=fnGetCollabAttributeValue(collabCurrentItem,associatedProductAttribPath,attribTypeName3);
									if(this.keyAttribValue!=null){
									itemAttribValue.put(associationProductPath,this.keyAttribValue);
									}
								}
							}
						}
					}
				this.mapListOfVal.add(itemAttribValue);
			}
				}
				
				this.rpLogger.logDebug("Inside Collaboration Item End");					
			}
			catch(Exception e){
				e.printStackTrace();
				this.rpLogger.logDebug("Exception in getItemPrimaryAttribValue");
			}
		}
		public String fnGetCollabAttributeValue(CollaborationItem collabCurrentItem,  String DirectAttribPath, String attribTypeName)
		{
			boolean flag=false;
			Object keyValueObj =null;
			String keyValue = null;
			if(attribTypeName.equals("INTEGER") && (null!=(collabCurrentItem.getAttributeValue(DirectAttribPath))))
			{
				flag=true;						 		
				int keyVal1;
				keyVal1 = (Integer) collabCurrentItem.getAttributeValue(DirectAttribPath);
				keyValue =  String.valueOf(keyVal1);
			}
			else if(attribTypeName.equals("NUMBER"))
			{
				if((flag == false) && (null!=(collabCurrentItem.getAttributeValue(DirectAttribPath))))
				{	 
					flag=true;								 		
					keyValue = (String) collabCurrentItem.getAttributeValue(DirectAttribPath).toString();
				}
			}
			else if((attribTypeName.equals("IMAGE_URL"))||(attribTypeName.equals("FLAG"))||(attribTypeName.equals("URL"))||(attribTypeName.equals("DATE")))
			{
				if((flag == false)){
				flag=true;								 		
				keyValueObj = (Object) collabCurrentItem.getAttributeValue(DirectAttribPath);
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
				Item relatedItemObj = (Item) collabCurrentItem.getAttributeValue(DirectAttribPath);
				if(relatedItemObj!=null){
				String relatedSpec=relatedItemObj.getCatalog().getSpec().getName();
				keyValue=(String)relatedItemObj.getAttributeValue(relatedSpec+"/UniqueProductQualifier");
				}
			}
			else
			{
				if(flag == false)
				{	
					keyValue= (String) collabCurrentItem.getAttributeValue(DirectAttribPath);
				}						 
			}	
			flag=false;
			return keyValue;
		}	
		public void getCtgItemAttribValue(PIMCollection<Item> ctgCurrentItemCollection)
		{
			try
			{
				this.rpLogger.logDebug("Inside Catalog Item");
				Spec primarySpec=this.prodCtg.getSpec();
				String primarySpecName=primarySpec.getName();
				Iterator ctgCurrentItemCollectionIterator = ctgCurrentItemCollection.iterator();
				this.prodCtg = this.ctx.getCatalogManager().getCatalog("Product Catalog");
				String   headerValue =null;
				String UnitOfMessure=null;
				int test=0;
				while(ctgCurrentItemCollectionIterator.hasNext() && test < 10)
				{
					test++;					
					Item ctgCurrentItem = (Item) ctgCurrentItemCollectionIterator.next();
					String hashPimPrimaryKey = ctgCurrentItem.getPrimaryKey();					
					String systemOfRecord=(String)ctgCurrentItem.getAttributeValue(primarySpecName+"/SystemOfRecord");
					if(systemOfRecord=="US-QAD"){
					HashMap<String,String> itemAttribValue=new HashMap<String,String>();
					this.rpLogger.logDebug("Catalog hashPimPrimaryKey==========="+hashPimPrimaryKey);
					Collection<Category>ctgbItemCatCollec=ctgCurrentItem.getCategories();
					Iterator ctgItemCatCollecIterator=ctgbItemCatCollec.iterator();
					while(ctgItemCatCollecIterator.hasNext())
					{
						Category ctgItemCategory=(Category)ctgItemCatCollecIterator.next();
						String ctgItemCategoryName=ctgItemCategory.getName();
						PIMCollection<Category> hierCategoryCollection=this.hierarchyOfItem.getCategories();
						Iterator hierCategoryCollectionIterator=hierCategoryCollection.iterator();
						while(hierCategoryCollectionIterator.hasNext()){
							Category hierCategory=(Category) hierCategoryCollectionIterator.next();
							if(hierCategory.getName().equals(ctgItemCategoryName))
							{
							String headerName="TempCategory"+"_"+ctgItemCategoryName;
							itemAttribValue.put(headerName,ctgItemCategoryName);
						}
						}
					}						
					for (String DirectAttribPath : this.itemAttribDirectPath)
					{
						AttributeDefinition attribDef=primarySpec.getAttributeDefinition(DirectAttribPath);
						Type attribType= attribDef.getType();					
						String attribTypeName=attribType.toString();					
						this.keyAttribValue=fnGetAttributeValue(ctgCurrentItem,DirectAttribPath,attribTypeName);
						String [] splitHeaderValues =   DirectAttribPath.split("/");
						if(splitHeaderValues.length > 1)
						{
							headerValue = splitHeaderValues[splitHeaderValues.length-1];						
						}
						if(this.keyAttribValue!=null){
						itemAttribValue.put(headerValue,this.keyAttribValue);		
						}
					}				
					AttributeInstance pathAttribInstance= ctgCurrentItem.getAttributeInstance(primarySpecName+"/Packaging Hierarchy#0"+"/Paths");
					int pathChildrenSize = pathAttribInstance.getChildren().size();
					if(pathChildrenSize > 0)
					{
						for(int i=0; i<pathChildrenSize;i++)
						{
						 AttributeInstance pathHierarchyLevelAttribInstance= ctgCurrentItem.getAttributeInstance(primarySpecName+"/Packaging Hierarchy#0"+"/Paths#"+i+"/Higher Packaging Levels");
						 int pathHierarchyLevelSize = pathHierarchyLevelAttribInstance.getChildren().size();
						 for(int k=0; k<pathHierarchyLevelSize;k++)
						 {
							 for (String itemChildNodeAttribName : this.itemAttribChildName)
							 {	
								String childNodePath=primarySpecName+"/Packaging Hierarchy#0"+"/Paths#"+i+"/Higher Packaging Levels#"+k+"/"+itemChildNodeAttribName;						    
								AttributeDefinition attribDef1=primarySpec.getAttributeDefinition(primarySpecName+"/Packaging Hierarchy/Paths/Higher Packaging Levels/"+itemChildNodeAttribName);
								if(attribDef1!=null)
								{
									Type attribType1= attribDef1.getType();
									String attribTypeName1=attribType1.toString();									  
									this.keyAttribValue=fnGetAttributeValue(ctgCurrentItem,childNodePath,attribTypeName1);
									if(itemChildNodeAttribName.equals("Unit of Measure"))
									{
										UnitOfMessure=fnGetAttributeValue(ctgCurrentItem,childNodePath,attribTypeName1);
										
									}
									String childNodeSinglePath=UnitOfMessure+"_"+itemChildNodeAttribName;
									if(this.keyAttribValue!=null){
									itemAttribValue.put(childNodeSinglePath,this.keyAttribValue);
									}
								 }
							  }						 
							}					
						}			  			   
					}
					AttributeInstance associationAttribInstance= ctgCurrentItem.getAttributeInstance(primarySpecName+"/Associations");
					int associationChildrenSize = associationAttribInstance.getChildren().size();			
					if(associationChildrenSize > 0)
					{
						for(int i=0; i<associationChildrenSize;i++)
						{
							String typeAttribPath=primarySpecName+"/Associations#"+i+"/Type";
							AttributeDefinition attribDef2=primarySpec.getAttributeDefinition(primarySpecName+"/Associations/Type");
							String associationTypePath="Type";
							if(attribDef2!=null)
							{
								Type attribType2= attribDef2.getType();
								String attribTypeName2=attribType2.toString();
								this.keyAttribValue=fnGetAttributeValue(ctgCurrentItem,typeAttribPath,attribTypeName2);
								if(this.keyAttribValue!=null){
								itemAttribValue.put(associationTypePath,this.keyAttribValue);
								}
							}
							String type=this.keyAttribValue;
							AttributeInstance associatedPartsAttribInstance= ctgCurrentItem.getAttributeInstance(primarySpecName+"/Associations#"+i+"/AssociatedParts");
							int associatedPartsSize = associatedPartsAttribInstance.getChildren().size();
							for(int k=0; k<associatedPartsSize;k++)
							{
								String associatedProductAttribPath=primarySpecName+"/Associations#"+i+"/AssociatedParts#"+k;
								AttributeDefinition attribDef3=primarySpec.getAttributeDefinition(primarySpecName+"/Associations/AssociatedParts");
								String associationProductPath=type+"#"+k+"_"+"AssociatedParts";
								if(attribDef3!=null)
								{
									Type attribType3= attribDef3.getType();
									String attribTypeName3=attribType3.toString();
									this.keyAttribValue=fnGetAttributeValue(ctgCurrentItem,associatedProductAttribPath,attribTypeName3);
									if(this.keyAttribValue!=null){
									itemAttribValue.put(associationProductPath,this.keyAttribValue);
									}
								}
							}
						}
					}
				this.mapListOfVal.add(itemAttribValue);
				
					}
			}		
				
				this.rpLogger.logDebug("Inside Catalog Item End");			
			}
			catch(Exception e){
				e.printStackTrace();
				this.rpLogger.logDebug("Exception in getItemPrimaryAttribValue");
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
			else if((attribTypeName.equals("IMAGE_URL"))||(attribTypeName.equals("FLAG"))||(attribTypeName.equals("URL"))||(attribTypeName.equals("DATE")))
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
	
	public void xmlCreation(ArrayList<HashMap<String, String>> mapListOfVal)
	{
		try 
	       {
			 this.rpLogger.logDebug("XML Creation"); 
				DocumentBuilderFactory dbFactory =
		         DocumentBuilderFactory.newInstance();
		         DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		         Document doc = dBuilder.newDocument();
		         TransformerFactory transformerFactory = TransformerFactory.newInstance();
		         Transformer transformer = transformerFactory.newTransformer();
		         Iterator<HashMap<String, String>> mapListIterator = mapListOfVal.iterator();
		         HashMap<String,String> soProductList=new HashMap<String, String>();
		         Element rootElement = doc.createElement("import_data");
		         doc.appendChild(rootElement);
			     for(int j=0;j<mapListOfVal.size();j++)
				{
			        HashMap<String,String> hashValues = mapListOfVal.get(j); 
			        this.rpLogger.logDebug(mapListOfVal.get(j));
			        Iterator hashValuesiterator = hashValues.entrySet().iterator();
			        while(hashValuesiterator.hasNext())
             	   {			        
         		   	Element soProduct = doc.createElement("so_product");
         		   	rootElement.appendChild(soProduct); 
         		   	for(int i=0;i<hashValues.entrySet().size();i++){
         		   	Map.Entry<String,String> e = (Map.Entry) hashValuesiterator.next();         		   	
         		   	String key = e.getKey();	         		  
         		   	String value = e.getValue(); 
         		   	if(key.contains("SystemOfRecord"))
         		   	{
         		   	Attr companyAttr = doc.createAttribute("Company");
         		   	companyAttr.setValue(value);
         		   	soProduct.setAttributeNode(companyAttr);
         		   	}
         		   	else if(key.equals("Part Number")){
         		   		key=key.replace(" ", "_");         			 
				   		Attr partNoAttr = doc.createAttribute(key);
				   		partNoAttr.setValue(value);				   	
				   		soProduct.setAttributeNode(partNoAttr); 				   		
				   		}
         			  else if(key.contains("Description1")){         		   
         				  	Attr descAttr = doc.createAttribute("Description1");
         				  	descAttr.setValue(value);
         				  	soProduct.setAttributeNode(descAttr);         				  
         			  }
         			  else if (key.contains("Category")){
         				  	Attr catAttr = doc.createAttribute("Category");
         				  	catAttr.setValue(value);
         				  	soProduct.setAttributeNode(catAttr);      
         				  	
         			  }
         			 else
        			  {
        				Element soProductAttrib = doc.createElement("so_product_attribute");
      				   	rootElement.appendChild(soProductAttrib);
      				   	Attr keyAttr = doc.createAttribute("name");
      				   	keyAttr.setValue(key);
      				   	Attr valueAttr = doc.createAttribute("value");
      				  	valueAttr.setValue(value);
      				  	soProductAttrib.setAttributeNode(keyAttr);     				  	
    				   	soProductAttrib.setAttributeNode(valueAttr);
    				   	soProduct.appendChild(soProductAttrib);
        			  }	  
      	       
      	         DOMSource source = new DOMSource(doc);
      	        StreamResult result = new StreamResult(new File("/opt/IBM/MDMCSv115/Reports/Web_Bridge_Export.xml"));
     	         transformer.transform(source, result);
  				   	
         		   	}
         		 
     	        
         		
             	   }
  				 
         			 
             	   }
			     this.rpLogger.logDebug("XML Created Successfully");  
			    
			    	
	       }
		catch(Exception e)
        {
            e.printStackTrace();
        }
	}
	       }

