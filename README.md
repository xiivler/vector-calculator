# vector-calculator
A program to calculate optimal vector angles in Super Mario Odyssey, primarily for use in TASing

## Configuring the Initial Movement
Upon opening vector-calculator.jar, you will see the movement configuration window.

First, select the initial movement type you would like (ex. triple jump, vault) from the menu that appears when you click on the cell that currently says "Single Jump." Enter the number of frames you want this movement to last in the cell below this. If you instead would like to enter the amount of distance you'd like the movement to rise or fall before executing the next movement, change the Initial Movement Duration Type to Vertical Displacement. Use a negative number if you'd like the initial movement to end lower than it began, or a positive number if you'd like the initial movement to end higher than it began.

Note that roll cancel vectors will only be calculated accurately if Mario preserves his speed no matter his roll cancel angle. If you roll cancel at the last possible frame when rolling off a ledge, this should happen (you should notice Mario fall by 7 units on the frame before the roll cancel).

For some types of jumps, the number of frames you hold A/B matters. If you selected one of these jumps, you can configure the number of frames you hold A/B in the next row; the value can range from 1 to 10. If you selected a jump where this does not matter, you will see "N/A" in this row. In the row below this, set the initial horizontal speed, or the horizontal speed you are traveling at the frame before the jump, unless this row has "N/A" in it. Configure the value of Initial Vector Direction to specify whether Mario vectors to the left or right for the initial movement.

There are two special options for the initial movement. "Optimal Distance Motion" will determine the movement that will give you the greatest distance given how high or low a position you want the movement to end at, and "Optimal Distance RCV" will do the same, but only with the different types of RCVs. Note that the movement options take different amounts of time to fall to your specified position, so the movement it produces might not be the fastest option.

## Configuring the Coordinates and Angles
If you're just experimenting and not TASing a particular vector, you can skip to the next section. Otherwise, enter the initial X, Y, and Z positions of Mario in the first three rows, and decide whether you will be entering a target angle or an initial angle.

If you enter a target angle, the program will find the vector(s) such that you travel the farthest in this direction. If you enter an initial angle, the program will calculate optimal vector(s) given that Mario is initially moving in this direction. If you would like to enter a target angle, click on the cell labeled "Target Angle" in the Angle Type row and select Initial Angle from the dropdown menu. If you selected a roll cancel vector as your initial movement type, you will be able to enter both an initial angle and a target angle.

If you are using my fork of practice mod or another mod that displays 0 as the horizontal speed angle when traveling in the positive Z direction, click on "X" in the 0 Degree Axis row and select "Z." You can calculate the target angle from where you'd like jump (X1, Y1, Z1) to where you'd like to land (X2, Y2, Z2) by taking arctan((X2 - X1)/(Z2 - Z1)) in degrees and adding 180 degrees if necessary.

If you are using any other mod, you can calculate the target angle from where you'd like jump (X1, Y1, Z1) to where you'd like to land (X2, Y2, Z2) by taking arctan((Z2 - Z1)/(X2 - X1)) in degrees and adding 180 degrees if necessary.

Now, determine what camera angle you'd like to have when TASing the vector. If you would like the camera to be facing in the direction of the target angle, leave the Camera Angle set to "Target Angle." You can also change Camera Angle to be "Initial Angle" if you would like the camera to face the direction of the initial angle, "Custom" if you would like to specify a particular camera angle, or "Absolute" if you would like all joystick angles to be absolute. If you selected "Custom," enter the camera's horizontal rotation in the Custom Camera Angle row that appears below.

## Configuring Additional Properties
Select "Moon" from the Gravity row's dropdown menu if you want to use moon gravity instead of regular gravity. The next row, Hyperoptimize Cap Throws, is set to "True," meaning that cap throw rotations will be carefully controlled in order to gain a small amount of distance (more the longer the cap throw lasts). If you do not need this level of optimization, set this row to "False."

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
Go back and tweak the various parameters until you are happy with the result. A good strategy to get more distance is to find movements that have frames with lots of falling speed and not a lot of horizontal speed and lower the number of frames these movements last, adding frames to movements that have a better ratio of horizontal speed to falling speed.

Then, if the top row of the Vector Calculations window displays an initial angle, modify the part of the TAS before the jump until Mario's velocity is the direction of the initial angle. This may change your initial X, Y, and Z coordinates, in which case you should update them in vector calculator, change the target angle if desired, and press Calculate Vectors again.

It is recommended, if possible, to turn the camera as far down as possible because this will make it not rotate as Mario moves.

Now, select the script format you are using from the menu at the bottom left of the window, and enter the path of a script file to which you would like to add the inputs Vector Calculator has calculated, or enter the path to a new script file that you'd like to create with these inputs. If you entered an existing script, make sure it is saved, then click "Append." Otherwise, click "Create."   You can instead press "Copy to Clipboard" to copy the inputs to your clipboard so you can paste them into an open script file in a text or spreadsheet editor.

Once your script file is ready, run the TAS and check if your final position roughly matches the one given by Vector Calculator (it should be accurate within a couple of units). If they vary greatly, you can use the frame-by-frame data to troubleshoot. For frames where the joystick radius is .11, you should expect Mario to be doing a fast turnaround, and if you notice yourself losing speed during a vector compared to what Vector Calculator is reporting, try holding slightly less to the side to vector. If you entered the information correctly, this is unlikely to be the case unless you didn't point the camera down, in which case you will likely have to tweak a lot of inputs to get an optimal vector.

## Current Limitations

The program does not currently calculate edge cap bounces. If you would like to include one, try cap throwing more strongly than the program suggests, then rotating Mario to still dive in the same direction as the program calculates.

If the only midair movement is a dive (ex. triple jump -> dive with no cap throw), the program assumes your initial rotation is the same as your initial angle, but if you buffer the initial jump, this may not be the case. Likewise, the height for single and double jumps may be incorrect because they are based on Mario's velocity in the direction he is facing, which is not necessarily forwards.

Vector Calculator is not currently equipped to optimize roll cancel bounces or roll cancels into dives. It also does not currently create inputs for you to home roll cancel throws before the next movement.

Rotations during cap throws may be slightly inaccurate, but shouldn't affect the end result significantly. The same is true                            for cap bounces right into dives.