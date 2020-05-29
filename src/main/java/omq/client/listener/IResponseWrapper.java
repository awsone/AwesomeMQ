package omq.client.listener;

public interface IResponseWrapper {
	public void setResult(byte[] result);

	public Object getResult();
}