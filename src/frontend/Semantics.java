package frontend;

import antlr4.*;
import intermediate.Kind;
import intermediate.Predefined;
import intermediate.scope.Scope;
import intermediate.scope.ScopeEntry;
import intermediate.type.*;

public class Semantics extends SixtyFortranBaseVisitor<Object>
{    
    private Scope worldScope;
    private Scope localScope;
    private SemanticErrorHandler error;
    
    public Semantics(SemanticErrorHandler error)
    {
        this.error = error;
        worldScope = new Scope(null, Kind.MAIN);
        localScope = worldScope;
        
        Predefined.addPredefinedTypesToScope(worldScope);
        Predefined.addPredefinedRoutinesToScope(worldScope);
    }
    
    public Scope getWorldScope()
    {
        return worldScope;
    }
    
    @Override
    public Object visitIfStatement(SixtyFortranParser.IfStatementContext ctx) {
        return visitChildren(ctx); // Nothing to do here (container for if/else if/else blocks)
    }
    
    @Override
    public Object visitIfBlock(SixtyFortranParser.IfBlockContext ctx) {
        localScope = new Scope(localScope, Kind.INNER); // Create a new local scope for an else if block
        ctx.scope = localScope; // Set the current scope to this else if block scope
        // *** Type checking ***
        if (ctx.expression().type == null) // If type is null
            return null; // Message printed down the line
        else if (!TypeChecker.areTypesCompatible(Predefined.boolType, ctx.expression().type)) // If type is not boolType
            error.boolTypeError(ctx.expression().type, ctx); // Throw a boolType exception
        else // If type is okay (no semantic problem)
            visitChildren(ctx); // Visit rest of block (children)
        localScope = localScope.getParent(); // After visiting if block, go back to the parent scope
        return null;
    }
    
    @Override
    public Object visitElseifBlock(SixtyFortranParser.ElseifBlockContext ctx) {
        localScope = new Scope(localScope, Kind.INNER); // Create a new local scope for an else if block
        ctx.scope = localScope; // Set the current scope to this else if block scope
        // *** Type checking ***
        if (ctx.expression().type == null) // If type is null
            return null; // Message printed down the line
        else if (!TypeChecker.areTypesCompatible(Predefined.boolType, ctx.expression().type)) // If type is not boolType
            error.boolTypeError(ctx.expression().type, ctx); // Throw a boolType exception
        else // If type is okay (no semantic problem)
            visitChildren(ctx); // Visit rest of block (children)
        localScope = localScope.getParent(); // After visiting if block, go back to the parent scope
        return null;
    }
    
    @Override
    public Object visitElseBlock(SixtyFortranParser.ElseBlockContext ctx) {
        localScope = new Scope(localScope, Kind.INNER); // Create a new local scope for an else block
        ctx.scope = localScope; // Set the current scope to this else if block scope
        visitChildren(ctx); // Visit rest of block (children)
        localScope = localScope.getParent(); // After visiting else block, go back to the parent scope
        return null;
    }
    
    @Override
    public Object visitLoopStatement(SixtyFortranParser.LoopStatementContext ctx) {
        localScope = new Scope(localScope, Kind.INNER); // Create a new local scope for an else if block
        ctx.scope = localScope; // Set the current scope to this else if block scope
        // *** Type checking ***
        if (ctx.expression().type == null) // If type is null
            return null; // Message printed down the line
        else if (!TypeChecker.areTypesCompatible(Predefined.boolType, ctx.expression().type)) // If type is not boolType
            error.boolTypeError(ctx.expression().type, ctx); // Throw a boolType exception
        else // If type is okay (no semantic problem)
            visitChildren(ctx); // Visit rest of block (children)
        localScope = localScope.getParent(); // After visiting if block, go back to the parent scope
        return null;
    }
    
