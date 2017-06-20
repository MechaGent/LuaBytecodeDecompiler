package CleanStart.Mk04_Working.Instructions;

import CharList.CharList;
import CleanStart.Mk04_Working.CanBeInlinedTypes;
import CleanStart.Mk04_Working.Variable;
import CleanStart.Mk04_Working.Instructions.Enums.IrCodes;
import CleanStart.Mk04_Working.Variables.MystVar;

public class CopyArray extends BaseInstruct
{
	private static int instanceCounter = 0;
	
	private final int offsetOffset;
	private final MystVar targetTable;
	private final MystVar[] sourceArray;
	private final int firstSourceIndex;
	private final String sourceArrayName;
	private final boolean sourcesAreVariable;

	private CopyArray(int inOffsetOffset, MystVar inTargetTable, MystVar[] inSourceArray, int inFirstSourceIndex, String inSourceArrayName, boolean inSourcesAreVariable)
	{
		super(IrCodes.CopyArray, instanceCounter++);
		this.offsetOffset = inOffsetOffset;
		this.targetTable = inTargetTable;
		this.sourceArray = inSourceArray;
		this.firstSourceIndex = inFirstSourceIndex;
		this.sourceArrayName = inSourceArrayName;
		this.sourcesAreVariable = inSourcesAreVariable;
	}

	public static CopyArray getInstance(int inOffsetOffset, MystVar inTargetTable, MystVar[] inSourceArray, int inFirstSourceIndex, String inSourceArrayName, boolean inSourcesAreVariable)
	{
		return new CopyArray(inOffsetOffset, inTargetTable, inSourceArray, inFirstSourceIndex, inSourceArrayName, inSourcesAreVariable);
	}

	public int getOffsetOffset()
	{
		return this.offsetOffset;
	}

	public MystVar getTargetTable()
	{
		return this.targetTable;
	}

	public MystVar[] getSourceArray()
	{
		return this.sourceArray;
	}
	
	public int getFirstSourceIndex()
	{
		return this.firstSourceIndex;
	}

	public String getSourceArrayName()
	{
		return this.sourceArrayName;
	}

	public boolean sourcesAreVariable()
	{
		return this.sourcesAreVariable;
	}

	@Override
	public CanBeInlinedTypes canBeInlined()
	{
		return CanBeInlinedTypes.NonApplicable;
	}

	@Override
	public Variable getInlinedForm()
	{
		return null;
	}
	
	@Override
	public CharList getVerboseCommentForm()
	{
		final CharList result = new CharList();
		
		result.add("<> NeedToImplement: ");
		result.add(this.IrCode.toString());
		result.add(" - ");
		result.add(this.toString());
		
		return result;
	}

	@Override
	public void revVarTypes()
	{
		this.targetTable.revVarType();
		
		for(MystVar curr: this.sourceArray)
		{
			curr.revVarType();
		}
	}
}
