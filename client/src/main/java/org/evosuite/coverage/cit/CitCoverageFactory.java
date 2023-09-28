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


import static org.evosuite.coverage.io.IOCoverageConstants.*;
import static org.evosuite.coverage.io.IOCoverageConstants.REF_NONNULL;
import static org.evosuite.coverage.io.IOCoverageConstants.STRING_NONEMPTY;
import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.evosuite.coverage.MethodNameMatcher;
import org.evosuite.graphs.cfg.BytecodeInstructionPool;
import org.evosuite.setup.TestClusterUtils;
import org.evosuite.setup.TestUsageChecker;
import org.evosuite.testsuite.AbstractFitnessFactory;
//import org.evosuite.utils.LoggingUtils;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @author Jose Miguel Rojas, Andrea Calvagna
 */

public class CitCoverageFactory extends AbstractFitnessFactory<CitCoverageTestFitness> {

    private static final Logger logger = LoggerFactory.getLogger(CitCoverageFactory.class);

    // private static final InputCoverageFactory inputFactory = new InputCoverageFactory();
    private static final List<CitCoverageTestFitness> goals = new ArrayList<CitCoverageTestFitness>();
   
    Class<?>[] argumentClasses;
    /*
     * (non-Javadoc)
     * 
     * @see
     * org.evosuite.coverage.TestCoverageFactory#getCoverageGoals()
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CitCoverageTestFitness> getCoverageGoals() {   //return goals to be covered by tests, as a list of testFitnessFunctions
        //don't recompute if already computed 
        if (!goals.isEmpty()) return goals; //fitnessGoals

        long start = System.currentTimeMillis();
        String targetClass = Properties.TARGET_CLASS;
        //String descriptor; 
        //LoggingUtils.getEvoLogger().warn("* logger ciao\n!");
        //logger.info("** logger ciao\n!");

        final MethodNameMatcher matcher = new MethodNameMatcher();
        for (String className : BytecodeInstructionPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).knownClasses()) {
            if (!(targetClass.equals("") || className.endsWith(targetClass)))
                continue;

            for (Method method : TestClusterUtils.getClass(className).getDeclaredMethods()) { //per ogni metodo 
                String methodName = method.getName() + Type.getMethodDescriptor(method);
                if (!TestUsageChecker.canUse(method) || !matcher.methodMatches(methodName))
                    continue;
                logger.trace("** Adding input goals for method {}.{}", className, method.getName());
                Type[] argumentTypes = Type.getArgumentTypes(method);
                argumentClasses = method.getParameterTypes();
                List<Assignment> assignments = new ArrayList<Assignment>();
                
                for (int i=0; i<argumentTypes.length;i++){ //calcolo gli input goals
                    Type argType = argumentTypes[i];
                    assignments.addAll(createInputAssignments(i, argType)); // add to assignments goal list for this argument
                }
                logger.trace("found {} input assignments for method {}", assignments.size(), method.getName());
                
                //create Cit tuples, distinct pairs in this version 
                for (Assignment a1 : assignments)
                for (Assignment a2 : assignments)
                if (a1.index < a2.index) {
                    Set<Assignment> tuple = new HashSet<>();
                    tuple.add(a1); tuple.add(a2);  //put a pair in the set
                    goals.add(new CitCoverageTestFitness( new CitCoverageGoal(className, method.getName(), tuple)));
                    logger.trace("added {} cit tuples to be covered for method:{}", a1.toString()+a2.toString(), method.getName());     
                }            
                //prepare for next method iteration
                //assignments.clear();
                //methodCitGoals.clear();
            }            
        }
        goalComputationTime=System.currentTimeMillis()-start; 
        logger.info("computed {} cit goals overall", goals.size()); 
        return goals;
    }

    //compute abstract assignments to be covered for the i-th parameter based on its type 
    public List<Assignment> createInputAssignments(int i, Type argType) {
        List<Assignment> assignments = new ArrayList<Assignment>();

        switch (argType.getSort()) {
            case Type.BOOLEAN:
                assignments.add(new Assignment(i, BOOL_TRUE));
                assignments.add(new Assignment(i, BOOL_FALSE));
                break;
            case Type.CHAR:
                assignments.add(new Assignment(i, CHAR_ALPHA));
                assignments.add(new Assignment(i, CHAR_DIGIT));
                assignments.add(new Assignment(i, CHAR_OTHER));
                break;
            case Type.BYTE:
            case Type.SHORT:
            case Type.INT:
            case Type.FLOAT:
            case Type.LONG:
            case Type.DOUBLE:
                assignments.add(new Assignment(i, NUM_NEGATIVE));
                assignments.add(new Assignment(i, NUM_ZERO));
                assignments.add(new Assignment(i, NUM_POSITIVE));
                break;
            case Type.ARRAY:
                assignments.add(new Assignment(i, REF_NULL));
                assignments.add(new Assignment(i, ARRAY_EMPTY));
                assignments.add(new Assignment(i, ARRAY_NONEMPTY));
                break;
            case Type.OBJECT:
                assignments.add(new Assignment(i, REF_NULL));
                if (argType.getClassName().equals("java.lang.String")) {
                    assignments.add(new Assignment(i, STRING_EMPTY));
                    assignments.add(new Assignment(i, STRING_NONEMPTY));

                } else if(List.class.isAssignableFrom(argumentClasses[i])) {
                    assignments.add(new Assignment(i, LIST_EMPTY));
                    assignments.add(new Assignment(i, LIST_NONEMPTY));

                } else if(Set.class.isAssignableFrom(argumentClasses[i])) {
                    assignments.add(new Assignment(i, SET_EMPTY));
                    assignments.add(new Assignment(i, SET_NONEMPTY));

                } else if(Map.class.isAssignableFrom(argumentClasses[i])) {
                    assignments.add(new Assignment(i, MAP_EMPTY));
                    assignments.add(new Assignment(i, MAP_NONEMPTY));
                // TODO: Collection.class?
                } else
                    assignments.add(new Assignment(i, REF_NONNULL));
                break;
            default:
                break;
        }
        return assignments;

    }

    // compute goals from existing test result based on actual arguments numeric values
    public static Set<CitCoverageGoal> createCoveredGoalsFromParameters(String className, String methodName, String methodDesc, List<Object> argumentsValues) {
        Set<CitCoverageGoal> citGoals = new HashSet<>();
        List<Assignment> assignments = new ArrayList<>();

        Type[] argTypes = Type.getArgumentTypes(methodDesc);

        for (int i=0;i<argTypes.length;i++) {
            Type argType = argTypes[i];
            Object argValue = argumentsValues.get(i);
            String argValueDesc = "";
            Number numberValue = null;  //posso evitare di calcolarlo dato che in questa versione non lo uso
            switch (argType.getSort()) {
                case Type.BOOLEAN:
                    argValueDesc = (((boolean) argValue)) ? BOOL_TRUE : BOOL_FALSE;
                    break;
                case Type.CHAR:
                    char c = (char) argValue;
                    if (Character.isAlphabetic(c))
                        argValueDesc = CHAR_ALPHA;
                    else if (Character.isDigit(c))
                        argValueDesc = CHAR_DIGIT;
                    else
                        argValueDesc = CHAR_OTHER;
                    break;
                case Type.BYTE:
                case Type.SHORT:
                case Type.INT:
                case Type.FLOAT:
                case Type.LONG:
                case Type.DOUBLE:
                    // assert (argValue instanceof Number); // not always true: char can be assigned to integers
                    double value;

                    if (argValue instanceof Character) {
                        value = ((Number) ((int) (char) argValue)).doubleValue();
                    } else {
                        value = ((Number) argValue).doubleValue();
                    }
                    numberValue = value;
                    argValueDesc = (value < 0) ? NUM_NEGATIVE : (value == 0) ? NUM_ZERO : NUM_POSITIVE;
                    break;
                case Type.ARRAY:
                    if (argValue == null)
                        argValueDesc = REF_NULL;
                    else
                        argValueDesc = (Array.getLength(argValue) == 0) ? ARRAY_EMPTY : ARRAY_NONEMPTY;
                    break;
                case Type.OBJECT:
                    if (argValue == null)
                        argValueDesc = REF_NULL;
                    else {
                        if (argType.getClassName().equals("java.lang.String")) {
                            argValueDesc = ((String) argValue).isEmpty() ? STRING_EMPTY : STRING_NONEMPTY;
                        }
                        else if(argValue instanceof List) {
                            argValueDesc = ((List) argValue).isEmpty() ? LIST_EMPTY : LIST_NONEMPTY;
                        }
                        else if(argValue instanceof Set) {
                            argValueDesc = ((Set) argValue).isEmpty() ? SET_EMPTY : SET_NONEMPTY;
                        }
                        else if(argValue instanceof Map) {
                            argValueDesc = ((Map) argValue).isEmpty() ? MAP_EMPTY : MAP_NONEMPTY;
                        }
                        else
                            argValueDesc = REF_NONNULL;
                    }
                    break;
                default:
                    break;
            }
            if (!argValueDesc.isEmpty())
                assignments.add(new Assignment(i, argValueDesc)); //ignoring numberValue at this implementation version
        }

        logger.debug("Computed {} assignments in test method {} invocation.", assignments.size(), methodName);

        // enumerate pairs of distinct assignments to make pairwise coverage
        for (Assignment a1 : assignments)
            for (Assignment a2 : assignments)
                if (a1.index < a2.index) {
                    Set<Assignment> tuple = new HashSet<Assignment>();
                    tuple.add(a1); tuple.add(a2);   
                    citGoals.add(new CitCoverageGoal(className, methodName, tuple));
                }

        logger.debug("covered {} cit goals from statement: {}", citGoals.size(), methodName);       
        return citGoals;
    }


}
