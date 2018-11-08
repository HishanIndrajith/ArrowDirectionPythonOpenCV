from picamera.array import PiRGBArray
from picamera import PiCamera
import time
import cv2
import sys
import imutils
import numpy as np

#functions start
def control_robot(situation,signal):
    print(situation + '\n '+ signal)
    font = cv2.FONT_HERSHEY_SIMPLEX
    cv2.putText(image,situation + " - "+ signal,(10,20), font, 0.3,(0,255,255),1)
    
def processTriangle(image):
    # arrow angle detection algorithm
    gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
    #GaussianBlur()
    blur = cv2.GaussianBlur(gray,(9, 9), 2, 2)
    ret,thresh = cv2.threshold(gray,127,255,cv2.THRESH_BINARY | cv2.THRESH_OTSU)
    kernel = np.ones((5,5),np.uint8)
    erosion = cv2.erode(thresh,kernel,iterations = 1)
    dilation = cv2.dilate(erosion,kernel,iterations = 1)
    contours, hierarchy = cv2.findContours(dilation,cv2.RETR_LIST,cv2.CHAIN_APPROX_SIMPLE)
    #print("large " + str(len(contours)))
    if len(contours) == 3 :
        #print("Found 3 contours")
        maxX=0;
        maxI=0;
        i =0;

        for cont in contours:
            mu = cv2.moments(cont)
            if mu['m00'] > 100.0 :
                #cv2.drawContours(image, [cont], 0, (0,255,0), 3)
                x,y,w,h = cv2.boundingRect(cont)
                if i==0  :
                    maxX=x;
                else :
                    maxI = i if x>maxX else maxI

            i=i+1;
        #new algorithm - CIRCLE ALGO 26-09-2018 to get direction of arrow
	#System.out.println("1 contour detected");
	pointarrray = contours[maxI];
	(x,y),radius = cv2.minEnclosingCircle(pointarrray)
	cv2.drawContours(image, pointarrray, -1, (255,255,0), 2)
        center = (int(x),int(y))
        rad = int(radius)
        rad=rad-5;
        
	cv2.circle(image,center,rad,(255, 128, 255),2)
        
        corners = []
        for p in pointarrray:
            point = (p[0][0],p[0][1])
            #print pt
            #cv2.circle(im,pt,5,(200,0,0),2)
            leftV = (point[1]-center[1])*(point[1]-center[1])+(point[0]-center[0])*(point[0]-center[0]);
            if leftV > rad*rad :
                corners.append(point)
                #System.out.println("hi"+point.x+" "+point.y);
        #print("corners  " + str(len(corners)))
        p1x = 0
        p1y = 0
	pn1=0
	p2x = 0
        p2y = 0
	pn2=0
	p3x = 0
        p3y = 0
	pn3=0
	if len(corners)>3:
            p1x=corners[0][0];
            p1y=corners[0][1];
            pn1=pn1+1
            offset=10;
            for corner in corners :
                cornerX=corner[0]
                cornerY=corner[1]
                if (cornerX < p1x/pn1+offset) and (cornerX > p1x/pn1-offset) and (cornerY < p1y/pn1+offset) and (cornerY > p1y/pn1-offset) :
                    p1x=p1x+cornerX
                    p1y=p1y+cornerY
                    pn1=pn1+1
                else :
                    if pn2==0 :
                        p2x=cornerX
                        p2y=cornerY
                        pn2=pn2+1
                    elif (cornerX < p2x/pn2+offset) and (cornerX > p2x/pn2-offset) and (cornerY < p2y/pn2+offset) and (cornerY > p2y/pn2-offset) :
                        p2x=p2x+cornerX
                        p2y=p2y+cornerY
                        pn2=pn2+1
                    else :
                        if pn3==0  :
                            p3x=cornerX;
                            p3y=cornerY;
                            pn3=pn3+1;
                        elif (cornerX < p3x/pn3+offset) and (cornerX > p3x/pn3-offset) and (cornerY < p3y/pn3+offset) and (cornerY > p3y/pn3-offset) :
                            p3x=p3x+cornerX;
                            p3y=p3y+cornerY;
                            pn3=pn3+1;
            if pn1 > 0 and pn2 > 0 and pn3 > 0 : 
                p1x/=pn1;
                p2x/=pn2;
                p3x/=pn3;
                p1y/=pn1;
                p2y/=pn2;
                p3y/=pn3;

                #print("corner point1 = " + str(p1x)+","+str(p1y))
                #print("corner point2 = " + str(p2x)+","+str(p2y))
                #print("corner point3 = " + str(p3x)+","+str(p3y))
                cv2.circle(image,(p1x,p1y),4,(0, 255, 255),2)
                cv2.circle(image,(p2x,p2y),4,(0, 255, 255),2)
                cv2.circle(image,(p3x,p3y),4,(0, 255, 255),2)

                length1_2 = (p1x - p2x)*(p1x - p2x)+(p1y - p2y)*(p1y - p2y);
                length2_3 = (p2x - p3x)*(p2x - p3x)+(p2y - p3y)*(p2y - p3y);
                length3_1 = (p3x - p1x)*(p3x - p1x)+(p3y - p1y)*(p3y - p1y);

                #print("length1_2 = " + str(length1_2))
                #print("length2_3 = " + str(length2_3))
                #print("length3_1 = " + str(length3_1))
                
                directedX=0
                directedY=0
                if length1_2 <=length2_3 :
                    if length3_1<length1_2 : 
                        directedX=p2x
                        directedY=p2y
                    else :
                        directedX=p3x
                        directedY=p3y
                else :
                    if length3_1<length2_3 :
                        directedX=p2x
                        directedY=p2y
                    else :
                        directedX=p1x
                        directedY=p1y
                cv2.circle(image,(directedX,directedY),5,(0, 0, 255),2)
                angle = 0
                
                numer = y-directedY #numerator
                denom = x-directedX #denominator
                if denom != 0 :
                    angle = np.absolute(np.degrees(np.arctan(numer/denom)))
                else :
                    angle = 90
                #print("angle "+str(angle));
                turnningSignal = "";
                if angle < 8 and denom < 0:
                    turnningSignal = "TURN RIGHT ANGLE = 90"
                elif angle < 8 and denom > 0:
                    turnningSignal = "TURN LEFT ANGLE = 90"
                elif angle > 82 and numer > 0:
                    turnningSignal = "GO FORWARD"
                    isTriangleFound = False;
                elif angle > 82 and numer < 0:
                    turnningSignal = "TURN DIRECTLY BACK"
                elif numer > 0 and  denom < 0:
                    turnningSignal = "TURN RIGHT ANGLE = "+ str(90-angle)
                elif numer > 0 and  denom > 0:
                    turnningSignal = "TURN LEFT ANGLE = "+ str(90-angle)
                elif numer < 0 and  denom < 0:
                    turnningSignal = "TURN RIGHT ANGLE = "+ str(90+angle)
                elif numer < 0 and  denom > 0:
                    turnningSignal = "TURN LEFT ANGLE = "+ str(90+angle)
                control_robot("JUNCTION MODE",turnningSignal)
	     #new algorithm over - CIRCLE ALGO 26-09-2018 to get direction of arrow
    
