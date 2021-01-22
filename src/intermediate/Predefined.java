package intermediate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import backend.compiler.CodeGenerator;
import intermediate.scope.Scope;
import intermediate.scope.ScopeEntry;
import intermediate.type.ArgumentList;
import intermediate.type.RoutineSpec;
import intermediate.type.TypeSpec;

public class Predefined
{
    public static String INT = "integer";
    public static String STRING = "string";
    public static String REAL = "real";
    public static String BOOL = "bool";
    public static String ROUTINE = "routine";
    public static String ARRAY = "array";
    public static String LIST = "list";
    
    public static String UNARY_MINUS = "unary_minus";
    public static String UNARY_NOT = "operator_not";
    
    public static String OPERATOR_STAR = "operator_star";
    public static String OPERATOR_SLASH = "operator_slash";
    public static String OPERATOR_MOD = "operator_mod";
    
    public static String OPERATOR_PLUS = "operator_plus";
    public static String OPERATOR_MINUS = UNARY_MINUS;
    
    public static String OPERATOR_EQUALS = "operator_equals";
    public static String OPERATOR_NOT_EQUALS = "operator_not_equals";
    public static String OPERATOR_LESS_EQUALS = "operator_less_equals";
    public static String OPERATOR_GREATER_EQUALS = "operator_greater_equals";
    public static String OPERATOR_LESS_THAN= "operator_less_than";
    public static String OPERATOR_GREATER_THAN= "operator_greater_than";
    
    public static String OPERATOR_AND = "operator_and";
    public static String OPERATOR_OR = "operator_or";
    
    public static String OPERATOR_PAREN = "operator_parenthesis";
    public static String OPERATOR_BRACKET = "operator_brackets";
    
    public static String OPERATOR_ASSIGN = "operator_assignment";
    
    //defines the types
    public static TypeSpec intType = new TypeSpec(INT);
    public static TypeSpec realType = new TypeSpec(REAL);
    public static TypeSpec stringType = new TypeSpec(STRING);
    public static TypeSpec boolType = new TypeSpec(BOOL);
    public static TypeSpec routineType = new TypeSpec(ROUTINE);
    
    //printType and readType are special types that aren't routines
    public static TypeSpec printType = new TypeSpec("print");
    public static TypeSpec readType = new TypeSpec("read");
    
    //defines the world scope's routines
    public static ScopeEntry printEntry;
    public static ScopeEntry readEntry;
    
    public static void addPredefinedTypesToScope(Scope scope)
    {
        //adds the types to the scope
        scope.addType(intType);
        scope.addType(realType);
        scope.addType(stringType);
        scope.addType(boolType);
        scope.addType(routineType);
        scope.addType(printType);
        scope.addType(readType);
        //scope.addType(arrayType);
        //scope.addType(listType);
        
        initIntegerType();
        initRealType();
        initStringType();
        initBoolType();
        
        
        RoutineSpec stringParenInt = new RoutineSpec(OPERATOR_PAREN, new ArgumentList(intType), stringType);
        stringType.addStaticRoutine(stringParenInt);        
    }
    
    public static void addPredefinedRoutinesToScope(Scope scope)
    {
        RoutineSpec print = new RoutineSpec(OPERATOR_PAREN, new ArgumentList(stringType), null);
        TypeSpec printType = Predefined.printType; //print has to be a special type
        printType.setPath("print");
        printType.addRoutine(print);
        printEntry = new ScopeEntry("print", printType);
        scope.addEntry(printEntry);
        
        RoutineSpec read = new RoutineSpec(OPERATOR_PAREN, new ArgumentList(), stringType);
        TypeSpec readType = Predefined.readType; //print is a special type
        readType.setPath("read");
        readType.addRoutine(read);
        readEntry = new ScopeEntry("read", readType);
        scope.addEntry(readEntry);
    }
    
    public static void emitPredefinedRoutines(CodeGenerator code)
    {
        //emit print
        code.emitConstructorCall(printEntry, null);
        code.emitStoreEntry(printEntry);
    }
    
