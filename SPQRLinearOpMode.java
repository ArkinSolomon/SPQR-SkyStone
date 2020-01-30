package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;

/**
 * Custom Linear OpMode class with extra functions.
 *
 * @author Arkin Solomon
 * @author Owen Peterson
 */
public abstract class SPQRLinearOpMode extends LinearOpMode {

    //Variables
    public final double speed = 0.75;
    public final double ppr = 280;
    public final double degppr = 21.35;
    public final double powerScalar = 1.25;

    //The switch on the robot that dictates what color it is. (red or blue)
    private boolean teamSwitch;
    private Color teamColor;

    //The switch on the robot that determines the starting posistion (front or back)
    private boolean positionSwitch;
    private Position position;

    //The switch on the robot that determines which lane it will primarily travel on (inside, outside)
    private boolean laneSwitch;
    private Lane lane;

    //The switch on the robot that determines whether to try and pull the foundation into the area
    private boolean foundationSwitch;

    //The switch on the robot that determines whether to try and pickup a block
    private boolean blockSwitch;

    //Values for the wheels
    private final double wheelRadius = 5*25.4;
    private final double wheelCircumference = wheelRadius * 2 * Math.PI;

    private int leftFrontEncoder;
    private int rightFrontEncoder;
    private int leftBackEncoder;
    private int rightBackEncoder;

    public HardwareSPQR robot = new HardwareSPQR();

    /**
     * This method is an abstraction to initialize the hardware of the robot.
     */
    public void hardwareInit(){
        this.robot.init(hardwareMap);
    }

    /**
     * This method is an abstraction to close the block-grabber.
     */
    public void grabBlock(){
        this.robot.grabBlock();
    }

    /**
     * This method is an abstraction to open the block-grabber.
     */
    public void releaseBlock(){
        this.robot.releaseBlock();
    }

    /**
     * This method moves the robot forward until the robot's color sensor is above the tape. It
     * assumes that the robot is already facing the direction of the tape and has no obstacles
     * between it and the tape. If a tolerated color to the right color is not detected, the robot
     * will not stop moving.
     *
     * @param tapeColor An array of the color of the tape of which the robot is to stop over. Given
     *                  in the format {RED, GREEN, BLUE} as detected by a sensor of the same type as
     *                  the one on the underside of the robot.
     * @param change The tolerance of error of each component of the color that is detected by the
     *               color sensor.
     */
    public void stopAtTape(int[] tapeColor, int change){
        this.robot.setPowers(.6);
        boolean isOnLine = false;
        while (!isOnLine && this.opModeIsActive()) {
            int[] r = this.plusOrMinus(this.robot.lineParkSensor.red(), change);
            int[] g = this.plusOrMinus(this.robot.lineParkSensor.green(), change);
            int[] b = this.plusOrMinus(this.robot.lineParkSensor.blue(), change);

            //Check if the robot is over the line
            isOnLine = (((tapeColor[0] > r[0]) && (tapeColor[0] < r[1])) && ((tapeColor[1] > g[0]) && (tapeColor[1] < g[1])) && ((tapeColor[2] > b[0]) && (tapeColor[2] < b[1])));
        }
        this.robot.lineParkSensor.enableLed(false);
        this.robot.stopMoving();
    }

    /**
     * This method calculates the approximate distance that the robot has traveled with the average
     * encoder value of all of the robot's drive motor's encoders.
     *
     * @return The approximate distance in centimeters (or millimeters, unsure) that the robot has
     * traveled since the last time the encoders were reset to the zero position.
     */
    public double calculateDistance(){
      double encoder = this.driveAverage();
      return (encoder / ppr) * wheelCircumference;
    }

    /**
     * This method calculates and returns the average encoder value of all of the robot's drive
     * motors using the absolute values of each encoder position of each drive motor of the robot.
     *
     * @return A double greater than zero which represents the average encoder values of all of the
     * robot's drive motors.
     */
    public double getAverageEncoder(){
        int[] encoderPositions = {this.robot.leftFrontDrive.getCurrentPosition(), this.robot.rightFrontDrive.getCurrentPosition(), this.robot.leftBackDrive.getCurrentPosition(), this.robot.rightBackDrive.getCurrentPosition()};
        int sum = 0;
        for (int encoderPosition : encoderPositions){
            sum += Math.abs(encoderPosition);
        }
        return sum / encoderPositions.length;
    }

    /**
     * UNKNOWN
     *
     * @param start UNKNOWN
     * @return UNKNOWN
     */
    public double calculateDistance(double start){
        return calculateDistance() - start;
    }

    /**
     * This method takes a value and determines the two values equidistant from the given value with
     * both having a given distance from the given initial value.
     *
     * @param value An integer to be used to calculate final points based on change.
     * @param change An integer which is the absolute distance from the value.
     * @return An array of integers with two indexes with index zero being the smallest possible
     * integer from the given value with a given distance and with index one being the largest
     * possible integer from the given value with a given distance.
     */
    private int[] plusOrMinus(int value, int change){
        change = Math.abs(change);
        return new int[] {value - change, value + change};
    }

