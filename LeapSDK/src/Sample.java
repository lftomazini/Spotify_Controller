
/**
 * ****************************************************************************\
 * Copyright (C) 2012-2013 Leap Motion, Inc. All rights reserved. * Leap Motion
 * proprietary and confidential. Not for distribution. * Use subject to the
 * terms of the Leap Motion SDK Agreement available at *
 * https://developer.leapmotion.com/sdk_agreement, or another agreement *
 * between Leap Motion and you, your company or other organization. *
 * \*****************************************************************************
 */
import com.leapmotion.leap.CircleGesture;
import com.leapmotion.leap.Controller;
import com.leapmotion.leap.Frame;
import com.leapmotion.leap.Gesture;
import com.leapmotion.leap.Gesture.State;
import com.leapmotion.leap.GestureList;
import com.leapmotion.leap.KeyTapGesture;
import com.leapmotion.leap.Listener;
import com.leapmotion.leap.ScreenTapGesture;
import com.leapmotion.leap.SwipeGesture;
import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

class SampleListener extends Listener {
    public void onInit(Controller controller) {
        System.out.println("Initialized");
    }

    public void onConnect(Controller controller) {
        System.out.println("Connected");
        controller.enableGesture(Gesture.Type.TYPE_SWIPE);
        controller.enableGesture(Gesture.Type.TYPE_CIRCLE);
        controller.enableGesture(Gesture.Type.TYPE_SCREEN_TAP);
        controller.enableGesture(Gesture.Type.TYPE_KEY_TAP);
        controller.setPolicy(Controller.PolicyFlag.POLICY_BACKGROUND_FRAMES);
    }

    public void onDisconnect(Controller controller) {
        //Note: not dispatched when running in a debugger.
        System.out.println("Disconnected");
    }

    public void onExit(Controller controller) {
        System.out.println("Exited");
    }

    public void onFrame(Controller controller) {
        // Get the most recent frame and report some basic information
        Frame frame = controller.frame();

        GestureList gestures = frame.gestures();
        for (int i = 0; i < gestures.count(); i++) {
            try {
                Gesture gesture = gestures.get(i);

                Robot robot = new Robot();
                Thread.sleep(1000);

                switch (gesture.type()) {
                    case TYPE_CIRCLE:
                        CircleGesture circle = new CircleGesture(gesture);

                        // Calculate clock direction using the angle between circle normal and pointable
                        String clockwiseness;
                        if (circle.pointable().direction().angleTo(
                                circle.normal()) <= Math.PI / 2) {
                            // Clockwise if angle is less than 90 degrees
                            clockwiseness = "clockwise";
                            System.out.println("clockwise");
                            robot.delay(40);
                            robot.keyPress(KeyEvent.VK_META);
                            robot.keyPress(KeyEvent.VK_UP);
                            robot.keyRelease(KeyEvent.VK_UP);
                            robot.keyRelease(KeyEvent.VK_META);
                        } else {
                            clockwiseness = "counterclockwise";
                            System.out.println("counter clockwise");
                            robot.delay(40);
                            robot.keyPress(KeyEvent.VK_META);
                            robot.keyPress(KeyEvent.VK_DOWN);
                            robot.keyRelease(KeyEvent.VK_DOWN);
                            robot.keyRelease(KeyEvent.VK_META);
                        }

                        // Calculate angle swept since last frame
                        double sweptAngle = 0;
                        if (circle.state() != State.STATE_START) {
                            CircleGesture previousUpdate = new CircleGesture(
                                    controller.frame(1).gesture(circle.id()));
                            sweptAngle = (circle.progress() - previousUpdate.progress()) * 2 * Math.PI;
                        }
                        break;
                    case TYPE_SWIPE:
                        SwipeGesture swipe = new SwipeGesture(gesture);
                        System.out.println("Swipe");
                        robot.keyPress(KeyEvent.VK_META);
                        robot.keyPress(KeyEvent.VK_RIGHT);
                        robot.keyRelease(KeyEvent.VK_RIGHT);
                        robot.keyRelease(KeyEvent.VK_META);
                        break;
                    case TYPE_SCREEN_TAP:
                        ScreenTapGesture screenTap = new ScreenTapGesture(
                                gesture);
//                        System.out.println("Screen tap");
                        break;
                    case TYPE_KEY_TAP:
                        KeyTapGesture keyTap = new KeyTapGesture(gesture);
//                        System.out.println("Key tap");
                        break;
                    default:
                        break;
                }
            } catch (AWTException ex) {
                Logger.getLogger(SampleListener.class.getName()).log(
                        Level.SEVERE,
                        null, ex);
            } catch (InterruptedException ex) {
                Logger.getLogger(SampleListener.class.getName()).log(
                        Level.SEVERE,
                        null, ex);
            }
        }

        if (!frame.hands().isEmpty() || !gestures.isEmpty()) {
        }
    }
}

class Sample {
    public static void main(String[] args) {
        // Create a sample listener and controller
        SampleListener listener = new SampleListener();
        Controller controller = new Controller();

        // Have the sample listener receive events from the controller
        controller.addListener(listener);

        // Keep this process running until Enter is pressed
        System.out.println("Press Enter to quit...");
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Remove the sample listener when done
        controller.removeListener(listener);
    }
}
