package karazuba;

import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;

public class Karazuba{

    // Method which returns the value of x and y with leading zeroes,
    // if the numbers are not equal
    static String[] leadingZeros(String x, String y){

        // If x is less long than y, add the the length difference
        // as leading zeroes
        if (x.length() < y.length()){
            int diff = y.length() - x.length();
            String zeroes = "";
            for(int i = 0; i < diff; i++){
                zeroes = zeroes.concat("0");
            }
            x = zeroes + x;

        //Same for y
        }else if (y.length() < x.length()){
            int diff = x.length() - y.length();
            String zeroes = "";
            for(int i = 0; i < diff; i++){
                zeroes = zeroes.concat("0");
            }
            y = zeroes + y;
        }
        // If they are the same length but odd, add one leading zero
        //to both of them
        if (x.length() == y.length() && x.length() % 2 == 1){
            String zero = "0";
            x = zero.concat(x);
            y = zero + y;
        }
        return new String[]{x,y};

    }

    static BigInteger calculateKarazuba(String x, String y){

        // Transform x and y to BigInteger
        BigInteger bigX = new BigInteger(x);
        BigInteger bigY = new BigInteger(y);

        // If x and y are less than 10, return their product
        if(x.length() == 1 && y.length() == 1){
            return bigX.multiply(bigY);
        }

        // If x or y is 0, return 0
        if(bigX.equals(0) || bigY.equals(0)){
            return new BigInteger("0");
        }

        // Check, if x and y are same length and can be divided by 2
        String[] transformedValues = leadingZeros(x, y);
        x = transformedValues[0];
        y = transformedValues[1];

        // Get length of of x and y
        int xLength = x.length();
        int yLength = y.length();

        // Declare a, b, c, d and calculate p and q
        String a = x.substring(0, (xLength/2));
        String b = x.substring((xLength/2), xLength);
        String c = y.substring(0, (yLength/2));
        String d = y.substring((yLength/2), yLength);
        String p = new BigInteger(a).add(new BigInteger(b)).toString();
        String q = new BigInteger(c).add(new BigInteger(d)).toString();

        // Declare n for calculation of the result
        int n = xLength;

        // Recursion to calculate ac, bd and pq
        BigInteger ac = calculateKarazuba(a, c);
        BigInteger bd = calculateKarazuba(b, d);
        BigInteger pq = calculateKarazuba(p, q);

        BigInteger adbc = pq.subtract(ac).subtract(bd);

        // Calculate the product of x and y
        BigInteger result = 
            bd.add(
                (ac.multiply(
                    BigDecimal.valueOf(Math.pow(10,n)).toBigInteger()
                    )
                ).add(
                    (adbc.multiply(
                        BigDecimal.valueOf(Math.pow(10, (n/2))).toBigInteger()
                    )
                )
                )
            );
        return result;
    }

    public static void main(String[] args) {

	    //Start algorithm
		// Create new CSV file and write column names into the file
	    try {
			BufferedWriter writer = Files.newBufferedWriter(Paths.get("karazubaResultsSeq.csv"));
			
			// Write column names
			writer.write("X, Y, Resultat, Dauer (ms)");
			writer.newLine();
			
			// Calculate 1000 products of random generated factors with random length
	        for(int iteration = 0; iteration <= 1000; iteration++) {
	        	
	        	// Create two variables of the type Random
	        	Random randomX = new Random();
	        	Random randomY = new Random();
	        	
	        	// Create two integer variables for storing random integer values between 1 and 30
	        	// They will be used as parameters for the length of the generated random number for x and y
	        	int lenX = new Random().nextInt((30 - 1) + 1) + 1;
	        	int lenY = new Random().nextInt((30 - 1) + 1) + 1;
	        	
	        	//Generate random numbers with random length
	        	BigInteger xNumber = new BigInteger(lenX, randomX);
	        	BigInteger yNumber = new BigInteger(lenY, randomY);
	        	
	        	// Start counting the execution time
	        	long startTime = System.currentTimeMillis();
	        	
	        	BigInteger res = calculateKarazuba(xNumber.toString(), yNumber.toString());
	            
	            // Store current time
	            long endTime = System.currentTimeMillis();
	            
	            // Calculate duration of the calculation - difference between start and end time in milliseconds
	            int duration = (int)endTime- (int)startTime;
	            
	            // Store the information in a String variable to write it to the csv afterwards
	            String newLineForCSV = xNumber.toString() + ", " + yNumber.toString() + ", " + res.toString() + ", " + Integer.toString(duration);
	            
	            // Write the String from line 88 to the csv file
	            writer.write(newLineForCSV);
	            
	            // Make line break in csv file
	            writer.newLine();
	    	}
	        
	        // Close the writer
	        writer.close();
	    	} catch (IOException e1) {
	    		e1.printStackTrace();
		}
	}
}
