package LuaBytecodeDecompiler.Mk03.AnnoInstructions.ABC;

import DataStructures.Linkages.CharList.Mk03.CharList;
import LuaBytecodeDecompiler.Mk01.Abstract.Enums.AbstractOpCodes;
import LuaBytecodeDecompiler.Mk02.MiscAutobot;
import LuaBytecodeDecompiler.Mk02.Instructions.InstructionTypes;
import LuaBytecodeDecompiler.Mk03.AnnoInstruction;
import LuaBytecodeDecompiler.Mk03.AnnoInstructions.VarFlowHandler;
import LuaBytecodeDecompiler.Mk03.AnnoVars.AnnoVar;

public class AnnoInstruction_NewTable extends AnnoInstruction
{
	private final AnnoVar target;
	private final int length;
	private final int hashSize;

	public AnnoInstruction_NewTable(int inRaw, int inI, AnnoVar inTarget, int inLength, int inHashSize)
	{
		super(inRaw, inI, InstructionTypes.iABC, AbstractOpCodes.NewTable);
		this.target = inTarget;
		this.length = inLength;
		this.hashSize = inHashSize;
	}

	@Override
	protected CharList toCharList_internal(boolean inIsVerbose)
	{
		return this.toPseudoJava(0);
	}

	public AnnoVar getTarget()
	{
		return this.target;
	}

	public int getLength()
	{
		return this.length;
	}

	public int getHashSize()
	{
		return this.hashSize;
	}

	@Override
	public void addAllReadsAndWrites(VarFlowHandler in)
	{
		in.addVarWrite(this.target, this, this.getInstructionAddress(), 0);
	}

	@Override
	public CharList toCharList_InlineForm()
	{
		final CharList result = new CharList();

		result.add(MiscAutobot.GlobalMethods_createLuaTable);
		result.add(Integer.toString(this.length));
		result.add(", ");
		result.add(Integer.toString(this.hashSize));
		result.add(')');

		return result;
	}

	@Override
	public AnnoInstruction replaceStep(int inSubstep, AnnoVar inReplacement)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public CharList toPseudoJava(int inOffset)
	{
		final CharList result = new CharList();

		result.add('\t', inOffset);
		result.add(this.target.getAdjustedName());
		result.add(" = ");
		result.add(MiscAutobot.GlobalMethods_createLuaTable);
		result.add(Integer.toString(this.length));
		result.add(", ");
		result.add(Integer.toString(this.hashSize));
		result.add(')');

		return result;
	}
}
