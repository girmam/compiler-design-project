package backend.compiler;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import antlr4.SixtyFortranParser;
import backend.semantics.FileSpec;
import intermediate.Kind;
import intermediate.Predefined;
import intermediate.scope.Scope;
import intermediate.scope.ScopeEntry;
import intermediate.type.ArgumentList;
import intermediate.type.RoutineSpec;
import intermediate.type.TypeSpec;

import static backend.compiler.Directive.*;

public class CodeGenerator
{
    private static String STACK_LIMIT = "16";
    
    private FileSpec currentFile;
    private Scope localScope;
    private PrintWriter objectFile;
    
    public CodeGenerator()
    {
        currentFile = null;
        objectFile = null;
        localScope = null;
    }
    
    public void setCurrentFile(FileSpec file)
    {
        if(objectFile != null)
            objectFile.close();

        currentFile = file;
        try
        {
            objectFile = new PrintWriter(new FileWriter(currentFile.getFullName(), true));
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    
    public void setCurrentScope(Scope scope)
    {
        localScope = scope;
    }
    
    public void close()
    {
        if(objectFile != null)
            objectFile.close();
    }
    
    public void emitMainPrologue(String programName)
    {
        emitConstructor(programName, null);
        
        emit(METHOD_PUBLIC_STATIC, "main([Ljava/lang/String;)V");
    }
    
    public void emitConstructor(String className, String superClass)
    {
        emit(CLASS_PUBLIC, className);
        
        String superclassName = "java/lang/Object";
        if(superClass != null)
            superclassName = superClass;
        
        emit(SUPER, superclassName);
        
        emit();
        
        emit(METHOD_PUBLIC, "<init>()V");
        emit(VAR, "0 is this L" + className + ";");
        
        emitLoad(0);
        emit("invokespecial java/lang/Object/<init>()V");
        emit("return");
        
        emit(LIMIT_LOCALS,"1");
        emit(LIMIT_STACK, "1");
        emit(END_METHOD);
        
        emit();
    }
    
    public void emitClass(TypeSpec type, String superClass, Scope scope)
    {
        emit(CLASS_PUBLIC, type.getPath());

        String superclassName = "java/lang/Object";
        if(superClass != null)
            superclassName = superClass;
        
        emit(SUPER, superclassName);
        
        emit();
        
        emitFields(type.getFields(), false);
        emitFields(type.getStaticFields(), true);
        
        for(ScopeEntry routineEntry: type.getRoutines())
        {
            //prevents the problem when there is a field with the same name as the class
            if(!routineEntry.name.equals(type.getPath().substring(1 + type.getPath().lastIndexOf("$"))))
            {
                //the type doesn't preserve a path to the routine's class file, but the localscope does, so if we just lookup the routine's name, we should get it's path
                ScopeEntry routineEntryWithPath = localScope.lookupEntry(routineEntry.name);
                emitField(routineEntryWithPath, false);
            }
            emit();
        }
        
        for(ScopeEntry routineEntry : type.getStaticRoutines())
        {
            //TODO: static routines haven't actually been added to the local scope, so we have to figure out their path manually
            String routinePath = currentFile.getNameNoExtension() + "$" + routineEntry.name;
            routineEntry.type.setPath(routinePath);
            emitField(routineEntry, true);
            
            emit();
        }
        
        emitFields(scope.getNonlocalEntries(), true);
        
        emit();
        
        emitConstructorDefinition(type, superclassName);

        emit();
    }
    
    public void emitRoutineFile(Set<RoutineSpec> routineSignatures)
    {
        emitConstructor(currentFile.getNameNoExtension(), null);

        for(RoutineSpec routine: routineSignatures)
        {
            //emit(METHOD_PUBLIC, routine.name + "")
            String methodSignature = routine.name + "("; 
            
            for(TypeSpec type : routine.arguments.argumentTypes)
                methodSignature += "L" + type.getPath() + ";";
            
            methodSignature += ")";
            
            if(routine.returnType == null)
                methodSignature += "V";
            else
                methodSignature += "L" + routine.returnType.getPath() + ";";
            
            emit(METHOD_PUBLIC, methodSignature);
            
            if(routine.returnType == null)
            {
                emit("return");
                emit(LIMIT_LOCALS, String.valueOf(1 + routine.arguments.argumentTypes.size()));
                emit(LIMIT_STACK, "0");
            }
            else
            {
                emit("aconst_null");
                emit("areturn");
                emit(LIMIT_LOCALS, String.valueOf(1 + routine.arguments.argumentTypes.size()));
                emit(LIMIT_STACK, "1");
            }
            
            emit(END_METHOD);
            emit();
        }
    }
    
    public void emitClassStaticInitialization(SixtyFortranParser.TypeDefinitionContext ctx, Compiler compiler)
    {
        TypeSpec type = ctx.type;
        emit(METHOD_PUBLIC_STATIC, "$staticInitialization()V");
        
        //handles the normal routines
        for(SixtyFortranParser.RoutineDefinitionContext routineCtx: ctx.definitionBlock().routineBlock().routineDefinition())
        {
            Scope routineScope = routineCtx.routineScope;
            String routineName = routineCtx.routine.name;
            for(ScopeEntry nonlocalEntry : routineScope.getNonlocalEntries())
            {
                //load the entry somehow
                emitLoadEntry(nonlocalEntry);
                ///print and read are special cases that are taken care of by this
                String path = nonlocalEntry.type.getPath();
                if(nonlocalEntry.type.name.equals(Predefined.ROUTINE))
                    path = Predefined.routineType.getPath();
                emit("putstatic " + type.getPath() + "$" + routineName + "/" + nonlocalEntry.name + " L" + path + ";" );
            }
            
            emit();
        }
        
        for(SixtyFortranParser.StaticVarDefinitionContext varDef : ctx.definitionBlock().varBlock().staticVarDefinition())
        {
            compiler.visit(varDef.expression());
            for(int i = 0; i < varDef.varDeclaration().entries.size() - 1; i++)
                emit("dup");
            
            for(ScopeEntry entry : varDef.varDeclaration().entries)
            {
                emit("putstatic " + type.getPath() + "/" + entry.name + " L" + entry.type.getPath() + ";");
            }
            
            emit();
        }
        
        //handles the static routines
        for(SixtyFortranParser.StaticRoutineDefinitionContext routineCtx: ctx.definitionBlock().routineBlock().staticRoutineDefinition())
        {
            Scope routineScope = routineCtx.routineDefinition().routineScope;
            String routineName = routineCtx.routineDefinition().routine.name;
            
            ScopeEntry routineEntry = type.lookupStaticRoutine(routineName);
            //need to set the entry's path
            routineEntry.type.setPath(type.getPath() + "$" + routineName);
            //create an instance of the class
            emitStaticFieldInitialization(routineEntry);
            
            for(ScopeEntry nonlocalEntry : routineScope.getNonlocalEntries())
            {
                //load the entry somehow
                emitLoadEntry(nonlocalEntry);
                
                //print and read are special cases that are taken care of by this
                String path = nonlocalEntry.type.getPath();
                if(nonlocalEntry.type.name.equals(Predefined.ROUTINE))
                    path = Predefined.routineType.getPath();
                emit("putstatic " + type.getPath() + "$" + routineName + "/" + nonlocalEntry.name + " L" + path + ";" );
            }
            
            emit();
        }
        
        emit("return");
        emit(LIMIT_LOCALS, "0");
        emit(LIMIT_STACK, STACK_LIMIT); 
        emit(END_METHOD);
    }
    
    public void emitMainEpilogue(int numLocals)
    {
        emit("return");
        emit(LIMIT_LOCALS, String.valueOf(numLocals));
        emit(LIMIT_STACK, STACK_LIMIT);
        emit(END_METHOD);
    }
    
    public void emitConstructorDefinition(TypeSpec type, String superclass)
    {
        String methodSignature = "<init>(";
        
        //if the routine belongs to a type, then it will have a field called type. This will need to be passed to it's constructor. 
        ScopeEntry instance = type.lookupField("type");
        if(instance != null)
            methodSignature += "L" + instance.type.getPath() + ";";
            
        methodSignature += ")V";
        
        emit(METHOD_PUBLIC, methodSignature);
        emit(VAR, "0 is this L" + type.getPath() + ";");
        
        //we don't technically have to declare what each var is
        
        //creates the "this" object
        emitLoad(0);
        emit("invokespecial " + superclass + "/<init>()V");
        emit();
        
        //if there is a type parameter to our routine, then this assigns the parameter to the routine's type field
        if(instance != null)
        {
            emitLoad(0); 
            emitLoad(1); 
            emit("putfield " + type.getPath() + "/type L" + instance.type.getPath() + ";");
            emit();
        }
           
        for(ScopeEntry routineEntry: type.getRoutines() )
        {
            //TODO: we currently have to include because routines currently have a field with the same name as themselves (haven't fixed yet) and then they try to emit the constructor
            if(!routineEntry.name.equals(currentFile.name))
            {
                //the type doesn't preserve a path to the routine's class file, but the localscope does, so if we just lookup the routine's name, we should get it's path
                ScopeEntry routineEntryWithPath = localScope.lookupEntry(routineEntry.name);
                emitFieldInitialization(routineEntryWithPath, type);
            }    
        }
        
        for(ScopeEntry field : type.getFields())
        {
            //if there is a 'type' field, then it has already been taken care of in the constructor
            if(!field.name.equals("type"))
                emitFieldInitialization(field, type);
        }
        emit("return");
        
        emit(LIMIT_LOCALS, String.valueOf(1 +  type.getFields().size() + type.getRoutines().size()));
        emit(LIMIT_STACK, STACK_LIMIT);
        emit(END_METHOD);
    }
    public void emitLocalVars(Scope scope)
    {
        for(ScopeEntry entry: scope.getEntries())
        {
            emitLocalVar(entry);
        }
        emit();
    }
    
    public void emitLocalVars(Scope scope, RoutineSpec routine)
    {
        emitLocalVars(scope);
        ScopeEntry returnEntry = new ScopeEntry(routine.name, routine.returnType);
        returnEntry.slotNumber = scope.getNumLocals();
        emitLocalVar(returnEntry);
    }
    
    public void emitFields(Collection<ScopeEntry> entries, boolean areStatic)
    {
        for(ScopeEntry entry : entries)
        {
            emitField(entry, areStatic);
        }
    }
    
    public void emitField(ScopeEntry entry, boolean isStatic)
    {
        Directive dir = (isStatic)? FIELD_STATIC : FIELD;
        
        if(entry.type.name.equals(Predefined.ROUTINE))
            emit(dir, entry.name + " L" + Predefined.routineType.getPath() + ";");
        else
            emit(dir, entry.name + " L" + entry.type.getPath() + ";");
    }
    
    public void emitStaticFieldInitialization(ScopeEntry entry)
    {
        emit("new " + entry.type.getPath());
        emit("dup");
        emit("invokespecial " + entry.type.getPath() + "/<init>()V");
        
        String path = entry.type.getPath();
        if(entry.type.name.equals(Predefined.ROUTINE))
            path = Predefined.routineType.getPath();
        emit("putstatic " + currentFile.getNameNoExtension() + "/" + entry.name + " L" + path + ";");
    }
    
    public void emitVariableDec(ScopeEntry entry)
    {
        emitConstructorCall(entry, null);
        
        if(localScope.getKind() == Kind.MAIN || localScope.getKind() == Kind.STATIC_FIELD)
            emit("astore " + entry.slotNumber);
        else
            //need to increment slot number to account for "this" field in methods
            emit("astore " + (entry.slotNumber + 1));
    }
    
    public void emitFieldInitialization(ScopeEntry entry, TypeSpec type)
    {
        emitLoad(0);
        emitConstructorCall(entry, type);
        
        //routines are stored as a 'routine' object
        String path = entry.type.getPath();
        if(entry.type.name.equals(Predefined.ROUTINE))
            path = Predefined.routineType.getPath();
        
        emit("putfield " + type.getPath() + "/" + entry.name + " L" + path + ";");
        emit();
    }
    
    public void emitStaticInitializer(ScopeEntry entry, List<ScopeEntry> entries)
    {
        emit("new " + entry.type.getPath());
        emit("dup");
        for(ScopeEntry arg : entries)
            emitLoad(arg.slotNumber);
        
        String methodSignature = "invokespecial " + entry.type.getPath() + "/<init>(";
        for(int i = 0; i < entries.size(); i++)
            methodSignature += entries.get(i).type.getPath() + ";";
        methodSignature = ")V";
        emit(methodSignature);
        emit("putstatic " + currentFile.getNameNoExtension() + "/" + entry.name + " L" + entry.type.getPath() + ";");
    }
    
    public void emitConstructorCall(ScopeEntry entry, TypeSpec instanceType)
    {
        emit("new " + entry.type.getPath());
        emit("dup");
        //2 cases: emit a constructor for a normal type (which requires no parameters) or emit a constructor for a routine field, which takes one parameter (the object the routine belongs to)
        if(instanceType != null && entry.type.name.equals(Predefined.ROUTINE))
            emitLoad(0);
        
        String methodSignature = "invokespecial " + entry.type.getPath() + "/<init>(";

        if(instanceType != null && entry.type.name.equals(Predefined.ROUTINE))
            methodSignature += "L" + instanceType.getPath() + ";";
        
        methodSignature += ")V";
        emit(methodSignature);
    }
    
    public void emitRoutineHeader(SixtyFortranParser.RoutineDefinitionContext ctx, ScopeEntry routineEntry)
    {
        String signature = Predefined.OPERATOR_PAREN + "(";
        for(TypeSpec arg: ctx.routine.arguments.argumentTypes)
            signature += "L" + arg.getPath() + ";";
        signature += ")" + ((ctx.routine.returnType == null)? "V" : "V" + ctx.routine.returnType.getPath() + ";");
        emit(METHOD_PUBLIC, signature);
        emit(VAR, "0 is this L" + routineEntry.type.getPath() + ";");
        
        for(ScopeEntry entry : ctx.routineScope.getEntries())
        {
            if(entry.kind == Kind.VARIABLE)
            {
                entry.slotNumber = entry.slotNumber + 1;
                emitLocalVar(entry);
            }
        }
    }
    
    public void emitRoutineCloser(SixtyFortranParser.RoutineDefinitionContext ctx)
    {
        if(ctx.routine.returnType == null)
            emit("return");
        else
            emit("areturn");

        emit(LIMIT_LOCALS, String.valueOf(1 + ctx.routineScope.getNumLocals()));
        emit(LIMIT_STACK, STACK_LIMIT);
        emit(END_METHOD);
    }
    
    public void emitRoutineCall(String routineName, TypeSpec t1, ArgumentList args, TypeSpec returnType, boolean isStatic)
    {
        String routineCall;
        
        if(isStatic)
            routineCall = "invokestatic ";
        else
            routineCall = "invokevirtual ";
        
        if(t1.getPath() == Predefined.ROUTINE) //can use object equality because it may be the exact same object
            t1.setPath(Predefined.routineType.getPath());
        
        routineCall += t1.getPath() + "/" + routineName + "(";
        
        for(TypeSpec t: args.argumentTypes)
        {
            routineCall += "L" + t.getPath() + ";";
        }
        
        routineCall += ")";
        if(returnType == null)
            routineCall += "V";
        else
            routineCall += returnType.getPath() + ";";
        
        emit(routineCall);
    }
    
    public void emitLoadEntry(ScopeEntry entry)
    {
        String path = entry.type.getPath();
        
        //all routines are stored as a routine object, although they are actually a subclass of that object
        if(entry.type.name.equals(Predefined.ROUTINE))
            path = Predefined.routineType.getPath();
        
        if(entry.kind == Kind.VARIABLE) 
        {
            emitLoad(entry.slotNumber);
        }
        else if(entry.kind == Kind.FIELD)
        {
            emit("getfield " + entry.instanceType.getPath() + "/" + entry.name + " L" + path + ";");
        }
        else if(entry.kind == Kind.STATIC_FIELD)
        {
            emit("getstatic " + entry.instanceType.getPath() + "/" + entry.name + " L" + path + ";");
        }
        else if(entry.kind == Kind.NONLOCAL)
        {
            emit("getstatic " + currentFile.getNameNoExtension() + "/" + entry.name + " L" + path + ";"); 
        }
    }
    
    public void emitStoreEntry(ScopeEntry entry)
    {
        String path = entry.type.getPath();
        
        //all routines are stored as a routine object, although they are actually a subclass of that object
        if(entry.type.name.equals(Predefined.ROUTINE))
            path = Predefined.routineType.getPath();
        
        if(entry.kind == Kind.VARIABLE)
        {
            emitStore(entry.slotNumber);
        }
        else if(entry.kind == Kind.FIELD)
        {
            emit("putfield " + currentFile.getNameNoExtension() + "/" + entry.name + " L" + path + ";");
        }
        else if(entry.kind == Kind.STATIC_FIELD)
        {
            emit("putstatic " + entry.instanceType.getPath() + "/" + entry.name + " L" + path + ";");
        }
        else if(entry.kind == Kind.NONLOCAL)
        {
            emit("putstatic " + currentFile.getNameNoExtension() + "/" + entry.name + " L" + path + ";"); 
        }
    }
    
    public void emitLocalVar(ScopeEntry entry)
    {
        String path = entry.type.getPath();
        
        //all routines are stored as a routine object, although they are actually a subclass of that object
        if(entry.type.name.equals(Predefined.ROUTINE))
            path = Predefined.routineType.getPath();
        
        emit(VAR, String.valueOf(entry.slotNumber) + " is " + entry.name + " L" + path + ";");
    }
    
    public void emitLoad(int slotNum)
    {
        //TODO: figure out shortcuts
        emit("aload " + String.valueOf(slotNum));
    }
    
    public void emitStore(int slotNum)
    {
      //TODO: figure out shortcuts
        emit("astore " + String.valueOf(slotNum));
    }
    
    public void emitLabel(Label l)
    {
        emit(l.toString());
    }
    
    public void emit(String str, Label l)
    {
        emit(str + " " + l.toString());
    }
    
    public void emitBoolValue()
    {
        emit("getfield " + Predefined.boolType.getPath() + "/value I");
    }
    
    public void emit()
    {
        emit("");
    }
    
    public void emit(Directive dir)
    {
        emit(dir.toString());
    }
    
    public void emit(Directive dir, String str)
    {
        emit(dir.toString() + " " + str);
    }
    
    public void emit(String str)
    {
        objectFile.println(str);
    }
}
