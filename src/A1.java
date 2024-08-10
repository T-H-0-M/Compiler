import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.io.FileReader;

public class A1 {

    public static void main(String[] args) {
        String filePath = args[0];

        TokenTerminator tokenTerminator = new TokenTerminator(filePath);

    }

}
