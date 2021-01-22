.class public ugly_bin/ugly$TestType$printName
.super ugly_bin/routine

.field public type Lugly_bin/ugly$TestType;

.field public static print Llibrary/print;

.method public <init>(Lugly_bin/ugly$TestType;)V
.var 0 is this Lugly_bin/ugly$TestType$printName;
aload 0
invokespecial ugly_bin/routine/<init>()V

aload 0
aload 1
putfield ugly_bin/ugly$TestType$printName/type Lugly_bin/ugly$TestType;

return
.limit locals 3
.limit stack 16
.end method

.method public operator_parenthesis()V
.var 0 is this Lugly_bin/ugly$TestType$printName;
getstatic ugly_bin/ugly$TestType$printName/print Llibrary/print;
aload 0
getfield ugly_bin/ugly$TestType$printName/type Lugly_bin/ugly$TestType;
getfield ugly_bin/ugly$TestType/name Llibrary/string;

invokevirtual library/print/operator_parenthesis(Llibrary/string;)V

return
.limit locals 2
.limit stack 16
.end method
