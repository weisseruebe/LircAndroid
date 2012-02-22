package re.rettig;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;



public class RemoteDescription {
	int freq = 38000;
	
	//Apple	  
	//	Integer[] header = {9065,4484};
	//	Integer[] one    = {574,1668};
	//	Integer[] zero   = {574,574};
	//	Integer[] repeat = {9031,2242};
	//Apple
	//Byte[] preBitsData = {0x77,(byte) 0xE1};
	//Byte[] postBitsData = {(byte) 0x97};
	//int ptrail = 567;
	
	//Pana 0239

	Integer[] header = {3437,1634};
	Integer[] one    = {500,1200};
	Integer[] zero   = {500,345};
	Byte[] preBitsData = {(byte) 0x40,(byte) 0x04, 0x0D};
	Byte[] postBitsData = {};
	int ptrail = 479;
	
	int preBits = 16;
	int postBits = 8;
	int gap = 37600;
	int togglebit = 0;

	HashMap<String, Byte[]> codes = new HashMap<String, Byte[]>();

	private List<Integer> bit2pulses(boolean b){
		return b ? Arrays.asList(one) : Arrays.asList(zero);
	}

	public RemoteDescription() {
		
	}

	public List<Integer> getPulses(String code) throws Exception{

		Byte[] dataPulses = codes.get(code);
		if (dataPulses == null){
			throw new Exception("Code not available");
		}
		List<Integer> pulses = new ArrayList<Integer>();
		pulses.addAll(Arrays.asList(header));

		pulses.addAll(code2Pulses(preBitsData));
		pulses.addAll(code2Pulses(dataPulses));
		pulses.addAll(code2Pulses(postBitsData));
		pulses.add(ptrail);
		return pulses;
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
		return buffer.toString();
	}
}
