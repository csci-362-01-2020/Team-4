/*******************************************************************************
 * Copyright (c) 2009, 2010, 2011, 2012, 2013, 2014, 2015, 2016, 2017, 2018
 * IBM Corporation, BfR, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation and new features
 *     Bundesinstitut für Risikobewertung - Pajek Graph interface, new Veterinary Models
 *******************************************************************************/

package org.eclipse.stem.analysis.impl;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.impl.EObjectImpl;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.stem.analysis.Activator;
import org.eclipse.stem.analysis.AnalysisPackage;
import org.eclipse.stem.analysis.DiseaseType;
import org.eclipse.stem.analysis.ReferenceScenarioDataMap;
import org.eclipse.stem.analysis.States;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Reference Scenario Data Map</b></em>'.
 * <!-- end-user-doc -->
 *
 * @generated
 */
public class ReferenceScenarioDataMapImpl extends EObjectImpl implements ReferenceScenarioDataMap {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	protected ReferenceScenarioDataMapImpl() {
		super();
		referenceScenarioDataMap = new HashMap<String, ReferenceScenarioDataInstance>();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return AnalysisPackage.Literals.REFERENCE_SCENARIO_DATA_MAP;
	}

	public static final String ITERATION_KEY = "iteration";//$NON-NLS-1$
	/**
	 * key for incidence data
	 */
	public static final String INCIDENCE_KEY = "Incidence";//$NON-NLS-1$
	public static final String TIME_KEY = "time";//$NON-NLS-1$
	private double maxIncidence = 0.0;
	private String maxIncidenceLocation;
	private static final double PPM = 1.0/1000000.0;
	
	/**
	 * We have generalize the possible keys 
	 * so there can be more than one of each type but they
	 * are still, for now, of type SEIR. I might be Iactive or Ilatent though
	 */
	public Set<String> S_KEY_SET = new HashSet<String>();
	public Set<String> E_KEY_SET = new HashSet<String>();
	public Set<String> I_KEY_SET = new HashSet<String>();
	public Set<String> R_KEY_SET = new HashSet<String>();
	public Set<String> INCIDENCE_KEY_SET = new HashSet<String>();
	
	
	/**
	 * A single Reference Scenario is stored in a map
	 * for each  key<String> = Region id
	 */
	Map<String, ReferenceScenarioDataInstance> referenceScenarioDataMap = null;
	
	/**
	 * Directory used
	 */
	
	private String refDir;
	
	/**
	 * Population Identifier used for the log files
	 */
	
	private String populationIdentifier;
	
	public String getPopulationIdentifier() {
		return populationIdentifier;
	}

	public void setPopulationIdentifier(String populationIdentifier) {
		this.populationIdentifier = populationIdentifier;
	}

	/**
	 * Type of data (e.g. SIR)
	 */
	
	DiseaseType type;
	
	/**
	 * addInstance Add a new data instance (e.g. data from .csv file) to the map
	 * 
	 * @param key The key, e.g. city id
	 * @param data ReferenceScenarioDataInstance 
	 */
	public void addInstance(String key, ReferenceScenarioDataInstance data) {
		referenceScenarioDataMap.put(key, data);
	}
	
	/**
	 * 
	 */
	public void findMaxIncidence() {
		Iterator<String> iter = referenceScenarioDataMap.keySet().iterator();
		while((iter!=null)&&(iter.hasNext()) ) {
			String key = iter.next();
			ReferenceScenarioDataInstance data = referenceScenarioDataMap.get(key);
			double count = getTotalIncidence(data);
			if(count >= maxIncidence) {
				maxIncidence = count;
				maxIncidenceLocation = key;
			}
		}
	}
	
	/**
	 * Return the type of the data. Used to determine which parameter estimator(s)
	 * can be used.
	 * 
	 * @return ParameterEstimator.Type The type, e.g. SEIR 
	 */
	
	public DiseaseType getType() {
		if(referenceScenarioDataMap == null || referenceScenarioDataMap.size() == 0) {
			return null;
		}
		return this.type;
	}
	
	/**
	 * Set the type of the data
	 * 
	 * @param t New type 
	 */
	
