package LuaBytecodeDecompiler.Mk03.MiniBlocks;

import DataStructures.Linkages.CharList.Mk03.CharList;
import LuaBytecodeDecompiler.Mk03.AnnoInstruction;

public class MiniBlock_If extends MiniBlock
{
	private final MiniBlock boolExpressions;
	private final MiniBlock ifBranch;
	private final MiniBlock elseBranch;
	
	//if ifBranch ends in a return, there is no elseBranch
	public MiniBlock_If(AnnoInstruction[] inSpine, int inStart, int inEnd, MiniBlock inBoolExpressions, MiniBlock inIfBranch, MiniBlock inElseBranch)
	{
		super(inSpine, inStart, inEnd);
		this.boolExpressions = inBoolExpressions;
		this.ifBranch = inIfBranch;
		this.elseBranch = inElseBranch;
	}

	@Override
	protected CharList toCharList_Internal(int inOffset)
	{
		final CharList result = this.innerBlock.toCharList_Internal(inOffset + 1);
		result.pushNewLine();
		result.push('\t', inOffset);
		result.push('{');
		result.pushNewLine();
		result.push(')');
		
		for(int i = this.boolExpressions.length - 1; i >= 0; i--)
		{
			result.push(this.boolExpressions[i].toCharList_Internal(0), true);
			
			if(i != 0)
			{
				result.push(this.joinOps[i - 1].toString());
			}
		}
		
		result.push("if (");
		result.push('\t', inOffset);
		
		result.addNewLine();
		result.add('\t', inOffset);
		result.add('}');
		
		if(this.hasNext())
		{
			result.addNewLine();
			result.add(this.getNext().toCharList_Internal(inOffset), true);
		}
		
		return result;
	}

	private static enum LogicOps
	{
		AND,
		OR;
	}
}
