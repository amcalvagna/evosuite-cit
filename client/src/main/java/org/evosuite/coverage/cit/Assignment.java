package org.evosuite.coverage.cit;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Assignment implements Serializable, Comparable<Assignment>{
    private static final Logger logger = LoggerFactory.getLogger(CitCoverageGoal.class);

    public final int index; 
    public final String valueDescriptor;
    private boolean covered=false;


    public Assignment(int index, String valueDescriptor) {
        this.index = index;
        this.valueDescriptor = valueDescriptor;
    }
    
    //unused?
    public boolean isCovered() {return covered;}
    public void setCovered(){ covered=true;}


    @Override
    public String toString() {
        return "["+index+":"+valueDescriptor+"]";
    }

    @Override
    public int compareTo(Assignment other) {
        if (this.equals(other)) return 0;
        return new Integer(this.index).compareTo(new Integer(other.index));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj==this) return true; 
        if (obj.getClass() != getClass()) return false; 
        Assignment other = (Assignment) obj;
        boolean result = this.index==other.index && this.valueDescriptor.equals(other.valueDescriptor);
        logger.debug("into equals for ass. {} vs {} = {} ",this.toString(),other.toString(),result);
        return result;

    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;

        result = prime * result + index;
        result = prime * result + (valueDescriptor == null ? 0 : valueDescriptor.hashCode());
        return result;
    }
    
}
