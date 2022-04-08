package by.radioegor146.instructions;

import by.radioegor146.MethodContext;
import by.radioegor146.Util;
import org.objectweb.asm.tree.LabelNode;

public class LabelHandler extends GenericInstructionHandler<LabelNode> {

    @Override
    public void accept(MethodContext context, LabelNode node) {
        context.method.tryCatchBlocks.stream().filter(x -> x.start.equals(node))
                .forEachOrdered(context.tryCatches::add);
        context.method.tryCatchBlocks.stream().filter(x -> x.end.equals(node))
                .forEachOrdered(context.tryCatches::remove);

    	try {
    		super.accept(context, node);
    	} catch (UnsupportedOperationException ex) {
    		// ignored
    	}
        context.output.append(String.format("%s: %s\n", context.getLabelPool().getName(node.getLabel()), trimmedTryCatchBlock));
    }

    @Override
    protected void process(MethodContext context, LabelNode node) {
    	throw new UnsupportedOperationException("break at super.process()");
    }
}