	public void setType(DiseaseType t, Set<String> Skeys, Set<String> Ekeys, Set<String>Ikeys, Set<String> Rkeys, Set<String> IncKeys) {
		this.type = t;
		this.S_KEY_SET=Skeys;
		this.E_KEY_SET=Ekeys;
		this.I_KEY_SET=Ikeys;
		this.R_KEY_SET=Rkeys;
		this.INCIDENCE_KEY_SET=IncKeys;
	}
	
	/**
	 * get all Region Keys
	 * @return
	 */
	public String[] getRegionKeys() {
		Set<String> keySet = referenceScenarioDataMap.keySet();
		return keySet.toArray(new String[keySet.size()]);
	}
	
	/**
	 * get all model Label Keys
	 * @return
	 */
	public String[] getModelLabelKeys() {
		Set<String> keySet = referenceScenarioDataMap.keySet();
		String[] keys = keySet.toArray(new String[keySet.size()]);
		if((keys!=null)&&(keys.length>0)) {
			ReferenceScenarioDataInstance rsdi = referenceScenarioDataMap.get(keys[0]);
			Set<String> dataKeysSet = rsdi.instance.keySet();
			String[] dataKeys = dataKeysSet.toArray(new String[dataKeysSet.size()]);
			return dataKeys;
		}
	    return null;	
	}
	
	/**
	 * 
	 * @return
	 */
	public String[] getS_Keys() {
		String[] keys = new String[0];
		if(S_KEY_SET!=null) {
			keys = S_KEY_SET.toArray(new String[S_KEY_SET.size()]);
		}
		return keys;
	}
	
	/**
	 * 
	 * @return
	 */
	public String[] getE_Keys() {
		String[] keys = new String[0];
		if(E_KEY_SET!=null) {
			keys = E_KEY_SET.toArray(new String[E_KEY_SET.size()]);
		}
		return keys;
	}
	
	/**
	 * 
	 * @return
	 */
	public String[] getI_Keys() {
		String[] keys = new String[0];
		if(I_KEY_SET!=null) {
			keys = I_KEY_SET.toArray(new String[I_KEY_SET.size()]);
		}
		return keys;
	}
	
	/**
	 * 
	 * @return
	 */
	public String[] getR_Keys() {
		String[] keys = new String[0];
		if(R_KEY_SET!=null) {
			keys = R_KEY_SET.toArray(new String[R_KEY_SET.size()]);
		}
		return keys;
	}

	public String[] getInc_KEY_SET() {
		String[] keys = new String[0];
		if(INCIDENCE_KEY_SET!=null) {
			keys = INCIDENCE_KEY_SET.toArray(new String[INCIDENCE_KEY_SET.size()]);
		}
		return keys;
	}

	
	/**
	 * getNumLocations. Return the number of locations analyzed
	 * 
	 * @return int Number of locations
	 */
	
	public int getNumLocations() {
		return referenceScenarioDataMap.size();
	}
	
	/**
	 * getLocations. Return the locations
	 * 
	 * @return Set<String> Set of all locations
	 */
	
	public Set<String> getLocations() {
		return referenceScenarioDataMap.keySet();
	}
	
	
	/**
	 * The the total incidence (summing over all time for some location instance 
	 * @param instance
	 * @return the total Incidence count
	 */
	public double getTotalIncidence(ReferenceScenarioDataInstance instance) {
		double retValue = 0.0;
		if(instance.getData().containsKey(INCIDENCE_KEY)) {
			List<Double> incidence = instance.getData().get(INCIDENCE_KEY);
			for (int i = 0; i < incidence.size(); i ++) {
				retValue += incidence.get(i).doubleValue();
			}
		}
		return retValue;
	}
	
	/**
	 * containsLocation Return true if the map contains the specified location
	 * 
	 * @param loc
	 * @return boolean True if the location is found in the map
	 */
	public boolean containsLocation(String loc) {
		return referenceScenarioDataMap.containsKey(loc);
	}
	
