
package org.usfirst.frc.team4361.robot;

import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.*;

import com.kauailabs.navx.frc.AHRS;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 * test addition
 * test addition three
 * practice addtional comment
 * help>
 * 
 */
public class Robot extends IterativeRobot implements PIDOutput {
    final String defaultAuto = "Default";
    final String customAuto = "My Auto";
    String autoSelected;
    SendableChooser chooser;
    Talon[] talon = new Talon[8];
    Tracking track;
    NetworkTable table;
    WeightedAverage ave; 
    AHRS ahrs;
    
	PIDController turnController;
	
    static final double kP = 0.03;
    static final double kI = 0.00;
    static final double kD = 0.00;
    static final double kF = 0.00;
    
    static final double kToleranceDegrees = 2.0f;
    
    double rotateToAngleRate;
    double distance;
    Timer timer;
    /**
     * This function is run when the robot is first started up and should be
     * used for any initialization code.
     */
    public void robotInit() {
        chooser = new SendableChooser();
        chooser.addDefault("Default Auto", defaultAuto);
        chooser.addObject("My Auto", customAuto);
        SmartDashboard.putData("Auto choices", chooser);
        track = new Tracking(133,105);
        ave = new WeightedAverage(10,700);
        for(int i = 0; i < talon.length; i++)
        {
        	talon[i] = new Talon(i);
        }
        ahrs=new AHRS(SPI.Port.kMXP);
        
        turnController = new PIDController(kP, kI, kD, kF, ahrs, this);
        turnController.setInputRange(-180.0f,  180.0f);
        turnController.setOutputRange(-1.0, 1.0);
        turnController.setAbsoluteTolerance(kToleranceDegrees);
        turnController.setContinuous(true);
        
        distance =0;
        timer = new Timer();
    }
    
	/**
	 * This autonomous (along with the chooser code above) shows how to select between different autonomous modes
	 * using the dashboard. The sendable chooser code works with the Java SmartDashboard. If you prefer the LabVIEW
	 * Dashboard, remove all of the chooser code and uncomment the getString line to get the auto name from the text box
	 * below the Gyro
	 *
	 * You can add additional auto modes by adding additional comparisons to the switch structure below with additional strings.
	 * If using the SendableChooser make sure to add them to the chooser code above as well.
	 */
    public void autonomousInit() {
    	autoSelected = (String) chooser.getSelected();
//		autoSelected = SmartDashboard.getString("Auto Selector", defaultAuto);
		System.out.println("Auto selected: " + autoSelected);
		
		
    }

    /**
     * This function is called periodically during autonomous
     */
    public void autonomousPeriodic() {
    	switch(autoSelected) {
    	case customAuto:
        //Put custom auto code here   
            break;
    	case defaultAuto:
    	default:
    	//Put default auto code here
            break;
    	}
    }

