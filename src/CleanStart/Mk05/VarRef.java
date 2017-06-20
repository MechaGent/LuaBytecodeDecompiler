package CleanStart.Mk05;

public class VarRef implements VariableReference
{
	private static int instanceCounter = 0;
	
	private final String namePrefix;
	private final int[] instanceNumbers;
	private final Variable target;

	private VarRef(String inNamePrefix, int[] inInstanceNumbers, Variable inTarget)
	{
		this.namePrefix = inNamePrefix;
		this.instanceNumbers = inInstanceNumbers;
		this.target = inTarget;
		inTarget.incrementNumReads();
	}

	public static VarRef getInstance(String inPrefix, Variable in)
	{
		final VarRef result = new VarRef(inPrefix, new int[]{instanceCounter++}, in);
		return result;
	}
	
	public static VarRef getInstance(String inPrefix, int[] instanceNumberPrefixes, Variable in)
	{
		final int[] instances = new int[instanceNumberPrefixes.length + 1];
		
		for(int i = 0; i < instanceNumberPrefixes.length; i++)
		{
			instances[i] = instanceNumberPrefixes[i];
		}
		
		instances[instanceNumberPrefixes.length] = instanceCounter++;
		
		final VarRef result = new VarRef(inPrefix, instances, in);
		return result;
	}
	
	public static VarRef getInstance(String inPrefix, ShyVar in)
	{
		final VarRef result = new VarRef(inPrefix, new int[]{instanceCounter++}, in);
		in.setFallbackValue();
		return result;
	}
	
	public static VarRef getInstance(String inPrefix, int[] instanceNumberPrefixes, Variable in)
	{
		
	}
	
	public static VarRef getInstance(ShyVar in)
	{
		final VarRef result = new VarRef(in);
		in.setFallbackValue(in);
		return result;
	}
	
	public static VarRef getInstance(VarRef in)
	{
		final VarRef result = new VarRef(in.target);
		return result;
	}
	
	@Override
	public String getVariableName()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Variable getValue()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void assignValue(Variable inValue)
	{
		// TODO Auto-generated method stub
		
	}
}
