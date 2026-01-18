#Vector Calculator Tutorial

This tutorial will help someone who has no experience TASing TAS jumps using Vector Calculator.

##Part 1: Setting up TASing tools

Join the Super Mario Odyssey TAS server and follow the instructions in #resources to set up either Practice Mod or LunaKit to TAS with. If using practice mod, make sure you install and test the smo-practice-server before continuing.

This tutorial assumes that you will be using TSV-TAS-2 as your script format, as it is recommended for Vector Calculator because it works the most easily. (Link to this.) Make sure to familiarize yourself with the script format by reading the tutorial.

Additionally, copy the folder `absolute-joystick` to the SD:/atmosphere/exefs. This mod, created by tetraxile, will cause the game to ignore the camera angle when translating joystick angles to Mario's movement, making it possible to TAS jumps without having to point the camera downward to prevent the camera from turning. This step is not required to use Vector Calculator, but the rest of the tutorial assumes yoyu have completed it.

##Part 2: TASing a Vault

If double clicking Vector Calculator does not open it, open it by navigating to its directory in a terminal window and running the command java -jar vector-calculator.jar. If this does not work, make sure you have Java installed.

Click the menu item `Reset to Program Defaults` in the File menu.

In Super Mario Odyssey, stand at the point where the vault begins. This does not have to be exactly precise.

In Vector Calculator, click on the `value` cell of the Initital Coordinates row (it says (0, 0, 0)) and then enter Mario's X, Y, and Z coordinates from the game.

Now, stand at the point where you believe the vault will land, and enter Mario's coordinates in the Target Coordinates row.

Select Vault as the initial movement type, and choose the direction you want the vault to go in in the Vector Direction row.

Select the type of midairs you want from the midairs row. If you want to have a triple throw, set the value of the Triple Throw row to `Yes`.

Now, press the Solve button. After some time, a window will open with frame-by-frame information about the jump. This is a good time to save your Vector Calculator project, which you can do by typing command/control + S while the main window is selected.

Make a copy of `ud-vault.tsv` from the Templates folder and open this file in a spreadsheet editor or even a text editor. Replace the current value for $initial_joystick_angle with the number Vector Calculator displays in the window that opened after you clicked `Solve`. The camera angle can be set to whatever you want; the Target Angle from Vector Calculator usually looks good.

! Lunakit is different

In the SMO practice server, use the tp command to teleport to where you think the up-down vault should start. Now, run the TSV-TAS script and see if you get a vault. Keep playing around with the teleport coordinates until you get a vault as close to the edge as possible.

Now, click the `Copy to Clipboard` button in Vector Calculator and paste the result to overwrite the commented row at the bottom of the template. Run the script again and the inputs that Vector Calculator solved will be performed in-game.

Part 3: TASing other jumps

You should now be able to TAS other types of jumps as well. The moonwalk triple and RCV templates are the next easiest to use. If you are familiar with TASing Super Mario Odyssey, you can also just write your own inputs leading up to the jump or modify the templates further to suit your needs.


Some other stuff to include

Trickjump TAS guide
initial_angle is the same initial angle as Vector Calculator, assuming you have the 0 Degree Axis set to X. (If you have 0 Degree Axis set to Z, it should be 90 minus the initial angle from Vector Caclulator.)
ledge_angle is the angle of the ledge. To find this angle, walk directly toward the edge (along a line perpendicular to the edge) and record the velocity angle from Practice Mod.
vector_direction should be -1 if the initial movement has a left vector, or 1 if it has a right vector.
moonwalk_rotation_strength is how rotated Mario is when he moonwalks. 60 results in the most possible distance, but if Mario does not get a triple jump, reduce this value.