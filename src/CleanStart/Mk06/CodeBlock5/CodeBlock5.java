package CleanStart.Mk06.CodeBlock5;

import CleanStart.Mk06.Instruction;
import CleanStart.Mk06.Instructions.Label;
import SingleLinkedList.Mk01.SingleLinkedList;

public class CodeBlock5
{
	private final Label nameLabel;
	private final String name;
	private final Instruction[] instructions;
	
	private final CodeBlock5[] children;
	
	private CodeBlock5(Label inNameLabel, Instruction[] inInstructions, int numChildren)
	{
		this.nameLabel = inNameLabel;
		this.name = inNameLabel.getMangledName(false);
		this.instructions = inInstructions;
		this.children = new CodeBlock5[numChildren];
	}
	
	public static CodeBlock5 getInstance(Label inNameLabel, SingleLinkedList<Instruction> inInstructions, int numChildren)
	{
		return getInstance(inNameLabel, inInstructions.toArray(new Instruction[inInstructions.getSize()]), numChildren);
	}
	
	public static CodeBlock5 getInstance(Label inNameLabel, Instruction[] inInstructions, int numChildren)
	{
		return new CodeBlock5(inNameLabel, inInstructions, numChildren);
	}
}
