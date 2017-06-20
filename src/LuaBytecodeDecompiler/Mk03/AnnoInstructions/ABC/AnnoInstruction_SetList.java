package LuaBytecodeDecompiler.Mk03.AnnoInstructions.ABC;

import DataStructures.Linkages.CharList.Mk03.CharList;
import LuaBytecodeDecompiler.Mk01.Abstract.Enums.AbstractOpCodes;
import LuaBytecodeDecompiler.Mk02.Instructions.InstructionTypes;
import LuaBytecodeDecompiler.Mk03.AnnoInstruction;
import LuaBytecodeDecompiler.Mk03.AnnoInstructions.VarFlowHandler;
import LuaBytecodeDecompiler.Mk03.AnnoVars.AnnoVar;

public class AnnoInstruction_SetList extends AnnoInstruction
{
	private static final int FieldsPerFlush = 50;
	private static final String zeroCStub = "((((int) nextInstruction) - 1) * " + FieldsPerFlush;

	private final AnnoVar targetArray;
	private final AnnoVar[] sources;
	private final int B;
	private final int C;

	public AnnoInstruction_SetList(int inRaw, int inI, AnnoVar inTargetArray, AnnoVar[] inSources, int inB, int inC)
	{
		super(inRaw, inI, InstructionTypes.iABC, AbstractOpCodes.SetList);
		this.targetArray = inTargetArray;
		this.sources = inSources;
		this.B = inB;
		this.C = inC;
	}

	@Override
	protected CharList toCharList_internal(boolean inIsVerbose)
	{
		final CharList result = new CharList();

		result.add(this.targetArray.getAdjustedName());
		result.add("[from ");

		if (this.C == 0)
		{
			result.add(zeroCStub);
			result.add(" + 1) to ");
			result.add(zeroCStub);
			result.add(" + ");
			result.add(Integer.toString(this.sources.length));
			result.add(")]");
		}
		else
		{
			final int CMinusOne = this.C - 1;
			final int start = CMinusOne * FieldsPerFlush + 1;
			final int end = CMinusOne * FieldsPerFlush + this.sources.length;

			result.add(Integer.toString(start));
			result.add(" to ");
			result.add(Integer.toString(end));
			result.add(']');
		}

		result.add(" = ");
		result.add(this.sources[0].getAdjustedName());

		for (int i = 1; i < this.sources.length; i++)
		{
			result.add(", ");
			result.add(this.sources[i].getAdjustedName());
		}

		return result;
	}

	public static int getFieldsPerFlush()
	{
		return FieldsPerFlush;
	}

	public AnnoVar getTargetArray()
	{
		return this.targetArray;
	}

	public AnnoVar[] getSources()
	{
		return this.sources;
	}

	public int getB()
	{
		return this.B;
	}

	public int getC()
	{
		return this.C;
	}

	@Override
	public void addAllReadsAndWrites(VarFlowHandler in)
	{
		final int place = this.getInstructionAddress();
		int count = 0;

		for (int i = 0; i < this.sources.length; i++)
		{
			in.addVarRead(this.sources[i], this, place, count++);
			in.addVarWrite(this.targetArray, this, place, count++);
		}
	}

	@Override
	public CharList toCharList_InlineForm()
	{
		final CharList result = new CharList();
		
		result.add(this.sources[0].getAdjustedName());

		for (int i = 1; i < this.sources.length; i++)
		{
			result.add(", ");
			result.add(this.sources[i].getAdjustedName());
		}

		return result;
	}

	@Override
	public AnnoInstruction replaceStep(int inSubstep, AnnoVar inReplacement)
	{
		this.sources[inSubstep / 2] = inReplacement;

		return this;
	}

	@Override
	public CharList toPseudoJava(int inOffset)
	{
		final CharList result = new CharList();
		
		result.addNewLine();
		
		final String targetArrayElem = this.targetArray.getAdjustedName() + '[';

		if (this.C == 0)
		{
			throw new UnsupportedOperationException();
		}
		else
		{
			final int CMinusOne = this.C - 1;
			final int start = CMinusOne * FieldsPerFlush + 1;
			final int end = CMinusOne * FieldsPerFlush + this.sources.length;
			int sourcesOffset = 0;

			for(int i = start; i <= end; i++)
			{
				result.add('\t', inOffset);
				result.add(targetArrayElem);
				result.add(Integer.toString(i));
				result.add("] = ");
				result.add(this.sources[sourcesOffset++].getAdjustedName());
				result.add(";\r\n");
			}
		}

		return result;
	}
}
