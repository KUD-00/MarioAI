package ch.idsia.scenarios;

import ch.idsia.agents.Agent;
import ch.idsia.agents.controllers.ForwardJumpingAgent;
import ch.idsia.agents.controllers.RLagent;
import ch.idsia.benchmark.tasks.BasicTask;
import ch.idsia.benchmark.tasks.SystemOfValues;
import ch.idsia.tools.EvaluationInfo;
import ch.idsia.tools.MarioAIOptions;

public final class Comp01 {
    public static void main(String[] args) {

        // ゲーム設定
        final MarioAIOptions marioAIOptions = new MarioAIOptions(args);
        // marioAIOptions.setScale2X(true);

        // エージェントの設定
        final Agent agent = new RLagent();
        marioAIOptions.setAgent(agent);

        // ステージの設定

        marioAIOptions.setVisualization(false); // ゲーム描画の有無
        marioAIOptions.setFPS(96);
        marioAIOptions.setLevelDifficulty(0); // 難易度
        marioAIOptions.setLevelType(1); // ステージ
        marioAIOptions.setLevelLength(256); // 長さ
        marioAIOptions.setEnemies("off"); // 敵の種類

        marioAIOptions.setDeadEndsCount(false); // 行き止まり
        marioAIOptions.setCannonsCount(true); // キラー
        marioAIOptions.setTubesCount(true); // 土管
        marioAIOptions.setGapsCount(true); // 穴
        marioAIOptions.setBlocksCount(true); // ブロック
        marioAIOptions.setCoinsCount(true); // コイン
        marioAIOptions.setHiddenBlocksCount(true); // 隠しブロック
        marioAIOptions.setHillStraightCount(false); // 丘

        int goalNum = 0;
        int N = 50;
        int fitnessSum = 0;

        for (int i = 100; i > N; i--) {
            int seed = i;
            marioAIOptions.setLevelRandSeed(seed); // ステージシード

            final BasicTask basicTask = new BasicTask(marioAIOptions);
            basicTask.setOptionsAndReset(marioAIOptions);
            basicTask.runSingleEpisode(1);

            // System.out.println(basicTask.getEvaluationInfo());
            EvaluationInfo e = basicTask.getEvaluationInfo();
            fitnessSum += e.computeWeightedFitness();

            System.out.println(i);
            System.out.println("適合度 :" + e.computeWeightedFitness());
            System.out.println("ゴール可否(1:ゴール): " + e.marioStatus);
            System.out.println("マリオ状態(0:small, 1:Large, 2:Fire): " + e.marioMode);
            System.out.println("敵衝突回数: " + e.collisionsWithCreatures);
            System.out.println("ステージ進捗: " + e.distancePassedCells + " / " + e.levelLength);
            System.out.println("経過時間(sec): " + e.timeSpent);
            System.out.println("残り時間(sec): " + e.timeLeft);
            System.out.println("コイン取得数: " + e.coinsGained + " / " + e.totalNumberOfCoins);
            System.out.println("隠しブロック発見数: " + e.hiddenBlocksFound + " / " + e.totalNumberOfHiddenBlocks);
            System.out.println("キノコ発見数: " + e.mushroomsDevoured + " / " + e.totalNumberOfMushrooms);
            System.out.println("フラワー発見数: " + e.flowersDevoured + " / " + e.totalNumberOfFlowers);
            System.out.println("敵討伐数: " + e.killsTotal + " / " + e.totalNumberOfCreatures);
            System.out.println("ファイア討伐数: " + e.killsByFire);
            System.out.println("甲羅討伐数: " + e.killsByShell);
            System.out.println("踏みつけ討伐数: " + e.killsByStomp);

            SystemOfValues sov = new SystemOfValues();
            System.out.println(sov.distance);
            System.out.println(sov.flowerFire);
            System.out.println(sov.win);
        }

        int averageFitnessSum = fitnessSum / 100;
        System.out.println("Average Fitnness Sum " + averageFitnessSum);
    }
}