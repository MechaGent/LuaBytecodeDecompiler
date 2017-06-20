package CleanStart.Mk04_Working.Instructions;

import CharList.CharList;
import CleanStart.Mk04_Working.CanBeInlinedTypes;
import CleanStart.Mk04_Working.Variable;
import CleanStart.Mk04_Working.Instructions.Enums.IrCodes;
import CleanStart.Mk04_Working.Variables.MystVar;

public class HandleVarArgs extends BaseInstruct
{
	private static int instanceCounter = 0;
	
	private final MystVar[] targets;
	private final boolean copyAllPossibles;
	
	private HandleVarArgs(MystVar[] inTargets, boolean inCopyAllPossibles)
	{
		super(IrCodes.VarArgsHandler, instanceCounter++);
		this.targets = inTargets;
		this.copyAllPossibles = inCopyAllPossibles;
	}
	
	public static HandleVarArgs getInstance(MystVar[] inTargets, boolean inCopyAllPossibles)
	{
		return new HandleVarArgs(inTargets, inCopyAllPossibles);
	}

	public MystVar[] getTargets()
	{
		return this.targets;
	}

	public boolean isCopyAllPossibles()
	{
		return this.copyAllPossibles;
	}

	@Override
	public CanBeInlinedTypes canBeInlined()
	{
		return CanBeInlinedTypes.No;
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
		for(MystVar curr: this.targets)
		{
			curr.revVarType();
		}
	}
}
