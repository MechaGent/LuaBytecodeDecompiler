package CleanStart.Mk06.FunctionObjects;

public class RefinementStagesChecklist
{
	private final boolean shouldReinsertLabels;
	private final boolean shouldDeduceLoops;
	private final boolean shouldProcessSimpleIfElseStatements;

	private RefinementStagesChecklist(boolean inShouldReinsertLabels, boolean inShouldDeduceLoops, boolean inShouldProcessSimpleIfElseStatements)
	{
		this.shouldReinsertLabels = inShouldReinsertLabels;
		this.shouldDeduceLoops = inShouldDeduceLoops;
		this.shouldProcessSimpleIfElseStatements = inShouldProcessSimpleIfElseStatements;
	}

	public static RefinementStagesChecklist getInstance(boolean inShouldReinsertLabels, boolean inShouldDeduceLoops, boolean inShouldProcessSimpleIfElseStatements)
	{
		return new RefinementStagesChecklist(inShouldReinsertLabels, inShouldDeduceLoops, inShouldProcessSimpleIfElseStatements);
	}

	public boolean shouldReinsertLabels()
	{
		return this.shouldReinsertLabels;
	}

	public boolean shouldDeduceLoops()
	{
		return this.shouldDeduceLoops;
	}
	
	public boolean shouldProcessSimpleIfElseStatements()
	{
		return this.shouldProcessSimpleIfElseStatements;
	}
}