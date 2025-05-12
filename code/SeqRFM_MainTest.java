import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * Copyright (C), 2023, JNU
 * FileName: SeqRFM_MainTest
 * Author:   Yanxin Zheng
 * Date:     2023/9/15 23:25
 * Description: The main file of SeqRFM algorithm.
 */

public class SeqRFM_MainTest {
	public static PreLargeSet seqrfmMain(String input, String output, double delta, double minRecency,
                                  double minsupRatio,double minUtilityRatio,int timeSpan) throws IOException {
		AlgoSeqRFM algo = new AlgoSeqRFM();
        System.out.println("test dataset: " + input);
        System.out.println("minUtilityRatio: " + String.format("%.5f", minUtilityRatio));

		// BIBLE_sequence_utility MSNBC_spmf
        algo.runAlgorithm(input, output, delta, minUtilityRatio, minsupRatio, minRecency, timeSpan);
		// return the number of RFMs by running SeqRFM
		PreLargeSet res = algo.printStats();
		return res;
	}
}


