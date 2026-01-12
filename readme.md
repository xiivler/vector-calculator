# vector-calculator
A program to calculate optimal vector angles in Super Mario Odyssey, primarily for use in TASing

## Using the Program
Upon opening vector-calculator.jar, you will see the movement configuration window, containing the following fields:

    Initial X/Y/Z Position – enter Mario's coordinates the frame before the movement begins

    Angle – enter a target or initial angle

    Angle Type – specify whether you entered a target or initial angle
        Target angle: the program will find the vector(s) such that you travel the farthest in this direction
        Initial angle: the program will calculate optimal vector(s) given Mario initially moving in this direction
        
    Initial Movement Type
    
    Initial Movement Frames
    
    Frames of Holding Jump – applicable for some jumps to determine how long Mario travels at his initial vertical speed
    
    Initial Horizontal Speed – Mario's horizontal speed the frame before the movement begins
    
    Initial Vector Direction – whether Mario vectors to the left or right for the first movement
    
    Gravity – choose between regular and moon gravity

There is also a table for midair movement options, including cap throws, dives, cap bounces, and rainbow spins. Use the plus and minus buttons to add and remove these.

When you are finished configuring the movement, click Calculate Vectors. The Vector Calculations window will appear, with the following information:

    Initial/Target Angle – the value of whichever angle you didn't set in the configuration window
    
    Final X/Y/Z Position
    
    Horizontal Displacement – the difference between Mario's final and initial horizontal position
    
    Vertical Displacement – the difference between Mario's final and initial Y-position
    
    Total Frames

Below this information is a table containing frame-by-frame information regarding the movement type, input(s), angle to hold, position, velocity, and horizontal speed.

## TASing a Vector

You will need CraftyBoss' ramwatch or a similar mod that displays Mario's position and speed each frame.

If you are calculating a vector based on a target angle you can determine this angle by finding arctan((Z2 - Z1)/(X2 - X1)) for two points P2 and P1 in the game. You can also enter first person mode from where you would like to start the jump, face the end position, and add 90 degrees to Mario's position. For both of these options, you may need to adjust the angle by 180 degrees.

If you are calculating a vector based on an initial angle (such as when flinging from a vertical pole), use arctan(Vz0/Vx0) for the initial angle. Same as for calculating target angles, you may need to adjust the angle by 180 degrees.

If you are TASing a cap throw into a dive into a cap bounce, the duration of the cap throw affects how long the dive will be. Use [this table](https://docs.google.com/spreadsheets/d/1_MpaK-Ym6sUGMppYo0vVH_JIwWFEt6G1nwNJ_na5e0I/edit#gid=241883068&range=B28) if you are not throwing cappy against a wall, or use trial and error in other scenarios.

Once you are happy with the vector(s) you have calculated, use a TAS editing software to input the frames as indicated by the frame-by-frame display in the Vector Calculations window. However, depending on your camera angle, you will need to adjust the holding angles, which can usually only be done by trial and error by comparing Mario's expected and actual positions and/or velocities. Note that if you make the camera face directly down, it will not rotate as Mario's velocity changes, meaning that you just need to adjust one angle properly and then adjust the rest by the same amount.

## Current Limitations

When optimizing a cap throw vector or another vector before a dive, the program will spend half the frames vectoring at the maximum possible distance and the other half rotating Mario back to the correct angle for the dive. This is slightly unoptimal compared to holding near backwards on the joystick to do a fast turnaround.
