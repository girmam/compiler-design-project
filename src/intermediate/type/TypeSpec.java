package intermediate.type;

import java.util.ArrayList;
import java.util.HashMap;

import intermediate.Kind;
import intermediate.Predefined;
import intermediate.scope.ScopeEntry;

public class TypeSpec
{
    public String name;
    private String path;
    private ArrayList<RoutineSpec> routines;
    private ArrayList<RoutineSpec> staticRoutines;
    private ArrayList<ScopeEntry> fields;
    private ArrayList<ScopeEntry> staticFields;
    
    public TypeSpec(String name)
    {
        this.name = name;
        this.path = name;
        this.routines = new ArrayList<>();
        this.staticRoutines = new ArrayList<>();
        this.fields = new ArrayList<>();
        this.staticFields = new ArrayList<>();
    }

    public String getPath()
    {
        return path;
    }
    
    public void setPath(String path)
    {
        this.path = path;
    }
    
    public void addRoutine(RoutineSpec routine)
    {
        routines.add(routine);
    }
    
    public ArrayList<ScopeEntry> getRoutines()
    {
        return getRoutinesHelper(false);
    }
    
    public void addStaticRoutine(RoutineSpec routine)
    {
        staticRoutines.add(routine);
    }
    
    public ArrayList<ScopeEntry> getStaticRoutines()
    {
        return getRoutinesHelper(true);
    }
    
    public void addField(ScopeEntry field)
    {
        field.kind = Kind.FIELD;
        field.instanceType = this;
        fields.add(field);
    }
    
    public ArrayList<ScopeEntry> getFields()
    {
        return fields;
    }
    
    public void addStaticField(ScopeEntry field)
    {
        field.kind = Kind.STATIC_FIELD;
        field.instanceType = this;
        staticFields.add(field);
    }
    
    public ArrayList<ScopeEntry> getStaticFields()
    {
        return staticFields;
    }
    
    /**
     * If a type has a routine that matches the given name and the types in the argument list, return it's routine spec. Otherwise, return null.
     * This does not currently check if an argument can be automatically cast to a type that would be acceptable by the routine
     * @param routineName
     * @param arguments
     * @return
     */
    public RoutineSpec lookupRoutine(String routineName, ArgumentList arguments)
    {
        for(RoutineSpec routine : routines)
        {
            //if the name and arguments match for the routine, return it
            if(routine.name.equals(routineName) && routine.arguments.equals(arguments))
                return routine;
        }
        
        //otherwise, return null
        return null;
    }
    
    public ScopeEntry lookupRoutine(String routineName)
    {
        TypeSpec routineType = new TypeSpec(Predefined.ROUTINE);
        boolean routineFound = false;

        for(RoutineSpec routine : routines)
            if(routine.name.equals(routineName))
            {
                //we create a new routine spec with the name Operator_Paren because that's the only available routine that can be applied to that object
                routineType.addRoutine(new RoutineSpec(Predefined.OPERATOR_PAREN, routine.arguments, routine.returnType));
                routineFound = true;
            }
        
        if(routineFound)
        {
            ScopeEntry returnEntry = new ScopeEntry(routineName, routineType, Kind.FIELD);
            returnEntry.instanceType = this;
            return returnEntry;
        }
        else
            return null;
    }
    
    public RoutineSpec lookupStaticRoutine(String routineName, ArgumentList arguments)
    {
        for(RoutineSpec routine : staticRoutines)
        {
            //if the name and arguments match for the routine, return it
            if(routine.name.equals(routineName) && routine.arguments.equals(arguments))
                return routine;
        }
        
        //otherwise, return null
        return null;
    }
    
    public ScopeEntry lookupStaticRoutine(String routineName)
    {
        TypeSpec routineType = new TypeSpec(Predefined.ROUTINE);
        boolean routineFound = false;
        
        for(RoutineSpec routine : staticRoutines)
            if(routine.name.equals(routineName))
            {
                routineType.addRoutine(routine);
                routineFound = true;
            }
        
        if(routineFound)
        {
            ScopeEntry returnEntry = new ScopeEntry(routineName, routineType, Kind.STATIC_FIELD);
            returnEntry.instanceType = this;
            return returnEntry;
        }
        else
            return null;
    }
    
    public boolean hasStaticRoutine(String routineName)
    {
        for(RoutineSpec routine : staticRoutines)
            if(routine.name.equals(routineName))
                return true;
        
        return false;
    }
    
    public ScopeEntry lookupField(String varName)
    {
        for(ScopeEntry field : fields)
        {
            if(field.name.equals(varName))
                return field;
        }
        
        return null;
    }
    
    public ScopeEntry lookupStaticField(String varName)
    {
        for(ScopeEntry field : staticFields)
        {
            if(field.name.equals(varName))
                return field;
        }
        
        return null;
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
        
        TypeSpec other = (TypeSpec) o;

        //if both types are routines, check if their arguments and return types match
        if(name.equals(Predefined.ROUTINE) && other.name.equals(Predefined.ROUTINE))
        {
            //if we're testing for routine equality, then we assume the lhs is a routine variable and thus only has 1 routine
            RoutineSpec lhs = routines.get(0);
            
            for(RoutineSpec routine: other.routines)
            {
                //check to see if the argument list and return types are equal
                if(lhs.arguments.equals(routine.arguments) && ((lhs.returnType == null && routine.returnType == null) || lhs.returnType.equals(routine.returnType)))
                    return true;
            }
            return false;
        }
        return false;
    }
    
    @Override
    public String toString()
    {
        String str = "type " + name + "\n\nFields:\n";
        for(ScopeEntry field : fields)
            str += "\t" + field + "\n";
        
        str += "\nStatic Fields\n";
        for(ScopeEntry field : staticFields)
            str += "\t" + field + "\n";
        
        str += "\nRoutines\n";
        for(RoutineSpec routine : routines)
            str += "\t" + routine + "\n";
        
        str += "\nStatic Routines\n";
        for(RoutineSpec routine : staticRoutines)
            str += "\t" + routine + "\n";
        
        str += "\nend " + name + "\n";
        
        return str;
    }
    
    private ArrayList<ScopeEntry> getRoutinesHelper(boolean isStatic)
    {
        HashMap<String, ScopeEntry> entryMap = new HashMap<>();
        
        for(RoutineSpec routine: ((isStatic)? staticRoutines : routines))
        {
            //if the routine with the same name has already been added to the map:
            if(entryMap.containsKey(routine.name))
            {
                //we create a new routine spec with the name OPERATOR_PAREN because that's the operator being applied to the routine object
                entryMap.get(routine.name).type.addRoutine(new RoutineSpec(Predefined.OPERATOR_PAREN, routine.arguments, routine.returnType));
            }
            else
            {
                TypeSpec routineType = new TypeSpec(Predefined.ROUTINE);
                routineType.addRoutine(new RoutineSpec(Predefined.OPERATOR_PAREN, routine.arguments, routine.returnType));
                ScopeEntry routineEntry = new ScopeEntry(routine.name, routineType, (isStatic)? Kind.STATIC_FIELD : Kind.FIELD);
                routineEntry.instanceType = this;
                entryMap.put(routine.name, routineEntry);
            }
        }
        
        ArrayList<ScopeEntry> entries = new ArrayList<>();
        
        for(ScopeEntry entry: entryMap.values())
           entries.add(entry);
        
        return entries;
    }
}
