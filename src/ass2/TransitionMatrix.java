package ass2;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class TransitionMatrix {

    private int[][] matrix;

    public TransitionMatrix(File matrixFile){
        matrix = new int[7][7];
        Scanner input;
        try {
            input = new Scanner(matrixFile);
            //throw first line
            for(int i = 0; i < 7; i++){
                input.next();
            }
            for(int i = 0; i < 7; ++i)
            {
                //throw first char
                input.next();
                for(int j = 0; j < 7; ++j)
                {
                    if(input.hasNext())
                    {
                        if(input.hasNextInt()){
                            matrix[i][j] = input.nextInt();
                        }
                        else{
                            input.next();
                        }
                    }
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    public int score(char from, char to){
        int i = 99,j = 99;
        switch(from){
            case 'A': i = 0;
                break;
            case 'T': i = 1;
                break;
            case 'G': i = 2;
                break;
            case 'C': i = 3;
                break;
            case 'U': i = 4;
                break;
            case 'N': i = 5;
                break;
            case '*': i = 6;
                break;
        }
        switch(to){
            case 'A': j = 0;
                break;
            case 'T': j = 1;
                break;
            case 'G': j = 2;
                break;
            case 'C': j = 3;
                break;
            case 'U': j = 4;
                break;
            case 'N': j = 5;
                break;
            case '*': j = 6;
                break;
        }
        return matrix[i][j];
    }
    public String toString(){
        StringBuilder str = new StringBuilder();
        for(int i = 0; i < 7; i++){
            for(int j = 0; j < 7; j++){
                str.append(matrix[i][j] + " ");
            }
            str.append("\n");
        }
        return String.valueOf(str);
    }
}
