import { useState } from "react";
import { setSimulationConfig } from "../services/simulationService";

const SimulationPanel = () => {
  const [networkFail, setNetworkFail] = useState(false);
  const [forceFail, setForceFail] = useState(false);
  const [delay, setDelay] = useState(0);
  const [message, setMessage] = useState("");

  const applyConfig = () => {
    const config = {
      networkFail,
      forceFail,
      delay: Number(delay),
    };
    
    setSimulationConfig(config);
    
    if (forceFail || networkFail || delay > 0) {
      setMessage(" Failure simulation added. Keep it off for real transactions.");
    } else {
      setMessage(" Simulation disabled. Real transactions will work normally.");
    }
    
    setTimeout(() => setMessage(""), 3000);
  };

  return (
    <div className="card">
      <h3>Failure Simulation (Dev Mode)</h3>

      <label>
        <input
          type="checkbox"
          checked={networkFail}
          onChange={(e) => setNetworkFail(e.target.checked)}
        />
        Network Fail
      </label>

      <br />

      <label>
        <input
          type="checkbox"
          checked={forceFail}
          onChange={(e) => setForceFail(e.target.checked)}
        />
        Force Failure
      </label>

      <br /><br />

      <input
        type="number"
        placeholder="Delay (ms)"
        value={delay}
        onChange={(e) => setDelay(e.target.value)}
      />

      <br /><br />

      <button onClick={applyConfig}>
        Apply Simulation
      </button>

      {message && <p style={{ marginTop: "10px", fontSize: "12px" }}>{message}</p>}
    </div>
  );
};

export default SimulationPanel;