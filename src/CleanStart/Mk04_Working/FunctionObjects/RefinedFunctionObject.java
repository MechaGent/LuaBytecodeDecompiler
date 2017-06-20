package CleanStart.Mk04_Working.FunctionObjects;

import CharList.CharList;
import CleanStart.Mk04_Working.CodePrinter;
import CleanStart.Mk04_Working.Instruction;
import CleanStart.Mk04_Working.InstructionList;
import SingleLinkedList.Mk01.SingleLinkedList;

public class RefinedFunctionObject extends FunctionObject
{
	private final Instruction[] instructions;
	private final RefinedFunctionObject[] refinedFunctionPrototypes;

	public RefinedFunctionObject(RawFunctionObject inIn, Instruction[] inInstructions, RefinedFunctionObject[] inRefinedFunctionPrototypes)
	{
		super(inIn);
		this.instructions = inInstructions;
		this.refinedFunctionPrototypes = inRefinedFunctionPrototypes;
	}

	public static RefinedFunctionObject getInstance(RawFunctionObject raw)
	{
		final RefinedFunctionObject[] convertedPrototypes = convertPrototypes((RawFunctionObject[]) raw.getFunctionPrototypes());
		// final Instruction[] inInstructions = cullInstructions(raw.refineInstructions(convertedPrototypes));
		final Instruction[] inInstructions = cullInstructions(raw.refineInstructions2(convertedPrototypes));
		return new RefinedFunctionObject(raw, inInstructions, convertedPrototypes);
	}

	private static RefinedFunctionObject[] convertPrototypes(RawFunctionObject[] prototypes)
	{
		final RefinedFunctionObject[] result = new RefinedFunctionObject[prototypes.length];

		for (int i = 0; i < result.length; i++)
		{
			result[i] = getInstance(prototypes[i]);
		}

		return result;
	}

	public Instruction[] getInstructions()
	{
		return this.instructions;
	}

	@Override
	public FunctionObject[] getFunctionPrototypes()
	{
		return this.refinedFunctionPrototypes;
	}

	@Override
	protected CharList functionPrototypesToCharList(int offset)
	{
		final CharList result = new CharList();

		for (int i = 0; i < this.refinedFunctionPrototypes.length; i++)
		{
			result.addNewLine();
			result.add(this.refinedFunctionPrototypes[i].disassemble(offset, false), true);
		}

		return result;
	}

	@Override
	protected CharList disassembleInstructions(int offset)
	{
		final CharList result = new CharList();

		for (int i = 0; i < this.instructions.length; i++)
		{
			result.addNewLine();
			result.addAsPaddedString(this.instructions[i].getIrCode().toString(), 24, true);
			result.add(' ', offset);
			result.addAsPaddedString(Integer.toString(i), 4, true);
			result.add('\t');
			result.add(this.instructions[i].getVerboseCommentForm(), true);
		}

		return result;
	}

	public CharList translateInstructionsToCode()
	{
		return this.translateInstructionsToCode(0, true, false, false);
	}
	
	public CharList translateInstructionsToCode(int offset, boolean includeGlobals, boolean wantsCommentedRaws, boolean showInferredTypes)
	{
		final CharList result = new CharList();

		result.add('\t', offset++);
		result.add("FunctionName: ");
		result.add(this.name);
		result.addNewLine();
		result.add('\t', offset);
		result.add("numUpvalues: ");
		result.addAsString(this.numUpvalues);
		result.addNewLine();
		result.add('\t', offset);
		result.add("numParams: ");
		result.addAsString(this.numParams);
		result.addNewLine();
		result.add('\t', offset);
		result.add("numLocals: ");
		result.addAsString(this.numRegisters_max);
		result.addNewLine();
		result.add('\t', offset);
		result.add("VarArgFlags: ");
		result.add(this.VarArgFlags.toCharList(), true);
		result.addNewLine();
		result.add('\t', offset);
		result.add("Vars: {");
		result.addNewLine();
		result.add(this.Vars.toCharList(offset + 1, 4), true);

		if (includeGlobals)
		{
			result.addNewLine();
			result.add('\t', offset + 1);
			result.add("Globals: ");
			result.add(this.Vars.globalsToCharList(offset + 2), true);
		}

		result.addNewLine();
		result.add('\t', offset);
		result.add('}');

		result.addNewLine();
		result.add('\t', offset);
		result.add("Instructions: ");
		result.addNewLine();

		if (wantsCommentedRaws)
		{
			result.add(this.disassembleInstructions(offset + 1), true);
		}
		else
		{
			final CodePrinter temp = new CodePrinter();
			result.add(temp.toCode(this.instructions, offset + 1, showInferredTypes), true);
		}

		result.addNewLine();
		result.add('\t', offset);
		result.add("SubFunctions: ");

		final FunctionObject[] subs = this.getFunctionPrototypes();

		for (int i = 0; i < subs.length; i++)
		{
			result.addNewLine();
			result.add(subs[i].translateInstructionsToCode(offset + 1, false, wantsCommentedRaws, showInferredTypes), true);
		}

		// result.add(this.functionPrototypesToCharList(offset + 1), true);

		return result;
	}
	
