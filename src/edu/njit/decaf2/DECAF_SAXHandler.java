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

import edu.njit.decaf2.structures.FailureNode;

/**
 * 
 * DECAF - DECAF_SAXHandler
 * 
 * @author Sashank Tadepalli, Mihir Sanghavi
 * @version 2.0
 * 
 */
public class DECAF_SAXHandler extends DefaultHandler {

	/*
	 * Singleton Instance
	 */
	private static DECAF_SAXHandler instance;

	/*
	 * Resulting data
	 */
	private double[][] demandMatrix;
	private ArrayList<String> typeList = new ArrayList<String>();
	private ConcurrentHashMap<String, FailureNode> nodeCache = new ConcurrentHashMap<String, FailureNode>();

	/*
	 * SAX Flags
	 */
	private boolean grabDemandInfo = false;
	private boolean grabDemandChangeTo = false;
	private boolean grabDemandChangeRate = false;
	private boolean grabComponentInfo = false;
	private boolean grabComponent = false;
	private boolean grabCompType = false;
	private boolean grabCompRedundancy = false;
	private boolean grabCompRequired = false;
	private boolean grabCompDemand = false;
	private boolean grabCascading = false;
	private boolean grabCompDemandRate = false;
	private boolean grabCompDemandRepair = false;
	private boolean grabDemandLevels = false;

	/*
	 * Temporary caches of data
	 */
	private HashMap<String, List<String>> cascadingCache = new HashMap<String, List<String>>();
	private int currentDemand;
	private String currentType;
	private int currentRequired;
	private int currentRedundancy;
	private double[] currentRepairRates;
	private double[] currentFailureRates;
	private String currentCascadingType;
	private int currentDemandChangeFrom;
	private int currentDemandChangeTo;

	public static void clear(){
		getInstance().demandMatrix = null;
		getInstance().typeList = new ArrayList<String>();
		getInstance().nodeCache = new ConcurrentHashMap<String, FailureNode>();
		getInstance().grabDemandInfo = false;
		getInstance().grabDemandChangeTo = false;
		getInstance().grabDemandChangeRate = false;
		getInstance().grabComponentInfo = false;
		getInstance().grabComponent = false;
		getInstance().grabCompType = false;
		getInstance().grabCompRedundancy = false;
		getInstance().grabCompRequired = false;
		getInstance().grabCompDemand = false;
		getInstance().grabCascading = false;
		getInstance().grabCompDemandRate = false;
		getInstance().grabCompDemandRepair = false;
		getInstance().grabDemandLevels = false;
		getInstance().cascadingCache = new HashMap<String, List<String>>();
		getInstance().currentDemand = 0;
		getInstance().currentType = "";
		getInstance().currentRequired = 0;
		getInstance().currentRedundancy = 0;
		getInstance().currentRepairRates = null;
		getInstance().currentFailureRates = null;
		getInstance().currentCascadingType = "";
		getInstance().currentDemandChangeFrom = 0;
		getInstance().currentDemandChangeTo = 0;
	}
	
	public static DECAF_SAXHandler getInstance() {
		return instance == null ? instance = new DECAF_SAXHandler() : instance;
	}

	public void startElement(String a, String b, String tag, Attributes attr) throws SAXException {

		if (grabCompDemand) {
			if (tag.equalsIgnoreCase("failure-rate") && currentDemand != -1) {
				grabCompDemandRate = true;
			} else if (tag.equalsIgnoreCase("repair-rate") && currentDemand != -1) {
				grabCompDemandRepair = true;
			} else {
				grabCompDemand = false;
			}
		}

		if (grabCascading) {
			if (tag.equalsIgnoreCase("triggers-failure")) {
				currentCascadingType = attr.getValue("type");
			} else {
				grabCascading = false;
				currentCascadingType = null;
			}
		}

		if (grabDemandInfo) {
			if (tag.equalsIgnoreCase("demand-levels")) {
				grabDemandLevels = true;
			} else if (tag.equalsIgnoreCase("change-rate")) {
				currentDemandChangeFrom = Integer.parseInt(attr.getValue("from"));
				grabDemandChangeTo = true;
			} else if (grabDemandChangeTo && tag.equalsIgnoreCase("to")) {
				currentDemandChangeTo = Integer.parseInt(attr.getValue("level"));
				grabDemandChangeRate = true;
			} else {
				grabDemandInfo = false;
				grabDemandChangeRate = false;
			}
		}

		if (grabComponent) {
			if (tag.equalsIgnoreCase("type")) {
				grabCompType = true;
			} else if (tag.equalsIgnoreCase("redundancy")) {
				grabCompRedundancy = true;
			} else if (tag.equalsIgnoreCase("required")) {
				grabCompRequired = true;
			} else if (tag.equalsIgnoreCase("cascade-failure")) {
				grabCascading = true;
			} else if (tag.equalsIgnoreCase("demand")) {
				grabCompDemand = true;
				currentDemand = Integer.parseInt(attr.getValue("demandID"));
			}
		}

		if (grabComponentInfo && tag.equalsIgnoreCase("component")) {
			grabComponent = true;
		}

		if (tag.equalsIgnoreCase("component-info")) {
			grabComponentInfo = true;
		}

		if (tag.equalsIgnoreCase("demand-info")) {
			grabDemandInfo = true;
		}

	}

