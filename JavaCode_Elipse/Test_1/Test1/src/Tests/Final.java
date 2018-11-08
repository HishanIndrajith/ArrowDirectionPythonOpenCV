package Tests;

import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.ImageProducer;
import java.util.ArrayList;
import java.util.List;



public class Final extends JPanel {

   private static BufferedImage image; // The actual displayed image on screen
   private static CascadeClassifier faceCascade;
   private static CascadeClassifier eyesCascade;
   private static int absoluteFaceSize;
   static int oldSignal=-1;

   static boolean isTriangleFound=false;

   public static void main(String args[]) {
      System.loadLibrary(Core.NATIVE_LIBRARY_NAME); // Import openCV
      Final panel = new Final(); // Initialize itself

      // Initialize JPanel
      JFrame frame = new JFrame("Webcam");
      frame.setSize(200, 200);
      frame.setContentPane(panel);
      frame.setVisible(true);

      VideoCapture camera = new VideoCapture(0); // The camera

      // Attempt to set the frame size, but it doesn't really work. Only just makes it bigger
      camera.set(Videoio.CAP_PROP_FRAME_WIDTH, 1900);
      camera.set(Videoio.CAP_PROP_FRAME_HEIGHT, 1000);


      // Special window listener because there are threads that need to be shutdown on a close
      frame.addWindowListener(new WindowAdapter() {
         @Override
         public void windowClosing(WindowEvent e) {
            e.getWindow().dispose();
            camera.release();
            System.exit(0);
         }
      });

      if (camera.isOpened()) {

         // Create SwingWorker to encapsulate the process in a thread
         SwingWorker<Void, Mat> worker = new SwingWorker<Void, Mat>() {
            @Override
            protected Void doInBackground() throws Exception {

               // Put something into thisFrame so it doesn't glitch at the beginning
               Mat thisFrame = new Mat();
               camera.read(thisFrame);
               
               // Set up new Mats for diffing them later, manually set the amount of channels to avoid an openCV error
               Mat pastFrame = new Mat();

               // isCancelled is set by the SwingWorker
               while (!isCancelled()) {

                  thisFrame.copyTo(pastFrame); // What was previously the frame is now the pastFrame
                  camera.read(thisFrame); // Get camera image, and set it to currentImage
                  //Imgproc.cvtColor(thisFrame, thisFrame, Imgproc.COLOR_BGR2GRAY); // Convert the diff to gray

                  if (!thisFrame.empty()) {

                     // Set the frame size to have a nice border around the image
                     frame.setSize(thisFrame.width() + 40, thisFrame.height() + 60);
                     getMidPoint(1,thisFrame);
                     //detectCircles(thisFrame);
                     image = matrixToBuffer(thisFrame); // Update the display image
                     panel.repaint(); // Refresh the panel
                  } else {
                     System.err.println("Error: no frame captured");
                  }
                  //Thread.sleep(70); // Set refresh rate, as well as prevent the code from tripping over itself
               }
               return null;
            }
         };
         worker.execute();
      }
      return;
   }
   
