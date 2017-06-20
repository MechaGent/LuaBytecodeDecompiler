package CleanStart.Mk05;

public class NewTableVar extends ShyVar
{
	private NewTableVar()
	{
		super();
	}
	
	public static NewTableVar getInstance()
	{
		return new NewTableVar();
	}
}
