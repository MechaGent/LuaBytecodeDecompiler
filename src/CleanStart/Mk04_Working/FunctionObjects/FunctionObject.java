package CleanStart.Mk04_Working.FunctionObjects;

import CharList.CharList;
import CleanStart.Mk04_Working.VarsDispenser;

public abstract class FunctionObject
{
	protected static final int RK_Mask = 0x100;
	protected static final int FieldsPerFlush = 50;

	protected String name;
	protected final int numUpvalues;
	protected final int numParams;
	protected final VarArgFlagSet VarArgFlags;
	protected final int numRegisters_max; // maxStackSize
	// instructions go here, in terms of order
	protected final int[] sourceLinePositions;
	protected final VarsDispenser Vars; // includes locals, upvalues, and globals

	public FunctionObject(String inName, int inNumUpvalues, int inNumParams, VarArgFlagSet inVarArgFlags, int inNumRegisters_max, int[] inSourceLinePositions, VarsDispenser inVars)
	{
		this.name = inName;
		this.numUpvalues = inNumUpvalues;
		this.numParams = inNumParams;
		this.VarArgFlags = inVarArgFlags;
		this.numRegisters_max = inNumRegisters_max;
		this.sourceLinePositions = inSourceLinePositions;
		this.Vars = inVars;
	}

	public FunctionObject(RawFunctionObject in)
	{
		this.name = in.name;
		this.numUpvalues = in.numUpvalues;
		this.numParams = in.numParams;
		this.VarArgFlags = in.VarArgFlags;
		this.numRegisters_max = in.numRegisters_max;

		this.sourceLinePositions = in.sourceLinePositions;
		this.Vars = in.Vars;
	}

	public String getName()
	{
		return this.name;
	}

	public int getNumParams()
	{
		return this.numParams;
	}

	public VarArgFlagSet getVarArgFlags()
	{
		return this.VarArgFlags;
	}

	public int getNumRegisters_max()
	{
		return this.numRegisters_max;
	}

	public abstract FunctionObject[] getFunctionPrototypes();

	public int[] getSourceLinePositions()
	{
		return this.sourceLinePositions;
	}

	public VarsDispenser getVars()
	{
		return this.Vars;
	}

	protected abstract CharList functionPrototypesToCharList(int offset);

	protected abstract CharList disassembleInstructions(int offset);

	public CharList disassemble(int offset)
	{
		return this.disassemble(offset, false);
	}

	public CharList disassemble(int offset, boolean includeGlobals)
	{
		return this.disassemble(offset, includeGlobals, true);
	}

	public CharList disassemble(int offset, boolean includeGlobals, boolean includeInstructions)
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
		result.add('}');

		if (includeGlobals)
		{
			result.addNewLine();
			result.add('\t', offset + 1);
			result.add("Globals: ");
			result.add(this.Vars.globalsToCharList(offset + 2), true);
		}
		
		if (includeInstructions)
		{
			result.addNewLine();
			result.add('\t', offset);
			result.add("Instructions: ");
			result.add(this.disassembleInstructions(offset + 1), true);
			//throw new IllegalArgumentException();
		}

		result.addNewLine();
		result.add('\t', offset);
		result.add("SubFunctions: ");
		
		final FunctionObject[] subs = this.getFunctionPrototypes();
		
		for(int i = 0; i < subs.length; i++)
		{
			result.addNewLine();
			result.add(subs[i].disassemble(offset + 1, false, includeInstructions), true);
		}
		
		//result.add(this.functionPrototypesToCharList(offset + 1), true);

		return result;
	}

	public static class VarArgFlagSet
	{
		private final boolean hasArg;
		private final boolean isVarArg;
		private final boolean needsArg;

		public VarArgFlagSet(int isVarArgsFlagByte)
		{
			this.hasArg = (isVarArgsFlagByte & 1) == 1;
			this.isVarArg = (isVarArgsFlagByte & 2) == 1;
			this.needsArg = (isVarArgsFlagByte & 4) == 1;
		}

		public boolean hasArg()
		{
			return this.hasArg;
		}

		public boolean isVarArg()
		{
			return this.isVarArg;
		}

		public boolean needsArg()
		{
			return this.needsArg;
		}

		public CharList toCharList()
		{
			final CharList result = new CharList();

			result.add('{');
			result.add("hasArg: ");
			result.addAsString(this.hasArg);
			result.add(", ");
			result.add("isVarArg: ");
			result.addAsString(this.isVarArg);
			result.add(", ");
			result.add("needsArg: ");
			result.addAsString(this.needsArg);
			result.add('}');

			return result;
		}
	}

	public abstract CharList translateInstructionsToCode(int offset, boolean includeGlobals, boolean inWantsCommentedRaws, boolean inShowInferredTypes);
}
