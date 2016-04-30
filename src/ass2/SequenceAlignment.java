package ass2;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class SequenceAlignment {
    HashMap<String, String> sequenceQueries;
    HashMap<String, String> sequenceText;
    int threshold;
    int k;
    int a;
    int b;
    TransitionMatrix matrix;

    public SequenceAlignment(String queriesFile, String matrixFile, String textFile, int num, int threshold) {
        k = num;
        this.threshold = threshold;
        sequenceQueries = new HashMap<>();
        FastaSequence reader = new FastaSequence(queriesFile);
        for (int i = 0; i < reader.size(); i++) {
            sequenceQueries.put(reader.getDescription(i), reader.getSequence(i));
        }
        sequenceText = new HashMap<>();
        reader = new FastaSequence(textFile);
        for (int i = 0; i < reader.size(); i++) {
            sequenceText.put(reader.getDescription(i), reader.getSequence(i));
        }


        matrix = new TransitionMatrix(fixMatrix(matrixFile));
        File toDelete = new File("mid.txt");
        toDelete.deleteOnExit();

        //make hash of words and indexes in text

        LinkedHashMap<String, ArrayList<Word>> wordsTextMap = new LinkedHashMap<String, ArrayList<Word>>();

        String currentWord;
        for (Map.Entry<String, String> entry : sequenceText.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            for (int i = 0; i <= value.length() - k; i++) {
                currentWord = value.substring(i, i + k + 1);
                if (wordsTextMap.containsKey(currentWord))    //contains word add only new location
                    wordsTextMap.get(currentWord).add(new Word(key, i));
                else {
                    ArrayList<Word> arraylist = new ArrayList<Word>();    //create a new arraylist for the word
                    arraylist.add(new Word(key, i));
                    wordsTextMap.put(currentWord, arraylist);
                }
            }
        }

        queryPreProcess();
    }

    private void queryPreProcess() {
        LinkedHashMap<String, ArrayList<Word>> similerWordsMap = new LinkedHashMap<String, ArrayList<Word>>();
        //for each query
        String currentWindow;
        for (Map.Entry<String, String> entry : sequenceQueries.entrySet()) {
            String value = entry.getValue();
            //for each sliding window, find all similar words
            for (int i = 0; i <= value.length() - k; i++) {
                currentWindow = value.substring(i, i + k + 1);
                if (!similerWordsMap.containsKey(currentWindow)) {
                    similerWordsMap.put(currentWindow, findSimilarWords(currentWindow));
                }
            }
        }
    }


    public File fixMatrix(String orgMatrix) {
        try {
            BufferedReader bf = new BufferedReader(new FileReader(orgMatrix));
            BufferedWriter writer = new BufferedWriter(new FileWriter("mid.txt"));
            String line = bf.readLine();
            while ((line != null) && line.contains("#")) {    //dont read lines with #
                line = bf.readLine();
            }
            while ((line != null) && (!line.contains("#"))) { //read matrix
                writer.write(line);
                writer.write("\n");
                writer.flush();
                line = bf.readLine();
            }
            while ((line != null) && line.contains("#")) { //dont read lines with #
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


    private ArrayList<Word> findSimilarWords(String currentWindow) {
        ArrayList<Word> similarWords = new ArrayList<>();
        //find similar words to "currentWindow"
        checkPerm(similarWords,currentWindow,currentWindow,0,currentWindow.length()*5);
        return similarWords;
    }
    private ArrayList<Word> checkPerm(ArrayList<Word> similarWords, String currentWindow,String originalWindow, int i, int currentScore){
        StringBuilder currPerm = new StringBuilder(currentWindow);
        if(i == currentWindow.length()){
            return similarWords;
        }
        else{
            //check all permutations of i and send them to checkPerm
            //the idea: change currWindow[i] from X to Y, if it's already Y do nothing
            if(currPerm.charAt(i) != 'a'){
                //if score after change >= threshold, add perm to array
                if(currentScore + matrix.score(currPerm.charAt(i),'a') >= threshold){
                    currPerm.setCharAt(i,'a');
                    similarWords.add(new Word(currPerm.toString(),currentScore + matrix.score(currPerm.charAt(i),'a')));
                    checkPerm(similarWords,currPerm.toString(),originalWindow,i+1,currentScore + matrix.score(currPerm.charAt(i),'a'));
                }
            }
            if(currPerm.charAt(i) != 't'){
                //if score after change >= threshold, add perm to array
                if(currentScore + matrix.score(currPerm.charAt(i),'t') >= threshold){
                    currPerm.setCharAt(i,'t');
                    similarWords.add(new Word(currPerm.toString(),currentScore + matrix.score(currPerm.charAt(i),'t')));
                    checkPerm(similarWords,currPerm.toString(),originalWindow,i+1,currentScore + matrix.score(currPerm.charAt(i),'t'));
                }
            }
            if(currPerm.charAt(i) != 'g'){
                //if score after change >= threshold, add perm to array
                if(currentScore + matrix.score(currPerm.charAt(i),'g') >= threshold){
                    currPerm.setCharAt(i,'g');
                    similarWords.add(new Word(currPerm.toString(),currentScore + matrix.score(currPerm.charAt(i),'g')));
                    checkPerm(similarWords,currPerm.toString(),originalWindow,i+1,currentScore + matrix.score(currPerm.charAt(i),'g'));
                }
            }
            if(currPerm.charAt(i) != 'c'){
                //if score after change >= threshold, add perm to array
                if(currentScore + matrix.score(currPerm.charAt(i),'c') >= threshold){
                    currPerm.setCharAt(i,'c');
                    similarWords.add(new Word(currPerm.toString(),currentScore + matrix.score(currPerm.charAt(i),'c')));
                    checkPerm(similarWords,currPerm.toString(),originalWindow,i+1,currentScore + matrix.score(currPerm.charAt(i),'c'));
                }
            }
            return similarWords;
        }
    }
}