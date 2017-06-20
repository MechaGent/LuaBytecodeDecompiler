package CleanStart.Mk06.VarFormula;

import CleanStart.Mk06.Enums.OpCodes;
import CustomExceptions.UnhandledEnum.Mk01.UnhandledEnumException;

public enum Operators implements FormulaToken
{
	/*
	 * Sub == Add
	 * Mult > Add
	 * Div == Mult
	 * 
	 */
	Scope_OpenBrace(-1, "(", false, false, -1, true),
	Scope_CloseBrace(Operators.Scope_OpenBrace, ")", true),
	Conditional_Or(2, "||", true, true, 1, true),
	Conditional_And(2, "&&", true, true, Operators.Conditional_Or.getPrecedence() + 1, true),
	Bitwise_Or(2, "|", true, true, Operators.Conditional_And.getPrecedence() + 1, true),
	Bitwise_Xor(2, "^", true, true, Operators.Bitwise_Or.getPrecedence() + 1, true),
	Bitwise_And(2, "&", true, true, Operators.Bitwise_Xor.getPrecedence() + 1, true),
	ArithToBool_Equals(2, "==", true, true, Operators.Bitwise_And.getPrecedence() + 1, true),
	ArithToBool_NotEquals(Operators.ArithToBool_Equals, "!=", true),
	ArithToBool_LessThan(2, "<", true, true, Operators.ArithToBool_Equals.getPrecedence() + 1, true),
	ArithToBool_GreaterThanOrEquals(Operators.ArithToBool_LessThan, ">=", true),
	ArithToBool_LessThanOrEquals(Operators.ArithToBool_LessThan, "<=", true),
	ArithToBool_GreaterThan(Operators.ArithToBool_LessThan, ">", true),
	Arith_Add(2, "+", true, true, Operators.ArithToBool_LessThan.getPrecedence() + 1, true),
	Arith_Sub(Operators.Arith_Add, "-", true),
	Arith_Mul(2, "*", true, true, Operators.Arith_Add.getPrecedence() + 1, true),
	Arith_Div(Operators.Arith_Mul, "/", true),
	Arith_Mod(Operators.Arith_Mul, "%", true),
	Arith_Pow(2, "<pow>", true, true, Operators.Arith_Mul.getPrecedence() + 1, true),
	Bitwise_Not(1, "~", true, false, Operators.Arith_Pow.getPrecedence() + 1, false),
	Logic_Not(Operators.Bitwise_Not, "!", false),
	Arith_Neg(Operators.Bitwise_Not, "-", false),
	;
	
	private final int numOperands;
	private final String symbol;
	private final boolean needsLeftSpace;
	private final boolean needsRightSpace;
	private final int precedenceLevel;
	private final boolean isLeftAssociative;

	private Operators(Operators copy, String inSymbol, boolean inIsLeftAssociative)
	{
		this.numOperands = copy.numOperands;
		this.symbol = inSymbol;
		this.needsLeftSpace = copy.needsLeftSpace;
		this.needsRightSpace = copy.needsRightSpace;
		this.precedenceLevel = copy.precedenceLevel;
		this.isLeftAssociative = inIsLeftAssociative;
	}

	private Operators(int inNumOperands, String inSymbol, boolean inNeedsLeftSpace, boolean inNeedsRightSpace, int inPrecedenceLevel, boolean inIsLeftAssociative)
	{
		this.numOperands = inNumOperands;
		this.symbol = inSymbol;
		this.needsLeftSpace = inNeedsLeftSpace;
		this.needsRightSpace = inNeedsRightSpace;
		this.precedenceLevel = inPrecedenceLevel;
		this.isLeftAssociative = inIsLeftAssociative;
	}
	
	public static Operators parse(OpCodes in)
	{
		final Operators result;
		
		switch(in)
		{
			case Add:
			{
				result = Arith_Add;
				break;
			}
			case Subtract:
			{
				result = Arith_Sub;
				break;
			}
			case Multiply:
			{
				result = Arith_Mul;
				break;
			}
			case Divide:
			{
				result = Arith_Div;
				break;
			}
			case Modulo:
			{
				result = Arith_Mod;
				break;
			}
			case Power:
			{
				result = Arith_Pow;
				break;
			}
			case ArithmeticNegation:
			{
				result = Arith_Neg;
				break;
			}
			case LogicalNegation:
			{
				result = Logic_Not;
				break;
			}
			default:
			{
				throw new UnhandledEnumException(in);
			}
		}
		
		return result;
	}

	public int getNumOperands()
	{
		return this.numOperands;
	}

	public String getSymbol()
	{
		return this.symbol;
	}
	
	public boolean isLeftAssociative()
	{
		return this.isLeftAssociative;
	}

	public int getPrecedence()
	{
		return this.precedenceLevel;
	}

	@Override
	public boolean isOperator()
	{
		return true;
	}

	@Override
	public boolean isOperand()
	{
		return false;
	}

	@Override
	public boolean isConstant()
	{
		return true;
	}

	@Override
	public String getValueAsString()
	{
		return (this.needsLeftSpace? " " : "") + this.symbol + (this.needsRightSpace? " " : "");
	}
}