	public CharList disassemble(int offset, boolean includeGlobals, boolean wantsCommentedRaws)
	{
		return this.disassemble(offset, includeGlobals, wantsCommentedRaws, true);
	}

	public CharList disassemble(int offset, boolean includeGlobals, boolean wantsCommentedRaws, boolean showInferredTypes)
	{
		final CharList result = new CharList();

		result.add('\t', offset++);
		result.add("FunctionName: ");
		result.add(this.name);
		result.addNewLine();
		result.add('\t', offset);
		result.add("numUpvalues: ");
		result.addAsString(this.numUpvalues);
		result.addNewLine();
		result.add('\t', offset);
		result.add("numParams: ");
		result.addAsString(this.numParams);
		result.addNewLine();
		result.add('\t', offset);
		result.add("numLocals: ");
		result.addAsString(this.numRegisters_max);
		result.addNewLine();
		result.add('\t', offset);
		result.add("VarArgFlags: ");
		result.add(this.VarArgFlags.toCharList(), true);
		result.addNewLine();
		result.add('\t', offset);
		result.add("Vars: {");
		result.addNewLine();
		result.add(this.Vars.toCharList(offset + 1, 4), true);

		if (includeGlobals)
		{
			result.addNewLine();
			result.add('\t', offset + 1);
			result.add("Globals: ");
			result.add(this.Vars.globalsToCharList(offset + 2), true);
		}

		result.addNewLine();
		result.add('\t', offset);
		result.add('}');

		if (wantsCommentedRaws)
		{
			result.addNewLine();
			result.add('\t', offset);
			result.add("Instructions: ");
			result.addNewLine();
			result.add(this.disassembleInstructions(offset + 1), true);
		}

		result.addNewLine();
		result.add('\t', offset);
		result.add("SubFunctions: ");

		final FunctionObject[] subs = this.getFunctionPrototypes();

		for (int i = 0; i < subs.length; i++)
		{
			result.addNewLine();
			result.add(subs[i].disassemble(offset + 1, false, wantsCommentedRaws), true);
		}

		// result.add(this.functionPrototypesToCharList(offset + 1), true);

		return result;
	}

	public static CharList disassembleInstructions(int offset, Instruction[] instructions)
	{
		final CharList result = new CharList();

		for (int i = 0; i < instructions.length; i++)
		{
			result.addNewLine();
			result.addAsPaddedString(instructions[i].getIrCode().toString(), 16, true);
			result.add(' ', offset);
			result.addAsPaddedString(Integer.toString(i), 4, true);
			result.add('\t');
			result.add(instructions[i].getVerboseCommentForm(), true);
		}

		return result;
	}

	public SingleLinkedList<RefinedFunctionObject> getAll_asQueue()
	{
		final SingleLinkedList<RefinedFunctionObject> result = new SingleLinkedList<RefinedFunctionObject>();
		final SingleLinkedList<RefinedFunctionObject> working = new SingleLinkedList<RefinedFunctionObject>();
		working.add(this);

		while (!working.isEmpty())
		{
			final RefinedFunctionObject loc = working.pop();

			working.push(loc.refinedFunctionPrototypes);

			result.add(loc);
		}

		return result;
	}

	/**
	 * use this only as a coordination method
	 * 
	 * @param in
	 * @return
	 */
	private static Instruction[] cullInstructions(SingleLinkedList<Instruction> in)
	{
		// final SingleLinkedList<Instruction> result = new SingleLinkedList<Instruction>();

		// return result.toArray(new Instruction[result.getSize()]);

		final Instruction[] result = new Instruction[in.getSize()];

		for (int i = 0; i < result.length; i++)
		{
			result[i] = in.pop();
			result[i].revVarTypes();
		}

		return result;
	}

	private static Instruction[] cullInstructions(InstructionList in)
	{
		// final SingleLinkedList<Instruction> result = new SingleLinkedList<Instruction>();

		// return result.toArray(new Instruction[result.getSize()]);
		in.popLeadingNonOps();

		final Instruction[] result = new Instruction[in.getSize()];
		Instruction current = in.getHead();

		for (int i = 0; i < result.length; i++)
		{
			result[i] = current;
			current = current.getNext();
			result[i].revVarTypes();
		}

		return result;
	}
}
