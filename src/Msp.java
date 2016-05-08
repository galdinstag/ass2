
public class Msp {
	private int startTxt;
	private int endTxt;
	private int startQuery;
	private int endQuery;
	private String queryLabel;
	private String textLabel;
	private int score;
	
	public Msp(int startTxt, int endTxt, int startQ, int endQ, String queryLabel, String textLabel, int textLength){
		this.startQuery = startQ;
		this.endQuery = endQ;
		this.startTxt = startTxt;
		this.endTxt = endTxt;
		this.queryLabel = queryLabel;
		this.textLabel = textLabel;
		score = (endQuery - startQuery);
	}
	public int getStartTxt(){ return startTxt;}
	public int getEndTxt(){ return endTxt;}
	public int getStartQuery(){ return startQuery;}
	public int getEndQuery(){ return endQuery;}
	public String getQueryLabel(){ return queryLabel;}
	public String getTextLabel(){ return textLabel;}
	public  int getScore(){ return score;}
	public String toString(){ return new String("query: " + queryLabel + " text: " + textLabel + " st: " + startTxt + " et: " + endTxt + " qs: " + startQuery + " qe: " + endQuery);}

}
