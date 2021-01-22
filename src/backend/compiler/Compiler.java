package backend.compiler;

import antlr4.SixtyFortranBaseVisitor;
import antlr4.SixtyFortranParser;

import org.antlr.v4.runtime.tree.ParseTree;
import backend.semantics.CompilerSemantics;
import backend.semantics.FileSpec;
import intermediate.Kind;
import intermediate.Predefined;
import intermediate.scope.Scope;
import intermediate.scope.ScopeEntry;
import intermediate.type.ArgumentList;
import intermediate.type.RoutineSpec;
import intermediate.type.TypeSpec;

public class Compiler extends SixtyFortranBaseVisitor<Object>
{
    private Scope worldScope;
    private Scope localScope;
    
    private CompilerSemantics semantics;
    private CodeGenerator code;
    
    private FileSpec currentFile;
    private String programName;

    public Compiler(Scope worldScope, String programName)
    {
        this.worldScope = worldScope;
        this.localScope = worldScope;
       
        semantics = new CompilerSemantics(worldScope, programName);
        code = new CodeGenerator();
        
        currentFile = null;
        this.programName = programName;
    }
    
    @Override
    public Object visit(ParseTree tree)
    {
       semantics.visit(tree);
       //return null;
       return super.visit(tree);
    }
    
    @Override
    public Object visitProgramBody(SixtyFortranParser.ProgramBodyContext ctx)
    {
        //moves the predefined .class files to the library folder
        Predefined.movePredefinedTypes(semantics.getBinFolderName());
        
        //creates the routine file
        FileSpec routineFile = new FileSpec(Predefined.ROUTINE);
        routineFile.directory = semantics.getBinFolderName();
        routineFile.mkFile();
        code.setCurrentFile(routineFile);
        code.emitRoutineFile(semantics.getRoutineSignatures());
        
        //starts emitting the actual program
        enterFile((FileSpec) semantics.visit(ctx), localScope);
        
        code.emitMainPrologue(programName);
        
        code.emitLocalVars(worldScope);
        
        Predefined.emitPredefinedRoutines(code);
        
        visitChildren(ctx);
        code.setCurrentFile(currentFile);
        
        code.emitMainEpilogue(worldScope.getNumLocals());
        
        code.close();
       
        return null;
    }
    
