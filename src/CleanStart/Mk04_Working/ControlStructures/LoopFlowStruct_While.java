package CleanStart.Mk04_Working.ControlStructures;

import CleanStart.Mk04_Working.VarFormula.Formula;
import SingleLinkedList.Mk01.SingleLinkedList;

public class LoopFlowStruct_While extends LoopFlowStruct
{
	private final Formula test;
	private final SingleLinkedList<FlowStruct> structs;
	
	public LoopFlowStruct_While(Formula inTest)
	{
		super();
		this.test = inTest;
		this.structs = new SingleLinkedList<FlowStruct>();
	}
}
