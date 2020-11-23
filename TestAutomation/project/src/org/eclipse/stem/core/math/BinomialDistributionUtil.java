package org.eclipse.stem.core.math;


//import org.apache.commons.math.special.Beta;
import java.util.Random;

import org.apache.commons.math3.random.JDKRandomGenerator;


 
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

/**
 * Utility primarily used by stochastic models
 * This technique makes a random pick from a binomial distribution.
 * The complex part of this operation is to compute the binomial coefficient
 * efficiently (see for example: http://en.wikipedia.org/wiki/Binomial_distribution)
 * In order to do the computation for large N (large S) we compute the log of the binomial coefficient
 * so we do a sum (instead of a factorial product) and then exponentiate the result. 
 */
public class BinomialDistributionUtil {

	public static int MAX_N = 2000000000; // If n is larger than this we do not
	
	/**
	 * The random generator
	 */
	protected Random rand = new Random();

	/**
	 * Construct a new instance with a seed
	 * @param seed
	 */
	public BinomialDistributionUtil(long seed) {
		rand.setSeed(seed);
	}
	
	/**
	 * set the seed
	 * 
	 * @param seed The seed
	 */
	
	public void setSeed(long seed) {
		rand.setSeed(seed); // Will have the same effect as creating a new Random instance with the specified seed
	}
	
	 /**
	  * Returns the random pick from a binomial dist
	  * given 0<=rndVar<=1
	  * This sums the binomial distribution and returns the k value with that integrated probability
	  * @param p The probability,  usually (new incidence)/(Number Susceptible)
	  * @param n (The number in the compartment e.g. should be n= Math.round(total number susceptible))	   
	  * @param rndVal A random number from 0-1
	  * @return K Random pick
	  **/ 
	 public int fastPickFromBinomialDist(double p, int n){		 
		 int result = 0; // default is the deterministic prediction
		 if(p == 0){
		 p = p*100;
		  return result;} // zero probability means fast pick is zero. Fixes problem where -1 was returned for some reasone (bug?)
		 if(Double.isInfinite(p) || Double.isNaN(p)) return result;
		 
		 double rndVal = rand.nextDouble();
		 boolean success = false;
		 // this should run in constant time
		 if(n < MAX_N) {
			 // 9/5/13 Fix for CQ 7001
			org.apache.commons.math3.distribution.BinomialDistribution bdist = new org.apache.commons.math3.distribution.BinomialDistribution(new JDKRandomGenerator(), n,p);
			 //org.apache.commons.math3.distribution.BinomialDistribution bdist = new org.apache.commons.math3.distribution.BinomialDistribution(n,p);
			//BinomialDistributionImpl bdist = new BinomialDistributionImpl(n,p);
		 	result = bdist.inverseCumulativeProbability(rndVal);
		 	success = true;
		 }
		 if(!success) 
			 result = (int) Math.round((double)n*p); // default is the deterministic prediction
		 
		 return result;
	 }
	 

	 
	 
	 ///////////////////////////////////////////////
	 //
	 // This code is exact but runs in linear time N
	 //
	 ///////////////////////////////////////////////
//	 /**
//	  * Returns the random pick from a binomial dist
//	  * given 0<=rndVar<=1
//	  * This sums the binomial distribution and returns the k value with that integrated probability
//	  * @param p The probability
//	  * @param n (The number in the compartment e.g. n= total number susceptible)	   
//	  * @param rndVAR A random number from 0-1
//	  * @return K Random pick
//	  **/ 
//	 public static int fastPickFromBinomialDistOLD(double p, int n, double rndVAR){
//		 
//		 if(p == 0 || n == 0) return 0;
//		 double Bsum = 0.0;
//		 // compute this once
//		 double lnFactorialN = lnFactorial(n);
//		 double lnFactorialK = 0.0;
//		 double lnFactorialN_K = lnFactorialN;
//		 final double logP = Math.log(p);
//		 final double logOneMinusP = Math.log(1-p);
//		 int n_k = n;
//		 for(int k = 0; k <= n; k ++) {
//			
//			 if(k>=1) {
//				 // count up
//				 lnFactorialK += Math.log((double)k);
//			 
//				 // count down for n-k
//				 lnFactorialN_K -= Math.log((double)n_k);
//				 n_k -= 1;
//			 }
//			 // do the entire computation in log space
//			 double logB = lnFactorialN - lnFactorialK - lnFactorialN_K;
//			 // now instead of multiplying by { p^k * (1-p)^n-k } we add up
//			 // logB + k*log(p) + (n-k)*log(1-p);
//			 logB += (k*logP)  + ( (n-k) * logOneMinusP );
//			 // NOW we take the exponent
//			 Bsum += Math.exp(logB);
//			 
//			 if (Bsum >= rndVAR) return k;
//		 }
//		 return n;
//	 }
//	 
//	 
//	 /**
//	  * compute the log(n!)
//	  * @param n
//	  * @return log(n!)
//	  */
//	   static double lnFactorial(int n) {
//	      double retVal = 0.0;
//	
//	      for (int i = 1; i <= n; i++) {
//	    	  retVal += Math.log((double)i);
//	      }
//	       
//	      return retVal;
//	   }
	 
	 
	 
