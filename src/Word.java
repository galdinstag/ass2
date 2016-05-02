public class Word {
	private String paragraph;
	private int startIndex;
	public Word(String para, int index) {
		paragraph = para;
		startIndex = index;
	}

	public String getParagraph(){ return paragraph;}
	public String toString(){
		return(paragraph+" "+startIndex);
	}
}
