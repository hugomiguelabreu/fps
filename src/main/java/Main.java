import java.io.IOException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws IOException {

        Subscriber s = null;

        if (args[0].equals("PUB")){
            Publisher p = new Publisher();
            p.start();
        }else{
            s = new Subscriber();
            s.start();
        }

        // create a scanner so we can read the command-line input
        Scanner scanner = new Scanner(System.in);
        String si;
        while(!(si = scanner.next()).equals("quit")){
            s.para();
        }

    }

}