   private static void getMidPoint(int bias,Mat camImage)
   {
       //camera.read(camImage);
	   Mat mono=new Mat();
	   Mat blur=new Mat();
	   Mat thresh=new Mat();
	   Mat erodeImg=new Mat();
	   Mat erode=new Mat();
	   Mat dilateImg=new Mat();
	   Mat dilate=new Mat();
	   Mat notused=new Mat();
	   int x=2*camImage.cols()/8; //original = 10
	   int y=2*camImage.rows()/3; // must be 2*camImage.rows()/3
	   int width=4*camImage.cols()/8; //original =camImage.cols()-20
	// must be adjusted with camera position ********
	   int height= 80; //camImage.rows()/8; //12 must be
       Mat roi = new Mat(camImage, new Rect(x, y, width, height));
       
       Imgproc.cvtColor(roi, mono, Imgproc.COLOR_BGR2GRAY);
       Imgproc.GaussianBlur(mono, blur, new Size(9, 9), 2, 2);
       Imgproc.threshold(blur, thresh, 0, 255, Imgproc.THRESH_BINARY|Imgproc.THRESH_OTSU);
       Imgproc.erode(thresh, erodeImg, erode);
       Imgproc.dilate(erodeImg, dilateImg, dilate);
       List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
       Imgproc.findContours(dilateImg, contours, notused, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
       //Imgproc.drawContours(roi, contours, -1, new Scalar(125, 25, 0));
       int triangleContourId = 0;
       int size = contours.size();
       System.out.println("no of contours = "+size);
       if(size==1) {
    	 isTriangleFound=false;
    	 
    	   MatOfPoint cont = contours.get(0);
           Moments mu = Imgproc.moments(cont, false);
           if (mu.get_m00() > 100.0){
        	   Rect r = Imgproc.boundingRect(cont);
               Imgproc.rectangle(
            		   camImage,                                   //where to draw the box
                       new Point(x+r.x, y+r.y),                            //bottom left
                       new Point(x+r.x+r.width,y+r.y+r.height), //top right
                       new Scalar(0, 255, 255),//RGB color
                       2
                );
                int midOfBlackSqr = (r.x + r.width/2); //respective to green box
                int midOfGreenSqr = width/2;
                int gap = midOfBlackSqr-midOfGreenSqr;
                   // line width = 100 according to camera , ground gap
                   if(r.width<130) { //prevent taking beginning of circle as line           	   
                	   //System.out.println(gap);
                	   controlRobot(camImage,""+gap);
                   }else {
                	   //System.out.println("move forward");
                	   controlRobot(camImage,""+0);
                   }
                }
         //rectangle main
           Imgproc.rectangle(
        		   camImage,                                   //where to draw the box
                   new Point(x, y),                           //bottom left
                   new Point(x+width,y+height), //top right
                   new Scalar(64, 255, 0) ,//RGB colour
                   2
            );
         // Drawing a line
           Imgproc.line (
        	  camImage,                    //Matrix obj of the image
              new Point(x+width/2,y-20),        //p1
              new Point(x+width/2, y),       //p2
              new Scalar(64, 255, 0),     //Scalar object for color
              2                         //Thickness of the line
           );
       }else if(size==2 && !isTriangleFound) {

    	   //System.out.println("move forward");
           controlRobot(camImage,""+0);
    	 //rectangle main
           Imgproc.rectangle(
        		   camImage,                                   //where to draw the box
                   new Point(x, y),                           //bottom left
                   new Point(x+width,y+height), //top right
                   new Scalar(64, 255, 0) ,//RGB colour
                   2
            );
         // Drawing a line
           Imgproc.line (
        	  camImage,                    //Matrix obj of the image
              new Point(x+width/2,y-20),        //p1
              new Point(x+width/2, y),       //p2
              new Scalar(64, 255, 0),     //Scalar object for color
              2                         //Thickness of the line
           );
       }
       else if(size==3 &&!isTriangleFound){
    	   isTriangleFound=true;
    	   //controlRobot(camImage,"STOP TO ROTATE");
       }else if(!isTriangleFound){
    	 //rectangle main
           Imgproc.rectangle(
        		   camImage,                                   //where to draw the box
                   new Point(x, y),                           //bottom left
                   new Point(x+width,y+height), //top right
                   new Scalar(64, 255, 0) ,//RGB colour
                   2
            );
         // Drawing a line
           Imgproc.line (
        	  camImage,                    //Matrix obj of the image
              new Point(x+width/2,y-20),        //p1
              new Point(x+width/2, y),       //p2
              new Scalar(64, 255, 0),     //Scalar object for color
              2                         //Thickness of the line
           );
       }
       if(isTriangleFound) {
    	   processTriangle(camImage);
       }
       
   }
   
   private static void processTriangle(Mat camImage) {
	   int xx=2*camImage.cols()/8; //original = 10
	   int yy=camImage.rows()/3+100; // must be 2*camImage.rows()/3
	   int width2=4*camImage.cols()/8; //original =camImage.cols()-20
	// must be adjusted with camera position ********
	   int height2= 200; //camImage.rows()/8; //12 must be 
       Mat roi2 = new Mat(camImage, new Rect(xx, yy, width2, height2));
       
	   Mat mono2=new Mat();
	   Mat blur2=new Mat();
	   Mat thresh2=new Mat();
	   Mat erodeImg2=new Mat();
	   Mat erode2=new Mat();
	   Mat dilateImg2=new Mat();
	   Mat dilate2=new Mat();
	   Mat notused2=new Mat();
	   Imgproc.cvtColor(roi2, mono2, Imgproc.COLOR_BGR2GRAY);
	   Imgproc.GaussianBlur(mono2, blur2, new Size(9, 9), 2, 2);
	   Imgproc.threshold(blur2, thresh2, 0, 255, Imgproc.THRESH_BINARY|Imgproc.THRESH_OTSU);
	   Imgproc.erode(thresh2, erodeImg2, erode2);
	   Imgproc.dilate(erodeImg2, dilateImg2, dilate2);
	   List<MatOfPoint> contours2 = new ArrayList<MatOfPoint>();
	   Imgproc.findContours(dilateImg2, contours2, notused2, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE); 
	   
	   //Imgproc.drawContours(roi2, contours2, -1, new Scalar(64, 255, 0));
	   System.out.println("no of contours large = " +contours2.size());
	   if(contours2.size()==3) {
		   int maxX=0;
		   int maxI=0;
		   int i =0;
		   for(MatOfPoint cont : contours2)
	       {
	           Moments mu = Imgproc.moments(cont, false);
	           if (mu.get_m00() > 100.0)
	           {
	        	   Rect rr = Imgproc.boundingRect(cont);
	               if(i==0) {
	            	   maxX=rr.x;
	               }else {
	            	   maxI=rr.x>maxX ? i :maxI; 
	               }
	               
	           }
	           i++;
	       }
		   //new algorithm - CIRCLE ALGO 26-09-2018 to get direction of arrow
		   //System.out.println("1 contour detected");
		   Point [] pointarrray = contours2.get(maxI).toArray();
	       MatOfPoint2f  NewMtx = new MatOfPoint2f(pointarrray);
	       //MatOfPoint2f pointsMat = new MatOfPoint2f(points.toArray(new Point[]{}));
	       Point center = new Point();
	       float radius [] = new float[2];
	       Imgproc.minEnclosingCircle(NewMtx,center,radius);
	       int rad =(int)radius[0];
	       //System.out.println("Centre = "+center.x+ ", "+center.y+" radius = "+rad);
	       List<Point> corners = new ArrayList<Point>();
	       for(Point point : pointarrray)
	       {
	      	 double leftV = (point.y-center.y)*(point.y-center.y)+(point.x-center.x)*(point.x-center.x);
	      	 if(leftV > rad*rad) {
	      		 corners.add(point);
	      		 //System.out.println("hi"+point.x+" "+point.y); 
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
	    		    roi2,                 //Matrix obj of the image
		            directed,    //Center of the circle
		            5,                    //Radius
		            new Scalar(0, 0, 255),  //Scalar object for color
		            4                     //Thickness of the circle
		    );
	       int angle = (int)((Math.atan2(center.y-directed.y,directed.x-center.x))*(180/Math.PI));
	       System.out.println("angle "+angle);
	       int correctedAngle= 90-angle;
	       String turnningSignal = "";
	       if(correctedAngle <= 5 &&  correctedAngle >= -5){
	    	   turnningSignal="GO FORWARD";
	    	   isTriangleFound=false;
	       }else if(correctedAngle <= 90 &&  correctedAngle > 5) {
	    	   turnningSignal = "TURN RIGHT ANGLE = "+correctedAngle;
	       }else if(correctedAngle < -5 &&  correctedAngle >= -90) {
	    	   turnningSignal = "TURN LEFT ANGLE = "+-1*correctedAngle;
	       }else if(correctedAngle > 90 &&  correctedAngle <= 180) {
	    	   turnningSignal = "TURN RIGHT ANGLE = "+correctedAngle;
	       }else if(correctedAngle > 180 &&  correctedAngle < 270) {
	    	   turnningSignal = "TURN LEFT ANGLE = "+(360-correctedAngle);
	       }
	     //new algorithm over - CIRCLE ALGO 26-09-2018 to get direction of arrow
	       
	       controlRobot(camImage,turnningSignal);
	   }
	   Imgproc.rectangle(
    		   camImage,                                   //where to draw the box
               new Point(xx, yy),                           //bottom left
               new Point(xx+width2,yy+height2), //top right
               new Scalar(192, 128, 0) ,//RGB color
               2
        );
	   
   }
   
   private static void controlRobot(Mat im, String signal) {
	   // Adding Text
	      Imgproc.putText (
	         im,                          // Matrix obj of the image
	         signal,          // Text to be added
	         new Point(10, 50),               // point
	         Core.FONT_HERSHEY_SIMPLEX ,      // front face
	         1,                               // front scale
	         new Scalar(255, 255, 255),             // Scalar object for color
	         4                                // Thickness
	      );
   }
   
   
   /**
    * Converts/writes a Mat into a BufferedImage.
    *
    * @param matBGR Mat of type CV_8UC3 or CV_8UC1
    * @return BufferedImage of type TYPE_3BYTE_BGR or TYPE_BYTE_GRAY
    */
   public static BufferedImage matrixToBuffer(Mat matBGR) {
      int type = BufferedImage.TYPE_BYTE_GRAY;
      if (matBGR.channels() > 1) {
         type = BufferedImage.TYPE_3BYTE_BGR;
      }
      int width = matBGR.width(), height = matBGR.height(), channels = matBGR.channels();
      byte[] sourcePixels = new byte[width * height * channels];
      matBGR.get(0, 0, sourcePixels);

      // Create new image and get reference to backing data
      image = new BufferedImage(width, height, type);
      final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
      System.arraycopy(sourcePixels, 0, targetPixels, 0, sourcePixels.length);
      return image;
   }

  

   @Override
   public void paintComponent(Graphics g) {
      super.paintComponent(g);
      if (image == null) {
         return;
      }

      g.drawImage(image, 10, 10, image.getWidth(), image.getHeight(), null);
      g.setColor(Color.WHITE);
   }
}