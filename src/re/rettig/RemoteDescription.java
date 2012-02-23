package re.rettig;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class RemoteDescription {
	int freq = 38000;

	String name = "";

	Integer[] header = {0,0};
	Integer[] one    = {0,0};
	Integer[] zero   = {0,0};
	Byte[] preBitsData = {(byte) 0x40,(byte) 0x04, 0x0D};
	Byte[] postBitsData = {};
	int ptrail = 479;

	int preBits = 16;
	int postBits = 8;
	int gap = 37600;
	int togglebit = 0;

	HashMap<String, Code> codes = new HashMap<String, Code>();

	private List<Integer> bit2pulses(boolean b){
		return b ? Arrays.asList(one) : Arrays.asList(zero);
	}

	public List<Integer> getPulses(String command) throws Exception{
		if (!codes.containsKey(command)){
			throw new Exception("Code not available");
		}
		Code code = codes.get(command);
		if (code.isRaw()){
			return ((RawCode)code).pulses;
		} else{

			Byte[] dataPulses = ((HexCode)code).codes;
			List<Integer> pulses = new ArrayList<Integer>();
			pulses.addAll(Arrays.asList(header));

			pulses.addAll(code2Pulses(preBitsData));
			pulses.addAll(code2Pulses(dataPulses));
			pulses.addAll(code2Pulses(postBitsData));
			pulses.add(ptrail);
			return pulses;
		}
	}

	private List<Integer> code2Pulses(Byte[]data){
		List<Integer> pulses = new ArrayList<Integer>();
		for (Byte b : data){
			for (int i=0;i<8;i++){
				pulses.addAll(bit2pulses((b & 0x80 >> i)>0));
			}
		}
		return pulses;
	}

	public String pulsesToString(String code){
		StringBuffer buffer = new StringBuffer();
		try {
			buffer.append("Code "+code+":");
			for (Integer b:getPulses(code)){
				buffer.append(b+" ");
			}
			buffer.append("Length "+getPulses(code).size());
		} catch (Exception e) {
			return "Unknown code: "+code;
		}
		return buffer.toString();
	}

	public String toString(){
		StringBuffer buffer = new StringBuffer();
		buffer.append("Freq "+freq);
		buffer.append("Header "+header[0]+" "+header[1]);
		for (String cmd:codes.keySet()){
			buffer.append(cmd+": ");
			buffer.append(pulsesToString(cmd));
			buffer.append("\n");
		}
		return buffer.toString();
	}
}
