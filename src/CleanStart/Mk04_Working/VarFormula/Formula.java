package CleanStart.Mk04_Working.VarFormula;

import java.util.Iterator;

import CharList.CharList;
import CleanStart.Mk04_Working.CanBeInlinedTypes;
import CleanStart.Mk04_Working.Instruction;
import CleanStart.Mk04_Working.Variables.BaseVar;
import CleanStart.Mk04_Working.Variables.VarTypes;
import SingleLinkedList.Mk01.SingleLinkedList;

public class Formula extends BaseVar implements FormulaToken
{
	private static int instanceCounter = 0;
	
	//private final String mangledName;
	private final FormulaToken[] tokens;

	public Formula(String inNamePrefix, Instruction inAssignmentInstruction, FormulaToken[] inTokens)
	{
		super(inNamePrefix, VarTypes.Formula, instanceCounter++, inAssignmentInstruction);
		this.tokens = inTokens;
	}

	public static Formula parse(String inNamePrefix, Instruction inAssignmentInstruction, Iterable<FormulaToken> infixedFormula)
	{
		return parse(inNamePrefix, inAssignmentInstruction, infixedFormula.iterator());
	}

	public static Formula parse(String inNamePrefix, Instruction inAssignmentInstruction, Iterator<FormulaToken> infixedFormula)
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

		return new Formula(inNamePrefix, inAssignmentInstruction, postfix.toArray(new FormulaToken[postfix.getSize()]));
	}

	public static Formula combine(Formula var1, Operators operator, Formula var2)
	{
		/*
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
		*/
		throw new UnsupportedOperationException();
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
		return this.toCharList(true);
	}
	
	public CharList toCharList(boolean showInferredTypes)
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
			return stack.pop().toCharList(showInferredTypes);
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
	public String valueToString(boolean showInferredTypes)
	{
		return this.toCharList(showInferredTypes).toString();
	}
	
	@Override
	public CharList getValueAsCharList()
	{
		return this.toCharList();
	}

	@Override
	public CanBeInlinedTypes canBeInlined()
	{
		return CanBeInlinedTypes.Yes;
	}

	@Override
	public String getMostRelevantIdValue()
	{
		return this.toCharList().toString();
	}
	
	@Override
	public String getMostRelevantIdValue(boolean showInferredTypes)
	{
		return this.toCharList(showInferredTypes).toString();
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
		
		public Formula build(String inNamePrefix, Instruction inAssignmentInstruction)
		{
			return Formula.parse(inNamePrefix, inAssignmentInstruction, this.infix);
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
		
		@Override
		public String valueToString(boolean inShowInferredTypes)
		{
			return this.toCharList(inShowInferredTypes).toString();
		}
		
		public CharList toCharList()
		{
			return this.toCharList(true);
		}
		
		public CharList toCharList(boolean showInferredTypes)
		{
			final CharList result = new CharList();

			for (FormulaToken current : this.infix)
			{
				result.add(current.valueToString(showInferredTypes));
			}

			return result;
		}
	}
}
