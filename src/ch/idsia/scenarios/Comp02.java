package ch.idsia.scenarios;

import ch.idsia.agents.Agent;
import ch.idsia.agents.controllers.ForwardJumpingAgent;
import ch.idsia.agents.controllers.RLagent;
import ch.idsia.benchmark.tasks.BasicTask;
import ch.idsia.benchmark.tasks.SystemOfValues;
import ch.idsia.tools.EvaluationInfo;
import ch.idsia.tools.MarioAIOptions;

public class Comp02 {
    public static void main(String[] args) {

        // ゲーム設定
        final MarioAIOptions marioAIOptions = new MarioAIOptions(args);
        // marioAIOptions.setScale2X(true);

        // エージェントの設定
        final Agent agent = new RLagent();
        marioAIOptions.setAgent(agent);

        // ステージの設定
        marioAIOptions.setVisualization(true); // ゲーム描画の有無
        marioAIOptions.setGameViewer(false);
        marioAIOptions.setFPS(12);
        marioAIOptions.setLevelDifficulty(0); // 難易度
        marioAIOptions.setLevelType(0); // ステージ
        marioAIOptions.setLevelLength(256); // 長さ
        marioAIOptions.setEnemies("off"); // 敵の種類

        marioAIOptions.setDeadEndsCount(false); // 行き止まり
        marioAIOptions.setCannonsCount(true); // キラー
        marioAIOptions.setTubesCount(true); // 土管
        marioAIOptions.setGapsCount(true); // 穴
        marioAIOptions.setBlocksCount(true); // ブロック
        marioAIOptions.setCoinsCount(true); // コイン
        marioAIOptions.setHiddenBlocksCount(true); // 隠しブロック
        marioAIOptions.setHillStraightCount(true); // 丘

        int seed = 2;
        marioAIOptions.setLevelRandSeed(seed); // ステージシード

        final BasicTask basicTask = new BasicTask(marioAIOptions);
        basicTask.setOptionsAndReset(marioAIOptions);
        basicTask.runSingleEpisode(1);
        System.out.println(basicTask.getEvaluationInfo()); // ゲーム終了後の評価情報を出力
    }
}
