package CleanStart.Mk05;

/**
 * can be inlined once
 * @author MechaGent
 *
 */
public abstract class ShyVar extends BaseVar
{
	protected Variable FallbackValue;
	
	public ShyVar()
	{
		this.FallbackValue = null;
	}
	
	public void setFallbackValue(Variable in)
	{
		this.FallbackValue = in;
	}
	
	@Override
	public Variable getValue()
	{
		if(this.numReads != 1)
		{
			return this.FallbackValue;
		}
		else
		{
			return this;
		}
	}
}
