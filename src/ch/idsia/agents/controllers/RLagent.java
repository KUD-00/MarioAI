package ch.idsia.agents.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import javax.management.QueryEval;

import ch.idsia.agents.Agent;
import ch.idsia.benchmark.mario.engine.sprites.Mario;
import ch.idsia.benchmark.mario.environments.Environment;

public class RLagent extends BasicMarioAIAgent implements Agent {
    private int frame = 1;
    Properties properties = new Properties();
    private HashMap<String, HashMap<String, Double>> QTable = new HashMap<String, HashMap<String, Double>>();
    private String lastEnvString = "";
    private boolean[] lastAction = {false, true, false, false, false, false};
    private boolean isInit = false;
    private boolean[] noAction = {false, false, false, false, false, false};
    private boolean isJump = false;
    private HashMap<String, Double> lastStatusMap = new HashMap<String, Double>() {
        {
            put("MarioStatus", 2.0);
            put("Position", 320.0);
        }
    };
    private HashMap<String, Double> rewardMap = new HashMap<String, Double>() {
        {
            put("a", 10.0);
            put("c", 100.0);
        }
    };

    public RLagent() {
        super("RLagent");
        reset();
    }

    public RLagent(HashMap<String, HashMap<String, Double>> QTable) {
        super("RLagent");
        this.QTable = QTable;
        reset();
    }

    // return a rectangle environment two dimensional array
    private int[][] getTwoDimensionalEnv(int startRow, int startCol, int endRow, int endCol) {
        int[][] environment = new int[endRow - startRow + 1][endCol - startCol + 1];
        for (int row = startRow; row <= endRow; row++) {
            for (int col = startCol; col <= endCol; col++) {
                environment[row - startRow][col - startCol] = getReceptiveFieldCellValue(row, col);
            }
        }
        return environment;
    }

    // transform the environment array to string
    private String toEnvString(int[][] TwoDimEnv) {
        String envString = "";
        for (int[] i : TwoDimEnv) {
            for (int j : i) {
                envString = envString + Integer.toString(j);
            }
        }
        return envString;
    }

    // bring getTwoDimensionalEnv and toEnvString together
    private String getEnvString(int startRow, int startCol, int endRow, int endCol) {
        return toEnvString(getTwoDimensionalEnv(startRow, startCol, endRow, endCol));
    }

    private String action2string(boolean[] action) {
        String str = "";
        for (boolean b : action) {
            if(b == false) str += "0";
            else str += "1";
        }
        return str;
    }

    private boolean[] string2action(String s) {
        boolean[] action = {false, false, false, false, false, false};
        for(int i = 0; i < s.length(); i++){
            String str = Character.toString(s.charAt(i));
            if (str.equals("1")) {
                action[i] = true;
            }
        }
        return action;
    }

    private void updateQTable(String envString, boolean[] action, Double qValue) {
        if (qValue < 30) {return;}
        if(!QTable.containsKey(envString)) {
            HashMap<String, Double> temp = new HashMap<String, Double>();
            temp.put(action2string(action), qValue);
            QTable.put(envString, temp);
        } else if(!QTable.get(envString).containsKey(action) || QTable.get(envString).get(action) < qValue){
            QTable.get(envString).put(action2string(action), qValue);
        }
    }

    // get random action[]
    private boolean[] getRandomAction() {
        // double[] weight = { 0.8, , 0, 1, 1, 0 }; // left, right, shagamu?, jump, dash/fire, up
        boolean[] action = { false, false, false, false, false, false };

        for (int i = 0; i < 6; i++) {
            if(i == 2 || i == 5) {
                continue;
            }
            Random rand = new Random(); // instance of random class
            double double_random = rand.nextDouble();
            if (double_random > 0.5) {
                action[i] = true;
            }
        }
        return action;
    }

    private boolean epsilonGreedyExplorationStrategy() {
        double epsilon = 0.2;
        return true;
    }

    private double bellmanEquation(String state, String stateNext, boolean[] action, double reward) {
        double learningRate = 0.3;
        double gamma = 0.1;
        if (QTable.containsKey(stateNext) && QTable.containsKey(state) && QTable.get(state).containsKey(action)) {
            double bestActionQValue = QTable.get(stateNext).get(action2string(getBestAction(stateNext)));
            double newQValue = (1 - learningRate) * QTable.get(state).get(action) +
                    learningRate * (reward + gamma * bestActionQValue);
            return newQValue;
        } else {
            return reward;
        }
    }

