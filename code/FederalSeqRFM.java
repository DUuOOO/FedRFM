import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class FederalSeqRFM {
    //  deal with the dataset to run the federalRFM algo
    //		String fileName = "Syn_20K_rfm";
//        separateDataset(fileName);
    // SIGN_sequence_utility  kosarak10k  example_huspm2
    static String s = "test_";
    static long endTimestamp = 0;
    static long startTimestamp = 0;
    static BufferedWriter writer = null;

    static int cooperationNum =3;
    static double preDegree = 0.8;
    // when run the nopre use the "nopre_", while use the "T" to repeat the experiments for average results
    static String version = "T";

    // the minimum threshold of RFM pattern
    static double minRecency = 1.44;
    static double minsupRatio = 0.2;
    static double minUtilityRatio = 0.25;
    static int timeSpan = 60;
    static int parameter = timeSpan;

    // the minimum threshold of prelarge pattern
    static double prelargeDelta = 0.1;
    static double prelargeMinRecency = minRecency / cooperationNum * preDegree;
    static double prelargeMinsupRatio = minsupRatio * preDegree;
    static double prelargeMinUtilityRatio = minUtilityRatio * preDegree;


    static double totalUtility=0d;
    static int totalSequenceNum = 0;
    static int RFMs = 0;


    static Map<String, double[]> preLargePs = new HashMap<>();

    public static void main(String[] args) throws IOException {
        // deal with the dataset
//        separateDataset("test");
//        SeqRFM_MainTest.seqrfmMain("./dataset/e_shop_rfm.txt", "./output/e_shop_rfm_SeqRFM.txt", prelargeDelta, minRecency,minsupRatio, minUtilityRatio, timeSpan);
        // reset maximum

        // run the FedRFM algorithm
        AlgoFederalSeqRFM(s, prelargeMinRecency,prelargeMinsupRatio, prelargeMinUtilityRatio, timeSpan,1);

        // run the SeqRFM algorithm
//        AlgoSeqRFM("./dataset/" + s + ".txt", "./output/" + s + "_" + version + "_" + parameter + "_SeqRFM.txt",
//                prelargeDelta, prelargeMinRecency,prelargeMinsupRatio, prelargeMinUtilityRatio, timeSpan);

//        double[] pres = {1.00, 0.95, 0.90, 0.85, 0.80, 0.75, 0.70, 0.65};
//        for(double pre: pres){
//            AlgoSeqRFM("./dataset/" + "e_shop_rfm" + ".txt", "./output/" + "e_shop_rfm" + "_" + pre + "_SeqRFM.txt",
//                    0.009, 7000*pre,0.01*pre, 0.002*pre, 25);
//            AlgoSeqRFM("./dataset/" + "online_retail_rfm" + ".txt", "./output/" + "online_retail_rfm" + "_" + pre + "_SeqRFM.txt",
//                    0.009, 220*pre,0.05*pre, 0.01*pre, 100);
//            AlgoSeqRFM("./dataset/" + "Syn_40K_rfm" + ".txt", "./output/" + "Syn_40K_rfm" + "_" + pre + "_SeqRFM.txt",
//                    0.009, 40*pre,0.001*pre, 0.00002*pre, 25);
//        }

        // Contrast to the result of SeqRFM and FedRFM on the condition of no pre-large
//        double[] pres = {0.90, 0.80, 0.70};
//        for(double pre: pres){
//            AlgoFederalSeqRFM("e_shop_rfm_",7000.0*pre,0.01*pre, 0.002*pre, 25, pre);
//            AlgoFederalSeqRFM("online_retail_rfm_" , 220*pre,0.05*pre, 0.01*pre, 100, pre);
//            AlgoFederalSeqRFM("Syn_40K_rfm_",40*pre,0.001*pre, 0.00002*pre, 25, pre);
//        }
    }

    private static void AlgoSeqRFM(String input, String output, double delta, double R, double F, double M, int T) throws IOException {
        MemoryLogger.getInstance().reset();
        startTimestamp = System.currentTimeMillis();
        SeqRFM_MainTest.seqrfmMain(input, output, delta, R, F, M, T);
    }

    private static void AlgoFederalSeqRFM(String ss, double R, double F, double M, int T, double pre) throws IOException {
        // reset maximum
        MemoryLogger.getInstance().reset();
        startTimestamp = System.currentTimeMillis();
        totalUtility=0d;
        totalSequenceNum = 0;
        RFMs = 0;
        preLargePs = new HashMap<>();

        PreLargeSet[] PreLargeRes = new PreLargeSet[cooperationNum];
        for(int i=1; i<=cooperationNum; i++){
            String datasetString = ss + i;
            String input = "./dataset/" + datasetString + ".txt";
		    // the path for saving the patterns found
            String output = "./output/" + datasetString + "_" + version + "_" + pre + "_" + T + "_SeqRFM.txt";
            // has number lines RFM-patterns
            PreLargeRes[i-1] = SeqRFM_MainTest.seqrfmMain(input, output, prelargeDelta, R/cooperationNum, F, M, T);
            totalUtility += PreLargeRes[i-1].getTotalDbUtility();
            totalSequenceNum += PreLargeRes[i-1].getTotalDbSize();
        }

        // Create a single StringBuilder instance to be reused
        StringBuilder lineBuilder = new StringBuilder(1024); // Pre-allocate reasonable capacity
        
        // Process each file directly without storing in array
        for(int i=1; i<=cooperationNum; i++){
            String datasetString = ss + i;
            String readFile = "./output/" + datasetString + "_" + version + "_" + pre + "_" + T + "_SeqRFM.txt";
            
            try(BufferedReader br = Files.newBufferedReader(Paths.get(readFile))) {
                String line;
                while((line = br.readLine()) != null){
                    if(line.isEmpty()) continue;
                    
                    // Clear and reuse the StringBuilder
                    lineBuilder.setLength(0);
                    lineBuilder.append(line);
                    
                    // Parse the line using helper method
                    parseAndUpdatePreLargePattern(lineBuilder);
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }

        String output = "./output/" + ss + "_" + version + "_" + pre + "_" + T + "_FederalRFM.txt";
        writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output)));
        for(Map.Entry<String, double[]> entry : preLargePs.entrySet()){
            String preLargeP = entry.getKey();
            double[] values = entry.getValue();
            double util = values[0];
            double sup = values[1];
            double rec = values[2];
//            when run the FedRFM on the condition of pre-large, you should use the under if statement.
            if(rec >= minRecency && sup >= minsupRatio*totalSequenceNum && util >= minUtilityRatio*totalUtility){

//                when run the FedRFM algorithm without pre-large, you should use the under if statement.
//            if(rec >= R && sup >= F*totalSequenceNum && util >= M*totalUtility){
                writer.write(preLargeP); // + "   #UTIL: " + util + "   #SUP: " + sup + "   #RECENCY: " + String.format("%.2f", rec));
                writer.newLine();
                writer.flush();
                RFMs++;
            }
        }
        endTimestamp = System.currentTimeMillis();
        printFedRFMstats(R, F, M, T);
    }

    /**
     * Helper method to parse a line and update preLargePs map
     * @param lineBuilder StringBuilder containing the line to parse
     */
    private static void parseAndUpdatePreLargePattern(StringBuilder lineBuilder) {
        int firstHashIndex = lineBuilder.indexOf("#");
        if (firstHashIndex <= 4) return; // Invalid line format
        
        String preLargeP = lineBuilder.substring(0, firstHashIndex - 4).strip();
        lineBuilder.delete(0, firstHashIndex + 1);
        
        // Parse utility
        int utilStart = lineBuilder.indexOf(":") + 1;
        int utilEnd = lineBuilder.indexOf("#", utilStart) - 3;
        if (utilStart < 0 || utilEnd < 0) return;
        double util = Double.parseDouble(lineBuilder.substring(utilStart, utilEnd).strip());
        
        // Parse support
        lineBuilder.delete(0, utilEnd + 4);
        int supStart = lineBuilder.indexOf(":") + 1;
        int supEnd = lineBuilder.indexOf("#", supStart) - 3;
        if (supStart < 0 || supEnd < 0) return;
        double sup = Double.parseDouble(lineBuilder.substring(supStart, supEnd).strip());
        
        // Parse recency
        lineBuilder.delete(0, supEnd + 4);
        int recStart = lineBuilder.indexOf(":") + 1;
        if (recStart < 0) return;
        double rec = Double.parseDouble(lineBuilder.substring(recStart).strip());
        
        // Update preLargePs map
        preLargePs.compute(preLargeP, (key, oldValues) -> {
            if (oldValues == null) {
                return new double[]{util, sup, rec};
            } else {
                oldValues[0] += util;
                oldValues[1] += sup;
                oldValues[2] += rec;
                return oldValues;
            }
        });
    }

    private static void printFedRFMstats(double R, double F, double M, int T) throws IOException {
        System.out.println("===========  FederalRFM ALGORITHM - STATS =========");
        writer.write("===========  FederalRFM ALGORITHM - STATS =========" + "\n");

        System.out.println("alpha: " + R);
        writer.write("alpha: " + R + "\n");

        System.out.println("beta: " + F);
        writer.write("beta: " + F + "\n");

        System.out.println("gamma: " + M);
        writer.write("gamma: " + M + "\n");

        System.out.println("timeSpan: " + T);
        writer.write("timeSpan: " + T + "\n");

        System.out.println("Pre-Large Patterns size: " + preLargePs.size());
        writer.write("Pre-Large Patterns size: " + preLargePs.size() + "\n");

        System.out.println("RFM-Patterns size: " + RFMs);
        writer.write("RFM-Patterns size: " + RFMs + "\n");

        System.out.println("Max memory: " + MemoryLogger.getInstance().getMaxMemory() + " MB");
        writer.write("Max memory: " + MemoryLogger.getInstance().getMaxMemory() + " MB" + "\n");

        System.out.println("Total time: " + (endTimestamp - startTimestamp)/1000.0 + " s");
        writer.write("Total time: " + (endTimestamp - startTimestamp)/1000.0 + " s" + "\n");
        
        writer.flush();
        writer.close();
    }


    private static void separateDataset(String fileName) throws IOException {
        String inputFile = "./dataset/" + fileName + ".txt";
        String outputFile1 = "./dataset/" + fileName + "_1.txt";
        String outputFile2 = "./dataset/" + fileName + "_2.txt";
        String outputFile3 = "./dataset/" + fileName + "_3.txt";

        try {
            // 读取文件内容
            List<String> lines = Files.readAllLines(Paths.get(inputFile));
            int totalLines = lines.size();

            // 计算每部分的大小
            int partSize = totalLines / 3;
            int remainder = totalLines % 3; // 处理无法整除的情况

            // 分割内容
            List<String> part1 = lines.subList(0, partSize + (remainder > 0 ? 1 : 0));
            List<String> part2 = lines.subList(part1.size(), part1.size() + partSize + (remainder > 1 ? 1 : 0));
            List<String> part3 = lines.subList(part1.size() + part2.size(), totalLines);

            // 保存到新文件
            saveToFile(outputFile1, part1);
            saveToFile(outputFile2, part2);
            saveToFile(outputFile3, part3);

            System.out.println("文件分割完成！结果保存在 " + outputFile1 + ", " + outputFile2 + ", " + outputFile3);
        } catch (IOException e) {
            System.err.println("文件操作出错: " + e.getMessage());
        }
    }
    // 保存内容到文件
    private static void saveToFile(String fileName, List<String> content) throws IOException {
        Files.write(Paths.get(fileName), content);
    }
}