	/**
	 * getLocation. Return the data for the given location
	 * @param loc 
	 * @return ReferenceScenarioDataInstance Location instance, or null if not found
	 */
	
	public ReferenceScenarioDataInstance getLocation(String loc) {
		return referenceScenarioDataMap.get(loc);
	}
	
	/**
	 * Aggregate or integrate all data for all locations in a scenario.
	 * 
	 * @param runnableContext Runnable context for progress indicator
	 * @return ReferenceScenarioDataInstance aggregated data instance, or null if not found
	 */
	
	public ReferenceScenarioDataInstance aggregateScenarioData(IRunnableContext runnableContext) {
		final ReferenceScenarioDataInstance aggregatedData = new ReferenceScenarioDataInstance(this);
		
		IRunnableWithProgress aggregateTask = new IRunnableWithProgress() {
            public void run(IProgressMonitor progress) {
            	progress.beginTask("Aggregating data...", referenceScenarioDataMap.size());   	//$NON-NLS-1$
            	Iterator<String> iter = referenceScenarioDataMap.keySet().iterator();
            	while((iter!=null)&&(iter.hasNext())) {
            		progress.worked(1);
            		if(progress.isCanceled()) throw new OperationCanceledException();
            		String loc = iter.next();
			
            		ReferenceScenarioDataInstance locationData = referenceScenarioDataMap.get(loc);
            		aggregatedData.integrateData(locationData.instance);
            	}// all locations
            	progress.done();
            }
		};
		
		try {
			runnableContext.run(true, true, aggregateTask);
		} catch(InterruptedException ie) {
			// ToDo
			return null;
		} catch(InvocationTargetException ite) {
			// ToDo
			return null;
		}
		return aggregatedData;
	}
	
	/**
	 * getReferenceDirectory. Return the reference directory used
	 * 
	 * @return String Reference directory
	 */
	
	public String getReferenceDirectory() {
		return this.refDir;
	}
	
	/**
	 * setReferenceDirectoy. Set the reference directory
	 * 
	 * @param dir The directory
	 */
	
	public void setReferenceDirectory(String d) {
		this.refDir = d;
	}
	
	
	/**
	 * 
	 * @return the max incidence at the most effected location 
	 */
	public double getMaxIncidence() {
		return maxIncidence;
	}

	/**
	 * 
	 * @return the location with the max incidence
	 */
	public String getMaxIncidenceLocation() {
		return maxIncidenceLocation;
	}
	
	
	/**
	 * ReferenceScenarioDataInstance. Contains data for one location  
	
	 */
	public class ReferenceScenarioDataInstance implements Cloneable {
		
		private double maxS = 0.0;
		private double maxE = 0.0;
		private double maxI = 0.0;
		private double maxR = 0.0;
			
		/**
		 * The region data map is keyed by property (S,E, I, R, incidence, etc) and contains Data (mostly Doubles but
		 * STEMTime is stored as a String) so all data is stored as String.
		 */
		
		public Map<String,List<String>> instance;
		
		

		protected ReferenceScenarioDataMapImpl dataMap;
		
		public ReferenceScenarioDataMapImpl getDataMap() {
			return dataMap;
		}

		/**
		 * Create a new instance
		 *  
		 * @param data Map of parameter (e.g. a state) and its values
		 * @param map DataMap the instance belongs to
		 */
		public ReferenceScenarioDataInstance(Map<String, List<String>> data, ReferenceScenarioDataMapImpl map) {
			this.instance = data;
			this.dataMap = map;
		}
		
		/**
		 * Create an empty instance of some type
		 *  
		 * @param dataMap map
		 */
		public ReferenceScenarioDataInstance(ReferenceScenarioDataMapImpl dataMap) {
			this.instance = new HashMap<String, List<String>>();
			this.dataMap = dataMap;
		}
		