    @Override
    public Object visitIfStatement(SixtyFortranParser.IfStatementContext ctx) {
        //TODO: figure out what the hell is going on here
        if (ctx.elseifBlock() == null && ctx.elseBlock() == null) {
            // Simple if block
            Label conditional_end = new Label(); // Label for the end of conditional
            visit(ctx.ifBlock().expression()); // Code to evaluate expression
            code.emitBoolValue();
            code.emit("ifeq", conditional_end); // If equal, do statements below. Else, jump to end of
                                                            // conditional
            localScope = ctx.ifBlock().scope;
            code.setCurrentScope(localScope);
            visit(ctx.ifBlock().statementList()); // Code for the if block
            localScope = localScope.getParent();
            code.setCurrentScope(localScope);
            
            code.emitLabel(conditional_end); // End of conditional block (if)
           
        } else if (ctx.elseifBlock() == null) {
            // Simple if/else statement
            Label conditional_end = new Label(); // Label for the end of conditional
            visit(ctx.ifBlock().expression()); // Code to evaluate boolean expression in if
            code.emitBoolValue();
            Label else_label = new Label(); // Else label
            code.emit("ifeq", else_label); // If equal, do statements below. Else, jump to else block.
            
            localScope = ctx.ifBlock().scope;
            code.setCurrentScope(localScope);
            visit(ctx.ifBlock().statementList()); // Code for the if block
            localScope = localScope.getParent();
            code.setCurrentScope(localScope);
            
            code.emit("goto", conditional_end); // If condition is true and executed if block, jump to end of conditional
            code.emitLabel(else_label); // Print else block label (jump here if condition is not true)
            
            localScope = ctx.elseBlock().scope;
            code.setCurrentScope(localScope);
            visit(ctx.elseBlock().statementList()); // Code for the else statement
            localScope = localScope.getParent();
            code.setCurrentScope(localScope);
            
            code.emitLabel(conditional_end); // End of conditional block (if/else)
        } else {
            // if/(else if)+/else statement
            Label conditional_end = new Label(); // Label for the end of conditional
            visit(ctx.ifBlock().expression()); // ***Code to evaluate boolean expression in if
            code.emitBoolValue();
            Label next_label = new Label(); // Else label (or else if)
            code.emit("ifeq", next_label); // ***If equal, do statements below. Else, jump to next block (else if)
            
            localScope = ctx.ifBlock().scope;
            code.setCurrentScope(localScope);
            visit(ctx.ifBlock().statementList()); // Code for the if block
            localScope = localScope.getParent();
            code.setCurrentScope(localScope);
            
            code.emit("goto", conditional_end); // ***If condition is true and executed if block, jump to end of conditional
            // Print out all elseif statements
            for (int i = 0; i < ctx.elseifBlock().size(); i++) {
                code.emitLabel(next_label); // ***If if condition false, jump here
                visit(ctx.elseifBlock(i).expression()); // ***Code to evaluate boolean expression in elseif
                code.emitBoolValue();
                next_label = new Label(); // Else label (or else if)
                code.emit("ifeq", next_label); // ***If equal, do statements below. Else, jump to next block (else if)
                
                localScope = ctx.elseifBlock(i).scope;
                code.setCurrentScope(localScope);
                visit(ctx.elseifBlock(i).statementList()); // ***Code for the if block
                localScope = localScope.getParent();
                code.setCurrentScope(localScope);
                
                code.emit("goto", conditional_end); // ***If condition is true and executed if block, jump to end of
                                                // conditional
            }
            // If any of the blocks were executed, the would jump to conditional_end
            // next_label here is reached if none of the blocks were executed above
            // At next_label, we can either end the condition if no else block. If else
            // block, execute statements.
            code.emitLabel(next_label); // Print else block label (jump here if condition is not true)
            // Else statement below (if not null)
            if (ctx.elseifBlock() != null)
            {
                localScope = ctx.elseBlock().scope;
                code.setCurrentScope(localScope);
                visit(ctx.elseBlock().statementList()); // Code for the else statement
                localScope = localScope.getParent();
                code.setCurrentScope(localScope);
            }
           
            code.emitLabel(conditional_end); // End of conditional block (if/else)
        }
        return null; // If statement is a statement and does not return anything
    }
    
    @Override
    public Object visitLoopStatement(SixtyFortranParser.LoopStatementContext ctx) {
        //TODO: figure out how the hell to fix this
        Label loopTopLabel = new Label();
        Label loopExitLabel = new Label();
        code.emitLabel(loopTopLabel);
        visit(ctx.expression());
        code.emitBoolValue();
        code.emit("ifeq", loopExitLabel);
        visit(ctx.statementList());
        code.emit("goto", loopTopLabel);
        code.emitLabel(loopExitLabel);
        return null; // While loop is a statement is a statement and does not return anything
    }
    
    @Override
    public Object visitTypeDefinition(SixtyFortranParser.TypeDefinitionContext ctx)
    {
        //sets the nonlocal (aka static) fields for the type
        for(ScopeEntry nonlocalEntry : ctx.typeScope.getNonlocalEntries())
        {
            ScopeEntry entry = localScope.lookupEntry(nonlocalEntry.name);
            code.emitLoadEntry(entry);
            
            String path = entry.type.getPath();
            if(entry.type.name.equals(Predefined.ROUTINE))
                path = Predefined.routineType.getPath();
            
            code.emit("putstatic " + ctx.type.getPath() + "/" + entry.name + " L" + path + ";");
        }
        //handles passing nonlocals to routines
        code.emit("invokestatic " + ctx.type.getPath() + "/" + "$staticInitialization()V");
        
        enterFile(ctx.file, ctx.typeScope);
        localScope = ctx.typeScope;
        
        code.emitClass(ctx.type, null, localScope);
        code.emitClassStaticInitialization(ctx, this);
        
        for(SixtyFortranParser.RoutineDefinitionContext routineCtx : ctx.definitionBlock().routineBlock().routineDefinition())
        {
            routineDefinitionHelper(routineCtx, ctx.type, false);
        }
        
        for(SixtyFortranParser.StaticRoutineDefinitionContext routineCtx : ctx.definitionBlock().routineBlock().staticRoutineDefinition())
        {
            routineDefinitionHelper(routineCtx.routineDefinition(), ctx.type, true);
        }

        localScope = localScope.getParent();
        exitFile(localScope);
        return null;
    }
    