    @Override
    public Object visitTypeDefinition(SixtyFortranParser.TypeDefinitionContext ctx)
    {
        String typeName = ctx.simpleTypeName(0).getText(); //the first simpletypename is the actual type name
        
        //if a type with the same name already exists, throw an error      
        if(localScope.lookupType(typeName) != null)
        {
            error.duplicateTypeError(typeName, ctx);
            return null;
        }
        
        //if a variable with the same name already exists, throw an error
        if(localScope.lookupEntry(typeName) != null)
        {
            error.duplicateIdentifierError(typeName, ctx);
            return null;
        }
        
        //add the type to the local scope
        TypeSpec type = new TypeSpec(typeName);
        ctx.type = type;
        localScope.addType(type);
        
        //enter the type's scope
        localScope = new Scope(localScope, Kind.TYPE); 
        ctx.typeScope = localScope;
        
        //visit the other methods to create the scope entires and routine specs
        visitChildren(ctx);
        
        //adds all of the fields to the type
        for(SixtyFortranParser.VarDeclarationContext varDec : ctx.definitionBlock().varBlock().varDeclaration())
            for(ScopeEntry typeField : varDec.entries)
                type.addField(typeField);
                
        //adds all of the static fields to the type
        for(SixtyFortranParser.StaticVarDefinitionContext varDef : ctx.definitionBlock().varBlock().staticVarDefinition())
            for(ScopeEntry typeStaticField : varDef.varDeclaration().entries)
                type.addStaticField(typeStaticField);
                
        //adds all of the routines to the type
        for(SixtyFortranParser.RoutineDefinitionContext routineDef : ctx.definitionBlock().routineBlock().routineDefinition())
            if(routineDef.routine != null)
                type.addRoutine(routineDef.routine);
        
        //adds all of the static routines to the type
        for(SixtyFortranParser.StaticRoutineDefinitionContext routineDef : ctx.definitionBlock().routineBlock().staticRoutineDefinition())
            if(routineDef.routineDefinition().routine != null)
                type.addStaticRoutine(routineDef.routineDefinition().routine);
        
        //exist the type's scope
        localScope = localScope.getParent();
        
        String typeClosure = ctx.simpleTypeName(1).getText();
        
        if(!typeClosure.equals(typeName))
            error.typeNotClosedError(typeName, typeClosure, ctx);
        
        return null;
    }
    
    @Override
    public Object visitAssignNewVariables(SixtyFortranParser.AssignNewVariablesContext ctx)
    {
        SixtyFortranParser.VarDeclarationContext varDeclarations = ctx.varDeclaration();
        visit(varDeclarations.typeName()); //get the type of the var declaration
        TypeSpec type = varDeclarations.typeName().type;
        
        if(type == null)
            return null;

        visit(ctx.expression()); //visit the expression and assign it a type
        TypeSpec expType = ctx.expression().type;
        
        if(expType == null)
        {
            return null;
        }
        else if(TypeChecker.isAssignmentCompatible(varDeclarations.entries.get(0), type, expType)) //if the expression type can be converted to the variable's type
        {
            visit(ctx.varDeclaration()); //visits the variable declaration, checks if they've been added to the scope, and if not, adds them
        }
        else
        {
            error.typeConversionError(expType, type, ctx);
        }       
        
        return null;
    }
    
    @Override
    public Object visitAssignExistingVariable(SixtyFortranParser.AssignExistingVariableContext ctx)
    {
        //make sure that the call and expression are semantically correct and assign them their types
        visit(ctx.call());
        visit(ctx.expression());
        
        TypeSpec callType = ctx.call().type;
        ScopeEntry callEntry = ctx.call().callEntry;
        TypeSpec expType = ctx.expression().type;
        
        
        if(callType == null || expType == null)
        {
            return null;
        }
        else if(!TypeChecker.isAssignmentCompatible(callEntry, callType, expType)) //throw an error if you cannot assign the rhs to the lhs
        {
            if(!TypeChecker.areTypesCompatible(callType, expType))
                error.typeConversionError(expType, callType, ctx);
            else
                error.invalidLHSError(ctx.expression());
        }
        
        return null;
    }
    
    @Override
    public Object visitVarDeclaration(SixtyFortranParser.VarDeclarationContext ctx)
    {
        visit(ctx.typeName()); //visits the type (which also checks for type errors) 
        
        TypeSpec type = ctx.typeName().type; 

        if(type == null)
            return null;
        
        //if the type does exist, then continue
        for(SixtyFortranParser.VarNameContext varCtx : ctx.varName())
        {
            String varName = varCtx.getText();
            
            if(localScope.lookupEntryNoParent(varName) != null) //checks the local scope only to see if the name is already declared
            {
                error.duplicateIdentifierError(varName, varCtx);
            }
            if(localScope.lookupType(varName) != null)
            {
                error.duplicateVariableTypeError(varName, ctx);
            }
            else //there is no other identifier with the same name in the local scope
            {
                ScopeEntry entry = new ScopeEntry(varName, type);
                localScope.addEntry(entry);
                
                ctx.entries.add(entry);
            }
        }

        return null;
    }
    
    @Override
    public Object visitStaticRoutineDefinition(SixtyFortranParser.StaticRoutineDefinitionContext ctx)
    {
        ctx.routineDefinition().isStatic = true;
        return visitChildren(ctx);
    }
    
