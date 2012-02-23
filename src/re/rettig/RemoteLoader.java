package re.rettig;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/***
 * Android
 * @author andreasrettig
 *
 */

public class RemoteLoader {
	Processor processor;

	public RemoteDescription load(String name) throws IOException {
		Scanner scanner = new Scanner(new FileInputStream(name));
		RemoteDescription remoteDescription = new RemoteDescription();
		try {
			while (scanner.hasNextLine()) {
				String trimmed = scanner.nextLine().trim();
				processLine(trimmed, remoteDescription);
			}
		} finally {
			scanner.close();
		}
		return remoteDescription;
	}

	private void processLine(String nextLine, RemoteDescription remoteDescription) {
		if (nextLine.startsWith("begin remote")) {
			processor = remoteProcessor;
		}
		if (processor != null) {
			processor.process(nextLine, remoteDescription);
		}

	}

	public String[] split(String string) {
		return string.trim().split("(\\s+)");
	}

	public Integer[] parseInts(String[] strings){
		if (strings.length!=3){
			throw new IllegalArgumentException("Must be 3 Strings long");
		}
		return new Integer[]{Integer.parseInt(strings[1]),Integer.parseInt(strings[2])};
	}

	/***
	 * Processes a Remote Description File
	 */
	Processor remoteProcessor = new Processor() {

		public void process(String line, RemoteDescription remoteDescription) {
			if (line.startsWith("end remote")) {
				processor = null;
			} else if (line.startsWith("header")) {
				String[] values = split(line);
				remoteDescription.header = parseInts(values);
			} else if (line.startsWith("name")) {
				remoteDescription.name = split(line)[1];
			} else if (line.startsWith("zero")) {
				String[] values = split(line);
				remoteDescription.zero = parseInts(values);
			} else if (line.startsWith("one")) {
				String[] values = split(line);
				remoteDescription.one = parseInts(values);
			} else if (line.startsWith("pre_data ")) {
				String data = split(line)[1];
				remoteDescription.preBitsData = toByteArray(data);
			} else if (line.startsWith("post_data ")) {
				String data = split(line)[1];
				remoteDescription.postBitsData = toByteArray(data);
			} else if (line.startsWith("ptrail ")) {
				String data = split(line)[1];
				remoteDescription.ptrail = Integer.parseInt(data);
			} else if (line.startsWith("begin codes")) {
				processor = codeProcessor;
			} else if (line.startsWith("begin raw_codes")) {
				processor = rawProcessor;
			} 

		}
	};

	/***
	 * Processes a Code block
	 */
	Processor codeProcessor = new Processor() {

		public void process(String line, RemoteDescription remoteDescription) {
			if (line.startsWith("end codes")) {
				processor = remoteProcessor;
			} else {
				String[] splitted = split(line);
				if (splitted.length >= 2){
					remoteDescription.codes.put(splitted[0], new HexCode(toByteArray(splitted[1])));
				}
			}
		}
	};

	/***
	 * Processes a raw_code block
	 */
	Processor rawProcessor = new Processor() {
		String name;
		List<Integer> pulses = new ArrayList<Integer>();

		public void process(String line, RemoteDescription remoteDescription) {
			if (line.startsWith("end raw_codes")) {
				if (pulses.size()>0 & name!=null){
					remoteDescription.codes.put(name, new RawCode(pulses));
				}
				processor = remoteProcessor;
			} else if (line.startsWith("name")) {
				if (pulses.size()>0 & name!=null){
					remoteDescription.codes.put(name, new RawCode(pulses));
				}
				pulses = new ArrayList<Integer>();
				name = line.split(" ")[1];
			} else {
				pulses.addAll(toIntList(line));
			}
		}
	};


	public Byte[] toByteArray(String hexString){
		String hex = hexString.substring(2, hexString.length());

		Byte[] buffer = new Byte[hex.length()/2];
		int bufI = 0;
		for (int i = 0;i<hex.length();i+=2){
			buffer[bufI++] = (byte) Integer.parseInt(hex.substring(i,i+2),16);
		}
		return buffer;
	}

	protected List<Integer> toIntList(String string) {
		List<Integer> result = new ArrayList<Integer>();
		for (String n:split(string)){
			if (n.length()>0)result.add(Integer.parseInt(n));
		}
		return result;
	}

}

interface Processor {
	public void process(String line, RemoteDescription remoteDescription);
}
