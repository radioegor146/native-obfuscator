package by.radioegor146.instructions;

import by.radioegor146.MethodContext;
import by.radioegor146.Util;
import org.objectweb.asm.Label;
import org.objectweb.asm.tree.LookupSwitchInsnNode;

public class LookupSwitchHandler extends GenericInstructionHandler<LookupSwitchInsnNode> {

    @Override
    protected void process(MethodContext context, LookupSwitchInsnNode node) {
        StringBuilder output = context.output;

        output.append(getStart(context)).append("\n    ");

        for (int i = 0; i < node.labels.size(); ++i) {
            output.append(String.format("    %s\n    ", getPart(context,
                    node.keys.get(i),
                    node.labels.get(i).getLabel())));
        }
        output.append(String.format("    %s\n    ", getDefault(context, node.dflt.getLabel())));

        instructionName = "LOOKUPSWITCH_END";
    }

    private static String getStart(MethodContext context) {
        return context.getSnippets().getSnippet("LOOKUPSWITCH_START");
    }

    private static String getPart(MethodContext context, int key, Label label) {
        return context.getSnippets().getSnippet("LOOKUPSWITCH_PART", Util.createMap(
                "key", key,
                "label", context.getLabelPool().getName(label)
        ));
    }

    private static String getDefault(MethodContext context, Label label) {
        return context.getSnippets().getSnippet("LOOKUPSWITCH_DEFAULT", Util.createMap(
                "label", context.getLabelPool().getName(label)
        ));
    }

    @Override
    public String insnToString(MethodContext context, LookupSwitchInsnNode node) {
        return "LOOKUPSWITCH";
    }

    @Override
    public int getNewStackPointer(LookupSwitchInsnNode node, int currentStackPointer) {
        return currentStackPointer - 1;
    }
}
