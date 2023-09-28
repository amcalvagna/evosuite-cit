/**
 * New crossover operator implemented as part of the EvoSuite tutorial.
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

package org.evosuite.ga.operators.crossover;

import org.evosuite.ga.Chromosome;
import org.evosuite.ga.ConstructionFailedException;

public class MiddleCrossOver extends CrossOverFunction{

	@Override
	public void crossOver(Chromosome parent1, Chromosome parent2) throws ConstructionFailedException {
		// Is there more than one test case? If not, we cannot crossover.
		if(parent1.size() < 2 || parent2.size() < 2){
			return;
		}
		
		// If we have not returned, then there must be enough tests.
		// Now, find the middle of each parent.
		
		int middle1 = (int) (Math.round(parent1.size() / 2.0));
		int middle2 = (int) (Math.round(parent2.size() / 2.0));
		
		// Create the offspring as direct copies of parents.
		Chromosome t1 = parent1.clone();
		Chromosome t2 = parent2.clone();
		
		// Perform the crossover operation.
		parent1.crossOver(t2, middle1, middle2);
		parent2.crossOver(t1, middle2, middle1);
	}

}
