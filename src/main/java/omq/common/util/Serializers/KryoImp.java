package omq.common.util.Serializers;

import java.io.ByteArrayOutputStream;

import omq.common.message.Request;
import omq.common.message.Response;
import omq.exception.SerializerException;
import omq.server.RemoteObject;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * Kryo serializerimplementation. It uses the Kryo libraries.
 * 
 * @author Sergi Toda <sergi.toda@estudiants.urv.cat>
 * 
 */
public class KryoImp implements ISerializer {
	private final Kryo kryo = new Kryo();

	@Override
	public byte[] serialize(Object obj) throws SerializerException {
		try {
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			Output output = new Output(stream);
			kryo.writeObject(output, obj);

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
		return (Request) deserializeObject(bytes, Request.class);
	}

	@Override
	public Response deserializeResponse(byte[] bytes, Class<?> type) throws SerializerException {
		return (Response) deserializeObject(bytes, Response.class);
	}

	public Object deserializeObject(byte[] bytes, Class<?> type) throws SerializerException {
		try {
			Input input = new Input(bytes);
			Object obj = kryo.readObject(input, type);

			input.close();
			return obj;
		} catch (Exception e) {
			throw new SerializerException("Deserialize -> " + e.getMessage(), e);
		}
	}

}
