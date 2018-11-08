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

import static org.opencv.objdetect.Objdetect.CASCADE_SCALE_IMAGE;

public class JunctionProcessing extends JPanel {

   private static BufferedImage image; // The actual displayed image on screen
   private static CascadeClassifier faceCascade;
   private static CascadeClassifier eyesCascade;
   private static int absoluteFaceSize;
   static int oldSignal=-1;

   
   public static void main(String args[]) {
      System.loadLibrary(Core.NATIVE_LIBRARY_NAME); // Import openCV
      Webcam panel = new Webcam(); // Initialize itself

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
               //Imgproc.cvtColor(thisFrame, thisFrame, Imgproc.COLOR_BGR2GRAY); // Convert the diff to gray

               // Set up new Mats for diffing them later, manually set the amount of channels to avoid an openCV error
               Mat pastFrame = new Mat();
               Mat diff = new Mat();

               
               
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
       Imgproc.cvtColor(roi, mono, Imgproc.COLOR_BGR2GRAY);
       Imgproc.GaussianBlur(mono, blur, new Size(9, 9), 2, 2);
       Imgproc.threshold(blur, thresh, 0, 255, Imgproc.THRESH_BINARY_INV|Imgproc.THRESH_OTSU);
       Imgproc.erode(thresh, erodeImg, erode);
       Imgproc.dilate(erodeImg, dilateImg, dilate);
       List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
       Imgproc.findContours(dilateImg, contours, notused, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
       int triangleContourId = 0;
       int size = contours.size();
       int junctionDirection=-1; //0 - left, 1 - right
       if(size==3){
    	   int [] Xvalues = new int [size];
    	   int maxWidthIndex=0;
    	   int maxWidth=Imgproc.boundingRect(contours.get(maxWidthIndex)).width;
           int i=0;
    	   for(MatOfPoint cont : contours)
           {
    		   Rect r = Imgproc.boundingRect(cont);
    		   Xvalues[i] = r.x;
    		   if(r.width > maxWidth) {
    			   maxWidthIndex=i;
    			   maxWidth=r.width;
    		   }
    		   i++;
           }
    	   if(Xvalues[0]>Xvalues[1] && Xvalues[0]>Xvalues[2]) {
    		   triangleContourId = Xvalues[1]>Xvalues[2]?1:2;
    		   junctionDirection = maxWidthIndex == 0 ? 1:0;
    		   
    	   }else if (Xvalues[1]>Xvalues[0] && Xvalues[1]>Xvalues[2]) {
    		   triangleContourId = Xvalues[0]>Xvalues[2]?0:2;
    		   junctionDirection = maxWidthIndex == 1 ? 1:0;
    	   }else if(Xvalues[2]>Xvalues[1] && Xvalues[2]>Xvalues[0]) {
    		   triangleContourId = Xvalues[0]>Xvalues[1]?0:1;
    		   junctionDirection = maxWidthIndex == 2 ? 1:0;
    	   }
       }
       int currentContourId=0;
       for(MatOfPoint cont : contours)
       {
           Moments mu = Imgproc.moments(cont, false);
           if (mu.get_m00() > 100.0)
           {
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
               if(size==1 & r.width<130) { //must be adjusted with camera altitude and zoom factor
            	   if(gap>50) {
            		   controlRobot(5); //TURN SLIGHTLY RIGHT
                   }else if(gap<-50) {
                	   controlRobot(4); //TURN SLIGHTLY LEFT
                   }else {
                	   controlRobot(0);//GO
                	   //System.out.println("width = "+r.width);
                   }
            	   
               }else if(size==3 && currentContourId==triangleContourId) {
            	   //triangle processing
            	   
            	   if(r.y>2 && r.y + r.height<height-2) { //not touch with edges
            		   //controlRobot(3); //STOP
            		   if(r.height>r.width) {
            			   controlRobot(0); //GO
            		   }else {
            			  if(junctionDirection==1){
            				  controlRobot(7); //TURN 90 RIGHT
            			  }else{
            				  controlRobot(6); //TURN 90 LEFT
            			  }
            			   
            		   }
            	   }else {
            		   controlRobot(2);//GO VERY SLOWLY
            	   }
               }else if(size != 3){
            	   controlRobot(1); //GO SLOWLY
               }
            }
           currentContourId++;
       }
   }
   
   private static void controlRobot(int signal) {
	   /*
	    SIGNALS and MEANINGS
	    GO = 0
	    GO SLOWLY = 1
	    GO VERY SLOWLY = 2
	    STOP = 3
	    TURN SLIGHTLY LEFT = 4
	    TURN SLIGHTLY RIGHT = 5
	    TURN 90 LEFT = 6
	    TURN 90 RIGHT = 7
	    */
	   //up to now print to user
	   if(oldSignal != signal) {
		   switch(signal) {
		   	case 0: System.out.println("GO");oldSignal=0;break;
		   	case 1: System.out.println("GO SLOWLY");oldSignal=1;break;
		   	case 2: System.out.println("GO VERY SLOWLY");oldSignal=2;break;
		   	case 3: System.out.println("STOP");oldSignal=3;break;
		   	case 4: System.out.println("TURN SLIGHTLY LEFT");oldSignal=4;break;
		   	case 5: System.out.println("TURN SLIGHTLY RIGHT");oldSignal=5;break;
		   	case 6: System.out.println("TURN 90 LEFT");oldSignal=6;break;
		   	case 7: System.out.println("TURN 90 RIGHT");oldSignal=7;break;
		   }
	   }
   }
   
   
   private static float detectCircles(Mat camImage)
   {
	   Mat mono=new Mat();
	   Mat blur=new Mat();
	   Mat erodeImg=new Mat();
	   Mat erode=new Mat();
	   Mat dilateImg=new Mat();
	   Mat dilate=new Mat();
	   Mat bright = new Mat();
	   
	   int x=camImage.cols()/3; //original = 10
	   int y=camImage.rows()/6-20; // must be 2*camImage.rows()/3
	   int width=camImage.cols()/3; //original =camImage.cols()-20
	   int height=camImage.rows()/2; //12 must be
       Mat roi = new Mat(camImage, new Rect(x, y, width, height));
       
       Imgproc.cvtColor(camImage, mono, Imgproc.COLOR_BGR2GRAY);
       Imgproc.GaussianBlur(mono, blur, new Size(9, 9), 2, 2);
       Imgproc.erode(blur, erodeImg, erode);
       Imgproc.dilate(erodeImg, dilateImg, dilate);
       
     //rectangle main
       Imgproc.rectangle(
    		   camImage,                                   //where to draw the box
               new Point(x, y),                           //bottom left
               new Point(x+width,y+height), //top right
               new Scalar(255, 0, 255) ,//RGB colour
               2
        );
        
       // do hough circles 
       Mat circles = new Mat(); 
       
    // parameters
       int iCannyUpperThreshold = 100;
       int iMinRadius = 20;
       int iMaxRadius = 400;
       int iAccumulator = 100;
       
       Imgproc.HoughCircles(dilateImg, circles, Imgproc.CV_HOUGH_GRADIENT, 
                1.0, dilateImg.rows() / 8, iCannyUpperThreshold, iAccumulator, 
                iMinRadius, iMaxRadius);

       System.out.println(circles.dump()); 
       for(int i = 0; i < circles.cols(); i++) { 
           double[] circle = circles.get(0, i); 
           Imgproc.circle (           
        		 camImage,                 //Matrix obj of the image
       	         new Point((int)circle[0], (int)circle[1]),    //Center of the circle
       	         (int)circle[2],                    //Radius
       	         new Scalar(0, 0, 255),  //Scalar object for color
       	         2                   //Thickness of the circle
       	      );
       }

       //image = matrixToBuffer(camImage); // Update the display image
       image = matrixToBuffer(camImage);
       return 1;
   }

   private static int threshold(int gray) {
      return gray < 10 ? 255 : 0;
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

   /**
    * Detect and return a face
    *
    * @param frame The total webcam view. Must be in grayscale.
    * @return A Mat that contains only the face
    */
   public static Mat getFace(Mat frame, Mat drawFrame) {

      MatOfRect faces = new MatOfRect();

      // compute minimum face size (20% of the frame height)
      if (absoluteFaceSize == 0) {
         int height = frame.rows();
         if (Math.round(height * 0.2f) > 0) {
            absoluteFaceSize = Math.round(height * 0.2f);
         }
      }

      // detect faces
      faceCascade.detectMultiScale(frame, faces, 1.1, 3, 0 | CASCADE_SCALE_IMAGE,
            new Size(absoluteFaceSize, absoluteFaceSize), new Size());

      // each rectangle in faces is a face
      Rect[] facesArray = faces.toArray();
      for (int i = 0; i < facesArray.length; i++) {
         Point center = new Point(facesArray[i].x + (facesArray[i].width / 2), facesArray[i].y + facesArray[i].height / 2);

         Mat face = new Mat(frame, facesArray[i]);

         MatOfRect eyes = new MatOfRect();

         eyesCascade.detectMultiScale(face, eyes, 1.1, 2,
               0 | CASCADE_SCALE_IMAGE, new Size(30, 30), new Size());

         if (eyes.size().width > 0 && eyes.size().height > 0) {
            Imgproc.ellipse(drawFrame, center, new Size(facesArray[i].width / 2, facesArray[i].height / 2),
                  0, 0, 360, new Scalar(0, 0, 0), 4, 8, 0);
         }
      }
      return drawFrame;
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