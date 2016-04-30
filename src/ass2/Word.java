package ass2;

public class Word {
	public Word(String para, int index) {
		paragraph = para;
		startIndex = index;
	}
	String paragraph;
	int startIndex;
	public String toString(){
		return(paragraph+" "+startIndex);
	}
}
