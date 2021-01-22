package intermediate.scope;

import java.util.Collection;
import java.util.HashMap;

import intermediate.Kind;
import intermediate.type.*;

public class Scope
{    
    private Scope parent;
    private HashMap<String, ScopeEntry> entryMap;
    private HashMap<String, TypeSpec> typeMap;
    private Kind kind;
    
    private HashMap<String, ScopeEntry> nonlocalEntryMap;
    
    private int numInnerScopes;
    private int scopeNum;
    private int numLocals;
    
    public Scope()
    {
        this(null);
    }
    
    public Scope(Scope parent)
    {
        this(parent, Kind.OTHER);
    }

    public Scope(Scope parent, Kind kind)
    {
        this.parent = parent;
        entryMap = new HashMap<>();
        nonlocalEntryMap = new HashMap<>();
        typeMap = new HashMap<>();
        this.kind = kind;
        
        numInnerScopes = 0;
        scopeNum = 0;
        numLocals = 0;
        
        if(this.kind == Kind.INNER && parent != null)
        {
            scopeNum = parent.getNumInnerScopes();
            parent.incInnerScopes();
        }
        
        //handles the 'this' slot
        if(this.kind == Kind.FIELD)
            numLocals = 1;
    }
    
    public Scope getParent()
    {
        return parent;
    }
    
    public Kind getKind()
    {
        return kind;
    }
    
    public void addEntry(ScopeEntry entry)
    {
        entry.slotNumber = getNumLocals();
        entryMap.put(entry.name, entry);
        incLocals();
    }
    
    public void addNonlocalEntry(ScopeEntry entry)
    {
        nonlocalEntryMap.put(entry.name, entry);
        
        //TODO: figure out if this is complicated by nonlocal scopes
        if(parent != null && parent.lookupEntryNoParent(entry.name) == null)
            parent.addNonlocalEntry(entry);
    }
    
    public void addType(TypeSpec type)
    {
        typeMap.put(type.name, type);
    }
    
    public ScopeEntry lookupEntry(String name)
    {
        ScopeEntry entry = entryMap.get(name);
        ScopeEntry nonlocalEntry = nonlocalEntryMap.get(name);
        
        if(entry == null && nonlocalEntry != null)
            return nonlocalEntry;
        if(entry == null && parent != null)
        {
            entry = parent.lookupEntry(name);
        }
        
        return entry;
    }
    
    public ScopeEntry lookupEntryNoParent(String name)
    {
        return entryMap.get(name);
    }
    
    public TypeSpec lookupType(String name)
    {
        TypeSpec type = typeMap.get(name);
        
        if(type == null && parent != null)
        {
            type = parent.lookupType(name);
        }
        
        return type;
    }
    
    public TypeSpec lookupTypeNoParent(String name)
    {
        return typeMap.get(name);
    }
    
    public void incInnerScopes()
    {
        numInnerScopes++;
    }
    
    public int getNumInnerScopes()
    {
        return numInnerScopes;
    }
    
    public int getScopeNum()
    {
        return scopeNum;
    }
    
    public int getNumLocals()
    {
        if(kind == Kind.INNER)
            return numLocals + parent.getNumLocals();
        else
            return numLocals;
    }
    
    public Collection<ScopeEntry> getEntries()
    {
        return entryMap.values();
    }
    
    public Collection<ScopeEntry> getNonlocalEntries()
    {
        return nonlocalEntryMap.values();
    }
    
    private void incLocals()
    {
        numLocals++;
        
        if(this.kind == Kind.INNER)
            parent.incLocals();
    }
}
