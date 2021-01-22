import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import antlr4.*;
import frontend.*;
import backend.compiler.Compiler;

public class SixtyFortran
{

    public static void main(String[] args) throws Exception 
    {
        if (args.length != 1 && args.length != 2)
        {
            System.out.println("USAGE: SixtyFortran sourceFileName -noassemble (optional)");
            return;
        }
        
        String sourceFileName = args[0];
        
        // Create the input stream.
        InputStream source = new FileInputStream(sourceFileName);
        
        // Create the character stream from the input stream.
        CharStream cs = CharStreams.fromStream(source);
        
        // Custom syntax error handler.
        SyntaxErrorHandler syntaxErrorHandler = new SyntaxErrorHandler();
        
        SixtyFortranLexer lexer = new SixtyFortranLexer(cs);
        lexer.removeErrorListeners();
        lexer.addErrorListener(syntaxErrorHandler);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        
        SixtyFortranParser parser = new SixtyFortranParser(tokens);
        
        // Pass 1: Check syntax and create the parse tree.
        System.out.printf("\nPASS 1 Syntax: ");
        parser.removeErrorListeners();
        parser.addErrorListener(syntaxErrorHandler);
        ParseTree tree = parser.program();
        
        int errorCount = syntaxErrorHandler.getCount();
        if (errorCount > 0) 
        {
            System.out.printf("\nThere were %d syntax errors.\n", errorCount);
            System.out.println("Object file not created or modified.");
            return;
        }
        else
        {
            System.out.println("There were no syntax errors.");
        }
        
        System.out.println("\nPASS 2 Semantics:");
        SemanticErrorHandler error = new SemanticErrorHandler();
        
        Semantics pass2 = new Semantics(error);
        pass2.visit(tree);
        
        errorCount = error.getErrorCount();
        if (errorCount > 0)
        {
            System.out.printf("\nThere were %d semantic errors.\n", errorCount);
            System.out.println("Object file not created or modified.");
            return;
        }
        else
        {
            System.out.println("There were no semantic errors");
        }
        
        System.out.println("\nPASS 3 Compiler:");
        
        String programName = args[0].substring(args[0].lastIndexOf('/') + 1, args[0].lastIndexOf('.')); // Customize with program name
        
        Compiler compiler = new Compiler(pass2.getWorldScope(), programName); // Customize with program name

        compiler.visit(tree);
        
        if(args.length == 1 || !args[1].equals("-noassemble"))
            assemble(programName);
    }
    
    public static void assemble(String program_name) throws IOException {
        String program_bin = program_name + "_bin";
        //boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows"); // Check OS type
        // Convert main .j program to .class
        Runtime.getRuntime().exec(String.format("java -jar jasmin.jar %s.j", program_name));
        Runtime.getRuntime().exec("java -jar jasmin.jar library/routine.j");
        // Get all files in program_bin folder and save them to p
        
        for(File fileEntry : new File(program_bin).listFiles())
        {
            if(fileEntry.getName().endsWith(".j"))
                Runtime.getRuntime().exec(String.format("java -jar jasmin.jar %s/%s > %s/error.log", program_bin, fileEntry.getName(), program_bin));   
        }
            
    }

}
