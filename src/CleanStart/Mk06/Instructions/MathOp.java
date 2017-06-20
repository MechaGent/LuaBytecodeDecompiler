package CleanStart.Mk06.Instructions;

import CleanStart.Mk06.StepNum;
import CleanStart.Mk06.VarRef;
import CleanStart.Mk06.Enums.InferenceTypes;
import CleanStart.Mk06.Enums.InlineSchemes;
import CleanStart.Mk06.Enums.IrCodes;
import CleanStart.Mk06.VarFormula.Formula;

public class MathOp extends SetVar
{
	private static int instanceCounter = 0;

	private MathOp(StepNum inStartTime, VarRef inTarget, VarRef inCore)
	{
		super(IrCodes.MathOp, instanceCounter++, inStartTime, inTarget, inCore);
	}
	
	public static MathOp getInstance(StepNum inStartTime, VarRef inTarget, Formula inCore)
	{
		final MathOp result = new MathOp(inStartTime, inTarget, VarRef.getInstance_forWrapper(inCore, InferenceTypes.Formula));
		result.logAllReadsAndWrites();
		return result;
	}
	
	public static MathOp getInstance(StepNum inStartTime, VarRef inTarget, VarRef inCore)
	{
		final MathOp result = new MathOp(inStartTime, inTarget, inCore);
		result.logAllReadsAndWrites();
		return result;
	}

	@Override
	public InlineSchemes getRelevantSchemeForIndex(int inIndex)
	{
		return InlineSchemes.Inline_EveryTime;
	}

	@Override
	public void logAllReadsAndWrites()
	{
		this.logAllReadsAndWrites(InferenceTypes.Formula);
	}
}
