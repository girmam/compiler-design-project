package intermediate.type;

public class RoutineSpec
{
    public RoutineSpec(String name, ArgumentList arguments, TypeSpec returnType)
    {
        this.name = name;
        this.arguments = arguments;
        this.returnType = returnType;
    }
    
    public String toString()
    {
        String str = "[name: " + name + ", parameters: " + arguments;
        
        if(returnType != null)
            str += ", returns " + returnType.name;
        
        str += "]";
        
        return str;
    }
    
    @Override
    public boolean equals(Object o)
    {
        if(o == null)
            return false;
        if(getClass() != o.getClass())
            return false;
        if(this == o)
            return true;
        
        RoutineSpec other = (RoutineSpec) o;
        return name.equals(other.name) && arguments.equals(other.arguments);
    }
    
    @Override
    public int hashCode()
    {
        return toString().hashCode();
    }
    
    public String name;
    public ArgumentList arguments;
    public TypeSpec returnType;
}
