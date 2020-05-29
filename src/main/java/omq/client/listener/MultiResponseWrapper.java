package omq.client.listener;

import java.util.ArrayList;
import java.util.List;

public class MultiResponseWrapper implements IResponseWrapper {
	private List<byte[]> bytes;

	public MultiResponseWrapper(byte[] result) {
		bytes = new ArrayList<byte[]>();
		bytes.add(result);
	}

	@Override
	public void setResult(byte[] result) {
		bytes.add(result);
	}

	@Override
	public List<byte[]> getResult() {
		return bytes;
	}

}