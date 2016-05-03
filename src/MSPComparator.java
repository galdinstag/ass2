import java.util.Comparator;

public class MSPComparator implements Comparator{

	public MSPComparator(){}
	@Override
	public int compare(Object o1, Object o2) {
		return ((((Msp)o2).getEndTxt()-((Msp)o2).getStartTxt()) - (((Msp)o1).getEndTxt()-((Msp)o1).getStartTxt()));
//		if (((Msp)o1).getEndTxt() - ((Msp)o2).getStartTxt() > 0)
//			return 1;
//		else if (((Msp)o1).getEndTxt() - ((Msp)o2).getStartTxt() < 0)
//			return -1;
//		else return 0;
//		
		
		
//		if(((Msp)o1).getStartTxt() < ((Msp)o2).getStartTxt()){
//			return 1;
//		}
//		else if(((Msp)o1).getStartTxt() > ((Msp)o2).getStartTxt()){
//		return -1;
//		}
//		else{
//			if(((Msp)o1).getEndTxt() > ((Msp)o2).getEndTxt()){
//				return 1;
//			}
//			else if(((Msp)o1).getEndTxt() < ((Msp)o2).getEndTxt()){
//				return -1;
//			}
//		}
//		return 0;
	}	
}