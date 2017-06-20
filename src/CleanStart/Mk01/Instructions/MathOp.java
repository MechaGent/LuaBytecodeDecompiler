package CleanStart.Mk01.Instructions;

import CharList.CharList;
import CleanStart.Mk01.BaseInstruction;
import CleanStart.Mk01.Enums.IrCodes;
import CleanStart.Mk01.Interfaces.Variable;
import CleanStart.Mk01.VarFormula.Formula;
import CleanStart.Mk01.VarFormula.FormulaToken;
import CleanStart.Mk01.VarFormula.Operators;
import CleanStart.Mk01.VarFormula.Formula.FormulaBuilder;
import CleanStart.Mk01.Variables.Var_Ref;
import SingleLinkedList.Mk01.SingleLinkedList;

public class MathOp extends BaseInstruction
{
	private final Var_Ref target;
	private final Formula operation;
	private Variable operand1;
	private Variable operand2;

	public MathOp(int line, int subLine, int startStep, Var_Ref inTarget, Formula inOperation, Variable inOperand1, Variable inOperand2)
	{
		super(IrCodes.MathOp, line, subLine, startStep, 1);
		this.target = inTarget;
		this.operation = inOperation;
		this.operand1 = inOperand1;
		this.operand2 = inOperand2;
	}
	
	public static MathOp getInstance(int line, int subLine, int startStep, Var_Ref inTarget, Formula inOperation, Variable inOperand1, Variable inOperand2)
	{
		final MathOp result = new MathOp(line, subLine, startStep, inTarget, inOperation, inOperand1, inOperand2);
		result.handleInstructionIo();
		return result;
	}

	public static final MathOp generateMathOp(int line, int subLine, int startStep, Var_Ref target, Variable operand1, Operators operator, Variable operand2)
	{
		final FormulaBuilder buildy = new FormulaBuilder();
		
		buildy.add(operand1);
		buildy.add(operator);
		buildy.add(operand2);

		final Formula refined = buildy.build();
		return getInstance(line, subLine, startStep, target, refined, operand1, operand2);
	}

	public static final MathOp generateMathOp(int line, int subLine, int startStep, Var_Ref target, Variable operand1, Operators operator)
	{
		final SingleLinkedList<FormulaToken> rawFormula = new SingleLinkedList<FormulaToken>();

		rawFormula.add(operand1); // operand 1
		rawFormula.add(operator); // operator

		final Formula refined = Formula.parse(rawFormula.iterator());
		return getInstance(line, subLine, startStep, target, refined, operand1, null);
	}
	
	public static final MathOp generateMathOp(int line, int subLine, int startStep, Var_Ref target, Operators operator, Variable operand1)
	{
		final SingleLinkedList<FormulaToken> rawFormula = new SingleLinkedList<FormulaToken>();

		rawFormula.add(operator); // operator
		rawFormula.add(operand1); // operand 1

		final Formula refined = Formula.parse(rawFormula.iterator());
		return getInstance(line, subLine, startStep, target, refined, operand1, null);
	}

	public Var_Ref getTarget()
	{
		return this.target;
	}

	public Formula getOperation()
	{
		return this.operation;
	}

	public Variable getOperand1()
	{
		return this.operand1;
	}

	public Variable getOperand2()
	{
		return this.operand2;
	}

	@Override
	protected void handleInstructionIo()
	{
		this.target.logWriteAtTime(this.sigTimes[1], this.operation);
	}

	@Override
	public CharList translateIntoCommentedByteCode(int inOffset, CharList result)
	{
		result.add('\t', inOffset);
		result.add(this.OpCode.toString());
		result.add('\t');
		result.add(this.target.getMangledName());
		result.add(" = ");
		result.add(this.operation.toCharList(this.sigTimes[this.sigTimes.length - 1]), true);
		return result;
	}
}
