import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.List;
import java.util.HashMap;
import java.util.Arrays;
import java.lang.ArrayIndexOutOfBoundsException;
import java.lang.NumberFormatException;

public class PlagiarismChecker {
    public static void makeTuples(List<List<String>> inList, String tuple, int index, List<String> outList){
        if(index == inList.size()) {
            outList.add(tuple);
            return;
        }
        List<String> wordList = inList.get(index);
        for(String word: wordList) makeTuples(inList, tuple + word, index+1, outList);
    }
    
    public static String detect(String synonymFile, String file1, String file2, int tupleSize){
        String result;
        int tupleCount = 0;
        int plagiarismCount = 0;
        HashMap<String, List<String>> synonyms = new HashMap<String, List<String>>();
        List<String> tuples = new ArrayList<String>();
        
        if(tupleSize < 1) return "Hey, watch your input! Make sure the tuple size is greater than 0.";
        
        try (Scanner file = new Scanner(new File(synonymFile))) {
            while(file.hasNextLine()) {
                String line = file.nextLine();
                List<String> words = new ArrayList<String>(Arrays.asList(line.split("\\s+")));
                for(String word : words){
                    if(synonyms.containsKey(word)){
                        synonyms.get(word).addAll(words);
                    }else{
                        synonyms.put(word.toLowerCase(), words);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            return fail(synonymFile);
        }
        
        try (Scanner file = new Scanner(new File(file1))) {
            while(file.hasNextLine()) {
                String line = file.nextLine();
                List<String> words = new ArrayList<String>(Arrays.asList(line.split("\\s+")));
                while(words.size() >= tupleSize){
                    List<List<String>> newTuples = new ArrayList<List<String>>();
                    for(int i = 0; i < tupleSize; i++){
                        String word = words.get(i).toLowerCase().replaceAll("[^A-Za-z0-9]", "");
                        boolean hasSynonym = synonyms.containsKey(word);
                        List<String> wordsToAdd = hasSynonym ? synonyms.get(word) : Arrays.asList(word);
                        newTuples.add(wordsToAdd);
                    }
                    makeTuples(newTuples,"", 0, tuples);
                    words.remove(0);
                }
            }
        } catch (FileNotFoundException e) {
            return fail(file1);
        }
        
        try (Scanner file = new Scanner(new File(file2))) {
            while(file.hasNextLine()) {
                String line = file.nextLine();
                List<String> words = new ArrayList<String>(Arrays.asList(line.split("\\s+")));
                while(words.size() >= tupleSize){
                    tupleCount++;
                    String newTuple = "";
                    for(int i = 0; i < tupleSize; i++){
                        String word = words.get(i).toLowerCase().replaceAll("[^A-Za-z0-9]", "");
                       	newTuple+= word;
                    }
                    if(tuples.contains(newTuple)) plagiarismCount++;
                    words.remove(0);
                }
            }
        } catch (FileNotFoundException e) {
            return fail(file2);
        }
        
        float percentPlagiarised = plagiarismCount * 100f / tupleCount;
        result = Math.round(percentPlagiarised) + "%";
        return result;
    }
    
    public static String fail(String fileName){
        return "Oh no! The source file \"" + fileName + "\" is missing. \n"
        + "Double check that the file is spelled correctly and is in this directory.";
    }
    
    public static void main(String[] args) {
        try{
            Integer DEFAULT = 3;
            String result;
            String synonymFile = args[0];
            String file1 = args[1];
            String file2 = args[2];
            int tupleSize = args.length == 4? Integer.parseInt(args[3]) : DEFAULT;
            result = detect(synonymFile, file1, file2, tupleSize);
            System.out.println(result);
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Oh no! Invalid arguments. Make sure your format looks like: \n"
                               + "[synonym file name].txt [file1 name].txt [file2 name].txt [tuple size](OPTIONAL)");
        } catch (NumberFormatException e){
            System.out.println("Oh no! You entered a bad tuple size. \n"
                               + "Make sure to enter an integer greater than zero!");
        }
    }
}