		/**
		 * deep clone
		 * @see java.lang.Object#clone()
		 */
		@Override
		public  ReferenceScenarioDataInstance clone() {
			this.getData();
			ReferenceScenarioDataInstance newInstance = new ReferenceScenarioDataInstance(this.dataMap);
			Iterator<String> iter = this.instance.keySet().iterator();
			while(iter.hasNext()) {
				String key = iter.next();
				List<String> dataList = instance.get(key);
				List<String> newList = new ArrayList<String>();
			
				for (int i = 0; i < dataList.size(); i ++) {
					String val = dataList.get(i);
					newList.add(val);
				}
				newInstance.instance.put(key,newList);
			}
			newInstance.getData();
			return newInstance;
		}
		
		/**
		 * integrates data from another instance
		 * @param data
		 */
		public void integrateData(Map<String, List<String>> data) {
			// get the state variables SEIR... as keys
			for (Entry<String,List<String>> entry : data.entrySet()) {
				// get the key
				String state = entry.getKey();
				List<String> valueList = entry.getValue();
				if(valueList.size()>=1) {
					// get the current list
					List<String> aggregatedList = new ArrayList<String>();
					if(this.instance.containsKey(state)) {
						aggregatedList = this.instance.get(state);
					} else {
						// create it if it doesn't exist
						for(int i = 0; i < valueList.size(); i ++) {
							aggregatedList.add("0.0");//$NON-NLS-1$
						}
						this.instance.put(state,aggregatedList);
					}
					
					
					
					String first = valueList.get(0);
					// check if this is numeric data - it might just be a date
					boolean isData = true;
					try {
						@SuppressWarnings("unused")
						double val = (new Double(first)).doubleValue();
					} catch (NumberFormatException nfe) {
						isData = false;
					}
					for (int i = 0; i < valueList.size(); i ++) {
						String value = valueList.get(i);
						if(isData) {
							double val = (new Double(value)).doubleValue();
							double oldVal = (new Double(aggregatedList.get(i))).doubleValue();
							if(state.indexOf(ITERATION_KEY)==-1) {
								// add them up except for the iteration counter column
								oldVal += val;
							} else {
								oldVal = val;
							}
							aggregatedList.set(i,""+oldVal);//$NON-NLS-1$
							this.instance.put(state,aggregatedList);
						} else {
							// just copy the strings
							aggregatedList.set(i,value);
							this.instance.put(state,aggregatedList);
						}
					}
					
					
				}
				
				
			}
			
			
		}
		
		
		/**
		 * getData. Return data for the given parameter (e.g. state)
		 * 
		 * @param parm Parameter
		 * @return List<String> data
		 */
		
		public List<String> getData(String parm) {
			return instance.get(parm);
		}
		
		/**
		 * containsParameter. Return true if the data contains the given parameter (e.g. a state)
		 * 
		 * @param p Parameter to check
		 * @return boolean true if found, false otherwise
		 */
		
		public boolean containsParameter(String p) {
			return this.instance.containsKey(p);
		}
		
		/**
		 * get the all the data
		 * @return this instance map
		 */
		public Map<String, List<String>> getInstance() {
			return instance;
		}
		
		/**
		 * find the maximum values for every type of state
		 * grouping  states by type (SEIR)
		 */
		public void findMaxValues() {
			
					
					String[] S_keys = this.dataMap.getS_Keys();
					String[] E_keys = this.dataMap.getE_Keys();
					String[] I_keys = this.dataMap.getI_Keys();
					String[] R_keys = this.dataMap.getR_Keys();
					
					// S states
					if(this.dataMap.getS_Keys() != null) {
						for (int i =0; i < S_keys.length; i ++) {
							List<String> dataList = this.getData(S_keys[i]);
							if(dataList != null) {
								maxS = new Double(dataList.get(0)).doubleValue();
		            			for(int j = 0; j < dataList.size(); j ++) {
		            				double val = new Double(dataList.get(j)).doubleValue();
		            				if (val > maxS) {
		            					maxS = val;
		            				}
		            			}
							}
						}
					}
					
					// If E state
					if(this.dataMap.getE_Keys() != null) {
						for (int i =0; i < E_keys.length; i ++) {
							List<String> dataList = this.getData(E_keys[i]);
							if(dataList != null) {
								maxE = new Double(dataList.get(0)).doubleValue();
		            			for(int j = 0; j < dataList.size(); j ++) {
		            				double val = new Double(dataList.get(j)).doubleValue();
		            				if (val > maxE) {
		            					maxE = val;
		            				}
		            			}
							}
						}
					}

					
        			
        			// If have I state 
					if(this.dataMap.getI_Keys() != null) {
						for (int i =0; i < I_keys.length; i ++) {
							List<String> dataList = this.getData(I_keys[i]);
							if(dataList != null) {
								maxI = new Double(dataList.get(0)).doubleValue();
		            			for(int j = 0; j < dataList.size(); j ++) {
		            				double val = new Double(dataList.get(j)).doubleValue();
		            				if (val > maxI) {
		            					maxI = val;
		            				}
		            			}
							}
						}
					}
					

        			
        			// If have R state 
					if(this.dataMap.getR_Keys() != null) {
						for (int i =0; i < R_keys.length; i ++) {
							List<String> dataList = this.getData(R_keys[i]);
							if(dataList != null) {
								maxR = new Double(dataList.get(0)).doubleValue();
		            			for(int j = 0; j < dataList.size(); j ++) {
		            				double val = new Double(dataList.get(j)).doubleValue();
		            				if (val > maxR) {
		            					maxR = val;
		            				}
		            			}
							}
						}
					}
					
					   
        			
        			
		} // findMaxValues
		
		
		