    /**
    * @author Jordan
    * To whomever has to read this: I'm sorry it is such a mess. Also, too bad. Writing a semantic pass is hard.
    */
    @Override
    public Object visitRoutineDefinition(SixtyFortranParser.RoutineDefinitionContext ctx)
    {
        boolean parameterListValid = checkRoutineTypes(ctx.routineParameters(), ctx.typeName()); //checks the types of the parameter list and return type
        
        if(!parameterListValid)
            return null;
 
        String routineName = ctx.routineName(0).getText(); //get the first routine name, which is the actual name of the routine (the second routine name is for END routine) 
        ArgumentList routineParameters = createParameterList(ctx.routineParameters());
        
        //checks if a routine with the same name and parameters has already been defined in the current scope
        ScopeEntry preexistingEntry = localScope.lookupEntryNoParent(routineName); 
        
        //check if a type with the same name as a routine has already been declared
        TypeSpec preexistingType = localScope.lookupType(routineName);
        
        //a routine or variable with the same name already exists in the current scope
        if(preexistingEntry != null) 
        {
            if(!preexistingEntry.type.name.equals(Predefined.ROUTINE)) //if a variable with the same name already exists in the scope, throw an error
            {
                error.duplicateIdentifierError(routineName, ctx);
                return null;
            }
            
            error.printError("sorry, due to time constraints, routine overloading is not allowed. Cannot overload " + preexistingEntry.name + routineParameters, ctx);
            return null;
            /*
            FOR THE TIME BEING, I AM DISALLOWING OVERLOADING METHOD NAMES! THIS IS FOR CODE GENERATION PURPOSES
            TODO: FIGURE OUT A WAY TO ALLOW METHOD OVERLOADING IN OBJECT CODE
            RoutineSpec testSpec = preexistingEntry.type.lookupRoutine(routineName, routineParameters);
            
            if(testSpec != null) //if the routine with the same name and parameters already exist, throw an error and return from this
            {
                error.duplicateRoutineError(routineName, routineParameters, ctx);
                return null;
            }
            
            //if there is no error, add the routine's entry to the pre-existing Scope Entry's type (basically, overload the existing routine)
            visit(ctx.typeName());
            TypeSpec returnType = (ctx.typeName() == null)? null : ctx.typeName().type;
            RoutineSpec routineSpec = new RoutineSpec(routineName, routineParameters, returnType);

            preexistingEntry.type.addRoutine(routineSpec);
            
            ctx.routine = routineSpec;
            */
        }
        else if(preexistingType != null) //a type with the same name already exists in the scope
        {
            //TODO: work on constructor stuff
            error.duplicateVariableTypeError(preexistingType.name, ctx);
            return null;
        }
        else //if no routine or identifier in the local scope has the same name, create a new scope entry representing the routine
        {
            visitTypeName(ctx.typeName());
            TypeSpec returnType = (ctx.typeName() == null)? null : ctx.typeName().type;
            RoutineSpec routineSpec = new RoutineSpec(routineName, routineParameters, returnType);
            
            ctx.routine = routineSpec;
            
            //if the routine isn't static, then we can add it to the type's scope. But if it is static, don't add it to the type's scope.
            if(!ctx.isStatic)
            {
                TypeSpec routineType = new TypeSpec(Predefined.ROUTINE);
                
                routineType.addRoutine(routineSpec);
                
                ScopeEntry routineEntry = new ScopeEntry(routineName, routineType);
                localScope.addEntry(routineEntry);
            }
        }

        localScope = new Scope(localScope, Kind.ROUTINE); //enter a new scope with localScope as the parent
        ctx.routineScope = localScope; //sets the routine's scope to the current local scope
        
        addParamsToRoutineScope(ctx.routineParameters());
        visit(ctx.statementList()); //visit the routine's statement list
        
        //if there is a return statement and the routine returns a value
        if(ctx.RETURN() != null && ctx.RETURNS() != null)
        {
            visit(ctx.expression());
            
            //if the expression type or return type is null, then there was some other problem in the semantics pass (the appropriate error messages have already been thrown)
            if(ctx.expression().type != null && ctx.typeName().type != null)
            {
                if(!TypeChecker.areTypesCompatible(ctx.typeName().type, ctx.expression().type))
                    error.typeConversionError(ctx.expression().type, ctx.typeName().type, ctx);
            }
        }
        
        //if there is a return statement, but the routine doesn't return any a value
        else if(ctx.RETURN() != null && ctx.RETURNS() == null)
        {
            error.invalidReturnError(routineName, ctx);
        }
        else if(ctx.RETURN() == null && ctx.RETURNS() != null)
        {
            error.invalidReturnError(routineName, ctx.typeName().getText(), ctx);
        }
        
        localScope = localScope.getParent(); //exist the routine scope to resume parsing in the previous localScope
        
        if(!ctx.routineName(1).getText().equals(routineName)) //if the routine isn't closed properly, throw an error (but the rest of the routine is still good, so don't mess with it)
            error.routineNotClosedError(routineName, ctx.routineName(1).getText(), ctx.routineName(1));
        
        return null;
    }
    
