package frontend;

import org.antlr.v4.runtime.ParserRuleContext;

import intermediate.Predefined;
import intermediate.type.*;

public class SemanticErrorHandler
{
    private int count;
    
    public SemanticErrorHandler()
    {
        count = 0;
    }
    
    public int getErrorCount()
    {
        return count;
    }
    
    public void typeReturnFailed(ParserRuleContext ctx)
    {
        printError("expression doesn't return object", ctx);
    }
    
    /**
     * @param t1 the type that being converted from
     * @param t2 the type that being converted to
     * @param ctx
     */
    public void typeConversionError(TypeSpec t1, TypeSpec t2, ParserRuleContext ctx)
    {
        printError("cannot convert type '" + t1.name + "' to '" + t2.name + "'", ctx);
    }
    
    public void invalidLHSError(ParserRuleContext ctx)
    {
        printError("invalid left-hand side assignment", ctx);
    }
    
    public void boolTypeError(TypeSpec type, ParserRuleContext ctx) {
        printError(type.name + " is not a " + Predefined.BOOL, ctx);
    }
    
    public void typeNotFoundError(String typeName, ParserRuleContext ctx)
    {
        printError("cannot find type '" + typeName + "'", ctx);
    }
    
    public void duplicateIdentifierError(String idName, ParserRuleContext ctx)
    {
        printError("the identifier '" + idName + "' has already been declared in the local scope", ctx);
    }
    
    public void duplicateVariableTypeError(String varName, ParserRuleContext ctx)
    {
        printError("the identifier '" + varName + "' has already been declared as a type", ctx);
    }
    
    public void identifierNotDeclaredError(String varName, ParserRuleContext ctx)
    {
        printError("identifier '" + varName + "' has not been declared", ctx);
    }
    
    public void identifierNotDeclaredError(String id, String typeName, ParserRuleContext ctx)
    {
        printError("identifier '" + id + "' has not been declared for type '" + typeName + "'", ctx);
    }
    
    public void staticIdentifierNotDeclaredError(String id, String typeName, ParserRuleContext ctx)
    {
        printError("static identifier '" + id + "' has not been declared for type '" + typeName + "'", ctx);
    }
    
    public void routineNotFoundError(String routineName, ArgumentList arguments, ParserRuleContext ctx)
    {
        printError("no routine named '" + routineName + "' that accepts " + arguments + " exists", ctx);
    }
    
    public void routineNotFoundError(String routineName, ArgumentList arguments, TypeSpec type, ParserRuleContext ctx)
    {
        printError("no routine named '" + routineName + "' that accepts " + arguments + " exists for type '" + type.name + "'", ctx);
    }
    
    public void duplicateRoutineError(String routineName, ArgumentList arguments, ParserRuleContext ctx)
    {
        printError("routine named '" + routineName + "' that accepts " + arguments + " already exists", ctx);
    }
 
    public void invalidReturnError(String routineName, ParserRuleContext ctx)
    {
        printError("invalid return statement. '" + routineName + "' does not return a value", ctx);
    }
    
    public void invalidReturnError(String routineName, String returnTypeName, ParserRuleContext ctx)
    {
        printError("routine '" + routineName + "' returns type '" + returnTypeName + "'. No return statement found", ctx);
    }
    
    public void routineNotClosedError(String routineName, String routineClosure, ParserRuleContext ctx)
    {
        printError("expecting '" + routineName + "' to close routine. Got '" + routineClosure + "'", ctx);
    }
    
    public void duplicateTypeError(String typeName, ParserRuleContext ctx)
    {
        printError("type '" + typeName + "' already exists", ctx);
    }
    
    public void duplicateTypeVariableError(String typeName, ParserRuleContext ctx)
    {
        printError("the identifier '" + typeName + "' has already been declared in the local scope", ctx);
    }
    
    public void typeNotClosedError(String typeName, String typeClosure, ParserRuleContext ctx)
    {
        printError("expecting '" + typeName + "' to close type definiton. Got '" + typeClosure + "'", ctx);
    }
    
    public void typeCallError(String typeName, ParserRuleContext ctx)
    {
        printError("cannot call type '" + typeName + "' without any modifiers", ctx);
    }
    
    public void printError(String error, ParserRuleContext ctx)
    {
        System.out.println("Error on line " + ctx.getStart().getLine() + ": " + error + ". Found at: " + ctx.getText());
        count++;
    }
}