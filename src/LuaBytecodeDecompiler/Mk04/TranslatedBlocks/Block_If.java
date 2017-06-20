package LuaBytecodeDecompiler.Mk04.TranslatedBlocks;

import DataStructures.Linkages.CharList.Mk03.CharList;

public class Block_If extends Block
{
	private CharList boolStatement;
	private Block IfBlock;
	private Block ElseBlock;
	
	public Block_If()
	{
		this(NullBlock, NullBlock);
	}

	public Block_If(Block inIfBlock, Block inElseBlock)
	{
		this.boolStatement = new CharList();
		this.IfBlock = inIfBlock;
		this.ElseBlock = inElseBlock;
	}

	public CharList getBoolStatement()
	{
		return this.boolStatement;
	}

	public void setBoolStatement(CharList inBoolStatement)
	{
		this.boolStatement = inBoolStatement;
	}

	public Block getIfBlock()
	{
		return this.IfBlock;
	}

	public void setIfBlock(Block inIfBlock)
	{
		this.IfBlock = inIfBlock;
	}

	public Block getElseBlock()
	{
		return this.ElseBlock;
	}

	public void setElseBlock(Block inElseBlock)
	{
		this.ElseBlock = inElseBlock;
	}

	@Override
	public BlockTypes getBlockType()
	{
		return BlockTypes.If;
	}

	@Override
	public CharList toCharList_internal(int offset)
	{
		final CharList result = new CharList();
		
		result.addNewLine();
		
		result.add("if (");
		result.add(this.boolStatement, false);
		result.add(')');
		result.addNewLine();
		result.add('{');
		result.addNewLine();
		result.add(this.IfBlock.toCharList_internal(offset + 1), true);
		result.add("\r\n}");
		
		if(!this.ElseBlock.isNull())
		{
			result.add("\r\nelse\r\n{\r\n");
			result.add(this.ElseBlock.toCharList_internal(offset + 1), true);
		}
		
		result.addNewLine();
		
		return result;
	}
}