    /**
     * This function is called periodically during operator control
     */
    public void teleopInit()
    {
		ahrs.reset();
		ahrs.resetDisplacement();
		timer.start();
    }
    public void teleopPeriodic() {
    	//merge start
    	//also pull the two classes
    	//also make sure to update the centerX and Y values as well as area values 
    	Joystick left = new Joystick(0);
    	Joystick right = new Joystick(1);
    	table = NetworkTable.getTable("GRIP/myContoursReport");
    	double[] deafultVal = new double[0];
    	double[] centerX = table.getNumberArray("centerX", deafultVal);
    	double[] centerY = table.getNumberArray("centerY", deafultVal);
    	double[] width = table.getNumberArray("width",deafultVal);
    	double[] length = table.getNumberArray("height", deafultVal);
    	double[] area = table.getNumberArray("area",deafultVal);
    	String dir = "";
    	double filter=0;
    	double speed=.15;
    	
    	turnController.setSetpoint(90.0f);
    	turnController.enable();
    	//System.out.println(rotateToAngleRate);
    	if(left.getRawButton(6))
    	{
    		ahrs.resetDisplacement();
    		distance =0;
    	}
   
    	//System.out.println(ahrs.getVelocityX() + " , " + ahrs.getVelocityY());
    	distance += getDistance(ahrs.getVelocityY(), ahrs.getRawAccelY());
    	System.out.println(distance);

   
    	if(centerX.length>0&&left.getRawButton(3))
    	{
    		if(!ave.cal)
    		{
    			cal(length, width, centerX, centerY);
    			System.out.println("Calibrating");
    		}
    		else
    		{
    			double[] values = input(length, width, centerX, centerY, area);
    			 dir = track.track(values[0], values[1]);
    			 filter = values[2];
    			 System.out.println(dir);
    		}
    		
    		if(dir.equals("left"))
    		{
    			//1.5 to correct for slower turn
    			talon[0].set(-speed);
    			talon[1].set(-speed);
    			talon[2].set(-speed);
    			talon[3].set(-speed);
    		}
    		if(dir.equals("right"))
    		{
    			talon[0].set(speed);
    			talon[1].set(speed);
    			talon[2].set(speed);
    			talon[3].set(speed);
    		}
    		if(dir.equals("forward"))
    		{
    			talon[0].set(speed*1.25);
    			talon[1].set(speed*1.25);
    			talon[2].set(-speed*1.25);
    			talon[3].set(-speed*1.25);
    		}
    		if(dir.equals("back"))
    		{
    			talon[0].set(-speed*1.25);
    			talon[1].set(-speed*1.25);
    			talon[2].set(speed*1.25);
    			talon[3].set(speed*1.25);
    		}
    		if(dir.equals("shoot"))
    		{
    			talon[0].set(-right.getAxis(Joystick.AxisType.kY));
            	talon[1].set(-right.getAxis(Joystick.AxisType.kY));
            	talon[2].set(left.getAxis(Joystick.AxisType.kY));
            	talon[3].set(left.getAxis(Joystick.AxisType.kY));
    		}
    	}
    	else 
    	{
    	 	talon[0].set(-right.getAxis(Joystick.AxisType.kY));
        	talon[1].set(-right.getAxis(Joystick.AxisType.kY));
        	talon[2].set(left.getAxis(Joystick.AxisType.kY));
        	talon[3].set(left.getAxis(Joystick.AxisType.kY));
    	}
    	//end merge


    	if(right.getPOV()==0)
    	{
    		talon[6].set(1);
    		talon[7].set(-1);
    	}
    	else if(right.getPOV()==180)
    	{
    		talon[6].set(-1);
    		talon[7].set(1);
    	}
    	else 
    	{
    		talon[6].set(0);
    		talon[7].set(0);
    	}
    	
    	if(right.getRawButton(1))
    	{
    		talon[4].set(1);
    		talon[5].set(-1);
    	}
    	else if(left.getRawButton(1))
    	{
    		talon[4].set(-1);
    		talon[5].set(1);
    	}
    	else
    	{
    		talon[4].set(0);
    		talon[5].set(0);
    	}
      
    }
    //pull this method
    public void cal(double[] length, double[] width, double[] centerX, double[] centerY)
    {
    	//check to see if the array is full already
    	ave.cal=true;
		for(int i=0; i<ave.centerX.length; i++)
		{
			if(ave.centerX[i]==-1)
			{
				ave.cal=false;
			}
		}
		//intializes values to look for the best hit
    	int index =-1;
		double score =0;
		//calibrate this corScore value
		double corScore=1;
		//replace this loop with a loop to look through length and make score
		//length[i]/width[i]
		for(int i=0; i<length.length; i++)
		{
			score = length[i]/width[i];
			if(Math.abs(score-1.4)<Math.abs(corScore-1.4))
			{
				corScore=score;
				index = i;
			}
		}
		//do nothing if the index isn't changed
		if(index ==-1)
		{

		}
		else
		{
			//System.out.println(" 	"+corScore + " was closest at "+index);
			//put in centerX[index] here instead of corScore
			ave.xIn(centerX[index]);
			ave.yIn(centerY[index]);
		}
    }
    //merge this method
    public double[] input(double[] length, double width[], double[] x, double[] y, double[] a)
    {
    	int index =-1;
		double score =0;
		//calibrate this corScore value
		double corScore=1;
		//replace this loop with a loop to look through length and make score
		//length[i]/width[i]
		for(int i=0; i<length.length; i++)
		{
			score = length[i]/width[i];
			if(Math.abs(score-1.4)<Math.abs(corScore-1.4))
			{
				corScore=score;
				index = i;
			}
		}
		//do nothing if the index isn't changed
		if(index ==-1)
		{

		}
		else
		{
			//System.out.println(" 	"+corScore + " was closest at "+index);
			//put in centerX[index] here instead of corScore
			ave.xIn(x[index]);
			ave.yIn(y[index]);
			ave.areaIn(a[index]);
		}
    	double[] def = {ave.getAverage("x"),ave.getAverage("y"), ave.getAverage("area")};
    	return def;
    }
    
    /**
     * This function is called periodically during test mode
     */
    public void testPeriodic() {
    
    }

	@Override
	public void pidWrite(double output) 
	{
		rotateToAngleRate = output;
	}
	public double getDistance(double vel, double accel)
	{
		double distance =0;
		if(timer.get()>0)
		{
			distance = timer.get()*vel+.5*accel*timer.get()*timer.get();
			timer.reset();
		}
		return distance;
	}
    
}