    private boolean checkRoutineTypes(SixtyFortranParser.RoutineParametersContext ctx, SixtyFortranParser.TypeNameContext returnType)
    {
        boolean typesAreValid = true;
        if(ctx != null) //a routine may not have any parameters, so we need to check for that
        {
            //check if all of the parameters have valid types
            for(SixtyFortranParser.RoutineParameterListContext params : ctx.routineParameterList()) //check to make sure all of the types are correct
            {
                visit(params.typeName()); //visit the type name (which also checks for errors)
                
                if(params.typeName().type == null) //if there was an error
                {
                    typesAreValid = false;
                }
            }
        }
        
        //checks return type
        if(returnType != null)
        {
            visit(returnType);
            
            if(returnType.type == null)
                typesAreValid = false;
        }
        
        return typesAreValid;
    }
    
    private ArgumentList createParameterList(SixtyFortranParser.RoutineParametersContext ctx)
    {
        ArgumentList parameters = new ArgumentList();
        
        if(ctx != null) //a routine may not have any parameters, so we need to check for that
        {
            for(SixtyFortranParser.RoutineParameterListContext params : ctx.routineParameterList())
            {
                TypeSpec type = params.typeName().type;
                
                for(SixtyFortranParser.VarNameContext varName : params.varName())
                {
                    parameters.argumentTypes.add(type); //add the type of the variable to the parameter list
                    localScope.addEntry(new ScopeEntry(varName.getText(), type, Kind.PARAMETER)); //adds the parameter to the routine's scope
                }
            }
        }
        
        return parameters;
    }
    
    private void addParamsToRoutineScope(SixtyFortranParser.RoutineParametersContext ctx)
    {
        if(ctx != null) //a routine may not have any parameters, so we need to check for that
        {
            for(SixtyFortranParser.RoutineParameterListContext params : ctx.routineParameterList())
            {
                TypeSpec type = params.typeName().type;
                
                for(SixtyFortranParser.VarNameContext varName : params.varName())
                {
                    localScope.addEntry(new ScopeEntry(varName.getText(), type)); //adds the parameter to the routine's scope
                }
            }
        }
    }
    
    @Override
    public Object visitExpression(SixtyFortranParser.ExpressionContext ctx)
    {
        //check the number of errors caused by evaluating the term
        int startNumErrors = error.getErrorCount();
        visit(ctx.comparison());
        int endNumErrors = error.getErrorCount();
        if(ctx.expression() != null)
            visit(ctx.expression());
        
        //gets the types of the operators. The leftOp may be null even if it was semantically correct because a routine may not return anything (thus the returnType is null)
        TypeSpec leftOp = ctx.comparison().type;
        TypeSpec rightOp = (ctx.expression() == null)? null : ctx.expression().type;
        
        //a comparison needs to return a type, so if the term was semantically correct, but it doesn't cause any types to be returned, then throw an error
        if(leftOp == null && startNumErrors == endNumErrors)
        {
            error.typeReturnFailed(ctx.comparison());
        }
        
        if(leftOp != null && rightOp != null)
        {
            String routineName = Predefined.OPERATOR_AND;
            
            if(ctx.boolOp().AND() != null)
                routineName = Predefined.OPERATOR_OR;
            
            ArgumentList argument = new ArgumentList(rightOp);
            
            RoutineSpec addRoutine = leftOp.lookupRoutine(routineName, argument);
            
            if(addRoutine == null)
                error.routineNotFoundError(routineName, argument, leftOp, ctx);
            else
                ctx.type = addRoutine.returnType;
        }
        else if(leftOp != null && ctx.expression() == null)
        {
            ctx.type = leftOp;
        }
        
        return null;
    }
    
