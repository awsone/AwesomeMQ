package omq.common.util;

import java.io.IOException;
import java.util.Properties;

import omq.common.message.Request;
import omq.common.message.Response;
import omq.common.util.Serializers.GsonImp;
import omq.common.util.Serializers.ISerializer;
import omq.common.util.Serializers.JavaImp;
import omq.common.util.Serializers.KryoImp;
import omq.exception.SerializerException;
import omq.server.RemoteObject;

/**
 * 
 * Serializer enables to serialize the requests and the responses of the
 * remoteObjects. This class is used to have the same serializer object a not to
 * create new instances every time they are needed.
 * 
 * @author Sergi Toda <sergi.toda@estudiants.urv.cat>
 * 
 */
public class Serializer {
	public static final String KRYO = "kryo";
	public static final String JAVA = "java";
	public static final String GSON = "gson";

	// Client serializer
	public ISerializer serializer;

	// Server serializers
	private ISerializer kryoSerializer;
	private ISerializer javaSerializer;
	private ISerializer gsonSerializer;

	private Properties env;

	public Serializer(Properties env) {
		this.env = env;
	}

	private Boolean getEnableCompression() {
		return Boolean.valueOf(env.getProperty(ParameterQueue.ENABLE_COMPRESSION, "false"));
	}

	public ISerializer getInstance() throws SerializerException {
		if (serializer == null) {
			try {
				String className = env.getProperty(ParameterQueue.PROXY_SERIALIZER, Serializer.JAVA);

				if (className == null || className.isEmpty()) {
					throw new ClassNotFoundException("Class name is null or empty.");
				}

				serializer = getInstance(className);
			} catch (Exception ex) {
				throw new SerializerException(ex.getMessage(), ex);
			}
		}

		return serializer;
	}

	public ISerializer getInstance(String type) throws SerializerException {
		if (KRYO.equals(type)) {
			if (kryoSerializer == null) {
				kryoSerializer = new KryoImp();
			}
			return kryoSerializer;
		} else if (GSON.equals(type)) {
			if (gsonSerializer == null) {
				gsonSerializer = new GsonImp();
			}
			return gsonSerializer;
		} else if (JAVA.equals(type)) {
			if (javaSerializer == null) {
				javaSerializer = new JavaImp();
			}
			return javaSerializer;
		}

		throw new SerializerException("Serializer not found.");
	}

	public byte[] serialize(String type, Object obj) throws SerializerException {
		ISerializer instance = getInstance(type);

		Boolean enableCompression = getEnableCompression();
		if (enableCompression) {
			byte[] objSerialized = instance.serialize(obj);
			try {
				return Zipper.zip(objSerialized);
			} catch (IOException e) {
				throw new SerializerException(e.getMessage(), e);
			}
		} else {
			return instance.serialize(obj);
		}
	}

	public byte[] serialize(Object obj) throws SerializerException {
		ISerializer instance = getInstance();

		Boolean enableCompression = getEnableCompression();
		if (enableCompression) {
			byte[] objSerialized = instance.serialize(obj);
			try {
				return Zipper.zip(objSerialized);
			} catch (IOException e) {
				throw new SerializerException(e.getMessage(), e);
			}
		} else {
			return instance.serialize(obj);
		}
	}

	public Request deserializeRequest(String type, byte[] bytes, RemoteObject obj) throws SerializerException {
		ISerializer instance = getInstance(type);

		Boolean enableCompression = getEnableCompression();
		if (enableCompression) {
			try {
				byte[] unZippedBytes = Zipper.unzip(bytes);
				return instance.deserializeRequest(unZippedBytes, obj);
			} catch (IOException e) {
				throw new SerializerException(e.getMessage(), e);
			}
		} else {
			return instance.deserializeRequest(bytes, obj);
		}
	}

	public Response deserializeResponse(byte[] bytes, Class<?> type) throws SerializerException {
		ISerializer instance = getInstance();

		Boolean enableCompression = getEnableCompression();
		if (enableCompression) {
			try {
				byte[] unZippedBytes = Zipper.unzip(bytes);
				return instance.deserializeResponse(unZippedBytes, type);
			} catch (IOException e) {
				throw new SerializerException(e.getMessage(), e);
			}
		} else {
			return instance.deserializeResponse(bytes, type);
		}
	}

}