    @Override
    public Object visitAssignExistingVariable(SixtyFortranParser.AssignExistingVariableContext ctx)
    {
        ArgumentList arg = new ArgumentList(ctx.expression().type);
        RoutineSpec assignRoutine = ctx.call().type.lookupRoutine(Predefined.OPERATOR_ASSIGN, arg);
        
        //TODO: work on this
        //if we can use the assignment operator
        if(assignRoutine != null)
        {
            visit(ctx.call());
            code.emitLoadEntry(ctx.call().type.lookupRoutine(Predefined.OPERATOR_ASSIGN));
            visit(ctx.expression());
            code.emitRoutineCall(Predefined.OPERATOR_ASSIGN, ctx.call().type, arg, assignRoutine.returnType, false);
        }
        else
        {
            visit(ctx.call());
            visit(ctx.expression());
            code.emitLoadEntry(ctx.call().callEntry);
        }
        
        
        return null;
    }
    
    @Override
    public Object visitAssignNewVariables(SixtyFortranParser.AssignNewVariablesContext ctx)
    {
        visit(ctx.expression());
        for(int i = 0; i < ctx.varDeclaration().entries.size() - 1; i++)
            code.emit("dup");
        
        for(ScopeEntry entry : ctx.varDeclaration().entries)
        {
            code.emitLoad(entry.slotNumber);
        }
        
        return null;
    }
    @Override
    public Object visitVarDeclaration(SixtyFortranParser.VarDeclarationContext ctx)
    {
        for(ScopeEntry entry : ctx.entries)
        {
            code.emitVariableDec(entry);
        }
        return null;
    }
    
    @Override
    public Object visitRoutineDefinition(SixtyFortranParser.RoutineDefinitionContext ctx)
    {
        routineDefinitionHelper(ctx, null, ctx.isStatic);
        
        return null;
    }
    
    private void routineDefinitionHelper(SixtyFortranParser.RoutineDefinitionContext ctx, TypeSpec type, boolean isStatic)
    {
        enterFile(ctx.file, ctx.routineScope);
        
        ScopeEntry routineEntry;
        if(isStatic)
        {
            routineEntry = type.lookupStaticRoutine(ctx.routine.name);
            routineEntry.type.setPath(type.getPath() + "$" + ctx.routine.name);
        }
        else
            routineEntry = localScope.lookupEntry(ctx.routine.name);
        
        if(type != null && !isStatic)
            routineEntry.type.addField(new ScopeEntry("type", type));
        
        localScope = ctx.routineScope;
        
        //TODO: figure out what nonlocals we need
        
        if(type == null)
            code.emitConstructor(routineEntry.type.getPath(), Predefined.routineType.getPath());
        else
        {
            code.emitClass(routineEntry.type, Predefined.routineType.getPath(), localScope);
        }
        
        //emit all of the methods for the routine object
        //TODO: figure out how to do routine overloading
        code.emitRoutineHeader(ctx, routineEntry);
        visitChildren(ctx);
        code.emitRoutineCloser(ctx);
        
        localScope = localScope.getParent();
        exitFile(localScope);
    }
    
    @Override
    public Object visitAddition(SixtyFortranParser.AdditionContext ctx)
    {
        visitChildren(ctx);
        
        if(ctx.addOp() != null)
        {
            String routineName = Predefined.OPERATOR_PLUS;
            if(ctx.addOp().MINUS() != null)
                routineName = Predefined.OPERATOR_MINUS;
            
            ArgumentList arg = new ArgumentList(ctx.expression().type);
            TypeSpec returnType = ctx.type.lookupRoutine(routineName, arg).returnType;
            
            code.emitRoutineCall(routineName, ctx.multiplication().type, arg, returnType, false);
        }
        
        return null;
    }
    
