package CleanStart.Mk02.Instructions;

public interface Instruction
{
	public boolean isExpired();
	public void setExpireState(boolean in);
}
