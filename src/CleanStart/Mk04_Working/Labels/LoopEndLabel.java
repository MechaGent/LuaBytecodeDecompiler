package CleanStart.Mk04_Working.Labels;

public class LoopEndLabel extends BaseLabel
{
	//private static int instanceCounter = 0;

	private LoopEndLabel(String inPrefix, int inInstanceNumber)
	{
		super(inPrefix, inInstanceNumber);
	}
	
	public static LoopEndLabel getInstance()
	{
		return getInstance("LoopEndLabel_");
	}
	
	public static LoopEndLabel getInstance(String inPrefix)
	{
		return new LoopEndLabel(inPrefix, instanceCounter++);
	}
}
