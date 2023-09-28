/**
 * New fitness function, based on pairs of method calls. 
 * Part of EvoSuite tutorial.
 * 
 * @author Gregory Gay
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */

package org.evosuite.coverage.methodpair;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.evosuite.testcase.ExecutableChromosome;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testsuite.AbstractTestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;

public class MethodPairSuiteFitness extends TestSuiteFitnessFunction {

	// Track the method pairs.
	private final Set<MethodPairTestFitness> allMethodPairs = new HashSet<>();
	
	public MethodPairSuiteFitness(){
		// Get method pairs from the fitness factory.
		allMethodPairs.addAll(new MethodPairCoverageFactory().getCoverageGoals());
	}
	
	@Override
	public double getFitness(AbstractTestSuiteChromosome<? extends ExecutableChromosome> individual) {
		double fitness = 0.0;
		
		// Run all tests and gather the execution results.
		List<ExecutionResult> results = runTestSuite(individual);
		Set<MethodPairTestFitness> coveredMethodPairs = new HashSet<>();
		
		// Go through and look for covered goals.
		for(MethodPairTestFitness goal: allMethodPairs){
			for(ExecutionResult result: results){
				if(goal.isCovered(result)){
					coveredMethodPairs.add(goal);
					break;
				}
			}
		}
		
		// Fitness is the total number of goals - the number of covered goals.
		// If all goals are covered, fitness will be 0.
		fitness = allMethodPairs.size() - coveredMethodPairs.size();
		
		// Penalize fitness if the test suite times out.
		for (ExecutionResult result : results) {
			if (result.hasTimeout() || result.hasTestException()) {
				fitness = allMethodPairs.size();
				break;
			}
		}
		
		// Update the fitness score for the suite.
		updateIndividual(this, individual, fitness);
		individual.setNumOfCoveredGoals(this, coveredMethodPairs.size());
		if(!allMethodPairs.isEmpty()){
			individual.setCoverage(this, (double) coveredMethodPairs.size() / (double) allMethodPairs.size());
		}else{
			individual.setCoverage(this, 1.0);
		}
		return fitness;
	}

}
