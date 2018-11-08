package Tests;

import org.opencv.core.Core; 
import org.opencv.core.Mat;  
import org.opencv.core.CvType;  
import org.opencv.core.Scalar;   

class DisplayingMatrix { 
   public static void main(String[] args) {     
      //Loading the core library 
      System.loadLibrary(Core.NATIVE_LIBRARY_NAME);     

      //Creating a matrix 
      Mat matrix = new Mat(5, 5, CvType.CV_8UC1, new Scalar(0));  

      //Retrieving the row with index 0 
      Mat row0 = matrix.row(0);

      //setting values of all elements in the row with index 0 
      row0.setTo(new Scalar(1)); 

      //Retrieving the row with index 3 
      Mat col3 = matrix.col(3);  

      //setting values of all elements in the row with index 3 
      col3.setTo(new Scalar(3)); 

      //Printing the matrix 
      System.out.println("OpenCV Mat data:\n" + matrix.dump()); 
   } 
}