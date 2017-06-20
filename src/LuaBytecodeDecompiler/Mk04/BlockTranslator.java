package LuaBytecodeDecompiler.Mk04;

import DataStructures.Linkages.CharList.Mk03.CharList;

public class BlockTranslator
{
	public static CodeBlock[] applyFormalRules(CodeBlock in)
	{

	}

	private static interface Expression
	{
		public String getMangledName();
	}

	private static class SingleExpression implements Expression
	{
		private final CodeBlock cargo;

		@Override
		public String getMangledName()
		{
			return this.cargo.getMangledName();
		}
	}

	private static abstract class MultiExpression implements Expression
	{
		private final Expression[] subexpressions;
	}

	private static class MultiExpression_And extends MultiExpression
	{
		@Override
		public String getMangledName()
		{
			final CharList result = new CharList();

			return result.toString();
		}
	}

	private static class MultiExpression_Or extends MultiExpression
	{

		@Override
		public String getMangledName()
		{
			final CharList result = new CharList();

			return result.toString();
		}

	}
}
