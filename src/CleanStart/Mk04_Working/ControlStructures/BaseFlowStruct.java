package CleanStart.Mk04_Working.ControlStructures;

public abstract class BaseFlowStruct implements FlowStruct
{
	protected FlowStruct next;

	public BaseFlowStruct()
	{
		this.next = null;
	}
}
