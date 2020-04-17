package omq.common.util.Serializers;

import omq.common.message.Request;
import omq.common.message.Response;
import omq.exception.SerializerException;
import omq.server.RemoteObject;

/**
 * An ISerializer object can serialize any kind of objects and deserialize
 * Requests and Responses.
 * 
 * @author Sergi Toda <sergi.toda@estudiants.urv.cat>
 * 
 */
public interface ISerializer {
	/**
	 * Serialize
	 * 
	 * @param obj
	 *            - object to serialize
	 * @return objectSerialized
	 * @throws SerializerException
	 *             - If the serialization failed
	 */
	public byte[] serialize(Object obj) throws SerializerException;

	/**
	 * Deserialize a Request
	 * 
	 * @param bytes
	 *            - serialized request
	 * @param obj
	 *            - remoteObject which is receiving requests
	 * @return request
	 * @throws SerializerException
	 *             - If the serialization failed
	 */
	public Request deserializeRequest(byte[] bytes, RemoteObject obj) throws SerializerException;

	/**
	 * Deserialize a Response
	 * 
	 * @param bytes
	 *            serialized response
	 * @param type
	 *            - return type expected
	 * @return response
	 * @throws SerializerException
	 *             - If the serialization failed
	 */
	public Response deserializeResponse(byte[] bytes, Class<?> type) throws SerializerException;
}