#functions end

# initialize the camera and grab a reference to the raw camera capture
camera = PiCamera()
camera.resolution = (304, 304)
camera.framerate = 40

rawCapture = PiRGBArray(camera, size=(304, 304))
isTriangleFound = False;
# allow the camera to warmup
time.sleep(0.1)

# capture frames from the camera
for frame in camera.capture_continuous(rawCapture, format="bgr", use_video_port=True):
	# grab the raw NumPy array representing the image, then initialize the timestamp
	# and occupied/unoccupied text
    image = frame.array
    crop_line = image[130:170, 0:304]
    gray = cv2.cvtColor(crop_line, cv2.COLOR_BGR2GRAY)
    #GaussianBlur()
    blur = cv2.GaussianBlur(gray,(9, 9), 2, 2)
    ret,thresh = cv2.threshold(gray,127,255,cv2.THRESH_BINARY | cv2.THRESH_OTSU)
    kernel = np.ones((5,5),np.uint8)
    erosion = cv2.erode(thresh,kernel,iterations = 1)
    dilation = cv2.dilate(erosion,kernel,iterations = 1)
    contours, hierarchy = cv2.findContours(dilation,cv2.RETR_LIST,cv2.CHAIN_APPROX_SIMPLE)
    #print("small " + str(len(contours)))
    
    if len(contours) == 1:
        cnt = contours[0]
        M = cv2.moments(cnt)
        if M['m00'] > 100:
            x,y,w,h = cv2.boundingRect(cnt)
            cv2.rectangle(image,(x,130+y),(x+w,130+y+h),(0,255,255),1)
            midOfWhiteSqr = (x + w/2);
            midOfGreenSqr = 152;
            gap = midOfWhiteSqr-midOfGreenSqr;
            #line width = 100 according to camera , ground gap
            if w < 130:
                #prevent taking beginning of circle as line
                control_robot("LINE MODE","Gap= " + str(gap))
                #control_robot("Gap= " + str(gap));
                isTriangleFound = False;
            else :
                control_robot("LINE MODE","0");
        # rectangle main
        cv2.rectangle(image,(0,130),(300,170),(0,255,0),1)
        #centre line
        cv2.line(image,(152,130),(152,170),(0,255,0),1)
    elif len(contours) == 2 and not isTriangleFound:
        control_robot("LINE MODE","0");
        # rectangle main
        cv2.rectangle(image,(0,130),(300,170),(0,255,0),1)
        #centre line
        cv2.line(image,(152,130),(152,170),(0,255,0),1)
    elif len(contours) == 3 and not isTriangleFound:
      isTriangleFound = True;
    elif not isTriangleFound:
        # rectangle main
        cv2.rectangle(image,(0,130),(300,170),(0,255,0),1)
        #centre line
        cv2.line(image,(152,130),(152,170),(0,255,0),1)
    if isTriangleFound:
        #print("GOT JUNCTION")
        processTriangle(image);
        
    # show the frame
    cv2.imshow("Frame", image)
    key = cv2.waitKey(1) & 0xFF
 
	# clear the stream in preparation for the next frame
    rawCapture.truncate(0)
    
	# if the `q` key was pressed, break from the loop
    if key == ord("q"):
        break