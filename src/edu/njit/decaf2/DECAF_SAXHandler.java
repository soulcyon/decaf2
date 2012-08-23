/* ______  _______ _______ _______ _______      _____ _____
 * |     \ |______ |       |_____| |______        |     |  
 * |_____/ |______ |______ |     | |            __|__ __|__
 */
package edu.njit.decaf2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import edu.njit.decaf2.data.FailureNode;

/**
 * DECAF 2 - DECAF_SAXHandler
 *
 * @author Sashank Tadepalli
 * @version 2.0
 *
 */
public class DECAF_SAXHandler extends DefaultHandler {
	
	/*
	 * Singleton Instance
	 */
	private static DECAF_SAXHandler 		instance;
	
	/*
	 * Resulting data
	 */
	private double[][] 						demandMatrix;
	private ConcurrentHashMap<String, FailureNode>	nodeCache = new ConcurrentHashMap<String, FailureNode>();
	
	/*
	 * SAX Flags
	 */
	private boolean							grabDemandInfo = false;
	private boolean							grabDemandChangeTo = false;
	private boolean							grabDemandChangeRate = false;
	private boolean							grabComponentInfo = false;
	private boolean 						grabComponent = false;
	private boolean							grabCompType = false;
	private boolean							grabCompRedundancy = false;
	private boolean							grabCompRequired = false;
	private boolean							grabCompDemand = false;
	private boolean							grabCascading = false;
	private boolean							grabCompDemandRate = false;
	private boolean 						grabDemandLevels = false;
	
	/*
	 * Temporary caches of data
	 */
	private HashMap<String, List<String>>	cascadingCache = new HashMap<String, List<String>>();
	private int								currentDemand;
	private String 							currentType;
	private int 							currentRequired;
	private int 							currentRedundancy;
	private double[]						currentFailureRates;
	private String 							currentCascadingType;
	private int								currentDemandChangeFrom;
	private int								currentDemandChangeTo;

	public static DefaultHandler getInstance(){
		return instance == null ? instance = new DECAF_SAXHandler() : instance;
	}
	
	public void startElement(String a, String b, String tag, Attributes attr) throws SAXException {
		
		if( grabCompDemand ){
			if( tag.equalsIgnoreCase("failure-rate") && currentDemand != -1 ){
				grabCompDemandRate = true;
			} else {
				grabCompDemand = false;
			}
		}
		
		if( grabCascading ){
			if( tag.equalsIgnoreCase("triggers-failure") ){
				currentCascadingType = attr.getValue("type");
			} else {
				grabCascading = false;
				currentCascadingType = null;
			}
		}

		if( grabDemandInfo ){
			if( tag.equalsIgnoreCase("demand-levels") ){
				grabDemandLevels = true;
			} else if( tag.equalsIgnoreCase("change-rate") ){
				currentDemandChangeFrom = Integer.parseInt(attr.getValue("from"));
				grabDemandChangeTo = true;
			} else if( grabDemandChangeTo && tag.equalsIgnoreCase("to") ){
				currentDemandChangeTo = Integer.parseInt(attr.getValue("level"));
				grabDemandChangeRate = true;
			} else {
				grabDemandInfo = false;
				grabDemandChangeRate = false;
			}
		}
		
		if( grabComponent ){
			if( tag.equalsIgnoreCase("type") ){
				grabCompType = true;
			} else if( tag.equalsIgnoreCase("redundancy") ){
				grabCompRedundancy = true;
			} else if( tag.equalsIgnoreCase("required") ){
				grabCompRequired = true;
			} else if( tag.equalsIgnoreCase("cascade-failure") ){
				grabCascading = true;
			} else if( tag.equalsIgnoreCase("demand") ){
				grabCompDemand = true;
				currentDemand = Integer.parseInt(attr.getValue("demandID"));
			}
		}
		
		if( grabComponentInfo && tag.equalsIgnoreCase("component") ){
			grabComponent = true;
		}
		
		if( tag.equalsIgnoreCase("component-info") ){
			grabComponentInfo = true;
		}
		
		if( tag.equalsIgnoreCase("demand-info") ){
			grabDemandInfo = true;
		}
		
	}
	
	public void endElement(String a, String b, String tag) throws SAXException {
		
		if( grabDemandInfo && tag.equalsIgnoreCase("demand-info") ){
			grabDemandInfo = false;
		}
		
		if( grabComponentInfo && tag.equalsIgnoreCase("component-info") ){
			for( String key : nodeCache.keySet() ){
				FailureNode temp = nodeCache.get(key);
				for( String casKey : cascadingCache.keySet() ){
					List<String> gamma = cascadingCache.get(casKey);
					System.out.println(casKey + ":" + gamma.size());
					for( String k : gamma )
						if( !k.startsWith(key) )
							temp.addCascadingFailure(nodeCache.get(k.split(":")[0]), Double.parseDouble(k.split(":")[1]));
				}
			}
			grabComponentInfo = false;
		}
		
		if( grabComponent && tag.equalsIgnoreCase("component") ){
			FailureNode temp = new FailureNode(currentRequired, currentType, currentRedundancy, currentFailureRates);
			nodeCache.put(currentType, temp);
			grabComponent = false;
		}
		
		if( grabCompType && tag.equalsIgnoreCase("type") ){
			grabCompType = false;
		}
		
		if( grabCompRedundancy && tag.equalsIgnoreCase("redundancy") ){
			grabCompRedundancy = false;
		}
		
		if( grabCompRequired && tag.equalsIgnoreCase("required") ){
			grabCompRequired = false;
		}
		
		if( grabCascading && tag.equalsIgnoreCase("cascade-failures") ){
			grabCascading = false;
			grabComponent = true;
		}
		
		if( grabCompDemand && tag.equalsIgnoreCase("demand") ){
			grabCompDemand = false;
			grabComponent = true;
		}
		
		if( grabCompRequired && tag.equalsIgnoreCase("failure-rate") ){
			grabCompDemandRate = false;
		}
	}
	
	public void characters(char a[], int b, int c) throws SAXException {
		String res = new String(a, b, c);
		
		if( grabDemandChangeRate ){
			demandMatrix[currentDemandChangeFrom][currentDemandChangeTo] = Double.parseDouble(res);
			grabDemandChangeRate = false;
		}
		
		if( grabCompType ){
			currentType = res;
			cascadingCache.put(currentType, new ArrayList<String>());
			grabCompType = false;
		}
		
		if( grabCompRedundancy ){
			currentRedundancy = Integer.parseInt(res);
			grabCompRedundancy = false;
		}
		
		if( grabCompRequired ){
			currentRequired = Integer.parseInt(res);
			grabCompRequired = false;
		}
		
		if( grabDemandLevels ){
			int t = Integer.parseInt(res);
			demandMatrix = new double[t][t];
			currentFailureRates = new double[t];
			grabDemandLevels = false;
		}
		
		if( grabCompDemand && grabCompDemandRate && res.trim().length() > 0 ){
			currentFailureRates[currentDemand] = Double.parseDouble(res);
			grabCompDemandRate = false;
		}
		if( grabCascading && currentCascadingType != null && res.trim().length() > 0 ){
			cascadingCache.get(currentType).add(currentCascadingType + ":" + res);
			grabCascading = false;
		}
	}
	
	public static double[][] getDemand(){
		return instance.demandMatrix;
	}
	
	public static HashMap<String, FailureNode> getNodeMap(){
		HashMap<String, FailureNode> temp = new HashMap<String, FailureNode>();
		for( String k : instance.nodeCache.keySet() ){
			temp.put(k, instance.nodeCache.get(k));
		}
		return temp;
	}
}