    @Override
    public Object visitComparison(SixtyFortranParser.ComparisonContext ctx)
    {
        //check the number of errors caused by evaluating the term
        int startNumErrors = error.getErrorCount();
        visit(ctx.addition());
        int endNumErrors = error.getErrorCount();
        if(ctx.expression() != null)
            visit(ctx.expression());
        
        //gets the types of the operators. The leftOp may be null even if it was semantically correct because a routine may not return anything (thus the returnType is null)
        TypeSpec leftOp = ctx.addition().type;
        TypeSpec rightOp = (ctx.expression() == null)? null : ctx.expression().type;
        
        //an addition needs to return a type, so if the term was semantically correct, but it doesn't cause any types to be returned, then throw an error
        if(leftOp == null && startNumErrors == endNumErrors)
        {
            error.typeReturnFailed(ctx.addition());
        }
        
        if(leftOp != null && rightOp != null)
        {
            String routineName = Predefined.OPERATOR_EQUALS;
            
            if(ctx.relOp().GREATER_EQUALS() != null)
                routineName = Predefined.OPERATOR_GREATER_EQUALS;
            else if(ctx.relOp().LESS_EQUALS() != null)
                routineName = Predefined.OPERATOR_LESS_EQUALS;
            else if(ctx.relOp().LESS_THAN() != null)
                routineName = Predefined.OPERATOR_LESS_THAN;
            else if(ctx.relOp().GREATER_THAN() != null)
                routineName = Predefined.OPERATOR_GREATER_THAN;
            else if(ctx.relOp().NOT_EQUALS() != null)
                routineName = Predefined.OPERATOR_NOT_EQUALS;
            
            ArgumentList argument = new ArgumentList(rightOp);
            
            RoutineSpec relOpRoutine = leftOp.lookupRoutine(routineName, argument);
            
            if(relOpRoutine == null)
                error.routineNotFoundError(routineName, argument, leftOp, ctx);
            else
                ctx.type = relOpRoutine.returnType;
        }
        else if(leftOp != null && ctx.expression() == null)
        {
            ctx.type = leftOp;
        }
        
        return null;
    }
    
    
    @Override
    public Object visitAddition(SixtyFortranParser.AdditionContext ctx)
    {
        //check the number of errors caused by evaluating the term
        int startNumErrors = error.getErrorCount();
        visit(ctx.multiplication());
        int endNumErrors = error.getErrorCount();
        if(ctx.expression() != null)
            visit(ctx.expression());
        
        //gets the types of the operators. The leftOp may be null even if it was semantically correct because a routine may not return anything (thus the returnType is null)
        TypeSpec leftOp = ctx.multiplication().type;
        TypeSpec rightOp = (ctx.expression() == null)? null : ctx.expression().type;
        
        //a multiplication needs to return a type, so if the term was semantically correct, but it doesn't cause any types to be returned, then throw an error
        if(leftOp == null && startNumErrors == endNumErrors)
        {
            error.typeReturnFailed(ctx.multiplication());
        }
        
        if(leftOp != null && rightOp != null)
        {
            String routineName = Predefined.OPERATOR_PLUS;
            
            if(ctx.addOp().MINUS() != null)
                routineName = Predefined.OPERATOR_MINUS;
            
            ArgumentList argument = new ArgumentList(rightOp);
            
            RoutineSpec addRoutine = leftOp.lookupRoutine(routineName, argument);
            
            if(addRoutine == null)
                error.routineNotFoundError(routineName, argument, leftOp, ctx);
            else
                ctx.type = addRoutine.returnType;
        }
        else if(leftOp != null && ctx.expression() == null)
        {
            ctx.type = leftOp;
        }
        
        return null;
    }
    
    
    @Override
    public Object visitMultiplication(SixtyFortranParser.MultiplicationContext ctx)
    {
        //check the number of errors caused by evaluating the term
        int startNumErrors = error.getErrorCount();
        visit(ctx.term());
        int endNumErrors = error.getErrorCount();
        if(ctx.expression() != null)
            visit(ctx.expression());
        
        //gets the types of the operators. The leftOp may be null even if it was semantically correct because a routine may not return anything (thus the returnType is null)
        TypeSpec leftOp = ctx.term().type;
        TypeSpec rightOp = (ctx.expression() == null)? null : ctx.expression().type;
        
        //a term needs to return a type, so if the term was semantically correct, but it doesn't cause any types to be returned, then throw an error
        if(leftOp == null && startNumErrors == endNumErrors)
        {
            error.typeReturnFailed(ctx.term());
        }
        
        //if both sides of the multiplication are semantically correct
        if(leftOp != null && rightOp != null)
        {
            String routineName = Predefined.OPERATOR_STAR;
            
            if(ctx.mulOp().DIV() != null)
                routineName = Predefined.OPERATOR_SLASH;
            else if(ctx.mulOp().MOD() != null)
                routineName = Predefined.OPERATOR_MOD;
            
            ArgumentList argument = new ArgumentList(rightOp);
            
            RoutineSpec mulRoutine = leftOp.lookupRoutine(routineName, argument);
            
            if(mulRoutine == null)
                error.routineNotFoundError(routineName, argument, leftOp, ctx);
            else
                ctx.type = mulRoutine.returnType;
        }
        //checks that the left op of the multiplication is semantically correct and that there is no right op
        else if(leftOp != null && ctx.expression() == null)
        {
            ctx.type = leftOp;
        }
        
        return null;
    }
    
    @Override
    public Object visitNumTerm(SixtyFortranParser.NumTermContext ctx)
    {
        if(ctx.INTEGER() != null)
        {
            ctx.type = Predefined.intType;
        }
        else
        {
            ctx.type = Predefined.realType;
        }
        
        return null;
    }
    
    @Override
    public Object visitBoolTerm(SixtyFortranParser.BoolTermContext ctx)
    {
        ctx.type = Predefined.boolType;
        
        return null;
    }
    
    @Override
    public Object visitStringTerm(SixtyFortranParser.StringTermContext ctx)
    {
        ctx.type = Predefined.stringType;
        
        return null;
    }
    
