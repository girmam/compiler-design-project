.class public ugly_bin/ugly$TestType
.super java/lang/Object

.field public name Llibrary/string;
.field public static dname Llibrary/string;
.field public printName Lugly_bin/routine;

.field public static alsoPrintName Lugly_bin/routine;

.field public static print Llibrary/print;

.method public <init>()V
.var 0 is this Lugly_bin/ugly$TestType;
aload 0
invokespecial java/lang/Object/<init>()V

aload 0
new ugly_bin/ugly$TestType$printName
dup
aload 0
invokespecial ugly_bin/ugly$TestType$printName/<init>(Lugly_bin/ugly$TestType;)V
putfield ugly_bin/ugly$TestType/printName Lugly_bin/routine;

aload 0
new library/string
dup
invokespecial library/string/<init>()V
putfield ugly_bin/ugly$TestType/name Llibrary/string;

return
.limit locals 3
.limit stack 16
.end method

.method public static $staticInitialization()V
getstatic ugly_bin/ugly$TestType/print Llibrary/print;
putstatic ugly_bin/ugly$TestType$printName/print Llibrary/print;

new library/string
dup
ldc "general kenobi"
invokespecial library/string/<init>(Ljava/lang/String;)V
putstatic ugly_bin/ugly$TestType/dname Llibrary/string;

new ugly_bin/ugly$TestType$alsoPrintName
dup
invokespecial ugly_bin/ugly$TestType$alsoPrintName/<init>()V
putstatic ugly_bin/ugly$TestType/alsoPrintName Lugly_bin/routine;
getstatic ugly_bin/ugly$TestType/print Llibrary/print;
putstatic ugly_bin/ugly$TestType$alsoPrintName/print Llibrary/print;

return
.limit locals 0
.limit stack 16
.end method
