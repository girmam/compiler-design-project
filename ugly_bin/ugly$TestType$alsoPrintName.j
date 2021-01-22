.class public ugly_bin/ugly$TestType$alsoPrintName
.super ugly_bin/routine


.field public static print Llibrary/print;

.method public <init>()V
.var 0 is this Lugly_bin/ugly$TestType$alsoPrintName;
aload 0
invokespecial ugly_bin/routine/<init>()V

return
.limit locals 2
.limit stack 16
.end method

.method public operator_parenthesis()V
.var 0 is this Lugly_bin/ugly$TestType$alsoPrintName;
getstatic ugly_bin/ugly$TestType$alsoPrintName/print Llibrary/print;
new library/string
dup
ldc "hello there"
invokespecial library/string/<init>(Ljava/lang/String;)V
invokevirtual library/print/operator_parenthesis(Llibrary/string;)V

return
.limit locals 1
.limit stack 16
.end method
