package CleanStart.Mk05;

public abstract class BaseVar implements Variable
{
	protected int numReads;
	
	public BaseVar()
	{
		this.numReads = 0;
	}
	
	@Override
	public void incrementNumReads()
	{
		this.numReads++;
	}
}
