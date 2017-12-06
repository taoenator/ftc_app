package org.firstinspires.ftc.teamcode;

import android.graphics.Color;

import com.qualcomm.hardware.lynx.LynxI2cColorRangeSensor;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.navigation.RelicRecoveryVuMark;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackable;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackables;

/**
 * Created by serge on 12/5/2017.
 * Base class for all autonomous OpModes
 */
public abstract class BaseAutonomous extends BaseOpMode {
    protected final String vuforiaLicense = "AXOZJVv/////AAAAGb2y8LIaVE3fi65/6/TwcCMDDJlAw0RjWXtTZPLR" +
            "Q2o5U7/aeLkF3wRej13hegHVoLuP3QCE5STcnpT0ajAMqa4ObqC3n6R6hWGUCPl5ZjtGeNLghtVE5pswlpc8/8" +
            "Z4GwJEE65mmAwo6tfS54FIpfVq7qLKF3rByohYrwwKZ1mQM6STF1t8IsbeXrBEtfCQN5fSX2wLMPSJE34Iz0Ig" +
            "VlSAVbJfdxKkX8JONhqeAOWseLUkG+fI+Da71V4eMzfHarfuN7Nltbd+3zNE7DIwFQs5/PDIotbVVpYrpS4wiH" +
            "1lNPNxWLSyv/ArSyCyNi9Ygi5W/UqIlQ+7Mweg6f16d6nZpMN1Ejv1o0s7L4L0aXny";

    // Declare OpMode members.
    protected ElapsedTime runtime = new ElapsedTime(ElapsedTime.Resolution.MILLISECONDS);

    private VuforiaLocalizer vuforia;

    public RelicRecoveryVuMark getColumn() {
        int cameraMonitorViewId = hardwareMap.appContext.getResources()
                .getIdentifier("cameraMonitorViewId",
                        "id",
                        hardwareMap.appContext.getPackageName());

        VuforiaLocalizer.Parameters parameters = new VuforiaLocalizer.Parameters(cameraMonitorViewId);
        parameters.vuforiaLicenseKey = vuforiaLicense;
        parameters.cameraDirection = VuforiaLocalizer.CameraDirection.BACK;
        this.vuforia = ClassFactory.createVuforiaLocalizer(parameters);

        VuforiaTrackables relicTrackables = this.vuforia.loadTrackablesFromAsset("RelicVuMark");
        VuforiaTrackable relicTemplate = relicTrackables.get(0);
        relicTemplate.setName("relicVuMarkTemplate");
        relicTrackables.activate();

        this.resetStartTime();

        while(this.getRuntime() < 3.0 && opModeIsActive()) {
            RelicRecoveryVuMark vuMark = RelicRecoveryVuMark.from(relicTemplate);
            if (vuMark != RelicRecoveryVuMark.UNKNOWN) {
                return vuMark;
            } else {
                telemetry.addData("VuMark", "not visible");
            }
            telemetry.update();
        }
        return RelicRecoveryVuMark.UNKNOWN;
    }

    /*
     * Initialize the variables for the OpMode
     */
    protected void initOpMode() {
        // Motors initialization
        motor0 = hardwareMap.get(DcMotor.class, "motor0");
        motor1 = hardwareMap.get(DcMotor.class, "motor1");
        motor0.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        motor1.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        spoolMotor = hardwareMap.get(DcMotor.class, "spoolMotor");
        // Servos initialization
        servo0 = hardwareMap.get(Servo.class, "servo0");
        servo1 = hardwareMap.get(Servo.class, "servo1");
        servo2 = hardwareMap.get(Servo.class, "servo2");
        servo3 = hardwareMap.get(Servo.class, "servo3");
        servo5 = hardwareMap.get(Servo.class, "servo5");
        // Sensors intialization
        color0 = hardwareMap.get(LynxI2cColorRangeSensor.class, "color0");

        // initialize the spool encoder
        spoolMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        spoolMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
    }

    /**
     * Lift the clamp
     */
    protected void lift(double height) {
        // variable height is the position of the slide from 0
        double targetHeight = -600f * height;
        spoolMotor.setTargetPosition((int)targetHeight);
        spoolMotor.setPower(1);
        while (spoolMotor.isBusy());
        spoolMotor.setPower(0);
        spoolMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
    }

    /**
     * Open or close the clamp
     */
    protected void clamp(boolean doClose)
    {
        if (doClose) {
            servo0.setPosition(1);
            servo1.setPosition(-1.5);
        } else {
            servo0.setPosition(0.25);
            servo1.setPosition(0.5);
        }
    }

    protected float getSaturation(int color) {
        float[] array = new float[3];
        Color.colorToHSV(color, array);
        return array[1];
    }

    protected float getHue(int color) {
        float[] array = new float[3];
        Color.colorToHSV(color, array);
        return array[0];
    }

    /**
     * Move the robot forward
     */
    protected void moveInch(double inches) {
        motor0.setDirection(DcMotor.Direction.FORWARD);
        motor1.setDirection(DcMotor.Direction.FORWARD);
        motor0.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motor1.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motor0.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        motor1.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        //  If you are using AndyMark or REV motors, replace
        // 1440 with
        //     AndyMark NeveRest 40: 1120
        //     REV Hex Motor: 2240
        motor0.setTargetPosition((int) (inches * -57.75));
        motor1.setTargetPosition((int) (inches * 57.75));
        waitForStart(); // really ???
        // the maximum speed of the motors.
        motor0.setPower(0.1);
        motor1.setPower(0.1);
        // Loop until both motors are no longer busy.
        while (motor0.isBusy() || motor1.isBusy());
        motor0.setPower(0);
        motor1.setPower(0);
    }

    protected void turn(double degrees) {
        motor0.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motor1.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motor0.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        motor1.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        // Change 1440 appropriately if you are not using
        // Modern Robotics encoders
        motor0.setTargetPosition((int)(degrees * 7.55));
        motor1.setTargetPosition((int)(degrees * 7.55));
        motor0.setPower(0.15);
        motor1.setPower(0.15);
        while (motor0.isBusy() || motor1.isBusy());
        motor0.setPower(0);
        motor1.setPower(0);
    }
}