package CleanStart.Mk02.Instructions;

public class BaseInstruction implements Instruction
{
	private boolean isExpired;
	
	public BaseInstruction()
	{
		this.isExpired = false;
	}
	
	@Override
	public boolean isExpired()
	{
		return this.isExpired;
	}

	@Override
	public void setExpireState(boolean inIn)
	{
		this.isExpired = inIn;
	}
}
