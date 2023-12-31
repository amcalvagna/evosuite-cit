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
package org.evosuite.coverage.io.input;

import static org.evosuite.coverage.io.IOCoverageConstants.*;

import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.evosuite.coverage.MethodNameMatcher;
import org.evosuite.graphs.cfg.BytecodeInstructionPool;
import org.evosuite.setup.TestClusterUtils;
import org.evosuite.setup.TestUsageChecker;
import org.evosuite.testsuite.AbstractFitnessFactory;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.*;

/**
 * @author Jose Miguel Rojas
 */
public class InputCoverageFactory extends AbstractFitnessFactory<InputCoverageTestFitness> {

    private static final Logger logger = LoggerFactory.getLogger(InputCoverageFactory.class);

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
    public List<InputCoverageTestFitness> getCoverageGoals() {
        List<InputCoverageTestFitness> goals = new ArrayList<InputCoverageTestFitness>();

        long start = System.currentTimeMillis();
        String targetClass = Properties.TARGET_CLASS;

        final MethodNameMatcher matcher = new MethodNameMatcher();
        for (String className : BytecodeInstructionPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).knownClasses()) {
            if (!(targetClass.equals("") || className.endsWith(targetClass)))
                continue;

            for (Method method : TestClusterUtils.getClass(className).getDeclaredMethods()) {
                String methodName = method.getName() + Type.getMethodDescriptor(method);
                if (!TestUsageChecker.canUse(method) || !matcher.methodMatches(methodName))
                    continue;
                logger.info("Adding Input goals for method " + className + "." + methodName);

                Type[] argumentTypes = Type.getArgumentTypes(method);
                Class<?>[] argumentClasses = method.getParameterTypes();
                for (int i=0; i<argumentTypes.length;i++){
                    Type argType = argumentTypes[i];
                    switch (argType.getSort()) {
                        case Type.BOOLEAN:
                            goals.add(createGoal(className, methodName, i, argType, BOOL_TRUE));
                            goals.add(createGoal(className, methodName, i, argType, BOOL_FALSE));
                            break;
                        case Type.CHAR:
                            goals.add(createGoal(className, methodName, i, argType, CHAR_ALPHA));
                            goals.add(createGoal(className, methodName, i, argType, CHAR_DIGIT));
                            goals.add(createGoal(className, methodName, i, argType, CHAR_OTHER));
                            break;
                        case Type.BYTE:
                        case Type.SHORT:
                        case Type.INT:
                        case Type.FLOAT:
                        case Type.LONG:
                        case Type.DOUBLE:
                            goals.add(createGoal(className, methodName, i, argType, NUM_NEGATIVE));
                            goals.add(createGoal(className, methodName, i, argType, NUM_ZERO));
                            goals.add(createGoal(className, methodName, i, argType, NUM_POSITIVE));
                            break;
                        case Type.ARRAY:
                            goals.add(createGoal(className, methodName, i, argType, REF_NULL));
                            goals.add(createGoal(className, methodName, i, argType, ARRAY_EMPTY));
                            goals.add(createGoal(className, methodName, i, argType, ARRAY_NONEMPTY));
                            break;
                        case Type.OBJECT:
                            goals.add(createGoal(className, methodName, i, argType, REF_NULL));
                            if (argType.getClassName().equals("java.lang.String")) {
                                goals.add(createGoal(className, methodName, i, argType, STRING_EMPTY));
                                goals.add(createGoal(className, methodName, i, argType, STRING_NONEMPTY));

                            } else if(List.class.isAssignableFrom(argumentClasses[i])) {
                                goals.add(createGoal(className, methodName, i, argType, LIST_EMPTY));
                                goals.add(createGoal(className, methodName, i, argType, LIST_NONEMPTY));

                            } else if(Set.class.isAssignableFrom(argumentClasses[i])) {
                                goals.add(createGoal(className, methodName, i, argType, SET_EMPTY));
                                goals.add(createGoal(className, methodName, i, argType, SET_NONEMPTY));

                            } else if(Map.class.isAssignableFrom(argumentClasses[i])) {
                                goals.add(createGoal(className, methodName, i, argType, MAP_EMPTY));
                                goals.add(createGoal(className, methodName, i, argType, MAP_NONEMPTY));
                            // TODO: Collection.class?
                            } else
                                goals.add(createGoal(className, methodName, i, argType, REF_NONNULL));
                            break;
                        default:
                            break;
                    }
                }
            }
        }
        goalComputationTime = System.currentTimeMillis() - start;
        return goals;
    }

    public static InputCoverageTestFitness createGoal(String className, String methodName, int argIndex, Type argType, String descriptor) {
        return new InputCoverageTestFitness(new InputCoverageGoal(className, methodName, argIndex, argType, descriptor));
    }
}