		/**
		 * getData. Return the map that of raw data (not filtered)
		 * @return  Map<String, List<Double>> Map with state, value list
		 */
	
		public Map<String, List<Double>> getData() {
			
			HashMap<String, List<Double>> data = new HashMap<String, List<Double>>();
			
			for(String state : instance.keySet()) {
				ArrayList<Double> list = new ArrayList<Double>();
				if(state.equalsIgnoreCase("time"))continue;//$NON-NLS-1$
				for(String sd : instance.get(state)) 
					list.add(Double.parseDouble(sd));
				data.put(state, list);
			}
			return data;
		}
		
		/**
		 * Return the totals of type Susceptible (there could be more than one, eg
		 * by age or immune compromised state)
		 * @return  
		 */
		
		public List<Double> getStotals() {
			String[] keys = this.getDataMap().getS_Keys();
			List<Double> data = new ArrayList<Double>();
			if(keys.length == 0) return data;
			List<String> strList = instance.get(keys[0]);
			
			if(strList !=null) {
				for(int j = 0; j < strList.size(); j ++) {
					data.add(j, new Double(strList.get(j)).doubleValue());
				}
			}
			if(keys.length >=2) {
				// if we have more than one I compartment we sum them up
				for(int i = 1; i < keys.length; i ++) {
					strList = instance.get(keys[i]);
					for(int j = 0; j < strList.size(); j ++) {
	    				double val = new Double(strList.get(j)).doubleValue();
	    				val += data.get(j).doubleValue();
	    				data.set(j,new Double(val));
					}
				}
			}
			return data;
		}// getStotals
		
		/**
		 * Return the totals of type E (there could be more than one, eg
		 * by age or immune compromised state)
		 * @return  
		 */
		
		public List<Double> getEtotals() {
			String[] keys = this.getDataMap().getE_Keys();
			List<Double> data = new ArrayList<Double>();
			if(keys.length == 0) return data; // empty
			List<String> strList = instance.get(keys[0]);
			if(strList !=null) {
				for(int j = 0; j < strList.size(); j ++) {
					data.add(j, new Double(strList.get(j)).doubleValue());
				}
			}
			if(keys.length >=2) {
				// if we have more than one I compartment we sum them up
				for(int i = 1; i < keys.length; i ++) {
					strList = instance.get(keys[i]);
					for(int j = 0; j < strList.size(); j ++) {
	    				double val = new Double(strList.get(j)).doubleValue();
	    				val += data.get(j).doubleValue();
	    				data.set(j,new Double(val));
					}
				}
			}
			return data;
		}// getEtotals
		
		/**
		 * Return the totals of type I (there could be more than one, eg
		 * by age or immune compromised state)
		 * @return  
		 */
		
