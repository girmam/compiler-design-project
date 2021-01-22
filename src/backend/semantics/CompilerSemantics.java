package backend.semantics;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.antlr.v4.runtime.tree.ParseTree;

import antlr4.SixtyFortranBaseVisitor;
import antlr4.SixtyFortranParser;
import intermediate.Kind;
import intermediate.Predefined;
import intermediate.scope.Scope;
import intermediate.scope.ScopeEntry;
import intermediate.type.ArgumentList;
import intermediate.type.RoutineSpec;
import intermediate.type.TypeSpec;


//It's about 5 days until our presentation and I don't currently have any way to generate object code, so I'm in a bit of a rush. Good luck trying to understand any of this
public class CompilerSemantics extends SixtyFortranBaseVisitor<Object>
{
    private Scope localScope;
    
    private FileSpec mainFile;
    private FileSpec currentFile;
    
    private String bin_folder;
    
    private boolean filesGenerated;
    private HashSet<RoutineSpec> routineSignatures;
    
    public CompilerSemantics(Scope scope, String programName)
    {
        this.localScope = scope;
        this.bin_folder = programName + "_bin/";
        
        mainFile = new FileSpec(programName);
        mainFile.kind = Kind.MAIN;
        
        filesGenerated = false;
        routineSignatures = new HashSet<>();
    }
    
    public Set<RoutineSpec> getRoutineSignatures()
    {
        return routineSignatures;
    }
    
    public String getBinFolderName()
    {
        return bin_folder;
    }
    
    @Override
    public Object visit(ParseTree tree)
    {
        FileSpec f = (FileSpec) super.visit(tree);
        
        return f;
    }
    
    @Override
    public Object visitProgramBody(SixtyFortranParser.ProgramBodyContext ctx)
    {
        currentFile = mainFile;
        //we make the main file in the same folder as the bin file, but we say that the main file's directory is main_bin so that all of the other files are created there
        currentFile.mkFile();
        new File(bin_folder).mkdir();
        
        visitChildren(ctx);
        
        filesGenerated = true;
        return mainFile;
    }
    
    @Override
    public Object visitTypeDefinition(SixtyFortranParser.TypeDefinitionContext ctx)
    {
        FileSpec typeFile;
        
        if(!filesGenerated)
        {
            typeFile = new FileSpec(ctx.type.name, Kind.TYPE, currentFile);
            typeFile.directory = bin_folder; 
            ctx.file = typeFile;
        }
        else
            typeFile = ctx.file;
            
        //sets the path to lookup the type's class file
        ctx.type.setPath(typeFile.getNameNoExtension());
        
        if(!filesGenerated)
            new File(typeFile.directory).mkdirs();
        
        currentFile = typeFile;
        
        localScope = ctx.typeScope;
        visitChildren(ctx);
        localScope = localScope.getParent();
        
        currentFile = typeFile.parent;
        
        if(!filesGenerated)
            typeFile.mkFile();
        
        return typeFile;
    }
    
    @Override
    public Object visitRoutineDefinition(SixtyFortranParser.RoutineDefinitionContext ctx)
    {
        FileSpec routineFile;
        if(!filesGenerated)
        {
            routineFile = new FileSpec(ctx.routine.name, Kind.ROUTINE, currentFile);
            routineFile.directory = bin_folder;
            ctx.file = routineFile;
        }
        else
            routineFile = ctx.file;
        
        
        //sets the path to the routine's class file
        if(!ctx.isStatic)
            localScope.lookupEntry(ctx.routine.name).type.setPath(routineFile.getNameNoExtension());

        currentFile = routineFile;
        
        //TODO: go over this again
        
        localScope = ctx.routineScope;
        
        visitChildren(ctx);
        
        localScope = localScope.getParent();
        
        currentFile = routineFile.parent;
        
        if(!filesGenerated)
            routineFile.mkFile();
        
        return routineFile;
    }
    
    @Override
    public Object visitIfBlock(SixtyFortranParser.IfBlockContext ctx)
    {
        FileSpec ifFile = new FileSpec("inner" + ctx.scope.getScopeNum(), Kind.INNER, currentFile);
        currentFile = ifFile;
        ctx.file = currentFile;
        
        localScope = ctx.scope;
        
        visitChildren(ctx);
        
        localScope = localScope.getParent();
        
        currentFile = ifFile.parent;
        return null;
    }
    
    @Override
    public Object visitElseifBlock(SixtyFortranParser.ElseifBlockContext ctx)
    {
        FileSpec ifFile = new FileSpec("inner" + ctx.scope.getScopeNum(), Kind.INNER, currentFile);
        currentFile = ifFile;
        ctx.file = currentFile;
        
        localScope = ctx.scope;
        
        visitChildren(ctx);
        
        localScope = localScope.getParent();
        
        currentFile = ifFile.parent;
        return null;
    }
    
    @Override
    public Object visitElseBlock(SixtyFortranParser.ElseBlockContext ctx)
    {
        FileSpec ifFile = new FileSpec("inner" + ctx.scope.getScopeNum(), Kind.INNER, currentFile);
        currentFile = ifFile;
        ctx.file = currentFile;
        
        localScope = ctx.scope;
        
        visitChildren(ctx);
        
        localScope = localScope.getParent();
        
        currentFile = ifFile.parent;
        return null;
    }

    @Override
    public Object visitLoopStatement(SixtyFortranParser.LoopStatementContext ctx)
    {        
        FileSpec loopFile = new FileSpec("inner" + ctx.scope.getScopeNum(), Kind.INNER, currentFile);
        
        currentFile = loopFile;
        ctx.file = currentFile;
        
        localScope = ctx.scope;
        
        visitChildren(ctx);
        
        localScope = localScope.getParent();
        
        currentFile = loopFile.parent;
        
        return null;
    }
    
