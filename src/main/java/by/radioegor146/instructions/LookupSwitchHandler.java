package by.radioegor146.instructions;

import by.radioegor146.MethodContext;
import by.radioegor146.Util;
import org.objectweb.asm.tree.LookupSwitchInsnNode;

public class LookupSwitchHandler extends GenericInstructionHandler<LookupSwitchInsnNode> {

    @Override
    protected void process(MethodContext context, LookupSwitchInsnNode node) {
        StringBuilder output = context.output;
        output.append(context.getSnippets().getSnippet("LOOKUPSWITCH_START")).append("\n");
        for (int switchIndex = 0; switchIndex < node.labels.size(); ++switchIndex) {
            output.append(String.format("        %s\n",
                    context.getSnippets().getSnippet("LOOKUPSWITCH_PART", Util.createMap(
                            "key", node.keys.get(switchIndex),
                            "label", node.labels.get(switchIndex).getLabel()
                    ))));
        }
        output.append(String.format("        %s\n",
                context.getSnippets().getSnippet("LOOKUPSWITCH_DEFAULT", Util.createMap(
                        "label", node.dflt.getLabel()
                ))));
        instructionName = "LOOKUPSWITCH_END";
    }
}
