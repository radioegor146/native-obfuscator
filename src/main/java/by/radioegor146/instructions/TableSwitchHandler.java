package by.radioegor146.instructions;

import by.radioegor146.MethodContext;
import by.radioegor146.Util;
import org.objectweb.asm.tree.TableSwitchInsnNode;

public class TableSwitchHandler extends GenericInstructionHandler<TableSwitchInsnNode> {

    @Override
    protected void process(MethodContext context, TableSwitchInsnNode node) {
        StringBuilder output = context.output;
        output.append(context.getSnippets().getSnippet("TABLESWITCH_START")).append("\n");

        for (int switchIndex = 0; switchIndex < node.labels.size(); ++switchIndex) {
            output.append(String.format("        %s\n",
                    context.getSnippets().getSnippet("TABLESWITCH_PART", Util.createMap(
                            "index", String.valueOf(node.min + switchIndex),
                            "label", String.valueOf(node.labels.get(switchIndex).getLabel())
                    ))));
        }
        output.append(String.format("        %s\n",
                context.obfuscator.getSnippets().getSnippet("TABLESWITCH_DEFAULT", Util.createMap(
                        "label", String.valueOf(node.dflt.getLabel())
                ))));
        output.append("    ");
        instructionName = "TABLESWITCH_END";
    }
}
