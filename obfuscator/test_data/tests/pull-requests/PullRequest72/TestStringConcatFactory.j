.version 55 0
.class public super TestStringConcatFactory
.super java/lang/Object

.method public static test : ()V
    .code stack 4 locals 1
L0:     getstatic Field java/lang/System out Ljava/io/PrintStream;
L3:     iconst_1
L4:     iconst_2
L5:     iconst_3
L6:     invokedynamic [_28]
L11:    invokevirtual Method java/io/PrintStream println (Ljava/lang/String;)V
L14:    return
L15:    
    .end code
.end method
.bootstrapmethods
.const [_28] = InvokeDynamic invokeStatic Method java/lang/invoke/StringConcatFactory makeConcatWithConstants (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite; String "\u0001-\u0001-\u0001-\u0002-\u0002-\u0002" Double 3.14e0 Int 123 Int 1 : makeConcatWithConstants (III)Ljava/lang/String;
.end class
