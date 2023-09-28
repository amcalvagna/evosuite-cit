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

import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.*;


/**
 * A single input coverage goal: is a tuple of abstract parameter asignments,
 * where abstract values are those defined in io.IOCoverageConstants  
 * Evaluates the value depending on the type of the i-th input argument to a
 * method.
 * 
 *
 * @author Gordon Fraser, Andre Mis, Jose Miguel Rojas, Andrea Calvagna 
 */
public class CitCoverageGoal implements Serializable, Comparable<CitCoverageGoal> {  

    private static final Logger logger = LoggerFactory.getLogger(CitCoverageGoal.class);

    private static final long serialVersionUID = -2917009638438833179L;

    private final String className;
    private final String methodName;
    private final Set<Assignment> tuple;

    /*
     * private final String type1,type2;
     * private final String valueDescriptor1, valueDescriptor2;
     * private final Number numericValue1, numericValue2;
     */
    /**
     * Can be used to create an arbitrary {@code InputCoverageGoal} trying to cover
     * the
     * method such that it returns a given {@code value}
     * <p/>
     * <p/>
     * If the method returns a boolean, this goal will try to cover the method with
     * either {@code true} or {@code false}
     * If the given branch is {@code null}, this goal will try to cover the root
     * branch
     * of the method identified by the given name - meaning it will just try to
     * call the method at hand
     * <p/>
     * <p/>
     * Otherwise this goal will try to reach the given branch and if value is
     * true, make the branchInstruction jump and visa versa
     *
     * @param className       a {@link String} object.
     * @param methodName      a {@link String} object.
     * @param argIndex        an argument index.
     * @param type            a {@link Type} object.
     * @param valueDescriptor a value descriptor.
     */
    public CitCoverageGoal(String className, String methodName, Set<Assignment> tuple) {

        // s logger.info("new CitCoverageGoal! {}", descriptor);

        if (className == null || methodName == null)
            throw new IllegalArgumentException("null given");
        for (Assignment a : tuple)
            if (a == null)
                throw new IllegalArgumentException("null given");
        /*
         * TODO: check for duplicates
         * if (goal1 == goal2)
         * throw new IllegalArgumentException("same argument given");
         */
        this.tuple = tuple;
        this.className = className;
        this.methodName = methodName;
        
        logger.debug("created new CIT goal tuple: {}", this.toString());


    }

    /**
     * @return the className
     */
    public String getClassName() {
        return className;
    }

    /**
     * @return the methodName
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     * @return the argument index
     */
    public Set<Assignment> getTuple() {
        return this.tuple;
    }

    // inherited from Object

    /**
     * {@inheritDoc}
     * <p/>
     * Readable representation
     */
    @Override
    public String toString() {
        String s=getClassName()+"."+getMethodName(); 

        for(Assignment a: this.tuple) s+=a.toString();
        return s;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;

        result = prime * result + className.hashCode();
        result = prime * result + methodName.hashCode();
        result = prime * result + tuple.hashCode();
        return result;
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
        if (obj.getClass() != getClass()) {
            logger.error("comparing with wrong type of goal! {}", obj.getClass().getName());
            return false;
        }

        CitCoverageGoal other = (CitCoverageGoal) obj;

        if (!other.getClassName().equals(getClassName())) {
            logger.error("comparing goals of a wrong class {} vs {}", this.getClassName(), other.getClassName());
            return false;
        } 

        if (!this.getMethodName().equals(other.getMethodName())) {
            //logger.info("method not matching! {} - {} ", obj1.getMethodName(),
            // obj2.getMethodName());
            return false;
        }

        if (!this.tuple.containsAll(other.tuple)) {
            logger.info("compared NON matching tuples {} - {}", this.toString(), other.toString());
            return false;

        }
        logger.info("matching CIT goals {} = {}", this.toString(), other.toString());
        return true;

    }

    /*
     * prima confronto i primi goal e poi i secondi
     */
    @Override
    public int compareTo(CitCoverageGoal o) {
        int diff = className.compareTo(o.className);
        if (diff == 0) { // same class
            int diff2 = methodName.compareTo(o.methodName);
            if (diff2 == 0) { // same method
                return new Integer(this.tuple.hashCode()).compareTo( new Integer(o.tuple.hashCode()));  // 0 if same assignments            
            } else
                return diff2;
        } else
            return diff;
    }



   

 


}
