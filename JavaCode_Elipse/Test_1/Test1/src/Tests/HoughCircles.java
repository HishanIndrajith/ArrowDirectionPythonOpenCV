   package Tests;

  import java.awt.image.BufferedImage;

  import java.io.ByteArrayInputStream;
  import java.io.IOException;
  import java.io.InputStream;

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
  import org.opencv.core.Point;
  import org.opencv.core.Scalar;
  import org.opencv.imgcodecs.Imgcodecs;
  import org.opencv.imgproc.Imgproc;

  public class HoughCircles extends Application {
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
        
        String file ="D://Bsc(Computer_Engineering)//RoboGames//cirg.jpg";
        Mat image = Imgcodecs.imread(file);
        Mat gray = new Mat(); 
         
        // blur 
      //  Imgproc.medianBlur(gray, gray, 5); 
      //  Imgproc.GaussianBlur(gray, gray, new Size(3, 3), 100); 
         
        // convert to grayscale 
        //Imgproc.cvtColor(gray, gray, Imgproc.COLOR_BGR2GRAY); 
         
        // do hough circles 
        Mat circles = new Mat(); 
//        int minRadius = 50;
//        int maxRadius = 100; 
        
     // parameters
        int iCannyUpperThreshold = 100;
        int iMinRadius = 20;
        int iMaxRadius = 400;
        int iAccumulator = 100;
        
        Imgproc.cvtColor(image, gray, Imgproc.COLOR_RGB2GRAY, 4);
        Imgproc.HoughCircles(gray, circles, Imgproc.CV_HOUGH_GRADIENT, 
                 1.0, gray.rows() / 8, iCannyUpperThreshold, iAccumulator, 
                 iMinRadius, iMaxRadius);

        //Imgproc.HoughCircles(gray, circles, Imgproc.CV_HOUGH_GRADIENT, 1, 200); 
        System.out.println(circles.dump()); 
        for(int i = 0; i < circles.cols(); i++) { 
            double[] circle = circles.get(0, i); 
            //g.drawOval((int)circle[0] - (int)circle[2], (int)circle[1] - (int)circle[2], (int)circle[2] * 2, (int)circle[2] * 2); 
            Imgproc.circle (
            		image,                 //Matrix obj of the image
        	         new Point((int)circle[0], (int)circle[1]),    //Center of the circle
        	         (int)circle[2],                    //Radius
        	         new Scalar(0, 0, 255),  //Scalar object for color
        	         2                   //Thickness of the circle
        	      );
        }
        
     // Drawing a line
//        Imgproc.line (
//      	 image,                    //Matrix obj of the image
//           new Point(image.width()/2, 0),        //p1
//           new Point(image.width()/2, image.height()),       //p2
//           new Scalar(0, 0, 255),     //Scalar object for color
//           5                          //Thickness of the line
//        );
//        
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
     public static void main(String [] args) {
    	 
     }
  
}
