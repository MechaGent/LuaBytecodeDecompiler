package LuaBytecodeDecompiler.Mk02;

import DataStructures.Maps.HalfByteRadix.Mk02.HalfByteRadixMap;

public class Accountant
{
	private final NamesMap names;
	private final HalfByteRadixMap<Function> functionsMap;
	
	private Accountant()
	{
		this.names = NamesMap.getInstance(true);
		this.functionsMap = new HalfByteRadixMap<Function>();
	}
	
	public static Accountant getInstance()
	{
		return new Accountant();
	}
	
	public String getAlteredName(String oldName)
	{
		return this.names.getCorrectName(oldName);
	}
	
	public void register_Function(Function in)
	{
		this.functionsMap.add(in.getFunctionName(), in);
	}
	
	public Function lookup_Function(String name)
	{
		return this.functionsMap.get(name);
	}
}
