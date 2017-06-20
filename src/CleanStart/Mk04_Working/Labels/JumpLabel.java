package CleanStart.Mk04_Working.Labels;

public class JumpLabel extends BaseLabel
{
	//private static int instanceCounter = 0;

	private JumpLabel(String inPrefix, int inInstanceNumber)
	{
		super(inPrefix, inInstanceNumber);
	}
	
	public static JumpLabel getInstance()
	{
		return getInstance("JumpLabel");
	}
	
	public static JumpLabel getInstance(String inPrefix)
	{
		return new JumpLabel(inPrefix, instanceCounter++);
	}
}
