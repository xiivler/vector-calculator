# Vector Calculator Optimization Explanation
The ``Solver`` is the part of the program that determines the optimal intitial movement duration, midair durations, cap throw type, edge cap bounce angle, and so on.

The ``Calculator`` is the part of the program the part that finds the best angles to hold during the jump. The ``Solver`` uses the ``Calculator`` repeatedly to find the best possible durations.

## Solver
### Part 1: Finding Ballpark Durations
1. The solver makes the durations of the initial movement and many midairs too long, so it can subtract frames later. For all other midairs, the solver uses preset values.
2. The solver reduces the duration of the initial movement until the first GP and cap bounce are above the ground/liquid (if applicable)
3. The solver calculates the efficiencies of each frame of the initial movement and midairs
4. The solver shortens the duration of the movement with the least efficient final frame (assuming that frame can be removed) until the second GP is above the ground/liquid (if applicable) and the end of the jump is above the target Y position, taking into account the maximum upwarp (note that the last dive does is not allowed to be shortened until the second GP is above the ground/liquid, to make sure it is long enough)

At this point, the durations of each piece of movement are more or less what they should be, but they will need to be tweaked to optimize the jump.

### Part 2: Cap Bounce Testing
The solver tests all permutations of cap throw and dive lengths that are within the ``Duration Search Range`` of the durations from Part 1 to see if they can result in a cap bounce. For each test, it iterates through the permissible throw types, testing a range of ``Edge Cap Bounce Angles``. If no throw type works, the solver tries again with a smaller ``First Cap Throw Vector Angle`` (i.e., undervectoring the cap throws a bit). The solver keeps iterating until it finds parameters that result in a cap bounce or fails to do so. It records the results in a table for later use.

### Part 3: Duration Testing
1. The solver loops through many different permutations of durations for the initial movement and midairs, testing how much distance each one yields using the ``Calculator``. This part is optimized for speed, so it doesn’t do as good of a job optimizing as the program will do later. It keeps track of all permutations that are within some range of the best one for more rigorous testing later. The durations tested are the within the ``Duration Search Range`` of the ballpark durations generated in Part 1. Many permuations don’t need to be tested because Mario would land on the ground / get burned from lava / the permutation is obviously ineffcient / Mario would be too high or too low at the end of the jump / etc.
2. The solver tests the best results from part 1 using the ``Calculator`` with a higher degree of optimization to find which actually yields the most distance.

## Calculator
The calculator optimizes the following parameters:
1. The throw angle for the cap throw before the cap bounce
2. The throw angle for the final cap throw (so that Mario ends up diving in the direction of the entire jump)
3. The joystick angles for the falling part of a homing cap throw
* If the homing cap throw is long enough, Vector Calculator will vector the falling for a number of frames, then hold 180 degrees away from that for a number of frames to redirect Mario's velocity a bit in the other direction so that the rainbow spin angle is good
* If the homing cap throw is short, Vector Calculator will instead just find a good joystick angle to hold to set up the rainbow spin angle without worrying about vectoring strongly
4. The number of yank frames for the initial movement, rainbow spin, and cap bounce (frames in which the joystick is held directly backward to set up the next movement's initial velocity angle)
* If the initial movement is not vectorable (ex. a long jump), Vector Calculator will instead hold to the side near the end of the movement to redirect Mario's velocity angle without losing speed

Generally, the calculator vectors as strongly as possible (90 degrees from initial velocity angle), unless Mario can accelerate forward, in which case it first holds in the direction of the initial velocity angle until the forward velocity cap is reached. On the last frame of accelerating up to the forward velocity cap, the calculator may be able to vector a bit while still maxing out the forward velocity if holding forward would provide more acceleration than is needed to reach the cap. This latest version of Vector Calculator even vectors the falling part of final cap throws, and in moon gravity, the falling part of the cap throw before the cap bounce.

There are some exceptions in which case the calculator does not vector as strongly as possible, including movement that needs to reach a certain final facing angle at the end. For cap throws before cap bounces in 1P, the calculator starts off holding 90 degrees, but rotates in small increments opposite the direction of the movement to make Mario rotate less without losing significant distance. Then, it has Mario perform a fast turnaround at the end. In other situations, the calculator will simply vector as strongly as possible before performing a fast turnaround, or use the counter-rotation technique described above to simply rotate the needed amount without any need for a fast turnaround.

Dives before cap bounces are optimized by turning them to the side (holding 90 degrees from the dive’s current rotation each frame). This is so that the dive can start moving in a more forward direction (thereby gaining more forward distance) while still turning to the side enough by the end to set up the cap bounce vector.

RCVs are optimized to maximize how long they can spend traveling in the direction of the target angle (the direction of the entire jump). The calculator determines what velocity angle the RCV needs to end with so that the target angle matches the velocity angle of the RCV before it hooks.

If a reverse bonk is part of the jump, the program has the ability to calculate how large of an upwarp is possible. It also finds movement for Cappy so that he ends up in the right place so that the reverse bonk occurs at the desired angle.