    @Override
    public Object visitNotTerm(SixtyFortranParser.NotTermContext ctx)
    {
        visit(ctx.term()); //visit the term to get it's type
        
        TypeSpec termType = ctx.term().type;
        ArgumentList arg = new ArgumentList(termType);
        
        RoutineSpec unaryNot = termType.lookupRoutine(Predefined.UNARY_NOT, arg);
        
        if(unaryNot != null) //if the type has a unary not routine that fits the given arguments, then set the type of the term to the routine's return type
        {
            ctx.type = unaryNot.returnType;
        }
        else //otherwise, just throw an error
        {
            error.routineNotFoundError(Predefined.UNARY_NOT, arg, termType, ctx);
        }
        
        return null;
    }
    
    @Override
    public Object visitMinusTerm(SixtyFortranParser.MinusTermContext ctx)
    {
        visit(ctx.term()); //visit the term to get it's type
        
        TypeSpec termType = ctx.term().type;
        
        if(termType != null)
        {
            ArgumentList arg = new ArgumentList(termType);
            
            RoutineSpec unaryMinus = termType.lookupStaticRoutine(Predefined.UNARY_MINUS, arg);
            
            if(unaryMinus != null) //if the type has a unary not routine that fits the given arguments, then set the type of the term to the routine's return type
            {
                ctx.type = unaryMinus.returnType;
            }
            else //otherwise, just throw an error
            {
                error.routineNotFoundError(Predefined.UNARY_MINUS, arg, termType, ctx);
            }
        }

        return null;
    }
    
    @Override
    public Object visitParenTerm(SixtyFortranParser.ParenTermContext ctx)
    {
        visit(ctx.expression()); //visit the expression to assign it a type
        
        ctx.type = ctx.expression().type;
        
        return null;
    }
    
    @Override
    public Object visitTypeName(SixtyFortranParser.TypeNameContext ctx)
    {
        //the context object may be null (such as in the case of a routine that doesn't have a return statement)
        if(ctx == null)
            return null;
        
        //the simple type name is easy pretty simple
        if(ctx.simpleTypeName() != null)
        {
            ctx.type = localScope.lookupType(ctx.simpleTypeName().getText()); 
        }
        //the routine type name is a little less simple
        else if(ctx.routineTypeName() != null)
        {
           ctx.type = (TypeSpec) visit(ctx.routineTypeName());
        }
        /*
        else if(ctx.genericTypeName() != null)
        {
            //TODO: implement generic type name support
        }
        */
        
        if(ctx.type == null)
            error.typeNotFoundError(ctx.getText(), ctx);
        
        return null;
    }
    
    @Override
    public Object visitRoutineTypeName(SixtyFortranParser.RoutineTypeNameContext ctx)
    {
        TypeSpec type = new TypeSpec(Predefined.ROUTINE);
        
        ArgumentList routineParameters = new ArgumentList();
        
        //visits the type names and assigns them their type
        for(SixtyFortranParser.TypeNameContext typeName : ctx.typeName())
        {
            visit(typeName); 
            //if one of the types cannot be parsed as a type, return null
            if(typeName.type == null)
                return null;
        }
            
        //since the return type is also a typename, there may be one less parameter than the total number of typenames
        int numParameters = (ctx.RETURNS() == null)? ctx.typeName().size() : ctx.typeName().size() - 1;
        
        for(int i = 0; i < numParameters; i++)
            routineParameters.argumentTypes.add(ctx.typeName(i).type);
        
        //if there is a return type, then numParameters is 1 less than the size of the typeName list, which also means it points to the last index (the return type)
        TypeSpec returnType = (ctx.RETURNS() == null)? null : ctx.typeName(numParameters).type;
        
        RoutineSpec routine = new RoutineSpec(Predefined.OPERATOR_PAREN, routineParameters, returnType);
        
        type.addRoutine(routine);
        
        return type;
    }
    
    @Override
    public Object visitCallTerm(SixtyFortranParser.CallTermContext ctx)
    {
        visit(ctx.call());
        ctx.type = ctx.call().type;
        return null;
    }
    
    @Override
    public Object visitCall(SixtyFortranParser.CallContext ctx)
    {
        //you thought this would be where we do most of the semantic checking for a call, but it was me, Dio!
        if(ctx.identifierCall() != null)
        {
            visit(ctx.identifierCall());
            ctx.type = ctx.identifierCall().type;
            ctx.callEntry = ctx.identifierCall().callEntry;
        }
        else if(ctx.routineCall() != null)
        {
            visit(ctx.routineCall());
            ctx.type = ctx.routineCall().type;
            ctx.callEntry = ctx.routineCall().callEntry;
        }
        
        return null;
    }
    
    //-------- Be warned all ye who enter: the following code is ugly and horrible and many other adjectives that are other synonyms of those two words --------\\
    
