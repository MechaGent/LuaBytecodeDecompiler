package LuaBytecodeDecompiler.Mk01;

import DataStructures.Linkages.CharList.Mk03.CharList;

/**
 * Format: &ltPosition&gt	&ltHexToken&gt	&ltInterpretation&gt
 *
 */
public interface DisAssemblable
{
	public CharList disAssemble();
	public CharList disAssemble(int offset);
	
	public CharList disAssemble(String label);
	public CharList disAssemble(int offset, String label);
}