    private boolean[] getBestAction(String envString) {
        if (QTable.get(envString) == null) {
            return getRandomAction();
        }

        Double temp = Double.NEGATIVE_INFINITY;
        String bestAction = "000000";
        for (String actionString : QTable.get(envString).keySet()) {
            if (QTable.get(envString).get(actionString) > temp) {
                temp = QTable.get(envString).get(actionString);
                bestAction = actionString;
            }
        }
        return string2action(bestAction);
    }

    private HashMap<String, Double> getStatusMap(){
        HashMap<String, Double> statusMap = new HashMap<String, Double>();
        statusMap.put("MarioStatus", marioMode * 1.0);
        statusMap.put("Position", marioFloatPos[0] * 1.0);
        return statusMap;
    }

    private double getReward(HashMap<String, Double> lastStatusMap, HashMap<String, Double> nowStatusMap) {
        double QValue = 0.0;
        if (lastStatusMap.get("MarioStatus") > nowStatusMap.get("MarioStatus")) {
            QValue -= 100;
        } else if (lastStatusMap.get("MarioStatus") < nowStatusMap.get("MarioStatus")) {
            QValue += 100;
        }

        double nowPostion = nowStatusMap.get("Position");
        double lastPostion = lastStatusMap.get("Position");
        double diff = (nowPostion - lastPostion) * 10;

        if(nowPostion == lastPostion) QValue -= 10;

        QValue += diff;

        return QValue;
    }

    private void initQTable() {
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream("data.properties"));
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        for (String key : properties.stringPropertyNames()) {
            String[] pair = key.split("@", 2);
            if (!QTable.containsKey(pair[0])) {
                HashMap<String, Double> map = new HashMap<>();
                Object obj = properties.get(key);
                Double value = Double.valueOf(obj.toString()).doubleValue();
                map.put(pair[1], value);
                QTable.put(pair[0], map);
            } else {
                QTable.get(pair[0]).put(pair[1], Double.valueOf(properties.get(key).toString()));
            }
        }
    }

    private boolean[] releaseJumpButton(boolean[] action) {
        if (isJump && isMarioOnGround && action[3] == true) {
            action[3] = false;
            isJump = false;
        }
        return action;
    }

    private boolean[] init() {
        initQTable();
        boolean[] goRight = { false, true, false, false, false, false };
        while(!isMarioOnGround) {
            return this.noAction;
        }
        lastEnvString = getEnvStringRoutine();
        lastAction = goRight;
        lastStatusMap = getStatusMap();
        isInit = true;
        return goRight;
    }
    
    private void writeQTable() {
        Properties properties = new Properties();

        for (Map.Entry<String, HashMap<String, Double>> entry : QTable.entrySet()) {
            for (Map.Entry<String, Double> pair : entry.getValue().entrySet()) {
                String temp = entry.getKey() + "@" + pair.getKey().toString();
                properties.put(temp, pair.getValue().toString());
            }
        }

        try {
            properties.store(new FileOutputStream("data.properties"), null);
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    private String getEnvStringRoutine() {
        return getEnvString(marioEgoRow - 3, marioEgoCol - 1, marioEgoRow + 1, marioEgoCol + 2);
    }

    public boolean[] getAction() {
        if(!isInit) return init();

        if (frame % 20 == 0) writeQTable();
        
        if (frame % 4 != 0) {
            frame += 1;
            return this.lastAction;
        }

        String nowEnvString = getEnvStringRoutine();

        double reward = getReward(lastStatusMap, getStatusMap());

        double Q_Value = bellmanEquation(lastEnvString,nowEnvString,lastAction,reward);

        updateQTable(lastEnvString, action, Q_Value);

        Random rand = new Random();
        double double_random = rand.nextDouble();
        if (double_random < 0.2) { // epsilon
            //System.out.println("Random action");
            action = getRandomAction();
        } else {
            //System.out.println("Best action");
            this.action = getBestAction(nowEnvString);
        }

        if (action[3] == true && !isMarioOnGround) {
            isJump = true;
        }

        releaseJumpButton(action);

        this.lastEnvString = nowEnvString;
        this.lastAction = this.action;
        this.lastStatusMap = getStatusMap();
        this.frame++;
        return this.action;
    }
}