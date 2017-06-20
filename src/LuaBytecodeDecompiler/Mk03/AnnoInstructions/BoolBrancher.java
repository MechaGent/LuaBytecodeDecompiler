package LuaBytecodeDecompiler.Mk03.AnnoInstructions;

import DataStructures.Linkages.CharList.Mk03.CharList;

public interface BoolBrancher
{
	public CharList toBooleanStatement(boolean invertStatement);
}
