package ch.idsia.agents.controllers;

import java.util.ArrayList;
import java.util.HashMap;

import ch.idsia.agents.Agent;
import ch.idsia.benchmark.mario.engine.sprites.Mario;
import ch.idsia.benchmark.mario.environments.Environment;


/**
 * 強化学習エージェント
 */
public class MyAgentRL extends BasicMarioAIAgent implements Agent {
	double ALPHA = 0.1;
	double EPSILON = 0.05;
	
	double oldPos = 0;
	double oldReward = 0.0;
	String oldState = "";
	boolean oldFlgJump = false;
	
	HashMap<String, double[]> stateValuesMap = new HashMap<String, double[]>();


	public MyAgentRL() {
		super("MyAgentRL");
		reset();
	}

	public MyAgentRL(HashMap<String, double[]> stateValuesMap) {
		super("MyAgentRL");
		this.stateValuesMap = stateValuesMap;
		reset();
	}


	public boolean[] getAction() {
		int c1 = getReceptiveFieldCellValue(marioEgoRow - 1, marioEgoCol - 1);
		int c2 = getReceptiveFieldCellValue(marioEgoRow - 1, marioEgoCol);
		int c3 = getReceptiveFieldCellValue(marioEgoRow - 1, marioEgoCol + 1);
		int c4 = getReceptiveFieldCellValue(marioEgoRow, marioEgoCol - 1);		
		int c5 = getReceptiveFieldCellValue(marioEgoRow, marioEgoCol);		
		int c6 = getReceptiveFieldCellValue(marioEgoRow, marioEgoCol + 1);
		int c7 = getReceptiveFieldCellValue(marioEgoRow + 1, marioEgoCol - 1);
		int c8 = getReceptiveFieldCellValue(marioEgoRow + 1, marioEgoCol);
		int c9 = getReceptiveFieldCellValue(marioEgoRow + 1, marioEgoCol + 1);
	
		String state = c1 + "_" + c2 + "_" + c3 + "_" + c4 + "_" + c5 + "_" + c6 + "_" + c7 + "_" + c8 + "_" + c9;

		System.out.println(state);
		// System.out.println("oldPos:" + oldPos + "	pos:" + marioFloatPos[0]);
		

		double[] values;
		boolean flgJump = false;

		// 状態行動価値を取得
		if (stateValuesMap.containsKey(state)) {
			values = stateValuesMap.get(state);
		} else {
			values = new double[2];
			stateValuesMap.put(state, values);
		}

		// 状態行動価値に基づいて行動（ジャンプ）を決定
		if (EPSILON <= Math.random()) {
			if (values[0] < values[1]) {
				flgJump = true;
			} else {
				flgJump = false;
			}

			System.out.println("  V Action" + "	" + values[0] + "	" + values[1]);

		// Epsilonの確率でランダムジャンプ
		} else {
			if (Math.random() < 0.5) {
				flgJump = true;
			} else {
				flgJump = false;
			}

			System.out.println("  Random Action");
		}

		action[Mario.KEY_JUMP] = flgJump;
		
		// 以下，状態行動価値Vの更新
		double reward = 0.0;

		// マリオが以前の状態よりも進んでいたら+1
		if (oldPos <= marioFloatPos[0]) {
			reward = 1.0;
		} else {
			reward = 0.0;
		}

		// 前回の状態行動に基づいてVを更新
		double[] oldValues = stateValuesMap.get(oldState);
		
		if (stateValuesMap.containsKey(oldState)) {
			if (oldFlgJump) {
				oldValues[1] = (1 - ALPHA) * oldValues[1] + ALPHA * reward;
			} else {
				oldValues[0] = (1 - ALPHA) * oldValues[0] + ALPHA * reward;
			}
			stateValuesMap.put(oldState, oldValues);
		}

		oldPos = marioFloatPos[0];
		oldState = state;
		oldFlgJump = flgJump;

		return action;
	}

	
	public void reset() {
		action = new boolean[Environment.numberOfKeys];
		action[Mario.KEY_RIGHT] = true;
	}

	public HashMap<String, double[]> getStateValuesMap() {
		return this.stateValuesMap;
	}
}