package ch.idsia.scenarios;

import ch.idsia.benchmark.tasks.BasicTask;
import ch.idsia.tools.MarioAIOptions;

public final class Main {
	public static void main(String[] args) {
		final MarioAIOptions marioAIOptions = new MarioAIOptions(args);
		int seed = 99;
		marioAIOptions.setLevelRandSeed(seed);
		final BasicTask basicTask = new BasicTask(marioAIOptions);
		basicTask.setOptionsAndReset(marioAIOptions);
		basicTask.doEpisodes(1, true, 1);
		
	}

}