	 ///////////////////////////////////////////////
	 //
	 // TEST code comparing org.apache.commons.math.distribution.BinomialDistributionImpl.inverseCumulativeProbability()  with exact code
	 //
	 ///////////////////////////////////////////////
//	 /**
//	  * Returns the random pick from a binomial dist
//	  * given 0<=rndVar<=1
//	  * This sums the binomial distribution and returns the k value with that integrated probability
//	  * @param p The probability of getting infected 
//	  * @param n (The number in the compartment e.g. n= total number susceptible)	   
//	  * @param rndVAR A random number from 0-1
//	  * @return K Random pick
//	  **/ 
//	 public static void testBinomialDistBeta(double p, int n, double randVal){
//		 int k = 0;
//		 long t0 = 0;
//		 long t1 = 0;
//		 int result = 0;
//		 long  tApache2=0;
//		 try{
//			 t0 = System.currentTimeMillis();
//			 BinomialDistributionImpl bdist = new BinomialDistributionImpl(n,p);
//			 result = bdist.inverseCumulativeProbability(randVal);
//			 t1 = System.currentTimeMillis();
//			 tApache2 = t1-t0;
//			 System.out.println("appache DIST FOUND k="+result+" p="+p+", n="+n+", time="+tApache2 );
//		 }catch(Exception e) {
//			 System.out.println("problem with BinomialDistributionImpl"+e.getMessage());
//		 }
//		 
//		 
//		 /////////////////
//		 //
//		 // CUSTOM METHOD
//		 //
//		 double Bsum = 0.0;
//		 // compute this once
//		 double lnFactorialN = lnFactorial(n);
//		 double lnFactorialK = 0.0;
//		 double lnFactorialN_K = lnFactorialN;
//		 final double logP = Math.log(p);
//		 final double logOneMinusP = Math.log(1-p);
//		 int n_k = n;
//		 double test = 0;
//		 t0 = System.currentTimeMillis();
//		 //time custom
//		 k = 0;
//		 for(int kk = 0; kk <= n; kk ++) {
//			 
//			 if(kk>=1) {
//				 // count up
//				 lnFactorialK += Math.log((double)kk);
//			 
//				 // count down for n-k
//				 lnFactorialN_K -= Math.log((double)n_k);
//				 n_k -= 1;
//			 }
//			 // do the entire computation in log space
//			 double logB = lnFactorialN - lnFactorialK - lnFactorialN_K;
//			 // now instead of multiplying by { p^k * (1-p)^n-k } we add up
//			 // logB + k*log(p) + (n-k)*log(1-p);
//			 logB += (kk*logP)  + ( (n-kk) * logOneMinusP );
//			 // NOW we take the exponent
//			 Bsum += Math.exp(logB);
//			 if (Bsum >= randVal) {
//				 k = kk;
//				 break;
//			 }
//			 
//		 }
//		 
//		 t1 = System.currentTimeMillis();
//		 System.out.println("k="+k+" p="+p+", n="+n+", BSUM ="+Bsum);
//		 System.out.println("appache time = "+tApache2+" custom time = "+(t1-t0));
//	 }
//	 
//	
//	 
//	 public static void main(String [ ] args) {
//		 testBinomialDistBeta(0.15,100000000, .51);
//	 }
//	 
	   
	 
}
