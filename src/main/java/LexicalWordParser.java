
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 *
 */
public class LexicalWordParser {

    static HashMap<String, Integer> spamWords = new HashMap<>();
    static HashMap<String, Integer> hamWords = new HashMap<>();
    static double totalHamWords = 0;
    static double totalSpamWords = 0;
    static ArrayList<String> uniqueWords = new ArrayList<>();

    public static void main(String[] args) throws FileNotFoundException {

        try {
            String fileString = new Scanner(
                    new File(LexicalWordParser.class.getResource("spam_training.txt").toURI().getPath()))
                    .useDelimiter("\\A").next();
            fileString.replace("(", "").replace(".", "").replace(":", "").replace(")", "").replace("\"", "");

            String[] wordArray = fileString.split(" ");
            for (String s : wordArray) {
                if (spamWords.containsKey(s)) {
                    spamWords.put(s, spamWords.get(s) + 1);
                } else {
                    spamWords.put(s, 1);
                    uniqueWords.add(s);
                }
                totalSpamWords++;
            }

            fileString = new Scanner(
                    new File(LexicalWordParser.class.getResource("ham_training.txt").toURI().getPath()))
                    .useDelimiter("\\A").next();

            fileString.replace("(", "").replace(".", "").replace(":", "").replace(")", "").replace("\"", "");

            wordArray = fileString.split(" ");
            for (String s : wordArray) {
                if (hamWords.containsKey(s)) {
                    hamWords.put(s, hamWords.get(s) + 1);
                } else {
                    hamWords.put(s, 1);
                    uniqueWords.add(s);
                }
                totalHamWords++;
            }

            HashMap<String, Double> spamProb = new HashMap<>();
            HashMap<String, Double> hamProb = new HashMap<>();

            for (String s : uniqueWords) {
                Double numerator = 1.0;
                if (spamWords.containsKey(s)) {
                    numerator += spamWords.get(s);
                } else {
                    numerator = 0 + numerator;
                }

                Double denominator = totalSpamWords + uniqueWords.size();
                spamProb.put(s, numerator / denominator);
                numerator = 1.0;
                if (hamWords.containsKey(s)) {
                    numerator += hamWords.get(s);
                } else {
                    numerator = 0 + numerator;
                }
                denominator = totalHamWords + uniqueWords.size();
                hamProb.put(s, numerator / denominator);
            }

            String testFileString = new Scanner(new File(Baseline.class.getResource("temp.txt").toURI())).useDelimiter("\\A").next();
            testFileString.replace("(", "").replace(".", "").replace(":", "").replace(")", "").replace("\"", "");
            String[] wordArrayTest = testFileString.split(" ");
            Double spamProbability = 0.0;

            for (String s : wordArrayTest) {
                if (spamWords.containsKey(s)) {
                    spamProbability += Math.log(spamWords.get(s));
                }
            }

            Double hamProbability = 0.0;
            for (String s : wordArrayTest) {
                if (hamWords.containsKey(s)) {
                    hamProbability += Math.log(hamWords.get(s));
                }
            }
            Helper.displayClassification(spamProbability, hamProbability);

        } catch (URISyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException ex) {
            Logger.getLogger(LexicalWordParser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
