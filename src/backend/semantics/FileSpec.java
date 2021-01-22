package backend.semantics;

import java.io.File;
import java.io.IOException;

import intermediate.Kind;
import intermediate.type.TypeSpec;

public class FileSpec
{
    public static String SUFFIX = ".j";
    
    public String prefix = "";
    public String directory = "";
    public String name;
    public FileSpec parent = null;
    public Kind kind = Kind.OTHER;
    public TypeSpec type = null;
    
    public FileSpec(String name)
    {
        this.name = name;
    }
    
    public FileSpec(String name, FileSpec parent)
    {
        this.name = name;
        this.prefix = parent.prefix + parent.name + "$";
        this.directory = parent.directory;
        this.parent = parent;
    }
    
    public FileSpec(String name, Kind kind, FileSpec parent)
    {
        this(name, kind, parent, null);
    }
    
    public FileSpec(String name, Kind kind, FileSpec parent, TypeSpec type)
    {
        this.name = name;
        this.prefix = parent.prefix + parent.name + "$";
        this.directory = parent.directory;
        this.parent = parent;
        this.kind = kind;
        this.type = type;
    }
    
    public String getFullName()
    {
        return directory + prefix + name + SUFFIX;
    }
    
    public String getNameNoExtension()
    {
        return directory + prefix + name;
    }
    
    public void mkFile()
    {
        File file = new File(directory + prefix + name + SUFFIX);
        try
        {
            file.delete();
            file.createNewFile();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
