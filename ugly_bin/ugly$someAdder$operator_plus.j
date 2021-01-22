.class public ugly_bin/ugly$someAdder$operator_plus
.super ugly_bin/routine

.field public type Lugly_bin/ugly$someAdder;


.method public <init>(Lugly_bin/ugly$someAdder;)V
.var 0 is this Lugly_bin/ugly$someAdder$operator_plus;
aload 0
invokespecial ugly_bin/routine/<init>()V

aload 0
aload 1
putfield ugly_bin/ugly$someAdder$operator_plus/type Lugly_bin/ugly$someAdder;

return
.limit locals 3
.limit stack 16
.end method

.method public operator_parenthesis(Lugly_bin/ugly$someAdder;)Lugly_bin/ugly$someAdder;
.var 0 is this Lugly_bin/ugly$someAdder$operator_plus;
.var 1 is other Lugly_bin/ugly$someAdder;
.var 2 is newAdder Lugly_bin/ugly$someAdder;
new ugly_bin/ugly$someAdder
dup
invokespecial ugly_bin/ugly$someAdder/<init>()V
astore 2
aload 2
getfield ugly_bin/ugly$someAdder/i Llibrary/integer;

aload 0
getfield ugly_bin/ugly$someAdder$operator_plus/type Lugly_bin/ugly$someAdder;
getfield ugly_bin/ugly$someAdder/i Llibrary/integer;

aload 1
getfield ugly_bin/ugly$someAdder/i Llibrary/integer;

invokevirtual library/integer/operator_plus(Llibrary/integer;)Llibrary/integer;
invokevirtual library/integer/operator_assignment(Llibrary/integer;)V
aload 2

areturn
.limit locals 4
.limit stack 16
.end method
