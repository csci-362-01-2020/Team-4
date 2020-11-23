package org.eclipse.stem.analysis.impl;

import org.eclipse.stem.analysis.impl.ReferenceScenarioDataMapImpl;

public class ReferenceScenarioDataMapTestingFactory {

	public ReferenceScenarioDataMapTestingFactory() {
	}
	
	public ReferenceScenarioDataMapImpl makeReferenceScenarioDataMap() {
		return new ReferenceScenarioDataMapImpl();
	}
	
	public boolean testCloseEnough(ReferenceScenarioDataMapImpl rsdm, double d1, double d2) {
		return rsdm.closeEnough(d1, d2);
	}
	

}
