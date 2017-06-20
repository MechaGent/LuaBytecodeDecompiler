package LuaBytecodeDecompiler.Mk03.MiniBlocks;

import LuaBytecodeDecompiler.Mk01.Abstract.Enums.AbstractOpCodes;
import LuaBytecodeDecompiler.Mk03.AnnoInstruction;
import LuaBytecodeDecompiler.Mk03.AnnoInstructions.AsBx.AnnoInstruction_Jump;

public class InstructionTokenizer
{
	private final AnnoInstruction[] core;
	//private final PriorityQueue<Token> buffer;
	private int place;

	public InstructionTokenizer(AnnoInstruction[] inCore)
	{
		this.core = inCore;
		//this.buffer = new PriorityQueue<Token>();
		this.place = 0;
	}

	public Token getNextToken(int stepLimit)
	{
			final AnnoInstruction cur = this.core[this.place++];
			final Token result;

			switch (cur.getOpCode())
			{
				case TestIfEqual:
				case TestIfLessThan:
				case TestIfLessThanOrEqual:
				case TestLogicalComparison:
				case TestAndSaveLogicalComparison:
				{
					final AnnoInstruction_Jump next = (AnnoInstruction_Jump) this.core[this.place++];
					result = new Token(TokenTypes.Start_If, cur, next.getOffset() + 2);
					break;
				}
				default:
				{
					result = new Token(TokenTypes.Vanilla, cur);
					break;
				}
			}

			return result;
	}

	public static class Token
	{
		private final TokenTypes type;
		private final AnnoInstruction cargo;
		private final int expectedInclusiveLength;

		private Token(TokenTypes inType, AnnoInstruction inCargo)
		{
			this(inType, inCargo, 1);
		}
		
		private Token(TokenTypes inType, AnnoInstruction inCargo, int inExpectedLength)
		{
			this.type = inType;
			this.cargo = inCargo;
			this.expectedInclusiveLength = inExpectedLength;
		}

		public TokenTypes getType()
		{
			return this.type;
		}

		public AnnoInstruction getCargo()
		{
			return this.cargo;
		}

		public int getExpectedInclusiveLength()
		{
			return this.expectedInclusiveLength;
		}
	}

	public static enum TokenTypes
	{
		Vanilla,
		Start_If;
		
		public static TokenTypes parseFromOpCode(AbstractOpCodes in)
		{
			final TokenTypes result;
			
			switch(in)
			{
				case TestIfEqual:
				case TestIfLessThan:
				case TestIfLessThanOrEqual:
				case TestLogicalComparison:
				case TestAndSaveLogicalComparison:
				{
					result = Start_If;
					break;
				}
				default:
				{
					result = Vanilla;
					break;
				}
			}
			
			return result;
		}
	}
}
