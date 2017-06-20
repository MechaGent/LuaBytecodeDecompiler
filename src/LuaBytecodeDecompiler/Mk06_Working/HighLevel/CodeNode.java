package LuaBytecodeDecompiler.Mk06_Working.HighLevel;

public abstract class CodeNode
{
	private final CodeNode[] parents;
	private final CodeNode[] children;
	
	public CodeNode(CodeNode[] inParents, CodeNode[] inChildren)
	{
		this.parents = inParents;
		this.children = inChildren;
	}
}
