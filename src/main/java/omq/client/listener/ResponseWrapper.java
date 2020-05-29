package omq.client.listener;

public class ResponseWrapper implements IResponseWrapper {
	private byte[] result;

	public ResponseWrapper(byte[] result) {
		this.result = result;
	}

	public byte[] getResult() {
		return result;
	}

	public void setResult(byte[] result) {
		this.result = result;
	}

}