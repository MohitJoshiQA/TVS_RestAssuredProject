package configuration;

import simulation.*;

public class SimulatorDispatcher {
    public static void simulate(String model, String executingInstance, int row, int column) throws Exception {
        simulate(model, executingInstance, row, column, -1); // default seqNo as -1 (or any invalid value)
    }

    public static void simulate(String model, String executingInstance, int row, int column, int seqNo) throws Exception {
        if (model == null) {
            throw new IllegalArgumentException("Model is null in simulate()");
        }

        switch (model.toUpperCase()) {
            case "U546":
                // If seqNo is provided (valid), use alternate simulation
                if (seqNo > 0) {
                    SimulatorU546ClusterApproval.U546ClusterApproval(executingInstance, model, row, column, seqNo);
                } else {
                    SimulatorU546.U546(executingInstance, model, row, column);
                }
                break;
            case "U546_HCC":

                if (seqNo > 0) {
                    System.out.println("inside seq........"+seqNo);
                    SimulatorU546HCClusterApproval.U546HCClusterApproval(executingInstance, model, row, column, seqNo);
                } else {
                    System.out.println("inside else........"+seqNo);
                    HccSimulation.U546_HCC(executingInstance, model, row, column);
                }
                break;
            case "U546_PCC":

                if (seqNo > 0) {
                    SimulatorU546PCClusterApproval.U546PCClusterApproval(executingInstance, model, row, column, seqNo);
                } else {
                    System.out.println("inside else........"+seqNo);
                    PccSimulation.U546_PCC(executingInstance, model, row, column);
                }
                break;
            case "U577":
                SimulatorU546.U546(executingInstance, model, row, column);
                break;
            case "GENERIC":
                // Only GENERIC requires seqNo
                SimulatorU546ClusterApproval.U546ClusterApproval(executingInstance, model, row, column, seqNo);
                break;
            default:
                throw new IllegalArgumentException("Unknown model for simulation: " + model);
        }
    }
}