    @Override
    public Object visitIdentifierCall(SixtyFortranParser.IdentifierCallContext ctx)
    {
        String id = ctx.IDENTIFIER().getText();
        
        //check if the identifier is either a variable (which could also include a routine) or a type 
        ScopeEntry idEntry = localScope.lookupEntry(id);
        TypeSpec typeEntry = localScope.lookupType(id);
        
        //if the identifier points to an object in the scope
        if(idEntry != null)
        {
            //if there is no call after the identifier, then it's just the identifiers type
            if(ctx.call() == null)
            {
                ctx.type = idEntry.type;
                ctx.callEntry = idEntry;
            }
                
            /*
            //TODO: actually implement index helper
            else if(ctx.indexModifier() != null)
                indexHelper(ctx.indexModifier()); 
            */
            //otherwise, we're going down the rabbit hole
            else
            {
                ctx.type = callHelper(idEntry.type, ctx.call(), false);
                ctx.callEntry = ctx.call().callEntry;
            }
                
        }
        //if the identifier points to a type in the scope
        else if(typeEntry != null)
        {   
            /*
            //TODO: actually implement index helper
            if(ctx.indexModifier() != null)
                indexHelper(ctx.indexModifier());
            */
            if(ctx.call() != null)
            {
                ctx.type = callHelper(typeEntry, ctx.call(), true);
                ctx.callEntry = ctx.call().callEntry;
            }
                      
            else
                error.typeCallError(typeEntry.name, ctx);
        }
        //the identifier could not be found
        else
        {
            error.identifierNotDeclaredError(id, ctx);
        }
       
        return null;
    }
    
    @Override
    public Object visitRoutineCall(SixtyFortranParser.RoutineCallContext ctx)
    {
        String id = ctx.IDENTIFIER().getText();

        ArgumentList arguments = argumentListHelper(ctx.argumentList());

        //if there was a problem with the argument list, just return null (don't worry, error messages have already been printed)
        if(arguments == null)
            return null;

        //check if the identifier is either a variable (which could also include a routine) or a type 
        ScopeEntry idEntry = localScope.lookupEntry(id);
        TypeSpec typeEntry = localScope.lookupType(id);
        
        //if the identifier points to an object (like a variable, field, or routine) in the scope
        if(idEntry != null)
        {
            String routineName = idEntry.name;
            RoutineSpec routineSpec;
            
            //the names of for the routineSpec in a ROUTINE type (or a print or read type) are either OPERATOR_PAREN or the name of the routine itself. I know it's janky. Too bad.
            if(idEntry.type.name.equals(Predefined.ROUTINE) || idEntry.type == Predefined.printType || idEntry.type == Predefined.readType)
            {
                routineSpec = idEntry.type.lookupRoutine(routineName, arguments);
                
                if(routineSpec == null)
                {
                    routineName = Predefined.OPERATOR_PAREN;
                    routineSpec = idEntry.type.lookupRoutine(routineName, arguments);
                }
                
            }
            else //if we're not dealing with a routine, then we can check stuff normally
            {
                routineSpec = idEntry.type.lookupRoutine(routineName, arguments);
            }
            
            //only continue if the routine has been found
            if(routineSpec != null)
            {
                //if there is no call after the routine, then it's just the routine's return type and the scope entry is null (cannot do an assignment if there is no scope entry)
                if(ctx.call() == null)
                    ctx.type = routineSpec.returnType;
                /*
                //TODO: actually implement index helper
                else if(ctx.indexModifier() != null)
                    indexHelper(ctx.indexModifier()); 
                */
                //otherwise, we're going down the rabbit hole
                else
                {
                    ctx.type = callHelper(routineSpec.returnType, ctx.call(), false);
                    ctx.callEntry = ctx.call().callEntry;
                }
                    
            }
            else //the the idEntry's type does not have a routine matching the arguments
            {
                error.routineNotFoundError(routineName, arguments, idEntry.type, ctx);
            }
        }
        //if the identifier points to a type in the scope
        else if(typeEntry != null)
        {
            //TODO: implement constructor support
            RoutineSpec routineSpec = typeEntry.lookupStaticRoutine(Predefined.OPERATOR_PAREN, arguments);
            
            if(routineSpec != null)
            {
                /*
                //TODO: actually implement index helper
                if(ctx.indexModifier() != null)
                    indexHelper(ctx.indexModifier());
                */
                
                if(ctx.call() != null)
                {
                    ctx.type = callHelper(routineSpec.returnType, ctx.call(), false);
                    ctx.callEntry = ctx.call().callEntry;
                }
                //if there is no call after the first routine call, then there is no ScopeEntry for us to attempt to make an assignment
                else
                    ctx.type = routineSpec.returnType;
            }
            else //the routine matching the arguments could not be found for the type
            {
                error.routineNotFoundError(Predefined.OPERATOR_PAREN, arguments, typeEntry, ctx);
            }
            
        }
        else //the identifier could not be found
        {
            error.identifierNotDeclaredError(id, ctx);
        }
       
        return null;
    }
    
