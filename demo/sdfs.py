import os, sys
import traci
import traci.constants as tc

if "SUMO_HOME" in os.environ:
    tools = os.path.join(os.environ["SUMO_HOME"], "tools")
    sys.path.append(tools)
else:
    sys.exit("Please declare environment variable 'SUMO_HOME'")


traci.start(["sumo", "-c", "demo.sumocfg"])
traci.vehicle.subscribe("0", (tc.VAR_ROAD_ID, tc.VAR_LANEPOSITION))
print(traci.vehicle.getSubscriptionResults("0"))
for step in range(500):
    print("step", step)
    traci.simulationStep()
    print(traci.vehicle.getPosition("0"))
traci.close()
