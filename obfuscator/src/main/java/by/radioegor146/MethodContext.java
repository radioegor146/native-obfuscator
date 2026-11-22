package by.radioegor146;

import by.radioegor146.source.StringPool;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;

import java.util.*;

public class MethodContext {

    public NativeObfuscator obfuscator;

    public final MethodNode method;
    public final ClassNode clazz;
    public final int methodIndex;
    public final int classIndex;

    public final StringBuilder output;
    public final StringBuilder nativeMethods;

    public Type ret;
    public ArrayList<Type> argTypes;

    public int line;
    public List<Integer> stack;
    public List<Integer> locals;
    public final Map<CatchesBlock, String> catches = new LinkedHashMap<>();
    private final Map<LabelNode, List<Integer>> catchStarts = new HashMap<>();
    private final Map<LabelNode, List<Integer>> catchEnds = new HashMap<>();
    private final SortedSet<Integer> activeCatches = new TreeSet<>();
    private String exceptionCheck;
    private String inlineExceptionCheck;

    public HiddenMethodsPool.HiddenMethod proxyMethod;
    public MethodNode nativeMethod;

    public int stackPointer;
    public boolean needsClassLoader;
    public boolean needsArrayClassLoader;
    public boolean needsDeclaringClass;
    public boolean needsLookup;

    private final LabelPool labelPool = new LabelPool();

    public String cppNativeMethodName;

    public MethodContext(NativeObfuscator obfuscator, MethodNode method, int methodIndex, ClassNode clazz,
                         int classIndex) {
        this.obfuscator = obfuscator;
        this.method = method;
        this.methodIndex = methodIndex;
        this.clazz = clazz;
        this.classIndex = classIndex;

        this.output = new StringBuilder();
        this.nativeMethods = new StringBuilder();

        this.line = -1;
        this.stack = new ArrayList<>();
        this.locals = new ArrayList<>();
        for (int i = 0; i < method.tryCatchBlocks.size(); i++) {
            TryCatchBlockNode block = method.tryCatchBlocks.get(i);
            catchStarts.computeIfAbsent(block.start, label -> new ArrayList<>()).add(i);
            catchEnds.computeIfAbsent(block.end, label -> new ArrayList<>()).add(i);
        }
    }

    public void enterLabel(LabelNode label) {
        List<Integer> starting = catchStarts.get(label);
        List<Integer> ending = catchEnds.get(label);
        if (starting != null || ending != null) {
            if (starting != null) {
                activeCatches.addAll(starting);
            }
            if (ending != null) {
                activeCatches.removeAll(ending);
            }
            inlineExceptionCheck = null;
        }
    }

    public String getExceptionCheck() {
        getInlineExceptionCheck();
        return exceptionCheck;
    }

    public String getInlineExceptionCheck() {
        if (inlineExceptionCheck == null) {
            if (activeCatches.isEmpty()) {
                inlineExceptionCheck = getSnippets().getSnippet("TRYCATCH_EMPTY",
                        Collections.singletonMap("rettype", MethodProcessor.CPP_TYPES[ret.getSort()]));
            } else {
                Set<String> types = new HashSet<>();
                List<CatchesBlock.CatchBlock> blocks = new ArrayList<>();
                // JVM dispatch follows exception table order, including overlapping ranges.
                for (int index : activeCatches) {
                    TryCatchBlockNode block = method.tryCatchBlocks.get(index);
                    if (types.add(block.type)) {
                        blocks.add(new CatchesBlock.CatchBlock(block.type, block.handler));
                    }
                    if (block.type == null) {
                        break;
                    }
                }
                String target = catches.computeIfAbsent(new CatchesBlock(blocks), key -> "L_CATCH_" + catches.size());
                inlineExceptionCheck = getSnippets().getSnippet("TRYCATCH_START") + " goto " + target + "; }";
            }
            exceptionCheck = "\n    " + inlineExceptionCheck;
        }
        return inlineExceptionCheck;
    }

    public NodeCache<String> getCachedStrings() {
        return obfuscator.getCachedStrings();
    }

    public NodeCache<String> getCachedClasses() {
        return obfuscator.getCachedClasses();
    }

    public NodeCache<CachedMethodInfo> getCachedMethods() {
        return obfuscator.getCachedMethods();
    }

    public NodeCache<CachedFieldInfo> getCachedFields() {
        return obfuscator.getCachedFields();
    }

    public Snippets getSnippets() {
        return obfuscator.getSnippets();
    }

    public StringPool getStringPool() {
        return obfuscator.getStringPool();
    }

    public LabelPool getLabelPool() {
        return labelPool;
    }
}
