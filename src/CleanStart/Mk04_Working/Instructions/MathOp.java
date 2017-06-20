package CleanStart.Mk04_Working.Instructions;

import CharList.CharList;
import CleanStart.Mk04_Working.CanBeInlinedTypes;
import CleanStart.Mk04_Working.Variable;
import CleanStart.Mk04_Working.Instructions.Enums.IrCodes;
import CleanStart.Mk04_Working.VarFormula.Formula;
import CleanStart.Mk04_Working.Variables.MystVar;

public class MathOp extends BaseInstruct
{
	private static int instanceCounter = 0;
	
	private final MystVar target;
	private final Formula cargo;

	private MathOp(int inInstanceNumber, MystVar inTarget, Formula inCargo)
	{
		super(IrCodes.MathOp, inInstanceNumber);
		this.target = inTarget;
		this.cargo = inCargo;
	}
	
	public static MathOp getInstance(MystVar inTarget, Formula inCargo)
	{
		return new MathOp(instanceCounter++, inTarget, inCargo);
	}

	@Override
	public CanBeInlinedTypes canBeInlined()
	{
		return CanBeInlinedTypes.Yes;
	}

	@Override
	public Variable getInlinedForm()
	{
		return this.cargo;
	}
	
	@Override
	public CharList getVerboseCommentForm()
	{
		final CharList result = new CharList();
		
		result.add(this.target.getMostRelevantIdValue());
		result.add(" = ");
		result.add(this.cargo.getMostRelevantIdValue());
		
		return result;
	}

	public MystVar getTarget()
	{
		return this.target;
	}

	public Formula getCargo()
	{
		return this.cargo;
	}

	@Override
	public void revVarTypes()
	{
		this.cargo.revVarType();
		this.target.revVarType();
	}
}
