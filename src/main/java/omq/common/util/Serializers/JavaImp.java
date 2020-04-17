package omq.common.util.Serializers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import omq.common.message.Request;
import omq.common.message.Response;
import omq.exception.SerializerException;
import omq.server.RemoteObject;

/**
 * Java serialize implementation. It uses the default java serialization.
 * 
 * @author Sergi Toda <sergi.toda@estudiants.urv.cat>
 * 
 */
public class JavaImp implements ISerializer {

	@Override
	public byte[] serialize(Object obj) throws SerializerException {
		try {
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			ObjectOutputStream output = new ObjectOutputStream(stream);
			output.writeObject(obj);

			output.flush();
			output.close();

			byte[] bArray = stream.toByteArray();

			stream.flush();
			stream.close();

			return bArray;
		} catch (Exception e) {
			throw new SerializerException("Serialize -> " + e.getMessage(), e);
		}
	}

	@Override
	public Request deserializeRequest(byte[] bytes, RemoteObject obj) throws SerializerException {
		return (Request) deserializeObject(bytes);
	}

	@Override
	public Response deserializeResponse(byte[] bytes, Class<?> type) throws SerializerException {
		return (Response) deserializeObject(bytes);
	}

	public Object deserializeObject(byte[] bytes) throws SerializerException {
		try {
			ByteArrayInputStream input = new ByteArrayInputStream(bytes);
			ObjectInputStream objInput = new ObjectInputStream(input);

			Object obj = objInput.readObject();

			objInput.close();
			input.close();

			return obj;
		} catch (Exception e) {
			throw new SerializerException("Deserialize -> " + e.getMessage(), e);
		}
	}

}
