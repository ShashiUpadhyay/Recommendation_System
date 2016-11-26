import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Recommendation_System {

	static BufferedReader bufferreader = null;
	static BufferedWriter bufferedWriter = null;
	static int[][] input_user_matrix = new int[Constants.TOTAL_USER + 1][Constants.TOTAL_ITEMS + 1];
	static int[][] output_user_matrix = new int[Constants.TOTAL_USER + 1][Constants.TOTAL_ITEMS + 1];
	static Map<Integer, List<Integer>> similar_interest_sharing_users = new HashMap<>();
	static Map<Integer, HashMap<Integer, Double>> user1_user2_coefficient_mapping = new HashMap<>();
	static HashMap<Integer, Double> usercoefficientmapping = new HashMap<>();
	static double dividend = Constants.ZEROPOINTZERO;
	static double user1value = Constants.ZEROPOINTZERO;
	static double user2value = Constants.ZEROPOINTZERO;
	static double weight = Constants.ZEROPOINTZERO;
	static double user1meancoefficient = Constants.ZEROPOINTZERO;
	static double user2meancoefficient = Constants.ZEROPOINTZERO;
	static double ratingrecommendation;

	public static void main(String[] args) {

		try {
			bufferreader = new BufferedReader(new FileReader(new File(Constants.INPUT_FILE)));

			String input_line = null;
			Scanner scanner = null;
			
			System.out.println("");
			System.out.println("");
			System.out.println("========================================================================================================");
			System.out.println("=================Reading Values ========" + "\tfrom\t" + Constants.INPUT_FILE + "=============" );
			System.out.println("========================================================================================================");
			System.out.println("");
			while ((input_line = bufferreader.readLine()) != null) {	
				scanner = new Scanner(input_line);
				int user = scanner.nextInt();
				System.out.println("Reading User\t" + user);
				int item = scanner.nextInt();
				int rating = scanner.nextInt();
				input_user_matrix[user][item] = rating;
			}
			
			System.out.println("");
			System.out.println("");
			System.out.println("");
			
			if (new File(Constants.OUTPUT_FILE).exists()) {
				System.out.println("Deleting Existing Output File\t" + Constants.OUTPUT_FILE);
				new File(Constants.OUTPUT_FILE).delete();
			}
			System.out.println("");
			System.out.println("");
			System.out.println("");
			
			if (!new File(Constants.OUTPUT_FILE).exists()) {
				System.out.println("Creating New Output File\t" + Constants.OUTPUT_FILE);
				new File(Constants.OUTPUT_FILE).createNewFile();
			}
			bufferedWriter = new BufferedWriter(new FileWriter(new File(Constants.OUTPUT_FILE)));
			
			System.out.println("");
			System.out.println("");
			System.out.println("========================================================================================================");
			System.out.println("=======================Analysing Recommendation ========================================================");
			System.out.println("========================================================================================================");
			System.out.println("");
			
			for (int user1 = 1; user1 < input_user_matrix.length; user1++) {
				System.out.println("Analysing User\t" + user1);
				List<Integer> usersWithSimilarTasteList = new ArrayList<>();
				for (int user2 = 1; user2 < input_user_matrix.length; user2++) {
					if (user1 == user2)
						continue;

					double weightCoefficient = Constants.ZEROPOINTZERO;

					for (int rating = 1; rating < input_user_matrix[user1].length; rating++) {
						user1meancoefficient += input_user_matrix[user1][rating];
						user2meancoefficient += input_user_matrix[user2][rating];
					}
					user1meancoefficient /= input_user_matrix[user1].length - 1;
					user2meancoefficient /= input_user_matrix[user2].length - 1;

					for (int rating = 1; rating < input_user_matrix[user1].length; rating++) {
						dividend += (input_user_matrix[user1][rating] - user1meancoefficient)
								* (input_user_matrix[user2][rating] - user2meancoefficient);
						user1value += Math.pow(input_user_matrix[user1][rating] - user1meancoefficient, 2);
						user2value += Math.pow(input_user_matrix[user2][rating] - user2meancoefficient, 2);
					}
					weightCoefficient = dividend / Math.sqrt(user1value * user2value);

					if (weightCoefficient > Constants.ZEROPOINTTWENTY) {
						usersWithSimilarTasteList.add(user2);
					}
					usercoefficientmapping.put(user2, weightCoefficient);
				}
				user1_user2_coefficient_mapping.put(user1, usercoefficientmapping);
				similar_interest_sharing_users.put(user1, usersWithSimilarTasteList);
			}

			System.out.println("");
			System.out.println("");
			System.out.println("========================================================================================================");
			System.out.println("======================Generating Recommendation ========================================================");
			System.out.println("========================================================================================================");
			System.out.println("");
			
			for (int user = 1; user <= Constants.TOTAL_USER; user++) {
				System.out.println("Generating User\t" + user);
				for (int itemIndex = 1; itemIndex < Constants.TOTAL_ITEMS; itemIndex++) {
					if (input_user_matrix[user][itemIndex] == 0) {
						double numerator = 0.0, denominator = 0.0;
						for (int secondUserIndex : similar_interest_sharing_users.get(user)) {
							if (input_user_matrix[secondUserIndex][itemIndex] == 0)
								continue;
							numerator += input_user_matrix[secondUserIndex][itemIndex]
									* user1_user2_coefficient_mapping.get(user).get(secondUserIndex);
							denominator += Math.abs(user1_user2_coefficient_mapping.get(user).get(secondUserIndex));
						}
						ratingrecommendation = Math.round(numerator / denominator);
						if (ratingrecommendation < 1) {
							ratingrecommendation = 1;
						} else if (ratingrecommendation > 5) {
							ratingrecommendation = 5;
						}
						output_user_matrix[user][itemIndex] = (int) ratingrecommendation;
					} else {
						output_user_matrix[user][itemIndex] = input_user_matrix[user][itemIndex];
					}
				}

			}

			System.out.println("");
			System.out.println("");
			System.out.println("========================================================================================================");
			System.out.println("=================Populating Recommendation ========" + "\tin\t" + Constants.OUTPUT_FILE + "=============" );
			System.out.println("========================================================================================================");
			System.out.println("");
			
			for (int user = 1; user <= Constants.TOTAL_USER; user++) {
				System.out.println("Populating User\t" + user);
				for (int item = 1; item <= Constants.TOTAL_ITEMS; item++) {
					String line = user + " " + item + " " + output_user_matrix[user][item];
					bufferedWriter.write(line + "\n");
				}
			}
			System.out.println("");
			System.out.println("");
			System.out.println("========================================================================================================");
			System.out.println("========================Recommendation Sysytem Operation Completed======================================");
			System.out.println("========================================================================================================");
			System.out.println("");
			System.out.println("");

		} catch (Exception e) {
			System.err.println("Exception : " + e.getClass().getSimpleName());
			e.printStackTrace();
			System.exit(1);
		} finally {
			if (bufferreader != null) {
				try {
					bufferreader.close();
				} catch (IOException e) {
					System.err.println("Exception : " + e.getClass().getSimpleName());
					e.printStackTrace();
				}
			}

			if (bufferedWriter != null) {
				try {
					bufferedWriter.close();
				} catch (IOException e) {
					System.err.println("Exception : " + e.getClass().getSimpleName());
					e.printStackTrace();
				}
			}
		}
	}

}
