
package org.usfirst.frc.team4361.robot;

import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.CANTalon;
import edu.wpi.first.wpilibj.*;
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
public class Robot extends IterativeRobot {
    final String defaultAuto = "Default";
    final String customAuto = "My Auto";
    String autoSelected;
    SendableChooser chooser;
    Talon[] talon = new Talon[4];
    Tracking track;
    NetworkTable table;
    WeightedAverage ave; 
	
    /**
     * This function is run when the robot is first started up and should be
     * used for any initialization code.
     */
    public void robotInit() {
        chooser = new SendableChooser();
        chooser.addDefault("Default Auto", defaultAuto);
        chooser.addObject("My Auto", customAuto);
        SmartDashboard.putData("Auto choices", chooser);
        track = new Tracking(108,89);
        ave = new WeightedAverage(50,300);
        for(int i = 0; i < talon.length; i++)
        {
        	talon[i] = new Talon(i);
        }
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
    public void teleopPeriodic() {
    	Joystick left = new Joystick(0);
    	Joystick right = new Joystick(1);
    	table = NetworkTable.getTable("GRIP/myContoursReport");
    	double[] deafultVal = new double[0];
    	double[] centerX = table.getNumberArray("centerX", deafultVal);
    	double[] centerY = table.getNumberArray("centerY", deafultVal);
    	double[] width = table.getNumberArray("width",deafultVal);
    	double[] length=table.getNumberArray("length", deafultVal);
    	double[] area = table.getNumberArray("area",deafultVal);
    	String dir = "";
    	double filter=0;
   
    	if(centerX.length>0&&left.getRawButton(3))
    	{
    		if(!ave.cal)
    		{
    			cal(length, width, centerX, centerY);
    		}
    		else
    		{
    			double[] values = input(length, width, centerX, centerY, area);
    			 dir = track.track(values[0], values[1]);
    			 filter = values[2];
    		}
    		
    		if(dir.equals("left"))
    		{
    			talon[0].set(0);
    			talon[1].set(0);
    			talon[2].set(-filter);
    			talon[3].set(-filter);
    		}
    		if(dir.equals("right"))
    		{
    			talon[0].set(filter);
    			talon[1].set(filter);
    			talon[2].set(0);
    			talon[3].set(0);
    		}
    		if(dir.equals("forward"))
    		{
    			talon[0].set(filter);
    			talon[1].set(filter);
    			talon[2].set(-filter);
    			talon[3].set(-filter);
    		}
    		if(dir.equals("back"))
    		{
    			talon[0].set(-filter);
    			talon[1].set(-filter);
    			talon[2].set(filter);
    			talon[3].set(filter);
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
        
    }
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
		double corScore=.7;
		//replace this loop with a loop to look through length and make score
		//length[i]/width[i]
		for(int i=0; i<length.length; i++)
		{
			score = length[i]/width[i];
			if(Math.abs(score-1)<Math.abs(corScore-1))
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
    public double[] input(double[] length, double width[], double[] x, double[] y, double[] a)
    {
    	int index =-1;
		double score =0;
		//calibrate this corScore value
		double corScore=.7;
		//replace this loop with a loop to look through length and make score
		//length[i]/width[i]
		for(int i=0; i<length.length; i++)
		{
			score = length[i]/width[i];
			if(Math.abs(score-1)<Math.abs(corScore-1))
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
    
}
