package CleanStart.Mk01.VarFormula;

import java.util.Iterator;

import CharList.CharList;
import CleanStart.Mk01.StepStage;
import CleanStart.Mk01.Enums.Comparisons;
import CleanStart.Mk01.Enums.VariableTypes;
import CleanStart.Mk01.Interfaces.Variable;
import CleanStart.Mk01.Variables.Var_InstancedFormula;
import SingleLinkedList.Mk01.SingleLinkedList;

public class Formula implements Variable
{
	private static int instanceCounter = 0;
	
	private final String mangledName;
	private final FormulaToken[] tokens;

	public Formula(FormulaToken[] inTokens)
	{
		this.mangledName = "Formula_" + instanceCounter++;
		this.tokens = inTokens;
	}

	public static Formula parse(Iterable<FormulaToken> infixedFormula)
	{
		return parse(infixedFormula.iterator());
	}

	public static Formula parse(Iterator<FormulaToken> infixedFormula)
	{
		final SingleLinkedList<Operators> stack = new SingleLinkedList<Operators>();
		final SingleLinkedList<FormulaToken> postfix = new SingleLinkedList<FormulaToken>();

		while (infixedFormula.hasNext())
		{
			final FormulaToken current = infixedFormula.next();

			if (current.isOperand())
			{
				postfix.add(current);
			}
			else
			{
				// is operator
				final Operators op = (Operators) current;

				if (stack.isEmpty() || op == Operators.Scope_OpenBrace)
				{
					stack.push(op);
				}
				else if (op == Operators.Scope_CloseBrace)
				{
					Operators curr = stack.pop();

					// while (!stack.isEmpty() && (curr = stack.pop()) != Operators.Scope_OpenBrace)
					while (curr != null && curr != Operators.Scope_OpenBrace)
					{
						postfix.add(curr);
						// System.out.println(curr + "");
						curr = stack.pop();
					}

					// System.out.println(curr + "");
				}
				else
				{
					final int inPrecedence = op.getPrecedence();

					while (!stack.isEmpty())
					{
						final Operators curr = stack.pop();

						if (curr.isLeftAssociative())
						{
							if (curr.getPrecedence() < inPrecedence)
							{
								stack.push(curr);
								break;
							}
						}
						else
						{
							if (curr.getPrecedence() <= inPrecedence)
							{
								stack.push(curr);
								break;
							}
						}

						postfix.add(curr);
					}

					stack.push(op);
				}
			}
		}

		while (!stack.isEmpty())
		{
			postfix.add(stack.pop());
		}

		return new Formula(postfix.toArray(new FormulaToken[postfix.getSize()]));
	}

	public static Formula combine(Formula var1, Operators operator, Formula var2)
	{
		final FormulaToken[] core = new FormulaToken[var1.tokens.length + var2.tokens.length + 1];

		for (int i = 0; i < var1.tokens.length; i++)
		{
			core[i] = var1.tokens[i];
		}

		for (int i = var1.tokens.length; i < core.length - 1; i++)
		{
			core[i] = var2.tokens[i - var1.tokens.length];
		}

		core[core.length - 1] = operator;

		return new Formula(core);
	}

	public CharList tokensToCharList()
	{
		final CharList result = new CharList();

		for (int i = 0; i < this.tokens.length; i++)
		{
			if (i != 0)
			{
				result.add(' ');
			}

			result.add(this.tokens[i].valueToString());
		}

		return result;
	}

	@Override
	public String toString()
	{
		return this.toCharList().toString();
	}
	
	public CharList toCharList()
	{
		final SingleLinkedList<FormulaBuilder> stack = new SingleLinkedList<FormulaBuilder>();

		for (int i = 0; i < this.tokens.length; i++)
		{
			final FormulaToken current = this.tokens[i];

			if (current.isOperand())
			{
				final FormulaBuilder next = new FormulaBuilder();
				next.add(current);
				stack.push(next);
			}
			else
			{
				// is operator
				final Operators cast = (Operators) current;

				if (stack.getSize() < cast.getNumOperands())
				{
					throw new NullPointerException("stacksize: " + stack.getSize() + "\toperator: " + cast.toString());
				}
				else
				{
					switch (cast.getNumOperands())
					{
						case 1:
						{
							final FormulaBuilder var1 = stack.getFirst();

							if (cast.getPrecedence() > var1.lastPrec)
							{
								var1.push(Operators.Scope_OpenBrace);
								var1.add(Operators.Scope_CloseBrace);
							}

							if (cast.isLeftAssociative())
							{
								var1.add(cast);
							}
							else
							{
								var1.push(cast);
							}

							var1.lastPrec = cast.getPrecedence();

							break;
						}
						case 2:
						{
							final FormulaBuilder var2 = stack.pop();
							final FormulaBuilder var1 = stack.pop();

							// final String var1Before = var1.toString();
							// final String var2Before = var2.toString();

							if (cast.getPrecedence() > var1.lastPrec)
							{
								var1.push(Operators.Scope_OpenBrace);
								var1.add(Operators.Scope_CloseBrace);
							}

							var1.add(cast);

							var1.add(var2);

							stack.push(var1);
							var1.lastPrec = cast.getPrecedence();

							// System.out.println("var 1: " + var1Before + "\top: " + cast.getSymbol() + "\tvar2: " + var2Before + "\tyields: " + var1.toString());
							break;
						}
						default:
						{
							throw new IndexOutOfBoundsException(cast.toString());
						}
					}
				}
			}
		}

		if (stack.getSize() == 1)
		{
			return stack.pop().toCharList();
		}
		else
		{
			throw new IndexOutOfBoundsException();
		}
	}
	
