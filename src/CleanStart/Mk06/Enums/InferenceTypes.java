package CleanStart.Mk06.Enums;

import CleanStart.Mk04_Working.Variables.CastTypes;
import CleanStart.Mk04_Working.Variables.VarTypes;
import HandyStuff.FileParser;

public enum InferenceTypes
{
	Mystery(
			false),
	String(
			true),
	Bool(
			true),
	Int(
			true),
	Float(
			true),
	Null(
			true),
	Formula(
			false),
	TableObject(
			false),
	TableElement(
			false),
	FunctionObject(
			false),

	/**
	 * union type, representing either int or float constants
	 */
	Math_Constant(
			true),
	
	/**
	 * union type, representing either int or float variables
	 */
	Math_Variable(false),
	
	/**
	 * union type, representing either int or float, either constant or variable
	 */
	Math_Unknown(false),

	/**
	 * union type, representing either a Table or a FunctionObject
	 */
	LuaObject(
			false),
	
	/**
	 * represents a 'soft' boolean. In other words, this can be overruled by anything, but is in all other ways a Bool.
	 */
	ComparableVar(true),
	;

	private static final VarTypes[] All = VarTypes.values();
	private static final CastTypes[][] castStates = initCastTypesCheckTable("H:/Users/Thrawnboo/workspace - Java/LuaBytecodeDecompiler/src/CleanStart/Mk06/Enums/VarTypesCastingTableCsv.csv");
	
	private final boolean isFinal;

	private InferenceTypes(boolean inIsFinal)
	{
		this.isFinal = inIsFinal;
	}
	
	private static CastTypes[][] initCastTypesCheckTable(String dirPath)
	{
		final String raw = FileParser.parseFileAsString(dirPath);
		final String[] lines = raw.split("\r\n");
		final String[] firstLine = lines[0].split(",");
		final VarTypes[] columnsHeader = parseColumnsHeader(firstLine);
		final CastTypes[][] result = new CastTypes[lines.length - 1][lines.length - 1];
		
		for(int i = 1; i < lines.length; i++)
		{
			final String[] splitLine = lines[i].split(",");
			final VarTypes castTo = VarTypes.valueOf(splitLine[0]);
			
			for(int j = 1; j < splitLine.length; j++)
			{
				result[columnsHeader[j].ordinal()][castTo.ordinal()] = CastTypes.parse(splitLine[j]);
			}
		}
		
		return result;
	}
	
	private static VarTypes[] parseColumnsHeader(String[] firstLine)
	{
		final VarTypes[] result = new VarTypes[firstLine.length];
		
		result[0] = null;
		
		for(int i = 1; i < firstLine.length; i++)
		{
			result[i] = VarTypes.valueOf(firstLine[i]);
		}
		
		return result;
	}
	
	public CastTypes attemptCastTo(VarTypes cast)
	{
		return castStates[this.ordinal()][cast.ordinal()];
	}

	public boolean isFinal()
	{
		return this.isFinal;
	}
	
	public static VarTypes[] getAll()
	{
		return All;
	}
	
	/*
	public static void main(String[] args)
	{
		final int numValues = All.length;
		final CharList result = new CharList();
		
		for(int j = 0; j < numValues; j++)
		{
			if(j != 0)
			{
				result.addNewLine();
			}
			
			result.add(All[j].toString());
			result.add(": ");
			
			for(int i = 0; i < numValues; i++)
			{
				if(i != 0)
				{
					result.add(", ");
				}
				
				result.add(castStates[i][j].toString());
			}
		}
		
		System.out.println(result.toString());
	}
	*/
}
