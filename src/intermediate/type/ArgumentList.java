package intermediate.type;

import java.util.ArrayList;

public class ArgumentList
{
    public ArrayList<TypeSpec> argumentTypes;
    
    public ArgumentList(TypeSpec ...args)
    {
        argumentTypes = new ArrayList<>();
        
        for(TypeSpec arg: args)
            argumentTypes.add(arg);
    }
    
    public ArgumentList()
    {
        argumentTypes = new ArrayList<>();
    }
    
    @Override
    public String toString()
    {
        String argString = "(";
        
        for(int i = 0; i < argumentTypes.size(); i++)
        {
            if(i != 0)
                argString += ", ";
            
            argString += argumentTypes.get(i).name;
        }
        
        argString += ")";
        
        return argString;
    }
    
    @Override
    //Does not currently allow casting
    public boolean equals(Object o)
    {
        if(o == null)
            return false;
        if(getClass() != o.getClass())
            return false;
        if(this == o)
            return true;
        
        ArgumentList other = (ArgumentList) o;
        
        boolean areEqual = argumentTypes.size() == other.argumentTypes.size();
        
        for(int i = 0; i < argumentTypes.size() && areEqual; i++)
        {
            if(!areEqual)
                return false;
            areEqual = this.argumentTypes.get(i).equals(other.argumentTypes.get(i));
        }
            
        return areEqual;
    }
}
