package ass2;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class SequenceAlignment{
//	int TrashHold = 5;	//TODO: READ FROM USER!!!
	HashMap<String, String> sequenceQueries;
	HashMap<String, String> sequenceText;

	int k;
	int a;
	int b;
	TransitionMatrix matrix;
	
	public SequenceAlignment(String queriesFile, String matrixFile, String textFile, int num){
		k = num;
		sequenceQueries = new HashMap<>();
        FastaSequence reader = new FastaSequence(queriesFile);
        for (int i=0; i< reader.size(); i++)
        {
        	sequenceQueries.put(reader.getDescription(i), reader.getSequence(i));
        }
        sequenceText = new HashMap<>();
        reader = new FastaSequence(textFile);
        for (int i=0; i< reader.size(); i++)
        {
        	sequenceText.put(reader.getDescription(i), reader.getSequence(i));
        }
        

        
        matrix = new TransitionMatrix(fixMatrix(matrixFile));
        File toDelete = new File("mid.txt");
        toDelete.deleteOnExit();
        
        //make hash of words and indexes in text
        
        LinkedHashMap<String,ArrayList<Word>> wordsTextMap = new LinkedHashMap<String, ArrayList<Word>>();
        
        String currentWord;
        for (Map.Entry<String, String> entry : sequenceText.entrySet()) {
        	 String key = entry.getKey();
        	 String value = entry.getValue();
        	 for (int i = 0; i <= value.length() - k; i++){	
        		 currentWord = value.substring(i, i + k);     			 
        		 if (wordsTextMap.containsKey(currentWord))	//contains word add only new location
    				 wordsTextMap.get(currentWord).add(new Word(key, i));
        		 else{
        			 ArrayList<Word> arraylist = new ArrayList<Word>();	//create a new arraylist for the word
        			 arraylist.add(new Word(key, i));
        			 wordsTextMap.put(currentWord, arraylist);
        		 }	
       	 }
       }
        

        }
   
	
    public File fixMatrix(String orgMatrix){
        try {
            BufferedReader bf = new BufferedReader(new FileReader(orgMatrix));
            BufferedWriter writer = new BufferedWriter(new FileWriter("mid.txt"));
            String line = bf.readLine();
            while((line != null) && line.contains("#")){    //dont read lines with #
                line = bf.readLine();
            }
            while((line != null) && (!line.contains("#"))){ //read matrix
                writer.write(line);
                writer.write("\n");
                writer.flush();
                line = bf.readLine();
            }
            while((line != null) && line.contains("#")){ //dont read lines with #
                line = bf.readLine();
            }
            //read A for: W(x) = Ax + B
            String num = line.substring(2);
            a = Integer.parseInt(num.toString());

            //read B for: W(x) = Ax + B
            line = bf.readLine();
            num = line.substring(2);
            b = Integer.parseInt(num.toString());

            writer.close();
            return new File("mid.txt");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}