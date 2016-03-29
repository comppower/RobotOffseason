
package org.usfirst.frc.team4361.robot;

import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.CANTalon;
import edu.wpi.first.wpilibj.*;
import com.kauailabs.navx.frc.*;
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
    Talon[] talon = new Talon[9];
    Tracking track;
    NetworkTable table;
    AHRS ahrs;
	
    /**
     * This function is run when the robot is first started up and should be
     * used for any initialization code.
     */
    public void robotInit() {
        chooser = new SendableChooser();
        chooser.addDefault("Default Auto", defaultAuto);
        chooser.addObject("My Auto", customAuto);
        SmartDashboard.putData("Auto choices", chooser);
        track = new Tracking(244,181);
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
   
    	
    	if(centerX.length>0 && right.getRawButton(4))
    	{
    		String dir = track.track(centerX[0], centerY[0]);
    		if(dir.equals("left"))
    		{
    			talon[0].set(0);
    			talon[1].set(0);
    			talon[2].set(-.2);
    			talon[3].set(-.2);
    		}
    		if(dir.equals("right"))
    		{
    			talon[0].set(.2);
    			talon[1].set(.2);
    			talon[2].set(0);
    			talon[3].set(0);
    		}
    		if(dir.equals("forward"))
    		{
    			talon[0].set(.2);
    			talon[1].set(.2);
    			talon[2].set(-.2);
    			talon[3].set(-.2);
    		}
    		if(dir.equals("back"))
    		{
    			talon[0].set(-.2);
    			talon[1].set(-.2);
    			talon[2].set(.2);
    			talon[3].set(.2);
    		}

    	}
    	else
    	{
    		talon[0].set(-right.getAxis(Joystick.AxisType.kY));
        	talon[1].set(-right.getAxis(Joystick.AxisType.kY));
        	talon[2].set(left.getAxis(Joystick.AxisType.kY));
        	talon[3].set(left.getAxis(Joystick.AxisType.kY));
    	}
        
    	
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
    
    /**
     * This function is called periodically during test mode
     */
    public void testPeriodic() {
    	talon[8].set(0);
    
    }
    
}
