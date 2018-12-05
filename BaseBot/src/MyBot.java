import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

import lemon.halite3.strategy.Strategy;
import lemon.halite3.strategy.generator.GeneratorStrategy;
import lemon.halite3.util.Benchmark;
import lemon.halite3.util.DebugLog;
import lemon.halite3.util.GameConstants;
import lemon.halite3.util.GameMap;
import lemon.halite3.util.Networking;

public class MyBot {
	public static final SimpleDateFormat READABLE_DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
	public static final SimpleDateFormat FILENAME_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd--HH-mm-ss");

	public static void main(String[] args) {
		boolean debuglog = false;
		double timeout = 1900.0;
		if (args.length > 0) {
			try {
				debuglog = Boolean.parseBoolean(args[0]);
				timeout = Double.parseDouble(args[1]);
			} catch (ArrayIndexOutOfBoundsException ex) {
				// Ignore
			}
		}
		try (Benchmark benchmark = new Benchmark()) {
			GameConstants.populateConstants(Networking.readLine());
			GameMap gameMap = new GameMap();
			gameMap.parseStart();
			Date currentDate = new Date();
			if (debuglog) {
				DebugLog.initialize(String.format("logs/%s-%d.log", FILENAME_DATE_FORMAT.format(currentDate), gameMap.getMyPlayerId()));
			}
			DebugLog.log(String.format("Initialization - %s - %d", READABLE_DATE_FORMAT.format(currentDate), gameMap.getMyPlayerId()));
			Strategy strategy = new GeneratorStrategy();
			DebugLog.log(String.format("Executing Strategy: %s - Timeout: %f", strategy.getClass().getSimpleName(), timeout));
			gameMap.ready(strategy.init(gameMap, timeout));
			strategy.run(gameMap);
		}
		Scanner scanner = new Scanner(System.in);
		while (scanner.hasNextLine()) {
			DebugLog.log(scanner.nextLine());
		}
		scanner.close();
	}
}
