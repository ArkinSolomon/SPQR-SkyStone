package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

/**
 * Custom Linear OpMode class with extra functions.
 *
 * @author Arkin Solomon
 */
public abstract class SPQRLinearOpMode extends LinearOpMode {

    //Variables
    public final double speed = 0.75;
    public final double turnSpeed = 1;
    public final long ninetyDegreeRightTime = 990;
    public final long ninetyDegreeLeftTime = 970;

    //Intialize hardware
    public HardwareSPQR robot = new HardwareSPQR();
    public void hardwareInit(){
        this.robot.init(hardwareMap);
    }

    public void moveArm(int level, double speed){
        this.robot.moveArm(level, speed);
        }
    public void grabBlock (){
        this.robot.grabBlock();
    }

    public void releaseBlock (){
        this.robot.releaseBlock();
    }

    //Drive for a certain amount of time
    public void driveForTime(double speed, long milliseconds){
        this.robot.setPowers(speed);
        this.sleep(milliseconds);
        this.robot.setPowers(0);
    }

    //Turn for a time of 90 degrees?
    public void turnForTime90(Dir direction, double speed){
        if (direction == Dir.LEFT) {
            this.robot.tank(speed, -speed);
            this.sleep(ninetyDegreeRightTime);
        } else {
            this.robot.tank(-speed, speed);
            this.sleep(ninetyDegreeLeftTime);
        }
        this.robot.stopMoving();
    }

    //Turn for a set time
    public void turnForTime(Dir direction, double speed, long milliseconds){
        if (direction == Dir.LEFT) {
            this.robot.tank(speed, -speed);
            this.sleep(milliseconds);
        } else {
            this.robot.tank(-speed, speed);
            this.sleep(milliseconds);
        }
        this.robot.stopMoving();
    }

    //IDK? Owen things?
    public long justSpeed(long miliRatio, double speed){
        return ((long) Math.abs(miliRatio / speed));
    }

    //Stops the robot over the tape
    public void stopAtTape(int[] tapeColor, int change){
        this.robot.setPowers(.6);
        boolean isOnLine = false;
        while (!isOnLine && this.opModeIsActive()) {
            int[] r = this.plusOrMinus(this.robot.lineParkSensor.red(), change);
            int[] g = this.plusOrMinus(this.robot.lineParkSensor.green(), change);
            int[] b = this.plusOrMinus(this.robot.lineParkSensor.blue(), change);
            isOnLine = (((tapeColor[0] > r[0]) && (tapeColor[0] < r[1])) && ((tapeColor[1] > g[0]) && (tapeColor[1] < g[1])) && ((tapeColor[2] > b[0]) && (tapeColor[2] < b[1])));
        }
        this.robot.lineParkSensor.enableLed(false);
        this.robot. backward();
        this.sleep(100);
        this.robot.stopMoving();
    }

    //Returns an array plus or minus a change given of a value given
    private int[] plusOrMinus(int value, int change){
        return new int[] {value - change, value + change};
    }
}