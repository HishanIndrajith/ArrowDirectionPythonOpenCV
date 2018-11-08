package Tests;

import java.awt.image.BufferedImage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;

import javax.imageio.ImageIO;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import com.sun.prism.paint.Color;

import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.RotatedRect;


public class TriangleDir extends Application {
  @Override 
  public void start(Stage stage) throws IOException {   
     WritableImage writableImage = loadImage(); 
 
     //Setting the image view 
     ImageView imageView = new ImageView(writableImage); 
       
     //Setting the position of the image 
     imageView.setX(50); 
     imageView.setY(25); 
       
     //setting the fit height and width of the image view 
     imageView.setFitHeight(600); 
     imageView.setFitWidth(1000);
     
     //Setting the preserve ratio of the image view
     imageView.setPreserveRatio(true);
     
     //Creating a Group object  
     Group root = new Group(imageView);
     
     //Creating a scene object
     Scene scene = new Scene(root, 700, 700);
     
     //Setting title to the Stage 
     stage.setTitle("Loading an image");
     
     //Adding scene to the stage
     stage.setScene(scene);

     //Displaying the contents of the stage
     stage.show();
  } 
  public WritableImage loadImage() throws IOException {
     //Loading the OpenCV core library  
     System.loadLibrary( Core.NATIVE_LIBRARY_NAME ); 
     
     String file ="D://Bsc(Computer_Engineering)//RoboGames//1.jpg";
     Mat image = Imgcodecs.imread(file);
     Mat gray = new Mat(); 
      
     Mat mono=new Mat();
	 Mat blur=new Mat();
	 Mat thresh=new Mat();
	 Mat erodeImg=new Mat();
	 Mat erode=new Mat();
	 Mat dilateImg=new Mat();
	 Mat dilate=new Mat();
	 Mat notused=new Mat();
     Imgproc.cvtColor(image, mono, Imgproc.COLOR_BGR2GRAY);
     Imgproc.GaussianBlur(mono, blur, new Size(9, 9), 2, 2);
     Imgproc.threshold(blur, thresh, 0, 255, Imgproc.THRESH_BINARY_INV|Imgproc.THRESH_OTSU);
     Imgproc.erode(thresh, erodeImg, erode);
     Imgproc.dilate(erodeImg, dilateImg, dilate);                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       
     List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
     Imgproc.findContours(dilateImg, contours, notused, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE); 
     //Imgproc.drawContours(image, contours, -1, new Scalar(64, 255, 0));
     
     Mat line = new Mat();
     if(contours.size()==1) {
    	 System.out.println("1 contour detected");
         MatOfPoint2f  NewMtx = new MatOfPoint2f( contours.get(0).toArray() );
         //MatOfPoint2f pointsMat = new MatOfPoint2f(points.toArray(new Point[]{}));
         Point center = new Point();
         float radius [] = new float[2];
         Imgproc.minEnclosingCircle(NewMtx,center,radius);
         int rad =(int)radius[0];
         System.out.println("Centre = "+center.x+ ", "+center.y+" radius = "+rad);
       //Drawing a Circle
         Imgproc.circle (
        	image,                 //Matrix obj of the image
            new Point(center.x, center.y),    //Center of the circle
            rad,               //Radius
            new Scalar(0, 0, 255),  //Scalar object for color
            2                   //Thickness of the circle
         );
         List<Point> corners = new ArrayList<Point>();
         for(Point point : contours.get(0).toArray())
         {
        	 double leftV = (point.y-center.y)*(point.y-center.y)+(point.x-center.x)*(point.x-center.x);
        	 if(leftV > rad*rad) {
        		 corners.add(point);
        		 System.out.println("hi"+point.x+" "+point.y); 
        	 }
         }
         Point p1 = new Point();
         int pn1=0;
         Point p2 = new Point();
         int pn2=0;
         Point p3 = new Point();
         int pn3=0;
         
         p1=corners.get(0);
         pn1++;
         for(Point point : corners) {
        	 if(point.x < p1.x/pn1+5 && point.x > p1.x/pn1-5 && point.y < p1.y/pn1+5 && point.y > p1.y/pn1-5) {
        		 p1.x=p1.x+point.x;
        		 p1.y=p1.y+point.y;
        		 pn1++;
        	 }else {
        		 if(pn2==0) {
        			 p2=point;
        			 pn2++;
        		 }else if(point.x < p2.x/pn2+5 && point.x > p2.x/pn2-5 && point.y < p2.y/pn2+5 && point.y > p2.y/pn2-5) {
        			 p2.x=p2.x+point.x;
        			 p2.y=p2.y+point.y;
            		 pn2++;
        		 }else {
        			 if(pn3==0) {
            			 p3=point;
            			 pn3++;
            		 }else if(point.x < p3.x/pn3+5 && point.x > p3.x/pn3-5 && point.y < p3.y/pn3+5 && point.y > p3.y/pn3-5) {
            			 p3.x=p3.x+point.x;
            			 p3.y=p3.y+point.y;
                		 pn3++;
            		 } 
        		 }
        		 
        	 }
         }
         p1.x/=pn1;
         p2.x/=pn2;
         p3.x/=pn3;
         p1.y/=pn1;
         p2.y/=pn2;
         p3.y/=pn3;
         System.out.println("p1 "+p1.x+" "+p1.y); 
         System.out.println("p2 "+p2.x+" "+p2.y); 
         System.out.println("p3 "+p3.x+" "+p3.y);
    

         
         double length1_2 = (p1.x - p2.x)*(p1.x - p2.x)+(p1.y - p2.y)*(p1.y - p2.y);
         double length2_3 = (p2.x - p3.x)*(p2.x - p3.x)+(p2.y - p3.y)*(p2.y - p3.y);
         double length3_1 = (p3.x - p1.x)*(p3.x - p1.x)+(p3.y - p1.y)*(p3.y - p1.y);
         
         Point directed;
         if(length1_2<=length2_3) {
        	 if(length3_1<length1_2) {
        		 directed=p2;
        	 }else {
        		 directed=p3;
        	 }
         }else {
        	 if(length3_1<length2_3) {
        		 directed=p2;
        	 }else {
        		 directed=p1;
        	 }
         }
         
         Imgproc.circle (
 	            image,                 //Matrix obj of the image
 	            directed,    //Center of the circle
 	            5,                    //Radius
 	            new Scalar(0, 0, 255),  //Scalar object for color
 	            4                     //Thickness of the circle
 	         );
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
//         Point p1_2=new Point();
//         Point p2_3=new Point();
//         Point p3_1=new Point();
//         //assuming 1:10 ratio of arrow cut with arrow length --MUST ADJUST **********************
//         p1_2.x=(((p1.x+p2.x)/2)*4+p3.x)/5;
//         p1_2.y=(((p1.y+p2.y)/2)*4+p3.y)/5;
//         
//         p2_3.x=(((p2.x+p3.x)/2)*4+p1.x)/5;
//         p2_3.y=(((p2.y+p3.y)/2)*4+p1.y)/5;
//         
//         p3_1.x=(((p3.x+p1.x)/2)*4+p2.x)/5;
//         p3_1.y=(((p3.y+p1.y)/2)*4+p2.y)/5;
//         
//         double [] color1_2 = image.get((int)p1_2.x, (int)p1_2.y);
//         double []  color2_3 = image.get((int)p2_3.x, (int)p2_3.y);
//         double []  color3_1 = image.get((int)p3_1.x, (int)p3_1.y);
//         
//         if(color1_2[0]<128 && color1_2[1]<128 && color1_2[2]< 128) {
//        	 Imgproc.circle (
//        	            image,                 //Matrix obj of the image
//        	            p3,    //Center of the circle
//        	            5,                    //Radius
//        	            new Scalar(0, 0, 255),  //Scalar object for color
//        	            4                     //Thickness of the circle
//        	         );
//         }else if(color2_3[0]<128 && color2_3[1]<128 && color2_3[2]< 128) {
//        	 Imgproc.circle (
//        	            image,                 //Matrix obj of the image
//        	            p1,    //Center of the circle
//        	            5,                    //Radius
//        	            new Scalar(0, 0, 255),  //Scalar object for color
//        	            4                     //Thickness of the circle
//        	         );
//         }else if(color3_1[0]<128 && color3_1[1]<128 && color3_1[2]< 128) {
//        	 Imgproc.circle (
//        	            image,                 //Matrix obj of the image
//        	            p2,    //Center of the circle
//        	            5,                    //Radius
//        	            new Scalar(0, 0, 255),  //Scalar object for color
//        	            4                     //Thickness of the circle
//        	         );
//         }
         
         
         
         
         
         
         
         
         
         
//         RotatedRect r = Imgproc.minAreaRect(NewMtx);
//         System.out.println(r.angle);
//         Mat m =new Mat();
//         Imgproc.boxPoints(r, m);
//         System.out.println(m.dump());
//         double x1 = m.get(0, 0)[0];
//         double y1 = m.get(0, 1)[0];
//         double x2 = m.get(1, 0)[0];
//         double y2 = m.get(1, 1)[0];
//         double x3 = m.get(2, 0)[0];
//         double y3 = m.get(2, 1)[0];
//         double x4 = m.get(3, 0)[0];
//         double y4 = m.get(3, 1)[0];
//         
//         double length1 = Math.sqrt((y1-y2)*(y1-y2)+(x1-x2)*(x1-x2));
//         double length2 = Math.sqrt((y3-y2)*(y3-y2)+(x3-x2)*(x3-x2)); 
//         double mx1=0; //mid pint 1
//         double my1=0;
//         double mx2=0; //mid point 2
//         double my2=0;
//         
//         if(length1<length2) {
//        	 mx1=(x1+x2)/2;
//        	 my1=(y1+y2)/2;
//        	 mx2=(x3+x4)/2;
//        	 my2=(y3+y4)/2;
//         }else {
//        	 mx1=(x2+x3)/2;
//        	 my1=(y2+y3)/2;
//        	 mx2=(x4+x1)/2;
//        	 my2=(y4+y1)/2; 
//         }
////         double[] lengths= new double[6];
////         lengths[0] = Math.sqrt((y1-y2)*(y1-y2)+(x1-x2)*(x1-x2)); //red
////         lengths[1] = Math.sqrt((y3-y2)*(y3-y2)+(x3-x2)*(x3-x2)); //green
////         lengths[2] = Math.sqrt((y3-y4)*(y3-y4)+(x3-x4)*(x3-x4)); //blue
////         lengths[3] = Math.sqrt((y4-y1)*(y4-y1)+(x4-x1)*(x4-x1)); //black
////         lengths[4] = Math.sqrt((y1-y3)*(y1-y3)+(x1-x3)*(x1-x3)); //orange
////         lengths[5] = Math.sqrt((y2-y4)*(y2-y4)+(x2-x4)*(x2-x4)); //yellow
////         Arrays.sort(lengths); //smallest at beginning 0 and 1
//         
//         
//         
////        System.out.println("red ="+lengths[0]);
////        System.out.println("green ="+lengths[1]);
////        System.out.println("blue ="+lengths[2]);
////        System.out.println("black ="+lengths[3]);
////        System.out.println("orange ="+lengths[4]);
////        System.out.println("yellow ="+lengths[5]);
//         Imgproc.line (
//           	  image,                    //Matrix obj of the image
//                 new Point(mx1,my1),        //p1
//                 new Point(mx2,my2),       //p2
//                 new Scalar(0,0,255),     //Scalar object for color
//                 2                         //Thickness of the line
//              );
     }
     
     
     //Encoding the image
     MatOfByte matOfByte = new MatOfByte();
     Imgcodecs.imencode(".jpg", image, matOfByte);
     

     //Storing the encoded Mat in a byte array
     byte[] byteArray = matOfByte.toArray();
     
     //Displaying the image
     InputStream in = new ByteArrayInputStream(byteArray); 
     BufferedImage bufImage = ImageIO.read(in);

     System.out.println("Image Loaded");
     WritableImage writableImage = SwingFXUtils.toFXImage(bufImage, null);
     return writableImage; 
  }
//  public static void main(String [] args) {
//	  System.out.println("hi");
//  }

}
