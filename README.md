# vector-calculator
A program to calculate optimal vector angles in Super Mario Odyssey, primarily for use in TASing

## Configuring the Initial Movement
Upon opening vector-calculator.jar, you will see the movement configuration window.

First, select the initial movement type you would like (ex. triple jump, vault) from the menu that appears when you click on the cell that currently says "Single Jump." Enter the number of frames you want this movement to last in the cell below this. For some types of jumps, the number of frames you hold A/B matters. If you selected one of these jumps, you can configure the number of frames you hold A/B in the next row; the value can range from 1 to 10. If you selected a jump where this does not matter, you will see "N/A" in this row. In the row below this, set the initial horizontal speed, or the horizontal speed you are traveling at the frame before the jump, unless this row has "N/A" in it.

Next, configure the value of Initial Vector Direction to specify whether Mario vectors to the left or right for the initial movement, and select "Moon" from the Gravity row's dropdown menu if you want to use moon gravity instead of regular gravity. The bottom row, "Hyperoptimize Cap Throws," is set to "True," meaning that cap throw rotations will be carefully controlled in order to gain a small amount of distance (more the longer the cap throw lasts). If you do not need this level of optimization, set this row to "False."

If you're just experimenting and not TASing a particular vector, you can skip to the next section. Otherwise, enter the initial X, Y, and Z positions of Mario in the first three rows, and decide whether you will be entering a target angle or an initial angle.
If you enter a target angle, the program will find the vector(s) such that you travel the farthest in this direction. If you enter an initial angle, the program will calculate optimal vector(s) given that Mario is initially moving in this direction. If you would like to enter a target angle, click on the cell labeled "Target Angle" in the Angle Type row and select Initial Angle from the dropdown menu. If you selected a roll cancel as your initial movement type, you will be able to enter both an initial angle and a target angle.

## Configuring Midair Movement
Now, you can begin adding midair movements, such as cap throws, dives, cap bounces, and rainbow spins. Use the plus button in the bottom left corner to add a midair movement. Click on the cell in the Midair Movement Type column to change the movement type, and configure the number of frames the movement lasts in the Number of Frames column.

If you are TASing a cap throw into a dive into a cap bounce, the duration of the cap throw affects how long the dive will be. Use [this table](https://docs.google.com/spreadsheets/d/1_MpaK-Ym6sUGMppYo0vVH_JIwWFEt6G1nwNJ_na5e0I/edit#gid=241883068&range=B28) as a starting point if you are not throwing cappy against a wall, or use trial and error in other scenarios.

## Calculating the Vectors
When you are finished configuring the movement, click Calculate Vectors. The Vector Calculations window will appear, with the following information:

    Initial/Target Angle – the value of whichever angle you didn't set in the configuration window, or the target angle if you set both
    
    Final X/Y/Z Position
    
    Horizontal Displacement – the difference between Mario's final and initial horizontal position
    
    Vertical Displacement – the difference between Mario's final and initial Y-position
    
    Total Frames

Below this information is a table containing frame-by-frame information regarding the movement type, input(s), joystick angle, position, velocity, and horizontal speed.

The joystick column specifies how to hold the joystick in terms of polar coordinates (radius and angle). The horizontal speed column is also in polar coordinates, representing the speed and the direction the speed is in.

## Iterating and TASing
Go back and tweak the various parameters until you are happy with the result. Then, if the top row of the Vector Calculations window displays an initial angle, modify the part of the TAS before the jump until Mario's velocity is the direction of the initial angle. This may change your initial X, Y, and Z coordinates, in which case you should update them in vector calculator, change the target angle if desired, and press Calculate Vectors again.

Now, use your TAS editing software to input the inputs and joystick angles as indicated by the frame-by-frame display in the Vector Calculations window. However, depending on your camera angle, you may need to adjust the joystick angles, which can usually only be done by trial and error by comparing Mario's expected and actual positions and/or velocities. Note that if you make the camera face as far down as possible, it will not rotate as Mario's velocity changes, meaning that you just need to adjust one angle properly and then adjust the rest by the same amount. If you don't point the camera down, it is likely that if Hyperoptimize Cap Throws is set to True, it will be very difficult to rotate Mario properly.

A strategy is simply to face the target angle of the jump in-game, center the camera, face it down, then set the target angle in Vector Calculator to 90 degrees, calculate the vectors, and enter the joystick angles without having to adjust them at all.

## Current Limitations

The program does not currently calculate edge cap bounces. If you would like to include one, try cap throwing more strongly than the program suggests, then rotating Mario to still dive in the same direction as the program calculates.