    /**
     * This method synchronously turns the robot to a specified angle (in degrees) that is relative
     * to the robot at a given speed.
     *
     * @param angle A double which is the relative angle (in degrees) to turn.
     * @param speed A double between -1.0 and 1.0 which is the speed at which the robot is to turn.
     *              This value will be assigned as the speed of the motors
     */
    public void turn(double angle, double speed){
        DcMotor.ZeroPowerBehavior previousBehavior = this.robot.leftFrontDrive.getZeroPowerBehavior();
        this.robot.setDrivesBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        resetEncoders();
        double encoderLimit = Math.abs(this.degppr*angle);
        if (angle > 0) {
            this.robot.tank(speed, -speed);
        } else if (angle < 0) {
            this.robot.tank(-speed, speed);
        }
        while (encoderLimit > getAverageEncoder() && !isStopRequested() && opModeIsActive()){
            updateTelemetry();
        }
        this.robot.stopMoving();
        this.sleep(10000);
        this.robot.setDrivesBehavior(previousBehavior);
    }

    /**
     * This method drives the robot forward by a given distance in centimeters (or millimeters,
     * unsure) at a specified speed. The robot will go backwards if the speed given is a value less
     * than zero.
     *
     * @param distance A double which represents the distance for the robot to travel in centimeters
     *                 (or millimeters, unsure)
     * @param speed A double between -1.0 and 1.0 which is the speed at which the robot is to drive.
     */
    public void drive (double distance, double speed){
        this.robot.setDrivesBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        resetEncoders();
        double encoderLimit = (distance/wheelCircumference)*ppr;
        this.robot.setPowers(speed);
        while(calculateDistance() < distance && !isStopRequested() && opModeIsActive()){
            updateTelemetry();
            limitRate(encoderLimit);
        }
        this.robot.stopMoving();
        sleep(10000);
    }


    /**
     * UNKNOWN
     *
     * @param limit UNKNOWN
     */
    public void limitRate (double limit) {
        int[] encoderPositions = {this.robot.leftFrontDrive.getCurrentPosition(),
                this.robot.rightFrontDrive.getCurrentPosition(),
                this.robot.leftBackDrive.getCurrentPosition(),
                this.robot.rightBackDrive.getCurrentPosition()};
        for (int i = 0; i < encoderPositions.length; i++){
            if (Math.abs(encoderPositions[i]) > limit){
                if (i == 0) {
                    this.robot.leftFrontDrive.setPower(0);
                } else if (i == 1) {
                    this.robot.rightFrontDrive.setPower(0);
                } else if (i == 2) {
                    this.robot.leftBackDrive.setPower(0);
                } else if (i == 3) {
                    this.robot.rightBackDrive.setPower(0);
                }
            }
        }
    }

    /**
     * This method resets the encoder positions of the drive motors to zero and adds the current
     * encoder position to the total encoder positions.
     */
    public void resetEncoders(){
        this.leftFrontEncoder += this.robot.leftFrontDrive.getCurrentPosition();
        this.rightFrontEncoder += this.robot.rightFrontDrive.getCurrentPosition();
        this.leftBackEncoder += this.robot.leftBackDrive.getCurrentPosition();
        this.rightBackEncoder += this.robot.rightBackDrive.getCurrentPosition();

        this.robot.leftFrontDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        this.robot.rightFrontDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        this.robot.leftBackDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        this.robot.rightBackDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        this.robot.leftFrontDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        this.robot.rightFrontDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        this.robot.leftBackDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        this.robot.rightBackDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
    }

    /**
     * This method calculates and returns the average encoder value of all of the robot's drive
     * motors using the values of each encoder position of each drive motor of the robot.
     *
     * @return A double which represents the average encoder values of all of the robot's drive motors.
     */
    public double driveAverage(){
      int[] encoderPositions = {this.robot.leftFrontDrive.getCurrentPosition(), this.robot.rightFrontDrive.getCurrentPosition(), this.robot.leftBackDrive.getCurrentPosition(), this.robot.rightBackDrive.getCurrentPosition()};
      int sum = 0;
      for (int encoderPosition : encoderPositions){
        sum += encoderPosition;
      }
      return sum / encoderPositions.length;
    }

    /**
     * This method updates the telemetry data on both the driver station and the robot controller
     * with common debugging information.
     */
    public void updateTelemetry(){
        telemetry.addData("Distance", calculateDistance());
        telemetry.addData("leftFrontTempEncoder", this.robot.leftFrontDrive.getCurrentPosition());
        telemetry.addData("rightFrontTempEncoder", this.robot.rightFrontDrive.getCurrentPosition());
        telemetry.addData("leftBackTempEncoder", this.robot.leftBackDrive.getCurrentPosition());
        telemetry.addData("rightBackTempEncoder", this.robot.rightBackDrive.getCurrentPosition());
        telemetry.addData("leftFrontEncoder", this.leftFrontEncoder);
        telemetry.addData("rightFrontEncoder", this.rightFrontEncoder);
        telemetry.addData("leftBackEncoder", this.leftBackEncoder);
        telemetry.addData("rightBackEncoder", this.rightBackEncoder);
        telemetry.update();
    }
}
