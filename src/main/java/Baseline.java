import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Baseline {

    public static void main(String[] args) {

        String testFileString;
        try {

            boolean isSpam = false;
            HashMap<String, Integer> spamWordsList = new HashMap<>();

            testFileString = new Scanner(new File("C://email//temp.txt")).useDelimiter("\\A").next();
            testFileString.replace("(", "").replace(".", "").replace(":", "").replace(")", "").replace("\"", "");

            spamWordsList.put("discount", 0);
            spamWordsList.put("sale", 0);
            spamWordsList.put("credit", 0);
            spamWordsList.put("email", 0);

            String[] wordArrayTest = testFileString.split(" ");

            for (String s : wordArrayTest) {
                if (spamWordsList.containsKey(s)) {
                    spamWordsList.put(s, spamWordsList.get(s) + 1);
                }
            }

            for (Map.Entry<String, Integer> entry : spamWordsList.entrySet()) {
                if (entry.getValue() > 3) {
                    isSpam = true;
                    break;
                }
            }

            if (isSpam == true) {
                System.out.println("This is a spam email");
            } else {
                System.out.println("This is not a spam email");
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Baseline.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }
}