    //za warudo
    private TypeSpec callHelper(TypeSpec callerType, SixtyFortranParser.CallContext call, boolean staticCall)
    {
        //XXX: when we extend this so that types can contain other types, we'll need to modify this function. 
        String idName;
        
        if(call.identifierCall() != null)
            idName = call.identifierCall().IDENTIFIER().getText();
        else 
            idName = call.routineCall().IDENTIFIER().getText();

        //the id could be referring to a variable or it could be referring to a routine, so we'll have to lookup both
        ScopeEntry field;
        ScopeEntry routine;

        if(staticCall)
        {
            field = callerType.lookupStaticField(idName);
            routine = callerType.lookupStaticRoutine(idName);
        }
        else
        {
            field = callerType.lookupField(idName);
            routine = callerType.lookupRoutine(idName);
        }
        
        if(call.identifierCall() != null)
        {   
            TypeSpec callType = identifierCallHelper(field, routine, call.identifierCall());
            call.callEntry = call.identifierCall().callEntry;
            return callType;
        }
        if(call.routineCall() != null)
        {
            ArgumentList arguments = argumentListHelper(call.routineCall().argumentList());
            TypeSpec callType = routineCallHelper(field, routine, arguments, call.routineCall());
            call.callEntry = call.routineCall().callEntry;
            return callType;
        }
        
        //if we've gotten this far in the helper's execution, then it means we couldn't find any sort of match for the identifier
        error.identifierNotDeclaredError(idName, callerType.name, call);
        return null;
    }
    
    private TypeSpec identifierCallHelper(ScopeEntry field, ScopeEntry routine, SixtyFortranParser.IdentifierCallContext call)
    {
        //this means we are calling either a static or regular variable
        if(field != null)
        {
            //if there is another call, keep going down the rabbit hole
            if(call.call() != null)
            {
                TypeSpec callType = callHelper(field.type, call.call(), false);
                call.callEntry = call.call().callEntry;
                return callType;
            }
                
            //otherwise, just return the type of the field
            call.callEntry = field;
            return field.type;
        }
        else if(routine != null)
        {
            //if there is another call, keep going (although currently, routines don't really have any more calls to make)
            if(call.call() != null)
            {
                TypeSpec callType = callHelper(routine.type, call.call(), false);
                call.callEntry = call.call().callEntry;
                return callType;
            }
            
            call.callEntry = routine;
            return routine.type;
        }
        
        return null;
    }
    
    private TypeSpec routineCallHelper(ScopeEntry field, ScopeEntry routine, ArgumentList arguments, SixtyFortranParser.RoutineCallContext call)
    {
        RoutineSpec routineSpec = null;
        String routineName = Predefined.OPERATOR_PAREN;
        
        //this means we are doing something like x(5), or in other words, the only valid routine name is OPERATOR_PAREN
        if(field != null)
        {
            routineSpec = field.type.lookupRoutine(routineName, arguments);
            
            if(routineSpec != null)
            {
                //if there is another call, keep going down the rabbit hole
                if(call.call() != null)
                {
                    TypeSpec callType = callHelper(routineSpec.returnType, call.call(), false);
                    call.callEntry = call.call().callEntry;
                    return callType;
                }
                
                //otherwise, just return the return type of the routine (there is no valid scope entry in this instance)
                return routineSpec.returnType;
            }
            else
            {
                error.routineNotFoundError(routineName, arguments, field.type, call);
            }
            
        }
        //this means we are performing an actual routine call like do_stuff(5) so the routine name could be do_stuff or it could be OPERATOR_PAREN
        else if(routine != null)
        {
            routineSpec = routine.type.lookupRoutine(routineName, arguments);
            
            if(routineSpec == null)
            {
                routineName = routine.name;
                routineSpec = routine.type.lookupRoutine(routineName, arguments);
            }
            
            //we have to check again because it may be that the routine name was like do_stuff
            if(routineSpec != null)
            {
                //keep going down the rabbit hole
                if(call.call() != null)
                {
                    TypeSpec callType = callHelper(routineSpec.returnType, call.call(), false);
                    call.callEntry = call.call().callEntry;
                    return callType;
                }
                    
                return routineSpec.returnType;
            }
        }
        
        return null;
    }
    
    private ArgumentList argumentListHelper(SixtyFortranParser.ArgumentListContext ctx)
    {
        ArgumentList arguments = new ArgumentList();
        
        if(ctx != null)
        {
            visitChildren(ctx); //visit all of the expressions in the argument list
            
            for(SixtyFortranParser.ExpressionContext exp : ctx.expression())
            {
                //if there was a problem with any of the types, return null (don't worry, the error message has already been printed)
                if(exp.type == null)
                    return null;
                
                arguments.argumentTypes.add(exp.type);
            }
            
            ctx.args = arguments;
        }
        
        return arguments;
    }
}


