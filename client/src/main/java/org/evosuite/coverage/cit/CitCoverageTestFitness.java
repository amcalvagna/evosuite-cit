/**
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
package org.evosuite.coverage.cit;

//import static org.evosuite.coverage.io.IOCoverageConstants.*;

//import org.evosuite.coverage.io.output.OutputCoverageGoal;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.execution.ExecutionResult;
//import org.evosuite.testcase.statements.ConstructorStatement;
//import org.evosuite.testcase.statements.EntityWithParametersStatement;
//import org.evosuite.testcase.statements.MethodStatement;
//import org.evosuite.utils.generic.GenericConstructor;
//import org.evosuite.utils.generic.GenericMethod;
//import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import java.lang.reflect.Array;
import java.util.*;
//import java.util.Map.Entry;
/**
 * @author Jose Miguel Rojas, Andrea Calvagna
 */
public class CitCoverageTestFitness extends TestFitnessFunction {

    private static final long serialVersionUID = 6630097528288524492L;
	protected static final Logger logger = LoggerFactory.getLogger(CitCoverageTestFitness.class);

    /**
     * Target goal: a method parameter tuple to be covered by test
     */
    private final CitCoverageGoal goal;

    /**
     * Constructor - fitness is specific to a method
     *
     * @param goal the coverage goal
     * @throws IllegalArgumentException
     */
    public CitCoverageTestFitness(CitCoverageGoal goal) throws IllegalArgumentException {
        if (goal == null) {
            throw new IllegalArgumentException("goal cannot be null");
        }
        this.goal = goal;
    }




 /*    public boolean isCovered(ExecutionResult result) {

        for(Set<CitCoverageGoal> tuples : result.getCittuples().values()) 
        for(CitCoverageGoal g: tuples)
        if(g.equals(this.goal)) {        
            logger.info("cit goal isCovered");
            return true;            
        } 
        logger.info("cit goal not Covered!");
        return false;
    }
 */



    /**
     * <p>
     * getClassName
     * </p>
     *
     * @return a {@link String} object.
     */
    public String getClassName() {
        return goal.getClassName();
    }

    /**
     * <p>
     * getMethod
     * </p>
     *
     * @return a {@link String} object.
     */
    public String getMethod() {
        return goal.getMethodName();
    }

    /**
     * <p>
     * getValue
     * </p>
     *
     * @return a {@link String} object.
     */
    public Set<Assignment> getTuple() {
        return goal.getTuple();  
    }
   
    /**
     * <p>
     * getValue
     * </p>
     *
     * @return a {@link String} object.
     */
    public String getDescriptor() {
        return "coverage of "+goal.toString();
    }
   
    /**
     * {@inheritDoc}
     * <p/>
     * Calculate fitness
     *
     * @param individual a {@link org.evosuite.testcase.ExecutableChromosome} object.
     * @param result     a {@link ExecutionResult} object.
     * @return a double.
     */
    @Override
    public double getFitness(TestChromosome individual, ExecutionResult result) {
        double fitness = 1.0;
        //boolean wholeTuple=false;
        //look for whole tuples
        for(Set<CitCoverageGoal> tuples : result.getCitGoals().values()) //Actual tuples in the test
        if(tuples.contains(goal)) {
            //wholeTuple=true;
            fitness=0.0; 
            break;
        } 
        /** 
         * look for incomplete tuple.
         * compute fitness besed on most complete subtuple found in the test
         * by counting the number of matching assignments
         * with respct to those in this fitness goal
         **/
 
       /*  Commented since it did not made any difference in practice on the final test suite size outcome
       
        if (!wholeTuple) {
            double best=0, max = (double) goal.getTuple().size(); //number of assignments in the goal tuple
            //logger.debug("weight for a single matching assignment is :",1.0/CIT_DEGREE);
            
            //for each parametrized statement in the test
            for(int key : result.getCitGoals().keySet()) {

                Set<CitCoverageGoal> goals = result.getCitGoals().get(key); //tuples in one statement
                if(goals==null) continue; // is it possible?
                if(goals.size()==0) continue; // is it possible?
                CitCoverageGoal[] tupleList = goals.toArray(new CitCoverageGoal[goals.size()]); 
                if (!tupleList[0].getClassName().equals(goal.getClassName())) continue; //wrong class for this goal
                if (!tupleList[0].getMethodName().equals(goal.getMethodName())) continue; // wrong method for this goal

                //create a set of non duplicated assignements with all tuples covered by current statement 
                Set<Assignment> assignments = new HashSet<>(); 
                for (CitCoverageGoal tuple : tupleList)  
                    for(Assignment ass: tuple.getTuple()) assignments.add(ass);
                
                int score = 0; //count goal assignments covered by current statement 
                for (Assignment ass: goal.getTuple()) if (assignments.contains(ass)) score++;  
                best = score > best ? score : best ; //update best score 
                    
            }
            // complement best ratio of sparse (incomplete) assignments coverage    
            fitness = 1.0-best/max;                
        }
         */
        

        /** 
         * TODO: 
         *  aggiungere mezzi punti per coverage parziali del goal
         */
        updateIndividual(this, individual, fitness);
        logger.info("fitness of considered test result with respect to goal {} is: {}", goal.toString(), fitness);
        return fitness;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "[CIT]: "+goal.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int iConst = 13;
        return 51 * iConst + goal.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CitCoverageTestFitness other = (CitCoverageTestFitness) obj;
        logger.debug("into equals for {} vs {}", this.toString(), other.toString());
        return this.goal.equals(other.goal);
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.TestFitnessFunction#compareTo(org.evosuite.testcase.TestFitnessFunction)
     */
    @Override
    public int compareTo(TestFitnessFunction other) {
        if (other instanceof CitCoverageTestFitness) {
            CitCoverageTestFitness otherInputFitness = (CitCoverageTestFitness) other;
            return goal.compareTo(otherInputFitness.goal);
        }
        return compareClassName(other);
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.TestFitnessFunction#getTargetClass()
     */
    @Override
    public String getTargetClass() {
        return getClassName();
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.TestFitnessFunction#getTargetMethod()
     */
    @Override
    public String getTargetMethod() {
        return getMethod();
    }

}