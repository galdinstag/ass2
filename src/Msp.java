
public class Msp {
	private int startTxt;
	private int endTxt;
	private int startQuery;
	private int endQuery;
	
	public Msp(int startTxt, int endTxt, int startQ, int endQ){
		this.startQuery = startQ;
		this.endQuery = endQ;
		this.startTxt = startTxt;
		this.endTxt = endTxt;
	}
	public int getStartTxt(){ return startTxt;}
	public int getEndTxt(){ return endTxt;}
	public int getStartQuery(){ return startQuery;}
	public int getEndQuery(){ return endQuery;}
	public String toString(){ return new String("st: " + startTxt + " et: " + endTxt + " qs: " + startQuery + " qe: " + endQuery);}

}
