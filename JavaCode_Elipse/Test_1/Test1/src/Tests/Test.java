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

public class Test extends JPanel {





   private static BufferedImage image; // The actual displayed image on screen
   private static CascadeClassifier faceCascade;
   private static CascadeClassifier eyesCascade;
   private static int absoluteFaceSize;
   static int oldSignal=-1;

   public Test() {
      super();
      faceCascade = new CascadeClassifier("D:\\Bsc(Computer_Engineering)\\RoboGames\\OpenCV\\opencv\\build\\etc\\haarcascades\\haarcascade_frontalface_default.xml");
      eyesCascade = new CascadeClassifier("D:\\Bsc(Computer_Engineering)\\RoboGames\\OpenCV\\opencv\\build\\etc\\haarcascades\\haarcascade_eye.xml");
      absoluteFaceSize = 0;
   }

   public static void main(String args[]) {
      System.loadLibrary(Core.NATIVE_LIBRARY_NAME); // Import openCV
      Webcam panel = new Webcam(); // Initialize itself

      // Initialize JPanel
      JFrame frame = new JFrame("Webcam");
      frame.setSize(200, 200);
      frame.setContentPane(panel);
      frame.setVisible(true);

      VideoCapture camera = new VideoCapture(1); // The camera

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
                     //getMidPoint(1,thisFrame);
                     //detectCircles(thisFrame);
                     //image = matrixToBuffer(thisFrame); // Update the display image
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
}
   