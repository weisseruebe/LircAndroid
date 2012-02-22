package re.rettig;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Scanner;

public class RemoteLoader {
	private static final int DEFAULT_REPEAT = 0;
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

	private void processLine(String nextLine,
			RemoteDescription remoteDescription) {
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

	Processor remoteProcessor = new Processor() {

		public void process(String line, RemoteDescription remoteDescription) {
			if (line.startsWith("end remote")) {
				processor = null;
			} else if (line.startsWith("header")) {
				String[] values = split(line);
				remoteDescription.header = parseInts(values);
			} else if (line.startsWith("name")) {
				System.out.println("Name " + line);
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
			} 

		}
	};

	Processor codeProcessor = new Processor() {

		public void process(String line, RemoteDescription remoteDescription) {
			if (line.startsWith("end codes")) {
				processor = remoteProcessor;
			} else {
				String[] splitted = split(line);
				if (splitted.length == 2){
					remoteDescription.codes.put(splitted[0], toByteArray(splitted[1]));
				}
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
	
	public static void main(String[] args) throws IOException {
		RemoteLoader remoteLoader = new RemoteLoader();
		RemoteDescription desc = remoteLoader.load("/Users/andreasrettig/Desktop/remotes/apple/A1156");
	}

}

interface Processor {
	public void process(String line, RemoteDescription remoteDescription);
}
