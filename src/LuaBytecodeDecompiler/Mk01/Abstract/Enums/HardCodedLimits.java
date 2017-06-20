package LuaBytecodeDecompiler.Mk01.Abstract.Enums;

public enum HardCodedLimits
{
	StackFrameSize_Max(250),
	LocalVarsPerFunction_Max(200),
	UpvaluesPerFunction_Max(60),
	
	/**
	 * might not be set in stone
	 */
	JumpDistance_Max(131071),
	NumBitsPerField_opCode(6),
	NumBitsPerField_A(8),
	NumBitsPerField_B(9),
	NumBitsPerField_Bx(18),
	NumBitsPerField_sBx(18),
	NumBitsPerField_C(9)
	;
	
	private final int value;

	private HardCodedLimits(int inValue)
	{
		this.value = inValue;
	}

	public int getValue()
	{
		return this.value;
	}
}