    //-------- ZA WARUDO --------\\
    
    @Override
    public Object visitIdentifierCall(SixtyFortranParser.IdentifierCallContext ctx)
    {
        String id = ctx.IDENTIFIER().getText();
        
        ScopeEntry localEntry = localScope.lookupEntryNoParent(id);
        ScopeEntry nonlocalEntry = localScope.lookupEntry(id);
        TypeSpec type = localScope.lookupType(id);
        
        if(localEntry == null && nonlocalEntry != null)
        {
            ScopeEntry newLocalEntry = new ScopeEntry(id, nonlocalEntry.type);
            
            //if we're in a routine that belongs to a type, we can access it's fields
            if(localScope.getParent().getKind() == Kind.TYPE && localScope.getParent().lookupEntryNoParent(id) != null)
            {
                newLocalEntry.kind = Kind.FIELD;
                newLocalEntry.instanceType = nonlocalEntry.instanceType;
                localScope.addEntry(newLocalEntry);
            }
            else
            {
                newLocalEntry.kind = Kind.NONLOCAL;
                localScope.addNonlocalEntry(newLocalEntry);
            }
        }
        
        if(nonlocalEntry != null && ctx.call() != null)
            callHelper(ctx.call(), nonlocalEntry.type, false);
        else if(type != null && ctx.call() != null)
            callHelper(ctx.call(), type, true);            
        
        return null;
    }
    
    @Override
    public Object visitRoutineCall(SixtyFortranParser.RoutineCallContext ctx)
    {
        if(ctx.argumentList() != null)
            visit(ctx.argumentList());
        
        String id = ctx.IDENTIFIER().getText();
        
        ScopeEntry localEntry = localScope.lookupEntryNoParent(id);
        ScopeEntry nonlocalEntry = localScope.lookupEntry(id);
        TypeSpec type = localScope.lookupType(id);
        
        if(localEntry == null && nonlocalEntry != null)
        {
            ScopeEntry newLocalEntry = new ScopeEntry(id, nonlocalEntry.type);
            //if we're in a routine that belongs to a type, we can access it's fields
            if(localScope.getParent().getKind() == Kind.TYPE && localScope.getParent().lookupEntryNoParent(id) != null)
            {
                newLocalEntry.kind = Kind.FIELD;
                localScope.addNonlocalEntry(newLocalEntry);
            }
            else
            {
                newLocalEntry.kind = Kind.NONLOCAL;
                localScope.addNonlocalEntry(newLocalEntry);
            }            
        }
        
        //adds the needed routine signatures to Routine
        ArgumentList arguments;
        if(ctx.argumentList() != null)
            arguments = ctx.argumentList().args;
        else
            arguments = new ArgumentList();
        
        if(nonlocalEntry != null)
        {
            RoutineSpec routine = nonlocalEntry.type.lookupRoutine(nonlocalEntry.name, arguments);
            
            if(routine == null)
                routine = nonlocalEntry.type.lookupRoutine(Predefined.OPERATOR_PAREN, arguments);

           // putRoutineSignature(new RoutineSpec(Predefined.OPERATOR_PAREN, arguments, routine.returnType));
            
            if(ctx.call() != null)
                callHelper(ctx.call(), routine.returnType, false);
        }
        else if(type != null)
        {
            RoutineSpec routine = type.lookupStaticRoutine(Predefined.OPERATOR_PAREN, arguments);
            
            putRoutineSignature(new RoutineSpec(Predefined.OPERATOR_PAREN, arguments, routine.returnType));
            
            if(ctx.call() != null)
                callHelper(ctx.call(), type, true);
        }
        
        return null;
    }
    
    private void callHelper(SixtyFortranParser.CallContext ctx, TypeSpec callerType, boolean isStatic)
    {
        if(ctx.identifierCall() != null)
            idCallHelper(ctx.identifierCall(), callerType, isStatic);
        else if(ctx.routineCall() != null)
            routineCallHelper(ctx.routineCall(), callerType, isStatic);
    }
    
    private void idCallHelper(SixtyFortranParser.IdentifierCallContext ctx, TypeSpec callerType, boolean isStatic)
    {
        String id = ctx.IDENTIFIER().getText();
        
        ScopeEntry entry;
        if(isStatic)
         entry = callerType.lookupStaticField(id);
        else
            entry = callerType.lookupField(id);
        
        if(ctx.call() != null)
            callHelper(ctx.call(), entry.type, false);
    }
    
    private void routineCallHelper(SixtyFortranParser.RoutineCallContext ctx, TypeSpec callerType, boolean isStatic)
    {
        String id = ctx.IDENTIFIER().getText();
        
        ArgumentList arguments = new ArgumentList();
        if(ctx.argumentList() != null)
        {
            visit(ctx.argumentList());
            arguments = ctx.argumentList().args;
        }
        
        RoutineSpec routine;
        
        if(isStatic)
            routine = callerType.lookupStaticRoutine(id, arguments);
        else
            routine = callerType.lookupRoutine(id, arguments);
        
        putRoutineSignature(new RoutineSpec(Predefined.OPERATOR_PAREN, arguments, routine.returnType));
        
        if(ctx.call() != null)
            callHelper(ctx.call(), routine.returnType, false);
    }
    
    private void putRoutineSignature(RoutineSpec routine)
    {
        routineSignatures.add(routine);            
    }
    
}
