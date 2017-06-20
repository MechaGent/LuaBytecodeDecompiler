package CleanStart.Mk04_Working.ControlStructures;

import CleanStart.Mk04_Working.Instruction;

public class LinearFlowStruct extends BaseFlowStruct
{
	private final Instruction[] lines;

	public LinearFlowStruct(Instruction[] inLines)
	{
		super();
		this.lines = inLines;
	}
}
