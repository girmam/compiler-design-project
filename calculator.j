.class public calculator
.super java/lang/Object

.method public <init>()V
.var 0 is this Lcalculator;
aload 0
invokespecial java/lang/Object/<init>()V
return
.limit locals 1
.limit stack 1
.end method

.method public static main([Ljava/lang/String;)V
.var 0 is print Llibrary/print;
.var 2 is functionStr Llibrary/string;
.var 1 is read Llibrary/read;
.var 3 is function Llibrary/integer;

new library/print
dup
invokespecial library/print/<init>()V
astore 0
new library/read
dup
invokespecial library/read/<init>()V
astore 1
aload 0
new library/string
dup
ldc "Welcome to Calculator!"
invokespecial library/string/<init>(Ljava/lang/String;)V
invokevirtual library/print/operator_parenthesis(Llibrary/string;)V

aload 0
new library/string
dup
ldc "Supported functions:"
invokespecial library/string/<init>(Ljava/lang/String;)V
invokevirtual library/print/operator_parenthesis(Llibrary/string;)V

aload 0
new library/string
dup
ldc "Addition: 1"
invokespecial library/string/<init>(Ljava/lang/String;)V
invokevirtual library/print/operator_parenthesis(Llibrary/string;)V

aload 0
new library/string
dup
ldc "Subtraction: 2"
invokespecial library/string/<init>(Ljava/lang/String;)V
invokevirtual library/print/operator_parenthesis(Llibrary/string;)V

aload 0
new library/string
dup
ldc "Multiplication: 3"
invokespecial library/string/<init>(Ljava/lang/String;)V
invokevirtual library/print/operator_parenthesis(Llibrary/string;)V

aload 0
new library/string
dup
ldc "Division: 4"
invokespecial library/string/<init>(Ljava/lang/String;)V
invokevirtual library/print/operator_parenthesis(Llibrary/string;)V

aload 0
new library/string
dup
ldc "To quit, enter 0"
invokespecial library/string/<init>(Ljava/lang/String;)V
invokevirtual library/print/operator_parenthesis(Llibrary/string;)V

aload 0
new library/string
dup
ldc "Enter function number:"
invokespecial library/string/<init>(Ljava/lang/String;)V
invokevirtual library/print/operator_parenthesis(Llibrary/string;)V

aload 1
invokevirtual library/read/operator_parenthesis()Llibrary/string;

astore 2
new library/integer
dup
invokespecial library/integer/<init>()V
astore 3
return
.limit locals 4
.limit stack 16
.end method
