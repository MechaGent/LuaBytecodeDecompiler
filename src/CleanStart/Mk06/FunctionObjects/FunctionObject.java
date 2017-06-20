package CleanStart.Mk06.FunctionObjects;

import CharList.CharList;
import CleanStart.Mk06.MiscAutobot;
import CleanStart.Mk06.StepNum;
import CleanStart.Mk06.VarRef;
import HalfByteRadixMap.HalfByteRadixMap;

public abstract class FunctionObject
{
	private static final int numPerRow_Constants = 4;
	private static final int numPerRow_Globals = 4;

	protected static final int RK_Mask = 0x100;
	protected static final int FieldsPerFlush = 50;

	protected String name;
	protected final int numUpvalues;
	protected final int numParams;
	protected final VarArgFlagSet VarArgFlags;
	protected final int numRegisters_max; // maxStackSize
	// instructions go here, in terms of order
	protected final int[] sourceLinePositions;

	protected final VarRef[] Constants;
	protected final VarRef[] Locals;
	protected final VarRef[] Upvalues;
	protected final HalfByteRadixMap<VarRef> globals;

	public FunctionObject(String inName, int inNumUpvalues, int inNumParams, VarArgFlagSet inVarArgFlags, int inNumRegisters_max, int[] inSourceLinePositions, VarRef[] inConstants, VarRef[] inLocals, VarRef[] inUpvalues, HalfByteRadixMap<VarRef> inGlobals)
	{
		this.name = inName;
		this.numUpvalues = inNumUpvalues;
		this.numParams = inNumParams;
		this.VarArgFlags = inVarArgFlags;
		this.numRegisters_max = inNumRegisters_max;
		this.sourceLinePositions = inSourceLinePositions;
		this.Constants = inConstants;
		this.Locals = inLocals;
		this.Upvalues = inUpvalues;
		this.globals = inGlobals;
	}

	public FunctionObject(RawFunctionObject in)
	{
		this.name = in.name;
		this.numUpvalues = in.numUpvalues;
		this.numParams = in.numParams;
		this.VarArgFlags = in.VarArgFlags;
		this.numRegisters_max = in.numRegisters_max;

		this.sourceLinePositions = in.sourceLinePositions;
		this.Constants = in.Constants;
		this.Locals = in.Locals;
		this.Upvalues = in.Upvalues;
		this.globals = in.globals;
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

	public VarRef[] getConstants()
	{
		return this.Constants;
	}

	public VarRef[] getLocals()
	{
		return this.Locals;
	}

	public VarRef[] getUpvalues()
	{
		return this.Upvalues;
	}

	public HalfByteRadixMap<VarRef> getGlobals()
	{
		return this.globals;
	}

	protected abstract CharList disassembleInstructions(int offset, boolean showInferredTypes);
	protected abstract CharList decompileInstructions(int offset, boolean showInferredTypes);

	public CharList generateHeader(int offset, boolean includeGlobals)
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
		result.add('\t', offset + 1);
		result.add("Constants: {");
		result.addNewLine();
		result.add(MiscAutobot.prettyPrintVarRefArray(this.Constants, offset + 2, numPerRow_Constants, StepNum.getFirstStep()), true);
		result.addNewLine();
		result.add('\t', offset + 1);
		result.add('}');

		if (includeGlobals)
		{
			result.addNewLine();
			result.add('\t', offset + 1);
			result.add("Globals: ");
			result.add(MiscAutobot.prettyPrintVarRefMap(this.globals, offset + 2, numPerRow_Globals), true);
		}
		
		result.addNewLine();
		result.add('\t', offset);
		result.add('}');

		return result;
	}

	public CharList disassemble(int offset, boolean includeGlobals, boolean includeInstructions, boolean showInferredTypes)
	{
		final CharList result = this.generateHeader(offset, includeGlobals);

		if (includeInstructions)
		{
			result.addNewLine();
			result.add('\t', offset);
			result.add("Instructions: ");
			result.add(this.disassembleInstructions(offset + 1, showInferredTypes), true);
		}

		result.addNewLine();
		result.add('\t', offset);
		result.add("SubFunctions: ");

		final FunctionObject[] subs = this.getFunctionPrototypes();

		for (int i = 0; i < subs.length; i++)
		{
			result.addNewLine();
			result.add(subs[i].disassemble(offset + 1, false, includeInstructions, showInferredTypes), true);
		}

		return result;
	}

	public CharList decompile(int offset, boolean includeGlobals, boolean includeInstructions, boolean showInferredTypes)
	{
		final CharList result = this.generateHeader(offset, includeGlobals);

		if (includeInstructions)
		{
			result.addNewLine();
			result.add('\t', offset);
			result.add("Instructions: ");
			result.add(this.disassembleInstructions(offset + 1, showInferredTypes), true);
		}

		result.addNewLine();
		result.add('\t', offset);
		result.add("SubFunctions: ");

		final FunctionObject[] subs = this.getFunctionPrototypes();

		for (int i = 0; i < subs.length; i++)
		{
			result.addNewLine();
			result.add(subs[i].decompile(offset + 1, false, includeInstructions, showInferredTypes), true);
		}

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
}