	public void endElement(String a, String b, String tag) throws SAXException {

		if (grabDemandInfo && tag.equalsIgnoreCase("demand-info")) {
			grabDemandInfo = false;
		}

		if (grabComponentInfo && tag.equalsIgnoreCase("component-info")) {
			for (String casKey : cascadingCache.keySet()) {
				FailureNode temp = nodeCache.get(casKey);
				List<String> gamma = cascadingCache.get(casKey);
				for (String k : gamma) {
					temp.addCascadingFailure(nodeCache.get(k.split(":")[0]), Double.parseDouble(k.split(":")[1]));
				}
			}
			grabComponentInfo = false;
		}

		if (grabComponent && tag.equalsIgnoreCase("component")) {
			FailureNode temp = new FailureNode(currentRequired, currentType, currentRedundancy,
					currentFailureRates.clone(), currentRepairRates.clone());
			nodeCache.put(currentType, temp);
			grabComponent = false;
			grabDemandLevels = false;
		}

		if (grabCompType && tag.equalsIgnoreCase("type")) {
			grabCompType = false;
		}

		if (grabCompRedundancy && tag.equalsIgnoreCase("redundancy")) {
			grabCompRedundancy = false;
		}

		if (grabCompRequired && tag.equalsIgnoreCase("required")) {
			grabCompRequired = false;
		}

		if (grabCascading && tag.equalsIgnoreCase("cascade-failures")) {
			grabCascading = false;
			grabComponent = true;
		}

		if (grabCompDemand && tag.equalsIgnoreCase("demand")) {
			grabCompDemand = false;
			grabComponent = true;
		}

		if (grabCompRequired && tag.equalsIgnoreCase("failure-rate")) {
			grabCompDemandRate = false;
		}

		if (grabCompRequired && tag.equalsIgnoreCase("repair")) {
			grabCompDemandRepair = false;
		}

	}

	public void characters(char a[], int b, int c) throws SAXException {
		String res = new String(a, b, c);

		if (grabDemandChangeRate) {
			demandMatrix[currentDemandChangeFrom][currentDemandChangeTo] = Double.parseDouble(res);
			grabDemandChangeRate = false;
		}

		if (grabCompType) {
			currentType = res;
			typeList.add(currentType);
			cascadingCache.put(currentType, new ArrayList<String>());
			grabCompType = false;
		}

		if (grabCompRedundancy) {
			currentRedundancy = Integer.parseInt(res);
			grabCompRedundancy = false;
		}

		if (grabCompRequired) {
			currentRequired = Integer.parseInt(res);
			grabCompRequired = false;
		}

		if (grabDemandLevels) {
			int t = Integer.parseInt(res);
			demandMatrix = new double[t][t];
			currentFailureRates = new double[t];
			currentRepairRates = new double[t];
			grabDemandLevels = false;
		}

		if (grabCompDemand && grabCompDemandRate && res.trim().length() > 0) {
			currentFailureRates[currentDemand] = Double.parseDouble(res);
			grabCompDemandRate = false;
		}

		if (grabCompDemand && grabCompDemandRepair && res.trim().length() > 0) {
			currentRepairRates[currentDemand] = Double.parseDouble(res);
			grabCompDemandRepair = false;
		}

		if (grabCascading && currentCascadingType != null && res.trim().length() > 0) {
			cascadingCache.get(currentType).add(currentCascadingType + ":" + res);
		}
	}

	public static double[][] getDemand() {
		return instance.demandMatrix;
	}

	public static HashMap<String, FailureNode> getNodeMap() {
		HashMap<String, FailureNode> temp = new HashMap<String, FailureNode>();
		for (String k : getInstance().typeList) {
			temp.put(k, instance.nodeCache.get(k));
		}
		return temp;
	}

	public static ArrayList<String> getTypeList() {
		return getInstance().typeList;
	}
}