		public List<Double> getItotals() {
			String[] keys = this.getDataMap().getI_Keys();
			List<Double> data = new ArrayList<Double>();
			if(keys.length == 0) return data;
			List<String> strList = instance.get(keys[0]);
			
			if(strList !=null) {
				for(int j = 0; j < strList.size(); j ++) {
					data.add(j, new Double(strList.get(j)).doubleValue());
				}
			}
			if(keys.length >=2) {
				// if we have more than one I compartment we sum them up
				for(int i = 1; i < keys.length; i ++) {
					strList = instance.get(keys[i]);
					for(int j = 0; j < strList.size(); j ++) {
	    				double val = new Double(strList.get(j)).doubleValue();
	    				val += data.get(j).doubleValue();
	    				data.set(j,new Double(val));
					}
				}
			}
			return data;
		}// getItotals
		
		/**
		 * Return the totals of type I (there could be more than one, eg
		 * by age or immune compromised state)
		 * @return  
		 */
		
		public List<Double> getRtotals() {
			String[] keys = this.getDataMap().getR_Keys();
			List<Double> data = new ArrayList<Double>();
			if(keys.length == 0) return data;
			List<String> strList = instance.get(keys[0]);
			
			if(strList !=null) {
				for(int j = 0; j < strList.size(); j ++) {
					data.add(j, new Double(strList.get(j)).doubleValue());
				}
			}
			if(keys.length >=2) {
				// if we have more than one I compartment we sum them up
				for(int i = 1; i < keys.length; i ++) {
					strList = instance.get(keys[i]);
					for(int j = 0; j < strList.size(); j ++) {
	    				double val = new Double(strList.get(j)).doubleValue();
	    				val += data.get(j).doubleValue();
	    				data.set(j,new Double(val));
					}
				}
			}
			return data;
		}// getRtotals
		
		
		
		
		/**
		 * 
		 * @return maxS
		 */
		public double getMaxS() {
			return maxS;
		}

		/**
		 * 
		 * @return maxE
		 */
		public double getMaxE() {
			return maxE;
		}

		/**
		 * 
		 * @return maxI
		 */
		public double getMaxI() {
			return maxI;
		}

		/**
		 * 
		 * @return maxR
		 */
		public double getMaxR() {
			return maxR;
		}
		
		/**
		 * getSize. Return the number of data rows in the instance. Each array (value) is assumed same size
		 * 
		 * @return int The size of the 
		 */
		
		public int getSize() {
			if(this.instance.size() == 0) return 0;
			return this.instance.values().iterator().next().size();
		}
	}


	/**
	 * TODO implement this method
	 * This method insures that the output of a disease model (or the output of a population model) is sane!
	 * @return
	 */
	public boolean sane() {
		Activator.logInformation("TODO   ReferenceScenarioDataMapImpl.sane() must be fully implemented !!!"); //$NON-NLS-1$
		
		Iterator<String> locationIter = getLocations().iterator();
		while(locationIter!=null && locationIter.hasNext()) {
			String locationID = locationIter.next();
			ReferenceScenarioDataInstance instance = getLocation(locationID);
			// Get the data. Always have at least S and I
			List<String>popData = instance.getData(States.statesToFit[States.POPULATION]);
			if(popData == null) popData =  instance.getData("Population Count");//$NON-NLS-1$
			
			List<String>sData = instance.getData(States.statesToFit[States.SUSCEPTIBLE]);
			List<String>eData = null;
			List<String>iData = instance.getData(States.statesToFit[States.INFECTIOUS]);
			List<String>rData = null;
			List<String>ddData = instance.getData(States.statesToFit[States.DISEASE_DEATHS]);
			
			// If E state
			if(instance.dataMap.getType()== DiseaseType.SEIR)  {
				eData = instance.getData(States.statesToFit[States.EXPOSED]);
			}
			// If have R state (i.e., not an SI model
			if(instance.dataMap.getType()!= DiseaseType.SI)  {
				rData = instance.getData(States.statesToFit[States.RECOVERED]);
			}
			
			// check that three things are true at every time step.
			// 0. All existing lists have the same length
			// 1. No data label should ever go negative
			// 2. The sum of the disease states = the total population at that time.
			int length = popData.size();
			if((sData.size() != length)||(iData.size() != length)||(ddData.size() != length)) return false;
			if((eData != null)&&(eData.size() != length)) return false;
			if((rData != null)&&(rData.size() != length)) return false;
			
			for (int t = 0; t < length; t ++) {
				double sum = 0.0;
				double s = new Double(sData.get(t)).doubleValue();
				double i = new Double(iData.get(t)).doubleValue();
				double dd = new Double(ddData.get(t)).doubleValue();
				double r = 0.0;
				double e = 0.0;
				if(eData != null) e = new Double(eData.get(t)).doubleValue();
				if(rData != null) r = new Double(rData.get(t)).doubleValue();
				if((s < 0)||(i<0)||(e<0)||(r<0)||(dd<0)) {
					System.err.println("Negative value found at time = "+t+" s="+s+" e="+e+" i="+i+" r="+r+"dd= "+dd);
					return false; //1
				}
				sum = s+e+i+r;
				if(!closeEnough(sum,new Double(popData.get(t)).doubleValue())) {
					System.err.println("Sum not correct at time = "+t+" difference is "+(sum-new Double(popData.get(t)).doubleValue()));	
					return false; // 2
				}
				// or exact equality but we need to check across different processors
//				if(sum != new Double(popData.get(t)).doubleValue()) {
//					Activator.logInformation("Sum not exactly correct at time = "+t+" difference is "+(sum-new Double(popData.get(t)).doubleValue()));
					
//				}
			}
			
		}
		return true;
	}
	
