package intermediate.scope;

import intermediate.Kind;
import intermediate.type.TypeSpec;

public class ScopeEntry
{
    public ScopeEntry(String name, TypeSpec type)
    {
        this.name = name;
        this.type = type;
        kind = Kind.VARIABLE;
        slotNumber = 0;
        instanceType = null;
    }
    
    public ScopeEntry(String name, TypeSpec type, Kind kind)
    {
        this.name = name;
        this.type = type;
        this.kind = kind;
        slotNumber = 0;
        instanceType = null;
    }
    
    public String toString()
    {
        return "[name: " + name + ", type : " + type.name + "]";
    }
    
    public String name;
    public TypeSpec type;
    public TypeSpec instanceType; //if the ScopeEntry is a field, then the instance refers to what type the field belongs to
    public Kind kind;
    public int slotNumber;
}