	public CharList toCharList(StepStage inTime)
	{
		final SingleLinkedList<FormulaBuilder> stack = new SingleLinkedList<FormulaBuilder>();

		for (int i = 0; i < this.tokens.length; i++)
		{
			final FormulaToken current = this.tokens[i];

			if (current.isOperand())
			{
				final FormulaBuilder next = new FormulaBuilder();
				next.add(current);
				stack.push(next);
			}
			else
			{
				// is operator
				final Operators cast = (Operators) current;

				if (stack.getSize() < cast.getNumOperands())
				{
					throw new NullPointerException("stacksize: " + stack.getSize() + "\toperator: " + cast.toString());
				}
				else
				{
					switch (cast.getNumOperands())
					{
						case 1:
						{
							final FormulaBuilder var1 = stack.getFirst();

							if (cast.getPrecedence() > var1.lastPrec)
							{
								var1.push(Operators.Scope_OpenBrace);
								var1.add(Operators.Scope_CloseBrace);
							}

							if (cast.isLeftAssociative())
							{
								var1.add(cast);
							}
							else
							{
								var1.push(cast);
							}

							var1.lastPrec = cast.getPrecedence();

							break;
						}
						case 2:
						{
							final FormulaBuilder var2 = stack.pop();
							final FormulaBuilder var1 = stack.pop();

							// final String var1Before = var1.toString();
							// final String var2Before = var2.toString();

							if (cast.getPrecedence() > var1.lastPrec)
							{
								var1.push(Operators.Scope_OpenBrace);
								var1.add(Operators.Scope_CloseBrace);
							}

							var1.add(cast);

							var1.add(var2);

							stack.push(var1);
							var1.lastPrec = cast.getPrecedence();

							// System.out.println("var 1: " + var1Before + "\top: " + cast.getSymbol() + "\tvar2: " + var2Before + "\tyields: " + var1.toString());
							break;
						}
						default:
						{
							throw new IndexOutOfBoundsException(cast.toString());
						}
					}
				}
			}
		}

		if (stack.getSize() == 1)
		{
			return stack.pop().toCharListAtTime(inTime);
		}
		else
		{
			throw new IndexOutOfBoundsException();
		}
	}
	
	@Override
	public boolean isOperator()
	{
		return false;
	}

	@Override
	public boolean isOperand()
	{
		return true;
	}

	@Override
	public boolean isConstant()
	{
		return false;
	}

	@Override
	public int getPrecedence()
	{
		return -1;
	}

	@Override
	public String valueToString()
	{
		return this.toCharList().toString();
	}

	@Override
	public String valueToStringAtTime(StepStage inTime)
	{
		return this.toCharList(inTime).toString();
	}

	@Override
	public String getEvaluatedValueAsStringAtTime(StepStage inTime)
	{
		return this.valueToStringAtTime(inTime);
	}

	@Override
	public Variable getEvaluatedValueAtTime(StepStage inTime)
	{
		return new Var_InstancedFormula("instanceOf:" + this.mangledName, inTime, this);
	}

	@Override
	public VariableTypes getType()
	{
		return VariableTypes.Formula;
	}

	@Override
	public String getMangledName()
	{
		return this.mangledName;
	}

	@Override
	public Comparisons compareWith(Variable inIn, StepStage inTime)
	{
		if(inIn == this)
		{
			return Comparisons.SameType_SameValue;
		}
		else if(inIn.getType() == VariableTypes.Formula)
		{
			return Comparisons.SameType_DifferentValue;
		}
		else
		{
			return Comparisons.DifferentType;
		}
	}

	public static class FormulaBuilder implements FormulaToken
	{
		private final SingleLinkedList<FormulaToken> infix;
		private int lastPrec;

		public FormulaBuilder()
		{
			this.infix = new SingleLinkedList<FormulaToken>();
			this.lastPrec = 100;
		}
		
		public Formula build()
		{
			return Formula.parse(this.infix);
		}

		public int getLastPrec()
		{
			return this.lastPrec;
		}

		public void setLastPrec(int inLastPrec)
		{
			this.lastPrec = inLastPrec;
		}
		
		public void add(FormulaToken in)
		{
			this.infix.add(in);
		}
		
		public void push(FormulaToken in)
		{
			this.infix.push(in);
		}

		@Override
		public boolean isOperator()
		{
			return false;
		}

		@Override
		public boolean isOperand()
		{
			return true;
		}

		@Override
		public boolean isConstant()
		{
			return false;
		}

		@Override
		public int getPrecedence()
		{
			return -1;
		}

		@Override
		public String valueToString()
		{
			return this.toCharList().toString();
		}
		
		public CharList toCharList()
		{
			final CharList result = new CharList();

			for (FormulaToken current : this.infix)
			{
				result.add(current.valueToString());
			}

			return result;
		}

		@Override
		public String valueToStringAtTime(StepStage inTime)
		{
			return this.toCharListAtTime(inTime).toString();
		}
		
		public CharList toCharListAtTime(StepStage inTime)
		{
			final CharList result = new CharList();

			for (FormulaToken current : this.infix)
			{
				final String curr = current.valueToStringAtTime(inTime);
				
				if(curr == null)
				{
					throw new IllegalArgumentException();
				}
				
				result.add(curr);
			}

			return result;
		}
	}
}