	/**
	 * Are two values within 1ppm ?
	 * @param dbl1
	 * @param dbl2
	 * @return
	 */
	boolean closeEnough(double d1, double d2) {
		boolean retVal = true;
		if(d1 == 0.0) return (Math.abs(d2) < PPM);
		if(d2 == 0.0) return (Math.abs(d1) < PPM);
		double diff = (Math.abs(2.0*(d1-d2)/(d1+d2)));
		if(diff > PPM) retVal = false;
		return retVal;
	}
	
	/**
	 * Is this map consistent with another map?
	 * This is like "equal" but allows for small differences when comparing double(s)
	 * @param other
	 * @return
	 */
	public boolean consistentWith(ReferenceScenarioDataMapImpl other) {
		Activator.logInformation("TODO   ReferenceScenarioDataMapImpl.consistentWith(ReferenceScenarioDataMap other) must be fully implemented !!!");//$NON-NLS-1$
		
		// Make sure that both maps contain the same locations
		if(getLocations().size() !=  other.getLocations().size())
		{
			Activator.logInformation("Error mismatched number of locations in log files for "+other.getReferenceDirectory()+" vs "+getReferenceDirectory());
			return false;
		}
		for(String loc:getLocations())
			if(other.getLocation(loc) == null) {
				Activator.logInformation("Location missing in data map "+other.getReferenceDirectory());
				return false;
			}
		
		// Now check each location and each state
		for(String loc:getLocations()) {
			ReferenceScenarioDataInstance otherInst = other.getLocation(loc);
			ReferenceScenarioDataInstance thisInst = getLocation(loc);
			
			for(String state:thisInst.getInstance().keySet()) {
				List<String>thisData = thisInst.getInstance().get(state);
				List<String>otherData = otherInst.getInstance().get(state);
				if(thisData.size() != otherData.size()) {
					System.err.println("Error the list of data values does not have the same length for location "+loc+" folder "+getReferenceDirectory());
					return false;
				}
				
				for(int i=0;i<thisData.size();++i) {
					double thisD = Double.parseDouble(thisData.get(i));
					double otherD = Double.parseDouble(otherData.get(i));
					
					if(!closeEnough(thisD, otherD)) {
						System.err.println("Error mismatched result for state "+state+" location "+loc+" "+thisD+" not same as "+otherD+" timestep "+i+" folder "+getReferenceDirectory()+" vs "+other.getReferenceDirectory());
						return false;
					}
				}
					
			}
		}
			
		return true;
	}
	

} //ReferenceScenarioDataMapImpl