    @Override
    public Object visitMultiplication(SixtyFortranParser.MultiplicationContext ctx)
    {
        visitChildren(ctx);
        
        if(ctx.mulOp() != null)
        {
            String routineName = Predefined.OPERATOR_STAR;
            if(ctx.mulOp().DIV() != null)
                routineName = Predefined.OPERATOR_SLASH;
            else if(ctx.mulOp().MOD() != null)
                routineName = Predefined.OPERATOR_MOD;
            
            ArgumentList arg = new ArgumentList(ctx.expression().type);
            TypeSpec returnType = ctx.type.lookupRoutine(routineName, arg).returnType;
            
            code.emitRoutineCall(routineName, ctx.term().type, arg, returnType, false);
        }
        
        return null;
    }
    
    @Override
    public Object visitStringTerm(SixtyFortranParser.StringTermContext ctx)
    {
        code.emit("new " + Predefined.stringType.getPath());
        code.emit("dup");
        code.emit("ldc " + ctx.STRING().getText());
        code.emit("invokespecial " + Predefined.stringType.getPath() + "/<init>(Ljava/lang/String;)V");
        
        return null;
    }
    
    @Override
    public Object visitNumTerm(SixtyFortranParser.NumTermContext ctx)
    {
        if(ctx.INTEGER() != null)
        {
            code.emit("new " + Predefined.intType.getPath());
            code.emit("dup");
            code.emit("ldc " + ctx.INTEGER().getText());
            code.emit("invokespecial " + Predefined.intType.getPath() + "/<init>(I)V");
        }
        else
        {
            code.emit("new " + Predefined.realType.getPath());
            code.emit("dup");
            code.emit("ldc " + ctx.REAL().getText());
            code.emit("invokespecial " + Predefined.realType.getPath() + "/<init>(D)V");
        }
        
        
        return null;
    }
    
    @Override
    public Object visitBoolTerm(SixtyFortranParser.BoolTermContext ctx)
    {
        code.emit("new " + Predefined.boolType.getPath());
        code.emit("dup");
        
        if(ctx.TRUE() != null)
            code.emit("iload_1");
        else
            code.emit("iload_0");
        
        code.emit("invokespecial " + Predefined.boolType.getPath() + "/<init>(I)V");
        return null;
    }
    //-------- https://www.youtube.com/watch?v=-1qju6V1jLM --------\\
    
    
    @Override
    public Object visitIdentifierCall(SixtyFortranParser.IdentifierCallContext ctx)
    {
        String id = ctx.IDENTIFIER().getText();
        
        ScopeEntry entry = localScope.lookupEntry(id);
        TypeSpec typeEntry = localScope.lookupType(id);
        
        if(entry != null)
        {
            //if we're calling a field and the parent scope is a type, then that means we are in a routine accessing one of the types field and thus we need to load a ref to that instance first
            if(entry.kind == Kind.FIELD && localScope.getParent().getKind() == Kind.TYPE)
            {
                code.emitLoad(0);
                code.emit("getfield " + currentFile.getNameNoExtension() + "/type" + " L" + entry.instanceType.getPath() + ";");
            }
            code.emitLoadEntry(entry);
            
            if(ctx.call() != null)
            {
                callHelper(ctx.call(), entry.type, false);
            }
        }
        else if(typeEntry != null)
        {
            //we know that we're calling a static field, so we don't need to emit anything
            callHelper(ctx.call(), typeEntry, true);
        }
       
        code.emit();
        return null;
    }
    
