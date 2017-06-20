package CleanStart.Mk05;

public interface Variable
{
	/**
	 * CAUTION: only call this when performing final evaluation
	 * @return
	 */
	public Variable getValue();
	public void incrementNumReads();
}
