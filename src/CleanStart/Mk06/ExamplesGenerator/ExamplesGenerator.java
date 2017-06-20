package CleanStart.Mk06.ExamplesGenerator;

import CharList.CharList;

public class ExamplesGenerator
{
	private ExamplesGenerator()
	{
		// to prevent instantiation
	}

	public static CharList generateControlStatements(int depthOfNesting)
	{
		final CharList result = new CharList();

		return result;
	}

	private static CharList generateControlStatements_IfStatements(int depthOfNesting)
	{
		final CharList result = new CharList();

		for (int i = 0; i < depthOfNesting; i++)
		{
			result.add("If_");
			result.addAsString(i);
			
		}

		return result;
	}
	
	private static enum StatementTypes
	{
		Linear,
		If,
		IfElse;
	}
	
	private static abstract class ControlStatement
	{
		private static int instanceCounter = 0;
		
		private final int instanceNumber;
		
		protected ControlStatement()
		{
			this(instanceCounter++);
		}
		
		protected ControlStatement(int inInstanceNumber)
		{
			this.instanceNumber = inInstanceNumber;
		}
	}
	
	private static class LinearBlock extends ControlStatement
	{
		
	}
	
	private static class IfBlock extends ControlStatement
	{
		
	}
	
	private static class IfElseBlock extends ControlStatement
	{
		
	}
}
