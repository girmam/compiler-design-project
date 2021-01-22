.class public ugly
.super java/lang/Object

.method public <init>()V
.var 0 is this Lugly;
aload 0
invokespecial java/lang/Object/<init>()V
return
.limit locals 1
.limit stack 1
.end method

.method public static main([Ljava/lang/String;)V
.var 0 is print Llibrary/print;
.var 1 is read Llibrary/read;
.var 2 is t Lugly_bin/ugly$TestType;

new library/print
dup
invokespecial library/print/<init>()V
astore 0
aload 0
putstatic ugly_bin/ugly$TestType/print Llibrary/print;
invokestatic ugly_bin/ugly$TestType/$staticInitialization()V
new ugly_bin/ugly$TestType
dup
invokespecial ugly_bin/ugly$TestType/<init>()V
astore 2
aload 2
getfield ugly_bin/ugly$TestType/name Llibrary/string;

getfield library/string/operator_assignment Lugly_bin/routine;
new library/integer
dup
ldc 1
invokespecial library/integer/<init>(I)V
invokestatic library/string/operator_parenthesis(Llibrary/integer;)library/string;

invokevirtual library/string/operator_assignment(Llibrary/string;)V
aload 2
getfield ugly_bin/ugly$TestType/printName Lugly_bin/routine;
invokevirtual ugly_bin/routine/operator_parenthesis()V

getstatic ugly_bin/ugly$TestType/alsoPrintName Lugly_bin/routine;
invokevirtual ugly_bin/routine/operator_parenthesis()V

aload 0
getstatic ugly_bin/ugly$TestType/dname Llibrary/string;

invokevirtual library/print/operator_parenthesis(Llibrary/string;)V

return
.limit locals 3
.limit stack 16
.end method
