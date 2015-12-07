/**
 * 	This class prints the formatted output
 *
 */
class Helper {

    static void displayClassification(double spam, double ham) {
        spam = ((spam - 100) / 100);
        ham = ((ham - 100) / 100);

        System.out.println("Spam probability:   " + spam);
        System.out.println("Ham probability:   " + ham);

        if (spam > ham) {
            System.out.println("This is a spam email");
        } else {
            System.out.println("This is not a spam email");

        }
    }
}
