package org.firstinspires.ftc.teamcode;

import com.qualcomm.ftccommon.SoundPlayer;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

/**
 * TeleOp: BOTH ALLIANCES
 *
 * Start anywhere.
 *
 * Driver controls:
 *  - Right bumper: Strafe right.
 *  - Left bumper: Strafe left.
 *  - Right stick Y: Speed of the right drive motors of the robot.
 *  - Left stick Y: Speed of the left drive motors of the robot.
 *  - Button 'a': Reverse direction.
 *  - Button 'b': Toggle sniper mode.
 *  - Dpad down: Drop tow.
 *  - Dpad up: Raise tow.
 *
 * Gunner controls:
 *  - Button 'a': Grab block.
 *  - Button 'b': Release block.
 *  - Button 'x': Bring arm to capstone.
 *  - Right bumper: Reset arm encoder position to zero.
 *  - Right stick Y: Move arm.
 *
 * @author Arkin Solomon
 */
@TeleOp(name="Main OpMode", group="Competition")
public class MainOpMode extends OpMode {

    private HardwareSPQR robot = new HardwareSPQR();

    //Speed of the robot
    private double speed = 1.0;

    //Prevent detecting multiple clicks
    private boolean gamepad1_aPressed = false;
    private boolean gamepad1_bPressed = false;
    private boolean gamepad1_xPressed = false;

    @Override
    public void init() {

        //Initialize hardware
        this.robot.init(hardwareMap);
    }

    @Override
    public void loop() {

        /* Left and right strafing movement */
        if (gamepad1.left_bumper){
            if (gamepad1.right_bumper) return;
            this.robot.strafe(Dir.LEFT, this.speed);
        }
        if (gamepad1.right_bumper) {
            if (gamepad1.left_bumper) return;
            this.robot.strafe(Dir.RIGHT, this.speed);
        }

        /* Tank movement */ 
        double right = -gamepad1.right_stick_y * this.speed;
        double left = -gamepad1.left_stick_y * this.speed;
        if (this.speed < 0){
            double l = left;
            left = right;
            right = l;
        }

        //Only tank move if not strafing
        if (!gamepad1.left_bumper && !gamepad1.right_bumper){
            this.robot.tank(right, left);
        }

        /* Reverse direction */
        if (gamepad1.a) {
            if (this.gamepad1_aPressed) return;
            this.gamepad1_aPressed = true;
            this.speed *= -1;
        }
        if (!gamepad1.a){
            this.gamepad1_aPressed = false;
        }

        /* Sniper mode */
        if (gamepad1.b) {
            if (this.gamepad1_bPressed) return;
            this.gamepad1_bPressed = true;
            if (this.speed > 0){
                if (this.speed > 0.5){
                    this.speed = 0.5;
                }else{
                    this.speed = 1.0;
                }
            }else{
                if (this.speed < -0.5){
                    this.speed = -0.5;
                }else{
                    this.speed = -1.0;
                }
            }
        }
        if (!gamepad1.b){
            this.gamepad1_bPressed = false;
        }

        /* Bring tow down */
        if (gamepad1.dpad_down){
            this.robot.dropTow();
        }
        if (gamepad1.dpad_up){
            this.robot.raiseTow();
        }

        /* Grab blocks */
        if (gamepad2.a){
            this.robot.grabBlock();
        }
        if (gamepad2.b){
            this.robot.releaseBlock();
        }

        /* Get capstone */
        if (gamepad2.x){
            this.robot.armMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            this.robot.armMotor.setTargetPosition(-23);
            this.robot.armMotor.setPower(0.15);
        }

        /* Arm movement */

        //Reset arm zero
        if (gamepad2.right_bumper && gamepad2.left_bumper && !this.robot.armMotor.isBusy()){
            this.robot.armMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            this.robot.armMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        }

        if (!this.robot.armMotor.isBusy()){
            this.robot.armMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            this.robot.armMotor.setPower(gamepad2.right_stick_y / 10);
        }

        //Play sound
        if (gamepad1.x){
            if (this.gamepad1_xPressed) return;
            this.gamepad1_xPressed = true;
            SoundPlayer.getInstance().startPlaying(hardwareMap.appContext, this.robot.pacmanId);
        }
        if (!gamepad1.x){
            this.gamepad1_xPressed = false;
        }
        this.robot.armBalancer.setPosition(this.robot.getServoPosition(this.robot.armMotor.getCurrentPosition()));

        /* Telementry data */
        telemetry.addData("Tow", this.robot.tow.getCurrentPosition());
        telemetry.addData("Arm", this.robot.armMotor.getCurrentPosition());
        telemetry.addData("Servo", this.robot.armBalancer.getPosition());
        telemetry.addData("Red", this.robot.lineParkSensor.red());
        telemetry.addData("Green", this.robot.lineParkSensor.green());
        telemetry.addData("Blue", this.robot.lineParkSensor.blue());
        telemetry.update();
    }
}