    public static void movePredefinedTypes(String binFolder)
    {
        routineType.setPath(binFolder + ROUTINE);
        intType.setPath("library/" + Predefined.INT);
        realType.setPath("library/" + Predefined.REAL);
        boolType.setPath("library/" + Predefined.BOOL);
        stringType.setPath("library/" + Predefined.STRING);
        
        printEntry.type.setPath("library/print");
        readEntry.type.setPath("library/read");
        
        new File("library").mkdir();
        
        for(File predefinedFile : new File("bin/library").listFiles())
        {
            if(predefinedFile.getName().endsWith(".class"))
            {
                Path sourcePath = Paths.get("bin/library/" + predefinedFile.getName());
                Path destinationpath = Paths.get("library/" + predefinedFile.getName());
                try
                {
                    Files.copy(sourcePath, destinationpath, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }
    
    private static void initIntegerType()
    {
        //assignment operator
        RoutineSpec intAssign = new RoutineSpec(OPERATOR_ASSIGN, new ArgumentList(intType), null);
        
        //mathematical operators
        RoutineSpec intAddInt = new RoutineSpec(OPERATOR_PLUS, new ArgumentList(intType), intType);
        RoutineSpec intMinusInt = new RoutineSpec(OPERATOR_MINUS, new ArgumentList(intType), intType);
        RoutineSpec intTimesInt = new RoutineSpec(OPERATOR_STAR, new ArgumentList(intType), intType);
        RoutineSpec intDivInt = new RoutineSpec(OPERATOR_SLASH, new ArgumentList(intType), intType);
        RoutineSpec intModInt = new RoutineSpec(OPERATOR_MOD, new ArgumentList(intType), intType);
        
        //unary minus operator
        RoutineSpec intUnaryMinus = new RoutineSpec(UNARY_MINUS, new ArgumentList(intType), intType); 
        
        //cast operators
        RoutineSpec intCastStr = new RoutineSpec(OPERATOR_PAREN, new ArgumentList(stringType), intType);
        RoutineSpec intCastReal = new RoutineSpec(OPERATOR_PAREN, new ArgumentList(realType), intType);
        RoutineSpec intCastBool = new RoutineSpec(OPERATOR_PAREN, new ArgumentList(boolType), intType);
        
        //comparison operators
        RoutineSpec intEquality = new RoutineSpec(OPERATOR_EQUALS, new ArgumentList(intType), boolType);
        RoutineSpec intNotEquality = new RoutineSpec(OPERATOR_NOT_EQUALS, new ArgumentList(intType), boolType);
        RoutineSpec intLT = new RoutineSpec(OPERATOR_LESS_THAN, new ArgumentList(intType), boolType);
        RoutineSpec intGT = new RoutineSpec(OPERATOR_GREATER_THAN, new ArgumentList(intType), boolType);
        RoutineSpec intLE = new RoutineSpec(OPERATOR_LESS_EQUALS, new ArgumentList(intType), boolType);
        RoutineSpec intGE = new RoutineSpec(OPERATOR_GREATER_EQUALS, new ArgumentList(intType), boolType);
        
        intType.addRoutine(intAssign);
        intType.addRoutine(intAddInt);
        intType.addRoutine(intMinusInt);
        intType.addRoutine(intTimesInt);
        intType.addRoutine(intDivInt);
        intType.addRoutine(intModInt);
        
        intType.addStaticRoutine(intUnaryMinus);
        intType.addStaticRoutine(intCastStr);
        intType.addStaticRoutine(intCastReal);
        intType.addStaticRoutine(intCastBool);
        
        intType.addRoutine(intEquality);
        intType.addRoutine(intNotEquality);
        intType.addRoutine(intLT);
        intType.addRoutine(intGT);
        intType.addRoutine(intLE);
        intType.addRoutine(intGE);
    }
    
    private static void initRealType()
    {
        //assignment operators
        RoutineSpec realAssign = new RoutineSpec(OPERATOR_ASSIGN, new ArgumentList(realType), null);
        
        //mathematical operators
        RoutineSpec realAddReal = new RoutineSpec(OPERATOR_PLUS, new ArgumentList(realType), realType);
        RoutineSpec realMinusReal = new RoutineSpec(OPERATOR_MINUS, new ArgumentList(realType), realType);
        RoutineSpec realTimesReal = new RoutineSpec(OPERATOR_STAR, new ArgumentList(realType), realType);
        RoutineSpec realDivReal = new RoutineSpec(OPERATOR_SLASH, new ArgumentList(realType), realType);
        
        //unary minus operator
        RoutineSpec realUnaryMinus = new RoutineSpec(UNARY_MINUS, new ArgumentList(realType), realType);
        
        //cast operators
        RoutineSpec realCastStr = new RoutineSpec(OPERATOR_PAREN, new ArgumentList(stringType), realType);
        RoutineSpec realCastInt = new RoutineSpec(OPERATOR_PAREN, new ArgumentList(intType), realType);
        RoutineSpec realCastBool = new RoutineSpec(OPERATOR_PAREN, new ArgumentList(boolType), realType);
        
        //equality operators
        RoutineSpec realEquality = new RoutineSpec(OPERATOR_EQUALS, new ArgumentList(realType), boolType);
        RoutineSpec realNotEquality = new RoutineSpec(OPERATOR_NOT_EQUALS, new ArgumentList(realType), boolType);
        RoutineSpec realLT = new RoutineSpec(OPERATOR_LESS_THAN, new ArgumentList(realType), boolType);
        RoutineSpec realGT = new RoutineSpec(OPERATOR_GREATER_THAN, new ArgumentList(realType), boolType);
        RoutineSpec realLE = new RoutineSpec(OPERATOR_LESS_EQUALS, new ArgumentList(realType), boolType);
        RoutineSpec realGE = new RoutineSpec(OPERATOR_GREATER_EQUALS, new ArgumentList(realType), boolType);
        
        realType.addRoutine(realAssign);
        realType.addRoutine(realAddReal);
        realType.addRoutine(realMinusReal);
        realType.addRoutine(realTimesReal);
        realType.addRoutine(realDivReal);
        
        realType.addStaticRoutine(realUnaryMinus);
        realType.addStaticRoutine(realCastStr);
        realType.addStaticRoutine(realCastInt);
        realType.addStaticRoutine(realCastBool);
        
        realType.addRoutine(realEquality);
        realType.addRoutine(realNotEquality);
        realType.addRoutine(realLT);
        realType.addRoutine(realGT);
        realType.addRoutine(realLE);
        realType.addRoutine(realGE);
    }
    
    private static void initStringType()
    {
        //assignment operator
        RoutineSpec strAssignment = new RoutineSpec(OPERATOR_ASSIGN, new ArgumentList(stringType), null);
        
        //concatenation operator
        RoutineSpec strAddStr = new RoutineSpec(OPERATOR_PLUS, new ArgumentList(stringType), stringType);
        
        //substring
        RoutineSpec substr = new RoutineSpec("substring", new ArgumentList(intType, intType), stringType);
        
        //cast operators
        RoutineSpec strCastInt = new RoutineSpec(OPERATOR_PAREN, new ArgumentList(intType), stringType);
        RoutineSpec strCastBool = new RoutineSpec(OPERATOR_PAREN, new ArgumentList(boolType), stringType);
        RoutineSpec strCastReal = new RoutineSpec(OPERATOR_PAREN, new ArgumentList(realType), stringType);
        
        //equality operators
        RoutineSpec strEquality = new RoutineSpec(OPERATOR_EQUALS, new ArgumentList(stringType), boolType);
        RoutineSpec strNotEquality = new RoutineSpec(OPERATOR_NOT_EQUALS, new ArgumentList(stringType), boolType);
        
        stringType.addRoutine(strAssignment);
        stringType.addRoutine(strAddStr);
        stringType.addRoutine(substr);
        
        stringType.addStaticRoutine(strCastReal);
        stringType.addStaticRoutine(strCastInt);
        stringType.addStaticRoutine(strCastBool);
        
        stringType.addRoutine(strEquality);
        stringType.addRoutine(strNotEquality);
    }
    
    private static void initBoolType()
    {
        //assignment operator
        RoutineSpec boolAssignment = new RoutineSpec(OPERATOR_ASSIGN, new ArgumentList(boolType), null);
        
        //comparison operators
        RoutineSpec boolEquality = new RoutineSpec(OPERATOR_EQUALS, new ArgumentList(boolType), boolType);
        RoutineSpec boolNotEquality = new RoutineSpec(OPERATOR_NOT_EQUALS, new ArgumentList(boolType), boolType);
        
        //boolean operators
        RoutineSpec boolOR = new RoutineSpec(OPERATOR_OR, new ArgumentList(boolType), boolType);
        RoutineSpec boolAND = new RoutineSpec(OPERATOR_AND, new ArgumentList(boolType), boolType);
        
        //not operator
        RoutineSpec boolNot = new RoutineSpec(UNARY_NOT, new ArgumentList(boolType), boolType);
        
        //cast operators
        RoutineSpec boolCastString = new RoutineSpec(OPERATOR_PAREN, new ArgumentList(stringType), boolType);
        
        
        boolType.addRoutine(boolAssignment);
        
        boolType.addStaticRoutine(boolNot);
        boolType.addStaticRoutine(boolCastString);
        
        boolType.addRoutine(boolEquality);
        boolType.addRoutine(boolNotEquality);
        boolType.addRoutine(boolOR);
        boolType.addRoutine(boolAND);
    }
}
