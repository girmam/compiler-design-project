.class public testif
.super java/lang/Object

.method public <init>()V
.var 0 is this Ltestif;
aload 0
invokespecial java/lang/Object/<init>()V
return
.limit locals 1
.limit stack 1
.end method

.method public static main([Ljava/lang/String;)V
.var 0 is print Llibrary/print;
.var 1 is read Llibrary/read;

new library/print
dup
invokespecial library/print/<init>()V
astore 0
new library/bool
dup
iconst_1
invokespecial library/bool/<init>(I)V
getfield library/bool/value I
ifeq L002
aload 0
new library/string
dup
ldc "true"
invokespecial library/string/<init>(Ljava/lang/String;)V
invokevirtual library/print/operator_parenthesis(Llibrary/string;)V

goto L001
L002:
L001:
return
.limit locals 2
.limit stack 16
.end method
