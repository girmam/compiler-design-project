package intermediate.type;

import intermediate.Predefined;
import intermediate.scope.ScopeEntry;

public class TypeChecker
{
    public static boolean isAssignmentCompatible(ScopeEntry lhsEntry, TypeSpec lhsType, TypeSpec rhs)
    {
        if(lhsType != null)
        {
            //if there is a valid assignment operator, use it
            if(lhsType.lookupRoutine(Predefined.OPERATOR_ASSIGN, new ArgumentList(rhs)) != null)
                return true;
            
            //otherwise, we need to make sure that the lhsEntry is not null and that the type is compatible
            if(lhsEntry != null)
                return areTypesCompatible(lhsEntry.type, rhs);
        }
        
        return false;
    }
    
    public static boolean areTypesCompatible(TypeSpec lhs, TypeSpec rhs)
    {
        //TODO: include casting support
        return lhs.equals(rhs);
    }
}
