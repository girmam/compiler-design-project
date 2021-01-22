.class public ugly_bin/ugly$someAdder
.super java/lang/Object

.field public i Llibrary/integer;
.field public operator_plus Lugly_bin/routine;


.method public <init>()V
.var 0 is this Lugly_bin/ugly$someAdder;
aload 0
invokespecial java/lang/Object/<init>()V

aload 0
new ugly_bin/ugly$someAdder$operator_plus
dup
aload 0
invokespecial ugly_bin/ugly$someAdder$operator_plus/<init>(Lugly_bin/ugly$someAdder;)V
putfield ugly_bin/ugly$someAdder/operator_plus Lugly_bin/routine;

aload 0
new library/integer
dup
invokespecial library/integer/<init>()V
putfield ugly_bin/ugly$someAdder/i Llibrary/integer;

return
.limit locals 3
.limit stack 16
.end method

.method public static $staticInitialization()V

return
.limit locals 0
.limit stack 16
.end method