    @Override
    public Object visitRoutineCall(SixtyFortranParser.RoutineCallContext ctx)
    {
        String id = ctx.IDENTIFIER().getText();
        
        ScopeEntry entry = localScope.lookupEntry(id);
        TypeSpec staticType = localScope.lookupType(id);
        
        String routineName = Predefined.OPERATOR_PAREN;
        
        ArgumentList routineArgs = (ctx.argumentList() == null)? new ArgumentList() : ctx.argumentList().args;
        
        if(entry != null)
        {
            TypeSpec entryType = entry.type;

            RoutineSpec routine = entryType.lookupRoutine(routineName, routineArgs);
            if(routine == null) //basically, the routineSpec's name may be OPERATOR_PAREN, or it could be the name of the routine itself. I don't know if I've fixed this. 
                routine = entryType.lookupRoutine(entryType.name, routineArgs);
            
            //TODO: apparently routine can still be null?
            TypeSpec returnType = null;
            
            //if we're calling a field and the parent scope is a type, then that means we are in a routine accessing one of the types field and thus we need to load a ref to that instance first
            if(entry.kind == Kind.FIELD && localScope.getParent().getKind() == Kind.TYPE)
            {
                code.emitLoad(0);
                code.emit("getfield " + currentFile.getNameNoExtension() + "/type" + " L" + entry.instanceType.getPath() + ";");
            }
            code.emitLoadEntry(entry);
            
            //emit the code to push the arguments on the stack
            if(ctx.argumentList() != null)
                visit(ctx.argumentList());
            
            code.emitRoutineCall(routineName, entryType, routineArgs, returnType, false);            
        }
        else if(staticType != null)
        {
            TypeSpec returnType = staticType.lookupStaticRoutine(routineName, routineArgs).returnType;
            
            //emit the code to push the arguments on the stack
            if(ctx.argumentList() != null)
                visit(ctx.argumentList());
            
            code.emitRoutineCall(routineName, staticType, routineArgs, returnType, true);
            
            if(ctx.call() != null)
            {
                callHelper(ctx.call(), returnType, true);
            }
        }
        
        code.emit();
        return null;
    }
    
    private void callHelper(SixtyFortranParser.CallContext ctx, TypeSpec parentType, boolean isStatic)
    {
        if(ctx.identifierCall() != null)
            idCallHelper(ctx.identifierCall(), parentType, isStatic);
        else if(ctx.routineCall() != null)
            routineCallHelper(ctx.routineCall(), parentType, isStatic);
    }
    
    private void idCallHelper(SixtyFortranParser.IdentifierCallContext ctx, TypeSpec parentType, boolean isStatic)
    {
        String id = ctx.IDENTIFIER().getText();
        
        ScopeEntry entry;
        ScopeEntry routineEntry;
        
        if(isStatic)
        {
            entry = parentType.lookupStaticField(id);
            routineEntry = parentType.lookupStaticRoutine(id);
        }
        else
        {
            entry = parentType.lookupField(id);
            routineEntry = parentType.lookupRoutine(id);
        }
        
        if(entry != null)
        {
            code.emitLoadEntry(entry);
            
            if(ctx.call() != null)
            {
                callHelper(ctx.call(), entry.type, false);
            }
        }
        else if(routineEntry != null)
        {
            code.emitLoadEntry(routineEntry);
            
            if(ctx.call() != null)
            {
                callHelper(ctx.call(), routineEntry.type, false);
            }
        }
    }
    
    private void routineCallHelper(SixtyFortranParser.RoutineCallContext ctx, TypeSpec parentType, boolean isStatic)
    {
        String id = ctx.IDENTIFIER().getText();
        
        ScopeEntry entry;
        
        if(isStatic)
            entry = parentType.lookupStaticRoutine(id);
        else
            entry = parentType.lookupRoutine(id);
        
        ArgumentList args = new ArgumentList();
        if(ctx.argumentList() != null)
            args = ctx.argumentList().args;
        
        RoutineSpec routine = entry.type.lookupRoutine(entry.name, args);
        if(routine == null)
            routine = entry.type.lookupRoutine(Predefined.OPERATOR_PAREN, args);
        
        //since the parent type doesn't store the path to the routine, we need to construct it ourself
        String routinePath = Predefined.routineType.getPath();
        
        if(isStatic)
            code.emit("getstatic " + parentType.getPath() + "/" + id + " L" + routinePath + ";");
        else
            code.emit("getfield " + parentType.getPath() + "/" + id + " L" + routinePath + ";");
        
        code.emitRoutineCall(Predefined.OPERATOR_PAREN, entry.type, args, routine.returnType, false);
    }
    
    private void enterFile(FileSpec file, Scope scope)
    {
        currentFile = file;
        code.setCurrentFile(file);
        code.setCurrentScope(scope);
    }
    
    private void exitFile(Scope newScope)
    {
        currentFile = currentFile.parent;
        code.setCurrentFile(currentFile);
        code.setCurrentScope(newScope);
    }
            
}
