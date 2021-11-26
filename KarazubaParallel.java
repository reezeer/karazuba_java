package karazuba;

import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.math.BigDecimal;
import java.io.BufferedWriter;
import java.io.IOException;

public class KarazubaParallel{

    public static void main(String[] args) {
    	
        // Create new CSV file and write column names into the file
        try {
			BufferedWriter writer = Files.newBufferedWriter(Paths.get("karazubaResults.csv"));
			
			// Write column names
			writer.write("X, Y, Resultat, Dauer in ms");
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
	        	
	        	// Create new ThreadPool
	        	ExecutorService pool = Executors.newCachedThreadPool();
	        	
	        	// Create new KarazubaThread object and store it in a callable 
	            Callable<BigInteger> mainCallable = new KarazubaThread(xNumber.toString(),yNumber.toString(), pool);
	            
	            // Submit the created callable to the ThreadPool created on line 49
	            Future<BigInteger> mainFuture = pool.submit(mainCallable);
	            
	            // Create variable of type BigInteger with value 0. It is used to store the result of the
	            // callable, created on line 52
	            BigInteger mainResult = new BigInteger("0");
	            
	            // Store result of the submitted callable on line 55
	    		try {
	    			mainResult = mainFuture.get();
	    		} catch (InterruptedException e) {
	    			// TODO Auto-generated catch block
	    			e.printStackTrace();
	    		} catch (ExecutionException e) {
	    			// TODO Auto-generated catch block
	    			e.printStackTrace();
	    		}
	    		
	    		// Shutdown the pool
	            pool.shutdown();
	            try {
	    			pool.awaitTermination(1000, TimeUnit.MILLISECONDS);
	    		} catch (InterruptedException e) {
	    			// TODO Auto-generated catch block
	    			e.printStackTrace();
	    		}
	            
	            // Store current time
	            long endTime = System.currentTimeMillis();
	            
	            // Calculate duration of the calculation - difference between start and end time in milliseconds
	            int duration = (int)endTime- (int)startTime;
	            
	            // Store the information in a String variable to write it to the csv afterwards
	            String newLineForCSV = xNumber.toString() + ", " + yNumber.toString() + ", " + mainResult.toString() + ", " + Integer.toString(duration);
	            
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

class KarazubaThread implements Callable<BigInteger>{
    private String mX;
    private String mY;
    private volatile BigInteger result;
    private ExecutorService mP;
  
    public KarazubaThread(String x, String y, ExecutorService p) {
    	mX = x;
    	mY = y;
    	mP = p;
    }

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

    static BigInteger calculateKarazuba(String x, String y, ExecutorService pool){

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
        
        // Create new KarazubaThreads for ac, bd and pq and store them in callables
        // Afterwards, submit the callables to the pool, create at the beginning
        Callable<BigInteger> acCallable = new KarazubaThread(a,c, pool);
        Future<BigInteger> acFuture = pool.submit(acCallable);
        Callable<BigInteger> bdCallable = new KarazubaThread(b,d, pool);
        Future<BigInteger> bdFuture = pool.submit(bdCallable);
        Callable<BigInteger> pqCallable = new KarazubaThread(p,q, pool);
        Future<BigInteger> pqFuture = pool.submit(pqCallable);
        
        
        // Get the values of the submitted callables
        BigInteger ac = new BigInteger("0");
		try {
			ac = acFuture.get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
        
        BigInteger bd = new BigInteger("0");
		try {
			bd = bdFuture.get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
        
        BigInteger pq = new BigInteger("0");
		try {
			pq = pqFuture.get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		
		// Calculate adbc
        BigInteger adbc = pq.subtract(ac).subtract(bd);;

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

    @Override
    public BigInteger call(){
    	BigInteger result;
        result = calculateKarazuba(this.mX, this.mY, this.mP);
        return result;
    }
}
