package com.vectorcalculator;

import java.util.Vector;

import javax.swing.SwingWorker;

import com.vectorcalculator.Properties.HctType;
import com.vectorcalculator.Properties.Mode;
import com.vectorcalculator.Properties.TurnDuringDive;
import com.vectorcalculator.VectorCalculator.Parameter;

class CalculateThread extends SwingWorker<Boolean, String> {

    Properties p = Properties.p;

  	@Override
    public Boolean doInBackground(){
        VectorCalculator.calculating = true;

        try {
            if (p.targetCoordinatesGiven) {
                p.targetAngle = VectorCalculator.targetCoordinatesToTargetAngle();
            }
            boolean optimalDistanceMotion = p.initialMovementName.equals("Optimal Distance Motion");
            if (p.mode == Mode.SOLVE || p.mode == Mode.SOLVE_DIVES) {
                if (p.initialMovementName.equals("Optimal Distance RCV")) {
                    publish("Error: Optimal Distance RCV not supported in this mode");
                    return false;
                }

                boolean diveSolver = p.mode == Mode.SOLVE_DIVES;
                TurnDuringDive oldTurnDuringDive = p.diveTurn;
                SolverInterface solver;
                if (optimalDistanceMotion) {
                    p.initialMovementName = "Triple Jump";
                    p.framesJump = 10;
                    VectorCalculator.initialMovement = new Movement(p.initialMovementName, p.initialHorizontalSpeed, p.framesJump);
                    SolverInterface solverTJ = VectorCalculator.runSolver(diveSolver, false);
                    int[][] tjMidairs = p.midairs;
                    //solverTJ.solve(p.durationSearchRange);
                    p.initialMovementName = "Motion Cap Throw RCV";
                    VectorCalculator.initialMovement = new Movement(p.initialMovementName, p.initialHorizontalSpeed, p.framesJump);
                    SolverInterface solverRCV = VectorCalculator.runSolver(diveSolver, false);
                    int[][] rcvMidairs = p.midairs;
                    //solverRCV.solve(Math.min(p.durationSearchRange, 3));
                    p.initialMovementName = "Sideflip";
                    VectorCalculator.initialMovement = new Movement(p.initialMovementName, p.initialHorizontalSpeed, p.framesJump);
                    SolverInterface solverSideflip = VectorCalculator.runSolver(diveSolver, false);
                    int[][] sideflipMidairs = p.midairs;
                    //solverSideflip.solve(p.durationSearchRange);
                    // Debug.println("TJ: " + maximizerTJ.bestDisp);
                    // Debug.println("RC: " + maximizerRC.bestDisp);
                    // Debug.println("SF: " + maximizerSideflip.bestDisp);
                    if (solverTJ.getBestDisp() > solverRCV.getBestDisp() && solverTJ.getBestDisp() > solverSideflip.getBestDisp()) {
                        solver = solverTJ;
                        p.initialMovementName = "Triple Jump";
                        VectorCalculator.addPreset(tjMidairs);
                    }
                    else if (solverRCV.getBestDisp() > solverTJ.getBestDisp() && solverRCV.getBestDisp() > solverSideflip.getBestDisp()) {
                        solver = solverRCV;
                        p.initialMovementName = "Motion Cap Throw RCV";
                        VectorCalculator.addPreset(rcvMidairs);
                    }
                    else {
                        solver = solverSideflip;
                        p.initialMovementName = "Sideflip";
                        VectorCalculator.addPreset(sideflipMidairs);
                    }
                    VectorCalculator.retest(solver, diveSolver);
                }
                else if (p.initialMovementName.contains("Crouch Roll")) {
                    p.initialMovementName = "Crouch Roll";
                    VectorCalculator.initialMovement = new Movement(p.initialMovementName);
                    SolverInterface solverCR = VectorCalculator.runSolver(diveSolver, false);
                    p.initialMovementName = "Crouch Roll (No Vector)";
                    VectorCalculator.initialMovement = new Movement(p.initialMovementName);
                    SolverInterface solverCRNV = VectorCalculator.runSolver(diveSolver, false);
                    if (solverCR.getBestDisp() > solverCRNV.getBestDisp()) {
                        solver = solverCR;
                        p.initialMovementName = "Crouch Roll";
                    } else {
                        solver = solverCRNV;
                        p.initialMovementName = "Crouch Roll (No Vector)";
                    }
                    VectorCalculator.setPropertiesRow(Parameter.initial_movement);
                    VectorCalculator.initialMovement = new Movement(p.initialMovementName);
                    VectorCalculator.retest(solver, diveSolver);
                }
                else if (p.initialMovementName.contains("Roll Boost")) {
                    p.initialMovementName = "Roll Boost";
                    VectorCalculator.initialMovement = new Movement(p.initialMovementName);
                    SolverInterface solverRB = VectorCalculator.runSolver(diveSolver, false);
                    p.initialMovementName = "Roll Boost (No Vector)";
                    VectorCalculator.initialMovement = new Movement(p.initialMovementName);
                    SolverInterface solverRBNV = VectorCalculator.runSolver(diveSolver, false);
                    if (solverRB.getBestDisp() > solverRBNV.getBestDisp()) {
                        solver = solverRB;
                        p.initialMovementName = "Roll Boost";
                    } else {
                        solver = solverRBNV;
                        p.initialMovementName = "Roll Boost (No Vector)";
                    }
                    VectorCalculator.setPropertiesRow(Parameter.initial_movement);
                    VectorCalculator.initialMovement = new Movement(p.initialMovementName);
                    VectorCalculator.retest(solver, diveSolver);
                }
                else {
                    solver = VectorCalculator.runSolver(diveSolver, true);
                }

                if (VectorCalculator.cancelCalculating) {
                    cancelCalculating();
                    return false;
                }

                VectorCalculator.addPreset(p.midairs);
                VectorMaximizer maximizer = solver.getMaximizer();
                if (solver.solveSuccess() && maximizer != null) {
                    if (p.firstCTIndex >= 0) {
                        if (maximizer.ctType == Movement.TT || maximizer.ctType == Movement.TTU || maximizer.ctType == Movement.TTD || maximizer.ctType == Movement.TTL || maximizer.ctType == Movement.TTR)
                            p.midairs[p.firstCTIndex][0] = VectorCalculator.TT;
                        else if (maximizer.ctType == Movement.CT)
                            p.midairs[p.firstCTIndex][0] = VectorCalculator.CT;
                        else
                            p.midairs[p.firstCTIndex][0] = VectorCalculator.MCCT;
                    }
                    VectorCalculator.addPreset(p.midairs);
                    VectorCalculator.setPropertiesRow(Parameter.dive_angle);
                    VectorCalculator.setPropertiesRow(Parameter.dive_deceleration);
                    //System.out.println("Possible: " + possible + " " + maximizer.ctType);
                    VectorDisplayWindow.generateData(maximizer);
                    VectorDisplayWindow.display();
                }
                if (optimalDistanceMotion) {
                    VectorCalculator.setProperty(Parameter.initial_movement, "Optimal Distance Motion");
                    p.durationFrames = false;
                }
                if (p.hctType != HctType.CUSTOM) { //reload preset so that hct angle gets reset to 60 degrees for next use of the calculator
                    VectorCalculator.setProperty(Parameter.hct_type, p.hctType.name);
                }
                VectorCalculator.setProperty(Parameter.dive_turn, oldTurnDuringDive.displayName); //set back to "Test Both" if that is the setting
            }
            else if (p.mode == Mode.CALCULATE) {
                VectorMaximizer maximizer = null;
                if (p.initialMovementName.equals("Optimal Distance Motion")) {
                    p.initialMovementName = "Triple Jump";
                    p.framesJump = 10;
                    VectorCalculator.initialMovement = new Movement(p.initialMovementName, p.initialHorizontalSpeed, p.framesJump);
                    VectorMaximizer maximizerTJ = VectorCalculator.calculate();
                    p.initialMovementName = "Optimal Distance RCV";
                    VectorCalculator.initialMovement = new Movement(p.initialMovementName, p.initialHorizontalSpeed, p.framesJump);
                    VectorMaximizer maximizerRC = VectorCalculator.calculate();
                    p.initialMovementName = "Sideflip";
                    VectorCalculator.initialMovement = new Movement(p.initialMovementName, p.initialHorizontalSpeed, p.framesJump);
                    VectorMaximizer maximizerSideflip = VectorCalculator.calculate();
                    Debug.println("TJ: " + maximizerTJ.bestDisp);
                    Debug.println("RC: " + maximizerRC.bestDisp);
                    Debug.println("SF: " + maximizerSideflip.bestDisp);
                    if (maximizerTJ != null && maximizerRC != null && maximizerSideflip != null) {
                        if (maximizerTJ.bestDisp > maximizerRC.bestDisp && maximizerTJ.bestDisp > maximizerSideflip.bestDisp) {
                            maximizer = maximizerTJ;
                        }
                        else if (maximizerRC.bestDisp > maximizerTJ.bestDisp && maximizerRC.bestDisp > maximizerSideflip.bestDisp) {
                            maximizer = maximizerRC;
                        }
                        else {
                            maximizer = maximizerSideflip;
                        }
                    }
                    p.initialMovementName = "Optimal Distance Motion";
                }
                else {
                    maximizer = VectorCalculator.getMaximizer();
                    if (maximizer != null) {
                        maximizer.maximize();
                        if (maximizer.hasError()) {
                            publish(maximizer.error);
                            return false;
                        }
                        maximizer.recalculateDisps(true);
                        maximizer.adjustToGivenAngle();
                    }
                }
                if (maximizer != null && maximizer.bestDisp > 0) {
                    VectorDisplayWindow.generateData(maximizer);
                    VectorDisplayWindow.display();
                }
                Debug.println();
            }

            if (VectorCalculator.cancelCalculating) {
                cancelCalculating();
                return false;
            }

            //for all calculate modes
            Properties.p_calculated = new Properties();
            Properties.copyAttributes(p, Properties.p_calculated);
            UndoManager.recordState(false);
        }
        catch (Exception ex) {
            VectorCalculator.loadProperties(UndoManager.currentState(), false);
            VectorCalculator.calculateVector.setText(p.mode.name);
        }
        finally {
            VectorCalculator.calculating = false;
            VectorCalculator.cancelCalculating = false;

            VectorCalculator.calculateVector.setText(p.mode.name);

            VectorCalculator.refreshPropertiesRows(VectorCalculator.getRowParams(), true);
            VectorCalculator.addPreset(p.midairs);
        }
        return true;
    }

    @Override
    protected void process(java.util.List<String> chunks) {
        if (!VectorCalculator.calculating || VectorCalculator.cancelCalculating) {
            return;
        }
        String message = chunks.getLast();
        //for (String message : chunks) {
            if (message.equals("CLEAR")) {
                VectorCalculator.errorMessage.setText("");
                VectorCalculator.progressMessage.setText("");
            } else if (message.startsWith("Error:")) {
                VectorCalculator.errorMessage.setText(message);
            } else {
                VectorCalculator.progressMessage.setText(message);
            }
            //VectorCalculator.f.repaint();
        //}
    }

    public void publishText(String message) {
        publish(message);
    }

    public void cancelCalculating() {
        VectorCalculator.loadProperties(UndoManager.currentState(), false);
        VectorCalculator.calculateVector.setText(p.mode.name);
        VectorCalculator.calculating = false;
    }

    @Override
    public void done() {
        VectorCalculator.setProgressText(VectorCalculator.progressText);
        if (VectorCalculator.cancelCalculating) {
            VectorCalculator.setErrorText("Calculations cancelled");
        }
        else {
            VectorCalculator.setErrorText(VectorCalculator.errorText);
        }
    }
}